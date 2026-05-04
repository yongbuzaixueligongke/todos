package com.example.imageview;

import android.content.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectRepository {

    public static class ProjectListResult {
        private final List<Project> projects;
        private final Map<Long, Integer> subtaskCountMap;

        public ProjectListResult(List<Project> projects, Map<Long, Integer> subtaskCountMap) {
            this.projects = projects;
            this.subtaskCountMap = subtaskCountMap;
        }

        public List<Project> getProjects() {
            return projects;
        }

        public Map<Long, Integer> getSubtaskCountMap() {
            return subtaskCountMap;
        }
    }

    private final ProjectDao projectDao;
    private final TodoDao todoDao;

    public ProjectRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context.getApplicationContext());
        projectDao = database.projectDao();
        todoDao = database.todoDao();
    }

    public void getAll(DatabaseExecutor.Callback<List<Project>> callback) {
        DatabaseExecutor.execute(projectDao::getAll, callback);
    }

    public void getById(long projectId, DatabaseExecutor.Callback<Project> callback) {
        DatabaseExecutor.execute(() -> projectDao.getById(projectId), callback);
    }

    public void getAllWithSubtaskCounts(DatabaseExecutor.Callback<ProjectListResult> callback) {
        DatabaseExecutor.execute(() -> {
            List<Project> projects = projectDao.getAll();
            if (projects.isEmpty()) {
                addDefaultProjects();
                projects = projectDao.getAll();
            }

            Map<Long, Integer> countMap = new HashMap<>();
            List<ProjectTaskCount> projectTaskCounts = todoDao.getProjectTaskCounts();
            for (ProjectTaskCount projectTaskCount : projectTaskCounts) {
                if (projectTaskCount != null && projectTaskCount.projectId > 0L) {
                    countMap.put(projectTaskCount.projectId, projectTaskCount.taskCount);
                }
            }
            return new ProjectListResult(projects, countMap);
        }, callback);
    }

    public void insert(Project project, DatabaseExecutor.Callback<Long> callback) {
        DatabaseExecutor.execute(() -> projectDao.insert(project), callback);
    }

    public void update(Project project, DatabaseExecutor.Callback<Boolean> callback) {
        DatabaseExecutor.execute(() -> {
            projectDao.update(project);
            return true;
        }, callback);
    }

    public void deleteWithSubtasks(long projectId, DatabaseExecutor.Callback<Boolean> callback) {
        DatabaseExecutor.execute(() -> {
            todoDao.deleteByProjectId(projectId);
            projectDao.delete(projectId);
            return true;
        }, callback);
    }

    private void addDefaultProjects() {
        Project[] defaultProjects = {
                new Project("Study Plan", "", "", "Study", "Default study project"),
                new Project("Work Tasks", "", "", "Work", "Default work project"),
                new Project("Fitness Goal", "", "", "Health", "Default fitness project"),
                new Project("Travel Plan", "", "", "Travel", "Default travel project")
        };

        for (Project project : defaultProjects) {
            projectDao.insert(project);
        }
    }
}
