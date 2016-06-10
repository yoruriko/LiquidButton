package com.gospelware.liquidbutton.controller;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

import com.gospelware.liquidbutton.utils.Bubble;

import java.util.ArrayList;
import java.util.List;

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
    Paint bubblePaint;

    int pourHeight;
    int frameTop;
    int bottom;
    float pourStrokeWidth;

    List<Bubble> bubbles;

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

        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setStyle(Paint.Style.FILL);

        bubbles = new ArrayList<Bubble>();
    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);
        pourHeight = width;
        frameTop = centerY - 3 * radius;
        bottom = centerY + radius;
        pourStrokeWidth = radius / 6;
        pourPaint.setStrokeWidth(pourStrokeWidth);
    }

    @Override
    public void draw(Canvas canvas) {
        drawPour(canvas);

        if (hasBubble()) {
            for (Bubble bubble : bubbles) {
                bubble.draw(canvas, bubblePaint);
            }
        }
    }

    protected boolean hasBubble() {
        return bubbles != null && !bubbles.isEmpty();
    }

    @Override
    public void reset() {
        bubbles.clear();
    }

    @Override
    public void setRender(float interpolatedTime) {
        super.setRender(interpolatedTime);
        computePour(interpolatedTime);
    }

    protected abstract void computePour(float interpolatedTime);

    protected void drawPour(Canvas canvas) {
        canvas.drawLine(centerX, pourTop.y, centerX, pourBottom.y, pourPaint);
    }

    protected void generateBubble(float x, float y) {

        Bubble.BubbleGenerator generator =
                new Bubble.BubbleGenerator(x, y)
                        .with(getCheckView())
                        .generateBubbleX(x, radius * 0.5f, pourStrokeWidth * 0.5f)
                        .generateBubbleY(y, radius)
                        .generateRadius(radius * 0.2f)
                        .generateDuration(1500, 500);

        Bubble bubble = generator.generate();
        bubble.startAnim();
        bubbles.add(bubble);
    }

}
