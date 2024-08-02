package com.sigmob.sdk.base.common;

import android.os.Bundle;

import com.sigmob.sdk.base.models.BaseAdUnit;

import java.util.Map;


public abstract class LoadAdsInterstitial extends CustomEventInterstitial {

    protected CustomEventInterstitialListener mCustomEventInterstitialListener = null;
    protected BaseAdUnit mLoadAdUnit;
    private EventForwardingBroadcastReceiver mBroadcastReceiver;

    protected LoadAdsInterstitial(CustomEventInterstitialListener customEventInterstitialListener) {
        mCustomEventInterstitialListener = customEventInterstitialListener;
    }

    abstract protected void preloadAds(CustomEventInterstitialListener customEventInterstitialListener);

    protected abstract boolean baseAdUnitValid(BaseAdUnit adUnit);

    public void loadInterstitial(
            Map<String, Object> localExtras,
            BaseAdUnit adUnit) {

        mLoadAdUnit = adUnit;

        preloadAds(mCustomEventInterstitialListener);
    }


    public void showInterstitial(BaseAdUnit baseAdUnit, Bundle option) {
        mBroadcastReceiver = new EventForwardingBroadcastReceiver(baseAdUnit, mCustomEventInterstitialListener, baseAdUnit.getUuid());
        mBroadcastReceiver.register(mBroadcastReceiver);
    }

    public void onInvalidate(BaseAdUnit baseAdUnit) {
        if (baseAdUnit != null){
            AdStackManager.cleanPlayAdUnit(baseAdUnit);
            baseAdUnit.destroy();
        }
        mLoadAdUnit = null;
        if (mBroadcastReceiver != null) {
            mBroadcastReceiver.unregister(mBroadcastReceiver);
        }
    }

}
