package com.sigmob.sdk.newInterstitial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.czhj.sdk.common.utils.Preconditions;
import com.sigmob.sdk.base.common.BaseBroadcastReceiver;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.IntentActions;

public class NewInterstitialAdBroadcastReceiver extends BaseBroadcastReceiver {
    private static IntentFilter sIntentFilter;
    private NewInterstitial.InterstitialListener mListener;
    private BaseAdUnit mAdUnit;

    public NewInterstitialAdBroadcastReceiver(
            final BaseAdUnit adUnit,
            final NewInterstitial.InterstitialListener listener,
            final String broadcastIdentifier) {
        super(broadcastIdentifier);
        mAdUnit = adUnit;
        mListener = listener;
        getIntentFilter();
    }


    public IntentFilter getIntentFilter() {
        if (sIntentFilter == null) {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_PLAY);
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_SKIP);
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL);
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_COMPLETE);
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_CLOSE);


        }
        return sIntentFilter;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(intent);

        if (mListener == null || mAdUnit == null) {
            return;
        }

        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();

        switch (action) {
            case IntentActions.ACTION_REWARDED_VIDEO_PLAY:
                mListener.onAdShow(mAdUnit);
                break;
            case IntentActions.ACTION_REWARDED_VIDEO_SKIP:
                mListener.onAdSkip(mAdUnit);
                break;
            case IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL:
                String message = intent.getStringExtra("error");
                mListener.onAdShowFail(mAdUnit, message);
                unregister(this);
                mAdUnit = null;
                break;
            case IntentActions.ACTION_REWARDED_VIDEO_COMPLETE:
                break;
            case IntentActions.ACTION_REWARDED_VIDEO_CLOSE:
                mListener.onAdClose(mAdUnit);
                unregister(this);
                mAdUnit = null;
                break;
        }
    }

    @Override
    public void unregister(BroadcastReceiver broadcastReceiver) {
        super.unregister(broadcastReceiver);
        mListener = null;
    }
}
