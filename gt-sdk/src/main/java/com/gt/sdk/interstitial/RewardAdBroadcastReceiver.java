package com.gt.sdk.interstitial;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.czhj.sdk.common.utils.Preconditions;
import com.gt.sdk.base.common.BaseBroadcastReceiver;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;

public class RewardAdBroadcastReceiver extends BaseBroadcastReceiver {

    private static IntentFilter sIntentFilter;

    public BaseAdUnit mAdUnit;

    private RewardAdInterstitial.VideoAdListener mAdInterstitialListener;

    public RewardAdBroadcastReceiver(BaseAdUnit baseAdUnit, RewardAdInterstitial.VideoAdListener adListener, final String broadcastIdentifier) {
        super(broadcastIdentifier);
        this.mAdUnit = baseAdUnit;
        mAdInterstitialListener = adListener;
        getIntentFilter();
    }

    public IntentFilter getIntentFilter() {
        if (sIntentFilter == null) {
            sIntentFilter = new IntentFilter();
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_PLAY);
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_PLAY_FAIL);
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_CLOSE);
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_CLICK);
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_SKIP);
            sIntentFilter.addAction(IntentActions.ACTION_REWARDED_VIDEO_COMPLETE);
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

        String action = intent.getAction();

        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case IntentActions.ACTION_REWARDED_VIDEO_PLAY:
                    mAdInterstitialListener.onAdShow(mAdUnit);
                    break;
                case IntentActions.ACTION_REWARDED_VIDEO_CLICK:
                    mAdInterstitialListener.onAdClicked(mAdUnit);
                    break;
                case IntentActions.ACTION_REWARDED_VIDEO_CLOSE:
                    mAdInterstitialListener.onAdClose(mAdUnit);
                    break;
                case IntentActions.ACTION_REWARDED_VIDEO_SKIP:
                    mAdInterstitialListener.onAdSkip(mAdUnit);
                    break;
                case IntentActions.ACTION_REWARDED_VIDEO_COMPLETE:
                    mAdInterstitialListener.onAdPlayEnd(mAdUnit);
                    break;
                case IntentActions.ACTION_REWARDED_VIDEO_PLAY_FAIL:
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
