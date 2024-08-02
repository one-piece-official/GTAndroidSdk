package com.sigmob.sdk.videoAd;

import com.sigmob.windad.rewardVideo.WindRewardInfo;

public interface WindAdRewardListener {
    void onVideoAdRewarded(WindRewardInfo rewardInfo, String placementId);

}
