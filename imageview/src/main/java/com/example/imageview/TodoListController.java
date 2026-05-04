package com.example.imageview;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class TodoListController {

    public interface Listener {
        void onListChanged(
                List<TodoItem> filteredItems,
                List<TodoAdapter.DisplayItem> displayItems,
                List<String> availableTags
        );
    }

    private final TodoRepository todoRepository;
    private final List<TodoItem> allTodos = new ArrayList<>();
    private final TodoFilterOptions options = new TodoFilterOptions();

    public TodoListController(Context context) {
        todoRepository = new TodoRepository(context);
    }

    public TodoFilterOptions getOptions() {
        return options;
    }

    public void load(Listener listener) {
        todoRepository.getAll(todos -> {
            allTodos.clear();
            if (todos != null) {
                allTodos.addAll(todos);
            }
            dispatch(listener);
        });
    }

    public void refresh(Listener listener) {
        dispatch(listener);
    }

    public void toggleCompleted(TodoItem item, boolean completed, Listener listener) {
        if (item == null) {
            return;
        }
        item.setCompleted(completed);
        todoRepository.update(item, ignored -> load(listener));
    }

    public void updateTodo(TodoItem item, Listener listener) {
        if (item == null) {
            return;
        }
        todoRepository.update(item, ignored -> load(listener));
    }

    public void deleteTodo(TodoItem item, Listener listener) {
        if (item == null) {
            return;
        }
        todoRepository.deleteById(item.getId(), ignored -> load(listener));
    }

    public void moveTodoToToday(TodoItem item, Listener listener) {
        if (item == null) {
            return;
        }
        String today = DateTimeUtils.formatDate(System.currentTimeMillis());
        item.setStartTime(today);
        item.setEndTime("");
        item.setStartDateTimeMillis(DateTimeUtils.parseTaskStartMillis(today, ""));
        item.setEndDateTimeMillis(0L);
        item.setHasExplicitStartTime(false);
        item.setTime(DateTimeUtils.formatTaskSchedule(
                item.getStartDateTimeMillis(),
                item.getEndDateTimeMillis(),
                item.hasExplicitStartTime()
        ));
        item.setCompleted(false);
        if (TodoConstants.STATUS_ARCHIVED.equalsIgnoreCase(item.getStatus())) {
            item.setStatus(TodoConstants.STATUS_TODO);
        }
        updateTodo(item, listener);
    }

    public void moveTodoToLater(TodoItem item, Listener listener) {
        if (item == null) {
            return;
        }
        item.setStartTime("");
        item.setEndTime("");
        item.setStartDateTimeMillis(0L);
        item.setEndDateTimeMillis(0L);
        item.setHasExplicitStartTime(false);
        item.setTime("No schedule");
        item.setCompleted(false);
        if (TodoConstants.STATUS_ARCHIVED.equalsIgnoreCase(item.getStatus())) {
            item.setStatus(TodoConstants.STATUS_TODO);
        }
        updateTodo(item, listener);
    }

    public void completeTodo(TodoItem item, Listener listener) {
        if (item == null) {
            return;
        }
        item.setCompleted(true);
        updateTodo(item, listener);
    }

    private void dispatch(Listener listener) {
        if (listener == null) {
            return;
        }
        List<TodoItem> filtered = buildFilteredItems();
        listener.onListChanged(filtered, buildDisplayItems(filtered), buildAvailableTags());
    }

    private List<TodoItem> buildFilteredItems() {
        String keyword = safe(options.getSearchQuery()).trim().toLowerCase(Locale.getDefault());
        List<TodoItem> filtered = new ArrayList<>();
        for (TodoItem item : allTodos) {
            if (!matchesTaskFilter(item)) {
                continue;
            }
            if (options.isDateFilteringEnabled() && !matchesDateFilter(item)) {
                continue;
            }
            if (options.hasFixedTagFilter()) {
                if (!safe(item.getTag()).equalsIgnoreCase(options.getFixedTag())) {
                    continue;
                }
            } else if (!TodoConstants.ALL_TAGS_LABEL.equals(options.getSelectedTag())
                    && !safe(item.getTag()).equalsIgnoreCase(options.getSelectedTag())) {
                continue;
            }
            if (!keyword.isEmpty()
                    && !contains(item.getTitle(), keyword)
                    && !contains(item.getContent(), keyword)
                    && !contains(item.getDescription(), keyword)) {
                continue;
            }
            filtered.add(item);
        }

        sortTodos(filtered);
        return filtered;
    }

    private List<TodoAdapter.DisplayItem> buildDisplayItems(List<TodoItem> items) {
        if (!options.isGroupByDate() || !options.isDateFilteringEnabled()) {
            List<TodoAdapter.DisplayItem> rows = new ArrayList<>();
            for (TodoItem item : items) {
                rows.add(TodoAdapter.DisplayItem.todo(item));
            }
            return rows;
        }

        List<TodoAdapter.DisplayItem> rows = new ArrayList<>();
        Calendar today = startOfToday();
        String lastGroup = "";
        for (TodoItem item : items) {
            Calendar taskDate = parseTaskDate(item);
            String groupTitle = formatDateGroupTitle(today, taskDate);
            if (!groupTitle.equals(lastGroup)) {
                rows.add(TodoAdapter.DisplayItem.header(groupTitle));
                lastGroup = groupTitle;
            }
            rows.add(TodoAdapter.DisplayItem.todo(item));
        }
        return rows;
    }

    private List<String> buildAvailableTags() {
        List<String> tags = new ArrayList<>();
        tags.add(TodoConstants.ALL_TAGS_LABEL);
        Collections.addAll(tags, PriorityTagUtils.PRIORITY_TAGS);
        for (TodoItem todo : allTodos) {
            String tag = safe(todo.getTag()).trim();
            if (!tag.isEmpty()
                    && !PriorityTagUtils.isPriorityTag(tag)
                    && !containsIgnoreCase(tags, tag)) {
                tags.add(tag);
            }
        }
        return tags;
    }

    private boolean matchesTaskFilter(TodoItem item) {
        String taskFilter = options.getTaskFilter();
        if (TodoConstants.FILTER_COMPLETED_TASKS.equals(taskFilter)) {
            return item.isCompleted();
        }
        if (TodoConstants.FILTER_ARCHIVED_TASKS.equals(taskFilter)) {
            return !item.isCompleted() && item.isOverdue();
        }
        return true;
    }

    private boolean matchesDateFilter(TodoItem item) {
        Calendar taskDate = parseTaskDate(item);
        if (taskDate == null) {
            return false;
        }

        Calendar start = startOfToday();
        Calendar end = startOfToday();
        if (options.getDateFilter() == TodoConstants.DATE_FILTER_TODAY) {
            end.add(Calendar.DAY_OF_YEAR, 1);
        } else if (options.getDateFilter() == TodoConstants.DATE_FILTER_NEXT_THREE_DAYS) {
            end.add(Calendar.DAY_OF_YEAR, 3);
        } else if (options.getDateFilter() == TodoConstants.DATE_FILTER_NEXT_WEEK) {
            end.add(Calendar.DAY_OF_YEAR, 7);
        } else {
            return true;
        }
        return !taskDate.before(start) && taskDate.before(end);
    }

    private void sortTodos(List<TodoItem> items) {
        Comparator<TodoItem> comparator;
        if (options.isGroupByDate() && options.isDateFilteringEnabled()) {
            comparator = Comparator
                    .comparingLong(TodoItem::getStartDateTimeMillis)
                    .thenComparing(buildUserSortComparator());
        } else {
            comparator = buildUserSortComparator();
        }

        Comparator<TodoItem> overdueLast = (left, right) -> {
            boolean leftOverdue = left.isOverdue();
            boolean rightOverdue = right.isOverdue();
            if (leftOverdue && !rightOverdue) {
                return 1;
            }
            if (!leftOverdue && rightOverdue) {
                return -1;
            }
            return 0;
        };
        Collections.sort(items, overdueLast.thenComparing(comparator));
    }

    private Comparator<TodoItem> buildUserSortComparator() {
        switch (options.getSortMode()) {
            case TodoConstants.SORT_CREATED_ASC:
                return Comparator.comparingLong(TodoItem::getCreatedAt);
            case TodoConstants.SORT_TITLE_ASC:
                return Comparator.comparing(item -> safe(item.getTitle()), String.CASE_INSENSITIVE_ORDER);
            case TodoConstants.SORT_TITLE_DESC:
                return (left, right) -> safe(right.getTitle()).compareToIgnoreCase(safe(left.getTitle()));
            case TodoConstants.SORT_CREATED_DESC:
            default:
                return (left, right) -> Long.compare(right.getCreatedAt(), left.getCreatedAt());
        }
    }

    private String formatDateGroupTitle(Calendar today, Calendar taskDate) {
        if (taskDate == null) {
            return "";
        }
        int dayOffset = daysBetween(today, taskDate);
        if (dayOffset == 0) {
            return "Today";
        }
        if (dayOffset == 1) {
            return "Tomorrow";
        }
        if (dayOffset == 2) {
            return "In 2 days";
        }
        return formatWeekday(taskDate);
    }

    private int daysBetween(Calendar start, Calendar target) {
        Calendar startDay = (Calendar) start.clone();
        Calendar targetDay = (Calendar) target.clone();
        startOfDay(startDay);
        startOfDay(targetDay);
        long diffMillis = targetDay.getTimeInMillis() - startDay.getTimeInMillis();
        return (int) (diffMillis / (24L * 60L * 60L * 1000L));
    }

    private String formatWeekday(Calendar day) {
        switch (day.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:
                return "Mon";
            case Calendar.TUESDAY:
                return "Tue";
            case Calendar.WEDNESDAY:
                return "Wed";
            case Calendar.THURSDAY:
                return "Thu";
            case Calendar.FRIDAY:
                return "Fri";
            case Calendar.SATURDAY:
                return "Sat";
            case Calendar.SUNDAY:
            default:
                return "Sun";
        }
    }

    private Calendar parseTaskDate(TodoItem item) {
        if (item == null || item.getStartDateTimeMillis() <= 0L) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(item.getStartDateTimeMillis());
        return calendar;
    }

    private Calendar startOfToday() {
        Calendar calendar = Calendar.getInstance();
        startOfDay(calendar);
        return calendar;
    }

    private void startOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private boolean contains(String text, String keyword) {
        return text != null && text.toLowerCase(Locale.getDefault()).contains(keyword);
    }

    private boolean containsIgnoreCase(List<String> values, String target) {
        for (String value : values) {
            if (value.equalsIgnoreCase(target)) {
                return true;
            }
        }
        return false;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
