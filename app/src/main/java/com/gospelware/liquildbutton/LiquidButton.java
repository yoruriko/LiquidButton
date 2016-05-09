package com.gospelware.liquildbutton;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;

/**
 * Created by ricogao on 06/05/2016.
 */
public class LiquidButton extends View {

    private Paint pourPaint, liquidPaint;
    private int width, height, centreX, centerY, frameTop, left, radius, bottom;
    private int bounceY;
    private int pourHeight;

    private final int POUR_STROKE_WIDTH = 30;

//    private float mInterpolatedTime;

    private PointF pourTop, pourBottom;

    private float liquidLevel;
    private Path circlePath;
    private Path wavePath;
    private LiquidAnimation liquidAnimation;

    private int liquidColor;

    //control shift-x on sin wave
    private int fai = 0;

    private static final int FAI_FACTOR = 5;
    private static final int AMPLITUDE = 50;
    private static final float ANGLE_VELOCITY = 0.5f;

    private static final int POUR_START = 1;
    private static final int POUR_END = -1;

    private final float TOUCH_BASE = 0.1f;
    private final float FINISH_POUR = 0.9f;

    public LiquidButton(Context context) {
        super(context);
    }

    public LiquidButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiquidButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    class LiquidAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);

            computeColor(interpolatedTime);
            computePour(POUR_START, interpolatedTime);
            computeLiquid(interpolatedTime);

            invalidate();
        }
    }

    class BounceAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            computePour(POUR_END, interpolatedTime);
            computeBounceBall(interpolatedTime);
            invalidate();
        }
    }

    class LiquidAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            //reset the scroll in x direction while the animation start
            fai = 0;
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            BounceAnimation bounceAnimation = new BounceAnimation();
            bounceAnimation.setDuration(500);
            bounceAnimation.setInterpolator(new BounceInterpolator());
            startAnimation(bounceAnimation);
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }


    protected void init() {
        pourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pourPaint.setDither(true);
        pourPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pourPaint.setStrokeWidth(POUR_STROKE_WIDTH);

        liquidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        liquidPaint.setDither(true);
        liquidPaint.setStyle(Paint.Style.FILL);

        pourTop = new PointF();
        pourBottom = new PointF();
        circlePath = new Path();
        wavePath = new Path();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (liquidAnimation != null) {
            if (!liquidAnimation.hasEnded()) {
                drawLiquid(canvas);
                drawPour(canvas);
            } else {
                drawPour(canvas);
                drawBounceBall(canvas);
            }
        }

    }

    protected void computeColor(float interpolatedTime) {
        int blue = 24;
        int red = (interpolatedTime <= FINISH_POUR) ? 255 : Math.round(255 * (1 - (interpolatedTime - FINISH_POUR) / TOUCH_BASE));
        int green = (interpolatedTime >= FINISH_POUR) ? 255 : Math.round(255 * interpolatedTime / FINISH_POUR);
        liquidColor = Color.rgb(red, green, blue);
    }

    protected void computePour(int type, float interpolatedTime) {
        pourTop.x = centreX;
        pourBottom.x = centreX;
        if (type == POUR_START) {
            pourTop.y = frameTop;
            //0.0~0.1 drop to bottom, 0.9~1.0 on top
            pourBottom.y = (interpolatedTime < TOUCH_BASE) ? interpolatedTime / TOUCH_BASE * pourHeight + frameTop : bottom;
        } else if (type == POUR_END) {
            pourTop.y = frameTop + (2 * radius * interpolatedTime);
        }
    }

    protected void drawPour(Canvas canvas) {

        pourPaint.setColor(liquidColor);
        canvas.drawLine(pourTop.x, pourTop.y, pourBottom.x, pourBottom.y, pourPaint);
    }

    protected void computeLiquid(float interpolatedTime) {

        liquidLevel = (interpolatedTime < TOUCH_BASE) ? bottom : bottom - (2 * radius * (interpolatedTime - TOUCH_BASE) / FINISH_POUR);

        // scroll x by the fai factor
        if (interpolatedTime >= TOUCH_BASE) {
            //slowly reduce the wave frequency
            fai += FAI_FACTOR * (1.4f - interpolatedTime);
            if (fai == 360) {
                fai = 0;
            }
        }
        //clear the path for next render
        wavePath.reset();
        //slowly reduce the amplitude when filling comes to end
        float a = (interpolatedTime <= FINISH_POUR) ? AMPLITUDE : AMPLITUDE * (1.4f - interpolatedTime);

        for (int i = 0; i < 2 * radius; i++) {
            int dx = left + i;

            // y = a * sin( w * x + fai ) + h
            int dy = (int) (a * Math.sin((i * ANGLE_VELOCITY + fai) * Math.PI / 180) + liquidLevel);

            if (i == 0) {
                wavePath.moveTo(dx, dy);
            }

            wavePath.quadTo(dx, dy, dx + 1, dy);
        }

        wavePath.lineTo(centreX + radius, bottom);
        wavePath.lineTo(left, bottom);

        wavePath.close();
    }


    protected void drawLiquid(Canvas canvas) {

        //save the canvas status
        canvas.save();
        //clip the canvas to circle
        liquidPaint.setColor(liquidColor);
        canvas.clipPath(circlePath);
        canvas.drawPath(wavePath, liquidPaint);
        //restore the canvas status~
        canvas.restore();

    }


    protected void computeBounceBall(float interpolatedTime) {

    }

    protected void drawBounceBall(Canvas canvas) {
        canvas.drawCircle(centreX, bounceY, radius, liquidPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.width = getWidth();
        this.height = getHeight();

        centreX = width / 2;
        centerY = height / 2;
        bounceY = centerY;

        radius = width / 8;

        frameTop = centerY - 3 * radius;
        left = centreX - radius;
        bottom = centerY + radius;

        pourHeight = 4 * radius;

        circlePath.addCircle(centreX, centerY, radius, Path.Direction.CW);
    }

    public void startPour() {
        //reset some factors
        if (liquidAnimation == null) {
            liquidAnimation = new LiquidAnimation();
            liquidAnimation.setDuration(5000);
            liquidAnimation.setInterpolator(new DecelerateInterpolator(0.8f));
//        pour.setRepeatCount(Animation.INFINITE);
            liquidAnimation.setAnimationListener(new LiquidAnimationListener());
        }

        startAnimation(liquidAnimation);

    }
}
