package com.example.imageview;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 正式启动路由页：统一跳转到待办主页，避免开屏链路不稳定。
 */
public class RescueActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Class<?> targetActivity = new SessionManager(this).isLoggedIn()
                ? MessageActivity.class
                : LoginActivity.class;
        startActivity(new Intent(this, targetActivity));
        finish();
    }
}

