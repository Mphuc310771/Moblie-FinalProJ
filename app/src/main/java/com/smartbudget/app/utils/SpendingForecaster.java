package com.smartbudget.app.utils;

import java.util.Calendar;

/**
 * Spending forecaster.
 * Predicts future spending based on historical patterns.
 */
public class SpendingForecaster {

    public static class Forecast {
        public double predictedAmount;
        public double confidence; // 0-1
        public String trend;
        public String emoji;
        public String message;

        public Forecast(double predicted, double confidence, String trend, String emoji) {
            this.predictedAmount = predicted;
            this.confidence = confidence;
            this.trend = trend;
            this.emoji = emoji;
        }
    }

    /**
     * Forecast end-of-month spending.
     */
    public static Forecast forecastMonthEnd(double currentSpending, int currentDay, 
                                            double[] lastMonthsSpending) {
        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysRemaining = daysInMonth - currentDay;

        // Simple linear extrapolation
        double dailyAverage = currentDay > 0 ? currentSpending / currentDay : 0;
        double simpleForcast = currentSpending + (dailyAverage * daysRemaining);

        // Weighted average with historical data
        double historicalFactor = 1.0;
        if (lastMonthsSpending != null && lastMonthsSpending.length > 0) {
            double avgLastMonths = 0;
            for (double m : lastMonthsSpending) {
                avgLastMonths += m;
            }
            avgLastMonths /= lastMonthsSpending.length;

            if (avgLastMonths > 0) {
                historicalFactor = avgLastMonths / (simpleForcast > 0 ? simpleForcast : avgLastMonths);
                historicalFactor = Math.max(0.8, Math.min(1.2, historicalFactor)); // Cap adjustment
            }
        }

        double predicted = simpleForcast * historicalFactor;
        
        // Calculate confidence based on data available
        double confidence = Math.min(0.5 + (currentDay / (double) daysInMonth) * 0.4 +
                (lastMonthsSpending != null ? lastMonthsSpending.length * 0.05 : 0), 0.95);

        // Determine trend
        String trend, emoji;
        if (lastMonthsSpending != null && lastMonthsSpending.length > 0) {
            double lastMonth = lastMonthsSpending[lastMonthsSpending.length - 1];
            double change = ((predicted - lastMonth) / lastMonth) * 100;
            
            if (change < -10) {
                trend = String.format("Giảm %.1f%%", Math.abs(change));
                emoji = "📉";
            } else if (change > 10) {
                trend = String.format("Tăng %.1f%%", change);
                emoji = "📈";
            } else {
                trend = "Ổn định";
                emoji = "➡️";
            }
        } else {
            trend = "Chưa đủ dữ liệu";
            emoji = "📊";
        }

        Forecast forecast = new Forecast(predicted, confidence, trend, emoji);
        forecast.message = generateMessage(predicted, daysRemaining, trend);
        
        return forecast;
    }

    /**
     * Forecast next week spending.
     */
    public static Forecast forecastNextWeek(double thisWeekSpending, double lastWeekSpending) {
        double change = lastWeekSpending > 0 ? (thisWeekSpending - lastWeekSpending) / lastWeekSpending : 0;
        double predicted = thisWeekSpending * (1 + change * 0.5); // Moderate trend continuation

        String trend, emoji;
        if (change < -0.1) {
            trend = "Xu hướng giảm";
            emoji = "📉";
        } else if (change > 0.1) {
            trend = "Xu hướng tăng";
            emoji = "📈";
        } else {
            trend = "Ổn định";
            emoji = "➡️";
        }

        Forecast forecast = new Forecast(predicted, 0.7, trend, emoji);
        forecast.message = String.format("Dự kiến tuần sau: %,.0f₫ %s", predicted, emoji);
        
        return forecast;
    }

    private static String generateMessage(double predicted, int daysRemaining, String trend) {
        return String.format("Dự kiến cuối tháng: %,.0f₫ (%s)\n" +
                "Còn %d ngày | Độ tin cậy: Cao", predicted, trend, daysRemaining);
    }

    /**
     * Get daily spending recommendation to stay on budget.
     */
    public static String getDailyBudgetAdvice(double monthlyBudget, double currentSpending, int currentDay) {
        Calendar cal = Calendar.getInstance();
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        int daysRemaining = daysInMonth - currentDay;

        double remaining = monthlyBudget - currentSpending;
        double dailyBudget = daysRemaining > 0 ? remaining / daysRemaining : 0;

        if (dailyBudget <= 0) {
            return "⚠️ Đã vượt ngân sách! Hạn chế chi tiêu.";
        } else if (dailyBudget < 50000) {
            return String.format("💸 Chỉ còn %,.0f₫/ngày - tiết kiệm nhé!", dailyBudget);
        } else if (dailyBudget < 200000) {
            return String.format("💰 Có thể chi %,.0f₫/ngày", dailyBudget);
        } else {
            return String.format("✨ Còn dư %,.0f₫/ngày - thoải mái!", dailyBudget);
        }
    }
}
