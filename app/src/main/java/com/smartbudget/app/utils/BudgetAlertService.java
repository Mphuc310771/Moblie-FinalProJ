package com.smartbudget.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.smartbudget.app.R;
import com.smartbudget.app.data.local.AppDatabase;
import com.smartbudget.app.data.local.entity.BudgetEntity;
import com.smartbudget.app.presentation.MainActivity;

import java.util.Calendar;
import java.util.concurrent.Executors;

/**
 * Service class for monitoring budget thresholds and sending alerts.
 */
public class BudgetAlertService {
    
    private static final String CHANNEL_ID = "budget_alerts";
    private static final int NOTIFICATION_ID_BUDGET_WARNING = 1001;
    private static final int NOTIFICATION_ID_BUDGET_EXCEEDED = 1002;
    
    // Alert when spent >= 80% of budget
    private static final double WARNING_THRESHOLD = 0.8;
    
    private final Context context;
    private final AppDatabase database;
    
    public BudgetAlertService(Context context) {
        this.context = context;
        this.database = AppDatabase.getInstance(context);
        createNotificationChannel();
    }
    
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "Cảnh báo ngân sách",
                NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Thông báo khi vượt hoặc sắp vượt ngân sách");
            
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Check current month's budget and send alerts if needed.
     */
    public void checkBudgetStatus() {
        Executors.newSingleThreadExecutor().execute(() -> {
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            
            BudgetEntity budget = database.budgetDao().getTotalBudget(month, year);
            if (budget == null || budget.getLimitAmount() <= 0) {
                return; // No budget set
            }
            
            double spent = budget.getSpentAmount();
            double limit = budget.getLimitAmount();
            double ratio = spent / limit;
            
            if (ratio >= 1.0) {
                // Exceeded budget
                sendBudgetExceededNotification(spent, limit);
            } else if (ratio >= WARNING_THRESHOLD) {
                // Warning - approaching limit
                sendBudgetWarningNotification(spent, limit, (int)(ratio * 100));
            }
        });
    }
    
    private void sendBudgetWarningNotification(double spent, double limit, int percentage) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent, 
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        String message = String.format(
            "Bạn đã chi %d%% ngân sách tháng này (%.0f₫ / %.0f₫)",
            percentage, spent, limit
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⚠️ Sắp hết ngân sách!")
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        NotificationManager manager = (NotificationManager) 
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_BUDGET_WARNING, builder.build());
        }
    }
    
    private void sendBudgetExceededNotification(double spent, double limit) {
        Intent intent = new Intent(context, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        double exceeded = spent - limit;
        String message = String.format(
            "Bạn đã vượt ngân sách %.0f₫. Tổng chi: %.0f₫ / Giới hạn: %.0f₫",
            exceeded, spent, limit
        );
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("🚨 Vượt ngân sách!")
            .setContentText(message)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true);
        
        NotificationManager manager = (NotificationManager)
            context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(NOTIFICATION_ID_BUDGET_EXCEEDED, builder.build());
        }
    }
    
    /**
     * Get budget status summary for dashboard.
     */
    public interface BudgetStatusCallback {
        void onResult(BudgetStatus status);
    }
    
    public void getBudgetStatus(BudgetStatusCallback callback) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Calendar cal = Calendar.getInstance();
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            
            BudgetEntity budget = database.budgetDao().getTotalBudget(month, year);
            
            BudgetStatus status = new BudgetStatus();
            if (budget != null && budget.getLimitAmount() > 0) {
                status.hasBudget = true;
                status.limit = budget.getLimitAmount();
                status.spent = budget.getSpentAmount();
                status.remaining = status.limit - status.spent;
                status.percentage = (int) ((status.spent / status.limit) * 100);
                status.isExceeded = status.spent > status.limit;
                status.isWarning = status.percentage >= 80 && !status.isExceeded;
            }
            
            if (callback != null) {
                callback.onResult(status);
            }
        });
    }
    
    public static class BudgetStatus {
        public boolean hasBudget = false;
        public double limit = 0;
        public double spent = 0;
        public double remaining = 0;
        public int percentage = 0;
        public boolean isExceeded = false;
        public boolean isWarning = false;
    }
}
