package com.smartbudget.app.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Calendar;

/**
 * Smart notification scheduler.
 * Schedules intelligent reminders based on user behavior.
 */
public class NotificationScheduler {

    private static final String PREFS_NAME = "notification_prefs";
    private static final String KEY_DAILY_REMINDER = "daily_reminder";
    private static final String KEY_WEEKLY_REPORT = "weekly_report";
    private static final String KEY_STREAK_REMINDER = "streak_reminder";

    public enum NotificationType {
        DAILY_REMINDER("Nhắc nhở ghi chép", "📝 Đừng quên ghi lại chi tiêu hôm nay!", 1001),
        WEEKLY_REPORT("Báo cáo tuần", "📊 Xem báo cáo chi tiêu tuần này!", 1002),
        STREAK_REMINDER("Duy trì streak", "🔥 Đừng để mất streak! Ghi chép ngay!", 1003),
        BUDGET_WARNING("Cảnh báo ngân sách", "⚠️ Bạn đã chi gần hết ngân sách!", 1004),
        GOAL_PROGRESS("Tiến độ mục tiêu", "🎯 Cập nhật tiến độ mục tiêu tiết kiệm!", 1005),
        MONTHLY_SUMMARY("Tổng kết tháng", "📈 Tổng kết chi tiêu tháng này!", 1006);

        public final String title;
        public final String message;
        public final int id;

        NotificationType(String title, String message, int id) {
            this.title = title;
            this.message = message;
            this.id = id;
        }
    }

    /**
     * Schedule daily reminder at specific time.
     */
    public static void scheduleDailyReminder(Context context, int hour, int minute) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        // If time has passed today, schedule for tomorrow
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("type", NotificationType.DAILY_REMINDER.name());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                NotificationType.DAILY_REMINDER.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );

        savePreference(context, KEY_DAILY_REMINDER, true);
    }

    /**
     * Schedule weekly report every Sunday.
     */
    public static void scheduleWeeklyReport(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        calendar.set(Calendar.HOUR_OF_DAY, 20);
        calendar.set(Calendar.MINUTE, 0);

        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.putExtra("type", NotificationType.WEEKLY_REPORT.name());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                NotificationType.WEEKLY_REPORT.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
        );

        savePreference(context, KEY_WEEKLY_REPORT, true);
    }

    /**
     * Cancel all scheduled notifications.
     */
    public static void cancelAllNotifications(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) return;

        for (NotificationType type : NotificationType.values()) {
            Intent intent = new Intent(context, NotificationReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,
                    type.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
            );
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
     * Get optimal reminder time based on user activity.
     */
    public static int[] getOptimalReminderTime(Context context) {
        // Default: 21:00 (9 PM)
        return new int[]{21, 0};
    }

    private static void savePreference(Context context, String key, boolean value) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(key, value)
                .apply();
    }

    public static boolean isEnabled(Context context, String key) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(key, false);
    }

    /**
     * Placeholder receiver class (should be implemented separately).
     */
    public static class NotificationReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String typeName = intent.getStringExtra("type");
            if (typeName != null) {
                NotificationType type = NotificationType.valueOf(typeName);
                BudgetAlertHelper.showAlert(context, type.title, type.message, type.id);
            }
        }
    }
}
