package com.project.scheduleasy;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ClassNotificationScheduler {
    private static final String TAG = "NotificationScheduler";
    private final Context context;
    private final AlarmManager alarmManager;

    public ClassNotificationScheduler(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void scheduleAllClassNotifications() {
        AppDatabase db = AppDatabase.getInstance(context);
        db.classDao().getAllClassesWithNotifications().forEach(this::scheduleSingleClass);
    }

    private void scheduleSingleClass(ClassSchedule classSchedule) {
        Intent intent = new Intent(context, ClassNotificationReceiver.class)
                .putExtra("classId", classSchedule.id)
                .putExtra("className", classSchedule.className);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                classSchedule.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        long triggerTime = calculateNextTrigger(classSchedule.dayOfWeek, classSchedule.startTime);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                );
            }
        } else {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
            );
        }
    }

    private long calculateNextTrigger(int dayOfWeek, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Calendar now = Calendar.getInstance();
        Calendar trigger = Calendar.getInstance();

        try {
            // Parse hour and minute
            trigger.setTime(sdf.parse(time));
            trigger.set(Calendar.SECOND, 0);
            trigger.set(Calendar.MILLISECOND, 0);
        } catch (ParseException e) {
            e.printStackTrace();
            return System.currentTimeMillis() + 5 * 60 * 1000; // fallback: trigger after 5 minutes
        }

        // Set day of week
        trigger.set(Calendar.DAY_OF_WEEK, dayOfWeek);

        // If the time has already passed for today, set it for next week
        if (trigger.before(now)) {
            trigger.add(Calendar.WEEK_OF_YEAR, 1);
        }

        return trigger.getTimeInMillis();
    }

    public void cancelAll() {
        // Optionally loop through all scheduled classes and cancel them using the same PendingIntent logic
    }
}
