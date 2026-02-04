package com.smartbudget.app.utils;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.HapticFeedbackConstants;
import android.view.View;

/**
 * Utility class for haptic feedback.
 * Provides subtle vibrations for button presses and actions.
 */
public class HapticHelper {

    private static Vibrator vibrator;

    /**
     * Light haptic feedback for button clicks.
     */
    public static void lightClick(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
    }

    /**
     * Medium haptic feedback for confirmations.
     */
    public static void confirm(Context context) {
        vibrate(context, 50);
    }

    /**
     * Strong haptic feedback for success/completion.
     */
    public static void success(Context context) {
        vibrate(context, 100);
    }

    /**
     * Error haptic - double vibration.
     */
    public static void error(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long[] pattern = {0, 50, 100, 50};
            getVibrator(context).vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrate(context, 100);
        }
    }

    /**
     * Celebration haptic for achievements.
     */
    public static void celebrate(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            long[] pattern = {0, 30, 50, 30, 50, 100};
            getVibrator(context).vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrate(context, 200);
        }
    }

    private static void vibrate(Context context, long milliseconds) {
        Vibrator v = getVibrator(context);
        if (v == null || !v.hasVibrator()) return;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            v.vibrate(milliseconds);
        }
    }

    private static Vibrator getVibrator(Context context) {
        if (vibrator == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                VibratorManager vm = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                vibrator = vm != null ? vm.getDefaultVibrator() : null;
            } else {
                vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            }
        }
        return vibrator;
    }
}
