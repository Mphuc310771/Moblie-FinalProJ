package com.smartbudget.app.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced voice command system.
 * Supports natural language expense entry and app control.
 */
public class VoiceCommandSystem {

    public enum CommandType {
        ADD_EXPENSE,
        ADD_INCOME,
        SHOW_BALANCE,
        SHOW_REPORT,
        SET_BUDGET,
        SEARCH,
        UNKNOWN
    }

    public static class ParsedCommand {
        public CommandType type;
        public double amount;
        public String category;
        public String note;
        public String rawText;

        public ParsedCommand(CommandType type) {
            this.type = type;
        }
    }

    public interface CommandListener {
        void onCommandParsed(ParsedCommand command);
        void onListening();
        void onError(String error);
    }

    private static final Map<String, String> CATEGORY_KEYWORDS = new HashMap<>();
    
    static {
        CATEGORY_KEYWORDS.put("ăn", "Ăn uống");
        CATEGORY_KEYWORDS.put("uống", "Ăn uống");
        CATEGORY_KEYWORDS.put("cơm", "Ăn uống");
        CATEGORY_KEYWORDS.put("phở", "Ăn uống");
        CATEGORY_KEYWORDS.put("cafe", "Ăn uống");
        CATEGORY_KEYWORDS.put("trà sữa", "Ăn uống");
        CATEGORY_KEYWORDS.put("grab", "Di chuyển");
        CATEGORY_KEYWORDS.put("taxi", "Di chuyển");
        CATEGORY_KEYWORDS.put("xăng", "Di chuyển");
        CATEGORY_KEYWORDS.put("xe", "Di chuyển");
        CATEGORY_KEYWORDS.put("mua", "Mua sắm");
        CATEGORY_KEYWORDS.put("shopping", "Mua sắm");
        CATEGORY_KEYWORDS.put("quần áo", "Mua sắm");
        CATEGORY_KEYWORDS.put("điện", "Hóa đơn");
        CATEGORY_KEYWORDS.put("nước", "Hóa đơn");
        CATEGORY_KEYWORDS.put("internet", "Hóa đơn");
        CATEGORY_KEYWORDS.put("thuốc", "Y tế");
        CATEGORY_KEYWORDS.put("bác sĩ", "Y tế");
        CATEGORY_KEYWORDS.put("khám", "Y tế");
        CATEGORY_KEYWORDS.put("phim", "Giải trí");
        CATEGORY_KEYWORDS.put("game", "Giải trí");
        CATEGORY_KEYWORDS.put("nhạc", "Giải trí");
    }

    private Context context;
    private SpeechRecognizer recognizer;
    private CommandListener listener;

    public VoiceCommandSystem(Context context) {
        this.context = context;
    }

    public void setCommandListener(CommandListener listener) {
        this.listener = listener;
    }

    public void startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            if (listener != null) listener.onError("Thiết bị không hỗ trợ nhận dạng giọng nói");
            return;
        }

        recognizer = SpeechRecognizer.createSpeechRecognizer(context);
        recognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                if (listener != null) listener.onListening();
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    ParsedCommand command = parseCommand(matches.get(0));
                    if (listener != null) listener.onCommandParsed(command);
                }
            }

            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}

            @Override
            public void onError(int error) {
                if (listener != null) listener.onError("Lỗi nhận dạng giọng nói");
            }
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "🎤 Nói lệnh...");

        recognizer.startListening(intent);
    }

    public void stopListening() {
        if (recognizer != null) {
            recognizer.stopListening();
            recognizer.destroy();
        }
    }

    /**
     * Parse natural language command.
     */
    public static ParsedCommand parseCommand(String text) {
        text = text.toLowerCase().trim();
        ParsedCommand command = new ParsedCommand(CommandType.UNKNOWN);
        command.rawText = text;

        // Detect command type
        if (text.contains("chi") || text.contains("mua") || text.contains("trả")) {
            command.type = CommandType.ADD_EXPENSE;
        } else if (text.contains("thu") || text.contains("nhận") || text.contains("lương")) {
            command.type = CommandType.ADD_INCOME;
        } else if (text.contains("số dư") || text.contains("còn bao nhiêu")) {
            command.type = CommandType.SHOW_BALANCE;
            return command;
        } else if (text.contains("báo cáo") || text.contains("thống kê")) {
            command.type = CommandType.SHOW_REPORT;
            return command;
        } else if (text.contains("tìm") || text.contains("tìm kiếm")) {
            command.type = CommandType.SEARCH;
            command.note = text.replace("tìm", "").replace("kiếm", "").trim();
            return command;
        }

        // Extract amount
        Pattern amountPattern = Pattern.compile("(\\d+(?:[.,]\\d+)?)(\\s*(k|nghìn|ngàn|triệu|tr))?");
        Matcher matcher = amountPattern.matcher(text);
        if (matcher.find()) {
            String numStr = matcher.group(1).replace(",", ".");
            double amount = Double.parseDouble(numStr);
            String unit = matcher.group(3);
            
            if (unit != null) {
                if (unit.equals("k") || unit.equals("nghìn") || unit.equals("ngàn")) {
                    amount *= 1000;
                } else if (unit.equals("triệu") || unit.equals("tr")) {
                    amount *= 1000000;
                }
            }
            command.amount = amount;
        }

        // Detect category
        for (Map.Entry<String, String> entry : CATEGORY_KEYWORDS.entrySet()) {
            if (text.contains(entry.getKey())) {
                command.category = entry.getValue();
                break;
            }
        }
        if (command.category == null) {
            command.category = "Khác";
        }

        // Extract note (rest of text)
        command.note = text;

        return command;
    }

    /**
     * Get command description for UI.
     */
    public static String getCommandDescription(ParsedCommand command) {
        switch (command.type) {
            case ADD_EXPENSE:
                return String.format("💸 Chi: %,.0f₫ - %s", command.amount, command.category);
            case ADD_INCOME:
                return String.format("💰 Thu: %,.0f₫", command.amount);
            case SHOW_BALANCE:
                return "📊 Xem số dư";
            case SHOW_REPORT:
                return "📈 Xem báo cáo";
            case SEARCH:
                return "🔍 Tìm: " + command.note;
            default:
                return "❓ Không hiểu lệnh";
        }
    }
}
