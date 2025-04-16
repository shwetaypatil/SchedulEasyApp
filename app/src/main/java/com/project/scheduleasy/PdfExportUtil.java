package com.project.scheduleasy;

import android.content.Context;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PdfExportUtil {

    public static File generatePdfFromView(Context context, View view, String fileName) {
        // Create document
        PdfDocument document = new PdfDocument();

        // Create page
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                view.getWidth(),
                view.getHeight(),
                1).create();

        PdfDocument.Page page = document.startPage(pageInfo);

        // Draw view to PDF
        view.draw(page.getCanvas());
        document.finishPage(page);

        // Save to file
        File pdfFile = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                fileName + ".pdf"
        );

        try {
            document.writeTo(new FileOutputStream(pdfFile));
            return pdfFile;
        } catch (IOException e) {
            Log.e("PDF Export", "Error writing PDF", e);
            return null;
        } finally {
            document.close();
        }
    }
}
