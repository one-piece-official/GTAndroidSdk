package com.gt.sdk.natives;


public interface SigAdVideoStatusListener extends NativeAdData.NativeADMediaListener {

    void onVideoRestart();

    void onProgressUpdate(long current, long duration);

}
