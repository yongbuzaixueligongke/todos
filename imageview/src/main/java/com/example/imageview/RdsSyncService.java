package com.example.imageview;

import android.content.Context;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RdsSyncService {
    private static final String TAG = "RdsSync";
    private static RdsSyncService instance;
    private RdsConnectionManager connectionManager;
    private AppDatabase localDatabase;
    private ExecutorService executorService;
    private Context context;

    private RdsSyncService(Context context) {
        this.context = context.getApplicationContext();
        this.connectionManager = RdsConnectionManager.getInstance(context);
        this.localDatabase = AppDatabase.getInstance(context);
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public static synchronized RdsSyncService getInstance(Context context) {
        if (instance == null) {
            instance = new RdsSyncService(context);
        }
        return instance;
    }

    public interface SyncCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }

    public void syncAll(SyncCallback callback) {
        String userId = RdsAuthManager.getInstance(context).getCurrentUserId();
        if (userId == null) {
            callback.onFailure("用户未登录");
            return;
        }

        syncTodosToCloud(userId, new SyncCallback() {
            @Override
            public void onSuccess() {
                syncProjectsToCloud(userId, new SyncCallback() {
                    @Override
                    public void onSuccess() {
                        syncTodosFromCloud(userId, new SyncCallback() {
                            @Override
                            public void onSuccess() {
                                syncProjectsFromCloud(userId, callback);
                            }

                            @Override
                            public void onFailure(String errorMessage) {
                                callback.onFailure(errorMessage);
                            }
                        });
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        callback.onFailure(errorMessage);
                    }
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                callback.onFailure(errorMessage);
            }
        });
    }

    private void syncTodosToCloud(String userId, SyncCallback callback) {
        executorService.execute(() -> {
            List<TodoItem> unsyncedItems = localDatabase.todoDao().getUnsyncedItems();
            
            if (unsyncedItems.isEmpty()) {
                callback.onSuccess();
                return;
            }

            String insertSql = "INSERT INTO todo_items (uuid, user_id, title, content, time, completed, start_time, end_time, project, tag, description, project_id, sync_status, created_at, updated_at, synced_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), NOW())";
            List<List<Object>> batchParams = new ArrayList<>();

            for (TodoItem item : unsyncedItems) {
                try {
                    localDatabase.todoDao().updateSyncStatus(item.getId(), 1);
                    
                    List<Object> params = new ArrayList<>();
                    params.add(item.getUuid());
                    params.add(userId);
                    params.add(item.getTitle());
                    params.add(item.getContent());
                    params.add(item.getTime());
                    params.add(item.isCompleted() ? 1 : 0);
                    params.add(item.getStartTime());
                    params.add(item.getEndTime());
                    params.add(item.getProject());
                    params.add(item.getTag());
                    params.add(item.getDescription());
                    params.add(item.getProjectId());
                    params.add(2);
                    
                    batchParams.add(params);
                    
                } catch (Exception e) {
                    Log.e(TAG, "准备待办数据失败: " + item.getTitle(), e);
                    localDatabase.todoDao().updateSyncStatus(item.getId(), 3);
                }
            }

            if (!batchParams.isEmpty()) {
                connectionManager.executeBatchUpdate(insertSql, batchParams, new RdsConnectionManager.BatchUpdateCallback() {
                    @Override
                    public void onSuccess(int[] affectedRows) {
                        for (TodoItem item : unsyncedItems) {
                            localDatabase.todoDao().updateSyncStatusAndSyncedAt(item.getId(), 2, System.currentTimeMillis());
                        }
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "批量插入待办失败", new Exception(errorMessage));
                        for (TodoItem item : unsyncedItems) {
                            localDatabase.todoDao().updateSyncStatus(item.getId(), 3);
                        }
                        callback.onFailure(errorMessage);
                    }
                });
            } else {
                callback.onSuccess();
            }
        });
    }

    private void syncProjectsToCloud(String userId, SyncCallback callback) {
        executorService.execute(() -> {
            List<Project> unsyncedProjects = localDatabase.projectDao().getUnsyncedProjects();
            
            if (unsyncedProjects.isEmpty()) {
                callback.onSuccess();
                return;
            }

            String insertSql = "INSERT INTO projects (uuid, user_id, title, start_time, end_time, tag, remark, sync_status, created_at, updated_at, synced_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW(), NOW())";
            List<List<Object>> batchParams = new ArrayList<>();

            for (Project project : unsyncedProjects) {
                try {
                    localDatabase.projectDao().updateSyncStatus(project.getId(), 1);
                    
                    List<Object> params = new ArrayList<>();
                    params.add(project.getUuid());
                    params.add(userId);
                    params.add(project.getTitle());
                    params.add(project.getStartTime());
                    params.add(project.getEndTime());
                    params.add(project.getTag());
                    params.add(project.getRemark());
                    params.add(2);
                    
                    batchParams.add(params);
                    
                } catch (Exception e) {
                    Log.e(TAG, "准备项目数据失败: " + project.getTitle(), e);
                    localDatabase.projectDao().updateSyncStatus(project.getId(), 3);
                }
            }

            if (!batchParams.isEmpty()) {
                connectionManager.executeBatchUpdate(insertSql, batchParams, new RdsConnectionManager.BatchUpdateCallback() {
                    @Override
                    public void onSuccess(int[] affectedRows) {
                        for (Project project : unsyncedProjects) {
                            localDatabase.projectDao().updateSyncStatusAndSyncedAt(project.getId(), 2, System.currentTimeMillis());
                        }
                        callback.onSuccess();
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        Log.e(TAG, "批量插入项目失败", new Exception(errorMessage));
                        for (Project project : unsyncedProjects) {
                            localDatabase.projectDao().updateSyncStatus(project.getId(), 3);
                        }
                        callback.onFailure(errorMessage);
                    }
                });
            } else {
                callback.onSuccess();
            }
        });
    }

    private void syncTodosFromCloud(String userId, SyncCallback callback) {
        executorService.execute(() -> {
            String sql = "SELECT * FROM todo_items WHERE user_id = ?";
            List<Object> params = new ArrayList<>();
            params.add(userId);
            
            connectionManager.executeQuery(sql, params, new RdsConnectionManager.QueryCallback() {
                @Override
                public void onSuccess(List<Map<String, Object>> results) {
                    for (Map<String, Object> row : results) {
                        TodoItem cloudItem = mapToTodoItem(row);
                        
                        TodoItem localItem = localDatabase.todoDao().getByUuid(cloudItem.getUuid());
                        
                        if (localItem == null) {
                            localDatabase.todoDao().insert(cloudItem);
                        } else {
                            if (cloudItem.getUpdatedAt() > localItem.getUpdatedAt()) {
                                cloudItem.setId(localItem.getId());
                                localDatabase.todoDao().update(cloudItem);
                            }
                        }
                    }
                    callback.onSuccess();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "从云端同步待办失败", new Exception(errorMessage));
                    callback.onFailure(errorMessage);
                }
            });
        });
    }

    private void syncProjectsFromCloud(String userId, SyncCallback callback) {
        executorService.execute(() -> {
            String sql = "SELECT * FROM projects WHERE user_id = ?";
            List<Object> params = new ArrayList<>();
            params.add(userId);
            
            connectionManager.executeQuery(sql, params, new RdsConnectionManager.QueryCallback() {
                @Override
                public void onSuccess(List<Map<String, Object>> results) {
                    for (Map<String, Object> row : results) {
                        Project cloudProject = mapToProject(row);
                        
                        Project localProject = localDatabase.projectDao().getByUuid(cloudProject.getUuid());
                        
                        if (localProject == null) {
                            localDatabase.projectDao().insert(cloudProject);
                        } else {
                            if (cloudProject.getUpdatedAt() > localProject.getUpdatedAt()) {
                                cloudProject.setId(localProject.getId());
                                localDatabase.projectDao().update(cloudProject);
                            }
                        }
                    }
                    callback.onSuccess();
                }

                @Override
                public void onFailure(String errorMessage) {
                    Log.e(TAG, "从云端同步项目失败", new Exception(errorMessage));
                    callback.onFailure(errorMessage);
                }
            });
        });
    }

    private TodoItem mapToTodoItem(Map<String, Object> row) {
        String uuid = (String) row.get("uuid");
        String title = (String) row.get("title");
        String content = (String) row.get("content");
        String time = (String) row.get("time");
        boolean completed = ((Number) row.get("completed")).intValue() == 1;
        String startTime = (String) row.get("start_time");
        String endTime = (String) row.get("end_time");
        String project = (String) row.get("project");
        String tag = (String) row.get("tag");
        String description = (String) row.get("description");
        long projectId = ((Number) row.get("project_id")).longValue();
        
        long createdAt = 0;
        long updatedAt = 0;
        
        if (row.get("created_at") != null) {
            createdAt = ((java.sql.Timestamp) row.get("created_at")).getTime();
        }
        if (row.get("updated_at") != null) {
            updatedAt = ((java.sql.Timestamp) row.get("updated_at")).getTime();
        }
        
        TodoItem item = new TodoItem(title, content, time, completed, startTime, endTime, project, tag, description, projectId);
        item.setUuid(uuid);
        item.setCreatedAt(createdAt);
        item.setUpdatedAt(updatedAt);
        item.setSyncStatus(2);
        item.setSyncedAt(System.currentTimeMillis());
        
        return item;
    }

    private Project mapToProject(Map<String, Object> row) {
        String uuid = (String) row.get("uuid");
        String title = (String) row.get("title");
        String startTime = (String) row.get("start_time");
        String endTime = (String) row.get("end_time");
        String tag = (String) row.get("tag");
        String remark = (String) row.get("remark");
        
        long createdAt = 0;
        long updatedAt = 0;
        
        if (row.get("created_at") != null) {
            createdAt = ((java.sql.Timestamp) row.get("created_at")).getTime();
        }
        if (row.get("updated_at") != null) {
            updatedAt = ((java.sql.Timestamp) row.get("updated_at")).getTime();
        }
        
        Project project = new Project(title, startTime, endTime, tag, remark);
        project.setUuid(uuid);
        project.setCreatedAt(createdAt);
        project.setUpdatedAt(updatedAt);
        project.setSyncStatus(2);
        project.setSyncedAt(System.currentTimeMillis());
        
        return project;
    }

    public void shutdown() {
        executorService.shutdown();
        connectionManager.shutdown();
    }
}
