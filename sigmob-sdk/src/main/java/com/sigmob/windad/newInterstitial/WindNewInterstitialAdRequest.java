package com.sigmob.windad.newInterstitial;

import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.windad.WindAdRequest;

import java.util.Map;


public class WindNewInterstitialAdRequest extends WindAdRequest {

    private boolean enableScreenLockDisplayAd;
    private boolean enableKeepOn;

    public WindNewInterstitialAdRequest(String placementId, String userId, Map<String, Object> options) {
        super(placementId, userId, options);
        adType = AdFormat.NEW_INTERSTITIAL;
    }

    public boolean isEnableScreenLockDisPlayAd() {
        return enableScreenLockDisplayAd;
    }

    public void setEnableScreenLockDisPlayAd(boolean enableScreenLockDisPlayAd) {
        this.enableScreenLockDisplayAd = enableScreenLockDisPlayAd;
    }

    public boolean isEnableKeepOn() {
        return enableKeepOn;
    }

    public void setEnableKeepOn(boolean enableKeepOn) {
        this.enableKeepOn = enableKeepOn;
    }


}
