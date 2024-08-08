package com.gt.sdk.base.activity;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.gt.sdk.base.common.AdStackManager;
import com.gt.sdk.base.common.BaseBroadcastReceiver;
import com.gt.sdk.base.common.TransparentAdActivity;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;

import java.util.HashMap;

public class BaseAdActivity extends Activity {

    public static final String LANDPAGE = "LandPage";
    public static final String REWARD = "reward";
    public static final String MRAID = "mraid";
    public static final String MRAID_TWO = "mraid_two";
    public static final String LANDNATIVE = "LandNative";
    public static final String DISLIKE = "DisLike";
    protected static final String AD_CLASS_EXTRAS_KEY = "ad_view_class_name";
    protected static final String ADUNIT_REQUESTID_KEY = "adUnit_requestId_key";
    public static final String LAND_PAGE_URL = "land_page_url";
    public static final String NEW_INTERSTITIAL = "new_interstitial";

    public static void startActivity(final Context context, Class<? extends BaseAdActivity> cls, final String broadcastIdentifier, final Bundle bundle, String ad_class) {
        try {
            Class<? extends BaseAdActivity> activityClass = cls;
            BaseAdUnit playAdUnit = AdStackManager.getPlayAdUnit(broadcastIdentifier);
            int display_orientation = playAdUnit.getDisplay_orientation();

            if (display_orientation == 0) {
                display_orientation = ClientMetadata.getInstance().getOrientationInt();
            }
            if (cls == TransparentAdActivity.class) {
                switch (display_orientation) {
                    case 1:
                        activityClass = PortraitTransparentAdActivity.class;
                        break;
                    case 2:
                        activityClass = LandscapeTransparentAdActivity.class;
                        break;
                    default: {
                        activityClass = TransparentAdActivity.class;
                        break;
                    }
                }
            } else {
                switch (display_orientation) {
                    case 1:
                        activityClass = PortraitAdActivity.class;
                        break;
                    case 2:
                        activityClass = LandScapeAdActivity.class;
                        break;
                    default: {
                        activityClass = AdActivity.class;
                        break;
                    }
                }
            }
            final Intent intentVideoPlayerActivity = createIntent(context, activityClass, broadcastIdentifier, ad_class);

            if (bundle != null) {
                intentVideoPlayerActivity.putExtras(bundle);
            }
            context.startActivity(intentVideoPlayerActivity);
        } catch (Throwable e) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("error", e.getMessage());
            BaseBroadcastReceiver.broadcastAction(context, broadcastIdentifier, map, IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL);
        }
    }

    public static void startActivity(final Context context, Class<? extends BaseAdActivity> cls, String broadcastIdentifier) {
        startActivity(context, cls, broadcastIdentifier, null, LANDPAGE);
    }

    public static void startActivity(final Context context, Class<? extends BaseAdActivity> cls, BaseAdUnit adUnit) {

        final Intent intentVideoPlayerActivity = createIntent(context, cls, adUnit.getUuid(), LANDPAGE);

        try {
            Bundle bundle = new Bundle();
            bundle.putSerializable(LAND_PAGE_URL, adUnit);
            intentVideoPlayerActivity.putExtras(bundle);
            context.startActivity(intentVideoPlayerActivity);
        } catch (Throwable e) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("error", e.getMessage());
        }
    }

    private static Intent createIntent(final Context context, Class<? extends BaseAdActivity> cls, String broadcastIdentifier, final String ad_class) {
        final Intent intentVideoPlayerActivity = new Intent(context, cls);
        intentVideoPlayerActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intentVideoPlayerActivity.putExtra(AD_CLASS_EXTRAS_KEY, ad_class);
        intentVideoPlayerActivity.putExtra(ADUNIT_REQUESTID_KEY, broadcastIdentifier);

        int mRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_BEHIND;
        if (context instanceof Activity) {
            mRequestedOrientation = ((Activity) context).getRequestedOrientation();
        }

        if (mRequestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT && mRequestedOrientation != ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE) {

            int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
            int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
            //宽>高为横屏,反正为竖屏
            if (screenWidth > screenHeight) {
                mRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
            } else {
                mRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
            }
        }

        intentVideoPlayerActivity.putExtra(Constants.REQUESTED_ORIENTATION, mRequestedOrientation);

        intentVideoPlayerActivity.putExtra(Constants.BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
        return intentVideoPlayerActivity;
    }

}
