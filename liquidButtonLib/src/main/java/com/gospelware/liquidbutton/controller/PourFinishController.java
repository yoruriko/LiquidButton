package com.gospelware.liquidbutton.controller;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.animation.OvershootInterpolator;

/**
 * Created by ricogao on 12/05/2016.
 */
public class PourFinishController extends PourBaseController {
    private int bounceY;
    private final static String LIQUID_COLOR = "#00FF24";
    private static final float BOUNCE_OVERSHOOT_TENSION = 3.0f;
    private static final long BOUNCE_ANIMATION_DURATION = 500;

    public PourFinishController() {
        super();
        pourPaint.setColor(Color.parseColor(LIQUID_COLOR));
        liquidPaint.setColor(Color.parseColor(LIQUID_COLOR));
    }


    @Override
    public Animator buildAnimator() {
        return getBaseAnimator(BOUNCE_ANIMATION_DURATION,new OvershootInterpolator(BOUNCE_OVERSHOOT_TENSION));
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if(animator.isRunning()) {
            drawBounceBall(canvas);
        }
    }

    @Override
    public void render(float interpolatedTime) {
        super.render(interpolatedTime);
        computeBounceBall(interpolatedTime);
    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);
    }

    @Override
    protected void computePour(float interpolatedTime) {
        pourTop.y = frameTop + (2 * radius * interpolatedTime);
        pourBottom.y=bottom;
    }

    protected void computeBounceBall(float interpolatedTime) {
        bounceY = (interpolatedTime < 1f) ? centerY : Math.round((interpolatedTime - 1) * radius) + centerY;
    }

    protected void drawBounceBall(Canvas canvas) {
        canvas.drawCircle(centerX, bounceY, radius, liquidPaint);
    }
}
