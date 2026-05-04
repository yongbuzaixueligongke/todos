package com.example.imageview;

import android.content.Context;

public class TaskEditorUseCase {

    private final Context appContext;
    private final TodoRepository todoRepository;

    public TaskEditorUseCase(Context context) {
        appContext = context.getApplicationContext();
        todoRepository = new TodoRepository(appContext);
    }

    public void save(TaskEditorState state, TodoItem editingItem, DatabaseExecutor.Callback<Long> callback) {
        TodoItem target = editingItem != null
                ? editingItem
                : new TodoItem(
                        state.getTitle(),
                        state.getDescription(),
                        "Just now",
                        false,
                        state.getDate(),
                        state.getStartTime(),
                        "",
                        state.getTag(),
                        state.getDescription(),
                        state.getProjectId()
                );

        target.setTitle(state.getTitle());
        target.setContent(state.getDescription());
        target.setDescription(state.getDescription());
        target.setStartTime(state.getDate());
        target.setStartClockTimeValue(state.getStartTime());
        target.setEndTime(state.getEndTime());
        target.setStartDateTimeMillis(state.getDate().isEmpty()
                ? 0L
                : DateTimeUtils.parseTaskStartMillis(state.getDate(), state.getStartTime()));
        target.setEndDateTimeMillis(DateTimeUtils.parseDateTimeMillis(state.getDate(), state.getEndTime()));
        target.setHasExplicitStartTime(!state.getStartTime().isEmpty());
        target.setTag(state.getTag());
        target.setPriority(state.getPriority());
        target.setTime(DateTimeUtils.formatTaskSchedule(
                target.getStartDateTimeMillis(),
                target.getEndDateTimeMillis(),
                target.hasExplicitStartTime()
        ));
        target.setReminderEnabled(state.isReminderEnabled());
        target.setReminderTimeMillis(state.isReminderEnabled() ? buildReminderMillis(state) : 0L);

        long previousId = target.getId();
        todoRepository.saveWithProject(
                target,
                state.getProjectId(),
                state.getProjectName(),
                state.getDate(),
                savedId -> {
                    long reminderTargetId = previousId > 0L ? previousId : savedId;
                    ReminderScheduler.cancelReminder(appContext, reminderTargetId);
                    if (target.isReminderEnabled() && target.getReminderTimeMillis() > 0L) {
                        ReminderScheduler.scheduleReminder(appContext, target);
                    }
                    if (callback != null) {
                        callback.onComplete(savedId);
                    }
                }
        );
    }

    public void delete(TodoItem item, DatabaseExecutor.Callback<Boolean> callback) {
        if (item == null) {
            if (callback != null) {
                callback.onComplete(false);
            }
            return;
        }
        ReminderScheduler.cancelReminder(appContext, item.getId());
        todoRepository.deleteById(item.getId(), callback);
    }

    public long buildReminderMillis(TaskEditorState state) {
        if (state == null || state.getDate().trim().isEmpty()) {
            return 0L;
        }
        return DateTimeUtils.parseTaskStartMillis(state.getDate(), state.getStartTime());
    }
}
