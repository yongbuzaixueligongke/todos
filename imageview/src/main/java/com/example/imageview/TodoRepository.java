package com.example.imageview;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class TodoRepository {

    private final TodoDao todoDao;
    private final ProjectDao projectDao;

    public TodoRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context.getApplicationContext());
        todoDao = database.todoDao();
        projectDao = database.projectDao();
    }

    public void getAll(DatabaseExecutor.Callback<List<TodoItem>> callback) {
        DatabaseExecutor.execute(todoDao::getAll, callback);
    }

    public void getById(long todoId, DatabaseExecutor.Callback<TodoItem> callback) {
        DatabaseExecutor.execute(() -> todoDao.getById(todoId), callback);
    }

    public void getByProjectId(long projectId, DatabaseExecutor.Callback<List<TodoItem>> callback) {
        DatabaseExecutor.execute(() -> todoDao.getByProjectId(projectId), callback);
    }

    public void getByDateRange(long startInclusive, long endExclusive, DatabaseExecutor.Callback<List<TodoItem>> callback) {
        DatabaseExecutor.execute(() -> todoDao.getByDateRange(startInclusive, endExclusive), callback);
    }

    public void getCustomTags(DatabaseExecutor.Callback<List<String>> callback) {
        DatabaseExecutor.execute(() -> {
            List<TodoItem> todos = todoDao.getAll();
            List<String> tags = new ArrayList<>();
            for (TodoItem todo : todos) {
                String tag = safe(todo.getTag()).trim();
                if (!tag.isEmpty()
                        && !PriorityTagUtils.isPriorityTag(tag)
                        && !containsIgnoreCase(tags, tag)) {
                    tags.add(tag);
                }
            }
            return tags;
        }, callback);
    }

    public void setCompleted(long todoId, boolean completed) {
        DatabaseExecutor.execute(() -> todoDao.setCompleted(todoId, completed));
    }

    public void update(TodoItem item, DatabaseExecutor.Callback<Boolean> callback) {
        DatabaseExecutor.execute(() -> {
            todoDao.update(item);
            return true;
        }, callback);
    }

    public void save(TodoItem item, DatabaseExecutor.Callback<Long> callback) {
        DatabaseExecutor.execute(() -> {
            if (item.getId() > 0L) {
                todoDao.update(item);
                return item.getId();
            }
            long id = todoDao.insert(item);
            item.setId(id);
            return id;
        }, callback);
    }

    public void saveWithProject(
            TodoItem item,
            long selectedProjectId,
            String selectedProjectName,
            String projectStartTime,
            DatabaseExecutor.Callback<Long> callback
    ) {
        DatabaseExecutor.execute(() -> {
            long actualProjectId = selectedProjectId;
            String actualProjectName = selectedProjectName;
            if (actualProjectId == 0L && actualProjectName != null && !actualProjectName.isEmpty()) {
                Project newProject = new Project(actualProjectName, projectStartTime, projectStartTime, "", "");
                actualProjectId = projectDao.insert(newProject);
            }

            item.setProjectId(actualProjectId);
            item.setProject(actualProjectName);
            if (item.getId() > 0L) {
                todoDao.update(item);
                return item.getId();
            }
            long id = todoDao.insert(item);
            item.setId(id);
            return id;
        }, callback);
    }

    public void deleteById(long todoId, DatabaseExecutor.Callback<Boolean> callback) {
        DatabaseExecutor.execute(() -> {
            todoDao.deleteById(todoId);
            return true;
        }, callback);
    }

    public void clearTag(String tag, DatabaseExecutor.Callback<Boolean> callback) {
        DatabaseExecutor.execute(() -> {
            todoDao.clearTag(tag, System.currentTimeMillis());
            return true;
        }, callback);
    }

    public void getProjectTaskCounts(DatabaseExecutor.Callback<List<ProjectTaskCount>> callback) {
        DatabaseExecutor.execute(todoDao::getProjectTaskCounts, callback);
    }

    public void deleteByProjectId(long projectId) {
        DatabaseExecutor.execute(() -> todoDao.deleteByProjectId(projectId));
    }

    private static boolean containsIgnoreCase(List<String> values, String target) {
        for (String value : values) {
            if (value.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
