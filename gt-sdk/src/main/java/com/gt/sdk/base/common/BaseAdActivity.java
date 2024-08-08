package com.gt.sdk.base.common;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_BEHIND;
import static com.czhj.sdk.common.Constants.BROADCAST_IDENTIFIER_KEY;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.czhj.sdk.common.ClientMetadata;
import com.gt.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.common.LandscapeAdActivity;
import com.sigmob.sdk.base.common.LandscapeTransparentAdActivity;
import com.sigmob.sdk.base.common.PortraitAdActivity;
import com.sigmob.sdk.base.common.PortraitTransparentAdActivity;
import com.sigmob.sdk.base.common.TransparentAdActivity;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.IntentActions;

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
                        activityClass = LandscapeAdActivity.class;
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
            broadcastAction(context, broadcastIdentifier, map, IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL);


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

    /**
     * DisLikeDialog
     *
     * @param context
     * @param cls
     * @param broadcastIdentifier
     */
    public static void disLikeDialog(final Context context, Class<? extends BaseAdActivity> cls, final String broadcastIdentifier) {
        try {

//            Class<? extends BaseAdActivity> activityClass;
//            int mRequestedOrientation = SCREEN_ORIENTATION_BEHIND;
//            if (context instanceof Activity) {
//                mRequestedOrientation = ((Activity) context).getRequestedOrientation();
//            }
//            switch (mRequestedOrientation) {
//                case ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT:
//                case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
//                case ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT:
//                    activityClass = PortraitAdActivity.class;
//                    break;
//                case ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE:
//                case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
//                case ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE:
//                    activityClass = LandscapeAdActivity.class;
//                    break;
//                default: {
//                    activityClass = AdActivity.class;
//                    break;
//                }
//            }
            final Intent intentVideoPlayerActivity = new Intent(context, cls);
            intentVideoPlayerActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);

            intentVideoPlayerActivity.putExtra(AD_CLASS_EXTRAS_KEY, DISLIKE);
            intentVideoPlayerActivity.putExtra(ADUNIT_REQUESTID_KEY, broadcastIdentifier);


//            intentVideoPlayerActivity.putExtra(REQUESTED_ORIENTATION, mRequestedOrientation);
            intentVideoPlayerActivity.putExtra(BROADCAST_IDENTIFIER_KEY, "dislike_broadcastIdentifier");

            context.startActivity(intentVideoPlayerActivity);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static Intent createIntent(final Context context, Class<? extends BaseAdActivity> cls, String broadcastIdentifier, final String ad_class) {
        final Intent intentVideoPlayerActivity = new Intent(context, cls);
        intentVideoPlayerActivity.setFlags(FLAG_ACTIVITY_NEW_TASK);


        intentVideoPlayerActivity.putExtra(AD_CLASS_EXTRAS_KEY, ad_class);
        intentVideoPlayerActivity.putExtra(ADUNIT_REQUESTID_KEY, broadcastIdentifier);

        int mRequestedOrientation = SCREEN_ORIENTATION_BEHIND;
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
        intentVideoPlayerActivity.putExtra(REQUESTED_ORIENTATION, mRequestedOrientation);

        intentVideoPlayerActivity.putExtra(BROADCAST_IDENTIFIER_KEY, broadcastIdentifier);
        return intentVideoPlayerActivity;
    }

}
