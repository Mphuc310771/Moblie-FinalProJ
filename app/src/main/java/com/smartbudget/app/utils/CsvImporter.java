package com.smartbudget.app.utils;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;

import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Utility for importing expense data from CSV files.
 * Supports common bank export formats and custom CSV.
 * 
 * Expected CSV format:
 * Date,Category,Type,Amount,Note
 * 01/01/2025,Ăn uống,Expense,50000,Lunch
 */
public class CsvImporter {
    
    private final Context context;
    private final AppDatabase database;
    
    public interface ImportCallback {
        void onSuccess(int count);
        void onError(String error);
        void onProgress(int current, int total);
    }
    
    public CsvImporter(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
    }
    
    /**
     * Import expenses from a CSV file URI.
     */
    public void importFromUri(Uri uri, ImportCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                if (inputStream == null) {
                    if (callback != null) callback.onError("Không thể mở file");
                    return;
                }
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                List<ExpenseEntity> expenses = new ArrayList<>();
                
                String line;
                int lineNumber = 0;
                boolean hasHeader = true;
                
                while ((line = reader.readLine()) != null) {
                    lineNumber++;
                    
                    // Skip header
                    if (hasHeader && lineNumber == 1) {
                        if (line.toLowerCase().contains("date") || 
                            line.toLowerCase().contains("ngày")) {
                            continue;
                        }
                        hasHeader = false;
                    }
                    
                    ExpenseEntity expense = parseLine(line, lineNumber);
                    if (expense != null) {
                        expenses.add(expense);
                    }
                }
                
                reader.close();
                inputStream.close();
                
                // Insert into database
                int total = expenses.size();
                int current = 0;
                
                for (ExpenseEntity expense : expenses) {
                    database.expenseDao().insert(expense);
                    current++;
                    if (callback != null && current % 10 == 0) {
                        final int c = current;
                        callback.onProgress(c, total);
                    }
                }
                
                if (callback != null) {
                    callback.onSuccess(total);
                }
                
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Lỗi import: " + e.getMessage());
                }
            }
        });
    }
    
    private ExpenseEntity parseLine(String line, int lineNumber) {
        try {
            String[] parts = line.split(",");
            if (parts.length < 4) return null;
            
            ExpenseEntity expense = new ExpenseEntity();
            
            // Parse date (multiple formats)
            String dateStr = parts[0].trim();
            long date = parseDate(dateStr);
            expense.setDate(date);
            
            // Parse category name -> find or create category ID
            String categoryName = parts[1].trim();
            Long categoryId = findOrCreateCategory(categoryName, parts.length > 2 ? parts[2].trim() : "Expense");
            expense.setCategoryId(categoryId);
            
            // Parse amount
            String amountStr = parts[3].trim().replace(".", "").replace(",", "");
            double amount = Double.parseDouble(amountStr);
            expense.setAmount(amount);
            
            // Parse note (optional)
            if (parts.length > 4) {
                expense.setNote(parts[4].trim());
            }
            
            expense.setCreatedAt(System.currentTimeMillis());
            expense.setUpdatedAt(System.currentTimeMillis());
            
            return expense;
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private long parseDate(String dateStr) {
        String[] formats = {
            "dd/MM/yyyy",
            "yyyy-MM-dd",
            "MM/dd/yyyy",
            "dd-MM-yyyy",
            "yyyy/MM/dd"
        };
        
        for (String format : formats) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
                Date date = sdf.parse(dateStr);
                if (date != null) return date.getTime();
            } catch (ParseException ignored) {}
        }
        
        return System.currentTimeMillis();
    }
    
    private Long findOrCreateCategory(String name, String type) {
        // Check existing
        List<CategoryEntity> categories = database.categoryDao().getAllCategoriesSync();
        for (CategoryEntity cat : categories) {
            if (cat.getName().equalsIgnoreCase(name)) {
                return cat.getId();
            }
        }
        
        // Create new category
        int categoryType = type.toLowerCase().contains("income") ? 1 : 0;
        CategoryEntity newCat = new CategoryEntity(name, "📦", "#95979A", categoryType, true);
        return database.categoryDao().insert(newCat);
    }
    
    /**
     * Quick import with Toast feedback.
     */
    public static void quickImport(Context context, Uri uri) {
        new CsvImporter(context).importFromUri(uri, new ImportCallback() {
            @Override
            public void onSuccess(int count) {
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> 
                    Toast.makeText(context, "Đã nhập " + count + " giao dịch!", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onError(String error) {
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> 
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onProgress(int current, int total) {
                // Optional: Update progress UI
            }
        });
    }
}
