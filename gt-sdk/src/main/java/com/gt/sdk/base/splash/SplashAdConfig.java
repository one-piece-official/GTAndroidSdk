package com.gt.sdk.base.splash;


import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.common.BaseAdConfig;

public class SplashAdConfig extends BaseAdConfig {

    private int mShowDuration = 5;
    private boolean mClickClose = true;

    public static SplashAdConfig getAdConfig(BaseAdUnit adUnit) {
        return new SplashAdConfig(adUnit);
    }

    protected SplashAdConfig(BaseAdUnit adUnit) {
        super(adUnit);
    }

    public int getShowDuration() {
        return mShowDuration;
    }

    public boolean isClickClose() {
        return mClickClose;
    }

}
