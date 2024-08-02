package com.sigmob.windad.rewardVideo;

import com.sigmob.windad.WindAdError;

public interface WindRewardVideoAdListener {

    /**
     * 加载成功
     *
     * @param placementId
     */
    void onRewardAdLoadSuccess(final String placementId);

    /**
     * 广告请求成功
     *
     * @param placementId
     */
    void onRewardAdPreLoadSuccess(final String placementId);

    /**
     * 广告请求成功
     *
     * @param placementId
     */
    void onRewardAdPreLoadFail(final String placementId);

    /**
     * 开始播放
     *
     * @param placementId
     */
    void onRewardAdPlayStart(final String placementId);


    /**
     * 视频播放结束
     *
     * @param placementId
     */
    void onRewardAdPlayEnd(final String placementId);

    /**
     * 广告被点击
     *
     * @param placementId
     */
    void onRewardAdClicked(final String placementId);

    /**
     * 广告关闭，无论是否播放成功都会调用
     * 完成（奖励）
     *
     * @param placementId
     */
    void onRewardAdClosed(final String placementId);

    /**
     * 广告奖励回调
     */
    void onRewardAdRewarded(final WindRewardInfo rewardInfo, final String placementId);
    
    /**
     * 加载广告错误回调
     *
     * @param error
     * @param placementId
     */
    void onRewardAdLoadError(final WindAdError error, final String placementId);


    /**
     * 播放错误回调
     *
     * @param error
     * @param placementId
     */
    void onRewardAdPlayError(final WindAdError error, final String placementId);


}