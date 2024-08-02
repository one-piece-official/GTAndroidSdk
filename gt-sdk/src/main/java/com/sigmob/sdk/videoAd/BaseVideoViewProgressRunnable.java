package com.sigmob.sdk.videoAd;

import android.os.Handler;

import com.czhj.sdk.common.ThreadPool.RepeatingHandlerRunnable;
import com.czhj.sdk.common.utils.Preconditions;

class BaseVideoViewProgressRunnable extends RepeatingHandlerRunnable {

    private final VideoViewController mVideoViewController;
    private final BaseVideoConfig mBaseVideoConfig;


    public BaseVideoViewProgressRunnable(VideoViewController videoViewController,
                                         final BaseVideoConfig videoConfig,
                                         Handler handler) {
        super(handler);

        Preconditions.NoThrow.checkNotNull(videoViewController);
        Preconditions.NoThrow.checkNotNull(videoConfig);
        mVideoViewController = videoViewController;
        mBaseVideoConfig = videoConfig;

        // Keep track of quartile measurement for ExternalViewabilitySessions

    }

    @Override
    public void doWork() {
//        long videoLength = mVideoViewController.getDuration();
//        int currentPosition = mVideoViewController.getCurrentPosition();
//
//        if (videoLength > 0) {
//
//            if (mVideoViewController.showbeFinished()) {
//                mVideoViewController.makeIsfinish(false);
//            }
//
//
//            if (currentPosition + 1000 < videoLength && mVideoViewController.shouldBeSkipable()) {
//                mVideoViewController.makeSkipInteractable();
//            }
//
//            if (mVideoViewController.shouldBeShowCompanionAds()) {
//                mVideoViewController.showCompanionAds();
//            }
//
//            //send
//            final List<SigAdTracker> trackersToTrack =
//                    mBaseVideoConfig.getUntriggeredTrackersBefore(currentPosition, videoLength);
//
//
//            for (SigAdTracker tracker : trackersToTrack) {
//
//                mVideoViewController.handleViewabilityQuartileEvent(tracker.getAdEvent());
//
//                tracker.setTracked();
//            }
//            if (currentPosition > videoLength) {
//                mVideoViewController.stopVideoPlay(true);
//            }
//        }
    }

}
