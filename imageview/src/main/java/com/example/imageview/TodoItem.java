package com.example.imageview;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import java.util.UUID;

/**
 * 待办事项实体，用于 Room 数据库持久化。
 */
@Entity(tableName = "todo_items")
public class TodoItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String uuid;
    private String title;
    private String content;
    private String time;
    private boolean completed;
    private String startTime;
    private String endTime;
    private String project;
    private String tag;
    private String description;
    private long projectId;
    private int syncStatus;
    private long createdAt;
    private long updatedAt;
    private long syncedAt;

    @Ignore
    public TodoItem(String title, String content, String time, boolean completed) {
        this.uuid = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.time = time;
        this.completed = completed;
        this.projectId = 0;
        this.syncStatus = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.syncedAt = 0;
    }

    @Ignore
    public TodoItem(String title, String content, String time, boolean completed, String startTime, String endTime, String project, String tag, String description) {
        this.uuid = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.time = time;
        this.completed = completed;
        this.startTime = startTime;
        this.endTime = endTime;
        this.project = project;
        this.tag = tag;
        this.description = description;
        this.projectId = 0;
        this.syncStatus = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.syncedAt = 0;
    }

    public TodoItem(String title, String content, String time, boolean completed, String startTime, String endTime, String project, String tag, String description, long projectId) {
        this.uuid = UUID.randomUUID().toString();
        this.title = title;
        this.content = content;
        this.time = time;
        this.completed = completed;
        this.startTime = startTime;
        this.endTime = endTime;
        this.project = project;
        this.tag = tag;
        this.description = description;
        this.projectId = projectId;
        this.syncStatus = 0;
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.syncedAt = 0;
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
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getProjectId() {
        return projectId;
    }

    public void setProjectId(long projectId) {
        this.projectId = projectId;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getSyncStatus() {
        return syncStatus;
    }

    public void setSyncStatus(int syncStatus) {
        this.syncStatus = syncStatus;
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

    public long getSyncedAt() {
        return syncedAt;
    }

    public void setSyncedAt(long syncedAt) {
        this.syncedAt = syncedAt;
    }
}
