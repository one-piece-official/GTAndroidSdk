package com.gt.sdk.api;

import com.gt.sdk.AdError;

public interface SplashAdListener {

    /**
     * 开屏广告成功加载
     */
    void onSplashAdLoadSuccess(final String placementId);


    /**
     * 开屏广告加载失败
     */
    void onSplashAdLoadFail(final String codeId, final AdError error);

    /**
     * 开屏广告成功展示
     */
    void onSplashAdShow(final String codeId);

    /**
     * 开屏广告展示失败
     */
    void onSplashAdShowError(final String codeId, final AdError error);

    /**
     * 开屏广告被点击
     */

    void onSplashAdClick(final String codeId);

    /**
     * 开屏广告关闭
     */
    void onSplashAdClose(final String codeId);

}
