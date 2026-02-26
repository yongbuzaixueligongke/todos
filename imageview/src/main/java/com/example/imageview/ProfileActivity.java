package com.example.imageview;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ListView listView = findViewById(R.id.profile_list_view);

        // 创建个人主页列表项
        List<String> profileItems = new ArrayList<>();
        profileItems.add("专注数据统计");
        profileItems.add("个性化");
        profileItems.add("音乐");
        profileItems.add("退出登录");
        profileItems.add("设置");

        // 创建适配器并设置到 ListView
        ProfileAdapter adapter = new ProfileAdapter(this, profileItems);
        listView.setAdapter(adapter);
        // 设置 ListView 的点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 3) { // 退出登录选项的索引
                    Intent intent = new Intent(ProfileActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    finish(); // 可选：结束当前活动
                }
                if (position == 0) {
                    Intent intent = new Intent(ProfileActivity.this, ImageViewActivity.class);
                    startActivity(intent);
                    finish(); // 可选：结束当前活动
                }
                if (position == 2) { // 音乐选项的索引
                    Intent intent = new Intent(ProfileActivity.this, MusicActivity.class);
                    startActivity(intent);
                    finish(); // 可选：结束当前活动
                }
            }
        });

        // 设置底部导航栏的点击事件
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_message) {
                    startActivity(new Intent(ProfileActivity.this, MessageActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_calendar) {
                    startActivity(new Intent(ProfileActivity.this, CalendarActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    //在当前界面，不需要跳转
                    return true;
                } else if (item.getItemId() == R.id.nav_projects) {
                    startActivity(new Intent(ProfileActivity.this, MessageActivity.class));
                    return true;
                }
                return false;
            }
        });

    }
}