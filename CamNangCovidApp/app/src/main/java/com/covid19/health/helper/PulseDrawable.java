//Source: https://gist.github.com/txusballesteros/7e2f6fc1d1c0fe9998bb

package com.covid19.health.helper;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.animation.DecelerateInterpolator;

public class PulseDrawable extends Drawable {
    private final static float CENTER_AREA_SIZE = 0.6f;
    private final static int PULSE_START_COLOR_OPACITY = 0;
    private final static float MINIMUM_RADIUS = 0;
    private final static int ANIMATION_DURATION_IN_MS = 1500;
    private final int color;
    private Paint centerPaint;
    private Paint pulsePaint;
    private float fullSizeRadius;
    private float currentExpandAnimationValue = 0f;
    private int currentAlphaAnimationValue = 255;

    public PulseDrawable(int color) {
        this.color = color;
        initializeDrawable();
    }

    private void initializeDrawable() {
        preparePaints();
        prepareAnimation();
    }

    private void prepareAnimation() {
        final ValueAnimator expandAnimator = ValueAnimator.ofFloat(0f, 1f);
        expandAnimator.setRepeatCount(ValueAnimator.INFINITE);
        expandAnimator.setRepeatMode(ValueAnimator.RESTART);
        expandAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentExpandAnimationValue = (float) animation.getAnimatedValue();
                if (currentExpandAnimationValue == 0f) {
                    currentAlphaAnimationValue = 255;
                }
                invalidateSelf();
            }
        });
        final ValueAnimator alphaAnimator = ValueAnimator.ofInt(255, 0);
        alphaAnimator.setStartDelay(ANIMATION_DURATION_IN_MS / 4);
        alphaAnimator.setRepeatCount(ValueAnimator.INFINITE);
        alphaAnimator.setRepeatMode(ValueAnimator.RESTART);
        alphaAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                currentAlphaAnimationValue = (int) animation.getAnimatedValue();
            }
        });
        AnimatorSet animation = new AnimatorSet();
        animation.playTogether(expandAnimator, alphaAnimator);
        animation.setDuration(ANIMATION_DURATION_IN_MS);
        animation.setInterpolator(new DecelerateInterpolator());
        animation.start();
    }

    private void preparePaints() {
        pulsePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pulsePaint.setStyle(Paint.Style.FILL);
        centerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        centerPaint.setStyle(Paint.Style.FILL);
        centerPaint.setColor(color);
    }

    @Override
    public void setAlpha(int alpha) {
        pulsePaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) { }

    @Override
    public int getOpacity() {
        return pulsePaint.getAlpha();
    }

    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        float centerX = bounds.exactCenterX();
        float centerY = bounds.exactCenterY();
        calculateFullSizeRadius();
        preparePaintShader();
        renderPulse(canvas, centerX, centerY);
    }

    private void renderPulse(Canvas canvas, float centerX, float centerY) {
        float currentRadius = fullSizeRadius * currentExpandAnimationValue;
        if (currentRadius > MINIMUM_RADIUS) {
            canvas.drawCircle(centerX, centerY, currentRadius, pulsePaint);
        }
    }

    private void preparePaintShader() {
        Rect bounds = getBounds();
        float centerX = bounds.exactCenterX();
        float centerY = bounds.exactCenterY();
        float radius = (Math.min(bounds.width(), bounds.height()) / 2);
        if (radius > MINIMUM_RADIUS) {
            int edgeColor = getPulseColor();
            int centerColor = Color.argb(PULSE_START_COLOR_OPACITY, Color.red(color),
                    Color.green(color),
                    Color.blue(color));
            pulsePaint.setShader(new RadialGradient(centerX, centerY, radius,
                    centerColor, edgeColor, Shader.TileMode.CLAMP));
        } else {
            pulsePaint.setShader(null);
        }
    }

    private int getPulseColor() {
        return Color.argb(currentAlphaAnimationValue, Color.red(color),
                Color.green(color),
                Color.blue(color));
    }

    private void calculateFullSizeRadius() {
        Rect bounds = getBounds();
        float minimumDiameter = Math.min(bounds.width(), bounds.height());
        fullSizeRadius = (minimumDiameter / 2);
    }
}