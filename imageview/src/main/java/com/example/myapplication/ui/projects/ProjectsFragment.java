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
import com.example.imageview.R;
import com.example.myapplication.ui.projects.ProjectsAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ProjectsFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view_projects);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 创建项目列表
        List<String> projects = new ArrayList<>();
        projects.add("学习计划");
        projects.add("工作任务");
        projects.add("健身目标");
        projects.add("旅行计划");

        // 创建适配器并设置到 RecyclerView
        ProjectsAdapter adapter = new ProjectsAdapter(projects);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = view.findViewById(R.id.fab_add_project);
        fab.setOnClickListener(v -> {
            // 处理项目创建
        });

        return view;
    }
}
