package com.example.imageview;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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
    private List<Project> projects = new ArrayList<>();
    private AppDatabase database;
    private TodoDao todoDao;
    private ProjectDao projectDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        // 初始化视图
        initViews();
        
        // 初始化数据库
        initDatabase();
        
        // 初始化适配器
        initAdapter();
        
        // 加载数据
        loadTodosFromDb();
        
        // 处理导航逻辑
        handleNavigation();
        
        // 检查是否有待办ID传入
        long todoId = getIntent().getLongExtra("todo_id", -1);
        if (todoId != -1) {
            // 加载待办事项并显示编辑对话框
            executor.execute(() -> {
                TodoItem todo = todoDao.getById(todoId);
                if (todo != null) {
                    runOnUiThread(() -> {
                        showEditTodoDialog(todo);
                    });
                }
            });
        }
    }
    
    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        listView = findViewById(R.id.message_list_view);
        
        // 新增待办悬浮按钮
        com.google.android.material.floatingactionbutton.FloatingActionButton fabAddTodo = findViewById(R.id.fab_add_todo);
        fabAddTodo.setOnClickListener(v -> showAddTodoDialog());
    }
    
    private void initDatabase() {
        database = AppDatabase.getInstance(this);
        todoDao = database.todoDao();
        projectDao = database.projectDao();
    }
    
    private void initAdapter() {
        adapter = new TodoAdapter(this, todoList, new TodoAdapter.OnTodoCheckedListener() {
            @Override
            public void onCheckedChanged(TodoItem item, boolean checked) {
                item.setCompleted(checked);
                executor.execute(() -> todoDao.setCompleted(item.getId(), checked));
            }
        });
        listView.setAdapter(adapter);

        // 点击待办，打开待办详情界面
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TodoItem item = todoList.get(position);
                Intent intent = new Intent(MessageActivity.this, TodoDetailActivity.class);
                intent.putExtra("todo_id", item.getId());
                startActivity(intent);
            }
        });
    }
    
    private void handleNavigation() {
        // 底部导航
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        
        // 检查Intent，看看是否是从其他界面的项目图标点击过来的
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("navigate_to_projects")) {
            // 显示项目页面
            bottomNavigationView.setSelectedItemId(R.id.nav_projects);
            listView.setVisibility(View.GONE);
            findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProjectsFragment())
                .commit();
        } else {
            // 默认显示待办页面
            bottomNavigationView.setSelectedItemId(R.id.nav_message);
        }
        
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_message) {
                // 显示ListView和待办悬浮按钮，隐藏Fragment容器
                listView.setVisibility(View.VISIBLE);
                findViewById(R.id.fab_add_todo).setVisibility(View.VISIBLE);
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
                // 隐藏ListView和待办悬浮按钮，显示Fragment容器
                listView.setVisibility(View.GONE);
                findViewById(R.id.fab_add_todo).setVisibility(View.GONE);
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
                    showAddTodoDialog();
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
                list = insertDefaultTodos();
            }
            List<TodoItem> finalList = list;
            runOnUiThread(() -> {
                todoList.clear();
                todoList.addAll(finalList);
                adapter.notifyDataSetChanged();
            });
        });
    }

    private List<TodoItem> insertDefaultTodos() {
        List<TodoItem> defaultTodos = new ArrayList<>();
        defaultTodos.add(new TodoItem("Journal", "12/15", "5 minutes ago", false, "", "", "", "", ""));
        defaultTodos.add(new TodoItem("Homework", "Android应用开发", "10 minutes ago", false, "", "", "", "", ""));
        defaultTodos.add(new TodoItem("Hobbies", "音乐", "1 day ago", false, "", "", "", "", ""));
        defaultTodos.add(new TodoItem("Courses", "移动应用开发实践", "1 day ago", false, "", "", "", "", ""));
        defaultTodos.add(new TodoItem("Travel Planner", "长沙", "3 days ago", false, "", "", "", "", ""));
        
        // 批量插入，减少数据库操作次数
        for (TodoItem item : defaultTodos) {
            long id = todoDao.insert(item);
            item.setId(id);
        }
        
        return defaultTodos;
    }

    private void addNewTodo() {
        TodoItem newItem = new TodoItem("新待办", "点击可编辑内容", "刚刚", false, "", "", "", "", "");
        executor.execute(() -> {
            long id = todoDao.insert(newItem);
            newItem.setId(id);
            runOnUiThread(() -> {
                int position = todoList.size();
                todoList.add(newItem);
                adapter.notifyItemInserted(position);
                Toast.makeText(this, "已添加新待办", Toast.LENGTH_SHORT).show();
            });
        });
    }

    private void showAddTodoDialog() {
        // 加载项目列表
        executor.execute(() -> {
            List<Project> loadedProjects = projectDao.getAll();
            projects.clear();
            projects.addAll(loadedProjects);
            List<String> projectNames = new ArrayList<>();
            projectNames.add("请选择项目");
            for (Project project : projects) {
                projectNames.add(project.getTitle());
            }

            runOnUiThread(() -> {
                View dialogView = LayoutInflater.from(MessageActivity.this).inflate(R.layout.dialog_add_todo, null, false);
                EditText editTitle = dialogView.findViewById(R.id.edit_todo_title);
                EditText editStartTime = dialogView.findViewById(R.id.edit_todo_start_time);
                EditText editEndTime = dialogView.findViewById(R.id.edit_todo_end_time);
                Spinner spinnerProject = dialogView.findViewById(R.id.spinner_todo_project);
                EditText editProject = dialogView.findViewById(R.id.edit_todo_project);
                EditText editTag = dialogView.findViewById(R.id.edit_todo_tag);
                EditText editDescription = dialogView.findViewById(R.id.edit_todo_description);

                // 为时间输入框添加日历选择器
                setupDatePicker(editStartTime);
                setupDatePicker(editEndTime);

                // 设置Spinner适配器
                ArrayAdapter<String> projectAdapter = new ArrayAdapter<>(MessageActivity.this, android.R.layout.simple_spinner_item, projectNames);
                projectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerProject.setAdapter(projectAdapter);

                new AlertDialog.Builder(MessageActivity.this)
                        .setTitle("新增待办")
                        .setView(dialogView)
                        .setPositiveButton("保存", (dialog, which) -> {
                            String title = editTitle.getText().toString().trim();
                            String startTime = editStartTime.getText().toString().trim();
                            String endTime = editEndTime.getText().toString().trim();
                            String projectName = editProject.getText().toString().trim();
                            String tag = editTag.getText().toString().trim();
                            String description = editDescription.getText().toString().trim();

                            if (title.isEmpty()) {
                                Toast.makeText(MessageActivity.this, "请输入待办标题", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            long projectId = 0;
                            String finalProjectName = projectName;
                            
                            // 处理项目选择
                            if (spinnerProject.getSelectedItemPosition() > 0 && !projects.isEmpty()) {
                                Project selectedProject = projects.get(spinnerProject.getSelectedItemPosition() - 1);
                                projectId = selectedProject.getId();
                                finalProjectName = selectedProject.getTitle();
                            } else if (!projectName.isEmpty()) {
                                // 创建新项目
                                Project newProject = new Project(projectName, startTime, endTime, "", "");
                                projectId = projectDao.insert(newProject);
                            }

                            // 创建新的待办事项
                            TodoItem newItem = new TodoItem(
                                    title,
                                    description, // 将描述作为content存储
                                    "刚刚", // 默认时间为"刚刚"
                                    false, // 默认未完成
                                    startTime,
                                    endTime,
                                    finalProjectName,
                                    tag,
                                    description,
                                    projectId
                            );

                            // 插入到数据库
                            executor.execute(() -> {
                                long id = todoDao.insert(newItem);
                                newItem.setId(id);
                                runOnUiThread(() -> {
                                    int position = todoList.size();
                                    todoList.add(newItem);
                                    MessageActivity.this.adapter.notifyItemInserted(position);
                                    Toast.makeText(MessageActivity.this, "已添加新待办", Toast.LENGTH_SHORT).show();
                                });
                            });
                        })
                        .setNegativeButton("取消", null)
                        .show();
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

                    int position = todoList.indexOf(item);
                    executor.execute(() -> {
                        todoDao.update(item);
                        runOnUiThread(() -> {
                            if (position != -1) {
                                adapter.notifyItemChanged(position);
                            } else {
                                adapter.notifyDataSetChanged();
                            }
                            Toast.makeText(this, "已更新待办", Toast.LENGTH_SHORT).show();
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

    public void onUserInfoClick(View view) {
        drawerLayout.openDrawer(GravityCompat.START);
    }
}
