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
                todoTag.setPadding(4, 2, 4, 2);
                todoTag.setEllipsize(android.text.TextUtils.TruncateAt.END);
                todoTag.setSingleLine(true);
                
                // 根据待办事项的标签设置不同的颜色
                switch (todo.getTag()) {
                    case "工作":
                        todoTag.setBackgroundColor(0xFFE3F2FD); // 浅蓝色
                        break;
                    case "学习":
                        todoTag.setBackgroundColor(0xFFE8F5E8); // 浅绿色
                        break;
                    case "健康":
                        todoTag.setBackgroundColor(0xFFFFEBEE); // 浅红色
                        break;
                    case "旅行":
                        todoTag.setBackgroundColor(0xFFFFF3E0); // 浅橙色
                        break;
                    default:
                        todoTag.setBackgroundColor(0xFFF5F5F5); // 浅灰色
                        break;
                }

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

