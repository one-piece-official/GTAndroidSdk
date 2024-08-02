package com.sigmob.sdk.base;

import com.sigmob.windad.WindAdError;
import com.sigmob.windad.rewardVideo.WindRewardInfo;

public interface VideoAdListener {
    /**
     * 加载成功
     *
     * @param placementId
     */
    void onVideoAdLoadSuccess(String placementId);

    /**
     * 广告请求成功
     *
     * @param placementId
     */
    void onVideoAdPreLoadSuccess(String placementId);

    /**
     * 广告请求成功
     *
     * @param placementId
     */
    void onVideoAdPreLoadFail(String placementId);

    /**
     * 开始播放
     *
     * @param placementId
     */
    void onVideoAdPlayStart(String placementId);


    /**
     * 视频播放结束
     *
     * @param placementId
     */
    void onVideoAdPlayEnd(String placementId);

    /**
     * 广告被点击
     *
     * @param placementId
     */
    void onVideoAdClicked(String placementId);

    /**
     * 广告关闭，无论是否播放成功都会调用
     * 完成（奖励）
     *
     * @param info
     * @param placementId
     */
    void onVideoAdClosed(WindRewardInfo info, String placementId);


    /**
     * 加载广告错误回调
     *
     * @param error
     * @param placementId
     */
    void onVideoAdLoadError(WindAdError error, String placementId);


    /**
     * 播放错误回调
     *
     * @param error
     * @param placementId
     */
    void onVideoAdPlayError(WindAdError error, String placementId);

}
