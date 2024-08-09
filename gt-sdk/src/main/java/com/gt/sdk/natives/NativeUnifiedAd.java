package com.gt.sdk.natives;


import android.app.Activity;
import android.os.Handler;

import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.common.utils.AdLifecycleManager;
import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.base.GtBaseAd;
import com.gt.sdk.manager.NativeAdManager;

import java.util.List;

public class NativeUnifiedAd extends GtBaseAd implements NativeAdLoadListener, AdLifecycleManager.LifecycleListener {

    private NativeUnifiedAd nativeAdListener;
    private String codeId;
    private NativeAdManager mAdManager;
    private Handler mHandler;

    public NativeUnifiedAd(AdRequest adRequest) {
        super(adRequest);
        init(adRequest, null);
    }

    public NativeUnifiedAd(AdRequest adRequest, NativeUnifiedAd adListener) {
        super(adRequest);
        init(adRequest, adListener);
    }

    private void init(AdRequest adRequest, NativeUnifiedAd adListener) {
        if (adRequest != null) {
            codeId = adRequest.getCodeId();
        }

        if (adListener != null) {
            nativeAdListener = adListener;
        }

        mAdManager = new NativeAdManager(adRequest, this);
        mHandler = mAdManager.getHandler();
    }

    public void setNativeAdListener(NativeUnifiedAd nativeAdListener) {
        this.nativeAdListener = nativeAdListener;
    }

    public boolean loadAd() {
        AdError adError = loadAdFilter();
        if (adError != null) {//过滤无效请求
            onAdError(codeId, adError);
            return false;
        }

        if (isReady()) {
            onAdLoad(codeId, null);
            return true;
        }

        AdLifecycleManager.getInstance().addLifecycleListener(this);

        adStatus = AdStatus.AdStatusLoading;

        mAdManager.loadAd();
        return true;
    }

    public boolean isReady() {
        return adStatus == AdStatus.AdStatusReady && mAdManager.isReady();
    }

    @Override
    public void onCreate(Activity activity) {

    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {
        if (mAdManager != null) {
            mAdManager.onPause(activity);
        }
    }

    @Override
    public void onResume(Activity activity) {
        if (mAdManager != null) {
            mAdManager.onResume(activity);
        }
    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {

    }

    public void destroyAd() {
        if (mAdManager != null) {
            mAdManager.destroyAd();
        }

        if (nativeAdListener != null) {
            nativeAdListener = null;
        }
    }

    @Override
    public void onAdError(String codeId, AdError error) {
        adStatus = AdStatus.AdStatusNone;
        if (nativeAdListener != null) {
            nativeAdListener.onAdError(codeId, error);
        }
    }

    @Override
    public void onAdLoad(String codeId, List<NativeAdData> adDataList) {
        adStatus = AdStatus.AdStatusReady;
        if (nativeAdListener != null) {
            nativeAdListener.onAdLoad(codeId, adDataList);
        }
    }

}
