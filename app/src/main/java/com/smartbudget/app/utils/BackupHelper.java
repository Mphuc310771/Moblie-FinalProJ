package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Data backup and restore helper.
 * Provides backup/restore functionality for app data.
 */
public class BackupHelper {

    private static final String PREFS_NAME = "backup_prefs";
    private static final String KEY_LAST_BACKUP = "last_backup";

    public interface BackupCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    /**
     * Create a backup of app settings.
     */
    public static File createBackup(Context context, Map<String, Object> data, BackupCallback callback) {
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String filename = "smartbudget_backup_" + timestamp + ".json";
            
            File backupDir = new File(context.getExternalFilesDir(null), "backups");
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            File backupFile = new File(backupDir, filename);
            
            Gson gson = new Gson();
            String json = gson.toJson(data);
            
            try (FileOutputStream fos = new FileOutputStream(backupFile)) {
                fos.write(json.getBytes());
            }
            
            // Save last backup time
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    .edit()
                    .putLong(KEY_LAST_BACKUP, System.currentTimeMillis())
                    .apply();
            
            if (callback != null) {
                callback.onSuccess("✅ Sao lưu thành công: " + filename);
            }
            
            return backupFile;
        } catch (IOException e) {
            if (callback != null) {
                callback.onError("❌ Lỗi sao lưu: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Restore from backup file.
     */
    public static Map<String, Object> restoreBackup(Context context, File backupFile, BackupCallback callback) {
        try {
            byte[] bytes = new byte[(int) backupFile.length()];
            try (FileInputStream fis = new FileInputStream(backupFile)) {
                fis.read(bytes);
            }
            
            String json = new String(bytes);
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, Object>>(){}.getType();
            Map<String, Object> data = gson.fromJson(json, type);
            
            if (callback != null) {
                callback.onSuccess("✅ Khôi phục thành công!");
            }
            
            return data;
        } catch (IOException e) {
            if (callback != null) {
                callback.onError("❌ Lỗi khôi phục: " + e.getMessage());
            }
            return null;
        }
    }

    /**
     * Get list of backup files.
     */
    public static File[] getBackupFiles(Context context) {
        File backupDir = new File(context.getExternalFilesDir(null), "backups");
        if (!backupDir.exists()) {
            return new File[0];
        }
        return backupDir.listFiles((dir, name) -> name.endsWith(".json"));
    }

    /**
     * Get last backup time.
     */
    public static long getLastBackupTime(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getLong(KEY_LAST_BACKUP, 0);
    }

    /**
     * Get formatted last backup time.
     */
    public static String getLastBackupTimeFormatted(Context context) {
        long lastBackup = getLastBackupTime(context);
        if (lastBackup == 0) {
            return "Chưa sao lưu";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("vi", "VN"));
        return sdf.format(new Date(lastBackup));
    }

    /**
     * Delete old backups, keep only recent N.
     */
    public static void cleanOldBackups(Context context, int keepCount) {
        File[] backups = getBackupFiles(context);
        if (backups == null || backups.length <= keepCount) return;

        // Sort by date (oldest first)
        java.util.Arrays.sort(backups, (a, b) -> Long.compare(a.lastModified(), b.lastModified()));

        // Delete oldest
        for (int i = 0; i < backups.length - keepCount; i++) {
            backups[i].delete();
        }
    }
}
