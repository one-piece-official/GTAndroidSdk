// Copyright 2018-2019 Sigmob, Inc.
// Licensed under the Sigmob SDK License Agreement
// http://www.sigmob.com/legal/sdk-license-agreement/

package com.sigmob.sdk.mraid;

import android.content.Context;
import android.graphics.Rect;

import com.czhj.sdk.common.utils.Dips;

/**
 * Screen metrics needed by the MRAID container.
 * <p>
 * Each rectangle is stored using both it's original and scaled coordinates to avoid allocating
 * extra memory that would otherwise be needed to do these conversions.
 */
public class MraidScreenMetrics {
    private final Context mContext;
    private final Rect mScreenRect;
    private final Rect mScreenRectDips;

    private final Rect mRootViewRect;
    private final Rect mRootViewRectDips;

    private final Rect mCurrentAdRect;
    private final Rect mCurrentAdRectDips;

    private final Rect mDefaultAdRect;
    private final Rect mDefaultAdRectDips;

    private final float mDensity;

    public MraidScreenMetrics(Context context, float density) {
        mContext = context.getApplicationContext();
        mDensity = density;

        mScreenRect = new Rect();
        mScreenRectDips = new Rect();

        mRootViewRect = new Rect();
        mRootViewRectDips = new Rect();

        mCurrentAdRect = new Rect();
        mCurrentAdRectDips = new Rect();

        mDefaultAdRect = new Rect();
        mDefaultAdRectDips = new Rect();
    }

    private void convertToDips(Rect sourceRect, Rect outRect) {
        outRect.set(Dips.pixelsToIntDips(sourceRect.left, mContext),
                Dips.pixelsToIntDips(sourceRect.top, mContext),
                Dips.pixelsToIntDips(sourceRect.right, mContext),
                Dips.pixelsToIntDips(sourceRect.bottom, mContext));
    }

    public float getDensity() {
        return mDensity;
    }

    public void setScreenSize(int width, int height) {
        mScreenRect.set(0, 0, width, height);
        convertToDips(mScreenRect, mScreenRectDips);
    }


    Rect getScreenRect() {
        return mScreenRect;
    }


    public Rect getScreenRectDips() {
        return mScreenRectDips;
    }

    public void setRootViewPosition(int x, int y, int width, int height) {
        mRootViewRect.set(x, y, x + width, y + height);
        convertToDips(mRootViewRect, mRootViewRectDips);
    }


    Rect getRootViewRect() {
        return mRootViewRect;
    }


    public Rect getRootViewRectDips() {
        return mRootViewRectDips;
    }

    public void setCurrentAdPosition(int x, int y, int width, int height) {
        mCurrentAdRect.set(x, y, x + width, y + height);
        convertToDips(mCurrentAdRect, mCurrentAdRectDips);
    }


    Rect getCurrentAdRect() {
        return mCurrentAdRect;
    }


    public Rect getCurrentAdRectDips() {
        return mCurrentAdRectDips;
    }

    public void setDefaultAdPosition(int x, int y, int width, int height) {
        mDefaultAdRect.set(x, y, x + width, y + height);
        convertToDips(mDefaultAdRect, mDefaultAdRectDips);
    }


    Rect getDefaultAdRect() {
        return mDefaultAdRect;
    }


    public Rect getDefaultAdRectDips() {
        return mDefaultAdRectDips;
    }
}
