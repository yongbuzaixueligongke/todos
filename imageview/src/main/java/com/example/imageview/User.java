package com.example.imageview;

import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "users",
        indices = {@Index(value = {"username"}, unique = true)}
)
public class User {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private String username;
    private String passwordHash;
    private String passwordSalt;
    private String nickname;
    private String avatarUri;
    private long createdAt;
    private long updatedAt;

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getPasswordSalt() {
        return passwordSalt;
    }

    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getAvatarUri() {
        return avatarUri;
    }

    public void setAvatarUri(String avatarUri) {
        this.avatarUri = avatarUri;
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
}
