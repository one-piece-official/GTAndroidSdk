package com.gt.sdk.base.common;

import android.os.Bundle;

import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.splash.SplashAdConfig;

public class SplashAdInterstitial extends LoadAdsInterstitial {

    private SplashAdBroadcastReceiver mSplashAdBroadcastReceiver;
    private final CustomAdListener customEventInterstitialListener;
    public SplashAdConfig mAdConfig;

    public SplashAdInterstitial(CustomAdListener customAdListener) {
        this.customEventInterstitialListener = customAdListener;
    }

    public boolean baseAdUnitValid(BaseAdUnit adUnit) {
        if (adUnit != null) {
            return adUnit.isImageAd() || adUnit.isVideoAd();
        }
        return true;
    }

    @Override
    public void showInterstitial(BaseAdUnit baseAdUnit, Bundle option) {
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
