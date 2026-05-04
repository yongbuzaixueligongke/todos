package com.example.imageview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class NavigationHelper {

    public static void navigateToCalendar(Context context) {
        Intent intent = new Intent(context, CalendarActivity.class);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    public static void navigateToProfile(Context context) {
        Intent intent = new Intent(context, ProfileActivity.class);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    public static void navigateToMessage(Context context) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    public static void navigateToMessage(Context context, String taskFilter) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra(NavigationConstants.EXTRA_TASK_FILTER, taskFilter);
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    public static void navigateToProjects(Context context, boolean navigateToProjects) {
        Intent intent = new Intent(context, MessageActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (navigateToProjects) {
            intent.putExtra(NavigationConstants.EXTRA_NAVIGATE_TO_PROJECTS, true);
        }
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(0, 0);
        }
    }

    public static void navigateToProjectDetail(Context context, long projectId) {
        Intent intent = new Intent(context, ProjectDetailActivity.class);
        intent.putExtra(NavigationConstants.EXTRA_PROJECT_ID, projectId);
        context.startActivity(intent);
    }

    public static void navigateToRegister(Context context) {
        context.startActivity(new Intent(context, RegisterActivity.class));
    }

    public static void navigateToLogin(Context context) {
        context.startActivity(new Intent(context, LoginActivity.class));
    }

    public static void navigateToImageView(Context context) {
        context.startActivity(new Intent(context, ImageViewActivity.class));
    }

    public static void navigateToMusic(Context context) {
        context.startActivity(new Intent(context, MusicActivity.class));
    }

    public static void navigateToTodoList(Context context, String taskFilter) {
        Intent intent = new Intent(context, TodoListActivity.class);
        intent.putExtra(NavigationConstants.EXTRA_TASK_FILTER, taskFilter);
        context.startActivity(intent);
    }

    public static void navigateToTodoListByTag(Context context, String tag) {
        Intent intent = new Intent(context, TodoListActivity.class);
        intent.putExtra(NavigationConstants.EXTRA_TASK_FILTER, NavigationConstants.FILTER_ALL_TASKS);
        intent.putExtra(NavigationConstants.EXTRA_TAG_FILTER, tag);
        context.startActivity(intent);
    }

    public static void navigateToTagManagement(Context context) {
        context.startActivity(new Intent(context, TagManagementActivity.class));
    }
}
