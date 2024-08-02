package com.sigmob.sdk.base.common;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.sigmob.sdk.base.common.CustomEventInterstitial.CustomEventInterstitialListener;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.IntentActions;

public class EventForwardingBroadcastReceiver extends BaseBroadcastReceiver {
    private static IntentFilter sIntentFilter;
    public BaseAdUnit mAdUnit;
    private CustomEventInterstitialListener mCustomEventInterstitialListener;

    public EventForwardingBroadcastReceiver(BaseAdUnit adUnit, CustomEventInterstitial.CustomEventInterstitialListener customEventInterstitialListener, final String broadcastIdentifier) {
        super(broadcastIdentifier);
        mAdUnit = adUnit;
        mCustomEventInterstitialListener = customEventInterstitialListener;
        getIntentFilter();
    }


    public IntentFilter getIntentFilter() {
        if (sIntentFilter == null) {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_FAIL);
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_SHOW);
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_DISMISS);
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_CLICK);
            sIntentFilter.addAction(IntentActions.ACTION_INTERSTITIAL_VOPEN);
        }
        return sIntentFilter;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mCustomEventInterstitialListener == null || mAdUnit == null) {
            return;
        }

        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();
        switch (action) {
            case IntentActions.ACTION_INTERSTITIAL_FAIL:
                String error = intent.getStringExtra("error");
                mCustomEventInterstitialListener.onInterstitialFailed(mAdUnit, error);
                break;
            case IntentActions.ACTION_INTERSTITIAL_VOPEN:
                mCustomEventInterstitialListener.onInterstitialVOpen(mAdUnit);
                break;
            case IntentActions.ACTION_INTERSTITIAL_SHOW:
                mCustomEventInterstitialListener.onInterstitialShown(mAdUnit);
                break;
            case IntentActions.ACTION_INTERSTITIAL_DISMISS:
                mCustomEventInterstitialListener.onInterstitialDismissed(mAdUnit);
                unregister(this);
                mAdUnit = null;
                break;
            case IntentActions.ACTION_INTERSTITIAL_CLICK:
                mCustomEventInterstitialListener.onInterstitialClicked(mAdUnit);
                break;
            default: {
            }
            break;
        }

    }

    @Override
    public void unregister(BroadcastReceiver broadcastReceiver) {
        super.unregister(broadcastReceiver);
        mCustomEventInterstitialListener = null;
    }
}
