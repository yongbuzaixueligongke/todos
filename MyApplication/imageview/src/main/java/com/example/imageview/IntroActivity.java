package com.example.imageview;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class IntroActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private ImageView introImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, MessageActivity.class));
        finish();
    }

    private void startAutoScroll() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == 4) { // 假设有4个页面
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, 3000); // 每3秒切换一次
            }
        };
        handler.postDelayed(runnable, 3000); // 启动自动轮播
    }


}