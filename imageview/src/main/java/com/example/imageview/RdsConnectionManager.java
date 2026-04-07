package com.example.imageview;

import android.content.Context;
import android.util.Log;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RdsConnectionManager {
    private static final String TAG = "RdsConnection";
    private static RdsConnectionManager instance;
    private Context context;
    private ExecutorService executorService;

    private RdsConnectionManager(Context context) {
        this.context = context.getApplicationContext();
        this.executorService = Executors.newFixedThreadPool(4);
    }

    public static synchronized RdsConnectionManager getInstance(Context context) {
        if (instance == null) {
            instance = new RdsConnectionManager(context);
        }
        return instance;
    }

    private Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(
                RdsConfig.DB_URL,
                RdsConfig.DB_USER,
                RdsConfig.DB_PASSWORD
            );
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "MySQL驱动未找到", e);
            throw new SQLException("MySQL驱动未找到: " + e.getMessage());
        }
    }

    public void executeQuery(String sql, List<Object> params, QueryCallback callback) {
        executorService.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = getConnection();
                stmt = conn.prepareStatement(sql);
                
                if (params != null) {
                    for (int i = 0; i < params.size(); i++) {
                        stmt.setObject(i + 1, params.get(i));
                    }
                }
                
                rs = stmt.executeQuery();
                List<Map<String, Object>> results = new ArrayList<>();
                
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    int columnCount = rs.getMetaData().getColumnCount();
                    for (int i = 1; i <= columnCount; i++) {
                        String columnName = rs.getMetaData().getColumnName(i);
                        Object value = rs.getObject(i);
                        row.put(columnName, value);
                    }
                    results.add(row);
                }
                
                callback.onSuccess(results);
                
            } catch (SQLException e) {
                Log.e(TAG, "查询失败", e);
                callback.onFailure(e.getMessage());
            } finally {
                closeResources(conn, stmt, rs);
            }
        });
    }

    public void executeUpdate(String sql, List<Object> params, UpdateCallback callback) {
        executorService.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            
            try {
                conn = getConnection();
                stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
                
                if (params != null) {
                    for (int i = 0; i < params.size(); i++) {
                        stmt.setObject(i + 1, params.get(i));
                    }
                }
                
                int affectedRows = stmt.executeUpdate();
                
                if (affectedRows == 0) {
                    callback.onFailure("操作失败，没有行被影响");
                    return;
                }
                
                long generatedId = -1;
                rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    generatedId = rs.getLong(1);
                }
                
                callback.onSuccess(generatedId, affectedRows);
                
            } catch (SQLException e) {
                Log.e(TAG, "更新失败", e);
                callback.onFailure(e.getMessage());
            } finally {
                closeResources(conn, stmt, rs);
            }
        });
    }

    public void executeBatchUpdate(String sql, List<List<Object>> batchParams, BatchUpdateCallback callback) {
        executorService.execute(() -> {
            Connection conn = null;
            PreparedStatement stmt = null;
            
            try {
                conn = getConnection();
                conn.setAutoCommit(false);
                stmt = conn.prepareStatement(sql);
                
                for (List<Object> params : batchParams) {
                    if (params != null) {
                        for (int i = 0; i < params.size(); i++) {
                            stmt.setObject(i + 1, params.get(i));
                        }
                        stmt.addBatch();
                    }
                }
                
                int[] affectedRows = stmt.executeBatch();
                conn.commit();
                
                callback.onSuccess(affectedRows);
                
            } catch (SQLException e) {
                Log.e(TAG, "批量更新失败", e);
                try {
                    if (conn != null) {
                        conn.rollback();
                    }
                } catch (SQLException ex) {
                    Log.e(TAG, "回滚失败", ex);
                }
                callback.onFailure(e.getMessage());
            } finally {
                try {
                    if (conn != null) {
                        conn.setAutoCommit(true);
                    }
                } catch (SQLException e) {
                    Log.e(TAG, "设置自动提交失败", e);
                }
                closeResources(conn, stmt, null);
            }
        });
    }

    private void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                Log.e(TAG, "关闭ResultSet失败", e);
            }
        }
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                Log.e(TAG, "关闭PreparedStatement失败", e);
            }
        }
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                Log.e(TAG, "关闭Connection失败", e);
            }
        }
    }

    public interface QueryCallback {
        void onSuccess(List<Map<String, Object>> results);
        void onFailure(String errorMessage);
    }

    public interface UpdateCallback {
        void onSuccess(long generatedId, int affectedRows);
        void onFailure(String errorMessage);
    }

    public interface BatchUpdateCallback {
        void onSuccess(int[] affectedRows);
        void onFailure(String errorMessage);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
