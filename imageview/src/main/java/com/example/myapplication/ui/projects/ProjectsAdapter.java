package com.example.myapplication.ui.projects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.imageview.Project;
import com.example.imageview.R;
import java.util.List;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private final List<Project> projectList;
    private final Context context;

    public ProjectsAdapter(List<Project> projectList, Context context) {
        this.projectList = projectList;
        this.context = context;
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
        if (projectList != null && position < projectList.size()) {
            Project project = projectList.get(position);
            if (project != null && holder.projectTitleTextView != null) {
                holder.projectTitleTextView.setText(project.getTitle());

                // 添加点击事件
                holder.itemView.setOnClickListener(v -> {
                    android.content.Intent intent = new android.content.Intent(context, com.example.imageview.ProjectDetailActivity.class);
                    intent.putExtra("project_id", project.getId());
                    context.startActivity(intent);
                });
            }
        }
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
