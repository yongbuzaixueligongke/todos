package com.example.imageview;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.ui.projects.ProjectsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private TodoAdapter adapter;
    private View topHeader;
    private TextView headerTitle;
    private View taskCreateInput;
    private View aiClipboardAction;

    private final List<TodoItem> todoList = new ArrayList<>();

    private TodoListController todoListController;
    private AiTodoRecognitionController aiTodoRecognitionController;
    private boolean isProjectsMode = false;
    private boolean isMyTasksMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccountGuard.requireLogin(this)) {
            return;
        }
        setContentView(R.layout.activity_message);
        ensureWindowReceivesInput();
        initViews();
        initDatabase();
        readTaskFilter(getIntent());
        initAdapter();
        loadTodosFromDb();
        handleNavigation();

        long todoId = getIntent().getLongExtra(NavigationConstants.EXTRA_TODO_ID, -1L);
        if (todoId != -1L) {
            showTaskEditor(todoId, -1L, null);
        }
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        recyclerView = findViewById(R.id.message_recycler_view);
        topHeader = findViewById(R.id.top_header);
        headerTitle = findViewById(R.id.username);
        taskCreateInput = findViewById(R.id.task_create_input);
        aiClipboardAction = findViewById(R.id.action_ai_clipboard);

        taskCreateInput.setOnClickListener(v -> showTaskEditor(-1L, -1L, null));
        aiClipboardAction.setOnClickListener(v -> startAiTodoRecognitionFromClipboard());
        topHeader.setOnClickListener(v -> {
            if (!isProjectsMode && !isMyTasksMode) {
                showTaskListMenu();
            }
        });
    }

    private void initDatabase() {
        todoListController = new TodoListController(this);
        aiTodoRecognitionController = new AiTodoRecognitionController();
        TodoFilterOptions options = todoListController.getOptions();
        options.setSelectedTag(TodoConstants.ALL_TAGS_LABEL);
        options.setDateFilter(TodoConstants.DATE_FILTER_TODAY);
        options.setDateFilteringEnabled(true);
    }

    private void initAdapter() {
        adapter = new TodoAdapter(todoList, (item, checked) -> {
            todoListController.toggleCompleted(item, checked, this::renderTodoList);
        }, item -> showTaskEditor(item.getId(), -1L, null));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void handleNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (isProjectsModeRequested()) {
            bottomNavigationView.setSelectedItemId(R.id.nav_projects);
            showProjectsMode();
        } else {
            bottomNavigationView.setSelectedItemId(R.id.nav_message);
            applyDefaultTodoMode();
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_message) {
                isMyTasksMode = false;
                TodoFilterOptions options = todoListController.getOptions();
                options.setTaskFilter(TodoConstants.FILTER_ALL_TASKS);
                options.setDateFilteringEnabled(true);
                options.setGroupByDate(false);
                applyDefaultTodoMode();
                return true;
            }
            if (itemId == R.id.nav_calendar) {
                NavigationHelper.navigateToCalendar(MessageActivity.this);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            if (itemId == R.id.nav_profile) {
                NavigationHelper.navigateToProfile(MessageActivity.this);
                finish();
                overridePendingTransition(0, 0);
                return true;
            }
            if (itemId == R.id.nav_projects) {
                showProjectsMode();
                return true;
            }
            return false;
        });

        NavigationView navigationView = findViewById(R.id.nav_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(item -> {
                if (item.getItemId() == R.id.nav_new_task) {
                    showTaskEditor(-1L, -1L, null);
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                if (item.getItemId() == R.id.nav_completed_tasks) {
                    Toast.makeText(this, "Completed tasks", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                if (item.getItemId() == R.id.nav_project_management) {
                    Toast.makeText(this, "Project management", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                if (item.getItemId() == R.id.nav_recycle_bin) {
                    Toast.makeText(this, "Recycle bin", Toast.LENGTH_SHORT).show();
                    drawerLayout.closeDrawer(GravityCompat.START);
                    return true;
                }
                return false;
            });
        }
    }

    private boolean isProjectsModeRequested() {
        Intent intent = getIntent();
        return intent != null && intent.getBooleanExtra(NavigationConstants.EXTRA_NAVIGATE_TO_PROJECTS, false);
    }

    private void showProjectsMode() {
        recyclerView.setVisibility(View.GONE);
        taskCreateInput.setVisibility(View.GONE);
        if (topHeader != null) {
            topHeader.setVisibility(View.GONE);
        }
        findViewById(R.id.fragment_container).setVisibility(View.VISIBLE);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ProjectsFragment())
                .commit();
        isProjectsMode = true;
    }

    private void applyDefaultTodoMode() {
        recyclerView.setVisibility(View.VISIBLE);
        taskCreateInput.setVisibility(View.VISIBLE);
        if (topHeader != null) {
            topHeader.setVisibility(View.VISIBLE);
        }
        findViewById(R.id.fragment_container).setVisibility(View.GONE);
        isProjectsMode = false;
        TodoFilterOptions options = todoListController.getOptions();
        options.setDateFilteringEnabled(!isMyTasksMode);
        options.setGroupByDate(!isMyTasksMode && shouldShowDateGroups());
        applyTodoHeader();
        todoListController.refresh(this::renderTodoList);
    }

    private void applyTodoHeader() {
        if (headerTitle != null) {
            headerTitle.setText(isMyTasksMode ? getTaskFilterTitle() : getDateFilterTitle());
            headerTitle.setGravity(Gravity.CENTER);
            headerTitle.setPadding(0, 0, 0, 0);
        }
    }

    private void loadTodosFromDb() {
        todoListController.load(this::renderTodoList);
    }

    private void showTaskEditor(long todoId, long projectId, String projectName) {
        TaskEditorBottomSheet sheet;
        if (todoId > 0L) {
            sheet = TaskEditorBottomSheet.newEditInstance(todoId);
        } else if (projectId > 0L) {
            sheet = TaskEditorBottomSheet.newCreateForProject(projectId, projectName);
        } else {
            sheet = TaskEditorBottomSheet.newCreateInstance();
        }
        sheet.setTaskEditorListener(new TaskEditorBottomSheet.TaskEditorListener() {
            @Override
            public void onTaskSaved() {
                loadTodosFromDb();
            }

            @Override
            public void onTaskDeleted() {
                loadTodosFromDb();
            }
        });
        sheet.show(getSupportFragmentManager(), "task_editor");
    }

    private void showTaskEditor(AiTodoDraft draft) {
        TaskEditorBottomSheet sheet = TaskEditorBottomSheet.newCreateFromDraft(draft);
        sheet.setTaskEditorListener(new TaskEditorBottomSheet.TaskEditorListener() {
            @Override
            public void onTaskSaved() {
                loadTodosFromDb();
            }

            @Override
            public void onTaskDeleted() {
                loadTodosFromDb();
            }
        });
        sheet.show(getSupportFragmentManager(), "ai_task_editor");
    }

    private String readClipboardText() {
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager == null || !clipboardManager.hasPrimaryClip()) {
            return "";
        }
        ClipData clipData = clipboardManager.getPrimaryClip();
        if (clipData == null || clipData.getItemCount() == 0) {
            return "";
        }
        CharSequence text = clipData.getItemAt(0).coerceToText(this);
        return text == null ? "" : text.toString().trim();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readTaskFilter(intent);
        boolean shouldShowProjects = intent != null && intent.getBooleanExtra(NavigationConstants.EXTRA_NAVIGATE_TO_PROJECTS, false);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (shouldShowProjects && !isProjectsMode) {
            bottomNavigationView.setSelectedItemId(R.id.nav_projects);
            showProjectsMode();
        } else if (!shouldShowProjects && isProjectsMode) {
            bottomNavigationView.setSelectedItemId(R.id.nav_message);
            applyDefaultTodoMode();
        } else if (!shouldShowProjects) {
            applyDefaultTodoMode();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ensureWindowReceivesInput();
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        if (!isProjectsMode) {
            applyDefaultTodoMode();
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        ensureWindowReceivesInput();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            ensureWindowReceivesInput();
        }
    }

    private void ensureWindowReceivesInput() {
        getWindow().clearFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM
        );
        View decor = getWindow().getDecorView();
        decor.setClickable(false);
        decor.setFocusableInTouchMode(true);
        decor.requestFocus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void onUserInfoClick(View view) {
        if (!isProjectsMode && !isMyTasksMode) {
            showTaskListMenu();
        }
    }

    public void startAiTodoRecognitionFromClipboard() {
        aiTodoRecognitionController.recognize(readClipboardText(), new AiTodoRecognitionController.Callback() {
            @Override
            public void onMessage(String message) {
                Toast.makeText(MessageActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDraftReady(AiTodoDraft draft, String message) {
                if (message != null && !message.trim().isEmpty()) {
                    Toast.makeText(MessageActivity.this, message, Toast.LENGTH_SHORT).show();
                }
                showTaskEditor(draft);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(MessageActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void readTaskFilter(Intent intent) {
        TodoFilterOptions options = todoListController.getOptions();
        if (intent == null) {
            options.setTaskFilter(TodoConstants.FILTER_ALL_TASKS);
            isMyTasksMode = false;
            return;
        }
        String requestedFilter = intent.getStringExtra(NavigationConstants.EXTRA_TASK_FILTER);
        if (NavigationConstants.FILTER_COMPLETED_TASKS.equals(requestedFilter)
                || NavigationConstants.FILTER_ARCHIVED_TASKS.equals(requestedFilter)) {
            options.setTaskFilter(requestedFilter);
            isMyTasksMode = true;
        } else {
            options.setTaskFilter(TodoConstants.FILTER_ALL_TASKS);
            isMyTasksMode = NavigationConstants.FILTER_ALL_TASKS.equals(requestedFilter);
        }
        options.setDateFilteringEnabled(!isMyTasksMode);
        options.setGroupByDate(!isMyTasksMode && shouldShowDateGroups());
    }

    private boolean shouldShowDateGroups() {
        int dateFilter = todoListController.getOptions().getDateFilter();
        return dateFilter == TodoConstants.DATE_FILTER_NEXT_THREE_DAYS
                || dateFilter == TodoConstants.DATE_FILTER_NEXT_WEEK;
    }

    private String getTaskFilterTitle() {
        String currentTaskFilter = todoListController.getOptions().getTaskFilter();
        if (NavigationConstants.FILTER_COMPLETED_TASKS.equals(currentTaskFilter)) {
            return getString(R.string.menu_completed_tasks);
        }
        if (NavigationConstants.FILTER_ARCHIVED_TASKS.equals(currentTaskFilter)) {
            return getString(R.string.menu_archived_tasks);
        }
        return getString(R.string.menu_all_tasks);
    }

    private String getDateFilterTitle() {
        int currentDateFilter = todoListController.getOptions().getDateFilter();
        if (currentDateFilter == TodoConstants.DATE_FILTER_NEXT_THREE_DAYS) {
            return getString(R.string.task_filter_next_three_days);
        }
        if (currentDateFilter == TodoConstants.DATE_FILTER_NEXT_WEEK) {
            return getString(R.string.task_filter_next_week);
        }
        return getString(R.string.task_filter_today);
    }

    private void showTaskListMenu() {
        View menuView = LayoutInflater.from(this).inflate(R.layout.popup_task_list_filter, null, false);
        int menuWidth = getResources().getDimensionPixelSize(R.dimen.task_list_menu_width);
        PopupWindow popupWindow = new PopupWindow(menuView, menuWidth, WindowManager.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        menuView.findViewById(R.id.menu_today).setOnClickListener(v -> selectDateFilter(TodoConstants.DATE_FILTER_TODAY, popupWindow));
        menuView.findViewById(R.id.menu_next_three_days).setOnClickListener(v -> selectDateFilter(TodoConstants.DATE_FILTER_NEXT_THREE_DAYS, popupWindow));
        menuView.findViewById(R.id.menu_next_week).setOnClickListener(v -> selectDateFilter(TodoConstants.DATE_FILTER_NEXT_WEEK, popupWindow));

        int xOffset = topHeader == null ? 0 : (topHeader.getWidth() - menuWidth) / 2;
        popupWindow.showAsDropDown(topHeader, xOffset, -8);
    }

    private void selectDateFilter(int dateFilter, PopupWindow popupWindow) {
        TodoFilterOptions options = todoListController.getOptions();
        options.setDateFilter(dateFilter);
        options.setGroupByDate(shouldShowDateGroups());
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        applyTodoHeader();
        todoListController.refresh(this::renderTodoList);
    }

    private void renderTodoList(List<TodoItem> filteredItems, List<TodoAdapter.DisplayItem> displayItems, List<String> availableTags) {
        todoList.clear();
        todoList.addAll(filteredItems);
        adapter.setDisplayItems(displayItems);
    }

}
