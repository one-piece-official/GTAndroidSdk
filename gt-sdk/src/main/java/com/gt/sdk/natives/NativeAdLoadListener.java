package com.gt.sdk.natives;

import com.gt.sdk.AdError;

import java.util.List;

public interface NativeAdLoadListener {

        void onAdError(final String codeId, final AdError error);

        void onAdLoad(final String codeId, final List<NativeAdData> adDataList);
    }