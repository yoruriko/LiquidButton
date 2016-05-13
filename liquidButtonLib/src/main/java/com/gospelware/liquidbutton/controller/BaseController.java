package com.gospelware.liquidbutton.controller;


import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.graphics.Canvas;

import com.gospelware.liquidbutton.LiquidCheckView;

import java.lang.ref.WeakReference;

/**
 * Created by ricogao on 12/05/2016.
 */
public abstract class BaseController {
    //set WeakReference to avoid memory leak
    private WeakReference<LiquidCheckView> checkView;
    Animator animator;

    int centerX;
    int centerY;
    int radius;

    public BaseController() {
        animator=buildAnimator();
    }

    public void setCheckView(LiquidCheckView checkView) {
        this.checkView = new WeakReference<LiquidCheckView>(checkView);
    }

    public LiquidCheckView getCheckView() {
        return checkView != null ? checkView.get() : null;
    }

    public abstract void draw(Canvas canvas);

    public abstract void render(float interpolatedTime);

    public Animator buildAnimator() {
        return null;
    }

    public Animator getAnimator() {
        return animator;
    }


    public void getMeasure(int width, int height) {
        centerX = width / 2;
        centerY = height / 2;
        radius = width / 4;
    }


    public ValueAnimator getBaseAnimator(long duration, TimeInterpolator interpolator) {
        ValueAnimator animator = ValueAnimator.ofFloat(0.0f, 1.0f);
        animator.setDuration(duration);
        animator.setInterpolator(interpolator);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float interpolatedTime = (float) animation.getAnimatedValue();
                render(interpolatedTime);
                getCheckView().invalidate();
            }
        });
        return animator;
    }
}
