package com.project.scheduleasy;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfExportUtil {

    // Generates a PDF from the full content of a View, even if it's scrollable
    public static File generatePdfFromView(Context context, HorizontalScrollView horizontalScrollView, String fileName) {
        try {
            // 1. Get the root timetable layout
            ScrollView verticalScrollView = (ScrollView) horizontalScrollView.getChildAt(0);
            LinearLayout timetableRoot = (LinearLayout) verticalScrollView.getChildAt(0);

            // 2. Hide all delete buttons recursively
            hideDeleteButtons(timetableRoot);

            // 3. Measure full content
            timetableRoot.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
            );

            int width = timetableRoot.getMeasuredWidth();
            int height = timetableRoot.getMeasuredHeight();
            timetableRoot.layout(0, 0, width, height);

            // 4. Create bitmap
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            timetableRoot.draw(canvas);

            // 5. Create PDF
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(width, height, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            page.getCanvas().drawBitmap(bitmap, 0, 0, null);
            document.finishPage(page);

            // 6. Save to file
            File pdfFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName + ".pdf");
            try (FileOutputStream fos = new FileOutputStream(pdfFile)) {
                document.writeTo(fos);
                return pdfFile;
            } finally {
                document.close();
            }
        } catch (Exception e) {
            Log.e("PDF_EXPORT", "Error", e);
            return null;
        }
    }

    // Helper method to hide delete buttons
    private static void hideDeleteButtons(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof ViewGroup) {
                hideDeleteButtons((ViewGroup) child);
            } else if (child.getId() == R.id.deleteRow) { // CHANGE TO YOUR DELETE BUTTON ID
                child.setVisibility(View.GONE);
            }
        }
    }

    public static void sharePdfFile(Context context, File pdfFile, String title) {
        if (pdfFile == null || !pdfFile.exists()) {
            Toast.makeText(context, "PDF file not found to share.", Toast.LENGTH_SHORT).show();
            return;
        }

        Uri fileUri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                pdfFile
        );

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/pdf");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Verify there's an app to handle the intent
        if (shareIntent.resolveActivity(context.getPackageManager()) != null) {
            Intent chooser = Intent.createChooser(shareIntent, title);
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // <- important if context is not an Activity
            context.startActivity(chooser);
        } else {
            Toast.makeText(context, "No app found to share PDF.", Toast.LENGTH_SHORT).show();
        }

    }
}
