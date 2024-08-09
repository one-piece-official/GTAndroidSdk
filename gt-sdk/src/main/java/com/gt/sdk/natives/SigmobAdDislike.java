package com.gt.sdk.natives;


/**
 * created by lance on   2021/8/9 : 12:33 PM
 */
public interface SigmobAdDislike {

    void showDislikeDialog();

    void setDislikeInteractionCallback(NativeAdData.DislikeInteractionCallback callback);

}
