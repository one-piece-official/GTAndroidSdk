package com.gt.sdk.base.view;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class WringView extends MotionView {

    private WringArcView arcView;
    private WringPhoneView phone;
    private ObjectAnimator animator;

    private boolean isRunning;

    public WringView(Context context) {
        super(context);
        init();
    }

    public WringView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WringView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void startAnimator() {
        isRunning = true;
        if (animator != null) {
            animator.cancel();
            animator.start();
        }
    }

    @Override
    public void stopAnimator() {
        isRunning = false;
        if (animator != null) {
            animator.cancel();
        }
    }

    private void init() {

        BackgroundView backgroundView = new BackgroundView(getContext());
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);

        addView(backgroundView, layoutParams);

        arcView = new WringArcView(getContext());
        phone = new WringPhoneView(getContext());

        addView(arcView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(phone, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        animator = ObjectAnimator.ofFloat(phone, View.ROTATION_Y, -45, 0, 45);

        animator.setDuration(1000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setRepeatMode(ObjectAnimator.REVERSE);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isRunning) {
            startAnimator();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimator();
    }
}
