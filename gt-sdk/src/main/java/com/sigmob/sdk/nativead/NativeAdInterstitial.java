package com.sigmob.sdk.nativead;

import android.os.Bundle;

import com.czhj.volley.toolbox.DownloadItem;
import com.czhj.volley.toolbox.FileDownloadRequest;
import com.czhj.volley.toolbox.FileDownloader;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.common.AdActivity;
import com.sigmob.sdk.base.common.DownloaderFactory;
import com.sigmob.sdk.base.common.LoadAdsInterstitial;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.videoAd.BaseAdActivity;

import java.io.File;
import java.util.Map;

public class NativeAdInterstitial extends LoadAdsInterstitial {

    private NativeAdBroadcastReceiver mNativeBroadcastReceiver;

    protected NativeAdInterstitial(CustomEventInterstitialListener customEventInterstitialListener) {
        super(customEventInterstitialListener);
    }

    private static boolean nativeTypeCheck(MaterialMeta material) {

        return true;
    }

    protected boolean baseAdUnitValid(BaseAdUnit adUnit) {

        MaterialMeta materialMeta = adUnit.getMaterial();
        if (materialMeta != null) {
            return nativeTypeCheck(adUnit.getMaterial());
        }
        return false;
    }

    @Override
    public void loadInterstitial(Map<String, Object> localExtras, BaseAdUnit adUnit) {
        super.loadInterstitial(localExtras, adUnit);
    }

    @Override
    protected void preloadAds(CustomEventInterstitialListener customEventInterstitialListener) {
        File file = mLoadAdUnit.getAdPrivacyTemplateFile();
        if (file != null) {
            if (!file.exists()) {
                DownloadItem downloadItem = new DownloadItem();
                downloadItem.url = mLoadAdUnit.getadPrivacy().privacy_template_url;
                downloadItem.filePath = file.getAbsolutePath();
                downloadItem.type = DownloadItem.FileType.OTHER;
                FileDownloader downloader = DownloaderFactory.getDownloader();
                if (downloader != null) {
                    downloader.add(downloadItem, new FileDownloadRequest.FileDownloadListener() {
                        @Override
                        public void onSuccess(DownloadItem item) {

                        }

                        @Override
                        public void onCancel(DownloadItem item) {

                        }

                        @Override
                        public void onErrorResponse(DownloadItem item) {

                        }

                        @Override
                        public void downloadProgress(DownloadItem item, long totalSize, long readSize) {

                        }
                    });
                }
            }
        }
    }

    @Override
    public void showInterstitial(BaseAdUnit baseAdUnit, Bundle option) {
        super.showInterstitial(baseAdUnit, option);
        if (mNativeBroadcastReceiver == null && mCustomEventInterstitialListener instanceof NativeAdInterstitialListener) {
            mNativeBroadcastReceiver = new NativeAdBroadcastReceiver((NativeAdInterstitialListener) mCustomEventInterstitialListener,
                    baseAdUnit.getUuid());
            mNativeBroadcastReceiver.register(mNativeBroadcastReceiver);
        }
        AdActivity.startActivity(SDKContext.getApplicationContext(), AdActivity.class, baseAdUnit.getUuid(), option, BaseAdActivity.LANDNATIVE);
    }

    @Override
    public void onInvalidate(BaseAdUnit baseAdUnit) {
        if (mNativeBroadcastReceiver != null) {
            mNativeBroadcastReceiver.unregister(mNativeBroadcastReceiver);
            mNativeBroadcastReceiver = null;
        }
        super.onInvalidate(baseAdUnit);
    }


    interface NativeAdInterstitialListener extends CustomEventInterstitialListener, LandPageViewEventInterstitialListener {

        void onAdDetailShow();

        void onAdDetailClick();

        void onAdDetailDismiss();

    }

}
