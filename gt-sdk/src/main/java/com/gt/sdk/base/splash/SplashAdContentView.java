package com.gt.sdk.base.splash;

import android.content.Context;
import android.widget.RelativeLayout;

import com.gt.sdk.base.models.BaseAdUnit;

class SplashAdContentView extends RelativeLayout {

    protected int mDuration;

    public SplashAdContentView(Context context) {
        super(context);
    }

    public static SplashAdContentView getSplashAdContentView(Context context, BaseAdUnit adUnit) {
        if (adUnit.isVideoAd()) {
            return new SplashAdVideoContentView(context, adUnit);
        } else if (adUnit.isImageAd()) {
            return new SplashAdImageContentView(context);
        }
        return null;
    }

    public boolean loadResource(BaseAdUnit adUnit) {
        return false;
    }

    public int getDuration() {
        return mDuration;
    }

    public void showAd() {
        setVisibility(VISIBLE);
    }

    public void onPause() {

    }

    public void onResume() {

    }
}
