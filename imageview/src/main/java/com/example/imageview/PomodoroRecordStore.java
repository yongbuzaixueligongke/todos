package com.example.imageview;

import android.content.Context;
import android.content.SharedPreferences;

public class PomodoroRecordStore {

    private static final String PREFS_NAME = "pomodoro_records";
    private static final String KEY_PREFIX = "todo_";

    private final SharedPreferences preferences;

    public PomodoroRecordStore(Context context) {
        preferences = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public int getCompletedCount(long todoId) {
        return preferences.getInt(KEY_PREFIX + todoId, 0);
    }

    public int incrementCompletedCount(long todoId) {
        int nextCount = getCompletedCount(todoId) + 1;
        preferences.edit().putInt(KEY_PREFIX + todoId, nextCount).apply();
        return nextCount;
    }

    public void clear(long todoId) {
        preferences.edit().remove(KEY_PREFIX + todoId).apply();
    }
}
