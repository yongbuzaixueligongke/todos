package com.example.imageview;

import android.app.Activity;
import android.content.Intent;

public final class AccountGuard {

    private AccountGuard() {
    }

    public static boolean requireLogin(Activity activity) {
        if (new SessionManager(activity).isLoggedIn()) {
            return true;
        }
        Intent intent = new Intent(activity, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
        return false;
    }
}
