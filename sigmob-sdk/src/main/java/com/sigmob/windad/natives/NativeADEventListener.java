package com.sigmob.windad.natives;

import com.sigmob.windad.WindAdError;

public interface NativeADEventListener {

    void onAdExposed();

    void onAdClicked();

    void onAdDetailShow();

    void onAdDetailDismiss();

    void onAdError(final WindAdError error);
}
