package com.example.imageview;

import android.app.Application;

public class TodoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化RDS MySQL服务
        RdsAuthManager.getInstance(this);
        RdsSyncService.getInstance(this);
        NetworkManager.getInstance(this);
    }
}
