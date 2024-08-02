package com.sigmob.sdk.nativead;

import android.content.Context;
import android.graphics.Rect;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.czhj.sdk.logger.SigmobLog;

/**
 * created by lance on   2021/8/3 : 4:00 PM
 */

public class SigEmptyView extends View {

    private TimerHandler timer;

    private boolean isAttachedToWindow = false;
    private boolean hasWindowFocus = true;
    private AdVisibilityStatusChangeListener mAdVisibilityStatusChangeListener;
    private int impression_percent;
    private long lastImpression = 0;
    private int impression_time = 0;
    private int windowVisibility = -1;
    private boolean isShowed = false;
    private boolean mVisible;
    private int viewVisibility = -1;

    public SigEmptyView(Context context) {
        this(context, null);
    }

    public SigEmptyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SigEmptyView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setLayoutParams(new ViewGroup.LayoutParams(0, 0));
    }

    public void setAdVisibilityStatusChangeListener(AdVisibilityStatusChangeListener mAdVisibilityStatusChangeListener) {
        this.mAdVisibilityStatusChangeListener = mAdVisibilityStatusChangeListener;
    }

    /**
     * 开始检查
     */
    public void startAutoCheck(final int impression_percent, final int impression_time) {

        hasWindowFocus = true;

        if (timer != null) {
            disableAutoCheck();
        }
        hasWindowFocus = true;
        this.impression_percent = impression_percent;
        this.impression_time = impression_time;
        timer = new TimerHandler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (MSG_TIMER_ID == msg.what) {
                    if (mAdVisibilityStatusChangeListener != null) {

                        adViewVisibleCheck();
                        tick();
                    }
                }
            }
        };

        startTimer();
    }

    private void adViewVisibleCheck() {
        boolean visible = isVisible() && isViewAttached();
        boolean isValidImpression;

        if (visible) {

            View view = (ViewGroup) getParent();

            final int visibleViewArea = getVisibleViewArea(view);
            final int totalArea = view.getHeight() * view.getWidth();

            visible = totalArea > 0 && visibleViewArea >= impression_percent / 100.0f * totalArea;

            if (visibleViewArea > 0) {
                isShowed = true;
                if (mAdVisibilityStatusChangeListener != null) {
                    mAdVisibilityStatusChangeListener.onAdViewShow();
                }
            } else {
                isShowed = false;
                lastImpression = 0;
                if (mAdVisibilityStatusChangeListener != null) {
                    mAdVisibilityStatusChangeListener.onAdViewRemoved();
                }
            }

            if (visibleViewArea > 0) {
                if (visible && isShowed) {
                    if (lastImpression == 0) {
                        lastImpression = System.currentTimeMillis();
                    }
                    isValidImpression = lastImpression > 0 && System.currentTimeMillis() - lastImpression >= impression_time * 1000;
                    if (mAdVisibilityStatusChangeListener != null) {
                        mAdVisibilityStatusChangeListener.onAdViewStartImpression();
                    }

                    if (mAdVisibilityStatusChangeListener != null) {
                        mAdVisibilityStatusChangeListener.onAdViewImpression(isValidImpression);
                    }
                } else {
                    if (mAdVisibilityStatusChangeListener != null) {
                        mAdVisibilityStatusChangeListener.onAdViewPauseImpression();
                    }
                    if (mAdVisibilityStatusChangeListener != null) {
                        mAdVisibilityStatusChangeListener.onAdViewImpression(false);
                    }
                    lastImpression = 0;
                }
            } else {
                if (mAdVisibilityStatusChangeListener != null && isShowed) {
                    mAdVisibilityStatusChangeListener.onAdViewRemoved();
                }
            }

        } else {
            if (mAdVisibilityStatusChangeListener != null && isShowed) {
                mAdVisibilityStatusChangeListener.onAdViewRemoved();
            }
            isShowed = false;
            lastImpression = 0;
        }

    }

    public int getVisibleViewArea(View view) {

        // holds the visible part of a view
        Rect clippedArea = new Rect();

        if (!view.getGlobalVisibleRect(clippedArea)) {
            return 0;
        }

        return clippedArea.height() * clippedArea.width();
    }

    public boolean isVisible() {

        if (getVisibility() != View.VISIBLE || viewVisibility != View.VISIBLE || getParent() == null) {
            return false;
        }
        return true;
    }

    private void disableAutoCheck() {
        stopTimer();
        timer = null;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        SigmobLog.d("---------onWindowFocusChanged: hasWindowFocus: " + hasWindowFocus);
        this.hasWindowFocus = hasWindowFocus;
        if (isShowed) {
            adViewVisibleCheck();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        this.windowVisibility = visibility;
        SigmobLog.d("---------onWindowVisibilityChanged: visibility: " + visibility);
        if (visibility == VISIBLE) {
            startTimer();
        } else {
            stopTimer();
        }
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        SigmobLog.d("---------onVisibilityChanged---------" + visibility);
        this.viewVisibility = visibility;
        if (visibility == VISIBLE) {
            startTimer();
        } else {
            stopTimer();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        SigmobLog.d("---------onAttachedToWindow---------");
        startTimer();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        SigmobLog.d("---------onDetachedFromWindow----------");
        stopTimer();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        SigmobLog.d("---------onStartTemporaryDetach-----------");
        stopTimer();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        SigmobLog.d("---------onFinishTemporaryDetach-------------");
        startTimer();
    }

    private void startTimer() {
        if (timer == null || !timer.isStopped()) {
            return;
        }
        SigmobLog.d("-----------startTimer----------");
        timer.removeCallbacksAndMessages(null);
        timer.tick();
        timer.setStopped(false);
    }

    private boolean isViewAttached() {
        return isAttachedToWindow && windowVisibility == 0 && hasWindowFocus;
    }

    private void stopTimer() {
        //每次暂停检测器之前先报告一下可见性
        adViewVisibleCheck();

        if (timer == null || timer.isStopped()) {
            return;
        }
        SigmobLog.d("-----------stopTimer----------");
        timer.removeCallbacksAndMessages(null);
        timer.setStopped(true);
    }

    public interface AdVisibilityStatusChangeListener {

        void onAdViewImpression(boolean isValid);

        void onAdViewPauseImpression();

        void onAdViewStartImpression();

        void onAdViewRemoved();

        void onAdViewShow();
    }

}
