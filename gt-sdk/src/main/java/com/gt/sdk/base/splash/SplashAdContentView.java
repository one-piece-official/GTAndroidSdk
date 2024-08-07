package com.gt.sdk.base.splash;

import android.content.Context;
import android.widget.RelativeLayout;

import com.sigmob.sdk.base.common.CreativeType;
import com.sigmob.sdk.base.models.BaseAdUnit;

class SplashAdContentView extends RelativeLayout {
    protected int mDuration;

    public SplashAdContentView(Context context) {
        super(context);
    }

    public static SplashAdContentView getSplashAdContentView(Context context, BaseAdUnit adUnit) {

        if (adUnit.getMaterial().creative_type == null) return null;

        if (adUnit.getMaterial().creative_type == CreativeType.CreativeTypeSplashVideo.getCreativeType()) {
            return new SplashAdVideoContentView(context, adUnit);
        } else {
            return new SplashAdImageContentView(context);
        }
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
