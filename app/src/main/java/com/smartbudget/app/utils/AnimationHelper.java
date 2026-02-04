package com.smartbudget.app.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.recyclerview.widget.RecyclerView;

/**
 * Utility class for animations.
 * Provides smooth, modern animations for UI elements.
 */
public class AnimationHelper {

    private static final long ANIMATION_DURATION = 300L;
    private static final long STAGGER_DELAY = 50L;

    /**
     * Animate view entrance with scale and fade.
     */
    public static void popIn(View view) {
        view.setScaleX(0.8f);
        view.setScaleY(0.8f);
        view.setAlpha(0f);

        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    /**
     * Animate view with bounce effect.
     */
    public static void bounce(View view) {
        view.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .setInterpolator(new OvershootInterpolator())
                        .start())
                .start();
    }

    /**
     * Slide in from right animation.
     */
    public static void slideInRight(View view, int position) {
        view.setTranslationX(view.getWidth());
        view.setAlpha(0f);

        view.animate()
                .translationX(0)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(position * STAGGER_DELAY)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Fade in with slide up animation.
     */
    public static void fadeInUp(View view, long delay) {
        view.setTranslationY(30);
        view.setAlpha(0f);

        view.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(delay)
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    /**
     * Shake animation for errors.
     */
    public static void shake(View view) {
        ObjectAnimator shakeAnimator = ObjectAnimator.ofFloat(
                view, "translationX", 0, 20, -20, 15, -15, 10, -10, 5, -5, 0);
        shakeAnimator.setDuration(500);
        shakeAnimator.start();
    }

    /**
     * Pulse animation for drawing attention.
     */
    public static void pulse(View view) {
        view.animate()
                .scaleX(1.05f)
                .scaleY(1.05f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    /**
     * Animate RecyclerView items with staggered effect.
     * Call in onBindViewHolder for list item animations.
     */
    public static void animateListItem(RecyclerView.ViewHolder holder, int position) {
        View itemView = holder.itemView;
        itemView.setAlpha(0f);
        itemView.setTranslationY(50);

        itemView.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(position * STAGGER_DELAY)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * Crossfade between two views.
     */
    public static void crossfade(View viewToShow, View viewToHide) {
        viewToShow.setAlpha(0f);
        viewToShow.setVisibility(View.VISIBLE);

        viewToShow.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .start();

        viewToHide.animate()
                .alpha(0f)
                .setDuration(ANIMATION_DURATION)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        viewToHide.setVisibility(View.GONE);
                    }
                })
                .start();
    }
}
