package com.gospelware.liquidbutton.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.animation.DecelerateInterpolator;

import java.util.Random;

/**
 * Created by ricogao on 13/05/2016.
 */
public class Bubble {

    private PointF start, end, control, current;
    private int alpha;
    private float radius;
    private long duration;
    private ObjectAnimator animator;

    private final static float BUBBLE_INTERPOLATOR_FACTOR = 0.8f;

    private Bubble(BubbleGenerator generator) {
        this.start = generator.start;
        this.control = generator.control;
        this.end = generator.end;
        this.radius = generator.radius;
        this.duration = generator.duration;
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
        current.x = doMaths(timeLeft, interpolatedTime, start.x, control.x, end.x);
        current.y = doMaths(timeLeft, interpolatedTime, start.y, control.y, end.y);
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setAlpha(alpha);
        canvas.drawCircle(current.x, current.y, radius, paint);
    }

    public void startAnim() {
        animator = ObjectAnimator.ofFloat(this, "bubble", 0.0f, 1.0f);
        animator.setInterpolator(new DecelerateInterpolator(BUBBLE_INTERPOLATOR_FACTOR));
        animator.setDuration(duration);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                ObjectAnimator anim = (ObjectAnimator) animation;
                Bubble b = (Bubble) anim.getTarget();
                float interpolatedTime = (float) anim.getAnimatedValue();
                if (b != null) {
                    b.evaluate(interpolatedTime);
                }
            }
        });
        animator.start();
    }


    public static class BubbleGenerator {
        private Random random;
        private PointF start, end, control;
        private float radius;
        private int duration;

        public BubbleGenerator(float startX, float startY) {
            random = new Random();
            this.end = new PointF();
            this.control = new PointF();
            this.start = new PointF(startX, startY);
        }

        public Bubble generate() {
            return new Bubble(this);
        }


        public BubbleGenerator rangeX(float minX, float rangeX) {
            float offsetX = minX + getRandomX(rangeX);
            this.control.x = start.x + offsetX;
            this.end.x = control.x + (offsetX * random.nextFloat());
            return this;
        }

        public BubbleGenerator rangeY(float minY, float rangeY) {
            float offsetY = minY + getRandomFloatOfRange(rangeY);
            this.control.y = start.y - offsetY;
            this.end.y = control.y + (random.nextFloat() * offsetY);
            return this;
        }

        public BubbleGenerator rangeDuration(int minDuration, int rangeDuration) {
            int durationOffset = random.nextInt(rangeDuration);
            this.duration = minDuration + durationOffset;
            return this;
        }

        public BubbleGenerator rangeRadius(float min, float range) {
            float offsetRadius = min + getRandomFloatOfRange(range);
            this.radius = offsetRadius;
            return this;
        }

        private float getRandomX(float range) {
            int sign = random.nextInt(1);
            float result = getRandomFloatOfRange(range);
            return (sign % 2 == 0) ? result : -result;
        }

        private float getRandomFloatOfRange(float range) {
            return random.nextFloat() * range;
        }

    }
}


