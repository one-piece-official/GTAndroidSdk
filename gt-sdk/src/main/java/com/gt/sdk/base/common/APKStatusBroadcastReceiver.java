package com.gt.sdk.base.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.utils.Preconditions;
import com.gt.sdk.base.models.IntentActions;

public class APKStatusBroadcastReceiver extends BaseBroadcastReceiver {
    CustomEventAd.APKStatusEventAdListener mApkStatusEventInterstitialListener;
    IntentFilter sIntentFilter;

    public APKStatusBroadcastReceiver(CustomEventAd.APKStatusEventAdListener apkStatusEventInterstitialListener, final String broadcastIdentifier) {
        super(broadcastIdentifier);
        mApkStatusEventInterstitialListener = apkStatusEventInterstitialListener;
    }

    public IntentFilter getIntentFilter() {
        if (sIntentFilter == null) {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(IntentActions.ACTION_AD_DOWNLOAD_START);
            sIntentFilter.addAction(IntentActions.ACTION_AD_DOWNLOAD_PAUSE);
            sIntentFilter.addAction(IntentActions.ACTION_AD_DOWNLOAD_END);
            sIntentFilter.addAction(IntentActions.ACTION_AD_INSTALL_START);
            sIntentFilter.addAction(IntentActions.ACTION_AD_INSTALL_END);
        }
        return sIntentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(intent);

        if (mApkStatusEventInterstitialListener == null) {
            return;
        }

        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();

        boolean result = Constants.SUCCESS.equalsIgnoreCase(intent.getStringExtra("result"));
        long downloadId = intent.getLongExtra("downloadId", -1);

        switch (action) {
            case IntentActions.ACTION_AD_DOWNLOAD_START:
                mApkStatusEventInterstitialListener.onInterstitialDownloadAPKStart(result, downloadId);
                break;
            case IntentActions.ACTION_AD_DOWNLOAD_PAUSE:
                mApkStatusEventInterstitialListener.onInterstitialDownloadAPKPause(result, downloadId);
                break;
            case IntentActions.ACTION_AD_DOWNLOAD_END:
                mApkStatusEventInterstitialListener.onInterstitialDownloadAPKEnd(result, downloadId);
                break;
            case IntentActions.ACTION_AD_INSTALL_START:
                mApkStatusEventInterstitialListener.onInterstitialInstallAPKStart(result);
                break;
            case IntentActions.ACTION_AD_INSTALL_END:
                mApkStatusEventInterstitialListener.onInterstitialInstallAPKEnd(result);
                break;
        }

    }

    @Override
    public void unregister(BroadcastReceiver broadcastReceiver) {
        super.unregister(broadcastReceiver);
        mApkStatusEventInterstitialListener = null;
    }
}
