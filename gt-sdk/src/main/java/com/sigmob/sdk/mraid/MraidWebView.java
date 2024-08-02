package com.sigmob.sdk.mraid;

import android.content.Context;
import android.os.Build;
import android.view.View;

import com.sigmob.sdk.base.views.BaseWebView;

public class MraidWebView extends BaseWebView {

    private static final int DEFAULT_MIN_VISIBLE_PX = 1;

    public interface OnVisibilityChangedListener {
        void onVisibilityChanged(boolean isVisible);
    }

    private MraidWebView.OnVisibilityChangedListener mOnVisibilityChangedListener;

    private boolean mMraidViewable;

    public MraidWebView(Context context) {
        super(context);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
            // SigmobAndroid 22 and lower has a bug where onVisibilityChanged is not called all
            // the time when views are attached.
            mMraidViewable = getVisibility() == View.VISIBLE;
            return;
        }


    }

    public void setVisibilityChangedListener(MraidWebView.OnVisibilityChangedListener listener) {
        mOnVisibilityChangedListener = listener;
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);

        if (visibility == View.VISIBLE) {
        } else {
            setMraidViewable(false);
        }
    }

    private void setMraidViewable(final boolean viewable) {
        if (mMraidViewable == viewable) {
            return;
        }
        mMraidViewable = viewable;
        if (mOnVisibilityChangedListener != null) {
            mOnVisibilityChangedListener.onVisibilityChanged(viewable);
        }
    }

    public boolean isMraidViewable() {
        return mMraidViewable;
    }

    @Override
    public void destroy() {
        super.destroy();
        mOnVisibilityChangedListener = null;
    }
}

