package com.project.scheduleasy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
    private static final String TAG = "BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || intent.getAction() == null) {
            Log.w(TAG, "Received null intent or action.");
            return;
        }

        String action = intent.getAction();
        Log.d(TAG, "Received broadcast: " + action);  // Removed `msg:`

        if (Intent.ACTION_BOOT_COMPLETED.equals(action) ||
                Intent.ACTION_TIMEZONE_CHANGED.equals(action) ||
                Intent.ACTION_TIME_CHANGED.equals(action)) {  // Added missing parenthesis

            // It's best to run tasks like scheduling in a background thread
            new Thread(() -> {
                try {
                    new ClassNotificationScheduler(context).scheduleAllClassNotifications();
                    Log.d(TAG, "All class notifications scheduled.");
                } catch (Exception e) {
                    Log.e(TAG, "Error scheduling class notifications", e);
                }
            }).start();
        }
    }
}
