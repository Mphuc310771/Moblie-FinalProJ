package com.smartbudget.app.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Sentiment analysis for spending.
 * Analyzes emotional context of transactions.
 */
public class SentimentAnalyzer {

    public enum Emotion {
        HAPPY("😊", "Vui vẻ"),
        SAD("😢", "Buồn"),
        STRESSED("😫", "Mệt mỏi"),
        EXCITED("🤩", "Hào hứng"),
        REGRET("😞", "Hối tiếc"),
        NEUTRAL("😐", "Bình thường");

        public final String emoji;
        public final String label;

        Emotion(String emoji, String label) {
            this.emoji = emoji;
            this.label = label;
        }
    }

    /**
     * Analyze sentiment based on note and category.
     */
    public static Emotion analyze(String note, String category, double amount) {
        if (note == null) note = "";
        String lowerNote = note.toLowerCase();

        // Keyword analysis
        if (lowerNote.contains("thưởng") || lowerNote.contains("quà") || lowerNote.contains("party")) {
            return Emotion.HAPPY;
        }
        if (lowerNote.contains("thuốc") || lowerNote.contains("khám") || lowerNote.contains("phạt")) {
            return Emotion.SAD;
        }
        if (lowerNote.contains("deadline") || lowerNote.contains("gấp") || lowerNote.contains("nợ")) {
            return Emotion.STRESSED;
        }
        if (lowerNote.contains("du lịch") || lowerNote.contains("mua xe") || lowerNote.contains("iphone")) {
            return Emotion.EXCITED;
        }
        if (lowerNote.contains("lỡ") || lowerNote.contains("mất") || lowerNote.contains("đắt")) {
            return Emotion.REGRET;
        }

        // Category context
        if (category.equals("Giải trí") || category.equals("Du lịch")) {
            return Emotion.HAPPY;
        }
        if (category.equals("Y tế") || category.equals("Sửa chữa")) {
            return Emotion.STRESSED;
        }

        // Amount context (high spending might cause regret)
        if (amount > 5000000 && (category.equals("Mua sắm") || category.equals("Ăn uống"))) {
            return Emotion.REGRET;
        }

        return Emotion.NEUTRAL;
    }

    /**
     * Get advice based on emotion.
     */
    public static String getEmotionalAdvice(Emotion emotion) {
        switch (emotion) {
            case HAPPY:
                return "Tuyệt vời! Hãy tận hưởng niềm vui này.";
            case SAD:
                return "Đừng buồn, tiền có thể kiếm lại được.";
            case STRESSED:
                return "Sức khỏe là quan trọng nhất, hãy nghỉ ngơi nhé.";
            case EXCITED:
                return "Hãy cân nhắc kỹ trước khi xuống tiền nhé!";
            case REGRET:
                return "Rút kinh nghiệm cho lần sau, đừng dằn vặt.";
            default:
                return "Giữ tâm lý ổn định là chìa khóa quản lý tài chính.";
        }
    }
}
