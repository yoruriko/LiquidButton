package com.gospelware.liquidbutton.controller;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.Log;

/**
 * Created by ricogao on 12/05/2016.
 */
public class PourStartController extends PourBaseController {

    private float liquidLevel;
    private int left;
    private int pourLength;

    private Path wavePath;
    private Path circlePath;

    private final static int LIQUID_COLOR_BLUE = 24;
    private static final long LIQUID_ANIMATION_DURATION = 5000;

    //control shift-x on sin wave
    private float fai;
    private float amplitude;

    private float currAmplitude;
    private static final int FAI_FACTOR = 6;
    private static final float APTITUDE_RATIO = 0.2f;
    private static final float ANGLE_VELOCITY = 0.5f;

    public PourStartController() {
        super();
        wavePath = new Path();
        circlePath = new Path();
    }

    @Override
    public Animator buildAnimator() {
        return getBaseAnimator(LIQUID_ANIMATION_DURATION, new FastOutLinearInInterpolator());
    }

    @Override
    public void reset() {
        super.reset();
        fai = 0;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawLiquid(canvas);
    }

    @Override
    public void setRender(float interpolatedTime) {
        super.setRender(interpolatedTime);
        computeLiquid(interpolatedTime);
    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);
        pourLength = width;
        amplitude = radius * APTITUDE_RATIO;
        left = centerX - radius;
        circlePath.addCircle(centerX, centerY, radius, Path.Direction.CW);
    }

    private void computeColor(float liquidLevel) {

        int red = (liquidLevel >= centerY) ? 255 : Math.round((1 - ((float) centerY - liquidLevel) / (float) radius) * 255);
        int green = (liquidLevel <= centerY) ? 255 : Math.round((1 - (liquidLevel - centerY) / (float) radius) * 255);
        int liquidColor = Color.rgb(red, green, LIQUID_COLOR_BLUE);

        pourPaint.setColor(liquidColor);
        liquidPaint.setColor(liquidColor);
        bubblePaint.setColor(liquidColor);
    }

    @Override
    protected void computePour(float interpolatedTime) {
        //0.0~0.1 drop to bottom, 0.9~1.0 on top
        pourBottom.y = (interpolatedTime < TOUCH_BASE) ? interpolatedTime / TOUCH_BASE * pourLength + frameTop : bottom;

    }


    private void computeLiquid(float interpolatedTime) {

        liquidLevel = (interpolatedTime < TOUCH_BASE) ? bottom : bottom - (2 * radius * (interpolatedTime - TOUCH_BASE) / FINISH_POUR);

        computeColor(liquidLevel);

        computeWave(liquidLevel);

        //generate bubbles at 0.4, 0.6 ,0.8 and 1.0
        if (interpolatedTime > 0.2f) {
            if (interpolatedTime % 0.2f <= 0.01) {
                generateBubble(centerX, liquidLevel);
                Log.i(PourStartController.class.getSimpleName(), "Bubble Generated");
            }
        }


    }

    private void computeWave(float level) {
        float reduceRatio = 1.4f + (level - top) / (float) (2 * radius);

        // scroll x by the fai factor
        //slowly reduce the wave frequency
        if (level < bottom) {
            float faiFactor;
            if (level > centerY) {
                faiFactor = 0.4f + (level - centerY) / (float) radius;
            } else {
                faiFactor = 0.4f + ((float) centerY - level) / (float) radius;
            }

            fai += FAI_FACTOR * (faiFactor);
            if (fai >= 360) {
                fai = 0;
            }
        }

        //slowly reduce the amplitude when filling comes to end
        currAmplitude = amplitude * reduceRatio;
        computeWavePath();
    }

    private void computeWavePath() {
        //clear the path for next render
        wavePath.reset();

        for (int i = 0; i < 2 * radius; i++) {
            int dx = left + i;

            // y = a * sin( w * x + fai ) + h
            int dy = (int) (currAmplitude * Math.sin((i * ANGLE_VELOCITY + fai) * Math.PI / 180) + liquidLevel);

            if (i == 0) {
                wavePath.moveTo(dx, dy);
            }

            wavePath.quadTo(dx, dy, dx + 1, dy);
        }

        wavePath.lineTo(centerX + radius, bottom);
        wavePath.lineTo(left, bottom);

        wavePath.close();
    }

    private void drawLiquid(Canvas canvas) {
        //save the canvas status
        canvas.save();
        //clip the canvas to circle
        if (liquidLevel < bottom) {
            canvas.clipPath(circlePath);
            canvas.drawPath(wavePath, liquidPaint);
        }//restore the canvas status~
        canvas.restore();
    }
}
