package com.sigmob.sdk.splash;

import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.view.View;
import android.view.ViewGroup;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.BaseBroadcastReceiver;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.views.AdVideoPlayer;

import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_SHOW;

class SplashAdVideoContentView extends SplashAdContentView {
    private final AdVideoPlayer mVideoView;

    private final BaseAdUnit mAdUnit;

    public SplashAdVideoContentView(Context context, BaseAdUnit adUnit) {
        super(context);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mVideoView = new AdVideoPlayer(context);
        setBackgroundColor(Color.BLACK);
        mAdUnit = adUnit;
        addView(mVideoView, layoutParams);

    }


    @Override
    public boolean loadResource(BaseAdUnit adUnit) {
        if (adUnit == null || adUnit.getSplashFilePath() == null) {
            SigmobLog.e("adUnit or splashFilePath is null");
            return false;
        }

        String filePath = adUnit.getSplashFilePath();

        mVideoView.setMeasure(0, 0);
        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

            @Override
            public void onPrepared(MediaPlayer mp) {
                mDuration = (mp.getDuration() / 1000);
                mp.setVolume(0, 0);
                mp.start();
                BaseBroadcastReceiver.broadcastAction(getContext(), mAdUnit.getUuid(), ACTION_INTERSTITIAL_SHOW);

                SigmobLog.i("video onPrepared");
            }
        });

        mVideoView.setVideoPath(filePath);

        return true;
    }


    @Override
    public void showAd() {
        super.setVisibility(VISIBLE);
        SigmobLog.i("video showAd");
        mVideoView.setVisibility(VISIBLE);
        mVideoView.start();
    }

    @Override
    public void onPause() {
        mVideoView.onPause();
    }

    @Override
    public void onResume() {
        mVideoView.onResume();
    }

    @Override
    public void setVisibility(int visibility) {
        try {
            if (visibility == View.GONE && mVideoView != null) {
                SigmobLog.i("video GONE");
                mVideoView.onDestroy();
                removeAllViews();
            }
        } catch (Throwable th) {
            SigmobLog.e("set splash ad video content error: " + th.getMessage());
        }

        super.setVisibility(visibility);

    }
}
