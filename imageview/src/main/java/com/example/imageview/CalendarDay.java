package com.example.imageview;

import java.util.Calendar;
import java.util.List;

public class CalendarDay {
    private final Calendar date;
    private final boolean inCurrentMonth;
    private final boolean isToday;
    private List<TodoItem> todos;

    public CalendarDay(Calendar date, boolean inCurrentMonth, boolean isToday) {
        this.date = date;
        this.inCurrentMonth = inCurrentMonth;
        this.isToday = isToday;
    }

    public Calendar getDate() {
        return date;
    }

    public int getDayOfMonth() {
        return date.get(Calendar.DAY_OF_MONTH);
    }

    public boolean isInCurrentMonth() {
        return inCurrentMonth;
    }

    public boolean isToday() {
        return isToday;
    }

    public List<TodoItem> getTodos() {
        return todos;
    }

    public void setTodos(List<TodoItem> todos) {
        this.todos = todos;
    }

    public boolean hasTodos() {
        return todos != null && !todos.isEmpty();
    }
}

