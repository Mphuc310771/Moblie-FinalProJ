package com.smartbudget.app.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * PDF Export helper.
 * Creates beautiful PDF reports of expenses.
 */
public class PdfExportHelper {

    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 40;

    public static class ExpenseItem {
        public String category;
        public String note;
        public double amount;
        public long date;

        public ExpenseItem(String category, String note, double amount, long date) {
            this.category = category;
            this.note = note;
            this.amount = amount;
            this.date = date;
        }
    }

    /**
     * Export expenses to PDF file.
     */
    public static File exportToPdf(Context context, List<ExpenseItem> expenses, 
                                   String title, double totalIncome, double totalExpense) {
        PdfDocument document = new PdfDocument();
        
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        Paint titlePaint = new Paint();
        titlePaint.setColor(Color.parseColor("#1976D2"));
        titlePaint.setTextSize(24);
        titlePaint.setFakeBoldText(true);

        Paint headerPaint = new Paint();
        headerPaint.setColor(Color.parseColor("#333333"));
        headerPaint.setTextSize(14);
        headerPaint.setFakeBoldText(true);

        Paint textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#666666"));
        textPaint.setTextSize(11);

        Paint expensePaint = new Paint();
        expensePaint.setColor(Color.parseColor("#F44336"));
        expensePaint.setTextSize(11);

        Paint incomePaint = new Paint();
        incomePaint.setColor(Color.parseColor("#4CAF50"));
        incomePaint.setTextSize(11);

        Paint linePaint = new Paint();
        linePaint.setColor(Color.parseColor("#E0E0E0"));
        linePaint.setStrokeWidth(1);

        DecimalFormat formatter = new DecimalFormat("#,###");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", new Locale("vi", "VN"));

        int y = MARGIN + 30;

        // Logo/Title
        canvas.drawText("📊 SmartBudget Report", MARGIN, y, titlePaint);
        y += 20;

        // Subtitle
        textPaint.setTextSize(10);
        canvas.drawText("Báo cáo chi tiêu - " + title, MARGIN, y, textPaint);
        y += 10;
        canvas.drawText("Ngày xuất: " + dateFormat.format(new Date()), MARGIN, y, textPaint);
        y += 30;

        // Summary Box
        canvas.drawRect(MARGIN, y, PAGE_WIDTH - MARGIN, y + 60, linePaint);
        y += 20;
        canvas.drawText("💰 Thu nhập: " + formatter.format(totalIncome) + " ₫", MARGIN + 10, y, incomePaint);
        y += 18;
        canvas.drawText("💸 Chi tiêu: " + formatter.format(totalExpense) + " ₫", MARGIN + 10, y, expensePaint);
        y += 18;
        double balance = totalIncome - totalExpense;
        Paint balancePaint = balance >= 0 ? incomePaint : expensePaint;
        canvas.drawText("📈 Số dư: " + formatter.format(balance) + " ₫", MARGIN + 10, y, balancePaint);
        y += 30;

        // Table Header
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
        y += 15;
        canvas.drawText("Ngày", MARGIN, y, headerPaint);
        canvas.drawText("Danh mục", MARGIN + 80, y, headerPaint);
        canvas.drawText("Ghi chú", MARGIN + 180, y, headerPaint);
        canvas.drawText("Số tiền", PAGE_WIDTH - MARGIN - 80, y, headerPaint);
        y += 10;
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint);
        y += 15;

        // Expense Items
        textPaint.setTextSize(10);
        for (ExpenseItem expense : expenses) {
            if (y > PAGE_HEIGHT - 60) break; // Stop if near bottom

            canvas.drawText(dateFormat.format(new Date(expense.date)), MARGIN, y, textPaint);
            canvas.drawText(truncate(expense.category, 12), MARGIN + 80, y, textPaint);
            canvas.drawText(truncate(expense.note, 20), MARGIN + 180, y, textPaint);
            canvas.drawText("-" + formatter.format(expense.amount) + " ₫", PAGE_WIDTH - MARGIN - 80, y, expensePaint);
            
            y += 18;
        }

        // Footer
        y = PAGE_HEIGHT - MARGIN;
        textPaint.setTextSize(8);
        textPaint.setColor(Color.parseColor("#999999"));
        canvas.drawText("Được tạo bởi SmartBudget - Quản lý tài chính thông minh", MARGIN, y, textPaint);

        document.finishPage(page);

        // Save file
        File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "SmartBudget_Report_" + System.currentTimeMillis() + ".pdf");

        try {
            document.writeTo(new FileOutputStream(file));
            document.close();
            return file;
        } catch (IOException e) {
            document.close();
            return null;
        }
    }

    private static String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 2) + "..";
    }
}
