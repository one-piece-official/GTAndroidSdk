package com.sigmob.sdk.mraid2;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ScrollView;

public class MraidVScrollView extends ScrollView implements MraidScroll {
    private static final String TAG = "PageScrollView";
    public final int PAGE_HEIGHT = this.getResources().getDisplayMetrics().heightPixels;
    public final int DISTANCE_LIMIT = PAGE_HEIGHT / 2;
    public static final float SCROLL_CRITICAL_SPEED = 1000f;
    private static final int TO_SECOND = 1000;
    private int mDownY;
    private long mDownTime;
    private Mraid2Bridge.PageChangedListener mPageChangedListener;
    private int mCurrentY = 0;

    public MraidVScrollView(Context context) {
        this(context, null);
    }

    public MraidVScrollView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MraidVScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
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
        mDownY = y;
        mDownTime = System.currentTimeMillis();
        Log.d(TAG, mCurrentY + "--------onTouchStart--------" + mDownY);
    }

    @Override
    public void onTouchMove(int x, int y) {
        final int d = mDownY - y;//滑动的距离

//        Log.d(TAG, mCurrentY + "-----------onTouchMove--------:" + d);

        final int maxHeight = this.getChildAt(0).getHeight();

        this.post(new Runnable() {
            @Override
            public void run() {
                if (d > 0) {//上滑
                    if (mCurrentY + PAGE_HEIGHT < maxHeight) {
                        scrollTo(0, mCurrentY + Math.abs(d));
                    }
                } else {//下滑
                    if (mCurrentY > 0) {
                        scrollTo(0, mCurrentY - Math.abs(d));
                    }
                }
            }
        });
    }

    @Override
    public void onTouchEnd(final Mraid2WebView view, int x, int y) {
        final int d = mDownY - y;

        Log.d(TAG, mCurrentY + "-----------onTouchEnd--------:" + d);

        final boolean isGoPage = goPage(d);//是否滑动翻页

        final int maxHeight = this.getChildAt(0).getHeight();

        //直接调用smoothScrollTo(),没有效果
        this.post(new Runnable() {
            @Override
            public void run() {
                if (isGoPage) {
                    int type;
                    if (d > 0) {//向上滑
                        type = 1;
                        if (mCurrentY + PAGE_HEIGHT < maxHeight) {
                            mCurrentY = mCurrentY + PAGE_HEIGHT;
                        }
                    } else {//向下滑
                        type = 2;
                        if (mCurrentY > 0) {
                            mCurrentY = mCurrentY - PAGE_HEIGHT;
                        }
                    }
                    if (mPageChangedListener != null) {
                        mPageChangedListener.onPageChanged(view, type, mCurrentY / PAGE_HEIGHT);
                    }
                }
                smoothScrollTo(0, mCurrentY);
            }
        });
    }

    private boolean goPage(int d) {

        int remainder = d % PAGE_HEIGHT;

        int multiple = d / PAGE_HEIGHT;

        Log.d(TAG, remainder + ":-----goPage------:" + multiple);

        float speed = d * TO_SECOND / (System.currentTimeMillis() - mDownTime);

        //滑动速度慢，才会判断距离
        if (speed < SCROLL_CRITICAL_SPEED && speed > -SCROLL_CRITICAL_SPEED) {
            if (remainder < DISTANCE_LIMIT) {
                return false;//不滑
            }
            if (remainder > PAGE_HEIGHT - DISTANCE_LIMIT) {
                return true;//滑
            }
        }

        /**
         * 滑动速度快，直接走以下步骤
         */
        return true;//滑
    }
}
