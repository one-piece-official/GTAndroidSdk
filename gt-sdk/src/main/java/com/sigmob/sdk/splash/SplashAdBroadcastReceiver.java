package com.sigmob.sdk.splash;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.sigmob.sdk.base.common.BaseBroadcastReceiver;
import com.sigmob.sdk.base.models.IntentActions;
import com.czhj.sdk.common.utils.Preconditions;

public class SplashAdBroadcastReceiver extends BaseBroadcastReceiver {
    private static IntentFilter sIntentFilter;


    private SplashAdInterstitial.SplashAdInterstitialListener mSplashAdInterstitialListener;

    public SplashAdBroadcastReceiver(
            SplashAdInterstitial.SplashAdInterstitialListener splashAdInterstitialListener,
            final String broadcastIdentifier) {
        super(broadcastIdentifier);
        mSplashAdInterstitialListener = splashAdInterstitialListener;
        getIntentFilter();
    }


    public IntentFilter getIntentFilter() {
        if (sIntentFilter == null) {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(IntentActions.ACTION_SPLAH_STOP_TIME);
            sIntentFilter.addAction(IntentActions.ACTION_SPLAH_PLAYFAIL);
            sIntentFilter.addAction(IntentActions.ACTION_SPLAH_SKIP);
            sIntentFilter.addAction(IntentActions.ACTION_LANDPAGE_SHOW);
            sIntentFilter.addAction(IntentActions.ACTION_LANDPAGE_DISMISS);


        }
        return sIntentFilter;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(intent);

        if (mSplashAdInterstitialListener == null) {
            return;
        }

        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();

        switch (action) {
            case IntentActions.ACTION_SPLAH_PLAY:
                mSplashAdInterstitialListener.onAdPlay();
                break;
            case IntentActions.ACTION_SPLAH_PLAYFAIL:
                mSplashAdInterstitialListener.onAdPlayFail();
                break;
            case IntentActions.ACTION_SPLAH_SKIP:
                mSplashAdInterstitialListener.onAdSkip();
                break;
            case IntentActions.ACTION_SPLAH_STOP_TIME:
                mSplashAdInterstitialListener.onStopTime();
                break;
            case IntentActions.ACTION_LANDPAGE_SHOW:
                mSplashAdInterstitialListener.onLandPageShow();
                break;
            case IntentActions.ACTION_LANDPAGE_DISMISS:
                mSplashAdInterstitialListener.onLandPageClose();
                break;
        }
    }

    @Override
    public void unregister(BroadcastReceiver broadcastReceiver) {
        super.unregister(broadcastReceiver);
        mSplashAdInterstitialListener = null;
    }
}
