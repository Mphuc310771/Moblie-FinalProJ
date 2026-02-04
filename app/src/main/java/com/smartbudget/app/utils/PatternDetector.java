package com.smartbudget.app.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spending pattern detector.
 * Detects recurring expenses and unusual spending patterns.
 */
public class PatternDetector {

    public static class Pattern {
        public PatternType type;
        public String description;
        public String emoji;
        public double amount;
        public String category;
        public int frequency; // days between occurrences

        public Pattern(PatternType type, String emoji, String description) {
            this.type = type;
            this.emoji = emoji;
            this.description = description;
        }
    }

    public enum PatternType {
        DAILY,           // Happens every day
        WEEKLY,          // Happens every week
        MONTHLY,         // Happens every month
        UNUSUAL_SPIKE,   // Sudden increase
        UNUSUAL_DROP,    // Sudden decrease
        WEEKEND_SPENDER, // Spends more on weekends
        END_MONTH_SPIKE  // Spends more at month end
    }

    /**
     * Detect spending patterns from transaction history.
     */
    public static List<Pattern> detectPatterns(List<TransactionData> transactions) {
        List<Pattern> patterns = new ArrayList<>();

        if (transactions == null || transactions.isEmpty()) {
            return patterns;
        }

        // Analyze daily averages by day of week
        Map<Integer, Double> dayOfWeekSpending = new HashMap<>();
        Map<Integer, Integer> dayOfWeekCount = new HashMap<>();

        for (TransactionData t : transactions) {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(t.date);
            int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

            dayOfWeekSpending.merge(dayOfWeek, t.amount, Double::sum);
            dayOfWeekCount.merge(dayOfWeek, 1, Integer::sum);
        }

        // Check weekend spending pattern
        double weekdayAvg = 0, weekendAvg = 0;
        int weekdayDays = 0, weekendDays = 0;

        for (Map.Entry<Integer, Double> entry : dayOfWeekSpending.entrySet()) {
            int day = entry.getKey();
            double avg = entry.getValue() / dayOfWeekCount.getOrDefault(day, 1);

            if (day == Calendar.SATURDAY || day == Calendar.SUNDAY) {
                weekendAvg += avg;
                weekendDays++;
            } else {
                weekdayAvg += avg;
                weekdayDays++;
            }
        }

        if (weekdayDays > 0) weekdayAvg /= weekdayDays;
        if (weekendDays > 0) weekendAvg /= weekendDays;

        if (weekendAvg > weekdayAvg * 1.5) {
            Pattern p = new Pattern(
                    PatternType.WEEKEND_SPENDER,
                    "🎉",
                    String.format("Bạn chi tiêu nhiều hơn %.0f%% vào cuối tuần", 
                            ((weekendAvg / weekdayAvg) - 1) * 100)
            );
            patterns.add(p);
        }

        // Check for recurring category spending
        Map<String, List<Double>> categoryHistory = new HashMap<>();
        for (TransactionData t : transactions) {
            categoryHistory.computeIfAbsent(t.category, k -> new ArrayList<>()).add(t.amount);
        }

        for (Map.Entry<String, List<Double>> entry : categoryHistory.entrySet()) {
            List<Double> amounts = entry.getValue();
            if (amounts.size() >= 3) {
                // Check if similar amounts (potential recurring)
                double first = amounts.get(0);
                boolean isRecurring = amounts.stream()
                        .allMatch(a -> Math.abs(a - first) / first < 0.1);

                if (isRecurring) {
                    Pattern p = new Pattern(
                            PatternType.MONTHLY,
                            "🔄",
                            String.format("Chi tiêu định kỳ: %s - %,.0f₫", entry.getKey(), first)
                    );
                    p.category = entry.getKey();
                    p.amount = first;
                    patterns.add(p);
                }
            }
        }

        return patterns;
    }

    /**
     * Detect unusual spending spike.
     */
    public static Pattern detectSpike(double todaySpending, double averageDaily) {
        if (averageDaily <= 0) return null;

        double ratio = todaySpending / averageDaily;

        if (ratio > 3) {
            return new Pattern(
                    PatternType.UNUSUAL_SPIKE,
                    "🚨",
                    String.format("Chi tiêu hôm nay cao gấp %.1fx bình thường!", ratio)
            );
        } else if (ratio > 2) {
            return new Pattern(
                    PatternType.UNUSUAL_SPIKE,
                    "⚠️",
                    String.format("Chi tiêu hôm nay cao hơn %.0f%% bình thường", (ratio - 1) * 100)
            );
        }

        return null;
    }

    /**
     * Simple transaction data class.
     */
    public static class TransactionData {
        public long date;
        public double amount;
        public String category;

        public TransactionData(long date, double amount, String category) {
            this.date = date;
            this.amount = amount;
            this.category = category;
        }
    }
}
