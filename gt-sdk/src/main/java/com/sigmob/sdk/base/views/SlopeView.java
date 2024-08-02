package com.sigmob.sdk.base.views;

import android.content.Context;
import android.util.AttributeSet;

public class SlopeView extends MotionView {


    private SlopePhoneView mSlopePhoneView;
    private BackgroundView mBackgroundView;
    private boolean isRunning = false;

    public SlopeView(Context context) {
        super(context);
        init();

    }

    public SlopeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public SlopeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();

    }

    @Override
    public void startAnimator() {
        isRunning = true;

    }

    @Override
    public void stopAnimator() {
        isRunning = false;
        if (mSlopePhoneView != null) {
            mSlopePhoneView.updateScreen(0);
        }

    }

    private void init() {

        mBackgroundView = new BackgroundView(getContext());
        addView(mBackgroundView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        mSlopePhoneView = new SlopePhoneView(getContext());
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(mSlopePhoneView, layoutParams);
//        mBackgroundView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//
//            @Override
//            public void onGlobalLayout() {
//                mBackgroundView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                int width = mBackgroundView.getWidth();
//                int mragins = (int) (width*0.15f);
//
//                RelativeLayout.LayoutParams mSlopePhoneViewLayoutParams =(RelativeLayout.LayoutParams) mSlopePhoneView.getLayoutParams();
//                mSlopePhoneViewLayoutParams.setMargins(mragins, mragins, mragins, mragins);
//                mSlopePhoneView.setLayoutParams(mSlopePhoneViewLayoutParams);
//            }
//        });
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public void updateScreen(float progress) {
        if (mSlopePhoneView != null && isRunning) {
            mSlopePhoneView.updateScreen(progress);
        }
    }
}
