package com.example.imageview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TodoListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvPageTitle;
    private ImageButton btnBack;
    private SearchView searchTodoView;
    private Spinner tagFilterSpinner;
    private Spinner sortTodoSpinner;
    private TodoAdapter adapter;
    private final List<TodoItem> todoList = new ArrayList<>();
    private final List<String> availableTags = new ArrayList<>();

    private TodoListController todoListController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccountGuard.requireLogin(this)) {
            return;
        }
        setContentView(R.layout.activity_todo_list);

        initController();
        initViews();
        initAdapter();
        applyTitle();
        loadTodos();
    }

    private void initController() {
        todoListController = new TodoListController(this);
        TodoFilterOptions options = todoListController.getOptions();
        options.setTaskFilter(getIntent().getStringExtra(NavigationConstants.EXTRA_TASK_FILTER));
        options.setFixedTag(getIntent().getStringExtra(NavigationConstants.EXTRA_TAG_FILTER));
        options.setSelectedTag(TodoConstants.ALL_TAGS_LABEL);
        options.setDateFilteringEnabled(false);
        options.setGroupByDate(false);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_todo_list);
        tvPageTitle = findViewById(R.id.tv_page_title);
        btnBack = findViewById(R.id.btn_back);
        searchTodoView = findViewById(R.id.search_todo_list);
        tagFilterSpinner = findViewById(R.id.spinner_todo_list_tag_filter);
        sortTodoSpinner = findViewById(R.id.spinner_todo_list_sort);
        btnBack.setOnClickListener(v -> finish());
        if (todoListController.getOptions().hasFixedTagFilter()) {
            tagFilterSpinner.setVisibility(View.GONE);
        }

        searchTodoView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                todoListController.getOptions().setSearchQuery(query);
                todoListController.refresh(TodoListActivity.this::renderTodoList);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                todoListController.getOptions().setSearchQuery(newText);
                todoListController.refresh(TodoListActivity.this::renderTodoList);
                return true;
            }
        });

        List<String> sortOptions = new ArrayList<>();
        sortOptions.add("Newest first");
        sortOptions.add("Oldest first");
        sortOptions.add("Title A-Z");
        sortOptions.add("Title Z-A");
        ArrayAdapter<String> sortAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, sortOptions);
        sortAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortTodoSpinner.setAdapter(sortAdapter);
        sortTodoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                todoListController.getOptions().setSortMode(position);
                todoListController.refresh(TodoListActivity.this::renderTodoList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        availableTags.add(TodoConstants.ALL_TAGS_LABEL);
        Collections.addAll(availableTags, PriorityTagUtils.PRIORITY_TAGS);
        PriorityTagSpinnerAdapter tagAdapter = new PriorityTagSpinnerAdapter(this, availableTags);
        tagFilterSpinner.setAdapter(tagAdapter);
        tagFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                todoListController.getOptions().setSelectedTag(availableTags.get(position));
                todoListController.refresh(TodoListActivity.this::renderTodoList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initAdapter() {
        adapter = new TodoAdapter(todoList, (item, checked) ->
                todoListController.toggleCompleted(item, checked, this::renderTodoList), item -> {
            if (isOverdueFilter()) {
                showOverdueActions(item);
            } else {
                showTaskEditor(item.getId());
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void applyTitle() {
        if (todoListController.getOptions().hasFixedTagFilter()) {
            tvPageTitle.setText("Tag: " + todoListController.getOptions().getFixedTag());
            return;
        }
        switch (todoListController.getOptions().getTaskFilter()) {
            case NavigationConstants.FILTER_COMPLETED_TASKS:
                tvPageTitle.setText("Completed");
                break;
            case NavigationConstants.FILTER_ARCHIVED_TASKS:
                tvPageTitle.setText("Overdue");
                break;
            default:
                tvPageTitle.setText("All tasks");
                break;
        }
    }

    private void loadTodos() {
        todoListController.load(this::renderTodoList);
    }

    private void renderTodoList(List<TodoItem> filteredItems, List<TodoAdapter.DisplayItem> displayItems, List<String> tags) {
        todoList.clear();
        todoList.addAll(filteredItems);
        rebuildTagFilterOptions(tags);
        adapter.setDisplayItems(displayItems);
    }

    private void rebuildTagFilterOptions(List<String> tags) {
        if (todoListController.getOptions().hasFixedTagFilter()) {
            return;
        }

        String previousSelection = todoListController.getOptions().getSelectedTag();
        availableTags.clear();
        if (tags != null && !tags.isEmpty()) {
            availableTags.addAll(tags);
        } else {
            availableTags.add(TodoConstants.ALL_TAGS_LABEL);
            Collections.addAll(availableTags, PriorityTagUtils.PRIORITY_TAGS);
        }

        PriorityTagSpinnerAdapter tagAdapter = new PriorityTagSpinnerAdapter(this, availableTags);
        tagFilterSpinner.setAdapter(tagAdapter);

        int selection = availableTags.indexOf(previousSelection);
        if (selection >= 0) {
            tagFilterSpinner.setSelection(selection, false);
            todoListController.getOptions().setSelectedTag(previousSelection);
        } else {
            tagFilterSpinner.setSelection(0, false);
            todoListController.getOptions().setSelectedTag(TodoConstants.ALL_TAGS_LABEL);
        }
    }

    private boolean isOverdueFilter() {
        return NavigationConstants.FILTER_ARCHIVED_TASKS.equals(todoListController.getOptions().getTaskFilter());
    }

    private void showOverdueActions(TodoItem item) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View menuView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_overdue_actions, null, false);
        dialog.setContentView(menuView);

        menuView.findViewById(R.id.action_today).setOnClickListener(v -> {
            dialog.dismiss();
            todoListController.moveTodoToToday(item, this::renderTodoList);
            Toast.makeText(this, "Moved to today", Toast.LENGTH_SHORT).show();
        });
        menuView.findViewById(R.id.action_later).setOnClickListener(v -> {
            dialog.dismiss();
            todoListController.moveTodoToLater(item, this::renderTodoList);
            Toast.makeText(this, "Moved to later", Toast.LENGTH_SHORT).show();
        });
        menuView.findViewById(R.id.action_complete).setOnClickListener(v -> {
            dialog.dismiss();
            todoListController.completeTodo(item, this::renderTodoList);
            Toast.makeText(this, "Marked as completed", Toast.LENGTH_SHORT).show();
        });
        menuView.findViewById(R.id.action_delete).setOnClickListener(v -> {
            dialog.dismiss();
            todoListController.deleteTodo(item, this::renderTodoList);
            Toast.makeText(this, "Task deleted", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    private void showTaskEditor(long todoId) {
        TaskEditorBottomSheet sheet = TaskEditorBottomSheet.newEditInstance(todoId);
        sheet.setTaskEditorListener(new TaskEditorBottomSheet.TaskEditorListener() {
            @Override
            public void onTaskSaved() {
                loadTodos();
            }

            @Override
            public void onTaskDeleted() {
                loadTodos();
            }
        });
        sheet.show(getSupportFragmentManager(), "task_editor");
    }
}
