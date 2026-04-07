package com.example.imageview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoDetailActivity extends AppCompatActivity {

    private TextView tvTodoTitle, tvTodoTime, tvTodoProject, tvTodoTag, tvTodoDescription;
    private Button btnEdit, btnBack;
    private TodoItem currentTodo;
    private long todoId;
    private AppDatabase database;
    private TodoDao todoDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_todo_detail);

        // 初始化视图
        initViews();

        // 初始化数据库
        initDatabase();

        // 获取待办ID
        todoId = getIntent().getLongExtra("todo_id", -1);
        if (todoId != -1) {
            // 加载待办信息
            loadTodoInfo();
        } else {
            Toast.makeText(this, "待办ID无效", Toast.LENGTH_SHORT).show();
            finish();
        }

        // 设置点击事件
        setClickListeners();
    }

    private void initViews() {
        tvTodoTitle = findViewById(R.id.tv_todo_title);
        tvTodoTime = findViewById(R.id.tv_todo_time);
        tvTodoProject = findViewById(R.id.tv_todo_project);
        tvTodoTag = findViewById(R.id.tv_todo_tag);
        tvTodoDescription = findViewById(R.id.tv_todo_description);
        btnEdit = findViewById(R.id.btn_edit);
        btnBack = findViewById(R.id.btn_back);
    }

    private void initDatabase() {
        database = AppDatabase.getInstance(this);
        todoDao = database.todoDao();
    }

    private void loadTodoInfo() {
        executor.execute(() -> {
            currentTodo = todoDao.getById(todoId);
            if (currentTodo != null) {
                runOnUiThread(() -> {
                    tvTodoTitle.setText(currentTodo.getTitle());
                    tvTodoTime.setText(currentTodo.getStartTime() + " - " + currentTodo.getEndTime());
                    tvTodoProject.setText(currentTodo.getProject());
                    tvTodoTag.setText(currentTodo.getTag());
                    tvTodoDescription.setText(currentTodo.getDescription());
                });
            }
        });
    }

    private void setClickListeners() {
        // 返回按钮
        btnBack.setOnClickListener(v -> finish());

        // 编辑按钮
        btnEdit.setOnClickListener(v -> showEditTodoDialog());
    }

    private void showEditTodoDialog() {
        if (currentTodo == null) return;

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_todo, null, false);
        EditText editTitle = dialogView.findViewById(R.id.edit_todo_title);
        EditText editStartTime = dialogView.findViewById(R.id.edit_todo_start_time);
        EditText editEndTime = dialogView.findViewById(R.id.edit_todo_end_time);
        EditText editTag = dialogView.findViewById(R.id.edit_todo_tag);
        EditText editDescription = dialogView.findViewById(R.id.edit_todo_description);

        // 填充现有数据
        editTitle.setText(currentTodo.getTitle());
        editStartTime.setText(currentTodo.getStartTime());
        editEndTime.setText(currentTodo.getEndTime());
        editTag.setText(currentTodo.getTag());
        editDescription.setText(currentTodo.getDescription());

        // 为时间输入框添加日历选择器
        setupDatePicker(editStartTime);
        setupDatePicker(editEndTime);

        new AlertDialog.Builder(this)
                .setTitle("编辑待办")
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    String startTime = editStartTime.getText().toString().trim();
                    String endTime = editEndTime.getText().toString().trim();
                    String tag = editTag.getText().toString().trim();
                    String description = editDescription.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "请输入待办标题", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 更新待办信息
                    currentTodo.setTitle(title);
                    currentTodo.setStartTime(startTime);
                    currentTodo.setEndTime(endTime);
                    currentTodo.setTag(tag);
                    currentTodo.setDescription(description);

                    // 保存到数据库
                    executor.execute(() -> {
                        todoDao.update(currentTodo);
                        runOnUiThread(() -> {
                            // 更新UI
                            tvTodoTitle.setText(title);
                            tvTodoTime.setText(startTime + " - " + endTime);
                            tvTodoTag.setText(tag);
                            tvTodoDescription.setText(description);
                            Toast.makeText(this, "待办已更新", Toast.LENGTH_SHORT).show();
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