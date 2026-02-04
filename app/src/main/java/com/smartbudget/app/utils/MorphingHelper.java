package com.smartbudget.app.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Path;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * Morphing animation helper.
 * Creates smooth shape-shifting animations.
 */
public class MorphingHelper {

    /**
     * Morph view from circle to rectangle.
     */
    public static void morphCircleToRect(View view, int targetWidth, int targetHeight, long duration) {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 
                targetWidth / (float) view.getWidth());
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 
                targetHeight / (float) view.getHeight());

        // Animate corner radius if possible
        ValueAnimator cornerAnimator = ValueAnimator.ofFloat(500f, 20f);
        cornerAnimator.addUpdateListener(animation -> {
            float radius = (float) animation.getAnimatedValue();
            // Apply corner radius through drawable state
            view.setTag(radius);
        });

        animatorSet.playTogether(scaleX, scaleY, cornerAnimator);
        animatorSet.setDuration(duration);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.start();
    }

    /**
     * Morph view along a path.
     */
    public static void morphAlongPath(View view, float startX, float startY, 
                                      float endX, float endY, long duration) {
        Path path = new Path();
        path.moveTo(startX, startY);
        
        // Create curved path
        float controlX = (startX + endX) / 2;
        float controlY = Math.min(startY, endY) - 100f;
        path.quadTo(controlX, controlY, endX, endY);

        ObjectAnimator pathAnimator = ObjectAnimator.ofFloat(view, "x", "y", path);
        pathAnimator.setDuration(duration);
        pathAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        pathAnimator.start();
    }

    /**
     * Breathing/pulsing morph effect.
     */
    public static void breathingEffect(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.1f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.1f, 1f);
        
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(2000);
        animatorSet.start();
    }

    /**
     * Liquid morph effect.
     */
    public static void liquidMorph(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 0.9f, 1.05f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.85f, 1.1f, 0.95f, 1f);
        
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(500);
        animatorSet.start();
    }

    /**
     * Expand from point animation.
     */
    public static void expandFromPoint(View view, float pivotX, float pivotY) {
        view.setPivotX(pivotX);
        view.setPivotY(pivotY);
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setVisibility(View.VISIBLE);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new android.view.animation.OvershootInterpolator());
        animatorSet.start();
    }

    /**
     * Collapse to point animation.
     */
    public static void collapseToPoint(View view, float pivotX, float pivotY) {
        view.setPivotX(pivotX);
        view.setPivotY(pivotY);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(250);
        animatorSet.start();

        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                view.setVisibility(View.GONE);
            }
        });
    }
}
