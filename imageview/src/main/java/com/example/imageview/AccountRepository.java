package com.example.imageview;

import android.content.Context;
import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class AccountRepository {

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final User user;

        private AuthResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public static AuthResult success(User user) {
            return new AuthResult(true, "", user);
        }

        public static AuthResult failure(String message) {
            return new AuthResult(false, message, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public User getUser() {
            return user;
        }
    }

    private final UserDao userDao;

    public AccountRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context.getApplicationContext());
        userDao = database.userDao();
    }

    public void register(
            String username,
            String password,
            String nickname,
            DatabaseExecutor.Callback<AuthResult> callback
    ) {
        DatabaseExecutor.execute(() -> {
            String normalizedUsername = normalizeUsername(username);
            if (userDao.getByUsername(normalizedUsername) != null) {
                return AuthResult.failure("Username already exists");
            }

            long now = System.currentTimeMillis();
            byte[] salt = newSalt();
            User user = new User();
            user.setUsername(normalizedUsername);
            user.setPasswordSalt(encode(salt));
            user.setPasswordHash(hashPassword(password, salt));
            user.setNickname(normalizeNickname(nickname, normalizedUsername));
            user.setAvatarUri("");
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
            long id = userDao.insert(user);
            user.setId(id);
            return AuthResult.success(user);
        }, callback);
    }

    public void login(
            String username,
            String password,
            DatabaseExecutor.Callback<AuthResult> callback
    ) {
        DatabaseExecutor.execute(() -> {
            String normalizedUsername = normalizeUsername(username);
            User user = userDao.getByUsername(normalizedUsername);
            if (user == null) {
                return AuthResult.failure("User does not exist");
            }

            String expectedHash = hashPassword(password, Base64.decode(user.getPasswordSalt(), Base64.NO_WRAP));
            if (!expectedHash.equals(user.getPasswordHash())) {
                return AuthResult.failure("Incorrect password");
            }
            return AuthResult.success(user);
        }, callback);
    }

    public void getUser(long userId, DatabaseExecutor.Callback<User> callback) {
        DatabaseExecutor.execute(() -> userDao.getById(userId), callback);
    }

    public void updateProfile(long userId, String nickname, String avatarUri, DatabaseExecutor.Callback<User> callback) {
        DatabaseExecutor.execute(() -> {
            User user = userDao.getById(userId);
            if (user == null) {
                return null;
            }
            String nextNickname = normalizeNickname(nickname, user.getUsername());
            String nextAvatarUri = avatarUri == null ? "" : avatarUri;
            userDao.updateProfile(userId, nextNickname, nextAvatarUri, System.currentTimeMillis());
            user.setNickname(nextNickname);
            user.setAvatarUri(nextAvatarUri);
            return user;
        }, callback);
    }

    private static String normalizeUsername(String username) {
        return username == null ? "" : username.trim();
    }

    private static String normalizeNickname(String nickname, String fallbackUsername) {
        String cleanNickname = nickname == null ? "" : nickname.trim();
        return cleanNickname.isEmpty() ? fallbackUsername : cleanNickname;
    }

    private static byte[] newSalt() {
        byte[] salt = new byte[16];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    private static String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(salt);
            byte[] passwordBytes = (password == null ? "" : password).getBytes(StandardCharsets.UTF_8);
            byte[] hashed = digest.digest(passwordBytes);
            return encode(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }
}
