package com.example.imageview;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 兼容旧入口：统一跳转到真正的待办主页 MessageActivity。
 */
public class MainActivity extends AppCompatActivity {

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
