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
            if (parts.length < 4) {
                android.util.Log.w("CsvImporter", "Line " + lineNumber + ": Not enough columns (need 4, got " + parts.length + "): " + line);
                return null;
            }
            
            ExpenseEntity expense = new ExpenseEntity();
            
            // Parse date (multiple formats)
            String dateStr = parts[0].trim();
            long date = parseDate(dateStr);
            expense.setDate(date);
            
            // Parse category name -> find or create category ID
            String categoryName = parts[1].trim();
            String typeHint = parts.length > 2 ? parts[2].trim() : "Expense";
            Long categoryId = findOrCreateCategory(categoryName, typeHint);
            expense.setCategoryId(categoryId);
            
            // Parse amount - handle various number formats
            String amountStr = parts[3].trim()
                .replace(".", "")
                .replace(",", "")
                .replace(" ", "")
                .replace("₫", "")
                .replace("VND", "")
                .replace("đ", "");
            double amount = Double.parseDouble(amountStr);
            expense.setAmount(amount);
            
            // Parse note (optional)
            if (parts.length > 4) {
                expense.setNote(parts[4].trim());
            }
            
            expense.setCreatedAt(System.currentTimeMillis());
            expense.setUpdatedAt(System.currentTimeMillis());
            
            android.util.Log.d("CsvImporter", "Parsed line " + lineNumber + ": " + 
                "date=" + dateStr + ", category=" + categoryName + " (ID:" + categoryId + "), amount=" + amount);
            
            return expense;
            
        } catch (Exception e) {
            android.util.Log.e("CsvImporter", "Error parsing line " + lineNumber + ": " + line, e);
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
        // Check existing - try exact match first
        List<CategoryEntity> categories = database.categoryDao().getAllCategoriesSync();
        
        // Normalize input name
        String normalizedName = name.toLowerCase().trim();
        
        for (CategoryEntity cat : categories) {
            String catName = cat.getName().toLowerCase().trim();
            
            // Exact match
            if (catName.equals(normalizedName)) {
                android.util.Log.d("CsvImporter", "Found exact match: " + cat.getName() + " (ID: " + cat.getId() + ")");
                return cat.getId();
            }
            
            // Partial match (input contains category name or vice versa)
            if (catName.contains(normalizedName) || normalizedName.contains(catName)) {
                android.util.Log.d("CsvImporter", "Found partial match: " + cat.getName() + " for input: " + name);
                return cat.getId();
            }
        }
        
        // Try matching common Vietnamese category keywords
        Long matchedId = matchVietnameseCategory(normalizedName, categories);
        if (matchedId != null) {
            return matchedId;
        }
        
        // Create new category if not found
        int categoryType = type.toLowerCase().contains("income") ? 1 : 0;
        CategoryEntity newCat = new CategoryEntity(name, "📦", "#95979A", categoryType, true);
        long newId = database.categoryDao().insert(newCat);
        android.util.Log.d("CsvImporter", "Created new category: " + name + " (ID: " + newId + ", type: " + categoryType + ")");
        return newId;
    }
    
    private Long matchVietnameseCategory(String input, List<CategoryEntity> categories) {
        // Common Vietnamese category keywords mapping
        String[][] keywords = {
            {"ăn", "an uong", "food", "meal", "lunch", "dinner", "breakfast"}, // Food
            {"di chuyển", "di chuyen", "transport", "grab", "taxi", "xe"}, // Transport
            {"mua sắm", "mua sam", "shopping", "buy"}, // Shopping
            {"giải trí", "giai tri", "entertainment", "movie", "game"}, // Entertainment
            {"sức khỏe", "suc khoe", "health", "doctor", "medicine"}, // Health
            {"giáo dục", "giao duc", "education", "học", "hoc", "book"}, // Education
            {"tiền nhà", "tien nha", "rent", "house"}, // Housing
            {"điện nước", "dien nuoc", "bill", "utility"}, // Bills
        };
        
        for (int i = 0; i < keywords.length; i++) {
            for (String keyword : keywords[i]) {
                if (input.contains(keyword)) {
                    // Find matching category by common names
                    for (CategoryEntity cat : categories) {
                        String catName = cat.getName().toLowerCase();
                        for (String kw : keywords[i]) {
                            if (catName.contains(kw)) {
                                android.util.Log.d("CsvImporter", "Matched by keyword: " + input + " -> " + cat.getName());
                                return cat.getId();
                            }
                        }
                    }
                }
            }
        }
        return null;
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
