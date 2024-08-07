package com.gt.sdk.base.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.utils.ResourceUtil;

public class ShakeView extends RelativeLayout {
    public static final int SHAKE_ANIMATOR_SHAKE = 1;
    public static final int SHAKE_ANIMATOR_TIP = 2;
    private ImageView mImageView;
    private TextView mTitleView;
    private TextView mDescriptionView;
    private AnimatorSet animatorSet;
    private ObjectAnimator anim;
    private ObjectAnimator anim2;
    private View mShadowView;
    private boolean isShakeStop;

    public ShakeView(Context context) {
        super(context);
        initLayout(context);
    }

    public ShakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLayout(context);
    }

    public ShakeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initLayout(context);
    }

    private void initLayout(Context context) {

        View.inflate(context, getLayoutId(context), this);

        mImageView = findViewById(ResourceUtil.getId(context, "sig_shakeImageView"));
        mTitleView = findViewById(ResourceUtil.getId(context, "sig_shakeTitleView"));
        mDescriptionView = findViewById(ResourceUtil.getId(context, "sig_shakeDescView"));
        mShadowView = findViewById(ResourceUtil.getId(context, "sig_shake_view"));

        initAnimation2();
        initAnimation();
    }

    void StartUpDownAnimation() {
        anim2.start();
    }

    private void initAnimation() {
        int height = mImageView.getLayoutParams().height;
        int width = mImageView.getLayoutParams().width;

        mImageView.setPivotX(width);
        mImageView.setPivotY(height * 0.8f);
        anim = ObjectAnimator.ofFloat(mImageView, "rotation", 0f, 18f, 0f, -18f, 0, 18f, 0f, -18f, 0f, 18f, 0);
        anim.setInterpolator(new LinearInterpolator());
        anim.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (!isShakeStop) {
                            anim.start();
                        }
                    }
                }, 300);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        anim.setDuration(1500);
    }

    private void initAnimation2() {
        anim2 = ObjectAnimator.ofFloat(mShadowView, "translationY", 0f, 30f, 0f, -30f, 0);
        anim2.setInterpolator(new LinearInterpolator());
        anim2.setRepeatCount(2);
        anim2.setDuration(400);
    }

    public void startAnimation(int animatorType) {
        switch (animatorType) {
            case SHAKE_ANIMATOR_SHAKE: {
                anim.start();
            }
            break;
            default: {
                isShakeStop = true;
                anim.cancel();
                anim2.start();
            }
            break;
        }
    }

    private int getLayoutId(Context context) {
        return ResourceUtil.getLayoutId(context, "sig_shake_view_layout");
    }
}
