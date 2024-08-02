package com.sigmob.sdk.nativead;

public interface SigAdVideoEventListener {
    /**
     * 视频加载完成
     */
    void onVideoLoad();

    /**
     * 视频播放失败
     */
    void onVideoError(int errorCode, int extraCode);

    /**
     * 视频开始播放回调
     */
    void onVideoAdStartPlay();

    /**
     * 视频暂停播放回调
     */
    void onVideoAdPaused();

    /**
     * 视频唤起播放回调
     */
    void onVideoAdResume();

    /**
     * 视频播放进度回调 单位为毫秒
     */
    void onProgressUpdate(long current, long duration);

    /**
     * 视频播放完成回调
     */
    void onVideoAdComplete();


}
