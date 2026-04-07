package com.example.myapplication.ui.projects;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.imageview.AppDatabase;
import com.example.imageview.Project;
import com.example.imageview.ProjectDao;
import com.example.imageview.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class ProjectsFragment extends Fragment {

    private AppDatabase database;
    private ProjectDao projectDao;
    private RecyclerView recyclerView;
    private ProjectsAdapter adapter;
    private List<Project> projectList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_projects, container, false);

        // 初始化视图
        recyclerView = view.findViewById(R.id.recycler_view_projects);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        FloatingActionButton fab = view.findViewById(R.id.fab_add_project);
        if (fab != null) {
            fab.setOnClickListener(v -> showAddProjectDialog());
        }

        // 初始化数据库和加载项目列表
        if (getContext() != null) {
            database = AppDatabase.getInstance(getContext());
            projectDao = database.projectDao();

            // 加载项目列表（在后台线程中执行）
            new Thread(() -> {
                if (projectDao != null) {
                    projectList = projectDao.getAll();
                    if (projectList.isEmpty()) {
                        // 添加默认项目
                        addDefaultProjects();
                        projectList = projectDao.getAll();
                    }

                    // 在主线程中更新UI
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (recyclerView != null && getContext() != null) {
                                adapter = new ProjectsAdapter(projectList, getContext());
                                recyclerView.setAdapter(adapter);
                            }
                        });
                    }
                }
            }).start();
        }

        return view;
    }

    private void addDefaultProjects() {
        if (projectDao != null) {
            Project[] defaultProjects = {
                    new Project("学习计划", "", "", "学习", "这是一个学习计划项目"),
                    new Project("工作任务", "", "", "工作", "这是一个工作任务项目"),
                    new Project("健身目标", "", "", "健康", "这是一个健身目标项目"),
                    new Project("旅行计划", "", "", "旅行", "这是一个旅行计划项目")
            };
            for (Project project : defaultProjects) {
                projectDao.insert(project);
            }
        }
    }

    private void showAddProjectDialog() {
        if (getContext() != null && projectDao != null) {
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_project, null, false);
            EditText editTitle = dialogView.findViewById(R.id.edit_project_title);
            EditText editStartTime = dialogView.findViewById(R.id.edit_project_start_time);
            EditText editEndTime = dialogView.findViewById(R.id.edit_project_end_time);
            EditText editTag = dialogView.findViewById(R.id.edit_project_tag);
            EditText editRemark = dialogView.findViewById(R.id.edit_project_remark);

            // 为时间输入框添加日历选择器
            setupDatePicker(editStartTime);
            setupDatePicker(editEndTime);

            new AlertDialog.Builder(getContext())
                    .setTitle("创建项目")
                    .setView(dialogView)
                    .setPositiveButton("保存", (dialog, which) -> {
                        String title = editTitle.getText().toString().trim();
                        String startTime = editStartTime.getText().toString().trim();
                        String endTime = editEndTime.getText().toString().trim();
                        String tag = editTag.getText().toString().trim();
                        String remark = editRemark.getText().toString().trim();

                        if (title.isEmpty()) {
                            Toast.makeText(getContext(), "请输入项目标题", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // 在后台线程中插入数据库
                        new Thread(() -> {
                            Project newProject = new Project(title, startTime, endTime, tag, remark);
                            long id = projectDao.insert(newProject);
                            newProject.setId(id);

                            // 在主线程中更新UI
                            if (getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    if (projectList != null && adapter != null) {
                                        projectList.add(newProject);
                                        adapter.notifyItemInserted(projectList.size() - 1);
                                        Toast.makeText(getContext(), "已创建新项目", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        }).start();
                    })
                    .setNegativeButton("取消", null)
                    .show();
        }
    }

    private void setupDatePicker(final EditText editText) {
        // 设置点击事件
        editText.setOnClickListener(v -> showDatePicker(editText));
        
        // 设置焦点变化事件
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                showDatePicker(editText);
            }
        });
    }

    private void showDatePicker(final EditText editText) {
        if (getContext() != null) {
            // 获取当前日期
            java.util.Calendar calendar = java.util.Calendar.getInstance();
            int year = calendar.get(java.util.Calendar.YEAR);
            int month = calendar.get(java.util.Calendar.MONTH);
            int day = calendar.get(java.util.Calendar.DAY_OF_MONTH);

            // 创建日期选择器对话框
            android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(
                    getContext(),
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        // 格式化日期为 yyyy-MM-dd 格式
                        String date = String.format("%d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                        editText.setText(date);
                    },
                    year, month, day
            );

            // 显示日期选择器对话框
            datePickerDialog.show();
        }
    }
}
