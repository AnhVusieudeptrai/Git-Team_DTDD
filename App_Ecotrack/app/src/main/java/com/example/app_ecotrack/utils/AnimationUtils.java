package com.example.app_ecotrack.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;

import com.example.app_ecotrack.R;

/**
 * AnimationUtils - Utility class for UI animations
 */
public class AnimationUtils {

    /**
     * Animate activity completion with scale and fade
     */
    public static void animateActivityCompletion(View view, Runnable onComplete) {
        // Scale up and fade
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.2f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1f, 0.7f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(400);
        animatorSet.setInterpolator(new OvershootInterpolator());
        
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onComplete != null) {
                    onComplete.run();
                }
            }
        });
        
        animatorSet.start();
    }

    /**
     * Animate points earned with bounce effect
     */
    public static void animatePointsEarned(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.3f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.3f, 1f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, alpha);
        animatorSet.setDuration(500);
        animatorSet.setInterpolator(new BounceInterpolator());
        animatorSet.start();
    }

    /**
     * Animate badge earned with rotation and scale
     */
    public static void animateBadgeEarned(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0f, 1.2f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0f, 1.2f, 1f);
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, rotation, alpha);
        animatorSet.setDuration(600);
        animatorSet.setInterpolator(new OvershootInterpolator());
        animatorSet.start();
    }

    /**
     * Animate streak fire icon with pulsing effect
     */
    public static void animateStreakFire(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 1.15f, 1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY);
        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new AccelerateDecelerateInterpolator());
        animatorSet.setStartDelay(0);
        
        // Repeat infinitely
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        
        animatorSet.start();
    }

    /**
     * Animate view fade in
     */
    public static void fadeIn(View view, long duration) {
        view.setAlpha(0f);
        view.setVisibility(View.VISIBLE);
        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * Animate view fade out
     */
    public static void fadeOut(View view, long duration, Runnable onComplete) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }

    /**
     * Animate view slide up
     */
    public static void slideUp(View view, long duration) {
        view.setTranslationY(view.getHeight());
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * Animate view slide down
     */
    public static void slideDown(View view, long duration, Runnable onComplete) {
        view.animate()
                .translationY(view.getHeight())
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    view.setVisibility(View.GONE);
                    view.setTranslationY(0);
                    if (onComplete != null) {
                        onComplete.run();
                    }
                })
                .start();
    }

    /**
     * Animate card press effect
     */
    public static void animateCardPress(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    /**
     * Animate progress bar update
     */
    public static void animateProgressBar(View progressBar, int fromProgress, int toProgress, long duration) {
        if (progressBar instanceof android.widget.ProgressBar) {
            android.widget.ProgressBar pb = (android.widget.ProgressBar) progressBar;
            ObjectAnimator animator = ObjectAnimator.ofInt(pb, "progress", fromProgress, toProgress);
            animator.setDuration(duration);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
        }
    }

    /**
     * Animate counter text
     */
    public static void animateCounter(android.widget.TextView textView, int from, int to, long duration, String suffix) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(value + (suffix != null ? suffix : ""));
        });
        animator.start();
    }

    /**
     * Apply bounce animation from XML
     */
    public static void applyBounceAnimation(Context context, View view) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.bounce);
        view.startAnimation(animation);
    }

    /**
     * Apply pulse animation from XML
     */
    public static void applyPulseAnimation(Context context, View view) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.pulse);
        view.startAnimation(animation);
    }

    /**
     * Apply scale up animation from XML
     */
    public static void applyScaleUpAnimation(Context context, View view) {
        Animation animation = android.view.animation.AnimationUtils.loadAnimation(context, R.anim.scale_up);
        view.startAnimation(animation);
    }
}
