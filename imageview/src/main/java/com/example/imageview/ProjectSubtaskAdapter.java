package com.example.imageview;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProjectSubtaskAdapter extends RecyclerView.Adapter<ProjectSubtaskAdapter.SubtaskViewHolder> {

    public interface OnSubtaskCheckedListener {
        void onCheckedChanged(TodoItem item, boolean checked);
    }

    public interface OnSubtaskClickListener {
        void onSubtaskClick(TodoItem item);
    }

    public interface OnSubtaskDeleteListener {
        void onSubtaskDelete(TodoItem item);
    }

    public interface OnSubtaskPomodoroListener {
        void onSubtaskPomodoro(TodoItem item);
    }

    private final List<TodoItem> items;
    private final OnSubtaskCheckedListener checkedListener;
    private final OnSubtaskClickListener clickListener;
    private final OnSubtaskDeleteListener deleteListener;
    private final OnSubtaskPomodoroListener pomodoroListener;
    private final Map<Long, Integer> pomodoroCounts;
    private View openedForeground;

    public ProjectSubtaskAdapter(
            List<TodoItem> items,
            OnSubtaskCheckedListener checkedListener,
            OnSubtaskClickListener clickListener,
            OnSubtaskDeleteListener deleteListener,
            OnSubtaskPomodoroListener pomodoroListener,
            Map<Long, Integer> pomodoroCounts
    ) {
        this.items = items != null ? items : new ArrayList<>();
        this.checkedListener = checkedListener;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
        this.pomodoroListener = pomodoroListener;
        this.pomodoroCounts = pomodoroCounts;
    }

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project_subtask_swipe, parent, false);
        return new SubtaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
        TodoItem item = items.get(position);
        holder.boundItem = item;
        holder.foreground.setTranslationX(0f);

        holder.titleTextView.setText(item.getTitle());
        holder.contentTextView.setText(item.getContent());
        bindScheduleTime(holder, item);
        bindPomodoroMarks(holder, item);

        if (item.getTag() != null && !item.getTag().trim().isEmpty()) {
            PriorityTagUtils.applyTagIcon(holder.tagTextView, item.getTag());
        } else {
            holder.tagTextView.setText("");
            holder.tagTextView.setCompoundDrawables(null, null, null, null);
            holder.tagTextView.setVisibility(View.GONE);
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(item.isCompleted());
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (checkedListener != null && holder.boundItem != null) {
                holder.boundItem.setCompleted(isChecked);
                checkedListener.onCheckedChanged(holder.boundItem, isChecked);
            }
        });

        holder.itemView.setAlpha(item.isOverdue() ? 0.45f : 1.0f);
        holder.foreground.setBackgroundColor(Color.WHITE);
        bindSwipe(holder);

        holder.deleteButton.setOnClickListener(v -> {
            closeOpenItem();
            if (deleteListener != null && holder.boundItem != null) {
                deleteListener.onSubtaskDelete(holder.boundItem);
            }
        });
        holder.pomodoroButton.setOnClickListener(v -> {
            closeOpenItem();
            if (pomodoroListener != null && holder.boundItem != null) {
                pomodoroListener.onSubtaskPomodoro(holder.boundItem);
            }
        });
    }

    private void bindPomodoroMarks(SubtaskViewHolder holder, TodoItem item) {
        int count = 0;
        if (pomodoroCounts != null && item != null) {
            Integer storedCount = pomodoroCounts.get(item.getId());
            count = storedCount == null ? 0 : storedCount;
        }
        if (count <= 0) {
            holder.pomodoroMarksTextView.setText("");
            holder.pomodoroMarksTextView.setVisibility(View.GONE);
            return;
        }
        holder.pomodoroMarksTextView.setText("\u5DF2\u5B8C\u6210" + count + "\u4E2A\uD83C\uDF45");
        holder.pomodoroMarksTextView.setVisibility(View.VISIBLE);
    }

    private void bindScheduleTime(SubtaskViewHolder holder, TodoItem item) {
        String scheduleTime = item == null ? "" : item.getTime();
        if (scheduleTime == null
                || scheduleTime.trim().isEmpty()
                || "No schedule".equalsIgnoreCase(scheduleTime.trim())) {
            holder.timeTextView.setText("");
            holder.timeTextView.setVisibility(View.GONE);
            return;
        }
        holder.timeTextView.setText(scheduleTime);
        holder.timeTextView.setVisibility(View.VISIBLE);
    }

    private void bindSwipe(SubtaskViewHolder holder) {
        int touchSlop = ViewConfiguration.get(holder.itemView.getContext()).getScaledTouchSlop();
        int actionWidth = dp(holder.itemView, 104);
        final float[] downX = new float[1];
        final float[] downY = new float[1];
        final float[] startTranslation = new float[1];

        holder.foreground.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX[0] = event.getRawX();
                    downY[0] = event.getRawY();
                    startTranslation[0] = view.getTranslationX();
                    view.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float deltaX = event.getRawX() - downX[0];
                    float deltaY = event.getRawY() - downY[0];
                    if (Math.abs(deltaY) > touchSlop && Math.abs(deltaY) > Math.abs(deltaX)) {
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                        return false;
                    }
                    if (Math.abs(deltaX) > touchSlop && Math.abs(deltaX) > Math.abs(deltaY)) {
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                        float nextTranslation = clamp(startTranslation[0] + deltaX, -actionWidth, 0f);
                        view.setTranslationX(nextTranslation);
                        return true;
                    }
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    float totalDeltaX = event.getRawX() - downX[0];
                    if (Math.abs(totalDeltaX) < touchSlop) {
                        if (view.getTranslationX() < 0f) {
                            closeForeground(view);
                        } else if (clickListener != null && holder.boundItem != null) {
                            clickListener.onSubtaskClick(holder.boundItem);
                        }
                        return true;
                    }

                    if (view.getTranslationX() < -actionWidth / 2f) {
                        openForeground(view, actionWidth);
                    } else {
                        closeForeground(view);
                    }
                    return true;
                default:
                    return true;
            }
        });
    }

    private void openForeground(View foreground, int actionWidth) {
        if (openedForeground != null && openedForeground != foreground) {
            closeForeground(openedForeground);
        }
        openedForeground = foreground;
        foreground.animate().translationX(-actionWidth).setDuration(160).start();
    }

    private void closeOpenItem() {
        if (openedForeground != null) {
            closeForeground(openedForeground);
        }
    }

    private void closeForeground(View foreground) {
        foreground.animate().translationX(0f).setDuration(160).start();
        if (openedForeground == foreground) {
            openedForeground = null;
        }
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int dp(View view, int value) {
        return Math.round(value * view.getResources().getDisplayMetrics().density);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SubtaskViewHolder extends RecyclerView.ViewHolder {
        TodoItem boundItem;
        final View foreground;
        final TextView titleTextView;
        final TextView contentTextView;
        final TextView tagTextView;
        final TextView timeTextView;
        final TextView pomodoroMarksTextView;
        final CheckBox checkBox;
        final ImageButton deleteButton;
        final ImageButton pomodoroButton;

        SubtaskViewHolder(@NonNull View itemView) {
            super(itemView);
            foreground = itemView.findViewById(R.id.subtask_foreground);
            titleTextView = itemView.findViewById(R.id.message_title);
            contentTextView = itemView.findViewById(R.id.message_content);
            tagTextView = itemView.findViewById(R.id.message_tag);
            timeTextView = itemView.findViewById(R.id.message_time);
            pomodoroMarksTextView = itemView.findViewById(R.id.pomodoro_marks);
            checkBox = itemView.findViewById(R.id.message_checkbox);
            deleteButton = itemView.findViewById(R.id.action_delete_subtask);
            pomodoroButton = itemView.findViewById(R.id.action_pomodoro_subtask);
        }
    }
}
