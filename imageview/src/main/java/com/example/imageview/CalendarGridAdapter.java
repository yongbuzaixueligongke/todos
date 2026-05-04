package com.example.imageview;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    public interface OnTodoClickListener {
        void onTodoClick(TodoItem todo);
    }

    private final List<CalendarDay> days = new ArrayList<>();
    private final OnDayClickListener dayListener;
    private final OnTodoClickListener todoListener;
    private int cellHeightPx = 0;

    public CalendarGridAdapter(OnDayClickListener dayListener, OnTodoClickListener todoListener) {
        this.dayListener = dayListener;
        this.todoListener = todoListener;
    }

    public void setDays(List<CalendarDay> newDays) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        notifyDataSetChanged();
    }

    public void setCellHeightPx(int cellHeightPx) {
        if (this.cellHeightPx == cellHeightPx) {
            return;
        }
        this.cellHeightPx = cellHeightPx;
        notifyDataSetChanged();
    }

    public void setDaysAndCellHeight(List<CalendarDay> newDays, int cellHeightPx) {
        days.clear();
        if (newDays != null) {
            days.addAll(newDays);
        }
        this.cellHeightPx = cellHeightPx;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        CalendarDay day = days.get(position);

        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (cellHeightPx > 0 && lp != null) {
            lp.height = cellHeightPx;
            holder.itemView.setLayoutParams(lp);
        }

        holder.dayNumber.setText(String.valueOf(day.getDayOfMonth()));

        if (!day.isInCurrentMonth()) {
            holder.dayNumber.setAlpha(0.35f);
        } else {
            holder.dayNumber.setAlpha(1.0f);
        }

        if (day.isToday()) {
            holder.dayNumber.setTypeface(Typeface.DEFAULT_BOLD);
        } else {
            holder.dayNumber.setTypeface(Typeface.DEFAULT);
        }

        // 清空待办事项容器
        holder.todosContainer.removeAllViews();

        // 添加待办事项标签
        if (day.hasTodos()) {
            List<TodoItem> todos = day.getTodos();
            // 最多显示3个待办事项
            int maxTodos = Math.min(todos.size(), 3);
            for (int i = 0; i < maxTodos; i++) {
                TodoItem todo = todos.get(i);
                TextView todoTag = new TextView(holder.itemView.getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 4, 0, 4);
                todoTag.setLayoutParams(params);
                todoTag.setText(todo.getTitle());
                todoTag.setTextSize(10);
                todoTag.setTextColor(android.graphics.Color.parseColor("#30343B"));
                todoTag.setPadding(
                        PriorityTagUtils.dp(todoTag, 5),
                        PriorityTagUtils.dp(todoTag, 2),
                        PriorityTagUtils.dp(todoTag, 5),
                        PriorityTagUtils.dp(todoTag, 2)
                );
                todoTag.setEllipsize(android.text.TextUtils.TruncateAt.END);
                todoTag.setSingleLine(true);
                
                todoTag.setBackground(PriorityTagUtils.createCalendarTodoDrawable(
                        todo.getTag(),
                        PriorityTagUtils.dp(todoTag, 5)
                ));
                int iconSize = PriorityTagUtils.dp(todoTag, 8);
                todoTag.setCompoundDrawables(PriorityTagUtils.createDotDrawable(todo.getTag(), iconSize), null, null, null);
                todoTag.setCompoundDrawablePadding(PriorityTagUtils.dp(todoTag, 4));

                // 添加点击事件
                final TodoItem finalTodo = todo;
                todoTag.setOnClickListener(v -> {
                    if (todoListener != null) {
                        todoListener.onTodoClick(finalTodo);
                    }
                });

                holder.todosContainer.addView(todoTag);
            }
        }

        holder.itemView.setOnClickListener(v -> {
            if (dayListener != null) {
                dayListener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayNumber;
        LinearLayout todosContainer;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayNumber = itemView.findViewById(R.id.day_number);
            todosContainer = itemView.findViewById(R.id.day_todos_container);
        }
    }
}
