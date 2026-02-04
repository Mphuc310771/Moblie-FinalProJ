package com.smartbudget.app.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

/**
 * Animated wave view.
 * Creates beautiful flowing wave animation for backgrounds.
 */
public class WaveView extends View {

    private Paint wavePaint1;
    private Paint wavePaint2;
    private Paint wavePaint3;
    private Path wavePath;
    
    private float phase1 = 0;
    private float phase2 = 0;
    private float phase3 = 0;
    
    private float waveHeight = 80f;
    private int waveColor = Color.parseColor("#4CAF50");
    private ValueAnimator animator;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WaveView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        wavePaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint1.setStyle(Paint.Style.FILL);
        wavePaint1.setColor(adjustAlpha(waveColor, 0.3f));

        wavePaint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint2.setStyle(Paint.Style.FILL);
        wavePaint2.setColor(adjustAlpha(waveColor, 0.5f));

        wavePaint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint3.setStyle(Paint.Style.FILL);
        wavePaint3.setColor(adjustAlpha(waveColor, 0.7f));

        wavePath = new Path();
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        startAnimation();
    }

    private void startAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0, (float) (2 * Math.PI));
        animator.setDuration(3000);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            phase1 = (float) animation.getAnimatedValue();
            phase2 = phase1 + 0.5f;
            phase3 = phase1 + 1f;
            invalidate();
        });
        animator.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        int width = getWidth();
        int height = getHeight();
        int baseY = height - (int) (waveHeight * 2);

        // Draw three waves with different phases
        drawWave(canvas, wavePaint1, phase1, baseY, width, height, 1.0f);
        drawWave(canvas, wavePaint2, phase2, baseY + 20, width, height, 0.8f);
        drawWave(canvas, wavePaint3, phase3, baseY + 40, width, height, 0.6f);
    }

    private void drawWave(Canvas canvas, Paint paint, float phase, int baseY, 
                          int width, int height, float amplitude) {
        wavePath.reset();
        wavePath.moveTo(0, height);

        for (int x = 0; x <= width; x += 10) {
            float y = baseY + (float) (waveHeight * amplitude * 
                    Math.sin((x / (float) width * 2 * Math.PI) + phase));
            wavePath.lineTo(x, y);
        }

        wavePath.lineTo(width, height);
        wavePath.close();
        canvas.drawPath(wavePath, paint);
    }

    public void setWaveColor(int color) {
        this.waveColor = color;
        wavePaint1.setColor(adjustAlpha(color, 0.3f));
        wavePaint2.setColor(adjustAlpha(color, 0.5f));
        wavePaint3.setColor(adjustAlpha(color, 0.7f));
        invalidate();
    }

    public void setWaveHeight(float height) {
        this.waveHeight = height;
        invalidate();
    }

    public void stopAnimation() {
        if (animator != null) {
            animator.cancel();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
