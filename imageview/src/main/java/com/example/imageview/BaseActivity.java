package com.example.imageview;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void setupBottomNavigation(BottomNavigationView bottomNav) {
        setupBottomNavigation(bottomNav, -1);
    }

    protected void setupBottomNavigation(BottomNavigationView bottomNav, int currentNavItemId) {
        if (currentNavItemId != -1) {
            bottomNav.setSelectedItemId(currentNavItemId);
        }

        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Class<?> currentClass = BaseActivity.this.getClass();

            if (itemId == R.id.nav_message) {
                if (currentClass != MessageActivity.class) {
                    NavigationHelper.navigateToMessage(BaseActivity.this);
                    finish();
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.nav_calendar) {
                if (currentClass != CalendarActivity.class) {
                    NavigationHelper.navigateToCalendar(BaseActivity.this);
                    finish();
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                if (currentClass != ProfileActivity.class) {
                    NavigationHelper.navigateToProfile(BaseActivity.this);
                    finish();
                    overridePendingTransition(0, 0);
                }
                return true;
            } else if (itemId == R.id.nav_projects) {
                if (currentClass != MessageActivity.class) {
                    NavigationHelper.navigateToProjects(BaseActivity.this, true);
                    finish();
                    overridePendingTransition(0, 0);
                }
                return true;
            }
            return false;
        });
    }
}