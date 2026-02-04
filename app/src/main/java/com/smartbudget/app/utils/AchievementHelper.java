package com.smartbudget.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

/**
 * Achievement/Badge system.
 * Unlocks badges based on user actions and milestones.
 */
public class AchievementHelper {

    private static final String PREFS_NAME = "achievements_prefs";

    public enum Badge {
        // Getting started
        FIRST_EXPENSE("🌟", "Bước đầu tiên", "Ghi lại chi tiêu đầu tiên"),
        FIRST_WEEK("📆", "1 tuần sử dụng", "Dùng app liên tục 7 ngày"),
        FIRST_MONTH("🗓️", "1 tháng sử dụng", "Dùng app liên tục 30 ngày"),

        // Saving goals
        FIRST_GOAL("🎯", "Mục tiêu đầu tiên", "Tạo mục tiêu tiết kiệm"),
        GOAL_ACHIEVED("🏆", "Đạt mục tiêu", "Hoàn thành một mục tiêu"),
        FIVE_GOALS("⭐", "5 mục tiêu", "Hoàn thành 5 mục tiêu"),

        // Consistency
        DAILY_TRACKER("📝", "Người ghi chép", "Ghi 10 giao dịch"),
        SUPER_TRACKER("✍️", "Siêu ghi chép", "Ghi 100 giao dịch"),
        MASTER_TRACKER("💎", "Bậc thầy", "Ghi 500 giao dịch"),

        // Savings
        SAVER_BRONZE("🥉", "Tiết kiệm đồng", "Tiết kiệm 100k"),
        SAVER_SILVER("🥈", "Tiết kiệm bạc", "Tiết kiệm 1 triệu"),
        SAVER_GOLD("🥇", "Tiết kiệm vàng", "Tiết kiệm 10 triệu"),

        // Special
        AI_EXPLORER("🤖", "Khám phá AI", "Sử dụng trợ lý AI"),
        SCAN_MASTER("📸", "Quét nhanh", "Quét 10 hóa đơn"),
        STREAK_FIRE("🔥", "Chuỗi lửa", "Streak 30 ngày"),
        LEGEND("👑", "Huyền thoại", "Đạt tất cả thành tựu");

        public final String emoji;
        public final String title;
        public final String description;

        Badge(String emoji, String title, String description) {
            this.emoji = emoji;
            this.title = title;
            this.description = description;
        }
    }

    /**
     * Check if badge is unlocked.
     */
    public static boolean isBadgeUnlocked(Context context, Badge badge) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(badge.name(), false);
    }

    /**
     * Unlock a badge. Returns true if newly unlocked.
     */
    public static boolean unlockBadge(Context context, Badge badge) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (prefs.getBoolean(badge.name(), false)) {
            return false; // Already unlocked
        }
        prefs.edit().putBoolean(badge.name(), true).apply();
        return true;
    }

    /**
     * Get all unlocked badges.
     */
    public static List<Badge> getUnlockedBadges(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        List<Badge> unlocked = new ArrayList<>();
        for (Badge badge : Badge.values()) {
            if (prefs.getBoolean(badge.name(), false)) {
                unlocked.add(badge);
            }
        }
        return unlocked;
    }

    /**
     * Get progress towards all badges.
     */
    public static int getProgressPercent(Context context) {
        int total = Badge.values().length;
        int unlocked = getUnlockedBadges(context).size();
        return (unlocked * 100) / total;
    }

    /**
     * Get badge display string.
     */
    public static String getBadgeDisplay(Badge badge) {
        return badge.emoji + " " + badge.title;
    }
}
