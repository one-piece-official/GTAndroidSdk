package com.sigmob.sdk.mraid2;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

public class MraidHScrollView extends HorizontalScrollView implements MraidScroll {
    private static final String TAG = "PageScrollView";
    public final int PAGE_WIDTH = this.getResources().getDisplayMetrics().widthPixels;
    public final int DISTANCE_LIMIT = PAGE_WIDTH / 2;
    public static final float SCROLL_CRITICAL_SPEED = 1000f;
    private static final int TO_SECOND = 1000;
    private int mDownX;
    private long mDownTime;
    private Mraid2Bridge.PageChangedListener mPageChangedListener;
    private int mCurrentX = 0;

    public MraidHScrollView(Context context) {
        this(context, null);
    }

    public MraidHScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MraidHScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setPageChangedListener(Mraid2Bridge.PageChangedListener pageChangedListener) {
        mPageChangedListener = pageChangedListener;
    }

    @Override
    public void fling(int velocityX) {
        super.fling(0);
    }

    @Override
    public ViewGroup getView() {
        return this;
    }

    @Override
    public void onTouchStart(int x, int y) {
        /**
         * 记录mDownX，以判断滑动方向
         * 如果滑动临界是页宽的一半，不用记录mDownX
         */
        mDownX = x;
        mDownTime = System.currentTimeMillis();
        Log.d(TAG, mCurrentX + "--------onTouchStart--------" + mDownX);
    }

    @Override
    public void onTouchMove(int x, int y) {
        final int d = mDownX - x;//滑动的距离

//        Log.d(TAG, mCurrentX + "-----------onTouchMove--------:" + d);

        final int maxWidth = this.getChildAt(0).getWidth();

        this.post(new Runnable() {
            @Override
            public void run() {
                if (d > 0) {//左滑
                    if (mCurrentX + PAGE_WIDTH < maxWidth) {
                        scrollTo(mCurrentX + Math.abs(d), 0);
                    }
                } else {//右滑
                    if (mCurrentX > 0) {
                        scrollTo(mCurrentX - Math.abs(d), 0);
                    }
                }
            }
        });
    }

    @Override
    public void onTouchEnd(final Mraid2WebView view, int x, int y) {
        final int d = mDownX - x;

        Log.d(TAG, mCurrentX + "-----------onTouchEnd--------:" + d);

        final boolean isGoPage = goPage(d);//是否滑动翻页

        final int maxWidth = this.getChildAt(0).getWidth();

        Log.d(TAG, maxWidth + "------是否翻页----" + isGoPage);

        //直接调用smoothScrollTo(),没有效果
        this.post(new Runnable() {
            @Override
            public void run() {

                if (isGoPage) {
                    int type;
                    if (d > 0) {//向左滑
                        type = 1;
                        if (mCurrentX + PAGE_WIDTH < maxWidth) {
                            mCurrentX = mCurrentX + PAGE_WIDTH;
                        }
                    } else {//向右滑
                        type = 2;
                        if (mCurrentX > 0) {
                            mCurrentX = mCurrentX - PAGE_WIDTH;
                        }
                    }

                    if (mPageChangedListener != null) {
                        mPageChangedListener.onPageChanged(view, type, mCurrentX / PAGE_WIDTH);
                    }
                }

                smoothScrollTo(mCurrentX, 0);
            }
        });
    }

    private boolean goPage(int d) {

        int remainder = d % PAGE_WIDTH;

        int multiple = d / PAGE_WIDTH;

        Log.d(TAG, remainder + ":-----goPage------:" + multiple);

        float speed = d * TO_SECOND / (System.currentTimeMillis() - mDownTime);

        //滑动速度慢，才会判断距离
        if (speed < SCROLL_CRITICAL_SPEED && speed > -SCROLL_CRITICAL_SPEED) {
            if (remainder < DISTANCE_LIMIT) {
                return false;//不滑
            }
            if (remainder > PAGE_WIDTH - DISTANCE_LIMIT) {
                return true;//滑
            }
        }

        /**
         * 滑动速度快，直接走以下步骤
         */
        return true;//滑
    }
}
