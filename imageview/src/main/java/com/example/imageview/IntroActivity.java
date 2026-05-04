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

    private static final int PAGE_COUNT = 4;
    private static final long AUTO_SCROLL_MS = 1200L;

    private ViewPager2 viewPager;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private ImageView introImageView;
    private boolean hasNavigated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        goToHome();
    }

    private void startAutoScroll() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage >= PAGE_COUNT) {
                    goToHome();
                    return;
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, AUTO_SCROLL_MS);
            }
        };
        handler.postDelayed(runnable, AUTO_SCROLL_MS);
    }

    private void scheduleGoHome() {
        runnable = this::goToHome;
        handler.postDelayed(runnable, AUTO_SCROLL_MS);
    }

    private void goToHome() {
        if (hasNavigated) {
            return;
        }
        hasNavigated = true;
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        Intent intent = new Intent(this, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
    }
}