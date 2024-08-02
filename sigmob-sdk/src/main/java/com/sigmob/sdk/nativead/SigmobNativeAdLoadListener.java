package com.sigmob.sdk.nativead;

import com.sigmob.windad.natives.WindNativeAdData;

import java.util.List;

public interface SigmobNativeAdLoadListener {

    void onNativeAdLoaded(List<WindNativeAdData> adUnits);

    void onNativeAdLoadFail(int error_code, String error_message);
}
