package com.sigmob.sdk.base;

import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.windad.WindAdRequest;

import java.util.Map;

public class WindVideoAdRequest extends WindAdRequest {

    public WindVideoAdRequest(String placementId, String userId, Map<String, Object> options) {
        super(placementId, userId, options);
        this.adType = AdFormat.REWARD_VIDEO;
    }

    @Deprecated
    public WindVideoAdRequest(String placementId, String userId, boolean needReward, Map<String, Object> options) {
        this(placementId, userId, options);
        this.adType = needReward ? AdFormat.REWARD_VIDEO : AdFormat.FULLSCREEN_VIDEO;
    }

}
