package com.smartbudget.app.utils;

import android.view.View;
import android.view.ViewGroup;

import java.util.Arrays;

import nl.dionsegijn.konfetti.core.Party;
import nl.dionsegijn.konfetti.core.PartyFactory;
import nl.dionsegijn.konfetti.core.emitter.Emitter;
import nl.dionsegijn.konfetti.core.emitter.EmitterConfig;
import nl.dionsegijn.konfetti.core.models.Shape;
import nl.dionsegijn.konfetti.core.models.Size;
import nl.dionsegijn.konfetti.xml.KonfettiView;

/**
 * Utility class for confetti celebrations.
 * Makes the app feel more rewarding and fun.
 */
public class ConfettiHelper {

    /**
     * Show standard celebration confetti.
     * Use when user achieves a savings goal.
     */
    public static void celebrate(KonfettiView konfettiView) {
        if (konfettiView == null) return;

        EmitterConfig emitterConfig = new Emitter(300L, java.util.concurrent.TimeUnit.MILLISECONDS)
                .perSecond(100);

        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(90)
                .setSpeedBetween(1f, 5f)
                .timeToLive(2000L)
                .sizes(new Size(8, 50f, 0f), new Size(12, 50f, 0f))
                .position(0.0, 0.0, 1.0, 0.0)
                .build();

        konfettiView.start(party);
    }

    /**
     * Burst confetti from a specific view.
     * Use for individual achievements like completing a goal.
     */
    public static void burst(KonfettiView konfettiView, View targetView) {
        if (konfettiView == null || targetView == null) return;

        int[] location = new int[2];
        targetView.getLocationOnScreen(location);

        float centerX = location[0] + targetView.getWidth() / 2f;
        float centerY = location[1] + targetView.getHeight() / 2f;

        EmitterConfig emitterConfig = new Emitter(100L, java.util.concurrent.TimeUnit.MILLISECONDS)
                .max(50);

        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(360)
                .setSpeedBetween(5f, 10f)
                .timeToLive(1500L)
                .sizes(new Size(8, 50f, 0f))
                .position(centerX, centerY)
                .build();

        konfettiView.start(party);
    }

    /**
     * Rain effect - subtle celebration.
     */
    public static void rain(KonfettiView konfettiView) {
        if (konfettiView == null) return;

        EmitterConfig emitterConfig = new Emitter(5L, java.util.concurrent.TimeUnit.SECONDS)
                .perSecond(30);

        Party party = new PartyFactory(emitterConfig)
                .angle(270)
                .spread(45)
                .setSpeedBetween(0.5f, 2f)
                .timeToLive(3000L)
                .sizes(new Size(6, 50f, 0f))
                .position(0.0, 0.0, 1.0, 0.0)
                .build();

        konfettiView.start(party);
    }
}
