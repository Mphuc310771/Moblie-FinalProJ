package com.smartbudget.app.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

/**
 * Holographic card effect view.
 * Creates stunning rainbow shimmer effect based on touch/tilt.
 */
public class HolographicCardView extends CardView {

    private Paint holoPaint;
    private float touchX = 0.5f;
    private float touchY = 0.5f;
    private float animPhase = 0f;
    private ValueAnimator shimmerAnimator;

    private int[] holoColors = {
            Color.parseColor("#FF0000"),
            Color.parseColor("#FF7F00"),
            Color.parseColor("#FFFF00"),
            Color.parseColor("#00FF00"),
            Color.parseColor("#0000FF"),
            Color.parseColor("#4B0082"),
            Color.parseColor("#9400D3"),
            Color.parseColor("#FF0000")
    };

    public HolographicCardView(Context context) {
        super(context);
        init();
    }

    public HolographicCardView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HolographicCardView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        holoPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        holoPaint.setAlpha(60);
        setWillNotDraw(false);
        startShimmerAnimation();
    }

    private void startShimmerAnimation() {
        shimmerAnimator = ValueAnimator.ofFloat(0f, 1f);
        shimmerAnimator.setDuration(3000);
        shimmerAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shimmerAnimator.setInterpolator(new LinearInterpolator());
        shimmerAnimator.addUpdateListener(animation -> {
            animPhase = (float) animation.getAnimatedValue();
            invalidate();
        });
        shimmerAnimator.start();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = event.getX() / getWidth();
        touchY = event.getY() / getHeight();
        invalidate();
        return super.onTouchEvent(event);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        // Create dynamic gradient based on touch position
        float offset = animPhase * getWidth() * 2;
        float startX = -getWidth() + offset + (touchX - 0.5f) * getWidth();
        float startY = (touchY - 0.5f) * getHeight();

        LinearGradient gradient = new LinearGradient(
                startX, startY,
                startX + getWidth() * 2, startY + getHeight(),
                holoColors,
                null,
                Shader.TileMode.REPEAT
        );

        holoPaint.setShader(gradient);
        canvas.drawRoundRect(0, 0, getWidth(), getHeight(), getRadius(), getRadius(), holoPaint);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (shimmerAnimator != null) {
            shimmerAnimator.cancel();
        }
    }

    /**
     * Set custom holographic colors.
     */
    public void setHoloColors(int[] colors) {
        this.holoColors = colors;
        invalidate();
    }

    /**
     * Set holographic intensity (alpha 0-255).
     */
    public void setIntensity(int alpha) {
        holoPaint.setAlpha(alpha);
        invalidate();
    }
}
