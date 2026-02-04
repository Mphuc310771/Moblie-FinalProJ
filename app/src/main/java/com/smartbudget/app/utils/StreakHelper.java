package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Tracks daily usage streaks.
 * Gamifies the app experience and encourages daily usage.
 */
public class StreakHelper {

    private static final String PREFS_NAME = "streak_prefs";
    private static final String KEY_CURRENT_STREAK = "current_streak";
    private static final String KEY_LONGEST_STREAK = "longest_streak";
    private static final String KEY_LAST_OPEN_DATE = "last_open_date";

    /**
     * Call this on app launch to update streak.
     * Returns the current streak count.
     */
    public static int updateStreak(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        
        String today = getTodayString();
        String lastOpen = prefs.getString(KEY_LAST_OPEN_DATE, "");
        int currentStreak = prefs.getInt(KEY_CURRENT_STREAK, 0);
        int longestStreak = prefs.getInt(KEY_LONGEST_STREAK, 0);

        if (today.equals(lastOpen)) {
            // Already opened today, no change
            return currentStreak;
        }

        String yesterday = getYesterdayString();
        
        if (lastOpen.equals(yesterday)) {
            // Consecutive day! Increase streak
            currentStreak++;
        } else if (lastOpen.isEmpty()) {
            // First time user
            currentStreak = 1;
        } else {
            // Streak broken, reset to 1
            currentStreak = 1;
        }

        // Update longest streak
        if (currentStreak > longestStreak) {
            longestStreak = currentStreak;
        }

        // Save
        prefs.edit()
                .putString(KEY_LAST_OPEN_DATE, today)
                .putInt(KEY_CURRENT_STREAK, currentStreak)
                .putInt(KEY_LONGEST_STREAK, longestStreak)
                .apply();

        return currentStreak;
    }

    public static int getCurrentStreak(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_CURRENT_STREAK, 0);
    }

    public static int getLongestStreak(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getInt(KEY_LONGEST_STREAK, 0);
    }

    /**
     * Get emoji for streak milestones.
     */
    public static String getStreakEmoji(int streak) {
        if (streak >= 365) return "🏆"; // 1 year champion
        if (streak >= 100) return "💎"; // Diamond
        if (streak >= 30) return "👑";  // Crown
        if (streak >= 14) return "⭐";  // Star
        if (streak >= 7) return "🔥";   // Fire
        if (streak >= 3) return "✨";   // Sparkle
        return "💪";                     // Strong
    }

    /**
     * Get motivational message based on streak.
     */
    public static String getStreakMessage(int streak) {
        if (streak >= 365) return "1 năm liền! Bạn là huyền thoại!";
        if (streak >= 100) return "100 ngày! Không gì cản được bạn!";
        if (streak >= 30) return "1 tháng! Thói quen tuyệt vời!";
        if (streak >= 14) return "2 tuần! Tiếp tục phát huy!";
        if (streak >= 7) return "1 tuần! Bạn đang làm rất tốt!";
        if (streak >= 3) return "Chuỗi 3 ngày! Giữ vững nhé!";
        if (streak == 1) return "Ngày đầu tiên! Bắt đầu thôi!";
        return "Quay lại rồi! Tiếp tục nào!";
    }

    private static String getTodayString() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private static String getYesterdayString() {
        long yesterday = System.currentTimeMillis() - 24 * 60 * 60 * 1000;
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(yesterday));
    }
}
