package com.sigmob.sdk.videoAd;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.czhj.sdk.common.utils.Preconditions;
import com.sigmob.sdk.base.common.BaseBroadcastReceiver;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.IntentActions;

public class RewardVideoAdBroadcastReceiver extends BaseBroadcastReceiver {
    private static IntentFilter sIntentFilter;
    private VideoInterstitial.VideoInterstitialListener mRewardVideoListener;
    private BaseAdUnit mAdUnit;

    public RewardVideoAdBroadcastReceiver(
            final BaseAdUnit adUnit,
            VideoInterstitial.VideoInterstitialListener rewardVideoListener,
            final String broadcastIdentifier) {
        super(broadcastIdentifier);
        mAdUnit = adUnit;
        mRewardVideoListener = rewardVideoListener;
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

        if (mRewardVideoListener == null || mAdUnit == null) {
            return;
        }

        if (!shouldConsumeBroadcast(intent)) {
            return;
        }

        final String action = intent.getAction();

        switch (action) {
            case IntentActions.ACTION_REWARDED_VIDEO_PLAY:
                mRewardVideoListener.onVideoPlay(mAdUnit);
                break;
            case IntentActions.ACTION_REWARDED_VIDEO_SKIP:
                mRewardVideoListener.onVideoSkip(mAdUnit);
                break;
            case IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL:
                String message = intent.getStringExtra("error");
                mRewardVideoListener.onVideoPlayFail(mAdUnit, message);
                unregister(this);
                mAdUnit = null;
                break;
            case IntentActions.ACTION_REWARDED_VIDEO_COMPLETE:
                mRewardVideoListener.onVideoComplete(mAdUnit);
                break;
            case IntentActions.ACTION_REWARDED_VIDEO_CLOSE:
                mRewardVideoListener.onVideoClose(mAdUnit);
                unregister(this);
                mAdUnit = null;
                break;
        }
    }

    @Override
    public void unregister(BroadcastReceiver broadcastReceiver) {
        super.unregister(broadcastReceiver);
        mRewardVideoListener = null;
    }
}
