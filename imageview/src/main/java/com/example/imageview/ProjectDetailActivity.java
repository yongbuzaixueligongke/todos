package com.example.imageview;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ProjectDetailActivity extends AppCompatActivity {

    private TextView tvProjectTitle;
    private TextView tvProjectTime;
    private TextView tvProjectRemainingDays;
    private TextView tvProjectRemark;
    private ProjectProgressRingView projectProgressRing;
    private ImageButton btnBack;
    private ImageButton btnMore;
    private View btnAddSubtask;
    private RecyclerView listSubtasks;
    private final List<TodoItem> subtaskList = new ArrayList<>();
    private final Map<Long, Integer> pomodoroCounts = new HashMap<>();
    private ProjectRepository projectRepository;
    private TodoRepository todoRepository;
    private PomodoroRecordStore pomodoroRecordStore;
    private Project currentProject;
    private long projectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccountGuard.requireLogin(this)) {
            return;
        }
        setContentView(R.layout.activity_project_detail);

        initViews();
        initDatabase();

        projectId = getIntent().getLongExtra(NavigationConstants.EXTRA_PROJECT_ID, -1L);
        if (projectId != -1L) {
            loadProjectInfo();
            loadSubtasks();
        } else {
            Toast.makeText(this, "Invalid project id", Toast.LENGTH_SHORT).show();
            finish();
        }

        setClickListeners();
    }

    private void initViews() {
        tvProjectTitle = findViewById(R.id.tv_project_title);
        tvProjectTime = findViewById(R.id.tv_project_time);
        tvProjectRemainingDays = findViewById(R.id.tv_project_remaining_days);
        tvProjectRemark = findViewById(R.id.tv_project_remark);
        projectProgressRing = findViewById(R.id.project_progress_ring);
        btnBack = findViewById(R.id.btn_back);
        btnMore = findViewById(R.id.btn_more);
        btnAddSubtask = findViewById(R.id.btn_add_subtask);
        listSubtasks = findViewById(R.id.list_subtasks);
        listSubtasks.setLayoutManager(new LinearLayoutManager(this));
    }

    private void initDatabase() {
        projectRepository = new ProjectRepository(this);
        todoRepository = new TodoRepository(this);
        pomodoroRecordStore = new PomodoroRecordStore(this);
    }

    private void loadProjectInfo() {
        projectRepository.getById(projectId, project -> {
            currentProject = project;
            if (currentProject != null) {
                updateProjectSummary();
            }
        });
    }

    private void loadSubtasks() {
        todoRepository.getByProjectId(projectId, todos -> {
            subtaskList.clear();
            subtaskList.addAll(todos);
            refreshPomodoroCounts();
            ProjectSubtaskAdapter adapter = new ProjectSubtaskAdapter(
                    subtaskList,
                    (item, checked) -> {
                        item.setCompleted(checked);
                        todoRepository.setCompleted(item.getId(), checked);
                        updateProjectProgress();
                    },
                    item -> showTaskEditor(item.getId()),
                    this::deleteSubtask,
                    this::showPomodoro,
                    pomodoroCounts
            );
            listSubtasks.setAdapter(adapter);
            updateProjectProgress();
        });
    }

    private void refreshPomodoroCounts() {
        pomodoroCounts.clear();
        if (pomodoroRecordStore == null) {
            return;
        }
        for (TodoItem item : subtaskList) {
            int count = pomodoroRecordStore.getCompletedCount(item.getId());
            if (count > 0) {
                pomodoroCounts.put(item.getId(), count);
            }
        }
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnMore.setOnClickListener(this::showProjectMenu);
        btnAddSubtask.setOnClickListener(v -> showTaskEditor(-1L));
    }

    private void showProjectMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(this, anchor);
        popupMenu.getMenuInflater().inflate(R.menu.project_detail_menu, popupMenu.getMenu());
        forceShowMenuIcons(popupMenu);
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_edit_project) {
                showEditProjectDialog();
                return true;
            }
            if (item.getItemId() == R.id.action_delete_project) {
                showDeleteProjectDialog();
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    private void forceShowMenuIcons(PopupMenu popupMenu) {
        try {
            java.lang.reflect.Field field = PopupMenu.class.getDeclaredField("mPopup");
            field.setAccessible(true);
            Object helper = field.get(popupMenu);
            helper.getClass().getDeclaredMethod("setForceShowIcon", boolean.class).invoke(helper, true);
        } catch (Exception ignored) {
        }
    }

    private void showDeleteProjectDialog() {
        if (currentProject == null) {
            Toast.makeText(this, "Project not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Project")
                .setMessage("Delete this project and all subtasks?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCurrentProject())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCurrentProject() {
        projectRepository.deleteWithSubtasks(projectId, ignored -> {
            Toast.makeText(this, "Project deleted", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void showEditProjectDialog() {
        if (currentProject == null) {
            return;
        }

        ProjectEditorBottomSheet sheet = ProjectEditorBottomSheet.newEditInstance(currentProject);
        sheet.setProjectEditorListener(project -> {
            currentProject.setTitle(project.getTitle());
            currentProject.setStartTime(project.getStartTime());
            currentProject.setEndTime(project.getEndTime());
            currentProject.setTag(project.getTag());
            currentProject.setRemark(project.getRemark());

            projectRepository.update(currentProject, ignored -> {
                updateProjectSummary();
                Toast.makeText(this, "Project updated", Toast.LENGTH_SHORT).show();
            });
        });
        sheet.show(getSupportFragmentManager(), "project_editor");
    }

    private void showTaskEditor(long todoId) {
        TaskEditorBottomSheet sheet;
        if (todoId > 0L) {
            sheet = TaskEditorBottomSheet.newEditInstance(todoId);
        } else {
            sheet = TaskEditorBottomSheet.newCreateForProject(
                    projectId,
                    currentProject != null ? currentProject.getTitle() : ""
            );
        }
        sheet.setTaskEditorListener(new TaskEditorBottomSheet.TaskEditorListener() {
            @Override
            public void onTaskSaved() {
                loadSubtasks();
            }

            @Override
            public void onTaskDeleted() {
                loadSubtasks();
            }
        });
        sheet.show(getSupportFragmentManager(), "task_editor");
    }

    private void deleteSubtask(TodoItem item) {
        if (item == null) {
            return;
        }
        ReminderScheduler.cancelReminder(getApplicationContext(), item.getId());
        if (pomodoroRecordStore != null) {
            pomodoroRecordStore.clear(item.getId());
        }
        todoRepository.deleteById(item.getId(), ignored -> {
            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
            loadSubtasks();
        });
    }

    private void showPomodoro(TodoItem item) {
        if (item == null) {
            return;
        }
        PomodoroBottomSheet sheet = PomodoroBottomSheet.newInstance(item.getTitle());
        sheet.setPomodoroListener(() -> recordCompletedPomodoro(item));
        sheet.show(getSupportFragmentManager(), "pomodoro");
    }

    private void recordCompletedPomodoro(TodoItem item) {
        if (item == null || pomodoroRecordStore == null) {
            return;
        }
        int count = pomodoroRecordStore.incrementCompletedCount(item.getId());
        pomodoroCounts.put(item.getId(), count);
        if (listSubtasks.getAdapter() != null) {
            listSubtasks.getAdapter().notifyDataSetChanged();
        }
        Toast.makeText(this, "\u5B8C\u6210 1 \u4E2A\u756A\u8304\u949F", Toast.LENGTH_SHORT).show();
    }

    private void updateProjectSummary() {
        if (currentProject == null) {
            return;
        }

        tvProjectTitle.setText(safeText(currentProject.getTitle(), "Project"));
        tvProjectTime.setText(formatProjectRange(currentProject.getStartTime(), currentProject.getEndTime()));
        tvProjectRemainingDays.setText(formatRemainingDays(currentProject.getEndDateMillis()));

        String remark = safeText(currentProject.getRemark(), "");
        tvProjectRemark.setText(remark);
        tvProjectRemark.setVisibility(remark.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void updateProjectProgress() {
        if (projectProgressRing == null) {
            return;
        }

        int total = subtaskList.size();
        if (total == 0) {
            projectProgressRing.setProgressPercent(0);
            return;
        }

        int completed = 0;
        for (TodoItem item : subtaskList) {
            if (item.isCompleted()) {
                completed++;
            }
        }
        projectProgressRing.setProgressPercent(Math.round(completed * 100f / total));
    }

    private String formatProjectRange(String startTime, String endTime) {
        String start = safeText(startTime, "");
        String end = safeText(endTime, "");
        if (start.isEmpty() && end.isEmpty()) {
            return "No schedule";
        }
        if (start.isEmpty()) {
            return end;
        }
        if (end.isEmpty()) {
            return start;
        }
        return start + " - " + end;
    }

    private String formatRemainingDays(long endDateMillis) {
        if (endDateMillis <= 0L) {
            return "No end date";
        }

        Calendar today = Calendar.getInstance();
        startOfDay(today);
        Calendar endDay = Calendar.getInstance();
        endDay.setTimeInMillis(endDateMillis);
        startOfDay(endDay);

        long diffMillis = endDay.getTimeInMillis() - today.getTimeInMillis();
        long days = TimeUnit.MILLISECONDS.toDays(diffMillis);
        if (days > 0) {
            return "\u8FD8\u5269 " + days + " \u5929";
        }
        if (days == 0) {
            return "\u4ECA\u5929\u622A\u6B62";
        }
        return "\u5DF2\u8D85\u671F " + Math.abs(days) + " \u5929";
    }

    private void startOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private String safeText(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}
