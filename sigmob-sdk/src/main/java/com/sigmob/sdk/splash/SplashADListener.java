package com.sigmob.sdk.splash;

public interface SplashADListener {


    /**
     * 开屏广告加载成功
     */
    void onSplashAdLoadSuccess();

    /**
     * 开屏广告加载成功
     */
    void onSplashAdLoadFail(int error, String message);


    /**
     * 开屏广告成功展示
     */
    void onSplashAdSuccessPresentScreen();

    /**
     * 开屏广告展示失败
     */

    void onSplashAdFailToPresent(int error, String message);

    /**
     * 开屏广告被电机
     */

    void onSplashAdClicked();

    /**
     * 开屏广告被电机
     */

    void onSplashAdSkip();

    /**
     * 开屏广告关闭
     */
    void onSplashClosed();
}
