package com.smartbudget.app.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Investment tips provider.
 * Provides personalized investment advice based on financial situation.
 */
public class InvestmentTipsProvider {

    public static class InvestmentTip {
        public String emoji;
        public String title;
        public String description;
        public String actionText;
        public TipCategory category;

        public InvestmentTip(String emoji, String title, String description, 
                            String actionText, TipCategory category) {
            this.emoji = emoji;
            this.title = title;
            this.description = description;
            this.actionText = actionText;
            this.category = category;
        }
    }

    public enum TipCategory {
        BEGINNER,
        SAVINGS,
        STOCKS,
        BONDS,
        REAL_ESTATE,
        CRYPTO,
        RETIREMENT
    }

    // Investment tips database
    private static final InvestmentTip[] ALL_TIPS = {
            // Beginner
            new InvestmentTip("📚", "Học trước khi đầu tư",
                    "Dành thời gian tìm hiểu về các loại hình đầu tư trước khi bỏ tiền",
                    "Tìm hiểu thêm", TipCategory.BEGINNER),
            new InvestmentTip("💰", "Xây quỹ khẩn cấp trước",
                    "Nên có quỹ khẩn cấp 3-6 tháng chi phí trước khi đầu tư",
                    "Tạo quỹ", TipCategory.BEGINNER),
            new InvestmentTip("📈", "Bắt đầu nhỏ",
                    "Không cần nhiều tiền để bắt đầu đầu tư, bắt đầu từ số nhỏ",
                    "Bắt đầu ngay", TipCategory.BEGINNER),

            // Savings
            new InvestmentTip("🏦", "Gửi tiết kiệm lãi kép",
                    "Lãi kép là 'kỳ quan thứ 8' - bắt đầu sớm để tận dụng",
                    "So sánh lãi suất", TipCategory.SAVINGS),
            new InvestmentTip("📊", "Chia nhỏ tiền gửi",
                    "Gửi tiết kiệm nhiều kỳ hạn khác nhau để linh hoạt hơn",
                    "Xem thêm", TipCategory.SAVINGS),

            // Stocks
            new InvestmentTip("📉", "Mua khi giá giảm",
                    "Cơ hội thường đến khi thị trường điều chỉnh",
                    "Xem cơ hội", TipCategory.STOCKS),
            new InvestmentTip("🎯", "Đa dạng hóa danh mục",
                    "Đừng bỏ tất cả trứng vào một giỏ - mua nhiều loại cổ phiếu",
                    "Tìm hiểu", TipCategory.STOCKS),
            new InvestmentTip("⏳", "Đầu tư dài hạn",
                    "Thị trường chứng khoán thường mang lại lợi nhuận tốt trong dài hạn",
                    "Chiến lược", TipCategory.STOCKS),

            // Real Estate
            new InvestmentTip("🏠", "Mua nhà sớm",
                    "Bất động sản là kênh đầu tư an toàn và tăng giá theo thời gian",
                    "Xem tư vấn", TipCategory.REAL_ESTATE),

            // Retirement
            new InvestmentTip("👴", "Nghĩ đến hưu trí",
                    "Bắt đầu tiết kiệm cho hưu trí từ sớm, dù chỉ 5% thu nhập",
                    "Lên kế hoạch", TipCategory.RETIREMENT),
            new InvestmentTip("🎁", "Tận dụng quỹ hưu trí công ty",
                    "Nhiều công ty matching tiền đóng quỹ hưu trí - đừng bỏ lỡ!",
                    "Kiểm tra", TipCategory.RETIREMENT)
    };

    /**
     * Get personalized investment tips.
     */
    public static List<InvestmentTip> getPersonalizedTips(
            double monthlySavings, 
            double totalSavings,
            boolean hasEmergencyFund,
            int age) {

        List<InvestmentTip> tips = new ArrayList<>();

        // Beginners without emergency fund
        if (!hasEmergencyFund) {
            tips.add(ALL_TIPS[1]); // Build emergency fund first
        }

        // Based on savings level
        if (monthlySavings < 1000000) {
            tips.add(ALL_TIPS[0]); // Learn first
            tips.add(ALL_TIPS[2]); // Start small
        } else if (monthlySavings < 5000000) {
            tips.add(ALL_TIPS[3]); // Compound interest
            tips.add(ALL_TIPS[4]); // Ladder deposits
        } else {
            tips.add(ALL_TIPS[5]); // Buy the dip
            tips.add(ALL_TIPS[6]); // Diversify
            tips.add(ALL_TIPS[7]); // Long term
        }

        // Age-based tips
        if (age < 30) {
            tips.add(ALL_TIPS[9]); // Think about retirement early
        } else if (age >= 30) {
            tips.add(ALL_TIPS[10]); // Company matching
        }

        // Real estate if high savings
        if (totalSavings > 100000000) {
            tips.add(ALL_TIPS[8]); // Real estate
        }

        return tips;
    }

    /**
     * Get random daily tip.
     */
    public static InvestmentTip getDailyTip() {
        return ALL_TIPS[new Random().nextInt(ALL_TIPS.length)];
    }

    /**
     * Get tips by category.
     */
    public static List<InvestmentTip> getTipsByCategory(TipCategory category) {
        List<InvestmentTip> tips = new ArrayList<>();
        for (InvestmentTip tip : ALL_TIPS) {
            if (tip.category == category) {
                tips.add(tip);
            }
        }
        return tips;
    }
}
