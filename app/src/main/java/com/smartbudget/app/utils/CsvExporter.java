package com.smartbudget.app.utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CsvExporter {
    private static final String TAG = "CsvExporter";

    public static void exportDataToCsv(Context context, List<ExpenseEntity> expenses, Map<Long, CategoryEntity> categoryMap) {
        String fileName = "SmartBudget_Report_" + new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + ".csv";
        
        // Use public Documents directory or App specific directory
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists()) {
            dir = context.getExternalFilesDir(null); // Fallback
        }
        
        File file = new File(dir, fileName);

        try (FileWriter writer = new FileWriter(file)) {
            // Write Header
            writer.append("Date,Category,Type,Amount,Note\n");

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

            for (ExpenseEntity expense : expenses) {
                CategoryEntity cat = categoryMap.get(expense.getCategoryId());
                String categoryName = cat != null ? cat.getName() : "Unknown";
                String type = (cat != null && cat.getType() == 1) ? "Income" : "Expense";
                String date = dateFormat.format(new Date(expense.getDate()));
                
                writer.append(date).append(",");
                writer.append(escapeSpecialCharacters(categoryName)).append(",");
                writer.append(type).append(",");
                writer.append(String.valueOf(expense.getAmount())).append(",");
                writer.append(escapeSpecialCharacters(expense.getNote())).append("\n");
            }

            writer.flush();
            Toast.makeText(context, "Đã xuất file tại: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
            Log.i(TAG, "CSV created at: " + file.getAbsolutePath());

        } catch (IOException e) {
            Log.e(TAG, "Error exporting CSV", e);
            Toast.makeText(context, "Lỗi khi xuất file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static String escapeSpecialCharacters(String data) {
        if (data == null) return "";
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
