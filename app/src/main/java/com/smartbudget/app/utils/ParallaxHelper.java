package com.smartbudget.app.utils;

import android.view.View;
import android.view.animation.Interpolator;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;

/**
 * Parallax scroll effect helper.
 * Creates depth with different scroll speeds for elements.
 */
public class ParallaxHelper {

    public interface ParallaxListener {
        void onScroll(float scrollPercent);
    }

    /**
     * Apply parallax effect to a view based on scroll.
     */
    public static void applyParallax(View targetView, int scrollY, float factor) {
        float translationY = scrollY * factor;
        targetView.setTranslationY(translationY);
    }

    /**
     * Apply parallax with scale effect.
     */
    public static void applyParallaxWithScale(View targetView, int scrollY, 
                                               float translateFactor, float scaleFactor) {
        float translationY = scrollY * translateFactor;
        float scale = 1f + (scrollY * scaleFactor / 1000f);
        
        targetView.setTranslationY(translationY);
        targetView.setScaleX(Math.max(0.8f, scale));
        targetView.setScaleY(Math.max(0.8f, scale));
    }

    /**
     * Apply fade parallax effect.
     */
    public static void applyParallaxWithFade(View targetView, int scrollY, int fadeDistance) {
        float alpha = 1f - (Math.min(scrollY, fadeDistance) / (float) fadeDistance);
        targetView.setAlpha(Math.max(0f, alpha));
    }

    /**
     * Setup parallax for NestedScrollView.
     */
    public static void setupNestedScrollParallax(
            NestedScrollView scrollView, 
            View headerView, 
            float parallaxFactor) {
        
        scrollView.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) 
                (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            applyParallax(headerView, scrollY, parallaxFactor);
        });
    }

    /**
     * Create sticky header effect.
     */
    public static void applyStickyHeader(View headerView, int scrollY, int stickyThreshold) {
        if (scrollY > stickyThreshold) {
            headerView.setTranslationY(scrollY - stickyThreshold);
        } else {
            headerView.setTranslationY(0);
        }
    }

    /**
     * Apply reveal effect based on scroll.
     */
    public static void applyScrollReveal(View targetView, int scrollY, int revealStart, int revealEnd) {
        if (scrollY < revealStart) {
            targetView.setAlpha(0f);
            targetView.setTranslationY(50f);
        } else if (scrollY > revealEnd) {
            targetView.setAlpha(1f);
            targetView.setTranslationY(0f);
        } else {
            float progress = (scrollY - revealStart) / (float) (revealEnd - revealStart);
            targetView.setAlpha(progress);
            targetView.setTranslationY(50f * (1 - progress));
        }
    }

    /**
     * Create bouncy overscroll effect.
     */
    public static float calculateOverscroll(float overscrollAmount, float maxOverscroll) {
        float ratio = Math.abs(overscrollAmount) / maxOverscroll;
        return Math.signum(overscrollAmount) * maxOverscroll * (1 - (float) Math.exp(-ratio * 2));
    }
}
