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

    private int liquidColor;

    //control shift-x on sin wave
    private int fai = 0;
    private final int FAI_FACTOR = 5;
    private final int AMPLITUDE = 30;
    private final float ANGLE_VELOCITY = 0.5f;

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

    protected void init() {
        pourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pourPaint.setDither(true);
        pourPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pourPaint.setStrokeWidth(POUR_STROKE_WIDTH);

        liquidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        liquidPaint.setStyle(Paint.Style.FILL);

        pourTop = new PointF();
        pourBottom = new PointF();
        circlePath = new Path();
        wavePath = new Path();
    }

    class LiquidAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            mInterpolatedTime = interpolatedTime;

            if (interpolatedTime >= 0.2) {
                fai += FAI_FACTOR;
                if (fai == 360) {
                    fai = 0;
                }
            }
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        computeColor();
        drawLiquid(canvas);
        drawPour(canvas);
    }

    protected void computeColor() {
        int blue = 0;
        int red = (mInterpolatedTime <= 0.8f) ? 255 : Math.round(255 * (1 - (mInterpolatedTime - 0.8f) / 0.2f));
        int green = (mInterpolatedTime >= 0.8f) ? 255 : Math.round(255 * mInterpolatedTime / 0.8f);
        liquidColor = Color.rgb(red, green, blue);
    }


    protected void drawPour(Canvas canvas) {
        computePour();
        pourPaint.setColor(liquidColor);
        canvas.drawLine(pourTop.x, pourTop.y, pourBottom.x, pourBottom.y, pourPaint);
    }

    protected void computePour() {
        pourTop.x = centreX;
        //0.0~0.8 on top, 0.8~1.0 drop to bottom
        pourTop.y = (mInterpolatedTime > 0.8) ? (mInterpolatedTime - 0.8f) / 0.2f * 2 * radius + top : top;

        pourBottom.x = centreX;
        //0.0~0.2 drop to bottom, 0.8~1.0 on top
        pourBottom.y = (mInterpolatedTime < 0.2) ? mInterpolatedTime / 0.2f * pourHeight + top : bottom;
    }

    protected void drawLiquid(Canvas canvas) {
        computeLiquid();


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
        liquidLevel = (mInterpolatedTime < 0.2f) ? bottom : bottom - (2 * radius * (mInterpolatedTime - 0.2f) / 0.8f);

        int x = centreX - radius;
        int y = centerY + radius;


        wavePath.reset();

        for (int i = 0; i < 2 * radius; i++) {
            int dx = x + i;
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
        mInterpolatedTime = 0;
        LiquidAnimation pour = new LiquidAnimation();
        pour.setDuration(3000);
        pour.setInterpolator(new AccelerateInterpolator(0.6f));
//        pour.setRepeatCount(Animation.INFINITE);
        startAnimation(pour);
    }
}
