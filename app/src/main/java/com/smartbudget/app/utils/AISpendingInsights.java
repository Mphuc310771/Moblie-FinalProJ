package com.smartbudget.app.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI-powered spending insights generator.
 * Analyzes spending patterns and provides smart recommendations.
 */
public class AISpendingInsights {

    public static class Insight {
        public String emoji;
        public String title;
        public String description;
        public InsightType type;
        public double value;

        public Insight(String emoji, String title, String description, InsightType type) {
            this.emoji = emoji;
            this.title = title;
            this.description = description;
            this.type = type;
        }
    }

    public enum InsightType {
        POSITIVE,    // Good spending behavior
        WARNING,     // Potential issue
        TIP,         // Helpful suggestion
        ACHIEVEMENT, // Milestone reached
        PREDICTION   // Future forecast
    }

    /**
     * Generate insights from spending data.
     */
    public static List<Insight> generateInsights(
            double monthlyIncome,
            double monthlyExpense,
            Map<String, Double> categorySpending,
            double lastMonthExpense,
            int streakDays) {

        List<Insight> insights = new ArrayList<>();

        // Savings rate insight
        double savingsRate = monthlyIncome > 0 ? 
                ((monthlyIncome - monthlyExpense) / monthlyIncome) * 100 : 0;
        
        if (savingsRate >= 30) {
            insights.add(new Insight(
                    "🏆", "Tiết kiệm xuất sắc!",
                    String.format("Bạn đã tiết kiệm %.1f%% thu nhập - vượt mức khuyến nghị!", savingsRate),
                    InsightType.ACHIEVEMENT
            ));
        } else if (savingsRate >= 20) {
            insights.add(new Insight(
                    "🌟", "Tiết kiệm tốt",
                    String.format("%.1f%% thu nhập được tiết kiệm - tiếp tục phát huy!", savingsRate),
                    InsightType.POSITIVE
            ));
        } else if (savingsRate < 10) {
            insights.add(new Insight(
                    "⚠️", "Cần tiết kiệm hơn",
                    "Nên tiết kiệm ít nhất 10-20% thu nhập hàng tháng",
                    InsightType.WARNING
            ));
        }

        // Month-over-month comparison
        if (lastMonthExpense > 0) {
            double change = ((monthlyExpense - lastMonthExpense) / lastMonthExpense) * 100;
            if (change < -10) {
                insights.add(new Insight(
                        "📉", "Chi tiêu giảm",
                        String.format("Giảm %.1f%% so với tháng trước - tuyệt vời!", Math.abs(change)),
                        InsightType.POSITIVE
                ));
            } else if (change > 20) {
                insights.add(new Insight(
                        "📈", "Chi tiêu tăng mạnh",
                        String.format("Tăng %.1f%% so với tháng trước - nên xem lại!", change),
                        InsightType.WARNING
                ));
            }
        }

        // Category analysis
        String topCategory = "";
        double topAmount = 0;
        for (Map.Entry<String, Double> entry : categorySpending.entrySet()) {
            if (entry.getValue() > topAmount) {
                topAmount = entry.getValue();
                topCategory = entry.getKey();
            }
        }

        if (topAmount > monthlyExpense * 0.4 && monthlyExpense > 0) {
            insights.add(new Insight(
                    "🎯", "Danh mục chi tiêu cao",
                    String.format("%s chiếm %.0f%% tổng chi tiêu", topCategory, 
                            (topAmount / monthlyExpense) * 100),
                    InsightType.TIP
            ));
        }

        // Streak achievement
        if (streakDays >= 30) {
            insights.add(new Insight(
                    "🔥", "Streak tuyệt vời!",
                    String.format("%d ngày liên tục ghi chép - bạn là người kiên trì!", streakDays),
                    InsightType.ACHIEVEMENT
            ));
        } else if (streakDays >= 7) {
            insights.add(new Insight(
                    "✨", "Streak đang tốt",
                    String.format("%d ngày liên tục - cố thêm để đạt 30!", streakDays),
                    InsightType.POSITIVE
            ));
        }

        // Tips
        insights.add(new Insight(
                "💡", "Mẹo tiết kiệm",
                getRandomTip(),
                InsightType.TIP
        ));

        return insights;
    }

    private static String getRandomTip() {
        String[] tips = {
                "Áp dụng quy tắc 50/30/20: 50% nhu cầu, 30% mong muốn, 20% tiết kiệm",
                "Đặt mục tiêu tiết kiệm cụ thể sẽ giúp bạn có động lực hơn",
                "Kiểm tra và so sánh giá trước khi mua sắm lớn",
                "Nấu ăn tại nhà có thể tiết kiệm đến 40% chi phí ăn uống",
                "Sử dụng phương tiện công cộng để giảm chi phí di chuyển",
                "Đặt một quỹ khẩn cấp bằng 3-6 tháng chi phí sinh hoạt",
                "Hủy các subscription không sử dụng để tiết kiệm tiền",
                "Mua hàng với danh sách để tránh mua sắm bốc đồng"
        };
        return tips[(int) (Math.random() * tips.length)];
    }

    /**
     * Get daily spending recommendation.
     */
    public static String getDailyRecommendation(double remainingBudget, int daysRemaining) {
        if (daysRemaining <= 0) return "Tháng mới sắp bắt đầu!";
        
        double dailyBudget = remainingBudget / daysRemaining;
        
        if (dailyBudget <= 0) {
            return "⚠️ Đã vượt ngân sách - hạn chế chi tiêu!";
        } else if (dailyBudget < 100000) {
            return String.format("💰 Chi tiêu tối đa %,.0f₫/ngày", dailyBudget);
        } else {
            return String.format("✨ Có thể chi %,.0f₫/ngày còn lại", dailyBudget);
        }
    }
}
