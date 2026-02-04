package com.smartbudget.app.utils;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Monthly report generator.
 * Creates comprehensive monthly spending reports.
 */
public class MonthlyReportGenerator {

    public static class MonthlyReport {
        public String month;
        public int year;
        public double totalIncome;
        public double totalExpense;
        public double savings;
        public double savingsRate;
        public String topCategory;
        public double topCategoryAmount;
        public int transactionCount;
        public double averageDaily;
        public double comparedToLastMonth; // percent change
        public String emoji;
        public String summary;

        public MonthlyReport(int month, int year) {
            this.month = getMonthName(month);
            this.year = year;
        }
    }

    /**
     * Generate monthly report.
     */
    public static MonthlyReport generateReport(
            int month, int year,
            double totalIncome, double totalExpense,
            Map<String, Double> categoryBreakdown,
            int transactionCount,
            double lastMonthExpense) {

        MonthlyReport report = new MonthlyReport(month, year);
        report.totalIncome = totalIncome;
        report.totalExpense = totalExpense;
        report.savings = totalIncome - totalExpense;
        report.savingsRate = totalIncome > 0 ? (report.savings / totalIncome) * 100 : 0;
        report.transactionCount = transactionCount;

        // Calculate average daily spending
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, month - 1);
        cal.set(Calendar.YEAR, year);
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        report.averageDaily = totalExpense / daysInMonth;

        // Find top category
        String topCat = "Khác";
        double topAmount = 0;
        for (Map.Entry<String, Double> entry : categoryBreakdown.entrySet()) {
            if (entry.getValue() > topAmount) {
                topAmount = entry.getValue();
                topCat = entry.getKey();
            }
        }
        report.topCategory = topCat;
        report.topCategoryAmount = topAmount;

        // Compare to last month
        if (lastMonthExpense > 0) {
            report.comparedToLastMonth = ((totalExpense - lastMonthExpense) / lastMonthExpense) * 100;
        }

        // Generate emoji based on performance
        if (report.savingsRate >= 30) {
            report.emoji = "🏆";
        } else if (report.savingsRate >= 20) {
            report.emoji = "🌟";
        } else if (report.savingsRate >= 10) {
            report.emoji = "👍";
        } else if (report.savingsRate >= 0) {
            report.emoji = "😐";
        } else {
            report.emoji = "😰";
        }

        // Generate summary
        report.summary = generateSummary(report);

        return report;
    }

    private static String generateSummary(MonthlyReport report) {
        StringBuilder sb = new StringBuilder();
        DecimalFormat formatter = new DecimalFormat("#,###");

        sb.append("📊 Báo cáo tháng ").append(report.month).append(" ").append(report.year).append("\n\n");

        sb.append("💰 Thu nhập: ").append(formatter.format(report.totalIncome)).append(" ₫\n");
        sb.append("💸 Chi tiêu: ").append(formatter.format(report.totalExpense)).append(" ₫\n");
        sb.append("💵 Tiết kiệm: ").append(formatter.format(report.savings)).append(" ₫");
        sb.append(" (").append(String.format("%.1f", report.savingsRate)).append("%)\n\n");

        sb.append("📈 Thống kê:\n");
        sb.append("• ").append(report.transactionCount).append(" giao dịch\n");
        sb.append("• Chi tiêu TB/ngày: ").append(formatter.format(report.averageDaily)).append(" ₫\n");
        sb.append("• ").append(report.topCategory).append(": ")
                .append(formatter.format(report.topCategoryAmount)).append(" ₫\n\n");

        if (report.comparedToLastMonth != 0) {
            if (report.comparedToLastMonth > 0) {
                sb.append("📈 Tăng ").append(String.format("%.1f", report.comparedToLastMonth))
                        .append("% so với tháng trước\n");
            } else {
                sb.append("📉 Giảm ").append(String.format("%.1f", Math.abs(report.comparedToLastMonth)))
                        .append("% so với tháng trước\n");
            }
        }

        // Add advice
        sb.append("\n💡 Lời khuyên:\n");
        if (report.savingsRate < 10) {
            sb.append("• Cố gắng tiết kiệm ít nhất 10-20% thu nhập\n");
        }
        if (report.topCategoryAmount > report.totalExpense * 0.4) {
            sb.append("• Xem xét giảm chi tiêu cho ").append(report.topCategory).append("\n");
        }

        return sb.toString();
    }

    private static String getMonthName(int month) {
        String[] months = {
                "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4",
                "Tháng 5", "Tháng 6", "Tháng 7", "Tháng 8",
                "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12"
        };
        return months[month - 1];
    }

    /**
     * Format report for sharing.
     */
    public static String formatForSharing(MonthlyReport report) {
        return report.emoji + " " + report.summary;
    }
}
