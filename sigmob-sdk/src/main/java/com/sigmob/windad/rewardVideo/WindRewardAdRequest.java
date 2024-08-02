package com.sigmob.windad.rewardVideo;

import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.windad.WindAdRequest;

import java.util.Map;

public class WindRewardAdRequest extends WindAdRequest {
    private boolean enableScreenLockDisplayAd;
    private boolean enableKeepOn;

    public WindRewardAdRequest(String placementId, String userId, Map<String, Object> options) {
        super(placementId, userId, options);
        adType = AdFormat.REWARD_VIDEO;
    }

    public static WindRewardAdRequest getWindVideoAdRequest(WindAdRequest adRequest) {
        if (adRequest != null) {
            return new WindRewardAdRequest(adRequest.getPlacementId(), adRequest.getUserId(), adRequest.getOptions());
        }
        return null;
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
