package com.gospelware.liquidbutton.controller;

import android.animation.Animator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.view.animation.OvershootInterpolator;

/**
 * Created by ricogao on 16/05/2016.
 */
public class TickController extends BaseController {

    private float[] ticksCoordinates = new float[]{0.29f, 0.525f, 0.445f, 0.675f, 0.74f, 0.45f};
    private Path tickPath;
    private PointF tickPoint1, tickPoint2, tickPoint3, tickControl2, tickControl3;
    private static final float TICK_OVERSHOOT_TENSION = 2.0f;
    private static final long TICK_ANIMATION_DURATION = 800;
    private static final float SCALE_DOWN_SIZE = 0.8f;
    private final static String LIQUID_COLOR = "#00FF24";
    private Paint tickPaint, circlePaint;
    private float scale;

    public TickController() {
        super();
        tickPath = new Path();

        tickPoint1 = new PointF();
        tickPoint2 = new PointF();
        tickPoint3 = new PointF();

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setDither(true);
        tickPaint.setColor(Color.WHITE);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);
        tickPaint.setStyle(Paint.Style.STROKE);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setDither(true);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setColor(Color.parseColor(LIQUID_COLOR));
    }

    @Override
    public Animator buildAnimator() {
        return getBaseAnimator(TICK_ANIMATION_DURATION, new OvershootInterpolator(TICK_OVERSHOOT_TENSION));
    }

    @Override
    public void reset() {
        scale = 1.0f;
        tickControl2 = null;
        tickControl3 = null;
    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);

        float left = centerX - radius;
        float top = centerY - radius;

        tickPoint1.x = left + (ticksCoordinates[0] * 2 * radius);
        tickPoint1.y = top + (ticksCoordinates[1] * 2 * radius);

        tickPoint2.x = left + (ticksCoordinates[2] * 2 * radius);
        tickPoint2.y = top + (ticksCoordinates[3] * 2 * radius);

        tickPoint3.x = left + (ticksCoordinates[4] * 2 * radius);
        tickPoint3.y = top + (ticksCoordinates[5] * 2 * radius);

        tickPaint.setStrokeWidth(radius / 12);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.scale(scale, scale, centerX, centerY);
        drawCircle(canvas);
        drawTick(canvas);
    }

    private void drawCircle(Canvas canvas) {
        canvas.drawCircle(centerX, centerY, radius, circlePaint);
    }

    private void drawTick(Canvas canvas) {
        tickPath.reset();

        tickPath.moveTo(tickPoint1.x, tickPoint1.y);

        if (tickControl2 != null) {
            tickPath.lineTo(tickControl2.x, tickControl2.y);
        }

        if (tickControl3 != null) {
            tickPath.lineTo(tickControl3.x, tickControl3.y);
        }

        canvas.drawPath(tickPath, tickPaint);
    }

    @Override
    protected void setRender(float interpolatedTime) {
        super.setRender(interpolatedTime);
        computeScale(interpolatedTime);
        computeTick(interpolatedTime);
    }

    private void computeScale(float interpolatedTime) {
        float scaleDown = (1.0f - SCALE_DOWN_SIZE) * (interpolatedTime);
        scale = 1.0f - scaleDown;
    }

    private void computeTick(float interpolatedTime) {
        if (interpolatedTime <= 0.5f) {

            float dt = interpolatedTime / 0.5f;
            float dx = (tickPoint2.x - tickPoint1.x) * dt;
            float dy = (tickPoint2.y - tickPoint1.y) * dt;

            if (tickControl2 == null) {
                tickControl2 = new PointF();
            }

            tickControl2.x = tickPoint1.x + dx;
            tickControl2.y = tickPoint1.y + dy;
        } else {

            float dt = (interpolatedTime - 0.5f) / 0.5f;
            float dx = (tickPoint3.x - tickPoint2.x) * dt;
            float dy = (tickPoint3.y - tickPoint2.y) * dt;

            if (tickControl3 == null) {
                tickControl3 = new PointF();
            }

            tickControl3.x = tickPoint2.x + dx;
            tickControl3.y = tickPoint2.y + dy;
        }
    }
}
