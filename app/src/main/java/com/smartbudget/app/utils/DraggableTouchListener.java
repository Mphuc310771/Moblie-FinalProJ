package com.smartbudget.app.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewParent;

public class DraggableTouchListener implements View.OnTouchListener {

    private float dX, dY;
    private float startX, startY;
    private static final int CLICK_ACTION_THRESHOLD = 10;
    private long startClickTime;

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        ViewParent parent = view.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                dX = view.getX() - event.getRawX();
                dY = view.getY() - event.getRawY();
                startX = event.getRawX();
                startY = event.getRawY();
                startClickTime = System.currentTimeMillis();
                return true; // Consume event

            case MotionEvent.ACTION_MOVE:
                float newX = event.getRawX() + dX;
                float newY = event.getRawY() + dY;
                
                // Optional: Check screen boundaries here if needed
                // For CoordinatorLayout, simple translation usually works but boundaries are better.
                // Assuming simple drag for now.
                
                view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start();
                return true;

            case MotionEvent.ACTION_UP:
                float endX = event.getRawX();
                float endY = event.getRawY();
                if (isClick(startX, endX, startY, endY)) {
                    view.performClick();
                }
                return true;

            default:
                return false;
        }
    }

    private boolean isClick(float startX, float endX, float startY, float endY) {
        return Math.abs(startX - endX) < CLICK_ACTION_THRESHOLD && 
               Math.abs(startY - endY) < CLICK_ACTION_THRESHOLD;
    }
}
