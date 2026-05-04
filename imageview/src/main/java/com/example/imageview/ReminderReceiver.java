package com.example.imageview;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderReceiver extends BroadcastReceiver {

    public static final String CHANNEL_ID = "todo_reminder_channel";
    public static final String EXTRA_TODO_ID = "extra_todo_id";
    public static final String EXTRA_TITLE = "extra_title";
    public static final String EXTRA_TEXT = "extra_text";

    @Override
    public void onReceive(Context context, Intent intent) {
        createChannel(context);

        long todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L);
        String title = intent.getStringExtra(EXTRA_TITLE);
        String text = intent.getStringExtra(EXTRA_TEXT);

        Intent openIntent = new Intent(context, MessageActivity.class);
        openIntent.putExtra(NavigationConstants.EXTRA_TODO_ID, todoId);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(
                context,
                (int) todoId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle(title != null ? title : "Task Reminder")
                .setContentText(text != null ? text : "You have a scheduled reminder.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(contentIntent);

        NotificationManagerCompat.from(context).notify((int) todoId, builder.build());
    }

    private void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Task Reminders",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Reminder notifications for tasks");
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
