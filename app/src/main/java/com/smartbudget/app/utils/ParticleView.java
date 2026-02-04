package com.smartbudget.app.utils;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Floating particles background effect.
 * Creates beautiful animated money symbols floating upward.
 */
public class ParticleView extends View {

    private static final int PARTICLE_COUNT = 15;
    private static final String[] EMOJIS = {"💰", "💵", "💎", "✨", "⭐", "🪙"};

    private List<Particle> particles = new ArrayList<>();
    private Paint textPaint;
    private ValueAnimator animator;
    private Random random = new Random();

    private static class Particle {
        float x, y;
        float speed;
        float alpha;
        float size;
        String emoji;
        float swayOffset;
        float swaySpeed;
    }

    public ParticleView(Context context) {
        super(context);
        init();
    }

    public ParticleView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ParticleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initParticles();
        startAnimation();
    }

    private void initParticles() {
        particles.clear();
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(createParticle(true));
        }
    }

    private Particle createParticle(boolean randomY) {
        Particle p = new Particle();
        p.x = random.nextFloat() * getWidth();
        p.y = randomY ? random.nextFloat() * getHeight() : getHeight() + 50;
        p.speed = 0.5f + random.nextFloat() * 1.5f;
        p.alpha = 0.3f + random.nextFloat() * 0.4f;
        p.size = 16 + random.nextInt(20);
        p.emoji = EMOJIS[random.nextInt(EMOJIS.length)];
        p.swayOffset = random.nextFloat() * 360;
        p.swaySpeed = 1 + random.nextFloat() * 2;
        return p;
    }

    private void startAnimation() {
        if (animator != null && animator.isRunning()) {
            animator.cancel();
        }

        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(16); // ~60fps
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            updateParticles();
            invalidate();
        });
        animator.start();
    }

    private void updateParticles() {
        for (int i = 0; i < particles.size(); i++) {
            Particle p = particles.get(i);
            p.y -= p.speed;
            p.swayOffset += p.swaySpeed;
            
            // Sway left/right
            float sway = (float) Math.sin(Math.toRadians(p.swayOffset)) * 2;
            p.x += sway;

            // Reset if off screen
            if (p.y < -50) {
                particles.set(i, createParticle(false));
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (Particle p : particles) {
            textPaint.setTextSize(p.size);
            textPaint.setAlpha((int) (p.alpha * 255));
            canvas.drawText(p.emoji, p.x, p.y, textPaint);
        }
    }

    public void stopAnimation() {
        if (animator != null) {
            animator.cancel();
        }
    }

    public void resumeAnimation() {
        if (animator != null && !animator.isRunning()) {
            animator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}
