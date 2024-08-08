package com.gt.sdk.base.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.base.common.AdStackManager;
import com.gt.sdk.base.common.BaseAdViewController;
import com.gt.sdk.base.common.BaseAdViewControllerListener;
import com.gt.sdk.base.common.BaseBroadcastReceiver;
import com.gt.sdk.base.common.IntentUtil;
import com.gt.sdk.base.common.LandPageViewController;
import com.gt.sdk.base.common.SigUtils;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;

import java.lang.reflect.Field;
import java.util.HashMap;

public class AdActivity extends BaseAdActivity implements BaseAdViewControllerListener {

    private BaseAdViewController mBaseAdViewController;
    private String mBroadcastIdentifier;

    private static String getBroadcastIdentifierFromIntent(Intent intent) {
        return intent.getStringExtra(Constants.BROADCAST_IDENTIFIER_KEY);
    }

    private static void fixInputMethod(Context context) {
        if (context == null) {
            return;
        }
        InputMethodManager inputMethodManager = null;
        try {
            inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        } catch (Throwable th) {
            SigmobLog.e(th.getMessage());
        }
        if (inputMethodManager == null) {
            return;
        }
        Field[] declaredFields = inputMethodManager.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            try {
                if (!declaredField.isAccessible()) {
                    declaredField.setAccessible(true);
                }
                Object obj = declaredField.get(inputMethodManager);
                if (obj == null || !(obj instanceof View)) {
                    continue;
                }
                declaredField.set(inputMethodManager, null);
            } catch (Throwable th) {
                SigmobLog.e(th.getMessage());
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            if (mBaseAdViewController != null) {
                mBaseAdViewController.onStart();
            }
        } catch (Throwable ignored) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SigUtils.hookOrientation(this);
        super.onCreate(savedInstanceState);

        SigmobLog.i("TaskId :" + getTaskId());
        mBroadcastIdentifier = getBroadcastIdentifierFromIntent(getIntent());

        String uuid = getIntent().getStringExtra(ADUNIT_REQUESTID_KEY);

        try {
            if (ClientMetadata.getInstance() == null || TextUtils.isEmpty(uuid)) {

                SigmobLog.e("uuid is empty");
                HashMap<String, Object> map = new HashMap<>();
                map.put("error", "uuid is empty");
                BaseBroadcastReceiver.broadcastAction(this, mBroadcastIdentifier, map, IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL);
                finish();
                return;
            }

            BaseAdUnit playAdUnit = AdStackManager.getPlayAdUnit(uuid);

            if (!mBroadcastIdentifier.equals("dislike_broadcastIdentifier")) {
                if (playAdUnit == null) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("error", "playAdUnit is null");
                    BaseBroadcastReceiver.broadcastAction(this, mBroadcastIdentifier, map, IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL);
                    finish();
                    return;
                }
            }

            mBaseAdViewController = createAdViewController(playAdUnit, savedInstanceState);
            if (mBaseAdViewController != null) {
                mBaseAdViewController.onCreate();
            }

        } catch (Throwable e) {
            SigmobLog.e("AdActivity onCreate Throwable:" + e.getMessage());
            // This can happen if the activity was started without valid intent extras. We leave
            // mBaseAdViewController set to null, and finish the activity immediately.
            HashMap<String, Object> map = new HashMap<>();
            map.put("error", e.getMessage());
            BaseBroadcastReceiver.broadcastAction(this, mBroadcastIdentifier, map, IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL);
            finish();
        }
    }

    @Override
    protected void onPause() {
        try {
            super.onPause();
            if (mBaseAdViewController != null) {
                mBaseAdViewController.onPause();
            }
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
            HashMap<String, Object> map = new HashMap<>();
            map.put("error", e.getMessage());
            BaseBroadcastReceiver.broadcastAction(this, mBroadcastIdentifier, map, IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL);
            finish();
        }
    }

    @Override
    protected void onResume() {
        try {
            super.onResume();
            if (mBaseAdViewController != null) {
                mBaseAdViewController.onResume();
            }
        } catch (Throwable e) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("error", e.getMessage());
            BaseBroadcastReceiver.broadcastAction(this, mBroadcastIdentifier, map, IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL);
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        SigmobLog.d("RewardVideoAdPlayerActivity onDestroy() called");
//        fixInputMethod(this);

        if (mBaseAdViewController != null) {
            mBaseAdViewController.onDestroy();
        }
        mBaseAdViewController = null;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mBaseAdViewController != null) {
            mBaseAdViewController.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (mBaseAdViewController != null) {
            mBaseAdViewController.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (mBaseAdViewController != null && mBaseAdViewController.backButtonEnabled()) {
            super.onBackPressed();
            mBaseAdViewController.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (mBaseAdViewController != null) {
            mBaseAdViewController.onActivityResult(requestCode, resultCode, data);
        }
    }

    private BaseAdViewController createAdViewController(BaseAdUnit baseAdUnit, Bundle savedInstanceState) throws IllegalStateException {

        String clazz = getIntent().getStringExtra(AD_CLASS_EXTRAS_KEY);

        switch (clazz) {
//            case REWARD: {
//                return new VideoViewController(this, baseAdUnit, getIntent().getExtras(), savedInstanceState, mBroadcastIdentifier, this);
//            }
            case LANDPAGE: {
                return new LandPageViewController(this, baseAdUnit, getIntent().getExtras(), savedInstanceState, mBroadcastIdentifier, this);
            }
//            case LANDNATIVE: {
//                return new SigmobNativeAdLandViewController(this, baseAdUnit, getIntent().getExtras(), savedInstanceState, mBroadcastIdentifier, this);
//            }
//            case DISLIKE: {
//                return new SigmobDisLikeViewController(this, baseAdUnit, getIntent().getExtras(), savedInstanceState, mBroadcastIdentifier, this);
//            }
//            case NEW_INTERSTITIAL: {
//                return new NewInterstitialViewController(this, baseAdUnit, getIntent().getExtras(), savedInstanceState, mBroadcastIdentifier, this);
//            }
        }

        return null;
    }

    /**
     * Implementation of BaseVideoViewControllerListener
     */

    @Override
    public void onSetContentView(final View view) {
        setContentView(view);
    }

    /**
     * targetSdkVersion>26
     * Build.VERSION.SDK_INT == 26
     * Only fullscreen activities can request orientation
     */
    @Override
    public void onSetRequestedOrientation(final int requestedOrientation) {
        setRequestedOrientation(requestedOrientation);
    }

    @Override
    public void setRequestedOrientation(int requestedOrientation) {
        try {
            if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O && SigUtils.isTranslucentOrFloating(this)) {
                return;
            }

            super.setRequestedOrientation(requestedOrientation);
        } catch (Exception e) {
            SigmobLog.e("setRequestedOrientation: " + e.getMessage());
        }
    }

    @Override
    public void onFinish() {
        finish();
        overridePendingTransition(0, 0);
    }

    @Override
    public void onStartActivityForResult(final Class<? extends Activity> clazz, final int requestCode, final Bundle extras) {
        if (clazz == null) {
            return;
        }

        final Intent intent = IntentUtil.getStartActivityIntent(this, clazz, extras);

        try {
            startActivityForResult(intent, requestCode);
        } catch (Throwable e) {
            SigmobLog.d("Activity " + clazz.getName() + " not found. Did you declare it in your AndroidManifest.xml?");
        }
    }

}