package com.gospelware.liquidbutton.controller;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.support.v4.view.animation.FastOutLinearInInterpolator;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import com.gospelware.liquidbutton.LiquidButton;

import java.util.Random;

/**
 * Created by ricogao on 30/06/2016.
 */

public class WaveController extends PourBaseController {

    private float liquidProgress;
    private float nextLiquidProgress;
    private float liquidLevel;

    private int left;
    private int pourLength;

    private Path wavePath;
    private Path circlePath;

    private final static int LIQUID_COLOR_BLUE = 24;
    private static final long LIQUID_ANIMATION_DURATION = 5000;

    //control shift-x on sin wave
    private float phi;
    private float amplitude;

    private float currAmplitude;
    private static final int FAI_FACTOR = 6;
    private static final float APTITUDE_RATIO = 0.2f;
    private static final float ANGLE_VELOCITY = 0.5f;

    private ObjectAnimator liquidLevelAnimator;

    private Random random;

    private LiquidButton.PourFinishListener listener;

    public WaveController() {
        super();
        random = new Random();
        wavePath = new Path();
        circlePath = new Path();
    }

    public interface OnProgressUpdateListener {
        void onProgressUpdate(float progress);
    }

    public void setPourFinishListener(LiquidButton.PourFinishListener listener) {
        this.listener = listener;
    }

    public float getLiquidProgress() {
        return this.liquidProgress;
    }

    @Override
    public Animator buildAnimator() {
        ObjectAnimator animator = (ObjectAnimator) getBaseAnimator(LIQUID_ANIMATION_DURATION, new FastOutLinearInInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);
        return animator;
    }

    @Override
    public void reset() {
        super.reset();
        phi = 0;
        liquidLevel = 0;
        liquidProgress = 0;
        nextLiquidProgress = 0;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawLiquid(canvas);
    }

    @Override
    protected void setRender(float progress) {
        super.setRender(liquidProgress);
        computeLiquid(liquidProgress);
        computePhi();
    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);
        pourLength = width;
        amplitude = radius * APTITUDE_RATIO;
        left = centerX - radius;
        circlePath.addCircle(centerX, centerY, radius, Path.Direction.CW);
    }

    private void computePhi() {
        // scroll x by the fai factor
        //slowly reduce the wave frequency
        if (liquidLevel < bottom) {
            float faiFactor;
            if (liquidLevel > centerY) {
                faiFactor = 0.4f + (liquidLevel - centerY) / (float) radius;
            } else {
                faiFactor = 0.4f + ((float) centerY - liquidLevel) / (float) radius;
            }

            phi += FAI_FACTOR * (faiFactor);
            if (phi >= 360) {

                //Generate Bubbles when phi is larger than 360
                int count = random.nextInt(4);
                for (int i = 0; i < count; i++) {
                    generateBubble(centerX, liquidLevel);
                }
                Log.i(PourStartController.class.getSimpleName(), "Bubble Generated");
                phi = 0;
            }
        }
    }

    private void computeColor() {
        int red = (liquidLevel >= centerY) ? 255 : Math.round((1 - ((float) centerY - liquidLevel) / (float) radius) * 255);
        int green = (liquidLevel <= centerY) ? 255 : Math.round((1 - (liquidLevel - centerY) / (float) radius) * 255);
        int liquidColor = Color.rgb(red, green, LIQUID_COLOR_BLUE);

        pourPaint.setColor(liquidColor);
        liquidPaint.setColor(liquidColor);
        bubblePaint.setColor(liquidColor);
    }

    @Override
    protected void computePour(float progress) {
        pourBottom.y = pourLength + frameTop;
    }


    private void computeLiquid(float progress) {

        liquidLevel = computeLiquidLevel(progress);

        computeColor();

        computeWave();

    }

    private float computeLiquidLevel(float progress) {
        return bottom - (2 * radius * progress);
    }


    private void computeWave() {
        float reduceRatio = 1.4f + (liquidLevel - top) / (float) (2 * radius);

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
            int dy = (int) (currAmplitude * Math.sin((i * ANGLE_VELOCITY + phi) * Math.PI / 180) + liquidLevel);

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


    /**
     * This will only works when the progress is larger than the current Progress
     *
     * @param progress
     */
    public void changeProgress(float progress) {
        if (progress > nextLiquidProgress) {
            nextLiquidProgress = progress;
            if (liquidLevelAnimator != null) {
                liquidLevelAnimator.cancel();
                startLiquidChange(liquidProgress, nextLiquidProgress);
            } else {
                startLiquidChange(liquidProgress, nextLiquidProgress);
            }
        }
    }

    private void startLiquidChange(float current, float target) {
        liquidLevelAnimator = ObjectAnimator.ofFloat(this, "liquidProgress", current, target);
        liquidLevelAnimator.setDuration(computeDuration(current, target));
        liquidLevelAnimator.setInterpolator(new LinearInterpolator());
        if (target >= 1f) {
            liquidLevelAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    getCheckView().finishPour();
                    getAnimator().cancel();
                    reset();
                }
            });
        }
        liquidLevelAnimator.start();
    }

    private void setLiquidProgress(float liquidProgress) {
        if (liquidProgress > 1f) {
            this.liquidProgress = 1f;
        } else if (liquidProgress < 0f) {
            this.liquidProgress = 0f;
        } else {
            this.liquidProgress = liquidProgress;
        }

        if (listener != null) {
            listener.onProgressUpdate(liquidProgress);
        }
    }

    private int computeDuration(float current, float target) {
        return Math.round((target - current) * LIQUID_ANIMATION_DURATION);
    }

}
