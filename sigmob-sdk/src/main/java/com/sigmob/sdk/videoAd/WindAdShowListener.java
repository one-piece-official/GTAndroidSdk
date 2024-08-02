package com.sigmob.sdk.videoAd;

import com.sigmob.windad.WindAdError;

public interface WindAdShowListener {

    /**
     * 开始播放
     *
     * @param placementId
     */
    void onAdShow(String placementId);

    /**
     * 视频完整播放
     *
     * @param placementId
     */
    void onVideoAdPlayComplete(String placementId);

    /**
     * 视频播放关闭
     *
     * @param placementId
     */
    void onVideoAdPlayEnd(String placementId);

    /**
     * 广告被点击
     *
     * @param placementId
     */
    void onAdClicked(String placementId);

    /**
     * 广告关闭，无论是否播放成功都会调用
     * 完成（奖励）
     *
     * @param placementId
     */
    void onAdClosed(String placementId);

    /**
     * 播放错误回调
     *
     * @param error
     * @param placementId
     */
    void onAdShowError(WindAdError error, String placementId);

}
