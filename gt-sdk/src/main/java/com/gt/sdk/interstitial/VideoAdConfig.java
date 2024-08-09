package com.gt.sdk.interstitial;


import com.gt.sdk.base.common.BaseAdConfig;
import com.gt.sdk.base.models.BaseAdUnit;

public class VideoAdConfig extends BaseAdConfig {

    private int mShowDuration = 5;
    private boolean mClickClose = true;

    public static VideoAdConfig getAdConfig(BaseAdUnit adUnit) {
        return new VideoAdConfig(adUnit);
    }

    protected VideoAdConfig(BaseAdUnit adUnit) {
        super(adUnit);
    }

    public int getShowDuration() {
        return mShowDuration;
    }

    public boolean isClickClose() {
        return mClickClose;
    }

}
