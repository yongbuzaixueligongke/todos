package com.example.imageview;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TodoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_TODO = 1;

    public interface OnTodoCheckedListener {
        void onCheckedChanged(TodoItem item, boolean checked);
    }

    public interface OnTodoClickListener {
        void onTodoClick(TodoItem item);
    }

    public static class DisplayItem {
        private final String headerTitle;
        private final TodoItem todoItem;

        private DisplayItem(String headerTitle, TodoItem todoItem) {
            this.headerTitle = headerTitle;
            this.todoItem = todoItem;
        }

        public static DisplayItem header(String title) {
            return new DisplayItem(title, null);
        }

        public static DisplayItem todo(TodoItem item) {
            return new DisplayItem(null, item);
        }

        public boolean isHeader() {
            return headerTitle != null;
        }
    }

    private final List<DisplayItem> displayItems = new ArrayList<>();
    private final OnTodoCheckedListener checkedListener;
    private final OnTodoClickListener clickListener;

    public TodoAdapter(List<TodoItem> items,
                       OnTodoCheckedListener checkedListener,
                       OnTodoClickListener clickListener) {
        this.checkedListener = checkedListener;
        this.clickListener = clickListener;
        setDisplayItemsWithoutNotify(buildTodoRows(items));
    }

    public void setItems(List<TodoItem> items) {
        setDisplayItemsWithoutNotify(buildTodoRows(items));
        notifyDataSetChanged();
    }

    public void setDisplayItems(List<DisplayItem> rows) {
        setDisplayItemsWithoutNotify(rows);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            TextView headerView = new TextView(parent.getContext());
            headerView.setLayoutParams(new RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
            int horizontalPadding = dp(parent, 18);
            headerView.setPadding(horizontalPadding, dp(parent, 18), horizontalPadding, dp(parent, 6));
            headerView.setTextColor(0xFF202124);
            headerView.setTextSize(17);
            headerView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
            return new HeaderViewHolder(headerView);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message, parent, false);
        return new TodoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        DisplayItem row = displayItems.get(position);
        if (row.isHeader()) {
            ((HeaderViewHolder) viewHolder).titleTextView.setText(row.headerTitle);
            return;
        }

        TodoViewHolder holder = (TodoViewHolder) viewHolder;
        TodoItem item = row.todoItem;
        holder.boundItem = item;
        holder.titleTextView.setText(item.getTitle());
        holder.contentTextView.setText(item.getContent());
        if (item.getTag() != null && !item.getTag().trim().isEmpty()) {
            PriorityTagUtils.applyTagIcon(holder.tagTextView, item.getTag());
        } else {
            holder.tagTextView.setText("");
            holder.tagTextView.setCompoundDrawables(null, null, null, null);
            holder.tagTextView.setVisibility(View.GONE);
        }
        holder.timeTextView.setText(item.getTime());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isCompleted());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkedListener != null && holder.boundItem != null) {
                holder.boundItem.setCompleted(isChecked);
                checkedListener.onCheckedChanged(holder.boundItem, isChecked);
            }
        });

        if (item.isOverdue()) {
            holder.itemView.setAlpha(0.45f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null && holder.boundItem != null) {
                clickListener.onTodoClick(holder.boundItem);
            }
        });
    }

    @Override
    public int getItemCount() {
        return displayItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return displayItems.get(position).isHeader() ? VIEW_TYPE_HEADER : VIEW_TYPE_TODO;
    }

    private List<DisplayItem> buildTodoRows(List<TodoItem> items) {
        List<DisplayItem> rows = new ArrayList<>();
        if (items != null) {
            for (TodoItem item : items) {
                rows.add(DisplayItem.todo(item));
            }
        }
        return rows;
    }

    private void setDisplayItemsWithoutNotify(List<DisplayItem> rows) {
        displayItems.clear();
        if (rows != null) {
            displayItems.addAll(rows);
        }
    }

    private int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        final TextView titleTextView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView;
        }
    }

    static class TodoViewHolder extends RecyclerView.ViewHolder {
        TodoItem boundItem;
        final TextView titleTextView;
        final TextView contentTextView;
        final TextView tagTextView;
        final TextView timeTextView;
        final CheckBox checkBox;

        TodoViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.message_title);
            contentTextView = itemView.findViewById(R.id.message_content);
            tagTextView = itemView.findViewById(R.id.message_tag);
            timeTextView = itemView.findViewById(R.id.message_time);
            checkBox = itemView.findViewById(R.id.message_checkbox);
        }
    }
}
