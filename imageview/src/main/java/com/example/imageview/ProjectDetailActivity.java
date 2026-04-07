package com.example.imageview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProjectDetailActivity extends AppCompatActivity {

    private TextView tvProjectTitle, tvProjectTag, tvProjectTime, tvProjectRemark;
    private Button btnBack, btnEdit, btnAddSubtask;
    private ListView listSubtasks;
    private TodoAdapter adapter;
    private List<TodoItem> subtaskList = new ArrayList<>();
    private AppDatabase database;
    private ProjectDao projectDao;
    private TodoDao todoDao;
    private Project currentProject;
    private long projectId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_detail);

        // 初始化视图
        initViews();

        // 初始化数据库
        initDatabase();

        // 获取项目ID
        projectId = getIntent().getLongExtra("project_id", -1);
        if (projectId != -1) {
            // 加载项目信息和子任务
            loadProjectInfo();
            loadSubtasks();
        } else {
            Toast.makeText(this, "项目ID无效", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 设置点击事件
        setClickListeners();
    }

    private void initViews() {
        tvProjectTitle = findViewById(R.id.tv_project_title);
        tvProjectTag = findViewById(R.id.tv_project_tag);
        tvProjectTime = findViewById(R.id.tv_project_time);
        tvProjectRemark = findViewById(R.id.tv_project_remark);
        btnBack = findViewById(R.id.btn_back);
        btnEdit = findViewById(R.id.btn_edit);
        btnAddSubtask = findViewById(R.id.btn_add_subtask);
        listSubtasks = findViewById(R.id.list_subtasks);
    }

    private void initDatabase() {
        database = AppDatabase.getInstance(this);
        projectDao = database.projectDao();
        todoDao = database.todoDao();
    }

    private void loadProjectInfo() {
        executor.execute(() -> {
            currentProject = projectDao.getById(projectId);
            if (currentProject != null) {
                runOnUiThread(() -> {
                    tvProjectTitle.setText(currentProject.getTitle());
                    tvProjectTag.setText(currentProject.getTag());
                    String timeText = currentProject.getStartTime() + " - " + currentProject.getEndTime();
                    tvProjectTime.setText(timeText);
                    tvProjectRemark.setText(currentProject.getRemark());
                });
            }
        });
    }

    private void loadSubtasks() {
        executor.execute(() -> {
            List<TodoItem> todos = todoDao.getByProjectId(projectId);
            subtaskList.clear();
            subtaskList.addAll(todos);
            runOnUiThread(() -> {
                adapter = new TodoAdapter(this, subtaskList, new TodoAdapter.OnTodoCheckedListener() {
                    @Override
                    public void onCheckedChanged(TodoItem item, boolean checked) {
                        item.setCompleted(checked);
                        executor.execute(() -> todoDao.setCompleted(item.getId(), checked));
                    }
                });
                listSubtasks.setAdapter(adapter);
            });
        });
    }

    private void setClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 编辑按钮
        btnEdit.setOnClickListener(v -> showEditProjectDialog());

        // 添加子任务按钮
        btnAddSubtask.setOnClickListener(v -> showAddSubtaskDialog());
    }

    private void showEditProjectDialog() {
        if (currentProject == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_project, null, false);
        EditText editTitle = dialogView.findViewById(R.id.edit_project_title);
        EditText editStartTime = dialogView.findViewById(R.id.edit_project_start_time);
        EditText editEndTime = dialogView.findViewById(R.id.edit_project_end_time);
        EditText editTag = dialogView.findViewById(R.id.edit_project_tag);
        EditText editRemark = dialogView.findViewById(R.id.edit_project_remark);

        // 填充现有数据
        editTitle.setText(currentProject.getTitle());
        editStartTime.setText(currentProject.getStartTime());
        editEndTime.setText(currentProject.getEndTime());
        editTag.setText(currentProject.getTag());
        editRemark.setText(currentProject.getRemark());

        // 为时间输入框添加日历选择器
        setupDatePicker(editStartTime);
        setupDatePicker(editEndTime);

        new AlertDialog.Builder(this)
                .setTitle("编辑项目")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    String startTime = editStartTime.getText().toString().trim();
                    String endTime = editEndTime.getText().toString().trim();
                    String tag = editTag.getText().toString().trim();
                    String remark = editRemark.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "请输入项目标题", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 更新项目信息
                    currentProject.setTitle(title);
                    currentProject.setStartTime(startTime);
                    currentProject.setEndTime(endTime);
                    currentProject.setTag(tag);
                    currentProject.setRemark(remark);

                    // 保存到数据库
                    executor.execute(() -> {
                        projectDao.update(currentProject);
                        runOnUiThread(() -> {
                            // 更新UI
                            tvProjectTitle.setText(title);
                            tvProjectTag.setText(tag);
                            String timeText = startTime + " - " + endTime;
                            tvProjectTime.setText(timeText);
                            tvProjectRemark.setText(remark);
                            Toast.makeText(this, "项目已更新", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showAddSubtaskDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_todo, null, false);
        EditText editTitle = dialogView.findViewById(R.id.edit_todo_title);
        EditText editStartTime = dialogView.findViewById(R.id.edit_todo_start_time);
        EditText editEndTime = dialogView.findViewById(R.id.edit_todo_end_time);
        EditText editTag = dialogView.findViewById(R.id.edit_todo_tag);
        EditText editDescription = dialogView.findViewById(R.id.edit_todo_description);

        // 为时间输入框添加日历选择器
        setupDatePicker(editStartTime);
        setupDatePicker(editEndTime);

        new AlertDialog.Builder(this)
                .setTitle("添加子任务")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    String startTime = editStartTime.getText().toString().trim();
                    String endTime = editEndTime.getText().toString().trim();
                    String tag = editTag.getText().toString().trim();
                    String description = editDescription.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "请输入子任务标题", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 创建新的待办事项作为子任务
                    TodoItem newItem = new TodoItem(
                            title,
                            description, // 将描述作为content存储
                            "刚刚", // 默认时间为"刚刚"
                            false, // 默认未完成
                            startTime,
                            endTime,
                            currentProject.getTitle(),
                            tag,
                            description,
                            projectId
                    );

                    // 插入到数据库
                    executor.execute(() -> {
                        long id = todoDao.insert(newItem);
                        newItem.setId(id);
                        runOnUiThread(() -> {
                            subtaskList.add(newItem);
                            adapter.notifyItemInserted(subtaskList.size() - 1);
                            Toast.makeText(this, "已添加子任务", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void setupDatePicker(final EditText editText) {
        // 设置点击事件
        editText.setOnClickListener(v -> showDatePicker(editText));
        
        // 设置焦点变化事件
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker(editText);
            }
        });
    }

    private void showDatePicker(final EditText editText) {
        // 获取当前日期
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        int year = calendar.get(java.util.Calendar.YEAR);
        int month = calendar.get(java.util.Calendar.MONTH);
        int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

        // 创建日期选择器对话框
        android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    // 格式化日期为 yyyy-MM-dd 格式
                    String date = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                    editText.setText(date);
                },
                year, month, day
        );

        // 显示日期选择器对话框
        datePickerDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}