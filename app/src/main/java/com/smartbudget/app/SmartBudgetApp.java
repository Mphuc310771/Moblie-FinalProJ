package com.smartbudget.app;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.utils.ThemeManager;

public class SmartBudgetApp extends Application {

    public static final String CHANNEL_ID = "smartbudget_channel";
    public static final String CHANNEL_NAME = "SmartBudget Notifications";

    @Override
    public void onCreate() {
        super.onCreate();

        // Apply saved theme (Dark Mode)
        ThemeManager.applyTheme(this);

        // Initialize database
        AppDatabase.getDatabase(this);

        // Create notification channel
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Thông báo nhắc nhở ghi chép chi tiêu");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
