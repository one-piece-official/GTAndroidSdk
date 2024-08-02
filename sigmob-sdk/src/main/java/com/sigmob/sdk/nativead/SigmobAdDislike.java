package com.sigmob.sdk.nativead;

import com.sigmob.windad.natives.WindNativeAdData;

/**
 * created by lance on   2021/8/9 : 12:33 PM
 */
public interface SigmobAdDislike {

    void showDislikeDialog();

    void setDislikeInteractionCallback(WindNativeAdData.DislikeInteractionCallback callback);

}
