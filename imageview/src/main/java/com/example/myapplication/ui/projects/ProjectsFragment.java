package com.example.myapplication.ui.projects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import java.util.Arrays;
import androidx.viewpager2.widget.ViewPager2;

public class ProjectsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(com.example.imageview.R.layout.fragment_projects, container, false);

        TabLayout tabLayout = view.findViewById(com.example.imageview.R.id.tab_layout_projects);
        ViewPager2 viewPager = view.findViewById(com.example.imageview.R.id.view_pager_projects);

        // Set up ViewPager2 with a simple adapter
        viewPager.setAdapter(new ProjectsPagerAdapter(this));

        // Link TabLayout and ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText("Tab " + (position + 1)); // Example tab titles
        }).attach();

        FloatingActionButton fab = view.findViewById(com.example.imageview.R.id.fab_add_project);
        fab.setOnClickListener(v -> {
            // Handle project creation
        });

        return view;
    }
}
