package com.smartbudget.app.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.view.View;

/**
 * 3D Card flip animation helper.
 * Creates stunning card flip effects.
 */
public class CardFlipHelper {

    private static final int FLIP_DURATION = 300;

    public interface OnFlipListener {
        void onFlipStart();
        void onFlipMidpoint();
        void onFlipEnd();
    }

    /**
     * Flip card horizontally (Y-axis).
     */
    public static void flipHorizontal(View frontView, View backView, OnFlipListener listener) {
        // Set camera distance for 3D effect
        float distance = 8000f * frontView.getContext().getResources().getDisplayMetrics().density;
        frontView.setCameraDistance(distance);
        backView.setCameraDistance(distance);

        // Initially hide back
        backView.setAlpha(0f);
        backView.setRotationY(-90f);

        // Animate front to 90 degrees
        ObjectAnimator flipOut = ObjectAnimator.ofFloat(frontView, "rotationY", 0f, 90f);
        flipOut.setDuration(FLIP_DURATION);
        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (listener != null) listener.onFlipStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                frontView.setAlpha(0f);
                backView.setAlpha(1f);
                if (listener != null) listener.onFlipMidpoint();

                // Animate back from -90 to 0
                ObjectAnimator flipIn = ObjectAnimator.ofFloat(backView, "rotationY", -90f, 0f);
                flipIn.setDuration(FLIP_DURATION);
                flipIn.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        if (listener != null) listener.onFlipEnd();
                    }
                });
                flipIn.start();
            }
        });
        flipOut.start();
    }

    /**
     * Flip card vertically (X-axis).
     */
    public static void flipVertical(View frontView, View backView, OnFlipListener listener) {
        float distance = 8000f * frontView.getContext().getResources().getDisplayMetrics().density;
        frontView.setCameraDistance(distance);
        backView.setCameraDistance(distance);

        backView.setAlpha(0f);
        backView.setRotationX(90f);

        ObjectAnimator flipOut = ObjectAnimator.ofFloat(frontView, "rotationX", 0f, -90f);
        flipOut.setDuration(FLIP_DURATION);
        flipOut.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                frontView.setAlpha(0f);
                backView.setAlpha(1f);

                ObjectAnimator flipIn = ObjectAnimator.ofFloat(backView, "rotationX", 90f, 0f);
                flipIn.setDuration(FLIP_DURATION);
                flipIn.start();
            }
        });
        flipOut.start();
    }

    /**
     * Quick 360 spin effect.
     */
    public static void spin360(View view) {
        view.animate()
                .rotationBy(360f)
                .setDuration(500)
                .start();
    }

    /**
     * Tilt card on touch.
     */
    public static void tilt(View view, float x, float y) {
        float centerX = view.getWidth() / 2f;
        float centerY = view.getHeight() / 2f;

        float rotateX = (y - centerY) / centerY * 10f;
        float rotateY = (centerX - x) / centerX * 10f;

        view.setRotationX(rotateX);
        view.setRotationY(rotateY);
    }

    /**
     * Reset card tilt.
     */
    public static void resetTilt(View view) {
        view.animate()
                .rotationX(0f)
                .rotationY(0f)
                .setDuration(200)
                .start();
    }
}
