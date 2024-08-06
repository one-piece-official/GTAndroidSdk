package com.windmill.demo;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;

import com.gt.sdk.AdError;
import com.gt.sdk.AdRequest;
import com.gt.sdk.api.SplashAdListener;
import com.gt.sdk.splash.SplashAd;
import com.windmill.demo.natives.NativeAdActivity;
import com.windmill.demo.utils.PxUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainFragment extends Fragment implements SplashAdListener {

    private TextView logTextView;
    private String[] mLogs;
    private Activity mActivity;
    String splash_placement_id = "";
    private ViewGroup splashLY;
    private boolean isNewInstance;
    private final Map<String, SplashAd> splashAdMap = new HashMap<>();
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

    private void LoadSplashAd() {
        initViewGroup(getMyActivity());

        SplashAd splashAd = splashAdMap.get(splash_placement_id);
        Log.d("lance", (splashAd == null) + "---------LoadSplashAd---------" + splash_placement_id);

        Map<String, String> options = new HashMap<>();
        options.put("user_id", userID);
        AdRequest adRequest = new AdRequest.Builder().setCodeId("splash_placement_id").setUserID(userID).setWidth(PxUtils.getRealMetrics(getMyActivity()).widthPixels).setHeight(PxUtils.getRealMetrics(getMyActivity()).heightPixels).setExtOption(options).build();

        if (splashAd != null) {
            if (isNewInstance) {
                splashAdMap.remove(splash_placement_id);
                splashAd = new SplashAd(adRequest, this);
            }
        } else {
            splashAd = new SplashAd(adRequest, this);
        }
        Log.d("lance", "------------start--------loadAd-------" + System.currentTimeMillis());
        splashAd.loadAd();
        splashAdMap.put(splash_placement_id, splashAd);
    }

    private void showSplashAd() {
        SplashAd splashAd = splashAdMap.get(splash_placement_id);
        Log.d("lance", "---------showAd---------" + splash_placement_id);
        if (splashAd != null && splashAd.isReady()) {
            splashAd.show(splashLY);
        } else {
            logMessage("splashAd is not ready or splashAd is null");
        }
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
        isNewInstance = sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false);
        userID = sharedPreferences.getString(Constants.CONF_USER_ID, "");
        splash_placement_id = "123456";
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

    @Override
    public void onSplashAdLoadSuccess(String placementId) {
        Log.d("lance", "----------onSplashAdLoadSuccess----------" + placementId);
        logMessage("onSplashAdLoadSuccess");
    }

    @Override
    public void onSplashAdLoadFail(String placementId, AdError error) {
        Log.d("lance", "----------onSplashAdLoadFail----------" + error.toString() + ":" + placementId);
        logMessage("onSplashAdFailToLoad:" + error + " placementId: " + placementId);
        if (splashLY != null) {
            splashLY.removeAllViews();
            splashLY.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSplashAdShow(String placementId) {
        Log.d("lance", "----------onSplashAdShow----------" + placementId);
        logMessage("onSplashAdShow");
    }

    @Override
    public void onSplashAdShowError(String placementId, AdError error) {
        Log.d("lance", "----------onSplashAdShowError----------" + error.toString() + ":" + placementId);
        logMessage("onSplashAdShowError:" + error + " placementId: " + placementId);
        if (splashLY != null) {
            splashLY.removeAllViews();
            splashLY.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSplashAdClick(String placementId) {
        Log.d("lance", "----------onSplashAdClicked----------" + placementId);
        logMessage("onSplashAdClicked");
    }

    @Override
    public void onSplashAdClose(String placementId) {
        Log.d("lance", "----------onSplashAdClose----------" + placementId);
        logMessage("onSplashAdClose");
        //（1）当穿山甲、优量汇的开屏广告素材支持点睛时，splashEyeAd不为null
        //（2）当展示的是快手开屏广告时，splashEyeAd为非null值，但不一定表示此次快手开屏广告的素材支持点睛，不支持时调用IATSplashEyeAd#show()方法会直接回调ATSplashEyeAdListener#onAdDismiss()方法
        //（3）当splashEyeAd不为null，但是开发者不想支持点睛功能时，必须调用splashEyeAd.destroy()释放资源，然后跳转主页面或者移除开屏View
        if (splashLY != null) {
            splashLY.removeAllViews();
            splashLY.setVisibility(View.GONE);
        }
    }
}
