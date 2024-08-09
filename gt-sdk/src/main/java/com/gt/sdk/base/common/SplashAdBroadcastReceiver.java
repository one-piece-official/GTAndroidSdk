package com.gt.sdk.base.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.czhj.sdk.common.utils.Preconditions;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;

public class SplashAdBroadcastReceiver extends BaseBroadcastReceiver {

    private static IntentFilter sIntentFilter;

    public BaseAdUnit mAdUnit;

    private SplashAdInterstitial.SplashAdListener mAdInterstitialListener;

    public SplashAdBroadcastReceiver(BaseAdUnit baseAdUnit, SplashAdInterstitial.SplashAdListener splashAdListener, final String broadcastIdentifier) {
        super(broadcastIdentifier);
        this.mAdUnit = baseAdUnit;
        mAdInterstitialListener = splashAdListener;
        getIntentFilter();
    }

    public IntentFilter getIntentFilter() {
        if (sIntentFilter == null) {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(IntentActions.ACTION_SPLASH_PLAY);
            sIntentFilter.addAction(IntentActions.ACTION_SPLASH_CLICK);
            sIntentFilter.addAction(IntentActions.ACTION_SPLASH_CLOSE);
            sIntentFilter.addAction(IntentActions.ACTION_SPLASH_PLAY_ERROR);
            sIntentFilter.addAction(IntentActions.ACTION_LAND_PAGE_SHOW);
            sIntentFilter.addAction(IntentActions.ACTION_LAND_PAGE_DISMISS);
        }
        return sIntentFilter;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(intent);

        if (mAdInterstitialListener == null) {
            return;
        }

        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();

        if (action != null) {
            switch (action) {
                case IntentActions.ACTION_SPLASH_PLAY:
                    mAdInterstitialListener.onAdShow(mAdUnit);
                    break;
                case IntentActions.ACTION_SPLASH_CLICK:
                    mAdInterstitialListener.onAdClicked(mAdUnit);
                    break;
                case IntentActions.ACTION_SPLASH_CLOSE:
                    mAdInterstitialListener.onAdClose(mAdUnit);
                    break;
                case IntentActions.ACTION_SPLASH_PLAY_ERROR:
                    String error = intent.getStringExtra("error");
                    mAdInterstitialListener.onAdShowFailed(mAdUnit, error);
                    break;
                case IntentActions.ACTION_LAND_PAGE_SHOW:
                    mAdInterstitialListener.onLandPageShow();
                    break;
                case IntentActions.ACTION_LAND_PAGE_DISMISS:
                    mAdInterstitialListener.onLandPageClose();
                    break;
            }
        }
    }

    @Override
    public void unregister(BroadcastReceiver broadcastReceiver) {
        super.unregister(broadcastReceiver);
        mAdInterstitialListener = null;
    }
}
