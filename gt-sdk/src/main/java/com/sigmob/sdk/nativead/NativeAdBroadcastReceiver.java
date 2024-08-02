package com.sigmob.sdk.nativead;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.sigmob.sdk.base.common.BaseBroadcastReceiver;
import com.sigmob.sdk.base.models.IntentActions;
import com.czhj.sdk.common.utils.Preconditions;

public class NativeAdBroadcastReceiver extends BaseBroadcastReceiver {
    private static IntentFilter sIntentFilter;

    private NativeAdInterstitial.NativeAdInterstitialListener mNativeAdInterstitialListener;

    public NativeAdBroadcastReceiver(
            NativeAdInterstitial.NativeAdInterstitialListener nativeAdInterstitialListener,
            final String broadcastIdentifier) {
        super(broadcastIdentifier);
        mNativeAdInterstitialListener = nativeAdInterstitialListener;
        getIntentFilter();
    }


    public IntentFilter getIntentFilter() {
        if (sIntentFilter == null) {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(IntentActions.ACTION_NATIVE_TEMPLIE_SHOW);
            sIntentFilter.addAction(IntentActions.ACTION_NATIVE_TEMPLE_CLICK);
            sIntentFilter.addAction(IntentActions.ACTION_NATIVE_TEMPLE_DISMISS);
            sIntentFilter.addAction(IntentActions.ACTION_LANDPAGE_SHOW);
            sIntentFilter.addAction(IntentActions.ACTION_LANDPAGE_DISMISS);

        }
        return sIntentFilter;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(intent);

        if (mNativeAdInterstitialListener == null) {
            return;
        }

        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();

        switch (action) {
            case IntentActions.ACTION_NATIVE_TEMPLIE_SHOW:
                mNativeAdInterstitialListener.onAdDetailShow();
                break;
            case IntentActions.ACTION_NATIVE_TEMPLE_CLICK:
                mNativeAdInterstitialListener.onAdDetailClick();
                break;
            case IntentActions.ACTION_NATIVE_TEMPLE_DISMISS:
                mNativeAdInterstitialListener.onAdDetailDismiss();
                break;
            case IntentActions.ACTION_LANDPAGE_SHOW:
                mNativeAdInterstitialListener.onLandPageShow();
                break;
            case IntentActions.ACTION_LANDPAGE_DISMISS:
                mNativeAdInterstitialListener.onLandPageClose();
                break;

        }
    }

    @Override
    public void unregister(BroadcastReceiver broadcastReceiver) {
        super.unregister(broadcastReceiver);
        mNativeAdInterstitialListener = null;
    }


}
