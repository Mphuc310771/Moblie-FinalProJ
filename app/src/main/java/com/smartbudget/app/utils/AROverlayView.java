package com.smartbudget.app.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

/**
 * AR Overlay View.
 * Simulates AR elements for camera overlay features.
 */
public class AROverlayView extends View {

    private Paint paint;
    private Paint textPaint;
    private Paint boxPaint;
    
    private float[] detectedObjects = {0.3f, 0.4f, 0.7f, 0.6f}; // Normalized coordinates x1, y1, x2, y2

    public AROverlayView(Context context) {
        super(context);
        init();
    }

    public AROverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AROverlayView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4f);
        paint.setColor(Color.parseColor("#00FF00"));

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40f);
        textPaint.setShadowLayer(2f, 1f, 1f, Color.BLACK);

        boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaint.setStyle(Paint.Style.FILL);
        boxPaint.setColor(Color.parseColor("#80000000"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Simulate scanning animation
        drawScanLine(canvas);

        // Draw simulated AR tag for a product
        int w = getWidth();
        int h = getHeight();

        // Product Box
        float left = w * 0.3f;
        float top = h * 0.4f;
        float right = w * 0.7f;
        float bottom = h * 0.6f;

        paint.setColor(Color.CYAN);
        canvas.drawRect(left, top, right, bottom, paint);

        // Info Bubble
        float bubbleX = right + 20;
        float bubbleY = top;
        
        boxPaint.setColor(Color.parseColor("#CC000000"));
        RectF bubbleRect = new RectF(bubbleX, bubbleY, bubbleX + 300, bubbleY + 120);
        canvas.drawRoundRect(bubbleRect, 16, 16, boxPaint);

        canvas.drawText("MacBook Pro", bubbleX + 20, bubbleY + 40, textPaint);
        
        textPaint.setColor(Color.YELLOW);
        canvas.drawText("Price: 45.0M ₫", bubbleX + 20, bubbleY + 85, textPaint);
        textPaint.setColor(Color.WHITE);

        // Corner indicators
        drawCorner(canvas, left, top, 0);
        drawCorner(canvas, right, top, 90);
        drawCorner(canvas, right, bottom, 180);
        drawCorner(canvas, left, bottom, 270);
    }

    private float scanY = 0;
    private boolean scanningDown = true;

    private void drawScanLine(Canvas canvas) {
        if (scanningDown) {
            scanY += 10;
            if (scanY > getHeight()) scanningDown = false;
        } else {
            scanY -= 10;
            if (scanY < 0) scanningDown = true;
        }

        paint.setColor(Color.parseColor("#4000FF00"));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, scanY - 20, getWidth(), scanY + 20, paint);
        
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(0, scanY, getWidth(), scanY, paint);

        invalidate(); // Continuous animation
    }

    private void drawCorner(Canvas canvas, float x, float y, int angle) {
        canvas.save();
        canvas.rotate(angle, x, y);
        paint.setColor(Color.WHITE);
        canvas.drawLine(x, y, x + 50, y, paint);
        canvas.drawLine(x, y, x, y + 50, paint);
        canvas.restore();
    }
}
