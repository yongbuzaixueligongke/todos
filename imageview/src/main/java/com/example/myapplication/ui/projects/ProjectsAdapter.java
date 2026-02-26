package com.example.myapplication.ui.projects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private final List<String> projectList;

    public ProjectsAdapter(List<String> projectList) {
        this.projectList = projectList;
    }

    @NonNull
    @Override
    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        String projectName = projectList.get(position);
        holder.projectNameTextView.setText(projectName);
    }

    @Override
    public int getItemCount() {
        return projectList != null ? projectList.size() : 0;
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView projectNameTextView;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectNameTextView = itemView.findViewById(android.R.id.text1);
        }
    }
}
