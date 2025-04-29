package com.project.scheduleasy;

import android.content.Intent;
import android.content.SharedPreferences;
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

            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            String userId = prefs.getString("currentUserId", null);

            Intent intent;

            if (userId != null){
                intent = new Intent(SplashscreenActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashscreenActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();
        }, 5000);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Cancel any pending tasks if needed
    }
}