package com.example.imageview;

public final class TodoConstants {

    public static final String STATUS_TODO = "TODO";
    public static final String STATUS_DONE = "DONE";
    public static final String STATUS_ARCHIVED = "ARCHIVED";

    public static final String FILTER_ALL_TASKS = NavigationConstants.FILTER_ALL_TASKS;
    public static final String FILTER_COMPLETED_TASKS = NavigationConstants.FILTER_COMPLETED_TASKS;
    public static final String FILTER_ARCHIVED_TASKS = NavigationConstants.FILTER_ARCHIVED_TASKS;

    public static final String ALL_TAGS_LABEL = "All Tags";

    public static final int DATE_FILTER_ANY = -1;
    public static final int DATE_FILTER_TODAY = 0;
    public static final int DATE_FILTER_NEXT_THREE_DAYS = 1;
    public static final int DATE_FILTER_NEXT_WEEK = 2;

    public static final int SORT_CREATED_DESC = 0;
    public static final int SORT_CREATED_ASC = 1;
    public static final int SORT_TITLE_ASC = 2;
    public static final int SORT_TITLE_DESC = 3;

    private TodoConstants() {
    }
}
