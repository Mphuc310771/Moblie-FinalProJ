package com.smartbudget.app.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

/**
 * Custom circular progress view with animation.
 * Used for displaying budget progress, savings goals, etc.
 */
public class CircularProgressView extends View {

    private Paint backgroundPaint;
    private Paint progressPaint;
    private Paint textPaint;
    private Paint labelPaint;

    private RectF rectF;
    private float progress = 0f;
    private float maxProgress = 100f;
    private float strokeWidth = 20f;
    private int progressColor = Color.parseColor("#4CAF50");
    private int backgroundColor = Color.parseColor("#E0E0E0");
    private String centerText = "";
    private String labelText = "";

    public CircularProgressView(Context context) {
        super(context);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircularProgressView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundPaint.setStyle(Paint.Style.STROKE);
        backgroundPaint.setStrokeCap(Paint.Cap.ROUND);

        progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.parseColor("#212121"));

        labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setColor(Color.parseColor("#757575"));

        rectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float radius = (size - strokeWidth) / 2f;

        // Update rect
        rectF.set(
                centerX - radius,
                centerY - radius,
                centerX + radius,
                centerY + radius
        );

        // Update paints
        backgroundPaint.setStrokeWidth(strokeWidth);
        backgroundPaint.setColor(backgroundColor);
        progressPaint.setStrokeWidth(strokeWidth);
        progressPaint.setColor(progressColor);

        // Draw background circle
        canvas.drawArc(rectF, 0, 360, false, backgroundPaint);

        // Draw progress arc
        float sweepAngle = (progress / maxProgress) * 360f;
        canvas.drawArc(rectF, -90, sweepAngle, false, progressPaint);

        // Draw center text
        if (!centerText.isEmpty()) {
            textPaint.setTextSize(size * 0.2f);
            canvas.drawText(centerText, centerX, centerY + (size * 0.08f), textPaint);
        }

        // Draw label
        if (!labelText.isEmpty()) {
            labelPaint.setTextSize(size * 0.1f);
            canvas.drawText(labelText, centerX, centerY + (size * 0.2f), labelPaint);
        }
    }

    /**
     * Set progress with animation.
     */
    public void setProgressAnimated(float targetProgress) {
        ValueAnimator animator = ValueAnimator.ofFloat(progress, targetProgress);
        animator.setDuration(1000);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.start();
    }

    public void setProgress(float progress) {
        this.progress = progress;
        invalidate();
    }

    public void setMaxProgress(float maxProgress) {
        this.maxProgress = maxProgress;
        invalidate();
    }

    public void setProgressColor(int color) {
        this.progressColor = color;
        invalidate();
    }

    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        invalidate();
    }

    public void setStrokeWidth(float width) {
        this.strokeWidth = width;
        invalidate();
    }

    public void setCenterText(String text) {
        this.centerText = text;
        invalidate();
    }

    public void setLabelText(String text) {
        this.labelText = text;
        invalidate();
    }

    /**
     * Convenience method to set percentage display.
     */
    public void setPercentage(int percent) {
        this.progress = percent;
        this.maxProgress = 100;
        this.centerText = percent + "%";
        invalidate();
    }
}
