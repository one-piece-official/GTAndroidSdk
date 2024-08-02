package com.sigmob.sdk.base.common;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.sigmob.sdk.base.models.BaseAdUnit;


public class ViewGestureDetector extends GestureDetector {
    private final View mView;

    private AdAlertGestureListener mAdAlertGestureListener;

    public ViewGestureDetector( Context context,  View view,  BaseAdUnit adUnit)  {
        this(context, view, new AdAlertGestureListener(view, adUnit));
    }

    private ViewGestureDetector(Context context, View view,  AdAlertGestureListener adAlertGestureListener) {
        super(context, adAlertGestureListener);

        mAdAlertGestureListener = adAlertGestureListener;
        mView = view;

        setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        final boolean result = super.onTouchEvent(motionEvent);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_UP:
                mAdAlertGestureListener.finishGestureDetection();
                break;

            case MotionEvent.ACTION_MOVE:
                if (!isMotionEventInView(motionEvent, mView)) {
                    resetAdFlaggingGesture();
                }
                break;

            default:
                break;
        }
        return result;
    }

    void resetAdFlaggingGesture() {
        mAdAlertGestureListener.reset();
    }

    private boolean isMotionEventInView(MotionEvent motionEvent, View view) {
        if (motionEvent == null || view == null) {
            return false;
        }

        float x = motionEvent.getX();
        float y = motionEvent.getY();

        return (x >= 0 && x <= view.getWidth())
                && (y >= 0 && y <= view.getHeight());
    }

    public void onResetUserClick() {
        mAdAlertGestureListener.onResetUserClick();
    }

    public boolean isClicked() {
        return mAdAlertGestureListener.isClicked();
    }

}
