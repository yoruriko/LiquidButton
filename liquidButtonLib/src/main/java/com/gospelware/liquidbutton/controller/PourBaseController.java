package com.gospelware.liquidbutton.controller;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Created by ricogao on 12/05/2016.
 */
public abstract class PourBaseController extends BaseController {

    //interpolated time when liquid reach the bottom of the ball
    final static float TOUCH_BASE = 0.1f;
    //interpolated time when liquid starts to finish pouring
    final static float FINISH_POUR = 0.9f;

    PointF pourBottom, pourTop;
    Paint pourPaint;
    Paint liquidPaint;
//    Paint liquidPaint;

    int pourHeight;
    int frameTop;
    int bottom;

    public PourBaseController() {
        super();
        pourBottom = new PointF();
        pourTop = new PointF();
        pourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pourPaint.setDither(true);
        pourPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        liquidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        liquidPaint.setDither(true);
        liquidPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);
        pourHeight = width;
        frameTop = centerY - 3 * radius;
        bottom = centerY + radius;
        pourPaint.setStrokeWidth(radius / 6);
    }

    @Override
    public void draw(Canvas canvas) {
        if (animator.isRunning()) {
            drawPour(canvas);
        }
    }

    @Override
    public void render(float interpolatedTime) {
        computePour(interpolatedTime);
    }

    protected abstract void computePour(float interpolatedTime);

    protected void drawPour(Canvas canvas) {
        canvas.drawLine(centerX, pourTop.y, centerX, pourBottom.y, pourPaint);
    }
}
