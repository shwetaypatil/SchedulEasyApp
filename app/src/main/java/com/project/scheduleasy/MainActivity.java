package com.project.scheduleasy;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements TimetableAdapter.OnTimetableChangeListener {

    private Toolbar toolbar;
    private RecyclerView timetableList;
    private FloatingActionButton fabAddTimetable;
    private TextView emptyState;
    private TimetableAdapter adapter;
    private SharedPreferences sharedPreferences;
    private String userType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //get user type from login activity
        userType = getIntent().getStringExtra("USER_TYPE");
        if (userType == null) userType = "STUDENT"; //default to student id not specified

        // Initialize views
        toolbar = findViewById(R.id.toolbar);
        timetableList = findViewById(R.id.timetableList);
        fabAddTimetable = findViewById(R.id.fabAddTimetable);
        emptyState = findViewById(R.id.emptyState);

        // setup ui based on user type
        configureUserPermissions();

        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon(null);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
        }

        // Setup RecyclerView
        adapter = new TimetableAdapter(new ArrayList<>(), this, userType, this);
        timetableList.setLayoutManager(new LinearLayoutManager(this));
        timetableList.setAdapter(adapter);

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("TimetablePrefs", MODE_PRIVATE);

        // Load saved timetables
        loadTimetables();

        // Set click listeners
        fabAddTimetable.setOnClickListener(v -> createNewTimetable());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void configureUserPermissions(){
        if(userType.equals("FACULTY")){
            fabAddTimetable.setVisibility(View.VISIBLE);
            toolbar.setVisibility(View.VISIBLE);
        } else {
            fabAddTimetable.setVisibility(View.GONE);
        }
    }

    private void loadTimetables() {
        String json = sharedPreferences.getString("timetable_list", "[]");
        Type listType = new TypeToken<ArrayList<TimetableMeta>>(){}.getType();
        List<TimetableMeta> timetables = new Gson().fromJson(json, listType);

        adapter.updateTimetables(timetables);
        checkEmptyState();
    }

    private void createNewTimetable() {

        int nextTimetableNumber = getNextTimetableNumber();

        // Create new timetable with unique name and current timestamp
        TimetableMeta newTimetable = new TimetableMeta(
                UUID.randomUUID().toString(),
                "Timetable " + nextTimetableNumber,
                System.currentTimeMillis()
        );

        // Use the adapter's add method
        adapter.addTimetable(newTimetable);
        saveTimetableList();
        checkEmptyState();
        openTimetable(newTimetable);
    }

    private int getNextTimetableNumber(){
        String json = sharedPreferences.getString("timetable_list", "[]");
        Type listType = new TypeToken<ArrayList<TimetableMeta>>(){}.getType();
        List<TimetableMeta> existingTimetables = new Gson().fromJson(json, listType);

        // Find the highest existing number
        int maxNumber = 0;
        for (TimetableMeta timetable : existingTimetables) {
            try {
                String numberStr = timetable.getTitle().replace("Timetable ", "");
                int currentNumber = Integer.parseInt(numberStr);
                if (currentNumber > maxNumber) {
                    maxNumber = currentNumber;
                }
            } catch (NumberFormatException e) {
                // Skip if title format doesn't match
            }
        }

        return maxNumber + 1;
    }

    private void saveTimetableList() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("timetable_list", new Gson().toJson(adapter.getTimetables()));
        editor.apply();
    }

    @Override
    public void onTimetableDeleted(TimetableMeta timetable) {
        saveTimetableList();
        checkEmptyState();
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
        if (userType.equals("FACULTY")){
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }
        return false; //students get no menu
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (userType.equals("FACULTY")) {
            int id = item.getItemId();

            if (id == R.id.btnMenu){
                showPopup(findViewById(R.id.btnMenu));
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopup(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_main, popup.getMenu());
        popup.setOnMenuItemClickListener(clickedItem -> {
            // Handle popup menu items
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

                    // Return to Login
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTimetables(); // Refresh list when returning from SecondActivity
    }
}