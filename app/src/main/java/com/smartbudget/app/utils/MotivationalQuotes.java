package com.smartbudget.app.utils;

import java.util.Random;

/**
 * Provides motivational quotes about money and finance.
 * Displayed on dashboard for inspiration.
 */
public class MotivationalQuotes {

    private static final String[] QUOTES = {
        "💰 \"Tiền bạc là người đầy tớ tốt, nhưng là người chủ tồi.\"",
        "📈 \"Đầu tư vào bản thân là khoản đầu tư sinh lời nhất.\"",
        "🎯 \"Đừng tiết kiệm những gì còn lại sau chi tiêu, hãy chi tiêu những gì còn lại sau tiết kiệm.\"",
        "🌱 \"Cây sồi lớn bắt đầu từ một hạt giống nhỏ.\"",
        "💪 \"Tự do tài chính là khi tiền của bạn làm việc cho bạn.\"",
        "🚀 \"Hành trình ngàn dặm bắt đầu từ một bước chân.\"",
        "⭐ \"Thành công là tổng của những nỗ lực nhỏ, lặp đi lặp lại.\"",
        "🔥 \"Đừng làm việc vì tiền, hãy để tiền làm việc cho bạn.\"",
        "💎 \"Giàu có không phải là có nhiều tiền, mà là có nhiều lựa chọn.\"",
        "🎓 \"Học cách quản lý 100k, bạn sẽ biết cách quản lý 100 triệu.\"",
        "🌟 \"Mỗi đồng tiết kiệm là một người lính làm việc cho tương lai.\"",
        "🏆 \"Thói quen nhỏ tạo nên kết quả lớn.\"",
        "💡 \"Chi tiêu thông minh không phải là chi tiêu ít, mà là chi tiêu đúng.\"",
        "🎯 \"Mục tiêu không có kế hoạch chỉ là ước mơ.\"",
        "🌈 \"Hôm nay khó khăn, ngày mai sẽ tốt đẹp hơn.\"",
        "⚡ \"Bắt đầu ngay bây giờ, không phải ngày mai.\"",
        "🎪 \"Cuộc sống là để sống, không chỉ để tiết kiệm.\"",
        "🧠 \"Đầu tư tốt nhất là đầu tư vào kiến thức.\"",
        "🎁 \"Hạnh phúc không mua được bằng tiền, nhưng tiền giúp bạn an tâm.\"",
        "🌻 \"Một xu tiết kiệm là một xu kiếm được.\""
    };

    /**
     * Get a random quote.
     */
    public static String getRandomQuote() {
        return QUOTES[new Random().nextInt(QUOTES.length)];
    }

    /**
     * Get quote of the day (same quote for entire day).
     */
    public static String getQuoteOfTheDay() {
        int dayOfYear = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR);
        return QUOTES[dayOfYear % QUOTES.length];
    }

    /**
     * Get all quotes.
     */
    public static String[] getAllQuotes() {
        return QUOTES;
    }
}
