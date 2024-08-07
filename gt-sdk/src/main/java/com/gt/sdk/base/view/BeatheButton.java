package com.gt.sdk.base.view;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.czhj.sdk.common.utils.Dips;


public class BeatheButton extends FrameLayout {
    private static float line_width;
    private static final int ANIMATION_DURATION = 1700;

    private BeatheView beatheView;
    private BeatheView beatheView2;
    private BeatheView beatheView3;

    public BeatheButton(Context context) {
        super(context);
        init();
    }

    public BeatheButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public BeatheButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {


        line_width = Dips.dipsToIntPixels(6, getContext());

        beatheView = new BeatheView(getContext());
        addView(beatheView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        beatheView2 = new BeatheView(getContext());
        addView(beatheView2, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        beatheView3 = new BeatheView(getContext());
        beatheView3.showBackground(true);
        addView(beatheView3, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        startAnimation(beatheView, 0);
        startAnimation(beatheView2, ANIMATION_DURATION / 3);

    }

    public void startAnimation(BeatheView view, int delay) {
        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", 0, Dips.dipsToIntPixels(-30, getContext()));
        translateY.setDuration(ANIMATION_DURATION);
        translateY.setStartDelay(delay);

        ObjectAnimator alpha = ObjectAnimator.ofFloat(view, "alpha", 1, 0.1f);
        alpha.setDuration(ANIMATION_DURATION);
        alpha.setStartDelay(delay);


        ValueAnimator lineWidth = ValueAnimator.ofFloat(line_width, Dips.dipsToIntPixels(1, getContext()));

        lineWidth.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                view.setLineWidth((float) valueAnimator.getAnimatedValue());

            }
        });
        lineWidth.setDuration((int) (ANIMATION_DURATION * 0.8f));
        lineWidth.setStartDelay(delay + (long) (ANIMATION_DURATION * 0.2));

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(translateY, alpha, lineWidth);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                animator.start();
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }


}
