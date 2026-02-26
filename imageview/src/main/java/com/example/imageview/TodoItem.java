package com.example.imageview;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 待办事项实体，用于 Room 数据库持久化。
 */
@Entity(tableName = "todo_items")
public class TodoItem {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private String content;
    private String time;
    private boolean completed;

    public TodoItem(String title, String content, String time, boolean completed) {
        this.title = title;
        this.content = content;
        this.time = time;
        this.completed = completed;
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
}
