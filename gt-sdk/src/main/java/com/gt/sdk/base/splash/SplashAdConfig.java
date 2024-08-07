package com.gt.sdk.base.splash;

import android.content.Context;

import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.common.utils.TouchLocation;
import com.gt.sdk.base.BaseAdUnit;
import com.gt.sdk.base.common.BaseAdConfig;

public class SplashAdConfig extends BaseAdConfig {

    private int mShowDuration = 5;
    private boolean mClickClose;

    public static SplashAdConfig getAdConfig(BaseAdUnit adUnit) {
        SplashAdConfig adConfig = new SplashAdConfig();
        adConfig.initAdConfig(adUnit);
        return adConfig;
    }

    @Override
    public void initAdConfig(BaseAdUnit adUnit) {
        super.initAdConfig(adUnit);
        SplashAdSetting splashAdSetting = adUnit.getSplashAdSetting();
        if (splashAdSetting != null) {
            mShowDuration = splashAdSetting.show_duration;
            mClickClose = splashAdSetting.enable_close_on_click;

        }
    }

    public int getShowDuration() {
        return mShowDuration;
    }

    public boolean isClickClose() {
        return mClickClose;
    }

    @Override
    public void handleClose(Context context, int contentPlayHead, BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

        PointEntitySigmobUtils.eventRecord(PointCategory.CLOSE, null, adUnit);

        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_CLOSE);
    }

    @Override
    public void handleSkip(Context context, int contentPlayHead, BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

        PointEntitySigmobUtils.eventRecord(PointCategory.SKIP, null, adUnit);

        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_SKIP);
    }

    @Override
    public void handleImpression(Context context, int contentPlayHead, BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

        PointEntitySigmobUtils.SigmobTracking(PointCategory.START, null, adUnit, null);

        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_START);
    }


    @Override
    public void handleClick(final Context context, TouchLocation down, TouchLocation up, ClickUIType clickUIType, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_CLICK);

    }
}
