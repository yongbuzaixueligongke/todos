package com.example.imageview;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREFS_NAME = "account_session";
    private static final String KEY_CURRENT_USER_ID = "current_user_id";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void login(long userId) {
        prefs.edit().putLong(KEY_CURRENT_USER_ID, userId).apply();
    }

    public void logout() {
        prefs.edit().remove(KEY_CURRENT_USER_ID).apply();
    }

    public boolean isLoggedIn() {
        return getCurrentUserId() > 0L;
    }

    public long getCurrentUserId() {
        return prefs.getLong(KEY_CURRENT_USER_ID, 0L);
    }
}
