package com.project.scheduleasy;

import static android.content.Context.MODE_PRIVATE;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;

public class ClassNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getExtras() == null) return;

        String classId = intent.getStringExtra("classId");
        String className = intent.getStringExtra("className");
        String classTime = intent.getStringExtra("classTime");

        if (classId == null || className == null || classTime == null) return;


        AppDatabase database = AppDatabase.getInstance(context);
        List<StudentClass> students = database.studentClassDao().getStudentsForClass(classId);

        for (StudentClass student : students) {
            if (isCurrentUser(student.studentId, context)) {
                showNotification(context, className, classTime);
                break; // no need to check more students if current user is found
            }
        }

        // Reschedule all class notifications for the future
        new ClassNotificationScheduler(context).scheduleAllClassNotifications();
    }

    private boolean isCurrentUser(String studentId, Context context) {
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentUserId = prefs.getString("currentUserId", null);
        return studentId != null && studentId.equals(currentUserId);
    }

    private void showNotification(Context context, String className, String classTime) {
        String channelId = "class_reminders";
        createNotificationChannel(context, channelId);

        Notification notification = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_schedule)
                .setContentTitle(classTime)      // show only the time
                .setContentText(className)       // show only the subject
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build();

        /*NotificationManagerCompat.from(context)
                .notify((className + classTime).hashCode(), notification);*/

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify((className + classTime).hashCode(), notification);
    }


    private void createNotificationChannel(Context context, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(channelId) == null) {
                NotificationChannel channel = new NotificationChannel(
                        channelId,
                        "Class Reminders",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Notifies when your scheduled class is about to start");
                manager.createNotificationChannel(channel);
            }
        }
    }
}
