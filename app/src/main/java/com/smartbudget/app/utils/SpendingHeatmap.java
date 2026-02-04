package com.smartbudget.app.utils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Spending heatmap helper.
 * Calculates intensity for calendar heatmap display.
 */
public class SpendingHeatmap {

    public static class DayData {
        public final int dayOfMonth;
        public final double amount;
        public final float intensity; // 0.0 - 1.0
        public final String color;

        public DayData(int dayOfMonth, double amount, float intensity, String color) {
            this.dayOfMonth = dayOfMonth;
            this.amount = amount;
            this.intensity = intensity;
            this.color = color;
        }
    }

    // Intensity to color mapping
    private static final String[] INTENSITY_COLORS = {
        "#E8F5E9", // Very low (green - saving)
        "#C8E6C9",
        "#A5D6A7",
        "#FFECB3", // Medium (yellow - normal)
        "#FFE082",
        "#FFCC80",
        "#FFAB91", // High (orange - warning)
        "#FF8A65",
        "#EF5350", // Very high (red - danger)
        "#E53935"
    };

    /**
     * Calculate spending intensity for a day.
     * @param amount Spending amount
     * @param dailyBudget Daily budget
     * @return Intensity from 0.0 to 1.0
     */
    public static float calculateIntensity(double amount, double dailyBudget) {
        if (dailyBudget <= 0) return 0.5f;
        
        float ratio = (float) (amount / dailyBudget);
        return Math.min(1.0f, Math.max(0.0f, ratio));
    }

    /**
     * Get color for intensity level.
     */
    public static String getColorForIntensity(float intensity) {
        int index = Math.min((int) (intensity * 10), INTENSITY_COLORS.length - 1);
        return INTENSITY_COLORS[index];
    }

    /**
     * Generate month heatmap data.
     */
    public static DayData[] generateMonthData(Map<Integer, Double> dailySpending, double dailyBudget) {
        Calendar calendar = Calendar.getInstance();
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        DayData[] monthData = new DayData[daysInMonth];
        
        for (int day = 1; day <= daysInMonth; day++) {
            double amount = dailySpending.getOrDefault(day, 0.0);
            float intensity = calculateIntensity(amount, dailyBudget);
            String color = getColorForIntensity(intensity);
            
            monthData[day - 1] = new DayData(day, amount, intensity, color);
        }
        
        return monthData;
    }

    /**
     * Get spending summary for heatmap.
     */
    public static String getSummary(DayData[] monthData) {
        int lowCount = 0, mediumCount = 0, highCount = 0;
        
        for (DayData day : monthData) {
            if (day.intensity < 0.4f) lowCount++;
            else if (day.intensity < 0.7f) mediumCount++;
            else highCount++;
        }
        
        if (highCount > mediumCount && highCount > lowCount) {
            return "🔴 Tháng này chi tiêu khá cao";
        } else if (lowCount > mediumCount && lowCount > highCount) {
            return "🟢 Tháng này tiết kiệm tốt!";
        } else {
            return "🟡 Tháng này chi tiêu ổn định";
        }
    }

    /**
     * Get emoji for day intensity.
     */
    public static String getEmojiForIntensity(float intensity) {
        if (intensity < 0.2f) return "💚";
        if (intensity < 0.4f) return "✅";
        if (intensity < 0.6f) return "🟡";
        if (intensity < 0.8f) return "🟠";
        return "🔴";
    }
}
