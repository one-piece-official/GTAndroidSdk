package com.sigmob.windad.newInterstitial;

import com.sigmob.windad.WindAdError;

public interface WindNewInterstitialAdListener {

    /**
     * 加载成功
     *
     * @param placementId
     */
    void onInterstitialAdLoadSuccess(final String placementId);

    /**
     * 广告请求成功
     *
     * @param placementId
     */
    void onInterstitialAdPreLoadSuccess(final String placementId);

    /**
     * 广告请求成功
     *
     * @param placementId
     */
    void onInterstitialAdPreLoadFail(final String placementId);

    /**
     * 开始播放
     *
     * @param placementId
     */
    void onInterstitialAdShow(final String placementId);


    /**
     * 广告被点击
     *
     * @param placementId
     */
    void onInterstitialAdClicked(final String placementId);

    /**
     * 广告关闭
     *
     * @param placementId
     */
    void onInterstitialAdClosed(final String placementId);


    /**
     * 加载广告错误回调
     *
     * @param error
     * @param placementId
     */
    void onInterstitialAdLoadError(final WindAdError error, final String placementId);

    /**
     * 播放错误回调
     *
     * @param error
     * @param placementId
     */
    void onInterstitialAdShowError(final WindAdError error, final String placementId);


}