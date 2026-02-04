package com.smartbudget.app.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Smart search helper.
 * Advanced search with filters and natural language.
 */
public class SmartSearchHelper {

    public static class SearchQuery {
        public String keyword;
        public String category;
        public Double minAmount;
        public Double maxAmount;
        public Long startDate;
        public Long endDate;
        public String location;
        public boolean isIncome;
        public boolean isExpense;
    }

    public static class SearchResult {
        public String id;
        public String note;
        public double amount;
        public String category;
        public long date;
        public String highlightedText;
        public float relevanceScore;

        public SearchResult(String id, String note, double amount, String category, long date) {
            this.id = id;
            this.note = note;
            this.amount = amount;
            this.category = category;
            this.date = date;
        }
    }

    /**
     * Parse natural language search query.
     */
    public static SearchQuery parseQuery(String query) {
        SearchQuery result = new SearchQuery();
        String lower = query.toLowerCase().trim();

        // Extract amount patterns
        Pattern amountPattern = Pattern.compile("(trên|dưới|từ|đến)?\\s*(\\d+(?:[.,]\\d+)?)(k|nghìn|triệu|tr)?");
        Matcher matcher = amountPattern.matcher(lower);
        
        while (matcher.find()) {
            String prefix = matcher.group(1);
            double amount = parseAmount(matcher.group(2), matcher.group(3));
            
            if ("trên".equals(prefix) || "từ".equals(prefix)) {
                result.minAmount = amount;
            } else if ("dưới".equals(prefix) || "đến".equals(prefix)) {
                result.maxAmount = amount;
            } else {
                // Exact or approximate
                result.minAmount = amount * 0.9;
                result.maxAmount = amount * 1.1;
            }
        }

        // Extract time patterns
        if (lower.contains("hôm nay")) {
            long today = getTodayStart();
            result.startDate = today;
            result.endDate = today + 24 * 60 * 60 * 1000;
        } else if (lower.contains("tuần này")) {
            result.startDate = getWeekStart();
        } else if (lower.contains("tháng này")) {
            result.startDate = getMonthStart();
        } else if (lower.contains("tuần trước")) {
            result.startDate = getWeekStart() - 7 * 24 * 60 * 60 * 1000;
            result.endDate = getWeekStart();
        }

        // Extract category
        String[] categories = {"ăn uống", "di chuyển", "mua sắm", "giải trí", "hóa đơn", "y tế"};
        for (String cat : categories) {
            if (lower.contains(cat)) {
                result.category = capitalizeFirst(cat);
                break;
            }
        }

        // Extract type
        if (lower.contains("thu") || lower.contains("nhận")) {
            result.isIncome = true;
        }
        if (lower.contains("chi") || lower.contains("tiêu")) {
            result.isExpense = true;
        }

        // Extract keyword (remove parsed parts)
        String keyword = lower
                .replaceAll("(trên|dưới|từ|đến)?\\s*\\d+(?:[.,]\\d+)?(k|nghìn|triệu|tr)?", "")
                .replaceAll("(hôm nay|tuần này|tháng này|tuần trước)", "")
                .replaceAll("(ăn uống|di chuyển|mua sắm|giải trí|hóa đơn|y tế)", "")
                .replaceAll("(chi|thu|tiêu|nhận)", "")
                .trim();
        
        if (!keyword.isEmpty()) {
            result.keyword = keyword;
        }

        return result;
    }

    /**
     * Highlight matching text.
     */
    public static String highlightMatch(String text, String keyword) {
        if (keyword == null || keyword.isEmpty()) return text;
        
        String lower = text.toLowerCase();
        int index = lower.indexOf(keyword.toLowerCase());
        
        if (index >= 0) {
            return text.substring(0, index) + 
                   "【" + text.substring(index, index + keyword.length()) + "】" +
                   text.substring(index + keyword.length());
        }
        
        return text;
    }

    /**
     * Calculate relevance score.
     */
    public static float calculateRelevance(SearchResult result, SearchQuery query) {
        float score = 0;

        // Keyword match
        if (query.keyword != null && result.note != null) {
            if (result.note.toLowerCase().contains(query.keyword.toLowerCase())) {
                score += 0.4f;
            }
        }

        // Category match
        if (query.category != null && query.category.equalsIgnoreCase(result.category)) {
            score += 0.3f;
        }

        // Amount match
        if (query.minAmount != null && query.maxAmount != null) {
            if (result.amount >= query.minAmount && result.amount <= query.maxAmount) {
                score += 0.2f;
            }
        }

        // Recency bonus
        long daysAgo = (System.currentTimeMillis() - result.date) / (24 * 60 * 60 * 1000);
        if (daysAgo < 7) score += 0.1f;

        return Math.min(1.0f, score);
    }

    private static double parseAmount(String number, String unit) {
        double amount = Double.parseDouble(number.replace(",", "."));
        if (unit != null) {
            if (unit.equals("k") || unit.equals("nghìn")) amount *= 1000;
            else if (unit.equals("triệu") || unit.equals("tr")) amount *= 1000000;
        }
        return amount;
    }

    private static long getTodayStart() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        cal.set(java.util.Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private static long getWeekStart() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }

    private static long getMonthStart() {
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.set(java.util.Calendar.DAY_OF_MONTH, 1);
        cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
        cal.set(java.util.Calendar.MINUTE, 0);
        cal.set(java.util.Calendar.SECOND, 0);
        return cal.getTimeInMillis();
    }

    private static String capitalizeFirst(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
