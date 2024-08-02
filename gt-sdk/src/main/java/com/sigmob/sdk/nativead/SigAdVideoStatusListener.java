package com.sigmob.sdk.nativead;

import com.sigmob.windad.natives.WindNativeAdData;

public interface SigAdVideoStatusListener extends WindNativeAdData.NativeADMediaListener {
    void onVideoRestart();
    void onProgressUpdate(long current, long duration);

}
