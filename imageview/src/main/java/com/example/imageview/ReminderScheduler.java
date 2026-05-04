package com.example.imageview;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public final class ReminderScheduler {

    private ReminderScheduler() {
    }

    public static void scheduleReminder(Context context, TodoItem item) {
        if (item == null || !item.isReminderEnabled() || item.getReminderTimeMillis() <= 0L) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pendingIntent = buildPendingIntent(context, item);
        try {
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    item.getReminderTimeMillis(),
                    pendingIntent
            );
        } catch (SecurityException e) {
            alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    item.getReminderTimeMillis(),
                    pendingIntent
            );
        } catch (Exception e) {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    item.getReminderTimeMillis(),
                    pendingIntent
            );
        }
    }

    public static void cancelReminder(Context context, long todoId) {
        if (todoId <= 0L) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }
        PendingIntent pendingIntent = buildPendingIntent(context, todoId, "", "");
        alarmManager.cancel(pendingIntent);
    }

    private static PendingIntent buildPendingIntent(Context context, TodoItem item) {
        return buildPendingIntent(
                context,
                item.getId(),
                item.getTitle(),
                item.getDescription() == null || item.getDescription().trim().isEmpty()
                        ? "Reminder time reached"
                        : item.getDescription()
        );
    }

    private static PendingIntent buildPendingIntent(Context context, long todoId, String title, String text) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra(ReminderReceiver.EXTRA_TODO_ID, todoId);
        intent.putExtra(ReminderReceiver.EXTRA_TITLE, title);
        intent.putExtra(ReminderReceiver.EXTRA_TEXT, text);
        return PendingIntent.getBroadcast(
                context,
                (int) todoId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
    }
}
