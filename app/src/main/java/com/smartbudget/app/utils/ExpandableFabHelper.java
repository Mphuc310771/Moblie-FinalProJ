package com.smartbudget.app.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * Helper class for expandable FAB menu animation.
 * Creates a beautiful expanding menu effect.
 */
public class ExpandableFabHelper {

    private final FloatingActionButton mainFab;
    private final FloatingActionButton[] subFabs;
    private boolean isExpanded = false;
    private final float[] offsets = {70f, 140f, 210f}; // Distance from main FAB

    public interface OnFabClickListener {
        void onQuickAddClick();
        void onScanClick();
        void onVoiceClick();
    }

    private OnFabClickListener listener;

    public ExpandableFabHelper(FloatingActionButton mainFab, FloatingActionButton... subFabs) {
        this.mainFab = mainFab;
        this.subFabs = subFabs;
        
        // Initially hide sub FABs
        for (FloatingActionButton fab : subFabs) {
            fab.setVisibility(View.INVISIBLE);
            fab.setAlpha(0f);
            fab.setTranslationY(0f);
        }
    }

    public void setOnFabClickListener(OnFabClickListener listener) {
        this.listener = listener;
    }

    /**
     * Toggle FAB menu expansion.
     */
    public void toggle() {
        if (isExpanded) {
            collapse();
        } else {
            expand();
        }
    }

    /**
     * Expand the FAB menu with animation.
     */
    public void expand() {
        isExpanded = true;
        
        // Rotate main FAB
        mainFab.animate()
                .rotation(45f)
                .setDuration(200)
                .start();

        // Haptic feedback
        HapticHelper.lightClick(mainFab);

        // Animate sub FABs
        AnimatorSet animatorSet = new AnimatorSet();
        
        for (int i = 0; i < subFabs.length; i++) {
            FloatingActionButton fab = subFabs[i];
            fab.setVisibility(View.VISIBLE);
            
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(fab, "alpha", 0f, 1f);
            ObjectAnimator translateAnim = ObjectAnimator.ofFloat(fab, "translationY", 0f, -offsets[i]);
            ObjectAnimator scaleXAnim = ObjectAnimator.ofFloat(fab, "scaleX", 0.5f, 1f);
            ObjectAnimator scaleYAnim = ObjectAnimator.ofFloat(fab, "scaleY", 0.5f, 1f);

            AnimatorSet fabSet = new AnimatorSet();
            fabSet.playTogether(alphaAnim, translateAnim, scaleXAnim, scaleYAnim);
            fabSet.setDuration(200);
            fabSet.setStartDelay(i * 50L);
            fabSet.setInterpolator(new OvershootInterpolator(1.5f));
            
            fabSet.start();
        }
    }

    /**
     * Collapse the FAB menu with animation.
     */
    public void collapse() {
        isExpanded = false;
        
        // Rotate main FAB back
        mainFab.animate()
                .rotation(0f)
                .setDuration(200)
                .start();

        // Animate sub FABs
        for (int i = subFabs.length - 1; i >= 0; i--) {
            FloatingActionButton fab = subFabs[i];
            int delay = (subFabs.length - 1 - i) * 30;
            
            fab.animate()
                    .alpha(0f)
                    .translationY(0f)
                    .scaleX(0.5f)
                    .scaleY(0.5f)
                    .setDuration(150)
                    .setStartDelay(delay)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            fab.setVisibility(View.INVISIBLE);
                        }
                    })
                    .start();
        }
    }

    public boolean isExpanded() {
        return isExpanded;
    }
}
