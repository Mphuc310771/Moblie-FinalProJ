package com.smartbudget.app.utils;

import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.Interpolator;

/**
 * Spring physics animation helper.
 * Creates natural, bouncy animations.
 */
public class SpringAnimationHelper {

    /**
     * Custom spring interpolator.
     */
    public static class SpringInterpolator implements Interpolator {
        private final float tension;
        private final float friction;

        public SpringInterpolator(float tension, float friction) {
            this.tension = tension;
            this.friction = friction;
        }

        @Override
        public float getInterpolation(float t) {
            // Spring physics formula
            return (float) (1 - Math.exp(-tension * t) * Math.cos(friction * t));
        }
    }

    /**
     * Spring scale animation.
     */
    public static void springScale(View view, float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(600);
        animator.setInterpolator(new SpringInterpolator(5f, 10f));
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setScaleX(value);
            view.setScaleY(value);
        });
        animator.start();
    }

    /**
     * Spring translate animation.
     */
    public static void springTranslateY(View view, float from, float to) {
        ValueAnimator animator = ValueAnimator.ofFloat(from, to);
        animator.setDuration(600);
        animator.setInterpolator(new SpringInterpolator(5f, 10f));
        animator.addUpdateListener(animation -> {
            view.setTranslationY((float) animation.getAnimatedValue());
        });
        animator.start();
    }

    /**
     * Bouncy pop in effect.
     */
    public static void bouncyPopIn(View view) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setVisibility(View.VISIBLE);

        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(500);
        animator.setInterpolator(new SpringInterpolator(4f, 8f));
        animator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            view.setScaleX(value);
            view.setScaleY(value);
            view.setAlpha(value);
        });
        animator.start();
    }

    /**
     * Jelly wiggle effect.
     */
    public static void jellyWiggle(View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(400);
        animator.addUpdateListener(animation -> {
            float t = (float) animation.getAnimatedValue();
            float scaleX = 1f + 0.1f * (float) Math.sin(t * Math.PI * 4) * (1 - t);
            float scaleY = 1f - 0.1f * (float) Math.sin(t * Math.PI * 4) * (1 - t);
            view.setScaleX(scaleX);
            view.setScaleY(scaleY);
        });
        animator.start();
    }

    /**
     * Rubber band stretch effect on drag.
     */
    public static float rubberBand(float offset, float maxOffset) {
        float ratio = offset / maxOffset;
        return maxOffset * (1 - (float) Math.exp(-ratio * 2));
    }

    /**
     * Reset view with spring animation.
     */
    public static void springReset(View view) {
        springTranslateY(view, view.getTranslationY(), 0);
        springScale(view, view.getScaleX(), 1f);
    }
}
