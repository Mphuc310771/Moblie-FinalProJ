package com.smartbudget.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.smartbudget.app.R;
import com.smartbudget.app.presentation.MainActivity;

/**
 * Budget alerts helper.
 * Sends notifications when spending approaches limits.
 */
public class BudgetAlertHelper {

    private static final String CHANNEL_ID = "budget_alerts";
    private static final String CHANNEL_NAME = "Cảnh báo ngân sách";

    public enum AlertType {
        BUDGET_50("50% ngân sách", "Bạn đã chi 50% ngân sách tháng này 💰", 1),
        BUDGET_75("75% ngân sách", "Chỉ còn 25% ngân sách, hãy cẩn thận! ⚠️", 2),
        BUDGET_90("Gần hết ngân sách!", "Bạn đã chi 90% ngân sách, nên tiết kiệm hơn! 🚨", 3),
        BUDGET_EXCEEDED("Vượt ngân sách!", "Bạn đã vượt quá ngân sách tháng này! 😱", 4),
        DAILY_LIMIT("Giới hạn ngày", "Chi tiêu hôm nay đã đạt giới hạn ⏰", 5),
        GOAL_PROGRESS("Mục tiêu tiết kiệm", "Tiến độ mục tiêu tiết kiệm đã được cập nhật! 🎯", 6),
        GOAL_ACHIEVED("Đạt mục tiêu!", "Chúc mừng! Bạn đã đạt mục tiêu tiết kiệm! 🎉", 7),
        STREAK_REMINDER("Duy trì streak!", "Đừng quên ghi chép để duy trì streak! 🔥", 8);

        public final String title;
        public final String message;
        public final int id;

        AlertType(String title, String message, int id) {
            this.title = title;
            this.message = message;
            this.id = id;
        }
    }

    /**
     * Create notification channel (required for Android O+).
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Thông báo về ngân sách và chi tiêu");

            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Show a budget alert notification.
     */
    public static void showAlert(Context context, AlertType alertType) {
        showAlert(context, alertType.title, alertType.message, alertType.id);
    }

    /**
     * Show custom notification.
     */
    public static void showAlert(Context context, String title, String message, int notificationId) {
        createNotificationChannel(context);

        // Intent to open app
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) 
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }

    /**
     * Check spending and trigger appropriate alert.
     */
    public static void checkAndAlert(Context context, double currentSpending, double budget) {
        if (budget <= 0) return;

        double percentage = (currentSpending / budget) * 100;

        if (percentage >= 100) {
            showAlert(context, AlertType.BUDGET_EXCEEDED);
        } else if (percentage >= 90) {
            showAlert(context, AlertType.BUDGET_90);
        } else if (percentage >= 75) {
            showAlert(context, AlertType.BUDGET_75);
        } else if (percentage >= 50) {
            showAlert(context, AlertType.BUDGET_50);
        }
    }
}
