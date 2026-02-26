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
        setContentView(R.layout.activity_intro);

        viewPager = findViewById(R.id.viewPager);
        introImageView = findViewById(R.id.intro_image_view);
        IntroPageAdapter adapter = new IntroPageAdapter();
        viewPager.setAdapter(adapter);

        // 检查是否是第一次启动
        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean isFirstLaunch = preferences.getBoolean("isFirstLaunch", true);

        if (!isFirstLaunch) {
            // 如果不是第一次启动，直接显示一张启动页图片
            introImageView.setImageResource(R.drawable.intro_image); // 替换为您的启动页图片资源
            introImageView.setVisibility(View.VISIBLE);
            // 这里可以设置一个定时器，几秒后跳转到主活动
            new Handler().postDelayed(() -> {
                startActivity(new Intent(this, MessageActivity.class));
                finish();
            }, 3000); // 3秒后跳转
        } else {
            // 设置第一次启动标志
            preferences.edit().putBoolean("isFirstLaunch", false).apply();
            startAutoScroll(); // 如果是第一次启动，继续使用自动轮播
        }
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