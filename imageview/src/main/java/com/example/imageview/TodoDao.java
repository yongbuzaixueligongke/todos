package com.example.imageview;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TodoDao {
    @Query("SELECT * FROM todo_items")
    List<TodoItem> getAll();

    @Query("SELECT * FROM todo_items WHERE projectId = :projectId")
    List<TodoItem> getByProjectId(long projectId);

    @Insert
    long insert(TodoItem item);

    @Update
    void update(TodoItem item);

    @Delete
    void delete(TodoItem item);

    @Query("UPDATE todo_items SET completed = :completed WHERE id = :id")
    void setCompleted(long id, boolean completed);

    @Query("SELECT * FROM todo_items WHERE project = :project")
    List<TodoItem> getByProject(String project);

    @Query("SELECT * FROM todo_items WHERE tag = :tag")
    List<TodoItem> getByTag(String tag);

    @Query("SELECT * FROM todo_items WHERE id = :id")
    TodoItem getById(long id);

    @Query("SELECT * FROM todo_items WHERE uuid = :uuid")
    TodoItem getByUuid(String uuid);

    @Query("SELECT * FROM todo_items WHERE syncStatus != 2")
    List<TodoItem> getUnsyncedItems();

    @Query("UPDATE todo_items SET syncStatus = :syncStatus WHERE id = :id")
    void updateSyncStatus(long id, int syncStatus);

    @Query("UPDATE todo_items SET syncStatus = :syncStatus, syncedAt = :syncedAt WHERE id = :id")
    void updateSyncStatusAndSyncedAt(long id, int syncStatus, long syncedAt);

    @Query("SELECT * FROM todo_items WHERE updatedAt > :lastSyncTime")
    List<TodoItem> getItemsUpdatedAfter(long lastSyncTime);
}
