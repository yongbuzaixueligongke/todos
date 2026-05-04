package com.example.myapplication.ui.projects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.imageview.NavigationHelper;
import com.example.imageview.Project;
import com.example.imageview.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProjectsAdapter extends RecyclerView.Adapter<ProjectsAdapter.ProjectViewHolder> {

    private final List<Project> projectList;
    private final Context context;
    private Map<Long, Integer> subtaskCountMap;

    public ProjectsAdapter(List<Project> projectList, Context context) {
        this.projectList = projectList;
        this.context = context;
    }

    public void setSubtaskCountMap(Map<Long, Integer> subtaskCountMap) {
        this.subtaskCountMap = subtaskCountMap;
        notifyDataSetChanged();
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
        if (projectList == null || position >= projectList.size()) {
            return;
        }
        Project project = projectList.get(position);
        if (project == null) {
            return;
        }

        holder.projectTitleTextView.setText(project.getTitle());

        int count = 0;
        if (subtaskCountMap != null && subtaskCountMap.containsKey(project.getId())) {
            count = subtaskCountMap.get(project.getId());
        }
        holder.subtaskCountTextView.setText(count + " tasks");

        String dateStr = formatDate(project.getCreatedAt());
        holder.createdDateTextView.setText("Created " + dateStr);

        holder.itemView.setOnClickListener(v ->
                NavigationHelper.navigateToProjectDetail(context, project.getId()));
    }

    private String formatDate(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    @Override
    public int getItemCount() {
        return projectList != null ? projectList.size() : 0;
    }

    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
        TextView projectTitleTextView;
        TextView subtaskCountTextView;
        TextView createdDateTextView;

        public ProjectViewHolder(@NonNull View itemView) {
            super(itemView);
            projectTitleTextView = itemView.findViewById(R.id.project_title);
            subtaskCountTextView = itemView.findViewById(R.id.project_subtask_count);
            createdDateTextView = itemView.findViewById(R.id.project_created_date);
        }
    }
}
