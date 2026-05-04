package com.example.imageview;

import android.os.Bundle;

public class TaskEditorState {

    private static final String ARG_TODO_ID = "todo_id";
    private static final String ARG_PROJECT_ID = "project_id";
    private static final String ARG_PROJECT_NAME = "project_name";
    private static final String ARG_TASK_TITLE = "task_title";
    private static final String ARG_TASK_DESCRIPTION = "task_description";
    private static final String ARG_TASK_DATE = "task_date";
    private static final String ARG_TASK_TIME = "task_time";
    private static final String ARG_TASK_END_TIME = "task_end_time";
    private static final String ARG_TAG_VALUE = "tag_value";
    private static final String ARG_PRIORITY_VALUE = "priority_value";
    private static final String ARG_REMINDER_ENABLED = "reminder_enabled";
    private static final String ARG_REMINDER_TIME = "reminder_time";

    private long todoId = -1L;
    private long projectId = 0L;
    private String projectName = "";
    private String title = "";
    private String description = "";
    private String date = "";
    private String startTime = "";
    private String endTime = "";
    private String tag = "";
    private int priority = 0;
    private boolean reminderEnabled = false;
    private long reminderTimeMillis = 0L;

    public static TaskEditorState fromArguments(Bundle args) {
        TaskEditorState state = new TaskEditorState();
        if (args == null) {
            return state;
        }
        state.todoId = args.getLong(ARG_TODO_ID, -1L);
        state.projectId = args.getLong(ARG_PROJECT_ID, 0L);
        state.projectName = safe(args.getString(ARG_PROJECT_NAME, ""));
        state.title = safe(args.getString(ARG_TASK_TITLE, ""));
        state.description = safe(args.getString(ARG_TASK_DESCRIPTION, ""));
        state.date = safe(args.getString(ARG_TASK_DATE, ""));
        state.startTime = safe(args.getString(ARG_TASK_TIME, ""));
        state.endTime = safe(args.getString(ARG_TASK_END_TIME, ""));
        state.tag = safe(args.getString(ARG_TAG_VALUE, ""));
        state.priority = args.getInt(ARG_PRIORITY_VALUE, 0);
        state.reminderEnabled = args.getBoolean(ARG_REMINDER_ENABLED, false);
        state.reminderTimeMillis = args.getLong(ARG_REMINDER_TIME, 0L);
        return state;
    }

    public void writeTo(Bundle args) {
        if (args == null) {
            return;
        }
        args.putLong(ARG_TODO_ID, todoId);
        args.putLong(ARG_PROJECT_ID, projectId);
        args.putString(ARG_PROJECT_NAME, projectName);
        args.putString(ARG_TASK_TITLE, title);
        args.putString(ARG_TASK_DESCRIPTION, description);
        args.putString(ARG_TASK_DATE, date);
        args.putString(ARG_TASK_TIME, startTime);
        args.putString(ARG_TASK_END_TIME, endTime);
        args.putString(ARG_TAG_VALUE, tag);
        args.putInt(ARG_PRIORITY_VALUE, priority);
        args.putBoolean(ARG_REMINDER_ENABLED, reminderEnabled);
        args.putLong(ARG_REMINDER_TIME, reminderTimeMillis);
    }

    public void applyTodoItem(TodoItem item) {
        if (item == null) {
            return;
        }
        todoId = item.getId();
        projectId = item.getProjectId();
        projectName = safe(item.getProject());
        title = safe(item.getTitle());
        description = safe(item.getDescription());
        date = safe(item.getStartTime());
        startTime = safe(item.getStartClockTimeValue());
        endTime = safe(item.getEndTime());
        tag = safe(item.getTag());
        priority = item.getPriority();
        reminderEnabled = item.isReminderEnabled();
        reminderTimeMillis = item.getReminderTimeMillis();
    }

    public long getTodoId() {
        return todoId;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = safe(projectName);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = safe(title);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = safe(description);
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = safe(date);
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = safe(startTime);
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = safe(endTime);
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = safe(tag);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
    }

    public long getReminderTimeMillis() {
        return reminderTimeMillis;
    }

    public void setReminderTimeMillis(long reminderTimeMillis) {
        this.reminderTimeMillis = reminderTimeMillis;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
