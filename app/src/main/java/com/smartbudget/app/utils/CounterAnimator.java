package com.smartbudget.app.utils;

import android.animation.ValueAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Animated number counter for impressive UI.
 * Makes amounts animate from 0 to final value.
 */
public class CounterAnimator {

    private static final long DEFAULT_DURATION = 1500L;
    private static final DecimalFormat VND_FORMAT;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
        symbols.setGroupingSeparator('.');
        VND_FORMAT = new DecimalFormat("#,###", symbols);
    }

    /**
     * Animate a number from 0 to target with VND format.
     */
    public static void animateVND(TextView textView, double targetValue) {
        animateVND(textView, 0, targetValue, DEFAULT_DURATION);
    }

    /**
     * Animate a number from start to target with VND format.
     */
    public static void animateVND(TextView textView, double startValue, double targetValue, long duration) {
        ValueAnimator animator = ValueAnimator.ofFloat((float) startValue, (float) targetValue);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            textView.setText(VND_FORMAT.format(value) + " ₫");
        });

        animator.start();
    }

    /**
     * Animate a number with custom prefix/suffix.
     */
    public static void animateNumber(TextView textView, int startValue, int targetValue, 
                                     String prefix, String suffix, long duration) {
        ValueAnimator animator = ValueAnimator.ofInt(startValue, targetValue);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(prefix + value + suffix);
        });

        animator.start();
    }

    /**
     * Animate percentage with easing.
     */
    public static void animatePercent(TextView textView, int targetPercent) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetPercent);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(value + "%");
        });

        animator.start();
    }

    /**
     * Animate with bounce effect at end.
     */
    public static void animateWithBounce(TextView textView, double targetValue) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, (float) targetValue);
        animator.setDuration(DEFAULT_DURATION);
        animator.setInterpolator(new android.view.animation.OvershootInterpolator(1.2f));

        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            textView.setText(VND_FORMAT.format(value) + " ₫");
        });

        animator.start();
    }
}
