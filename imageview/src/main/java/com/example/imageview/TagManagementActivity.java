package com.example.imageview;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TagManagementActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TagManagementAdapter adapter;
    private TodoRepository todoRepository;
    private final List<TagSummary> tags = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!AccountGuard.requireLogin(this)) {
            return;
        }
        setContentView(R.layout.activity_tag_management);

        todoRepository = new TodoRepository(this);
        initViews();
        loadTags();
    }

    private void initViews() {
        ImageButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.recycler_tags);
        adapter = new TagManagementAdapter(
                tags,
                tag -> NavigationHelper.navigateToTodoListByTag(this, tag.name),
                tag -> confirmDeleteTag(tag.name)
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void loadTags() {
        todoRepository.getAll(todos -> {
            Map<String, TagSummary> summaries = new LinkedHashMap<>();
            for (String tag : PriorityTagUtils.PRIORITY_TAGS) {
                summaries.put(tag, new TagSummary(tag, 0, true));
            }

            for (TodoItem todo : todos) {
                String tag = safe(todo.getTag()).trim();
                if (tag.isEmpty()) {
                    continue;
                }
                String key = tag.toUpperCase(Locale.ROOT);
                TagSummary summary = summaries.get(key);
                if (summary == null) {
                    summary = new TagSummary(tag, 0, PriorityTagUtils.isPriorityTag(tag));
                    summaries.put(key, summary);
                }
                summary.count++;
            }

            tags.clear();
            tags.addAll(new ArrayList<>(summaries.values()));
            adapter.notifyDataSetChanged();
        });
    }

    private void confirmDeleteTag(String tag) {
        new AlertDialog.Builder(this)
                .setTitle("Delete tag")
                .setMessage("Deleting a tag will clear it from all tasks. Continue?")
                .setPositiveButton("Delete", (dialog, which) -> deleteTag(tag))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteTag(String tag) {
        todoRepository.clearTag(tag, ignored -> {
            Toast.makeText(this, "Tag deleted", Toast.LENGTH_SHORT).show();
            loadTags();
        });
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    static class TagSummary {
        final String name;
        int count;
        final boolean fixed;

        TagSummary(String name, int count, boolean fixed) {
            this.name = name;
            this.count = count;
            this.fixed = fixed;
        }
    }
}
