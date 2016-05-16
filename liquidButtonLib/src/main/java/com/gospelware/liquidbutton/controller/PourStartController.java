package com.gospelware.liquidbutton.controller;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.support.v4.view.animation.FastOutLinearInInterpolator;

/**
 * Created by ricogao on 12/05/2016.
 */
public class PourStartController extends PourBaseController {


    private float liquidLevel;
    private int left;

    private Path wavePath;
    private Path circlePath;

    private final static int LIQUID_COLOR_BLUE = 24;
    private static final long LIQUID_ANIMATION_DURATION = 5000;

    //control shift-x on sin wave
    private int fai = 0;
    private float aptitude;
    private static final int FAI_FACTOR = 5;
    private static final float APTITUDE_RATIO = 0.3f;
    private static final float ANGLE_VELOCITY = 0.5f;

    public PourStartController() {
        super();
        wavePath = new Path();
        circlePath = new Path();
    }

    @Override
    public Animator buildAnimator() {
        Animator animator = getBaseAnimator(LIQUID_ANIMATION_DURATION, new FastOutLinearInInterpolator());

        return animator;
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
        computeColor(interpolatedTime);
        computeLiquid(interpolatedTime);

    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);
        aptitude = radius * APTITUDE_RATIO;
        left = centerX - radius;
        circlePath.addCircle(centerX, centerY, radius, Path.Direction.CW);
    }

    protected void computeColor(float interpolatedTime) {

        int red = (interpolatedTime <= FINISH_POUR) ? 255 : Math.round(255 * (1 - (interpolatedTime - FINISH_POUR) / TOUCH_BASE));
        int green = (interpolatedTime >= FINISH_POUR) ? 255 : Math.round(255 * interpolatedTime / FINISH_POUR);
        int liquidColor = Color.rgb(red, green, LIQUID_COLOR_BLUE);

        pourPaint.setColor(liquidColor);
        liquidPaint.setColor(liquidColor);
        bubblePaint.setColor(liquidColor);
    }

    @Override
    protected void computePour(float interpolatedTime) {
        //0.0~0.1 drop to bottom, 0.9~1.0 on top
        pourBottom.y = (interpolatedTime < TOUCH_BASE) ? interpolatedTime / TOUCH_BASE * pourHeight + frameTop : bottom;

    }


    protected void computeLiquid(float interpolatedTime) {
        liquidLevel = (interpolatedTime < TOUCH_BASE) ? bottom : bottom - (2 * radius * (interpolatedTime - TOUCH_BASE) / FINISH_POUR);

        //generate bubbles at 0.4, 0.6 ,0.8 and 1.0
        if (interpolatedTime > 0.2f) {
            if (interpolatedTime % 0.2f <= 0.01) {
                generateBubble(centerX, liquidLevel);
            }
        }

        float reduceRatio = 1.4f - interpolatedTime;

        // scroll x by the fai factor
        if (interpolatedTime >= TOUCH_BASE) {
            //slowly reduce the wave frequency
            fai += FAI_FACTOR * (reduceRatio);
            if (fai == 360) {
                fai = 0;
            }
        }

        //clear the path for next render
        wavePath.reset();
        //slowly reduce the amplitude when filling comes to end
        float a = (interpolatedTime <= FINISH_POUR) ? aptitude : aptitude * (reduceRatio);

        for (int i = 0; i < 2 * radius; i++) {
            int dx = left + i;

            // y = a * sin( w * x + fai ) + h
            int dy = (int) (a * Math.sin((i * ANGLE_VELOCITY + fai) * Math.PI / 180) + liquidLevel);

            if (i == 0) {
                wavePath.moveTo(dx, dy);
            }

            wavePath.quadTo(dx, dy, dx + 1, dy);
        }

        wavePath.lineTo(centerX + radius, bottom);
        wavePath.lineTo(left, bottom);

        wavePath.close();
    }

    protected void drawLiquid(Canvas canvas) {
        //save the canvas status
        canvas.save();
        //clip the canvas to circle
        canvas.clipPath(circlePath);
        canvas.drawPath(wavePath, liquidPaint);
        //restore the canvas status~
        canvas.restore();
    }
}
