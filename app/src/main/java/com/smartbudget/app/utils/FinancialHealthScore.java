package com.smartbudget.app.utils;

/**
 * Financial health score calculator.
 * Calculates overall financial wellness score.
 */
public class FinancialHealthScore {

    public static class HealthScore {
        public int score; // 0-100
        public String grade; // A, B, C, D, F
        public String emoji;
        public String title;
        public String description;
        public String[] recommendations;

        public HealthScore(int score) {
            this.score = score;
            calculateGrade();
        }

        private void calculateGrade() {
            if (score >= 90) {
                grade = "A+";
                emoji = "🏆";
                title = "Xuất sắc!";
                description = "Tài chính của bạn rất tốt";
            } else if (score >= 80) {
                grade = "A";
                emoji = "🌟";
                title = "Tuyệt vời!";
                description = "Bạn đang quản lý tài chính rất tốt";
            } else if (score >= 70) {
                grade = "B";
                emoji = "👍";
                title = "Tốt";
                description = "Tài chính ổn định, có thể cải thiện thêm";
            } else if (score >= 60) {
                grade = "C";
                emoji = "😐";
                title = "Khá";
                description = "Cần chú ý hơn đến chi tiêu";
            } else if (score >= 50) {
                grade = "D";
                emoji = "⚠️";
                title = "Cần cải thiện";
                description = "Nên xem xét lại thói quen chi tiêu";
            } else {
                grade = "F";
                emoji = "🚨";
                title = "Cảnh báo";
                description = "Tài chính cần được cải thiện ngay";
            }
        }
    }

    /**
     * Calculate financial health score.
     */
    public static HealthScore calculateScore(
            double monthlyIncome,
            double monthlyExpense,
            double savings,
            double debt,
            int streakDays,
            boolean hasEmergencyFund,
            boolean hasGoals) {

        int score = 0;

        // 1. Savings rate (max 25 points)
        double savingsRate = monthlyIncome > 0 ? 
                ((monthlyIncome - monthlyExpense) / monthlyIncome) * 100 : 0;
        if (savingsRate >= 30) score += 25;
        else if (savingsRate >= 20) score += 20;
        else if (savingsRate >= 10) score += 15;
        else if (savingsRate >= 0) score += 10;
        else score += 0;

        // 2. Debt-to-income ratio (max 20 points)
        double debtRatio = monthlyIncome > 0 ? (debt / (monthlyIncome * 12)) * 100 : 0;
        if (debtRatio == 0) score += 20;
        else if (debtRatio < 20) score += 15;
        else if (debtRatio < 40) score += 10;
        else if (debtRatio < 60) score += 5;
        else score += 0;

        // 3. Emergency fund (max 15 points)
        if (hasEmergencyFund) {
            double monthsCovered = savings / monthlyExpense;
            if (monthsCovered >= 6) score += 15;
            else if (monthsCovered >= 3) score += 10;
            else if (monthsCovered >= 1) score += 5;
        }

        // 4. Financial goals (max 15 points)
        if (hasGoals) score += 15;

        // 5. Tracking consistency - streak (max 15 points)
        if (streakDays >= 90) score += 15;
        else if (streakDays >= 30) score += 12;
        else if (streakDays >= 14) score += 8;
        else if (streakDays >= 7) score += 5;

        // 6. Spending control (max 10 points)
        double expenseRatio = monthlyIncome > 0 ? (monthlyExpense / monthlyIncome) * 100 : 100;
        if (expenseRatio <= 50) score += 10;
        else if (expenseRatio <= 70) score += 7;
        else if (expenseRatio <= 90) score += 4;
        else score += 0;

        HealthScore healthScore = new HealthScore(score);
        healthScore.recommendations = getRecommendations(
                savingsRate, debtRatio, hasEmergencyFund, hasGoals, streakDays
        );

        return healthScore;
    }

    private static String[] getRecommendations(
            double savingsRate, double debtRatio,
            boolean hasEmergencyFund, boolean hasGoals, int streakDays) {

        java.util.List<String> recs = new java.util.ArrayList<>();

        if (savingsRate < 20) {
            recs.add("💰 Tăng tỷ lệ tiết kiệm lên ít nhất 20%");
        }

        if (debtRatio > 30) {
            recs.add("📉 Giảm nợ xuống dưới 30% thu nhập năm");
        }

        if (!hasEmergencyFund) {
            recs.add("🏦 Xây dựng quỹ khẩn cấp 3-6 tháng chi phí");
        }

        if (!hasGoals) {
            recs.add("🎯 Đặt mục tiêu tiết kiệm cụ thể");
        }

        if (streakDays < 7) {
            recs.add("📝 Ghi chép chi tiêu đều đặn hàng ngày");
        }

        if (recs.isEmpty()) {
            recs.add("✨ Tiếp tục duy trì thói quen tài chính tốt!");
        }

        return recs.toArray(new String[0]);
    }

    /**
     * Get score color based on value.
     */
    public static String getScoreColor(int score) {
        if (score >= 80) return "#4CAF50"; // Green
        if (score >= 60) return "#FFC107"; // Yellow
        if (score >= 40) return "#FF9800"; // Orange
        return "#F44336"; // Red
    }

    /**
     * Get motivational message based on score.
     */
    public static String getMotivation(int score) {
        if (score >= 90) {
            return "🏆 Bạn là chuyên gia quản lý tài chính!";
        } else if (score >= 70) {
            return "🌟 Tuyệt vời! Tiếp tục phát huy nhé!";
        } else if (score >= 50) {
            return "💪 Bạn đang tiến bộ, cố thêm nữa!";
        } else {
            return "🚀 Bắt đầu cải thiện từ hôm nay!";
        }
    }
}
