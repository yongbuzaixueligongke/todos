package com.example.imageview;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;
import java.util.ArrayList;

public class MessageActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        drawerLayout = findViewById(R.id.drawer_layout);
        ListView listView = findViewById(R.id.message_list_view); // 引用 ListView

        // 创建消息列表
        List<Message> messages = new ArrayList<>();
        messages.add(new Message("Journal", "12/15", "5 minutes ago"));
        messages.add(new Message("Homework", "Android应用开发", "10 minutes ago"));
        messages.add(new Message("Hobbies", "音乐", "1 day ago"));
        messages.add(new Message("Courses", "移动应用开发实践", "1 day ago"));
        messages.add(new Message("Travel Planner", "长沙", "3 days ago"));
        // 添加更多消息...

        // 创建适配器并设置到 ListView
        MessageAdapter adapter = new MessageAdapter(this, messages);
        listView.setAdapter(adapter);


        // 设置底部导航栏的点击事件
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_message) {
                    // 当前在消息界面，不需要跳转
                    return true;
                } else if (item.getItemId() == R.id.nav_music) {
                    startActivity(new Intent(MessageActivity.this, MusicActivity.class));
                    return true;
                } else if (item.getItemId() == R.id.nav_profile) {
                    startActivity(new Intent(MessageActivity.this, ProfileActivity.class));
                    return true;
                }
                return false;
            }
        });
    }

    public void onUserInfoClick(View view) {
        // 打开导航栏
        drawerLayout.openDrawer(GravityCompat.START);
    }
}