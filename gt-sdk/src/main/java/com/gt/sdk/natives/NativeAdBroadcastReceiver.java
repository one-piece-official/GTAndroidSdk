package com.gt.sdk.natives;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.czhj.sdk.common.utils.Preconditions;
import com.gt.sdk.base.common.BaseBroadcastReceiver;
import com.gt.sdk.base.models.IntentActions;

public class NativeAdBroadcastReceiver extends BaseBroadcastReceiver {

    private static IntentFilter sIntentFilter;

    private NativeAdInterstitial.NativeAdInterstitialListener adInterstitialListener;

    public NativeAdBroadcastReceiver(NativeAdInterstitial.NativeAdInterstitialListener adInterstitialListener, final String broadcastIdentifier) {
        super(broadcastIdentifier);
        this.adInterstitialListener = adInterstitialListener;
        getIntentFilter();
    }

    public IntentFilter getIntentFilter() {
        if (sIntentFilter == null) {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(IntentActions.ACTION_NATIVE_TEMPLE_SHOW);
            sIntentFilter.addAction(IntentActions.ACTION_NATIVE_TEMPLE_CLICK);
            sIntentFilter.addAction(IntentActions.ACTION_NATIVE_TEMPLE_DISMISS);
            sIntentFilter.addAction(IntentActions.ACTION_LAND_PAGE_SHOW);
            sIntentFilter.addAction(IntentActions.ACTION_LAND_PAGE_DISMISS);
        }
        return sIntentFilter;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(intent);

        if (adInterstitialListener == null) {
            return;
        }

        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();

        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case IntentActions.ACTION_NATIVE_TEMPLE_SHOW:
                    adInterstitialListener.onAdDetailShow();
                    break;
                case IntentActions.ACTION_NATIVE_TEMPLE_CLICK:
                    adInterstitialListener.onAdDetailClick();
                    break;
                case IntentActions.ACTION_NATIVE_TEMPLE_DISMISS:
                    adInterstitialListener.onAdDetailDismiss();
                    break;
                case IntentActions.ACTION_LAND_PAGE_SHOW:
                    adInterstitialListener.onLandPageShow();
                    break;
                case IntentActions.ACTION_LAND_PAGE_DISMISS:
                    adInterstitialListener.onLandPageClose();
                    break;
            }
        }
    }

    @Override
    public void unregister(BroadcastReceiver broadcastReceiver) {
        super.unregister(broadcastReceiver);
        adInterstitialListener = null;
    }
}
