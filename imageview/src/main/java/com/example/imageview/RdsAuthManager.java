package com.example.imageview;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RdsAuthManager {
    private static final String TAG = "RdsAuth";
    private static RdsAuthManager instance;
    private Context context;
    private ExecutorService executorService;
    private String currentUserId;
    private String currentUserEmail;
    private RdsConnectionManager connectionManager;

    private RdsAuthManager(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(4);
        this.connectionManager = RdsConnectionManager.getInstance(context);
    }

    public static synchronized RdsAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new RdsAuthManager(context);
        }
        return instance;
    }

    public interface AuthCallback {
        void onSuccess(String userId);
        void onFailure(String errorMessage);
    }

    public void register(String email, String password, AuthCallback callback) {
        executorService.execute(() -> {
            try {
                String userId = UUID.randomUUID().toString();
                
                String sql = "INSERT INTO users (user_id, email, password, created_at, updated_at) VALUES (?, ?, ?, NOW(), NOW())";
                List<Object> params = new ArrayList<>();
                params.add(userId);
                params.add(email);
                params.add(password);
                
                connectionManager.executeUpdate(sql, params, new RdsConnectionManager.UpdateCallback() {
                    @Override
                    public void onSuccess(long generatedId, int affectedRows) {
                        currentUserId = userId;
                        currentUserEmail = email;
                        callback.onSuccess(userId);
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "注册失败", new Exception(errorMessage));
                        callback.onFailure("注册失败: " + errorMessage);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "注册失败", e);
                callback.onFailure("注册失败: " + e.getMessage());
            }
        });
    }

    public void login(String email, String password, AuthCallback callback) {
        executorService.execute(() -> {
            try {
                String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
                List<Object> params = new ArrayList<>();
                params.add(email);
                params.add(password);
                
                connectionManager.executeQuery(sql, params, new RdsConnectionManager.QueryCallback() {
                    @Override
                    public void onSuccess(List<Map<String, Object>> results) {
                        if (results != null && !results.isEmpty()) {
                            Map<String, Object> userData = results.get(0);
                            String userId = (String) userData.get("user_id");
                            
                            currentUserId = userId;
                            currentUserEmail = email;
                            
                            callback.onSuccess(userId);
                        } else {
                            callback.onFailure("邮箱或密码错误");
                        }
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "登录失败", new Exception(errorMessage));
                        callback.onFailure("登录失败: " + errorMessage);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "登录失败", e);
                callback.onFailure("登录失败: " + e.getMessage());
            }
        });
    }

    public void signOut() {
        currentUserId = null;
        currentUserEmail = null;
    }

    public boolean isUserLoggedIn() {
        return currentUserId != null && !currentUserId.isEmpty();
    }

    public String getCurrentUserId() {
        return currentUserId;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
