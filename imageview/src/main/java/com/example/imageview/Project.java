package com.example.imageview;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * 椤圭洰瀹炰綋锛岀敤浜?Room 鏁版嵁搴撴寔涔呭寲銆? */
@Entity(tableName = "projects")
public class Project {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String title;
    private long startDateMillis;
    private long endDateMillis;
    @Ignore
    private String startTime;
    @Ignore
    private String endTime;
    private String tag;
    private String remark;
    private long createdAt;
    private long updatedAt;

    public Project() {
    }

    @Ignore
    public Project(String title, String startTime, String endTime, String tag, String remark) {
        long now = System.currentTimeMillis();
        this.title = title;
        setStartTimeInternal(startTime);
        setEndTimeInternal(endTime);
        this.tag = tag;
        this.remark = remark;
        this.createdAt = now;
        this.updatedAt = now;
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
        this.updatedAt = System.currentTimeMillis();
    }

    public String getStartTime() {
        String formatted = DateTimeUtils.formatDate(startDateMillis);
        return formatted.isEmpty() ? startTime : formatted;
    }

    public void setStartTime(String startTime) {
        setStartTimeInternal(startTime);
        this.updatedAt = System.currentTimeMillis();
    }

    public String getEndTime() {
        String formatted = DateTimeUtils.formatDate(endDateMillis);
        return formatted.isEmpty() ? endTime : formatted;
    }

    public void setEndTime(String endTime) {
        setEndTimeInternal(endTime);
        this.updatedAt = System.currentTimeMillis();
    }

    public long getStartDateMillis() {
        return startDateMillis;
    }

    public void setStartDateMillis(long startDateMillis) {
        this.startDateMillis = startDateMillis;
        this.startTime = DateTimeUtils.formatDate(startDateMillis);
        this.updatedAt = System.currentTimeMillis();
    }

    public long getEndDateMillis() {
        return endDateMillis;
    }

    public void setEndDateMillis(long endDateMillis) {
        this.endDateMillis = endDateMillis;
        this.endTime = DateTimeUtils.formatDate(endDateMillis);
        this.updatedAt = System.currentTimeMillis();
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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

    private void setStartTimeInternal(String startTime) {
        this.startTime = startTime;
        this.startDateMillis = DateTimeUtils.parseDateStartMillis(startTime);
    }

    private void setEndTimeInternal(String endTime) {
        this.endTime = endTime;
        this.endDateMillis = DateTimeUtils.parseDateEndMillis(endTime);
    }
}
