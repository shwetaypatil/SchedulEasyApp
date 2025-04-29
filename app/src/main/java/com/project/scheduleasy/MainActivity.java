package com.project.scheduleasy;

import android.Manifest;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements TimetableAdapter.OnTimetableChangeListener {

    private MaterialToolbar toolbar;
    private RecyclerView timetableList;
    private FloatingActionButton fabAddTimetable;
    private TextView emptyState;
    private TimetableAdapter adapter;
    private SharedPreferences sharedPreferences;
    private String userType;
    private AuthManager authManager;
    private AppDatabase db;

    private static final int NOTIFICATION_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //android 13+ notification permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE // Request code
                );
            }
        }

        // Get user type from LoginActivity
        userType = getIntent().getStringExtra("USER_TYPE");
        if (userType == null) userType = "STUDENT";

        Log.d("USER_TYPE_DEBUG", "UserType received: " + userType);

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        timetableList = findViewById(R.id.timetableList);
        fabAddTimetable = findViewById(R.id.fabAddTimetable);
        emptyState = findViewById(R.id.emptyState);

        toolbar.setOverflowIcon(null);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        configureUserPermissions();

        // Setup RecyclerView
        adapter = new TimetableAdapter(new ArrayList<>(), this, userType, this);
        timetableList.setLayoutManager(new LinearLayoutManager(this));
        timetableList.setAdapter(adapter);

        db = AppDatabase.getInstance(this);
        loadTimetables();

        fabAddTimetable.setOnClickListener(v -> createNewTimetable());

        authManager =AuthManager.getInstance(this);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configureUserPermissions() {
        if (userType != null && userType.equalsIgnoreCase("FACULTY")) {
            fabAddTimetable.setVisibility(View.VISIBLE);
        } else {
            fabAddTimetable.setVisibility(View.GONE);
        }
    }

    private void loadTimetables() {
        List<TimetableMeta> timetables = db.timetableDao().getAllTimetables();
        adapter.updateTimetables(timetables);
        checkEmptyState();
    }

    private void createNewTimetable() {
        int newId = getNextTimetableNumber();
        String timetableId = String.valueOf(newId);

        TimetableMeta newTimetable = new TimetableMeta(
                timetableId,
                "Timetable " + newId,
                System.currentTimeMillis()
        );

        db.timetableDao().insert(newTimetable);
        adapter.addTimetable(newTimetable);
        checkEmptyState();
        openTimetable(newTimetable);
    }

    private int getNextTimetableNumber() {
        List<TimetableMeta> existingTimetables = db.timetableDao().getAllTimetables();
        int maxNumber = 0;
        for (TimetableMeta timetable : existingTimetables) {
            try {
                String numberStr = timetable.getTitle().replace("Timetable ", "");
                int currentNumber = Integer.parseInt(numberStr);
                if (currentNumber > maxNumber) {
                    maxNumber = currentNumber;
                }
            } catch (NumberFormatException e) {
                // Ignore malformed titles
            }
        }
        return maxNumber + 1;
    }

    @Override
    public void onTimetableDeleted(TimetableMeta timetableMeta) {
        db.timetableDao().delete(timetableMeta);
       loadTimetables();
        Toast.makeText(this, "Timetable deleted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTimetableClick(TimetableMeta timetable) {
        openTimetable(timetable);
    }

    private void openTimetable(TimetableMeta timetable) {
        Intent intent = new Intent(this, SecondActivity.class);
        intent.putExtra("timetable_id", timetable.getId());
        startActivity(intent);
    }

    private void checkEmptyState() {
        if (adapter.getItemCount() == 0) {
            emptyState.setVisibility(View.VISIBLE);
            timetableList.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            timetableList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (userType != null && userType.equalsIgnoreCase("FACULTY")) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.btnMenu) {
            showPopup(findViewById(R.id.btnMenu));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(clickedItem -> {
            if (clickedItem.getItemId() == R.id.action_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
            } else if (clickedItem.getItemId() == R.id.action_logout) {
                showLogoutConfirmation();
            }
            return true;
        });
        popup.show();
    }

    private void showLogoutConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> {

                    authManager.logout();

                    // Optional: Clear saved login data
                    SharedPreferences preferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
                    preferences.edit().clear().apply();

                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.putExtra("LOGOUT", true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTimetables();
    }
}
