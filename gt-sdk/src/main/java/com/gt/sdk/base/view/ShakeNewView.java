package com.gt.sdk.base.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ResourceUtil;

public class ShakeNewView extends MotionView {

    private ImageView mImageView;
    private ObjectAnimator anim;
    private boolean isRunning;
    private BackgroundView mBackgroundView;

    public ShakeNewView(Context context) {
        super(context);
        init();
    }

    public ShakeNewView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShakeNewView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @Override
    public void startAnimator() {
        if (anim != null && !isRunning) {
            anim.start();
        }
        isRunning = true;
    }

    @Override
    public void stopAnimator() {
        if (anim != null) {
            anim.cancel();
        }
        isRunning = false;
    }

    private void init() {

        LayoutParams layoutParams1 = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutParams1.addRule(CENTER_IN_PARENT);
        mBackgroundView = new BackgroundView(getContext());
        addView(mBackgroundView, layoutParams1);

        int size = Dips.dipsToIntPixels(45, getContext());
        mImageView = new ImageView(getContext());
        Drawable drawable = getResources().getDrawable(ResourceUtil.getDrawableId(getContext(), "sig_image_shake_new"));
        mImageView.setImageDrawable(drawable);
        mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        LayoutParams layoutParams = new LayoutParams(size, size);
        layoutParams.addRule(CENTER_IN_PARENT);
        addView(mImageView, layoutParams);
        initView();
    }

    private void initView() {
        ShakeNewView.this.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                int height = ShakeNewView.this.getHeight();
                int width = ShakeNewView.this.getWidth();

                if (height == 0 || width == 0) return;
                ShakeNewView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                ViewGroup.LayoutParams layoutParams = mImageView.getLayoutParams();
                layoutParams.width = height / 2;
                layoutParams.height = height / 2;
                mImageView.setLayoutParams(layoutParams);
                mImageView.setPivotX(height / 2);
                mImageView.setPivotY(height / 2 * 0.8f);
            }
        });
    }

    private void initAnimation() {
        if (anim != null) return;
        anim = ObjectAnimator.ofFloat(mImageView, "rotation", 0f, 18f, 0f, -18f, 0, 18f, 0f, -18f, 0f, 18f, 0);
        anim.setInterpolator(new LinearInterpolator());
        anim.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isRunning) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            anim.start();
                        }
                    }, 300);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(1200);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initView();
        initAnimation();
        if (isRunning) {
            anim.cancel();
            anim.start();
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimator();
    }
}

