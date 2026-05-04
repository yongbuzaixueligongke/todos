package com.example.imageview;

public class TodoFilterOptions {

    private String searchQuery = "";
    private String selectedTag = TodoConstants.ALL_TAGS_LABEL;
    private String fixedTag = "";
    private String taskFilter = TodoConstants.FILTER_ALL_TASKS;
    private int dateFilter = TodoConstants.DATE_FILTER_ANY;
    private int sortMode = TodoConstants.SORT_CREATED_DESC;
    private boolean dateFilteringEnabled = false;
    private boolean groupByDate = false;

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery == null ? "" : searchQuery;
    }

    public String getSelectedTag() {
        return selectedTag;
    }

    public void setSelectedTag(String selectedTag) {
        this.selectedTag = selectedTag == null || selectedTag.trim().isEmpty()
                ? TodoConstants.ALL_TAGS_LABEL
                : selectedTag;
    }

    public String getFixedTag() {
        return fixedTag;
    }

    public void setFixedTag(String fixedTag) {
        this.fixedTag = fixedTag == null ? "" : fixedTag.trim();
    }

    public String getTaskFilter() {
        return taskFilter;
    }

    public void setTaskFilter(String taskFilter) {
        this.taskFilter = taskFilter == null ? TodoConstants.FILTER_ALL_TASKS : taskFilter;
    }

    public int getDateFilter() {
        return dateFilter;
    }

    public void setDateFilter(int dateFilter) {
        this.dateFilter = dateFilter;
    }

    public int getSortMode() {
        return sortMode;
    }

    public void setSortMode(int sortMode) {
        this.sortMode = sortMode;
    }

    public boolean isDateFilteringEnabled() {
        return dateFilteringEnabled;
    }

    public void setDateFilteringEnabled(boolean dateFilteringEnabled) {
        this.dateFilteringEnabled = dateFilteringEnabled;
    }

    public boolean isGroupByDate() {
        return groupByDate;
    }

    public void setGroupByDate(boolean groupByDate) {
        this.groupByDate = groupByDate;
    }

    public boolean hasFixedTagFilter() {
        return !fixedTag.isEmpty();
    }
}
