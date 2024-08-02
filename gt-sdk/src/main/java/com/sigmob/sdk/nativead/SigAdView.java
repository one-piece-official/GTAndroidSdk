package com.sigmob.sdk.nativead;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class SigAdView extends RelativeLayout {

    public static final int SCREEN_NORMAL = 0;
    public static final int SCREEN_FULLSCREEN = 1;
    public static final int SCREEN_TINY = 2;
    protected SigAdStyle mAdStyle;

    public SigAdView(Context context) {
        super(context);
    }

    public SigAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SigAdView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SigVideoAdController getSigVideoAdController() {

        return null;
    }

    boolean isCtaClick(MotionEvent event) {
        return false;
    }

    public boolean onBackPressed() {
        return false;
    }

    public void destroy() {

    }

    public void reset() {

    }
}
