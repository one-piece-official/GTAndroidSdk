package com.sigmob.sdk.base.views;

import static java.lang.String.format;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.logger.SigmobLog;

public class ProgressBarDrawable extends BaseWidgetDrawable {

    private final Paint mBackgroundPaint;
    private final Paint mProgressPaint;

    private int mDuration;
    private int mSkipOffset;
    private int mCurrentProgress;
    private int mLastProgress;
    private float mSkipRatio;
    private final int mNuggetWidth;

    public ProgressBarDrawable(final Context context) {
        super();

        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(DrawableConstants.ProgressBar.BACKGROUND_COLOR);
        mBackgroundPaint.setAlpha(DrawableConstants.ProgressBar.BACKGROUND_ALPHA);
        mBackgroundPaint.setStyle(DrawableConstants.ProgressBar.BACKGROUND_STYLE);
        mBackgroundPaint.setAntiAlias(true);

        mProgressPaint = new Paint();
        mProgressPaint.setColor(DrawableConstants.ProgressBar.PROGRESS_COLOR);
        mProgressPaint.setAlpha(DrawableConstants.ProgressBar.PROGRESS_ALPHA);
        mProgressPaint.setStyle(DrawableConstants.ProgressBar.PROGRESS_STYLE);
        mProgressPaint.setAntiAlias(true);

        mNuggetWidth = Dips.dipsToIntPixels(DrawableConstants.ProgressBar.NUGGET_WIDTH_DIPS, context);
    }

    @Override
    public void draw(final Canvas canvas) {
        // Background translucent bar
        canvas.drawRect(getBounds(), mBackgroundPaint);

        // Progress bar
        float progressRatio = mDuration > 0 ? (float) mCurrentProgress / mDuration : 0;
        canvas.drawRect(getBounds().left,
                getBounds().top,
                getBounds().right * progressRatio,
                getBounds().bottom,
                mProgressPaint);

        // Draw skipoffset nugget if skipoffset is in range (0, duration)
        if (mSkipOffset > 0 && mSkipOffset < mDuration) {
            float nuggetPosition = getBounds().right * mSkipRatio;

            canvas.drawRect(nuggetPosition,
                    getBounds().top,
                    nuggetPosition + mNuggetWidth,
                    getBounds().bottom,
                    mProgressPaint);
        }
    }

    public void reset() {
        mLastProgress = 0;
    }

    public void setDurationAndSkipOffset(final int duration, final int skipOffset) {
        mDuration = duration;
        mSkipOffset = skipOffset;
        mSkipRatio = mDuration > 0 ? (float) mSkipOffset / mDuration : 0;
    }

    @SuppressLint("DefaultLocale")
    public void setProgress(final int currentProgress) {
        // There exists two SigmobAndroid video player bugs in VideoView.getCurrentPosition():
        // 1) It temporarily returns 0 right after backgrounding and switching back to the app.
        // 2) Near the end of the video, it never reaches duration and actually returns decreasing values.
        //
        // Therefore, we incorporate two checks to get around the bugs and potential visual glitches:
        // 1) Check against the last known current position to ensure that it's monotonically increasing.
        // 2) If not monotonically increasing, we only force completion and draw the entire progress
        // bar when the current position is not 0, i.e. not right after backgrounding.

        if (currentProgress >= mLastProgress) {
            mCurrentProgress = currentProgress;
            mLastProgress = currentProgress;
        } else if (currentProgress != 0) {
            SigmobLog.d(format("Progress not monotonically increasing: last = %d, current = %d",
                    mLastProgress,
                    currentProgress));
            forceCompletion();
        }

        invalidateSelf();
    }


    private void forceCompletion() {
        mCurrentProgress = mDuration;
    }

    // for testing
    @Deprecated

    public float getSkipRatio() {
        return mSkipRatio;
    }

    // for testing
    @Deprecated

    public int getCurrentProgress() {
        return mCurrentProgress;
    }
}
