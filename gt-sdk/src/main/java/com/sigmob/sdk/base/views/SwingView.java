package com.sigmob.sdk.base.views;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;


import com.sigmob.sdk.base.utils.ViewUtil;

import java.util.UUID;

public class SwingView extends MotionView {

    private SwingPhoneView phoneView;
    private ArcProgressBar progressBar;
    private boolean isRunning = false;


    public SwingView( Context context) {
        super(context);

        BackgroundView backgroundView = new BackgroundView(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        addView(backgroundView, layoutParams);

        progressBar = new ArcProgressBar(context);
        phoneView = new SwingPhoneView(context);
        FrameLayout.LayoutParams layoutParams1 = new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT);
        progressBar.setLayoutParams(layoutParams1);
        addView(progressBar);

        addView(phoneView);

        obseverLayout();

    }


    public SwingView( Context context,  AttributeSet attrs) {
        super(context, attrs);
        obseverLayout();


    }

    public SwingView( Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obseverLayout();
    }

    @Override
    public void startAnimator() {
        isRunning = true;
        if (phoneView != null) {
            phoneView.startAnimation();
        }

    }

    @Override
    public void stopAnimator() {
        isRunning = false;
        if (progressBar != null) {
            progressBar.updateProcess(0);
        }
        if (phoneView != null) {
            phoneView.stopAnimation();
        }

    }

    private void obseverLayout() {
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                SwingView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                init();
            }
        });
    }

    private void init() {

        int width = (int) (getWidth() * 0.25f);
        int height = (int) (width * 1.62f);

        LayoutParams layoutParams1 = new LayoutParams(width, height);
        layoutParams1.setMargins(0, (int) (getHeight() * 0.38f), 0, 0);
        layoutParams1.addRule(CENTER_HORIZONTAL);
        phoneView.setLayoutParams(layoutParams1);
    }

    public void updateProcess(float process) {
        if (progressBar != null && isRunning) {
            progressBar.updateProcess(process);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        obseverLayout();
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
