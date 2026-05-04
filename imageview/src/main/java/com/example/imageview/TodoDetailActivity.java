package com.example.imageview;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TodoDetailActivity extends AppCompatActivity {

    private TextView tvTodoTitle;
    private TextView tvTodoTime;
    private TextView tvTodoProject;
    private TextView tvTodoTag;
    private TextView tvTodoDescription;
    private Button btnEdit;
    private Button btnBack;
    private Button btnDelete;
    private TodoItem currentTodo;
    private long todoId;
    private AppDatabase database;
    private TodoDao todoDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccountGuard.requireLogin(this)) {
            return;
        }
        setContentView(R.layout.activity_todo_detail);

        initViews();
        initDatabase();

        todoId = getIntent().getLongExtra(NavigationConstants.EXTRA_TODO_ID, -1L);
        if (todoId != -1L) {
            loadTodoInfo();
        } else {
            Toast.makeText(this, "Invalid todo id", Toast.LENGTH_SHORT).show();
            finish();
        }

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
        btnDelete = findViewById(R.id.btn_delete);
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
                    PriorityTagUtils.applyTagIcon(tvTodoTag, currentTodo.getTag());
                    tvTodoDescription.setText(currentTodo.getDescription());
                });
            }
        });
    }

    private void setClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnEdit.setOnClickListener(v -> showTaskEditor());
        btnDelete.setOnClickListener(v -> showDeleteTodoDialog());
    }

    private void showDeleteTodoDialog() {
        if (currentTodo == null) {
            Toast.makeText(this, "Todo is not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Todo")
                .setMessage("Are you sure you want to delete this todo?")
                .setPositiveButton("Delete", (dialog, which) -> deleteCurrentTodo())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCurrentTodo() {
        executor.execute(() -> {
            todoDao.deleteById(todoId);
            runOnUiThread(() -> {
                Toast.makeText(this, "Todo deleted", Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    private void showTaskEditor() {
        TaskEditorBottomSheet sheet = TaskEditorBottomSheet.newEditInstance(todoId);
        sheet.setTaskEditorListener(new TaskEditorBottomSheet.TaskEditorListener() {
            @Override
            public void onTaskSaved() {
                loadTodoInfo();
            }

            @Override
            public void onTaskDeleted() {
                finish();
            }
        });
        sheet.show(getSupportFragmentManager(), "task_editor");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
