package com.sigmob.sdk.base.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.blurkit.BlurKit;
import com.sigmob.sdk.base.utils.ViewUtil;

import java.lang.ref.WeakReference;
import java.util.Map;

public abstract class BaseAdViewController {

    protected String mBroadcastIdentifier;
    protected Context mContext;
    private ImageView mBlurEffectImageView;

    protected RelativeLayout mLayout;
    protected BaseAdViewControllerListener mBaseAdViewControllerListener;
    private RelativeLayout mChildLayout;
    protected AdSize adSize;
    private WeakReference<Activity> contextWeakReference;

    protected BaseAdViewController(final Activity context, final String broadcastIdentifier, final BaseAdViewControllerListener baseAdViewControllerListener) {
        mContext = context.getApplicationContext();

        contextWeakReference = new WeakReference<Activity>(context);
        mBroadcastIdentifier = broadcastIdentifier;
        mBaseAdViewControllerListener = baseAdViewControllerListener;
        mLayout = new RelativeLayout(mContext);
    }

    protected void optionAdSize(Context context, int orientation, Bundle intentExtras) {
        try {
            if (intentExtras != null) {
                boolean isHalfInterstitial = intentExtras.getBoolean(WindConstants.IS_HALF_INTERSTITIAL, false);
                if (isHalfInterstitial) {
                    int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
                    //宽>高为横屏,反正为竖屏
                    if (orientation == ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {
                        int height = Math.min(screenWidth, screenHeight) * 85 / 100;
                        int width = 16 * height / 9;
                        adSize = new AdSize(width, height);
                    } else {
                        int width = Math.min(screenWidth, screenHeight) * 85 / 100;
                        int height = 16 * width / 9;
                        adSize = new AdSize(width, height);
                    }

                    mChildLayout = new RelativeLayout(context);
                    RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(adSize.getWidth(), adSize.getHeight());
                    params.addRule(RelativeLayout.CENTER_IN_PARENT);
                    mChildLayout.setLayoutParams(params);
                    mLayout.removeAllViews();
                    mLayout.addView(mChildLayout);
                }
            }
        } catch (Throwable th) {

        }
    }

    public void setBackgroundDimHide() {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.dimAmount = 0f;
        getActivity().getWindow().setAttributes(lp);
    }

    public void blurEffectViewEnable() {
        try {
            generateBlurEffectView();
            if (mBlurEffectImageView != null) {
                mLayout.addView(mBlurEffectImageView, 0, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        } catch (Throwable e) {

        }

    }

    public void blurEffectViewDisable() {

        try {
            if (mBlurEffectImageView != null) {
                ViewUtil.removeFromParent(mBlurEffectImageView);
                mBlurEffectImageView = null;
            }
        } catch (Throwable e) {

        }

    }

    private void generateBlurEffectView() {
        Activity parent = SDKContext.getLastActivity();
        ViewGroup layout = getLayout();

        if (parent != null && layout != null) {
            ViewGroup contentView = (ViewGroup) parent.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
            if (contentView != null) {
                View childAt = contentView.getChildAt(0);
                if (childAt != null) {
                    BlurKit.init(mContext);
                    Bitmap blur = BlurKit.getInstance().blur(childAt, 25);
                    if (mBlurEffectImageView == null) {
                        mBlurEffectImageView = new ImageView(mContext);
                    }
                    mBlurEffectImageView.setImageBitmap(blur);
                }
            }
        }
    }


    public abstract void onCreate();

    public abstract void onPause();

    public abstract void onResume();

    public void onDestroy() {
        mBaseAdViewControllerListener = null;
        if (mLayout != null) {
            mLayout.removeAllViews();
//            ViewUtil.removeFromParent(mLayout);
        }
    }

    public abstract void onSaveInstanceState(Bundle outState);

    public abstract void onConfigurationChanged(Configuration configuration);

    public abstract void onBackPressed();

    public boolean backButtonEnabled() {
        return true;
    }

    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        // By default, the activity result is ignored
    }

    protected BaseAdViewControllerListener getBaseAdViewControllerListener() {
        return mBaseAdViewControllerListener;
    }

    protected Context getContext() {
        return mContext;
    }

    protected ViewGroup getLayout() {
        if (mChildLayout != null) {
            return mChildLayout;
        }
        return mLayout;
    }


    protected Activity getActivity() {

        Activity activityFromViewTop = ViewUtil.getActivityFromViewTop(mLayout);
        if (activityFromViewTop != null) {
            return activityFromViewTop;
        }
        if (contextWeakReference != null) {
            return contextWeakReference.get();
        }
        return null;
    }

    protected void broadcastAction(final String action, Map<String, Object> extras) {
        if (mBroadcastIdentifier != null) {
            BaseBroadcastReceiver.broadcastAction(mContext, mBroadcastIdentifier, extras, action, 0);
        } else {
            SigmobLog.w("Tried to broadcast a video event without a broadcast identifier to send to.");
        }
    }

    protected void broadcastAction(final String action) {
        if (mBroadcastIdentifier != null) {
            BaseBroadcastReceiver.broadcastAction(mContext, mBroadcastIdentifier, action);
        } else {
            SigmobLog.w("Tried to broadcast a video event without a broadcast identifier to send to.");
        }
    }

    protected void broadcastAction(final String action, int delay) {
        if (mBroadcastIdentifier != null) {
            BaseBroadcastReceiver.broadcastAction(mContext, mBroadcastIdentifier, action, delay);
        } else {
            SigmobLog.w("Tried to broadcast a video event without a broadcast identifier to send to.");
        }
    }


    public abstract void onStart();
}
