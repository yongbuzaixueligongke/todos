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

    @Query("SELECT * FROM todo_items WHERE startDateTimeMillis >= :startInclusive AND startDateTimeMillis < :endExclusive ORDER BY startDateTimeMillis ASC")
    List<TodoItem> getByDateRange(long startInclusive, long endExclusive);

    @Query("SELECT * FROM todo_items WHERE projectId = :projectId")
    List<TodoItem> getByProjectId(long projectId);

    @Insert
    long insert(TodoItem item);

    @Update
    void update(TodoItem item);

    @Delete
    void delete(TodoItem item);

    @Query("DELETE FROM todo_items WHERE id = :id")
    void deleteById(long id);

    @Query("DELETE FROM todo_items WHERE projectId = :projectId")
    void deleteByProjectId(long projectId);

    @Query("UPDATE todo_items SET completed = :completed WHERE id = :id")
    void setCompleted(long id, boolean completed);

    @Query("SELECT * FROM todo_items WHERE project = :project")
    List<TodoItem> getByProject(String project);

    @Query("SELECT * FROM todo_items WHERE tag = :tag")
    List<TodoItem> getByTag(String tag);

    @Query("SELECT * FROM todo_items WHERE id = :id")
    TodoItem getById(long id);

    @Query("UPDATE todo_items SET tag = '', updatedAt = :updatedAt WHERE tag = :tag COLLATE NOCASE")
    void clearTag(String tag, long updatedAt);

    @Query("SELECT projectId AS projectId, COUNT(*) AS taskCount FROM todo_items WHERE projectId > 0 GROUP BY projectId")
    List<ProjectTaskCount> getProjectTaskCounts();

}
