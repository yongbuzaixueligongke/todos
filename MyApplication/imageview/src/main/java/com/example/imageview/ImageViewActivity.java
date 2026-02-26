package com.example.imageview;

import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.Button;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class ImageViewActivity extends AppCompatActivity {

    private ImageView imagePic;
    private Button btnNext;
    private Button btnRegister;

    private int[] images = {
            R.drawable.chart_01,
            R.drawable.chart_02,
            R.drawable.chart_03,
            R.drawable.chart_04,
            R.drawable.pic01,
            R.drawable.pic_02,
            R.drawable.pic_03,
            R.drawable.pic_04,
            R.drawable.pic_05,
            R.drawable.pic_06,
            R.drawable.pic_07,
    };

    private int currentIndex = 0;
    private GestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 使用原来的布局

        imagePic = findViewById(R.id.imagePic);
        btnNext = findViewById(R.id.btnNext);

        // 显示第一张图片
        imagePic.setImageResource(images[currentIndex]);

        // 设置按钮点击事件
        btnNext.setOnClickListener(v -> showNextImage());

        // 初始化手势检测器
        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                // 检测滑动手势
                if (e1.getX() < e2.getX()) {
                    // 向右滑动
                    return false; // 不处理向右滑动
                } else {
                    // 向左滑动
                    showNextImage();
                    return true;
                }
            }
        });

        // 设置触摸事件监听器
        imagePic.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
    }

    private void showNextImage() {
        // 更新当前索引
        currentIndex++;
        if (currentIndex >= images.length) {
            currentIndex = 0; // 循环到第一张
        }
        imagePic.setImageResource(images[currentIndex]); // 显示下一张图片
    }
}