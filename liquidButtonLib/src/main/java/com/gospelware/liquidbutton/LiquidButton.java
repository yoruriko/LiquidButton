package com.gospelware.liquidbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.gospelware.liquidbutton.controller.BaseController;
import com.gospelware.liquidbutton.controller.PourFinishController;
import com.gospelware.liquidbutton.controller.PourStartController;
import com.gospelware.liquidbutton.controller.TickController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ricogao on 12/05/2016.
 */
public class LiquidButton extends View {

    private static final String TAG = LiquidButton.class.getSimpleName();

    private List<BaseController> mControllers;
    private PourFinishListener listener;
    private boolean isFillAfter;
    private boolean drawFillAfterFlag;
    private Animator mAnimator;

    public LiquidButton(Context context) {
        this(context, null);
    }

    public LiquidButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiquidButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    public void setFillAfter(boolean fillAfter) {
        this.isFillAfter = fillAfter;
    }

    private void setControllers(List<BaseController> controllers) {
        this.mControllers = controllers;
        if (hasControllers()) {
            for (BaseController controller : mControllers) {
                controller.setCheckView(this);
            }
        }
    }

    private boolean hasControllers() {
        return mControllers != null && mControllers.size() > 0;
    }



    /**
     * Basic Animations to build the LiquidButton
     */
    private void init() {
        List<BaseController> controllers = new ArrayList<>();
        PourStartController startController = new PourStartController();
        PourFinishController finishController = new PourFinishController();
        TickController tickController = new TickController();
        controllers.add(startController);
        controllers.add(finishController);
        controllers.add(tickController);
        setControllers(controllers);
    }

    public interface PourFinishListener {
        void onPourFinish();
    }

    public void setPourFinishListener(PourFinishListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int width = getWidth();
        int height = getHeight();

        if (hasControllers()) {
            for (BaseController controller : mControllers) {
                controller.getMeasure(width, height);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        BaseController controller = getRunningController();
        if (controller != null) {
            controller.draw(canvas);
        }

        //if the fill after flag is on, draw the lastFrame
        if (drawFillAfterFlag) {
            onFillAfter(canvas);
        }
    }

    private BaseController getRunningController() {
        if (hasControllers()) {
            for (BaseController controller : mControllers) {
                if (controller.isRunning()) {
                    return controller;
                }
            }
        }

        return null;

    }

    private void onFillAfter(Canvas canvas) {
        if (hasControllers()) {
            //draw the last frame of the last controller
            BaseController controller = mControllers.get(mControllers.size() - 1);
            if (!controller.isRunning()) {
                controller.draw(canvas);
            }
        }
    }

    private Animator buildAnimator() {
        if (hasControllers()) {
            List<Animator> animators = new ArrayList<>();
            for (BaseController controller : mControllers) {
                animators.add(controller.getAnimator());
            }
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(animators);
            return animatorSet;
        }

        return null;
    }

    public void startPour() {

        //clear the fillAfterFlag
        if (drawFillAfterFlag) {
            drawFillAfterFlag = false;
        }

        if (mAnimator == null) {
            mAnimator = buildAnimator();
            mAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    onPourEnd();
                    finishPour();
                }
            });
        }

        if (mAnimator != null && !mAnimator.isRunning()) {
            mAnimator.start();
        } else {
            Log.e(TAG, "No controller or Animator is been build");
        }
    }

    private void onPourEnd() {
        //turn the fillAfter flag ON if it's been set
        drawFillAfterFlag = isFillAfter;

        if (drawFillAfterFlag) {
            postInvalidate();
        }
    }


    private void finishPour() {
        if (listener != null) {
            listener.onPourFinish();
        }
    }

}
