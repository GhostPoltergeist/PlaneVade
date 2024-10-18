package com.game.planevadegame.animationFunction;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class zoomIn {
    public static void applyZoomAnimation(View view) {
        // Create a new ScaleAnimation to zoom in
        ScaleAnimation zoomInAnimation = new ScaleAnimation(
                1.15f, 1.0f, // From X and Y scale
                1.15f, 1.0f, // To X and Y scale
                Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point X coordinate (center)
                Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point Y coordinate (center)

        // Set duration and fill after properties
        zoomInAnimation.setDuration(300);
        zoomInAnimation.setFillAfter(false); // Set to true if you want to keep the scaled size

        // Start the animation
        view.startAnimation(zoomInAnimation);
    }
}
