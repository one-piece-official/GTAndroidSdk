package com.gt.sdk.interstitial;

import android.os.Bundle;

import com.gt.sdk.base.common.LoadAdsInterstitial;
import com.gt.sdk.base.models.BaseAdUnit;

public class RewardAdInterstitial extends LoadAdsInterstitial {

    private RewardAdBroadcastReceiver adBroadcastReceiver;
    private final CustomAdListener customEventInterstitialListener;
    public VideoAdConfig mAdConfig;

    public RewardAdInterstitial(CustomAdListener adListener) {
        this.customEventInterstitialListener = adListener;
    }

    public boolean baseAdUnitValid(BaseAdUnit adUnit) {
        if (adUnit != null) {
            return adUnit.isImageAd() || adUnit.isVideoAd();
        }
        return true;
    }

    @Override
    public void showInterstitial(BaseAdUnit baseAdUnit, Bundle option) {
        mAdConfig = (VideoAdConfig) baseAdUnit.getAdConfig();
        if (customEventInterstitialListener instanceof VideoAdListener) {
            adBroadcastReceiver = new RewardAdBroadcastReceiver(baseAdUnit, (VideoAdListener) customEventInterstitialListener, baseAdUnit.getUuid());
            adBroadcastReceiver.register(adBroadcastReceiver);
        }
    }

    @Override
    public void onInvalidate(BaseAdUnit baseAdUnit) {
        if (adBroadcastReceiver != null) {
            adBroadcastReceiver.unregister(adBroadcastReceiver);
            adBroadcastReceiver = null;
        }
    }

    public interface VideoAdListener extends CustomAdListener, LandPageViewEventAdListener {

        void onAdSkip(BaseAdUnit adUnit);

        void onAdPlayEnd(BaseAdUnit adUnit);
    }

}
