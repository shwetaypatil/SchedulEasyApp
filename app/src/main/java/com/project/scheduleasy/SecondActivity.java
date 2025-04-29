package com.project.scheduleasy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SecondActivity extends AppCompatActivity {

    private LinearLayout contentLayout;
    private MaterialButton addRowBtn, saveBtn;
    private HorizontalScrollView scrollView;
    private EditText currentEditingCell;
    private EditText titleText;
    private Button btnSharePdf;
    private String timetableId;
    private AppDatabase appDatabase;
    private TimetableDao timetableDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        // In SecondActivity's onCreate():
        timetableId = getIntent().getStringExtra("timetable_id");
        if (timetableId == null) {
            // Generate new ID if creating new timetable
            timetableId = UUID.randomUUID().toString();
        }
        appDatabase = AppDatabase.getInstance(this);
        timetableDao = appDatabase.timetableDao();

        titleText = findViewById(R.id.titleText);
        contentLayout = findViewById(R.id.contentLayout);
        scrollView = findViewById(R.id.scrollView);
        addRowBtn = findViewById(R.id.addRow);
        saveBtn = findViewById(R.id.saveButton);
        btnSharePdf = findViewById(R.id.btnSharePdf);

        addRowBtn.setOnClickListener(v -> addNewRow());
        saveBtn.setOnClickListener(v -> saveTimetable());
        btnSharePdf.setOnClickListener(v -> shareTable());

        loadSavedData();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void addNewRow() {
        View rowView = LayoutInflater.from(this).inflate(R.layout.table_row, contentLayout, false);
        EditText timeInput = rowView.findViewById(R.id.timeInput);
        timeInput.setText(calculateNextTimeSlot());
        setupEditableCells(rowView);

        ImageButton deleteBtn = rowView.findViewById(R.id.deleteRow);
        deleteBtn.setOnClickListener(v -> {
            // Remove from database first
            String time = timeInput.getText().toString();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                appDatabase.classDao().deleteByTimeAndTimetableId(time, timetableId);
            });
            // Then remove from UI
            contentLayout.removeView(rowView);
        });

        contentLayout.addView(rowView);
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
    }

    private void setupEditableCells(View rowView) {
        int[] cellIds = {
                R.id.timeInput,
                R.id.monInput, R.id.tueInput,
                R.id.wedInput, R.id.thuInput,
                R.id.friInput, R.id.satInput
        };

        for (int id : cellIds) {
            EditText cell = rowView.findViewById(id);
            cell.setOnClickListener(v -> showEditPopup(cell));
            cell.setFocusable(false);
            cell.setCursorVisible(false);
            cell.setBackgroundResource(android.R.color.transparent);
        }
    }

    private void showEditPopup(EditText cell) {
        currentEditingCell = cell;

        View popupView = LayoutInflater.from(this).inflate(R.layout.pop_cell, null);
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_background));
        popupWindow.setElevation(20f);

        EditText popupEditText = popupView.findViewById(R.id.popupEditText);
        Button saveButton = popupView.findViewById(R.id.saveButton);
        Button cancelButton = popupView.findViewById(R.id.cancelButton);

        popupEditText.setText(cell.getText().toString());
        popupEditText.requestFocus();

        saveButton.setOnClickListener(v -> {
            cell.setText(popupEditText.getText().toString());
            popupWindow.dismiss();
        });

        cancelButton.setOnClickListener(v -> popupWindow.dismiss());

        popupWindow.showAtLocation(findViewById(R.id.main), Gravity.CENTER, 0, 0);
    }

    private String calculateNextTimeSlot() {
        int childCount = contentLayout.getChildCount();
        if (childCount == 0) return "08:00";

        View lastRow = contentLayout.getChildAt(childCount - 1);
        EditText lastTimeInput = lastRow.findViewById(R.id.timeInput);
        String lastTime = lastTimeInput.getText().toString();

        try {
            String[] parts = lastTime.split(":");
            int hour = Integer.parseInt(parts[0]) + 1;
            if (hour > 23) hour = 8;
            return String.format("%02d:00", hour);
        } catch (Exception e) {
            return "08:00";
        }
    }

    private void saveTimetable() {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        executor.execute(() -> {
            // First delete all existing schedules for this timetable
            String time = null;
            appDatabase.classDao().deleteByTimeAndTimetableId(time, timetableId);

            // Save new schedules
            for (int i = 0; i < contentLayout.getChildCount(); i++) {
                View rowView = contentLayout.getChildAt(i);
                time = getTextFromView(rowView, R.id.timeInput);

                // Save for each day
                saveDaySchedule(rowView, time, R.id.monInput, 1); // Monday
                saveDaySchedule(rowView, time, R.id.tueInput, 2); // Tuesday
                saveDaySchedule(rowView, time, R.id.wedInput, 3);
                saveDaySchedule(rowView, time, R.id.thuInput, 4);
                saveDaySchedule(rowView, time, R.id.friInput, 5);
                saveDaySchedule(rowView, time, R.id.satInput, 6);
            }
        });
        Toast.makeText(this, "Timetable saved", Toast.LENGTH_SHORT).show();
    }

    private void saveDaySchedule(View rowView, String time, int dayViewId, int dayOfWeek) {
        String className = getTextFromView(rowView, dayViewId);
        if (!className.isEmpty()) {
            ClassSchedule schedule = new ClassSchedule(
                    UUID.randomUUID().toString(), // Generate unique ID
                    timetableId,
                    className,
                    dayOfWeek,
                    time,
                    true
            );
            appDatabase.classDao().insertAll(schedule);
        }
    }

    private String getTextFromView(View parentView, int viewId) {
        return ((EditText) parentView.findViewById(viewId)).getText().toString();
    }

    private void loadSavedData() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            List<ClassSchedule> schedules = appDatabase.classDao().getClassScheduleForTimetable(timetableId);

            handler.post(() -> {
                contentLayout.removeAllViews();

                if (schedules == null || schedules.isEmpty()) {
                    addNewRow(); // Start with one empty row
                    return;
                }

                // Group by time
                Map<String, List<ClassSchedule>> timeMap = new TreeMap<>();
                for (ClassSchedule schedule : schedules) {
                    if (!timeMap.containsKey(schedule.startTime)) {
                        timeMap.put(schedule.startTime, new ArrayList<>());
                    }
                    timeMap.get(schedule.startTime).add(schedule);
                }

                // Create rows
                for (Map.Entry<String, List<ClassSchedule>> entry : timeMap.entrySet()) {
                    View rowView = LayoutInflater.from(this)
                            .inflate(R.layout.table_row, contentLayout, false);

                    // Set time
                    EditText timeInput = rowView.findViewById(R.id.timeInput);
                    timeInput.setText(entry.getKey());

                    // Set day values
                    for (ClassSchedule schedule : entry.getValue()) {
                        switch (schedule.dayOfWeek) {
                            case 1: setTextSafe(rowView, R.id.monInput, schedule.className); break;
                            case 2: setTextSafe(rowView, R.id.tueInput, schedule.className); break;
                            case 3: setTextSafe(rowView, R.id.wedInput, schedule.className); break;
                            case 4: setTextSafe(rowView, R.id.thuInput, schedule.className); break;
                            case 5: setTextSafe(rowView, R.id.friInput, schedule.className); break;
                            case 6: setTextSafe(rowView, R.id.satInput, schedule.className); break;
                        }
                    }

                    setupEditableCells(rowView);

                    // Add delete functionality
                    ImageButton deleteBtn = rowView.findViewById(R.id.deleteRow);
                    deleteBtn.setOnClickListener(v -> {
                        // Delete from database
                        executor.execute(() -> {
                            appDatabase.classDao().deleteByTimeAndTimetableId(
                                    entry.getKey(),
                                    timetableId
                            );
                        });
                        // Remove from UI
                        contentLayout.removeView(rowView);
                    });

                    contentLayout.addView(rowView);
                }
            });
        });
    }

    // Helper method to clear day fields
    private void clearDayFields(View rowView) {
        int[] dayFields = {R.id.monInput, R.id.tueInput, R.id.wedInput,
                R.id.thuInput, R.id.friInput, R.id.satInput};
        for (int fieldId : dayFields) {
            ((EditText)rowView.findViewById(fieldId)).setText("");
        }
    }

    // Helper method to safely set text in EditText
    private void setTextSafe(View parentView, int viewId, String text) {
        EditText editText = parentView.findViewById(viewId);
        if (editText != null && text != null) {
            editText.setText(text);
        }
    }

    private void shareTable() {
        if (scrollView == null) {
            Toast.makeText(this, "Timetable not found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check storage permission for Android < 11
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
        }

        // Wait for ScrollView to render fully
        scrollView.post(() -> {
            // Check storage permission for Android < 11 (API 30+ uses scoped storage)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R &&
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }

            new Thread(() -> {
                try {
                    File pdfFile = PdfExportUtil.generatePdfFromView(
                            SecondActivity.this,
                            scrollView,
                            "TimeTable_" + System.currentTimeMillis()
                    );

                    runOnUiThread(() -> {
                        if (pdfFile != null && pdfFile.exists()) {
                            PdfExportUtil.sharePdfFile(
                                    SecondActivity.this,
                                    pdfFile,
                                    "Share Timetable PDF"
                            );
                        } else {
                            Toast.makeText(
                                    SecondActivity.this,
                                    "Failed to generate PDF",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(
                                SecondActivity.this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG
                        ).show();
                        Log.e("PDF_Export", "Thread crashed", e);
                    });
                }
            }).start();
        });
    }
}
