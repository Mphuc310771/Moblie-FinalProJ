package com.smartbudget.app.presentation;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.smartbudget.app.databinding.ActivitySplashBinding;

/**
 * Animated Splash Screen.
 * Shows app branding with smooth animations before navigating to MainActivity.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DURATION = 2000L;
    private ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Start animations
        animateLogo();
        animateText();

        // Navigate to MainActivity after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, SPLASH_DURATION);
    }

    private void animateLogo() {
        // Prepare initial state
        binding.ivLogo.setScaleX(0f);
        binding.ivLogo.setScaleY(0f);
        binding.ivLogo.setAlpha(0f);
        
        // Glow initial state
        binding.ivGlow.setScaleX(0.5f);
        binding.ivGlow.setScaleY(0.5f);
        binding.ivGlow.setAlpha(0f);

        // Animate Logo
        binding.ivLogo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
                
        // Animate Glow (Delayed pulse)
        binding.ivGlow.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .alpha(0.6f)
                .setDuration(1000)
                .setStartDelay(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                     // Continuous pulse
                     binding.ivGlow.animate()
                        .scaleX(1.1f)
                        .scaleY(1.1f)
                        .alpha(0.4f)
                        .setDuration(800)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .withEndAction(() -> {
                             binding.ivGlow.animate()
                                .scaleX(1.25f)
                                .scaleY(1.25f)
                                .alpha(0.6f)
                                .setDuration(800)
                                .start();
                        })
                        .start();
                })
                .start();

        // Subtle bounce loop for Logo
        binding.ivLogo.postDelayed(() -> {
            binding.ivLogo.animate()
                    .scaleX(1.05f)
                    .scaleY(1.05f)
                    .setDuration(500)
                    .withEndAction(() -> binding.ivLogo.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(500)
                            .start())
                    .start();
        }, 800);
    }

    private void animateText() {
        // App name - slide up
        binding.tvAppName.setTranslationY(50);
        binding.tvAppName.setAlpha(0f);
        binding.tvAppName.animate()
                .translationY(0)
                .alpha(1f)
                .setDuration(600)
                .setStartDelay(300)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // Tagline - fade in
        binding.tvTagline.setAlpha(0f);
        binding.tvTagline.animate()
                .alpha(0.8f)
                .setDuration(600)
                .setStartDelay(500)
                .start();

        // Progress bar - fade in
        binding.progressBar.setAlpha(0f);
        binding.progressBar.animate()
                .alpha(1f)
                .setDuration(400)
                .setStartDelay(700)
                .start();
    }
}
