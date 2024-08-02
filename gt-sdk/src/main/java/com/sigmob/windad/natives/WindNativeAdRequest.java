package com.sigmob.windad.natives;

import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.windad.WindAdRequest;

import java.util.Map;

public class WindNativeAdRequest extends WindAdRequest {

    @Deprecated
    public WindNativeAdRequest(String placementId, String userId, int adCount, Map<String, Object> options) {
        super(placementId, userId, options);
        this.adCount = adCount;
        adType = AdFormat.UNIFIED_NATIVE;
    }
    public WindNativeAdRequest(String placementId, String userId,  Map<String, Object> options) {
        super(placementId, userId, options);
        adType = AdFormat.UNIFIED_NATIVE;
    }
    public static WindNativeAdRequest getWindVideoAdRequest(WindAdRequest adRequest) {
        if (adRequest != null) {
            return new WindNativeAdRequest(adRequest.getPlacementId(), adRequest.getUserId(), 1, adRequest.getOptions());
        }
        return null;
    }

}
