package com.example.imageview;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 寰呭姙浜嬮」瀹炰綋锛岀敤浜?Room 鏁版嵁搴撴寔涔呭寲銆? */
@Entity(tableName = "todo_items")
public class TodoItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String content;
    @Ignore
    private String time;
    private boolean completed;
    @Ignore
    private String startTime;
    @Ignore
    private String endTime;
    @Ignore
    private String startClockTime;
    private long startDateTimeMillis;
    private long endDateTimeMillis;
    private boolean hasExplicitStartTime;
    private String project;
    private String tag;
    private String description;
    private long projectId;
    private String status;
    private int priority;
    private long createdAt;
    private long updatedAt;
    private boolean reminderEnabled;
    private long reminderTimeMillis;

    @Ignore
    public TodoItem(String title, String content, String time, boolean completed) {
        long now = System.currentTimeMillis();
        this.title = title;
        this.content = content;
        this.time = time;
        this.completed = completed;
        this.projectId = 0;
        this.status = completed ? "DONE" : "TODO";
        this.priority = 0;
        this.createdAt = now;
        this.updatedAt = now;
        this.reminderEnabled = false;
        this.reminderTimeMillis = 0L;
    }

    @Ignore
    public TodoItem(String title, String content, String time, boolean completed, String startTime, String endTime, String project, String tag, String description) {
        long now = System.currentTimeMillis();
        this.title = title;
        this.content = content;
        this.time = time;
        this.completed = completed;
        setScheduleInternal(startTime, endTime, "");
        this.project = project;
        this.tag = tag;
        this.description = description;
        this.projectId = 0;
        this.status = completed ? "DONE" : "TODO";
        this.priority = 0;
        this.createdAt = now;
        this.updatedAt = now;
        this.reminderEnabled = false;
        this.reminderTimeMillis = 0L;
    }

    public TodoItem() {
    }

    @Ignore
    public TodoItem(String title, String content, String time, boolean completed, String startTime, String endTime, String project, String tag, String description, long projectId) {
        long now = System.currentTimeMillis();
        this.title = title;
        this.content = content;
        this.time = time;
        this.completed = completed;
        setScheduleInternal(startTime, endTime, "");
        this.project = project;
        this.tag = tag;
        this.description = description;
        this.projectId = projectId;
        this.status = completed ? "DONE" : "TODO";
        this.priority = 0;
        this.createdAt = now;
        this.updatedAt = now;
        this.reminderEnabled = false;
        this.reminderTimeMillis = 0L;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        String formatted = DateTimeUtils.formatTaskSchedule(startDateTimeMillis, endDateTimeMillis, hasExplicitStartTime);
        return startDateTimeMillis > 0L ? formatted : time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        this.status = completed ? "DONE" : "TODO";
        this.updatedAt = System.currentTimeMillis();
    }

    public String getStartTime() {
        String formatted = DateTimeUtils.formatDate(startDateTimeMillis);
        return formatted.isEmpty() ? startTime : formatted;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
        this.startDateTimeMillis = startTime == null || startTime.trim().isEmpty()
                ? 0L
                : DateTimeUtils.parseTaskStartMillis(startTime, getStartClockTime());
        refreshDisplayTime();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getEndTime() {
        String formatted = endDateTimeMillis > 0L
                ? DateTimeUtils.formatTime(endDateTimeMillis)
                : "";
        return formatted.isEmpty() ? endTime : formatted;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
        String date = getStartTime();
        this.endDateTimeMillis = DateTimeUtils.parseDateTimeMillis(date, endTime);
        if (this.endDateTimeMillis > 0L && this.startDateTimeMillis <= 0L) {
            this.startDateTimeMillis = DateTimeUtils.parseTaskStartMillis(date, "");
        }
        refreshDisplayTime();
        this.updatedAt = System.currentTimeMillis();
    }

    public long getStartDateTimeMillis() {
        return startDateTimeMillis;
    }

    public void setStartDateTimeMillis(long startDateTimeMillis) {
        this.startDateTimeMillis = startDateTimeMillis;
        this.startTime = DateTimeUtils.formatDate(startDateTimeMillis);
        refreshDisplayTime();
        this.updatedAt = System.currentTimeMillis();
    }

    public long getEndDateTimeMillis() {
        return endDateTimeMillis;
    }

    public void setEndDateTimeMillis(long endDateTimeMillis) {
        this.endDateTimeMillis = endDateTimeMillis;
        this.endTime = endDateTimeMillis > 0L ? DateTimeUtils.formatTime(endDateTimeMillis) : "";
        refreshDisplayTime();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getStartClockTimeValue() {
        if (!hasExplicitStartTime && endDateTimeMillis <= 0L) {
            return "";
        }
        String formatted = DateTimeUtils.formatTime(startDateTimeMillis);
        return formatted.isEmpty() ? startClockTime : formatted;
    }

    public void setStartClockTimeValue(String startClockTime) {
        this.startClockTime = startClockTime;
        this.hasExplicitStartTime = startClockTime != null && !startClockTime.trim().isEmpty();
        String date = getStartTime();
        this.startDateTimeMillis = date == null || date.trim().isEmpty()
                ? 0L
                : DateTimeUtils.parseTaskStartMillis(date, startClockTime);
        refreshDisplayTime();
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean hasExplicitStartTime() {
        return hasExplicitStartTime;
    }

    public void setHasExplicitStartTime(boolean hasExplicitStartTime) {
        this.hasExplicitStartTime = hasExplicitStartTime;
        refreshDisplayTime();
        this.updatedAt = System.currentTimeMillis();
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
        this.updatedAt = System.currentTimeMillis();
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isReminderEnabled() {
        return reminderEnabled;
    }

    public void setReminderEnabled(boolean reminderEnabled) {
        this.reminderEnabled = reminderEnabled;
        this.updatedAt = System.currentTimeMillis();
    }

    public long getReminderTimeMillis() {
        return reminderTimeMillis;
    }

    public void setReminderTimeMillis(long reminderTimeMillis) {
        this.reminderTimeMillis = reminderTimeMillis;
        this.updatedAt = System.currentTimeMillis();
    }

    public boolean isOverdue() {
        if (completed || startDateTimeMillis <= 0L) {
            return false;
        }

        long boundaryMillis = endDateTimeMillis > 0L
                ? endDateTimeMillis
                : DateTimeUtils.endOfDayMillis(startDateTimeMillis);
        return boundaryMillis > 0L && boundaryMillis < System.currentTimeMillis();
    }

    private void setScheduleInternal(String date, String startTime, String endTime) {
        this.startTime = date;
        this.startClockTime = startTime;
        this.endTime = endTime;
        this.hasExplicitStartTime = startTime != null && !startTime.trim().isEmpty();
        this.startDateTimeMillis = date == null || date.trim().isEmpty()
                ? 0L
                : DateTimeUtils.parseTaskStartMillis(date, startTime);
        this.endDateTimeMillis = DateTimeUtils.parseDateTimeMillis(date, endTime);
        refreshDisplayTime();
    }

    private String getStartClockTime() {
        if (startDateTimeMillis > 0L) {
            return DateTimeUtils.formatTime(startDateTimeMillis);
        }
        return DateTimeUtils.DEFAULT_REMINDER_TIME;
    }

    private void refreshDisplayTime() {
        this.time = DateTimeUtils.formatTaskSchedule(startDateTimeMillis, endDateTimeMillis, hasExplicitStartTime);
        this.startTime = DateTimeUtils.formatDate(startDateTimeMillis);
        this.startClockTime = DateTimeUtils.formatTime(startDateTimeMillis);
        this.endTime = endDateTimeMillis > 0L ? DateTimeUtils.formatTime(endDateTimeMillis) : "";
    }
}
