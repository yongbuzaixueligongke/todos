package com.example.myapplication.ui.projects;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.imageview.R;
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
                .inflate(R.layout.item_project, parent, false);
        return new ProjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
        String projectName = projectList.get(position);
        holder.projectTitleTextView.setText(projectName);
    }

    @Override
    public int getItemCount() {
        return projectList != null ? projectList.size() : 0;
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView projectTitleTextView;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectTitleTextView = itemView.findViewById(R.id.project_title);
        }
    }
}
