package com.windmill.demo;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.windmill.demo.natives.NativeAdActivity;
import com.windmill.demo.splash.SplashZoomOutManager;
import com.windmill.demo.utils.ViewUtils;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.models.AdInfo;
import com.windmill.sdk.splash.IWMSplashEyeAd;
import com.windmill.sdk.splash.WMSplashAd;
import com.windmill.sdk.splash.WMSplashAdListener;
import com.windmill.sdk.splash.WMSplashAdRequest;
import com.windmill.sdk.splash.WMSplashEyeAdListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainFragment extends Fragment implements WMSplashAdListener {

    private TextView logTextView;
    private String[] mLogs;
    private Activity mActivity;
    String splash_placement_id = "";
    private ViewGroup splashLY;
    private boolean isNewInstance;
    private Map<String, WMSplashAd> rewardAdMap = new HashMap<>();
    private String userID;

    private void initViewGroup(Activity activity) {
        splashLY = new RelativeLayout(activity);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
        viewGroup.addView(splashLY, layoutParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    private void bindButton(@IdRes int id, Class clz) {
        getMyActivity().findViewById(id).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //开屏代码位id
                if (v.getId() == R.id.splash_button_load) {
                    LoadSplashAd();
                    return;
                }

                if (v.getId() == R.id.splash_button_show) {
                    showSplashAd();
                    return;
                }

                Intent intent = new Intent(getMyActivity(), clz);
                //激励视频代码位id
                if (v.getId() == R.id.reward_button) {
//                    intent.putExtra("horizontal_rit", "901121184");
//                    intent.putExtra("vertical_rit", "901121375");
                }
                //全屏视频代码位id
                if (v.getId() == R.id.interstitial_button) {
//                    intent.putExtra("horizontal_rit", "901121430");
//                    intent.putExtra("vertical_rit", "901121365");
                }
                startActivity(intent);
            }

        });
    }

    private DisplayMetrics getRealMetrics(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        Display display = windowManager.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm);
        } else {
            @SuppressWarnings("rawtypes") Class c;
            try {
                c = Class.forName("android.view.Display");
                @SuppressWarnings("unchecked") Method method = c.getMethod("getRealMetrics", DisplayMetrics.class);
                method.invoke(display, dm);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return dm;
    }

    private void LoadSplashAd() {
        MyApplication.initPlacementCustomMap(getMyActivity(), splash_placement_id);
        MyApplication.filterChannelId(getMyActivity(), splash_placement_id);
        initViewGroup(getMyActivity());
        WMSplashAd splashAd = rewardAdMap.get(splash_placement_id);
        Log.d("lance", (splashAd == null) + "---------LoadSplashAd---------" + splash_placement_id);

        Map<String, Object> options = new HashMap<>();
        options.put("user_id", userID);
        options.put(WMConstants.AD_WIDTH, getRealMetrics(getActivity()).widthPixels);//针对于穿山甲、GroMore、AdScope开屏有效、单位px
        options.put(WMConstants.AD_HEIGHT, getRealMetrics(getActivity()).heightPixels);//针对于穿山甲、GroMore、AdScope开屏有效、单位px

        if (splashAd != null) {
            if (isNewInstance) {
                rewardAdMap.remove(splash_placement_id);
                splashAd = new WMSplashAd(getMyActivity(), new WMSplashAdRequest(splash_placement_id, userID, options, true), this);
            }
        } else {
            splashAd = new WMSplashAd(getMyActivity(), new WMSplashAdRequest(splash_placement_id, userID, options, true), this);
        }
        Log.d("lance", "------------start--------loadAd-------" + System.currentTimeMillis());
        splashAd.loadAdOnly();

        rewardAdMap.put(splash_placement_id, splashAd);
    }

    private void showSplashAd() {
        WMSplashAd splashAd = rewardAdMap.get(splash_placement_id);
        Log.d("lance", "---------showAd---------" + splash_placement_id);
        if (splashAd != null) {
            List<AdInfo> adInfoList = splashAd.checkValidAdCaches();
            if (adInfoList != null && adInfoList.size() > 0) {
                for (int i = 0; i < adInfoList.size(); i++) {
                    AdInfo adInfo = adInfoList.get(i);
                    Log.d("lance", "---------showAd-----adInfo----:" + adInfo.toString());
                }
            }
        }
        if (splashAd != null && splashAd.isReady()) {
//            splashAd.showAd(null);
            splashAd.showAd(splashLY);
        } else {
            logMessage("splashAd is not ready or splashAd is null");
        }
    }

    private View zoomOutView;

    private void showSplashEyeAd(IWMSplashEyeAd splashEyeAd) {
        if (splashEyeAd == null) {
            return;
        }
        splashEyeAd.show(getMyActivity(), null, new WMSplashEyeAdListener() {
            @Override
            public void onAnimationStart(View splashView) {
                Log.i("lance", "----------onAnimationStart---------: eye ad");
                //执行缩放动画
                SplashZoomOutManager zoomOutManager = SplashZoomOutManager.getInstance(getMyActivity().getApplicationContext());

                ////建议优先使用IATSplashEyeAd#getSuggestedSize()返回的大小作为缩放动画的目标大小
                int[] suggestedSize = splashEyeAd.getSuggestedSize(getMyActivity().getApplicationContext());
                if (suggestedSize != null) {
                    zoomOutManager.setSplashEyeAdViewSize(suggestedSize[0], suggestedSize[1]);
                }

//                zoomOutManager.setSplashInfo(splashView, getMyActivity().getWindow().getDecorView());
                ViewGroup content = getMyActivity().findViewById(android.R.id.content);
                zoomOutView = zoomOutManager.startZoomOut(splashView, content, content, new SplashZoomOutManager.AnimationCallBack() {

                    @Override
                    public void animationStart(int animationTime) {
                        Log.i("lance", "----------animationStart---------: eye ad");
                    }

                    @Override
                    public void animationEnd() {
                        Log.i("lance", "----------animationEnd---------: eye ad");
                        //当缩放动画完成时必须调用IATSplashEyeAd#onFinished()通知SDK
                        splashEyeAd.onFinished();
                    }
                });
            }

            @Override
            public void onAdDismiss(boolean isSupportEyeSplash) {
                Log.i("lance", "----------onAdDismiss---------:" + isSupportEyeSplash);
                //建议在此回调中调用IATSplashEyeAd#destroy()释放资源以及释放其他资源，以免造成内存泄漏
                SplashZoomOutManager zoomOutManager = SplashZoomOutManager.getInstance(getMyActivity().getApplicationContext());
                zoomOutManager.clearStaticData();
                if (zoomOutView != null) {
                    ViewUtils.removeFromParent(zoomOutView);
                }
                splashEyeAd.destroy();
            }
        });
    }

    @Override
    public void onSplashAdSuccessPresent(AdInfo adInfo) {
        Log.d("lance", "----------onSplashAdSuccessPresent----------" + adInfo.toString());
        logMessage("onSplashAdSuccessPresent");
    }

    @Override
    public void onSplashAdSuccessLoad(String placementId) {
        Log.d("lance", "----------onSplashAdSuccessLoad----------" + placementId);
        logMessage("onSplashAdSuccessLoad");
    }

    @Override
    public void onSplashAdFailToLoad(WindMillError error, String placementId) {
        Log.d("lance", "------------start--------loadAd---fail----" + System.currentTimeMillis());
        Log.d("lance", "----------onSplashAdFailToLoad----------" + error.toString() + ":" + placementId);
        logMessage("onSplashAdFailToLoad:" + error + " placementId: " + placementId);
        if (splashLY != null) {
            splashLY.removeAllViews();
            splashLY.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSplashAdClicked(AdInfo adInfo) {
        Log.d("lance", "----------onSplashAdClicked----------" + adInfo.toString());
        logMessage("onSplashAdClicked");
    }

    @Override
    public void onSplashClosed(AdInfo adInfo, IWMSplashEyeAd splashEyeAd) {
        Log.d("lance", "----------onSplashClosed----------" + adInfo.toString());
        logMessage("onSplashClosed");
        //（1）当穿山甲、优量汇的开屏广告素材支持点睛时，splashEyeAd不为null
        //（2）当展示的是快手开屏广告时，splashEyeAd为非null值，但不一定表示此次快手开屏广告的素材支持点睛，不支持时调用IATSplashEyeAd#show()方法会直接回调ATSplashEyeAdListener#onAdDismiss()方法
        //（3）当splashEyeAd不为null，但是开发者不想支持点睛功能时，必须调用splashEyeAd.destroy()释放资源，然后跳转主页面或者移除开屏View
        if (splashLY != null) {
            splashLY.removeAllViews();
            splashLY.setVisibility(View.GONE);
        }
        //展示点睛广告
        showSplashEyeAd(splashEyeAd);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        bindButton(R.id.splash_button_load, null);
        bindButton(R.id.splash_button_show, null);
        bindButton(R.id.reward_button, RewardVideoActivity.class);
        bindButton(R.id.interstitial_button, InterstitialActivity.class);
        bindButton(R.id.native_button, NativeAdActivity.class);
        bindButton(R.id.banner_button, BannerActivity.class);
        bindButton(R.id.device_info, DeviceInfoActivity.class);
        bindButton(R.id.setting_button, SettingActivity.class);
        bindButton(R.id.setting_id_button, SettingIDActivity.class);
        bindButton(R.id.init_setting_button, InitSettingActivity.class);

        Button configurationBtn = getView().findViewById(R.id.configuration_button);
        Button cleanLogBtn = getView().findViewById(R.id.cleanLog_button);

        logTextView = getView().findViewById(R.id.logView);

        configurationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                configuration();
            }
        });

        cleanLogBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cleanLog();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        logTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });

        logTextView.setMovementMethod(ScrollingMovementMethod.getInstance());

        if (mLogs != null && mLogs.length > 0) {
            for (int i = 0; i < mLogs.length; i++) {
                logMessage(mLogs[i]);
            }
        }

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("setting", 0);

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, "");
        if (!TextUtils.isEmpty(configJson)) {
            try {
                JSONObject jsonObject = new JSONObject(configJson);
                JSONObject dataJson = jsonObject.getJSONObject("data");
                JSONArray array = dataJson.optJSONArray("slotIds");
                if (array != null && array.length() > 0) {
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject slotId = array.getJSONObject(i);
                        if (slotId != null) {
                            int ad_type = slotId.optInt("adType", -1);
                            if (ad_type == 2) {
                                if (TextUtils.isEmpty(splash_placement_id)) {
                                    splash_placement_id = slotId.optString("adSlotId");
                                }
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        isNewInstance = sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false);
        userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");
    }

    public void setLogs(String[] logs) {
        mLogs = logs;

        if (mLogs != null && mLogs.length > 0 && logTextView != null) {
            for (int i = 0; i < mLogs.length; i++) {
                logMessage(mLogs[i]);
            }
        }
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (Activity) context;//保存Context引用
    }

    private Activity getMyActivity() {
        if (mActivity == null) {
            mActivity = getActivity();
        }
        return mActivity;
    }

    private void configuration() {
        ConfigurationFragment configurationFragment = new ConfigurationFragment();
        FragmentTransaction transaction = getMyActivity().getFragmentManager().beginTransaction();
        transaction.add(R.id.fragment_container, configurationFragment);
        transaction.addToBackStack("configuration");
        transaction.commit();
    }

    private void cleanLog() {
        logTextView.setText("");
    }

    private static SimpleDateFormat dateFormat = null;

    private static SimpleDateFormat getDateTimeFormat() {

        if (dateFormat == null) {
            dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss SSS", Locale.CHINA);
        }
        return dateFormat;
    }

    private void logMessage(String message) {
        Date date = new Date();
        logTextView.append(getDateTimeFormat().format(date) + " " + message + '\n');
    }
}
