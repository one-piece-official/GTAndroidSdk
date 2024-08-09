package com.gt.sdk.natives;

import android.os.Bundle;

import com.gt.sdk.GtAdSdk;
import com.gt.sdk.base.activity.AdActivity;
import com.gt.sdk.base.activity.BaseAdActivity;
import com.gt.sdk.base.common.LoadAdsInterstitial;
import com.gt.sdk.base.models.BaseAdUnit;


public class NativeAdInterstitial extends LoadAdsInterstitial {

    private NativeAdBroadcastReceiver mNativeBroadcastReceiver;
    private final CustomAdListener customEventInterstitialListener;
    public NativeAdConfig mAdConfig;

    public NativeAdInterstitial(CustomAdListener customAdListener) {
        this.customEventInterstitialListener = customAdListener;
    }

    public boolean baseAdUnitValid(BaseAdUnit adUnit) {
        if (adUnit != null) {
            return adUnit.isImageAd() || adUnit.isVideoAd();
        }
        return false;
    }

    @Override
    protected void showInterstitial(BaseAdUnit baseAdUnit, Bundle option) {
        mAdConfig = (NativeAdConfig) baseAdUnit.getAdConfig();
        if (mNativeBroadcastReceiver == null && customEventInterstitialListener instanceof NativeAdInterstitialListener) {
            mNativeBroadcastReceiver = new NativeAdBroadcastReceiver((NativeAdInterstitialListener) customEventInterstitialListener, baseAdUnit.getUuid());
            mNativeBroadcastReceiver.register(mNativeBroadcastReceiver);
        }
        AdActivity.startActivity(GtAdSdk.sharedAds().getContext(), AdActivity.class, baseAdUnit.getUuid(), option, BaseAdActivity.LANDNATIVE);
    }

    @Override
    public void onInvalidate(BaseAdUnit baseAdUnit) {
        if (mNativeBroadcastReceiver != null) {
            mNativeBroadcastReceiver.unregister(mNativeBroadcastReceiver);
            mNativeBroadcastReceiver = null;
        }
    }

    public interface NativeAdInterstitialListener extends CustomAdListener, LandPageViewEventAdListener {

        void onAdDetailShow();

        void onAdDetailClick();

        void onAdDetailDismiss();

    }

}
