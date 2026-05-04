package com.example.imageview;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface UserDao {

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    User getById(long id);

    @Query("SELECT * FROM users WHERE username = :username COLLATE NOCASE LIMIT 1")
    User getByUsername(String username);

    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Query("UPDATE users SET nickname = :nickname, avatarUri = :avatarUri, updatedAt = :updatedAt WHERE id = :id")
    void updateProfile(long id, String nickname, String avatarUri, long updatedAt);
}
