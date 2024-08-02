package com.sigmob.sdk.videoAd;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.sigmob.sdk.base.views.DrawableConstants;
import com.sigmob.sdk.base.views.ProgressBarDrawable;
import com.czhj.sdk.common.utils.Dips;

public class VideoPlayerProgressBarWidget extends ImageView {

    private ProgressBarDrawable mProgressBarDrawable;
    private final int mProgressBarHeight;

    public VideoPlayerProgressBarWidget( final Context context) {
        super(context);


        mProgressBarDrawable = new ProgressBarDrawable(context);
        setImageDrawable(mProgressBarDrawable);

        mProgressBarHeight =
                Dips.dipsToIntPixels(DrawableConstants.ProgressBar.HEIGHT_DIPS, context);
    }

    public void setAnchorId(final int anchorId) {
        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                mProgressBarHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, anchorId);
        setLayoutParams(layoutParams);

    }

    public void calibrateAndMakeVisible(final int duration, final int skipOffset) {
        mProgressBarDrawable.setDurationAndSkipOffset(duration, skipOffset);
        setVisibility(View.VISIBLE);
    }

    public void updateProgress(final int progress) {
        mProgressBarDrawable.setProgress(progress);
    }

    public void reset() {
        mProgressBarDrawable.reset();
        mProgressBarDrawable.setProgress(0);
    }

    // for testing
    @Deprecated

    ProgressBarDrawable getImageViewDrawable() {
        return mProgressBarDrawable;
    }

    // for testing
    @Deprecated

    void setImageViewDrawable( ProgressBarDrawable drawable) {
        mProgressBarDrawable = drawable;
    }
}

