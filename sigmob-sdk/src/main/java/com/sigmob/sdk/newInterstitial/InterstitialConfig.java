package com.sigmob.sdk.newInterstitial;

import com.sigmob.sdk.base.common.BaseAdConfig;
import com.sigmob.sdk.base.models.BaseAdUnit;

public class InterstitialConfig extends BaseAdConfig {
    public static InterstitialConfig getAdConfig(BaseAdUnit adUnit) {
        InterstitialConfig adConfig = new InterstitialConfig();
        adConfig.initAdConfig(adUnit);

        return adConfig;
    }

    @Override
    public void initAdConfig(BaseAdUnit adUnit) {
        super.initAdConfig(adUnit);

    }


}
