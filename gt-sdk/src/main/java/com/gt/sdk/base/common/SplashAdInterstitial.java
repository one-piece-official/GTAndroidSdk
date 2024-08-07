package com.gt.sdk.base.common;

import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.splash.SplashAdConfig;

public class SplashAdInterstitial extends LoadAdsInterstitial {

    private SplashAdBroadcastReceiver mSplashAdBroadcastReceiver;
    private final CustomAdListener customEventInterstitialListener;
    public SplashAdConfig mAdConfig;

    public SplashAdInterstitial(CustomAdListener customEventInterstitialListener) {
        this.customEventInterstitialListener = customEventInterstitialListener;
    }

    public boolean baseAdUnitValid(BaseAdUnit adUnit) {
//        MaterialMeta material = adUnit.getMaterial();
//        if (TextUtils.isEmpty(adUnit.getCrid()) || (TextUtils.isEmpty(material.video_url) && TextUtils.isEmpty(material.image_src))) {
//            return false;
//        }
        return true;
    }

    @Override
    public void showInterstitial(BaseAdUnit baseAdUnit) {
        mAdConfig = (SplashAdConfig) baseAdUnit.getAdConfig();
        if (customEventInterstitialListener instanceof SplashAdListener) {
            mSplashAdBroadcastReceiver = new SplashAdBroadcastReceiver(baseAdUnit, (SplashAdListener) customEventInterstitialListener, baseAdUnit.getUuid());
            mSplashAdBroadcastReceiver.register(mSplashAdBroadcastReceiver);
        }
    }

    @Override
    public void onInvalidate(BaseAdUnit baseAdUnit) {
        if (mSplashAdBroadcastReceiver != null) {
            mSplashAdBroadcastReceiver.unregister(mSplashAdBroadcastReceiver);
            mSplashAdBroadcastReceiver = null;
        }
    }

    public interface SplashAdListener extends CustomAdListener, LandPageViewEventAdListener {

    }

}
