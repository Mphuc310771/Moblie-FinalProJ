package com.smartbudget.app.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.entity.BudgetEntity;
import com.smartbudget.app.data.local.entity.CategoryEntity;
import com.smartbudget.app.data.local.entity.ExpenseEntity;
import com.smartbudget.app.data.local.entity.SavingsGoalEntity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Manager for local backup and restore operations.
 * Exports/imports all data as JSON files.
 */
public class BackupManager {
    
    private final Context context;
    private final AppDatabase database;
    private final Gson gson;
    
    public interface BackupCallback {
        void onSuccess(String message);
        void onError(String error);
    }
    
    public BackupManager(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
        this.gson = new GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    }
    
    /**
     * Backup all data to a JSON file.
     */
    public void backupData(BackupCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                BackupData data = new BackupData();
                
                // Collect all data
                data.expenses = database.expenseDao().getAllExpensesSync();
                data.categories = database.categoryDao().getAllCategoriesSync();
                data.budgets = database.budgetDao().getAllBudgetsSync();
                data.savingsGoals = database.savingsGoalDao().getActiveGoalsSync();
                data.savingsGoals.addAll(database.savingsGoalDao().getCompletedGoalsSync());
                data.backupTime = System.currentTimeMillis();
                data.version = 1;
                
                // Create backup file
                File backupDir = getBackupDirectory();
                if (!backupDir.exists()) {
                    backupDir.mkdirs();
                }
                
                String fileName = "smartbudget_backup_" + 
                    new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                        .format(new Date()) + ".json";
                File backupFile = new File(backupDir, fileName);
                
                // Write JSON
                try (FileWriter writer = new FileWriter(backupFile)) {
                    gson.toJson(data, writer);
                }
                
                String message = "Đã sao lưu tại: " + backupFile.getAbsolutePath();
                if (callback != null) {
                    callback.onSuccess(message);
                }
                
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Lỗi sao lưu: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Restore data from a JSON backup file.
     */
    public void restoreData(File backupFile, BackupCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                // Parse backup file
                BackupData data;
                try (FileReader reader = new FileReader(backupFile)) {
                    data = gson.fromJson(reader, BackupData.class);
                }
                
                if (data == null) {
                    if (callback != null) {
                        callback.onError("File backup không hợp lệ");
                    }
                    return;
                }
                
                // Restore categories first (for foreign key)
                if (data.categories != null) {
                    for (CategoryEntity cat : data.categories) {
                        // Insert only if not exists
                        if (database.categoryDao().getCategoryById(cat.getId()) == null) {
                            database.categoryDao().insert(cat);
                        }
                    }
                }
                
                // Restore expenses
                if (data.expenses != null) {
                    for (ExpenseEntity exp : data.expenses) {
                        exp.setId(0); // Let Room generate new ID
                        database.expenseDao().insert(exp);
                    }
                }
                
                // Restore budgets
                if (data.budgets != null) {
                    for (BudgetEntity budget : data.budgets) {
                        budget.setId(0);
                        database.budgetDao().insert(budget);
                    }
                }
                
                // Restore savings goals
                if (data.savingsGoals != null) {
                    for (SavingsGoalEntity goal : data.savingsGoals) {
                        goal.setId(0);
                        database.savingsGoalDao().insert(goal);
                    }
                }
                
                if (callback != null) {
                    callback.onSuccess("Đã khôi phục dữ liệu thành công!");
                }
                
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError("Lỗi khôi phục: " + e.getMessage());
                }
            }
        });
    }
    
    /**
     * Get list of available backup files.
     */
    public File[] getAvailableBackups() {
        File backupDir = getBackupDirectory();
        if (!backupDir.exists()) {
            return new File[0];
        }
        return backupDir.listFiles((dir, name) -> name.endsWith(".json"));
    }
    
    private File getBackupDirectory() {
        // Use Documents or app-specific directory
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        if (!dir.exists() || !dir.canWrite()) {
            dir = context.getExternalFilesDir(null);
        }
        return new File(dir, "SmartBudget_Backup");
    }
    
    // Data class for backup
    private static class BackupData {
        int version;
        long backupTime;
        List<ExpenseEntity> expenses;
        List<CategoryEntity> categories;
        List<BudgetEntity> budgets;
        List<SavingsGoalEntity> savingsGoals;
    }
    
    /**
     * Quick access backup method with Toast feedback.
     */
    public static void quickBackup(Context context) {
        new BackupManager(context).backupData(new BackupCallback() {
            @Override
            public void onSuccess(String message) {
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onError(String error) {
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> Toast.makeText(context, error, Toast.LENGTH_SHORT).show());
            }
        });
    }
}
