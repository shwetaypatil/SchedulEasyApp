package com.project.scheduleasy;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import androidx.core.content.FileProvider;
import android.net.Uri;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;

public class SecondActivity extends AppCompatActivity {

    private LinearLayout contentLayout;
    private MaterialButton addRowBtn, saveBtn;
    private HorizontalScrollView scrollView;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "TimetablePrefs";
    private static final String TIMETABLE_DATA = "timetable_data";
    private EditText currentEditingCell;
    private Button btnSharePdf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_second);

        contentLayout = findViewById(R.id.contentLayout);
        scrollView = findViewById(R.id.scrollView);
        addRowBtn = findViewById(R.id.addRow);
        saveBtn = findViewById(R.id.saveButton);
        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        btnSharePdf = findViewById(R.id.btnSharePdf);

        addRowBtn.setOnClickListener(v -> addNewRow());
        saveBtn.setOnClickListener(v -> saveTimetable());
        btnSharePdf.setOnClickListener(v -> shareTimetableAsPdf());

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
        deleteBtn.setOnClickListener(v -> contentLayout.removeView(rowView));

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
        try {
            List<TimeTableEntry> entries = new ArrayList<>();

            for (int i = 0; i < contentLayout.getChildCount(); i++) {
                View rowView = contentLayout.getChildAt(i);
                entries.add(new TimeTableEntry(
                        getTextFromView(rowView, R.id.timeInput),
                        getTextFromView(rowView, R.id.monInput),
                        getTextFromView(rowView, R.id.tueInput),
                        getTextFromView(rowView, R.id.wedInput),
                        getTextFromView(rowView, R.id.thuInput),
                        getTextFromView(rowView, R.id.friInput),
                        getTextFromView(rowView, R.id.satInput)
                ));
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            Gson gson = new Gson();
            String json = gson.toJson(entries);
            editor.putString(TIMETABLE_DATA, json);
            editor.apply();

            Toast.makeText(this, "Timetable saved!", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String getTextFromView(View parentView, int viewId) {
        return ((EditText) parentView.findViewById(viewId)).getText().toString();
    }

    private void loadSavedData() {
        String json = sharedPreferences.getString(TIMETABLE_DATA, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<TimeTableEntry>>() {}.getType();
            List<TimeTableEntry> entries = gson.fromJson(json, type);

            if (entries != null) {
                contentLayout.removeAllViews();

                for (TimeTableEntry entry : entries) {
                    View rowView = LayoutInflater.from(this).inflate(R.layout.table_row, contentLayout, false);

                    ((EditText) rowView.findViewById(R.id.timeInput)).setText(entry.time);
                    ((EditText) rowView.findViewById(R.id.monInput)).setText(entry.monday);
                    ((EditText) rowView.findViewById(R.id.tueInput)).setText(entry.tuesday);
                    ((EditText) rowView.findViewById(R.id.wedInput)).setText(entry.wednesday);
                    ((EditText) rowView.findViewById(R.id.thuInput)).setText(entry.thursday);
                    ((EditText) rowView.findViewById(R.id.friInput)).setText(entry.friday);
                    ((EditText) rowView.findViewById(R.id.satInput)).setText(entry.saturday);

                    setupEditableCells(rowView);
                    ImageButton deleteBtn = rowView.findViewById(R.id.deleteRow);
                    deleteBtn.setOnClickListener(v -> contentLayout.removeView(rowView));

                    contentLayout.addView(rowView);
                }
            }
        }
    }


    private void shareTimetableAsPdf() {
        final String timetableName = ((EditText) findViewById(R.id.titleText)).getText().toString();
        ProgressDialog progress = new ProgressDialog(this);
        progress.setMessage("Generating PDF...");
        progress.setCancelable(false);
        progress.show();

        new Thread(() -> {
            PdfDocument document = new PdfDocument();
            try {
                String finalName = timetableName.isEmpty() ? "MyTimetable" : timetableName;

                // 1. Set up PDF page (A4 size at 72dpi)
                int pageWidth = 595;   // 8.27 inches * 72 dpi
                int pageHeight = 842;  // 11.69 inches * 72 dpi
                int margin = 36;       // 0.5 inch margins

                // 2. Create page
                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                        pageWidth, pageHeight, 1).create();
                PdfDocument.Page page = document.startPage(pageInfo);
                Canvas canvas = page.getCanvas();

                // 3. Set up drawing tools
                Paint paint = new Paint();
                paint.setColor(Color.BLACK);
                paint.setTextSize(12);
                paint.setAntiAlias(true);

                // 4. Draw title
                paint.setTextSize(16);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                float titleX = pageWidth / 2f;
                float titleY = margin + 30;
                canvas.drawText(timetableName, titleX, titleY, paint);
                paint.setTextSize(12);

                // 5. Draw table headers
                String[] headers = {"TIME", "MON", "TUE", "WED", "THU", "FRI", "SAT"};
                float colWidth = (pageWidth - 2 * margin) / headers.length;
                float rowHeight = 20;
                float tableTop = titleY + 30;

                // Header background
                paint.setColor(Color.rgb(63, 81, 181)); // Material Indigo
                canvas.drawRect(margin, tableTop, pageWidth - margin, tableTop + rowHeight, paint);

                // Header text
                paint.setColor(Color.WHITE);
                paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                float textY = tableTop + 15;
                for (int i = 0; i < headers.length; i++) {
                    float textX = margin + (i * colWidth) + (colWidth / 2) - (paint.measureText(headers[i]) / 2);
                    canvas.drawText(headers[i], textX, textY, paint);
                }

                // 6. Draw timetable content
                paint.setColor(Color.BLACK);
                paint.setTypeface(Typeface.DEFAULT);
                float currentY = tableTop + rowHeight + 5;

                for (int rowPos = 0; rowPos < contentLayout.getChildCount(); rowPos++) {
                    View row = contentLayout.getChildAt(rowPos);

                    // Alternate row colors
                    if (rowPos % 2 == 0) {
                        paint.setColor(Color.rgb(224, 224, 224)); // Light gray
                        canvas.drawRect(margin, currentY - 15, pageWidth - margin, currentY + 5, paint);
                        paint.setColor(Color.BLACK);
                    }

                    // Get all cell values
                    String time = ((EditText)row.findViewById(R.id.timeInput)).getText().toString();
                    String[] dayValues = {
                            ((EditText)row.findViewById(R.id.monInput)).getText().toString(),
                            ((EditText)row.findViewById(R.id.tueInput)).getText().toString(),
                            ((EditText)row.findViewById(R.id.wedInput)).getText().toString(),
                            ((EditText)row.findViewById(R.id.thuInput)).getText().toString(),
                            ((EditText)row.findViewById(R.id.friInput)).getText().toString(),
                            ((EditText)row.findViewById(R.id.satInput)).getText().toString()
                    };

                    // Draw cells
                    for (int col = 0; col < headers.length; col++) {
                        String cellText = col == 0 ? time : dayValues[col-1];
                        float textX = margin + (col * colWidth) + 5; // Left padding
                        canvas.drawText(cellText, textX, currentY, paint);
                    }

                    currentY += rowHeight;

                    // Page break if needed
                    if (currentY > pageHeight - margin) {
                        document.finishPage(page);
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        currentY = margin + 30;

                        // Redraw headers on new page
                        paint.setColor(Color.rgb(63, 81, 181));
                        canvas.drawRect(margin, currentY, pageWidth - margin, currentY + rowHeight, paint);
                        paint.setColor(Color.WHITE);
                        for (int i = 0; i < headers.length; i++) {
                            float textX = margin + (i * colWidth) + (colWidth / 2) - (paint.measureText(headers[i]) / 2);
                            canvas.drawText(headers[i], textX, currentY + 15, paint);
                        }
                        currentY += rowHeight + 5;
                        paint.setColor(Color.BLACK);
                    }
                }

                document.finishPage(page);

                // Save PDF
                File pdfFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                        timetableName + ".pdf");
                try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                    document.writeTo(fos);
                    runOnUiThread(() -> {
                        progress.dismiss();
                        sharePdfFile(pdfFile);
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
                Log.e("PDF_ERROR", "Generation failed", e);
            } finally {
                document.close();
            }
        }).start();
    }


    private void sharePdfFile(File pdfFile) {
        try {
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", pdfFile);

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(shareIntent, "Share Timetable"));
        } catch (Exception e) {
            Toast.makeText(this, "Error sharing file", Toast.LENGTH_SHORT).show();
        }
    }

    /*private LinearLayout createHeaderRow() {
        LinearLayout headerRow = new LinearLayout(this);
        headerRow.setOrientation(LinearLayout.HORIZONTAL);
        headerRow.setBackgroundColor(Color.LTGRAY);
        headerRow.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        String[] headers = {"Time", "MON", "TUE", "WED", "THU", "FRI", "SAT"};

        for (String title : headers) {
            TextView textView = new TextView(this);
            textView.setText(title); // ✅ this must be set before measuring
            textView.setTextColor(Color.BLACK); // make sure it's not transparent
            textView.setTextSize(14f);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setGravity(Gravity.CENTER);
            textView.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f // Equal weight for all columns
            );
            textView.setLayoutParams(params);

            headerRow.addView(textView);
        }

        return headerRow;
    }*/



    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    // Data model class
    private static class TimeTableEntry {
        String time, monday, tuesday, wednesday, thursday, friday, saturday;

        public TimeTableEntry(String time, String monday, String tuesday, String wednesday,
                              String thursday, String friday, String saturday) {
            this.time = time;
            this.monday = monday;
            this.tuesday = tuesday;
            this.wednesday = wednesday;
            this.thursday = thursday;
            this.friday = friday;
            this.saturday = saturday;
        }
    }
}
