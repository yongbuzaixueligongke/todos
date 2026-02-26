package com.example.imageview;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CalendarGridAdapter extends RecyclerView.Adapter<CalendarGridAdapter.DayViewHolder> {

    public interface OnDayClickListener {
        void onDayClick(CalendarDay day);
    }

    private final List<CalendarDay> days = new ArrayList<>();
    private final OnDayClickListener listener;
    private int cellHeightPx = 0;

    public CalendarGridAdapter(OnDayClickListener listener) {
        this.listener = listener;
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDayClick(day);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView dayNumber;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            dayNumber = itemView.findViewById(R.id.day_number);
        }
    }
}

