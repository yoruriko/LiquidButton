package com.gospelware.liquidbutton.controller;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.animation.OvershootInterpolator;

import java.util.Random;

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
        int color = Color.parseColor(LIQUID_COLOR);
        pourPaint.setColor(color);
        liquidPaint.setColor(color);
        bubblePaint.setColor(color);

    }


    @Override
    public Animator buildAnimator() {
        return getBaseAnimator(BOUNCE_ANIMATION_DURATION, new OvershootInterpolator(BOUNCE_OVERSHOOT_TENSION));
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawBounceBall(canvas);
    }

    @Override
    protected void setRender(float interpolatedTime) {
        super.setRender(interpolatedTime);
        computeBounceBall(interpolatedTime);
    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);
    }

    @Override
    protected void computePour(float interpolatedTime) {
        pourTop.y = frameTop + (2 * radius * interpolatedTime);
        pourBottom.y = bottom;

        //generate some bubbles when the pour animation comes to end
        if (Math.abs(interpolatedTime - 0.2f) <= 0.15f) {
            int count = new Random().nextInt(3) + 3;
            for (int i = 0; i < count; i++) {
                generateBubble(centerX, bottom - 2 * radius);
                Log.i(PourFinishController.class.getSimpleName(), "Bubble Generated");
            }
        }
    }

    private void computeBounceBall(float interpolatedTime) {
        bounceY = (interpolatedTime < 1f) ? centerY : Math.round((interpolatedTime - 1) * radius) + centerY;
    }

    private void drawBounceBall(Canvas canvas) {
        canvas.drawCircle(centerX, bounceY, radius, liquidPaint);
    }
}
