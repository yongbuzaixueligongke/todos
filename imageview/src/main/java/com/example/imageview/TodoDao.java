package com.example.imageview;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TodoDao {

    @Query("SELECT * FROM todo_items ORDER BY id ASC")
    List<TodoItem> getAll();

    @Insert
    long insert(TodoItem item);

    @Update
    void update(TodoItem item);

    @Delete
    void delete(TodoItem item);

    @Query("UPDATE todo_items SET completed = :completed WHERE id = :id")
    void setCompleted(long id, boolean completed);
}
