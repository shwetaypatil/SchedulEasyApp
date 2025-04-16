package com.project.scheduleasy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

public class SplashscreenActivity extends AppCompatActivity {

    private ImageView appIcon;
    private TextView appName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splashscreen);

        appIcon = findViewById(R.id.appIcon);
        appName = findViewById(R.id.appName);

        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        appIcon.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashscreenActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }, 5000);

        // 1. Initialize notification scheduler
        ClassNotificationScheduler scheduler = new ClassNotificationScheduler(this);

        // 2. Check and request permissions (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        // 3. Schedule notifications in background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            scheduler.scheduleAllClassNotifications();

            // Proceed to main activity
            runOnUiThread(() -> {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending tasks if needed
    }
}