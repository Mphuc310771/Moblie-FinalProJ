package com.smartbudget.app.utils;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Smart spending tips based on user behavior.
 * Provides personalized advice to help users save money.
 */
public class SmartTipsHelper {

    public static class SpendingTip {
        public final String emoji;
        public final String title;
        public final String message;
        public final TipType type;

        public SpendingTip(String emoji, String title, String message, TipType type) {
            this.emoji = emoji;
            this.title = title;
            this.message = message;
            this.type = type;
        }
    }

    public enum TipType {
        SAVING, WARNING, CELEBRATION, INSIGHT, SUGGESTION
    }

    // General tips pool
    private static final SpendingTip[] GENERAL_TIPS = {
        new SpendingTip("💡", "Quy tắc 50/30/20",
                "50% cho nhu cầu, 30% cho mong muốn, 20% tiết kiệm!", TipType.SAVING),
        new SpendingTip("☕", "Tiết kiệm cà phê",
                "Uống cà phê tự pha có thể tiết kiệm 2 triệu/tháng!", TipType.SAVING),
        new SpendingTip("🛒", "Lập danh sách",
                "Luôn lập danh sách trước khi đi mua sắm để tránh mua thừa.", TipType.SUGGESTION),
        new SpendingTip("📱", "Kiểm tra đăng ký",
                "Hủy các subscription không dùng có thể tiết kiệm đáng kể!", TipType.SAVING),
        new SpendingTip("🍱", "Mang cơm trưa",
                "Mang cơm đi làm tiết kiệm 50k-100k mỗi ngày!", TipType.SAVING),
        new SpendingTip("⏰", "Quy tắc 24 giờ",
                "Đợi 24h trước khi mua đồ không cần thiết.", TipType.SUGGESTION),
        new SpendingTip("🎯", "Mục tiêu rõ ràng",
                "Đặt mục tiêu tiết kiệm cụ thể giúp động lực hơn!", TipType.INSIGHT),
        new SpendingTip("💳", "Hạn chế thẻ tín dụng",
                "Dùng tiền mặt giúp kiểm soát chi tiêu tốt hơn.", TipType.SUGGESTION),
        new SpendingTip("📊", "Review hàng tuần",
                "Xem lại chi tiêu mỗi tuần để điều chỉnh kịp thời.", TipType.INSIGHT),
        new SpendingTip("🌱", "Bắt đầu nhỏ",
                "Tiết kiệm 10% lương là bước đầu tuyệt vời!", TipType.SAVING)
    };

    // Context-based tips
    private static final SpendingTip[] WEEKEND_TIPS = {
        new SpendingTip("🎬", "Cuối tuần tiết kiệm",
                "Thay vì đi xem phim, thử picnic công viên miễn phí!", TipType.SUGGESTION),
        new SpendingTip("🏠", "Staycation",
                "Ở nhà thư giãn cũng là cách nghỉ ngơi tuyệt vời!", TipType.SUGGESTION)
    };

    private static final SpendingTip[] HIGH_SPENDING_TIPS = {
        new SpendingTip("⚠️", "Chi tiêu cao",
                "Hôm nay bạn chi nhiều hơn bình thường, cẩn thận nhé!", TipType.WARNING),
        new SpendingTip("🎯", "Về đích",
                "Giữ vững ngân sách để đạt mục tiêu tháng này!", TipType.WARNING)
    };

    private static final SpendingTip[] LOW_SPENDING_TIPS = {
        new SpendingTip("🎉", "Tuyệt vời!",
                "Chi tiêu hôm nay rất hợp lý, tiếp tục phát huy!", TipType.CELEBRATION),
        new SpendingTip("⭐", "Xuất sắc!",
                "Bạn đang trên đường đạt mục tiêu tiết kiệm!", TipType.CELEBRATION)
    };

    /**
     * Get a random general tip.
     */
    public static SpendingTip getRandomTip() {
        return GENERAL_TIPS[new Random().nextInt(GENERAL_TIPS.length)];
    }

    /**
     * Get tip of the day (consistent for entire day).
     */
    public static SpendingTip getTipOfTheDay() {
        int dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR);
        return GENERAL_TIPS[dayOfYear % GENERAL_TIPS.length];
    }

    /**
     * Get smart tip based on context.
     */
    public static SpendingTip getSmartTip(double todaySpending, double avgDailySpending) {
        // Check if it's weekend
        int dayOfWeek = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK);
        boolean isWeekend = dayOfWeek == java.util.Calendar.SATURDAY || 
                           dayOfWeek == java.util.Calendar.SUNDAY;

        // Spending analysis
        if (todaySpending > avgDailySpending * 1.5) {
            // High spending day
            return HIGH_SPENDING_TIPS[new Random().nextInt(HIGH_SPENDING_TIPS.length)];
        } else if (todaySpending < avgDailySpending * 0.5) {
            // Low spending day
            return LOW_SPENDING_TIPS[new Random().nextInt(LOW_SPENDING_TIPS.length)];
        } else if (isWeekend) {
            return WEEKEND_TIPS[new Random().nextInt(WEEKEND_TIPS.length)];
        }

        return getRandomTip();
    }

    /**
     * Get all tips for display.
     */
    public static List<SpendingTip> getAllTips() {
        List<SpendingTip> all = new ArrayList<>();
        for (SpendingTip tip : GENERAL_TIPS) {
            all.add(tip);
        }
        return all;
    }
}
