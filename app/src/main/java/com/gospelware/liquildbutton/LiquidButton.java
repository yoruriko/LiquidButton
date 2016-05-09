package com.gospelware.liquildbutton;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by ricogao on 06/05/2016.
 */
public class LiquidButton extends View {

    private Paint pourPaint, liquidPaint;
    private int width, height, centreX, centerY, top, radius, bottom;
    private int pourHeight;

    private final int POUR_STROKE_WIDTH = 30;

    private float mInterpolatedTime;

    private PointF pourTop, pourBottom;

    private float liquidLevel;
    private Path circlePath;
    private Path wavePath;
    private LiquidAnimation liquidAnimation;

    private int liquidColor;

    //control shift-x on sin wave
    private int fai = 0;

    private static final int FAI_FACTOR = 5;
    private static final int AMPLITUDE = 40;
    private static final float ANGLE_VELOCITY = 0.5f;

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
            mInterpolatedTime = interpolatedTime;

            if (interpolatedTime >= TOUCH_BASE) {
                fai += FAI_FACTOR;
                if (fai == 360) {
                    fai = 0;
                }
            }

            computeColor();
            computePour();
            computeLiquid();

            invalidate();
        }
    }

    class LiquidAnimationListener implements Animation.AnimationListener {

        @Override
        public void onAnimationStart(Animation animation) {
            //reset the factors while the animation start
            mInterpolatedTime = 0;
            fai = 0;
        }

        @Override
        public void onAnimationEnd(Animation animation) {

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

        drawLiquid(canvas);
        drawPour(canvas);

    }

    protected void computeColor() {
        int blue = 24;
        int red = (mInterpolatedTime <= FINISH_POUR) ? 255 : Math.round(255 * (1 - (mInterpolatedTime - FINISH_POUR) / TOUCH_BASE));
        int green = (mInterpolatedTime >= FINISH_POUR) ? 255 : Math.round(255 * mInterpolatedTime / FINISH_POUR);
        liquidColor = Color.rgb(red, green, blue);
    }


    protected void drawPour(Canvas canvas) {

        pourPaint.setColor(liquidColor);
        canvas.drawLine(pourTop.x, pourTop.y, pourBottom.x, pourBottom.y, pourPaint);
    }

    protected void computePour() {
        pourTop.x = centreX;
        //0.0~0.9 on top, 0.9~1.0 drop to bottom
        pourTop.y = (mInterpolatedTime > FINISH_POUR) ? (mInterpolatedTime - FINISH_POUR) / TOUCH_BASE * 2 * radius + top : top;

//        pourTop.y = top;

        pourBottom.x = centreX;
        //0.0~0.1 drop to bottom, 0.9~1.0 on top
        pourBottom.y = (mInterpolatedTime < TOUCH_BASE) ? mInterpolatedTime / TOUCH_BASE * pourHeight + top : bottom;
    }

    protected void drawLiquid(Canvas canvas) {

        //save the canvas status
        canvas.save();
        //clip the canvas to circle
        liquidPaint.setColor(liquidColor);
        canvas.clipPath(circlePath);
        canvas.drawPath(wavePath, liquidPaint);
        //restore the canvas status
        canvas.restore();

    }

    protected void computeLiquid() {

        liquidLevel = (mInterpolatedTime < TOUCH_BASE) ? bottom : bottom - (2 * radius * (mInterpolatedTime - TOUCH_BASE) / FINISH_POUR);

        int x = centreX - radius;
        int y = centerY + radius;

        wavePath.reset();

        for (int i = 0; i < 2 * radius; i++) {
            int dx = x + i;

            // y = a * sin( w * x + fai ) + h
            int dy = (int) (AMPLITUDE * Math.sin((i * ANGLE_VELOCITY + fai) * Math.PI / 180) + liquidLevel);

            if (i == 0) {
                wavePath.moveTo(dx, dy);
            }

            wavePath.quadTo(dx, dy, dx + 1, dy);

        }

        wavePath.lineTo(centreX + radius, y);
        wavePath.lineTo(x, y);
        wavePath.close();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        this.width = getWidth();
        this.height = getHeight();

        centreX = width / 2;
        centerY = height / 2;

        radius = width / 8;

        top = centerY - 3 * radius;
        bottom = centerY + radius;
        pourHeight = 4 * radius;

        circlePath.addCircle(centreX, centerY, radius, Path.Direction.CW);
    }

    public void startPour() {
        //reset some factors
        if (liquidAnimation == null) {
            liquidAnimation = new LiquidAnimation();
            liquidAnimation.setDuration(5000);
            liquidAnimation.setInterpolator(new AccelerateInterpolator(0.6f));
//        pour.setRepeatCount(Animation.INFINITE);
            liquidAnimation.setAnimationListener(new LiquidAnimationListener());
        }

        startAnimation(liquidAnimation);

    }
}
