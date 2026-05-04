package com.example.imageview;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * 项目数据访问对象，用于数据库操作。
 */
@Dao
public interface ProjectDao {

    @Insert
    long insert(Project project);

    @Update
    void update(Project project);

    @Query("SELECT * FROM projects")
    List<Project> getAll();

    @Query("SELECT * FROM projects WHERE id = :id")
    Project getById(long id);

    @Query("DELETE FROM projects WHERE id = :id")
    void delete(long id);
}