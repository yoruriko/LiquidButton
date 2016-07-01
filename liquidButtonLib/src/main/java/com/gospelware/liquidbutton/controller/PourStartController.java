package com.gospelware.liquidbutton.controller;


import android.animation.Animator;
import android.graphics.Color;
import android.view.animation.AccelerateInterpolator;

/**
 * Created by ricogao on 12/05/2016.
 */
public class PourStartController extends PourBaseController {

    private static final int POUR_START_ANIMATION_DURATION = 500;
    private static final String LIQUID_COLOUR = "#FF0024";
    private int pourLength;

    public PourStartController() {
        super();
        int color = Color.parseColor(LIQUID_COLOUR);
        pourPaint.setColor(color);
    }

    @Override
    public void getMeasure(int width, int height) {
        super.getMeasure(width, height);
        pourLength = width;
    }

    @Override
    protected void computePour(float interpolatedTime) {
        pourBottom.y = interpolatedTime * pourLength + frameTop;
    }

    @Override
    public Animator buildAnimator() {
        return getBaseAnimator(POUR_START_ANIMATION_DURATION, new AccelerateInterpolator());
    }
}
