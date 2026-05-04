package com.example.myapplication.ui.projects;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imageview.Project;
import com.example.imageview.ProjectEditorBottomSheet;
import com.example.imageview.ProjectRepository;
import com.example.imageview.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class ProjectsFragment extends Fragment {

    private ProjectRepository projectRepository;
    private RecyclerView recyclerView;
    private ProjectsAdapter adapter;
    private final List<Project> projectList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        recyclerView = view.findViewById(R.id.recycler_view_projects);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        if (getContext() != null) {
            adapter = new ProjectsAdapter(projectList, getContext());
            recyclerView.setAdapter(adapter);
            projectRepository = new ProjectRepository(getContext());
        }

        FloatingActionButton fab = view.findViewById(R.id.fab_add_project);
        if (fab != null) {
            fab.setOnClickListener(v -> showAddProjectDialog());
        }

        loadProjects();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadProjects();
    }

    private void loadProjects() {
        if (projectRepository == null) {
            return;
        }

        projectRepository.getAllWithSubtaskCounts(result -> {
            if (getActivity() == null) {
                return;
            }
            projectList.clear();
            projectList.addAll(result.getProjects());
            if (adapter != null) {
                adapter.setSubtaskCountMap(result.getSubtaskCountMap());
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showAddProjectDialog() {
        if (getContext() == null || projectRepository == null) {
            return;
        }

        ProjectEditorBottomSheet sheet = ProjectEditorBottomSheet.newCreateInstance();
        sheet.setProjectEditorListener(project -> projectRepository.insert(project, ignored -> {
            if (getActivity() == null) {
                return;
            }
            Toast.makeText(getContext(), "Project created", Toast.LENGTH_SHORT).show();
            loadProjects();
        }));
        sheet.show(getParentFragmentManager(), "project_editor");
    }
}
