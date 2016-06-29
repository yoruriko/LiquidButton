package com.gospelware.liquidbutton.utils;

import android.animation.ObjectAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.animation.DecelerateInterpolator;


import com.gospelware.liquidbutton.LiquidButton;

import java.util.Random;

/**
 * Created by ricogao on 13/05/2016.
 */
public class Bubble {

    private PointF start, end, control, current;
    private int alpha;
    private float radius;
    private long duration;
    private LiquidButton target;

    private Bubble(BubbleGenerator generator) {
        this.start = generator.start;
        this.control = generator.control;
        this.end = generator.end;
        this.radius = generator.radius;
        this.duration = generator.duration;
        this.target=generator.target;
        current = start;
        alpha = 255;
    }

    // Bezier Curve B(t)=(1-t)^2*P0+2t(1-t)*P1+t^2P2
    private float doMaths(float time, float timeLeft, float start, float control, float end) {
        return timeLeft * timeLeft * start
                + 2 * time * timeLeft * control
                + time * time * end;
    }

    private void evaluate(float interpolatedTime) {
        float timeLeft = 1.0f - interpolatedTime;
        alpha = Math.round((1.0f - interpolatedTime) * 255);
        current.x = doMaths(interpolatedTime, timeLeft, start.x, control.x, end.x);
        current.y = doMaths(interpolatedTime, timeLeft, start.y, control.y, end.y);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setAlpha(alpha);
        canvas.drawCircle(current.x, current.y, radius, paint);
    }

    public void startAnim() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "bubble", 0.0f, 1.0f);
        animator.setInterpolator(new DecelerateInterpolator(0.8f));
        animator.setDuration(duration);
        animator.start();
    }

    //used by Animator
    public void setBubble(float interpolatedTime){
        evaluate(interpolatedTime);
        target.postInvalidate();
    }

    public static class BubbleGenerator {
        private Random random;
        private PointF start, end, control;
        private float radius;
        private int duration;
        private LiquidButton target;

        public BubbleGenerator(float startX, float startY) {
            random = new Random();
            this.end = new PointF();
            this.control = new PointF();
            this.start = new PointF(startX, startY);
        }


        public Bubble generate() {
            return new Bubble(this);
        }

        public BubbleGenerator with(LiquidButton target){
            this.target=target;
            return this;
        }

        public BubbleGenerator generateBubbleX(float origin, float range, float offset) {
            int side = random.nextInt();
            float dx;
            //generate bubbles
            if (side % 2 == 0) {
                dx = origin - (random.nextFloat() * range) - offset;
            } else {
                dx = origin + (random.nextFloat() * range) + offset;
            }

            this.control.x = dx;
            this.end.x = control.x + (control.x - start.x) * random.nextFloat();
            return this;
        }

        public BubbleGenerator generateBubbleY(float origin, float range) {
            this.control.y = origin - range * (random.nextFloat() + 0.2f);
            this.end.y = origin - (0.5f * (start.y - control.y));
            return this;
        }

        public BubbleGenerator generateRadius(float range) {
            this.radius = range * random.nextFloat();
            return this;
        }

        public BubbleGenerator generateDuration(int min,int range){
            this.duration=random.nextInt(range) + min;
            return this;
        }

    }
}


