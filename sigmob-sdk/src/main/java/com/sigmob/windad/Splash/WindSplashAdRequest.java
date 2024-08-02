package com.sigmob.windad.Splash;

import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.windad.WindAdRequest;

import java.util.Map;

public class WindSplashAdRequest extends WindAdRequest {

    private int fetchDelay = 5;
    private boolean disableAutoHideAd = false;


    public WindSplashAdRequest(String placementId, String userId, Map<String, Object> options) {
        super(placementId, userId, options);
        adType = AdFormat.SPLASH;
    }

    public int getFetchDelay() {
        if (fetchDelay <3){
            return 3;
        }
        return fetchDelay;
    }

    public void setFetchDelay(int fetchDelay) {
        this.fetchDelay = fetchDelay;
    }

    public boolean isDisableAutoHideAd() {
        return disableAutoHideAd;
    }

    public void setDisableAutoHideAd(boolean disableAutoHideAd) {
        this.disableAutoHideAd = disableAutoHideAd;
    }

}
