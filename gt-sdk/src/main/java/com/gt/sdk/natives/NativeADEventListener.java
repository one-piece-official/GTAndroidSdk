package com.gt.sdk.natives;

import com.gt.sdk.AdError;

public interface NativeADEventListener {

    void onAdExposed();

    void onAdClicked();

    void onAdDetailShow();

    void onAdDetailDismiss();

    void onAdRenderFail(AdError error);
}
