package com.gt.sdk.natives;

import android.content.Context;

import com.gt.sdk.base.models.BaseAdUnit;

public class SigNativeAdView extends SigAdView {

    SigmobNativeAdRenderListener mNativeAdRenderListener;

    public SigNativeAdView(Context context) {
        super(context);
    }

    public void init(SigmobNativeAdRenderListener listener) {
        mNativeAdRenderListener = listener;
    }

    public NativeAdData getNativeAdUnit() {
        NativeAdData nativeAdUnit = null;
        if (mNativeAdRenderListener != null) {
            nativeAdUnit = mNativeAdRenderListener.getNativeAdUnit();
        }
        return nativeAdUnit;
    }

    public BaseAdUnit getAdUnit() {
        BaseAdUnit adUnit = null;
        if (mNativeAdRenderListener != null) {
            adUnit = mNativeAdRenderListener.getAdUnit();
        }
        return adUnit;
    }

    public NativeAdConfig getAdConfig() {

        NativeAdConfig adConfig = null;
        if (mNativeAdRenderListener != null) {
            adConfig = mNativeAdRenderListener.getAdConfig();
        }
        return adConfig;
    }

    public SigAppInfoView getAppInfoView() {
        SigAppInfoView appInfo = null;
        if (mNativeAdRenderListener != null) {
            appInfo = mNativeAdRenderListener.getAppInfoView();
        }
        return appInfo;
    }

    public void onResume() {

    }

    public void onPaused() {

    }

    public double getVideoDuration() {
        return 0;
    }

    public double getVideoProgress() {
        return 0;
    }

    public void setUIStyle(SigAdStyle style) {
        mAdStyle = style;
    }


    public void animateFinish() {
    }

    @Override
    public void destroy() {
        super.destroy();
        mNativeAdRenderListener = null;
    }
}
