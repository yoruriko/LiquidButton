package com.gospelware.liquildbutton;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by ricogao on 06/05/2016.
 */

public class LiquidButton extends View {

    private Paint pourPaint, liquidPaint, tickPaint, bubblePaint;
    private int centreX, centerY, frameTop, left, top, radius, bottom;
    private int bounceY;
    private int pourHeight;

    private int pourStrokeWidth;
    private int tickStrokeWidth;

    private PointF pourTop, pourBottom, tickPoint1, tickPoint2, tickPoint3, tickControl2, tickControl3;

    private float liquidLevel;
    private Path circlePath;
    private Path wavePath, tickPath;
    private Animation liquidAnimation, bounceAnimation, tickAnimation;
    private AnimationSet set;

    private ObjectAnimator liquidAnimator, bounceAnimator, tickAnimator;
    private AnimatorSet animatorSet;

    private List<Bubble> bubbles = new ArrayList<>();
    private Random random;

    private int liquidColor;

    //control shift-x on sin wave
    private int fai = 0;

    private static final int FAI_FACTOR = 5;
    private float aptitude;
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

    class Bubble {
        PointF start, end, control, current;
        float alpha, radius;

        public Bubble(PointF start, PointF end, PointF control, float radius) {
            this.start = start;
            this.end = end;
            this.control = control;
            this.radius = radius;
            current = start;
        }

        // Bezier Curve B(t)=(1-t)^2*P0+2t(1-t)*P1+t^2P2

        public float doMaths(float timeLeft, float time, float start, float control, float end) {
            return timeLeft * timeLeft * start
                    + 2 * time * timeLeft * control
                    + time * time * end;
        }

        public void evaluate(float interpolatedTime) {
            float timeLeft = 1.0f - interpolatedTime;
            PointF pointF = new PointF();

            pointF.x = doMaths(timeLeft, interpolatedTime, start.x, control.x, end.x);
            pointF.y = doMaths(timeLeft, interpolatedTime, start.y, control.y, end.y);

            alpha = 1.0f - interpolatedTime;

            current = pointF;
        }
    }

    class LiquidAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);

            computeColor(interpolatedTime);
            computePourStart(interpolatedTime);
            computeLiquid(interpolatedTime);

            invalidate();
        }
    }

    class BounceAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            computePourFinish(interpolatedTime);
            computeBounceBall(interpolatedTime);
            invalidate();
        }
    }

    class TickAnimation extends Animation {
        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            computeTick(interpolatedTime);
            invalidate();
        }
    }


    protected void init() {
        pourPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pourPaint.setDither(true);
        pourPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        liquidPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        liquidPaint.setDither(true);
        liquidPaint.setStyle(Paint.Style.FILL);

        tickPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tickPaint.setDither(true);
        tickPaint.setColor(Color.WHITE);
        tickPaint.setStrokeCap(Paint.Cap.ROUND);
        tickPaint.setStyle(Paint.Style.STROKE);


        bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bubblePaint.setStyle(Paint.Style.FILL);

        pourTop = new PointF();
        pourBottom = new PointF();

        tickPoint1 = new PointF();
        tickPoint2 = new PointF();
        tickPoint3 = new PointF();

        circlePath = new Path();
        wavePath = new Path();
        tickPath = new Path();
        random = new Random();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (set != null) {
            drawPour(canvas);

            if (bubbles.size() > 0) {
                drawBubbles(canvas);
            }

            if (liquidAnimation.hasEnded()) {
                drawBounceBall(canvas);
                if (bounceAnimation.hasEnded()) {
                    drawTick(canvas);
                }
            } else {
                drawLiquid(canvas);
            }
        }


    }

    protected void computeColor(float interpolatedTime) {
        int blue = 24;
        int red = (interpolatedTime <= FINISH_POUR) ? 255 : Math.round(255 * (1 - (interpolatedTime - FINISH_POUR) / TOUCH_BASE));
        int green = (interpolatedTime >= FINISH_POUR) ? 255 : Math.round(255 * interpolatedTime / FINISH_POUR);
        liquidColor = Color.rgb(red, green, blue);
    }

    protected void computePourStart(float interpolatedTime) {
        //0.0~0.1 drop to bottom, 0.9~1.0 on top
        pourBottom.y = (interpolatedTime < TOUCH_BASE) ? interpolatedTime / TOUCH_BASE * pourHeight + frameTop : bottom;

    }

    protected void computePourFinish(float interpolatedTime) {
        pourTop.y = frameTop + (2 * radius * interpolatedTime);

        //generate some bubbles when the pour animation comes to end
        if (Math.abs(interpolatedTime - 0.2f) <= 0.15f) {
            int count = random.nextInt(3) + 3;
            for (int i = 0; i < count; i++) {
                generateBubble();
            }
        }
    }

    protected void drawPour(Canvas canvas) {
        pourPaint.setColor(liquidColor);
        canvas.drawLine(centreX, pourTop.y, centreX, pourBottom.y, pourPaint);
    }

    protected void computeLiquid(float interpolatedTime) {

        liquidLevel = (interpolatedTime < TOUCH_BASE) ? bottom : bottom - (2 * radius * (interpolatedTime - TOUCH_BASE) / FINISH_POUR);

        //generate bubbles at 0.3, 0.6 and 0.9
        if (interpolatedTime > 0.2f) {
            if (interpolatedTime % 0.3f <= 0.01) {
                generateBubble();
            }
        }

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
        float a = (interpolatedTime <= FINISH_POUR) ? aptitude : aptitude * (1.4f - interpolatedTime);

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

    protected void generateBubble() {

        PointF start = new PointF();
        PointF control = new PointF();
        PointF end = new PointF();

        start.x = centreX;
        control.x = generateBubbleX();
        end.x = control.x + (control.x - start.x) * random.nextFloat();
        start.y = liquidLevel;
        control.y = liquidLevel - radius * (random.nextFloat() + 0.2f);
        end.y = liquidLevel - (0.5f * (start.y - control.y));


        float radius = generateRadius();

        final Bubble bubble = new Bubble(start, end, control, radius);
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setInterpolator(new DecelerateInterpolator(0.8f));
        animator.setDuration(generateDuration());

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                bubble.evaluate((float) animation.getAnimatedValue());
            }
        });

        animator.start();
        bubbles.add(bubble);
    }

    protected float generateBubbleX() {
        int side = random.nextInt();
        //generate bubbles
        if (side % 2 == 0) {
            return centreX - 0.5f * pourStrokeWidth - (random.nextFloat() * radius * 0.8f);
        } else {
            return centreX + 0.5f * pourStrokeWidth + (random.nextFloat() * radius * 0.8f);
        }
    }

    protected float generateRadius() {
        return radius * 0.2f * random.nextFloat();
    }

    protected int generateDuration() {
        return random.nextInt(500) + 1000;
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
        bounceY = (interpolatedTime < 1f) ? centerY : Math.round((interpolatedTime - 1) * radius) + centerY;
    }

    protected void drawBounceBall(Canvas canvas) {
        canvas.drawCircle(centreX, bounceY, radius, liquidPaint);
    }


    protected void computeTick(float interpolatedTime) {
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

    protected void drawTick(Canvas canvas) {
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

    protected void drawBubbles(Canvas canvas) {
        for (Bubble bubble : bubbles) {
            bubblePaint.setColor(liquidColor);
            bubblePaint.setAlpha(Math.round(255 * bubble.alpha));
            canvas.drawCircle(bubble.current.x, bubble.current.y, bubble.radius, bubblePaint);
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int width = getWidth();
        int height = getHeight();

        centreX = width / 2;
        centerY = height / 2;
        bounceY = centerY;

        radius = width / 4;
        aptitude = radius * 0.3f;
        pourStrokeWidth = radius / 6;
        pourPaint.setStrokeWidth(pourStrokeWidth);
        tickStrokeWidth = pourStrokeWidth / 2;
        tickPaint.setStrokeWidth(tickStrokeWidth);

        frameTop = centerY - 3 * radius;
        left = centreX - radius;
        top = centerY - radius;
        bottom = centerY + radius;

        pourHeight = 4 * radius;

        tickPoint1.x = left + (0.29f * 2 * radius);
        tickPoint1.y = top + (0.525f * 2 * radius);

        tickPoint2.x = left + (0.445f * 2 * radius);
        tickPoint2.y = top + (0.675f * 2 * radius);

        tickPoint3.x = left + (0.74f * 2 * radius);
        tickPoint3.y = top + (0.45f * 2 * radius);

        circlePath.addCircle(centreX, centerY, radius, Path.Direction.CW);
    }

    public void startPour() {

        fai = 0;
        tickControl2 = null;
        tickControl3 = null;
        bubbles.clear();


        if (set == null) {
            set = new AnimationSet(false);
            liquidAnimation = new LiquidAnimation();
            liquidAnimation.setDuration(5000);
            liquidAnimation.setInterpolator(new DecelerateInterpolator(0.8f));

            bounceAnimation = new BounceAnimation();
            bounceAnimation.setDuration(500);
            bounceAnimation.setInterpolator(new OvershootInterpolator(3.0f));
            bounceAnimation.setStartOffset(5000);

            tickAnimation = new TickAnimation();
            tickAnimation.setDuration(800);
            tickAnimation.setInterpolator(new OvershootInterpolator(2.0f));
            tickAnimation.setStartOffset(5500);

            Animation scale = new ScaleAnimation(1f, 0.8f, 1f, 0.8f, centreX, centerY);
            scale.setDuration(500);
            scale.setStartOffset(5500);

            set.addAnimation(liquidAnimation);
            set.addAnimation(bounceAnimation);
            set.addAnimation(tickAnimation);
            set.setFillAfter(true);
            set.addAnimation(scale);
        }

        startAnimation(set);
    }

//        if (animatorSet == null) {
//            animatorSet = new AnimatorSet();
//
//            liquidAnimator = ObjectAnimator.ofFloat(this, "liquid", 0.0f, 1.0f);
//            liquidAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float interpolatedTime = (float) animation.getAnimatedValue();
//
//                    ObjectAnimator animator = (ObjectAnimator) animation;
//                    LiquidButton button = (LiquidButton) animator.getTarget();
//                    button.computeColor(interpolatedTime);
//                    button.computePourStart(interpolatedTime);
//                    button.computeLiquid(interpolatedTime);
//                    button.invalidate();
//                }
//            });
//            liquidAnimator.setDuration(5000);
//            liquidAnimator.setInterpolator(new DecelerateInterpolator(0.8f));
//
//            bounceAnimator = ObjectAnimator.ofFloat(this, "bounce", 0.0f, 1f);
//            bounceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float interpolatedTime = (float) animation.getAnimatedValue();
//
//                    ObjectAnimator animator = (ObjectAnimator) animation;
//                    LiquidButton button = (LiquidButton) animator.getTarget();
//
//                    button.computePourFinish(interpolatedTime);
//                    button.computeBounceBall(interpolatedTime);
//
//                    button.invalidate();
//                }
//            });
//            bounceAnimator.setDuration(500);
//            bounceAnimator.setInterpolator(new OvershootInterpolator(3.0f));
//
//            tickAnimator = ObjectAnimator.ofFloat(this, "tick", 0.0f, 1f);
//            tickAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                @Override
//                public void onAnimationUpdate(ValueAnimator animation) {
//                    float interpolatedTime = (float) animation.getAnimatedValue();
//
//                    ObjectAnimator animator = (ObjectAnimator) animation;
//                    LiquidButton button = (LiquidButton) animator.getTarget();
//                    button.computeTick(interpolatedTime);
//                    button.invalidate();
//                }
//            });
//            tickAnimator.setDuration(800);
//            tickAnimator.setInterpolator(new OvershootInterpolator(2.0f));
//
//            animatorSet.playSequentially(liquidAnimator, bounceAnimator, tickAnimator);
//        }
//        animatorSet.start();
//
//    }
}
