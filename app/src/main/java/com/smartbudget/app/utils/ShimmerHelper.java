package com.smartbudget.app.utils;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Shimmer effect for skeleton loading.
 * Creates an animated shine effect on loading placeholders.
 */
public class ShimmerHelper {

    private static final int SHIMMER_DURATION = 1200;

    /**
     * Start shimmer animation on a view.
     */
    public static ValueAnimator startShimmer(View view) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(SHIMMER_DURATION);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        animator.setInterpolator(new LinearInterpolator());

        animator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            view.setAlpha(0.5f + (0.5f * (float) Math.sin(progress * Math.PI * 2)));
        });

        animator.start();
        return animator;
    }

    /**
     * Stop shimmer animation.
     */
    public static void stopShimmer(ValueAnimator animator) {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }
    }

    /**
     * Shimmer Drawable for more advanced shimmer effect.
     */
    public static class ShimmerDrawable extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float shimmerPosition = 0f;
        private final int baseColor;
        private final int highlightColor;

        public ShimmerDrawable(int baseColor, int highlightColor) {
            this.baseColor = baseColor;
            this.highlightColor = highlightColor;
        }

        public void setShimmerPosition(float position) {
            this.shimmerPosition = position;
            invalidateSelf();
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            Rect bounds = getBounds();
            float shimmerWidth = bounds.width() * 0.3f;
            float shimmerX = bounds.left + (bounds.width() + shimmerWidth) * shimmerPosition - shimmerWidth;

            LinearGradient gradient = new LinearGradient(
                    shimmerX, 0, shimmerX + shimmerWidth, 0,
                    new int[]{baseColor, highlightColor, baseColor},
                    new float[]{0f, 0.5f, 1f},
                    Shader.TileMode.CLAMP
            );

            paint.setShader(gradient);
            canvas.drawRect(bounds, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            paint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
