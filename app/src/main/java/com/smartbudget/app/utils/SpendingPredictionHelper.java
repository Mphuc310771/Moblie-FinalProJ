package com.smartbudget.app.utils;

import java.util.Calendar;
import java.util.List;

/**
 * Spending prediction helper.
 * Uses simple ML to predict future spending patterns.
 */
public class SpendingPredictionHelper {

    public static class PredictionResult {
        public final double predictedMonthTotal;
        public final double remainingBudget;
        public final String insight;
        public final String emoji;
        public final boolean isOnTrack;

        public PredictionResult(double predictedMonthTotal, double budget,
                               String insight, String emoji, boolean isOnTrack) {
            this.predictedMonthTotal = predictedMonthTotal;
            this.remainingBudget = budget - predictedMonthTotal;
            this.insight = insight;
            this.emoji = emoji;
            this.isOnTrack = isOnTrack;
        }
    }

    /**
     * Predict end-of-month spending based on current spending pattern.
     */
    public static PredictionResult predictMonthEnd(double currentSpending, double monthlyBudget) {
        Calendar calendar = Calendar.getInstance();
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysRemaining = daysInMonth - dayOfMonth;

        // Average daily spending
        double dailyAvg = dayOfMonth > 0 ? currentSpending / dayOfMonth : 0;

        // Predicted total
        double predictedTotal = currentSpending + (dailyAvg * daysRemaining);

        // Analysis
        double percentage = (predictedTotal / monthlyBudget) * 100;
        String insight;
        String emoji;
        boolean isOnTrack;

        if (percentage <= 80) {
            insight = "Tuyệt vời! Bạn sẽ tiết kiệm được " + 
                     formatVND(monthlyBudget - predictedTotal) + " tháng này!";
            emoji = "🎉";
            isOnTrack = true;
        } else if (percentage <= 95) {
            insight = "Đang đi đúng hướng! Giữ vững nhịp chi tiêu này.";
            emoji = "👍";
            isOnTrack = true;
        } else if (percentage <= 105) {
            insight = "Cẩn thận! Có thể sát ngân sách cuối tháng.";
            emoji = "⚠️";
            isOnTrack = false;
        } else {
            insight = "Cảnh báo! Dự kiến vượt ngân sách " + 
                     formatVND(predictedTotal - monthlyBudget) + "!";
            emoji = "🚨";
            isOnTrack = false;
        }

        return new PredictionResult(predictedTotal, monthlyBudget, insight, emoji, isOnTrack);
    }

    /**
     * Predict best day to make a large purchase.
     */
    public static String predictBestDayForPurchase(double currentSpending, double monthlyBudget, 
                                                    double purchaseAmount) {
        Calendar calendar = Calendar.getInstance();
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        double remaining = monthlyBudget - currentSpending;
        double dailyAvg = currentSpending / Math.max(1, dayOfMonth);

        if (purchaseAmount > remaining) {
            return "💸 Nên hoãn sang tháng sau để không vượt ngân sách";
        }

        // Calculate optimal day
        double optimalRemaining = remaining - purchaseAmount;
        int optimalDaysRemaining = (int) (optimalRemaining / dailyAvg);
        int optimalDay = daysInMonth - optimalDaysRemaining;

        if (optimalDay <= dayOfMonth) {
            return "✅ Có thể mua ngay hôm nay!";
        } else if (optimalDay <= dayOfMonth + 7) {
            return "📅 Nên đợi đến ngày " + optimalDay + " để an toàn hơn";
        } else {
            return "⏳ Nên đợi gần cuối tháng (ngày " + optimalDay + ")";
        }
    }

    /**
     * Get spending velocity (trend).
     */
    public static String getSpendingTrend(double lastWeekSpending, double thisWeekSpending) {
        if (thisWeekSpending < lastWeekSpending * 0.8) {
            return "📉 Chi tiêu giảm " + 
                   Math.round((1 - thisWeekSpending / lastWeekSpending) * 100) + "% so với tuần trước";
        } else if (thisWeekSpending > lastWeekSpending * 1.2) {
            return "📈 Chi tiêu tăng " + 
                   Math.round((thisWeekSpending / lastWeekSpending - 1) * 100) + "% so với tuần trước";
        } else {
            return "➡️ Chi tiêu ổn định so với tuần trước";
        }
    }

    private static String formatVND(double amount) {
        return String.format("%,.0f ₫", Math.abs(amount));
    }
}
