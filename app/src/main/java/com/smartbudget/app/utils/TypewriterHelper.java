package com.smartbudget.app.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

/**
 * Typewriter animation helper.
 * Creates retro typewriter text effect.
 */
public class TypewriterHelper {

    private static final int DEFAULT_DELAY = 50; // ms per character

    public interface OnCompleteListener {
        void onComplete();
    }

    /**
     * Animate text with typewriter effect.
     */
    public static void type(TextView textView, String text, OnCompleteListener listener) {
        type(textView, text, DEFAULT_DELAY, listener);
    }

    /**
     * Animate text with custom delay.
     */
    public static void type(TextView textView, String text, int delayMs, OnCompleteListener listener) {
        textView.setText("");
        Handler handler = new Handler(Looper.getMainLooper());

        for (int i = 0; i <= text.length(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                textView.setText(text.substring(0, index));
                
                // Cursor blink effect
                if (index < text.length()) {
                    textView.append("▌");
                } else {
                    // Remove cursor at end
                    handler.postDelayed(() -> {
                        textView.setText(text);
                        if (listener != null) listener.onComplete();
                    }, 500);
                }
            }, (long) i * delayMs);
        }
    }

    /**
     * Type with sound-like haptic feedback.
     */
    public static void typeWithHaptic(TextView textView, String text, OnCompleteListener listener) {
        textView.setText("");
        Handler handler = new Handler(Looper.getMainLooper());

        for (int i = 0; i <= text.length(); i++) {
            final int index = i;
            handler.postDelayed(() -> {
                textView.setText(text.substring(0, index));
                if (index < text.length()) {
                    textView.append("▌");
                    HapticHelper.lightClick(textView);
                } else {
                    handler.postDelayed(() -> {
                        textView.setText(text);
                        if (listener != null) listener.onComplete();
                    }, 500);
                }
            }, (long) i * DEFAULT_DELAY);
        }
    }

    /**
     * Delete text with backspace effect.
     */
    public static void backspace(TextView textView, OnCompleteListener listener) {
        String text = textView.getText().toString();
        Handler handler = new Handler(Looper.getMainLooper());

        for (int i = text.length(); i >= 0; i--) {
            final int index = i;
            handler.postDelayed(() -> {
                if (index > 0) {
                    textView.setText(text.substring(0, index - 1) + "▌");
                } else {
                    textView.setText("");
                    if (listener != null) listener.onComplete();
                }
            }, (long) (text.length() - index) * 30);
        }
    }

    /**
     * Type multiple lines sequentially.
     */
    public static void typeLines(TextView textView, String[] lines, OnCompleteListener listener) {
        Handler handler = new Handler(Looper.getMainLooper());
        StringBuilder fullText = new StringBuilder();

        int totalDelay = 0;
        for (String line : lines) {
            for (int i = 0; i <= line.length(); i++) {
                final String currentText = fullText.toString() + line.substring(0, i);
                handler.postDelayed(() -> textView.setText(currentText + "▌"), totalDelay);
                totalDelay += DEFAULT_DELAY;
            }
            fullText.append(line).append("\n");
            totalDelay += 200; // Pause between lines
        }

        handler.postDelayed(() -> {
            textView.setText(fullText.toString().trim());
            if (listener != null) listener.onComplete();
        }, totalDelay);
    }
}
