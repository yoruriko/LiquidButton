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
abstract class PourBaseController extends BaseController {

    PointF pourBottom, pourTop;
    Paint pourPaint;
    Paint liquidPaint;
    Paint bubblePaint;

    int frameTop;
    int bottom;
    int top;
    private float pourStrokeWidth;

    private List<Bubble> bubbles;

    PourBaseController() {
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

        bubbles = new ArrayList<>();
    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);
        frameTop = centerY - 3 * radius;
        bottom = centerY + radius;
        top = centerY+radius;
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

    private boolean hasBubble() {
        return bubbles != null && !bubbles.isEmpty();
    }

    @Override
    public void reset() {
        bubbles.clear();
    }

    @Override
    protected void setRender(float interpolatedTime) {
        super.setRender(interpolatedTime);
        computePour(interpolatedTime);
    }

    protected abstract void computePour(float interpolatedTime);

    private void drawPour(Canvas canvas) {
        canvas.drawLine(centerX, pourTop.y, centerX, pourBottom.y, pourPaint);
    }

    void generateBubble(float x, float y) {

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
