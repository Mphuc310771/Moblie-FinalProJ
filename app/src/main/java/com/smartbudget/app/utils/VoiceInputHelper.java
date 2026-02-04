package com.smartbudget.app.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Voice input helper for hands-free expense entry.
 * Parses spoken input to extract amount and description.
 */
public class VoiceInputHelper {

    public interface VoiceResultListener {
        void onResult(VoiceParseResult result);
        void onError(String message);
    }

    public static class VoiceParseResult {
        public double amount;
        public String description;
        public boolean hasAmount;

        public VoiceParseResult(double amount, String description) {
            this.amount = amount;
            this.description = description;
            this.hasAmount = amount > 0;
        }
    }

    private SpeechRecognizer speechRecognizer;
    private final Context context;
    private VoiceResultListener listener;

    public VoiceInputHelper(Context context) {
        this.context = context;
    }

    /**
     * Start listening for voice input.
     */
    public void startListening(VoiceResultListener listener) {
        this.listener = listener;

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            if (listener != null) {
                listener.onError("Thiết bị không hỗ trợ nhận dạng giọng nói");
            }
            return;
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                String message = "Lỗi nhận dạng giọng nói";
                if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                    message = "Không nhận được giọng nói";
                } else if (error == SpeechRecognizer.ERROR_NETWORK) {
                    message = "Lỗi kết nối mạng";
                }
                if (listener != null) {
                    listener.onError(message);
                }
            }

            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches = results.getStringArrayList(
                        SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    String spokenText = matches.get(0);
                    VoiceParseResult parsed = parseVoiceInput(spokenText);
                    if (listener != null) {
                        listener.onResult(parsed);
                    }
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        });

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, new Locale("vi", "VN").toString());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói số tiền và ghi chú...");

        speechRecognizer.startListening(intent);
    }

    /**
     * Stop listening.
     */
    public void stopListening() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
            speechRecognizer.destroy();
            speechRecognizer = null;
        }
    }

    /**
     * Parse spoken text to extract amount and description.
     * Supports: "50 nghìn ăn trưa", "hai trăm nghìn mua sắm", etc.
     */
    public static VoiceParseResult parseVoiceInput(String input) {
        if (input == null || input.isEmpty()) {
            return new VoiceParseResult(0, "");
        }

        String text = input.toLowerCase().trim();
        double amount = 0;
        String description = text;

        // Pattern 1: Numbers with k/nghìn/triệu
        Pattern pattern = Pattern.compile("(\\d+)\\s*(k|nghìn|ngàn|triệu|tr)?");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            try {
                amount = Double.parseDouble(matcher.group(1));
                String unit = matcher.group(2);

                if (unit != null) {
                    if (unit.equals("k") || unit.equals("nghìn") || unit.equals("ngàn")) {
                        amount *= 1000;
                    } else if (unit.equals("triệu") || unit.equals("tr")) {
                        amount *= 1000000;
                    }
                }

                // Remove amount from description
                description = text.replaceFirst(matcher.group(0), "").trim();
            } catch (NumberFormatException e) {
                // Keep original
            }
        }

        // Handle Vietnamese number words
        text = text.replace("một trăm", "100")
                   .replace("hai trăm", "200")
                   .replace("ba trăm", "300")
                   .replace("năm chục", "50")
                   .replace("bốn chục", "40")
                   .replace("ba chục", "30")
                   .replace("hai chục", "20")
                   .replace("mười", "10");

        return new VoiceParseResult(amount, description);
    }
}
