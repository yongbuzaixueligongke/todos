package com.example.imageview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.myapplication.ui.projects.ProjectsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MessageActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ListView listView;
    private TodoAdapter adapter;
    private List<TodoItem> todoList = new ArrayList<>();
    private AppDatabase database;
    private TodoDao todoDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        drawerLayout = findViewById(R.id.drawer_layout);
        listView = findViewById(R.id.message_list_view);

        database = AppDatabase.getInstance(this);
        todoDao = database.todoDao();

        adapter = new TodoAdapter(this, todoList, new TodoAdapter.OnTodoCheckedListener() {
            @Override
            public void onCheckedChanged(TodoItem item, boolean checked) {
                item.setCompleted(checked);
                executor.execute(() -> todoDao.setCompleted(item.getId(), checked));
            }
        });
        listView.setAdapter(adapter);

        // 点击待办，打开编辑弹窗
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TodoItem item = todoList.get(position);
                showEditTodoDialog(item);
            }
        });

        loadTodosFromDb();

        // 底部导航
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_message);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_message) {
                // 显示ListView，隐藏Fragment容器
                listView.setVisibility(View.VISIBLE);
                findViewById(R.id.fragment_container).setVisibility(View.GONE);
                return true;
            }
            if (item.getItemId() == R.id.nav_calendar) {
                startActivity(new Intent(MessageActivity.this, CalendarActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_profile) {
                startActivity(new Intent(MessageActivity.this, ProfileActivity.class));
                return true;
            }
            if (item.getItemId() == R.id.nav_projects) {
                // 隐藏ListView，显示Fragment容器
                listView.setVisibility(View.GONE);
                findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
                getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ProjectsFragment())
                    .commit();
                return true;
            }
            return false;
        });

        // 侧边栏：新建待办、已完成存档等
        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_new_task) {
                    addNewTodo();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                if (item.getItemId() == R.id.nav_completed_tasks) {
                    Toast.makeText(this, "已完成存档", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                if (item.getItemId() == R.id.nav_project_management) {
                    Toast.makeText(this, "项目管理", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                if (item.getItemId() == R.id.nav_recycle_bin) {
                    Toast.makeText(this, "回收站", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            });
        }
    }

    private void loadTodosFromDb() {
        executor.execute(() -> {
            List<TodoItem> list = todoDao.getAll();
            if (list.isEmpty()) {
                // 首次使用：插入默认待办
                insertDefaultTodos();
                list = todoDao.getAll();
            }
            List<TodoItem> finalList = list;
            runOnUiThread(() -> {
                todoList.clear();
                todoList.addAll(finalList);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private void insertDefaultTodos() {
        TodoItem[] defaults = {
                new TodoItem("Journal", "12/15", "5 minutes ago", false),
                new TodoItem("Homework", "Android应用开发", "10 minutes ago", false),
                new TodoItem("Hobbies", "音乐", "1 day ago", false),
                new TodoItem("Courses", "移动应用开发实践", "1 day ago", false),
                new TodoItem("Travel Planner", "长沙", "3 days ago", false)
        };
        for (TodoItem item : defaults) {
            todoDao.insert(item);
        }
    }

    private void addNewTodo() {
        TodoItem newItem = new TodoItem("新待办", "点击可编辑内容", "刚刚", false);
        executor.execute(() -> {
            long id = todoDao.insert(newItem);
            newItem.setId(id);
            runOnUiThread(() -> {
                todoList.add(newItem);
                adapter.notifyDataSetChanged();
                Toast.makeText(this, "已添加新待办", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showEditTodoDialog(TodoItem item) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_todo, null, false);
        EditText editTitle = dialogView.findViewById(R.id.edit_todo_title);
        EditText editContent = dialogView.findViewById(R.id.edit_todo_content);
        EditText editTime = dialogView.findViewById(R.id.edit_todo_time);

        editTitle.setText(item.getTitle());
        editContent.setText(item.getContent());
        editTime.setText(item.getTime());

        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("保存", (dialog, which) -> {
                    String newTitle = editTitle.getText().toString().trim();
                    String newContent = editContent.getText().toString().trim();
                    String newTime = editTime.getText().toString().trim();

                    item.setTitle(newTitle);
                    item.setContent(newContent);
                    item.setTime(newTime);

                    executor.execute(() -> {
                        todoDao.update(item);
                        runOnUiThread(() -> {
                            adapter.notifyDataSetChanged();
                            Toast.makeText(this, "已更新待办", Toast.LENGTH_SHORT).show();
                        });
                    });
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }

    public void onUserInfoClick(View view) {
        drawerLayout.openDrawer(GravityCompat.START);
    }
}
