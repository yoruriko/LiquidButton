package com.gospelware.liquidbutton;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
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
public class LiquidCheckView extends View {

    private BaseController mController;
    private List<BaseController> mControllers;
    private PourFinishListener listener;

    public LiquidCheckView(Context context) {
        this(context, null);
    }

    public LiquidCheckView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiquidCheckView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setController(BaseController controller) {
        this.mController = controller;
        if (hasController()) {
            mController.setCheckView(this);
        }
    }

    private boolean hasController() {
        return mController != null;
    }

    public BaseController getController() {
        return mController;
    }

    public void setControllers(List<BaseController> controllers) {
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

    public List<BaseController> getControllers() {
        return mControllers;
    }

    protected void init() {
        List<BaseController> controllers = new ArrayList<>();
        PourStartController startController = new PourStartController();
        PourFinishController finishController = new PourFinishController();
        TickController tickController=new TickController();
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
        if (hasController()) {
            mController.getMeasure(width, height);
        } else if (hasControllers()) {
            for (BaseController controller : mControllers) {
                controller.getMeasure(width, height);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (hasController()) {
            mController.draw(canvas);
        } else if (hasControllers()) {
            for (BaseController controller : mControllers) {
                controller.draw(canvas);
            }
        }
    }

    public void startPour() {
        if (hasController()) {
            Animator animator=mController.getAnimator();
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishPour();
                }
            });
        } else if (hasControllers()) {
            List<Animator> animators = new ArrayList<>();

            for (BaseController controller : mControllers) {
                animators.add(controller.getAnimator());
            }
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.playSequentially(animators);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    finishPour();
                }
            });
            animatorSet.start();
        }
    }

    public void finishPour() {
        if (listener != null) {
            listener.onPourFinish();
        }
    }

}
