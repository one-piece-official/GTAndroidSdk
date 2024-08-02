package com.sigmob.sdk.splash;

import android.os.Bundle;
import android.text.TextUtils;

import com.sigmob.sdk.base.common.LoadAdsInterstitial;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;

import java.util.Map;

class SplashAdInterstitial extends LoadAdsInterstitial {

    SplashAdConfig mAdconfig;
    private SplashAdBroadcastReceiver mSplashAdBroadcastReceiver;


    protected SplashAdInterstitial(CustomEventInterstitialListener customEventInterstitialListener) {
        super(customEventInterstitialListener);
    }

    public static boolean checkAdValid(BaseAdUnit adUnit) {

        MaterialMeta material = adUnit.getMaterial();
        if (TextUtils.isEmpty(adUnit.getCrid()) ||
                (TextUtils.isEmpty(material.video_url) && TextUtils.isEmpty(material.image_src))) {
            return false;
        }
        return true;

    }

    protected boolean baseAdUnitValid(BaseAdUnit adUnit) {
        return checkAdValid(adUnit);
    }

    @Override
    protected void preloadAds(CustomEventInterstitialListener customEventInterstitialListener) {
        mCustomEventInterstitialListener = customEventInterstitialListener;
    }

    @Override
    public void showInterstitial(BaseAdUnit baseAdUnit, Bundle options) {

        if (baseAdUnit == null) {
            baseAdUnit = mLoadAdUnit;
        }
        mAdconfig = (SplashAdConfig) baseAdUnit.getAdConfig();

        super.showInterstitial(baseAdUnit, options);
        if (mCustomEventInterstitialListener instanceof SplashAdInterstitialListener) {
            mSplashAdBroadcastReceiver = new SplashAdBroadcastReceiver((SplashAdInterstitialListener) mCustomEventInterstitialListener,
                    baseAdUnit.getUuid());
            mSplashAdBroadcastReceiver.register(mSplashAdBroadcastReceiver);
        }

    }

    @Override
    public void loadInterstitial(Map<String, Object> localExtras, BaseAdUnit adUnit) {
        super.loadInterstitial(localExtras, adUnit);

    }

    @Override
    public void onInvalidate(BaseAdUnit baseAdUnit) {
        super.onInvalidate(baseAdUnit);

        if (mSplashAdBroadcastReceiver != null) {
            mSplashAdBroadcastReceiver.unregister(mSplashAdBroadcastReceiver);
            mSplashAdBroadcastReceiver = null;
        }
    }

    interface SplashAdInterstitialListener extends CustomEventInterstitialListener, LandPageViewEventInterstitialListener {
        void onAdPlayFail();

        void onAdSkip();

        void onAdPlay();

        void onStopTime();


    }


}
