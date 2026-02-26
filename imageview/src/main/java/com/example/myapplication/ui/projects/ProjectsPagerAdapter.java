package com.example.myapplication.ui.projects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProjectsPagerAdapter extends FragmentStateAdapter {

    public ProjectsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Return a new fragment instance for each tab
        return new Fragment(); // Replace with actual fragment instances
    }

    @Override
    public int getItemCount() {
        return 3; // Example: 3 tabs
    }
}
