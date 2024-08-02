package com.wind.demo;


import android.app.Fragment;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindAgeRestrictedUserStatus;
import com.sigmob.windad.WindConsentStatus;
import com.wind.demo.natives.NativeAdDemoRender;

import org.checkerframework.checker.units.qual.C;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.Executors;


public class ConfigurationFragment extends Fragment {

    protected static final String CONFIG_URL_MOCK = "http://39.105.1.99:8080/ssp/config";
    protected static final String CONFIG_URL_NORMAL = "https://adservice.sigmob.cn/s/config";
    protected static final String CONFIG_URL_TEST = "https://adstage.sigmob.cn/s/config";
    public static final String SIGDEMO_CONF_URL = "sigdemo_conf_url";

    private TextView textNoView;
    private TextView appIdView;
    private TextView appKeyView;

    private LinearLayout splashLayout;
    private LinearLayout rewardLayout;
    private LinearLayout interstitialLayout;
    private LinearLayout unifiedNativeLayout;
    private LinearLayout newInterstitialLayout;

    private CheckBox halfSplash;
    private CheckBox halfInterstitial;
    private CheckBox closeSplash;
    private CheckBox showWithoutAppInfo;

    private CheckBox playingLoad;
    private CheckBox newInstance;

    private CheckBox adult;
    private CheckBox personOn;
    private RadioGroup gdpr;
    private RadioGroup coppa;
    private RadioGroup conf_env;

    private TextView scene_desc;
    private TextView scene_id;
    private TextView age;
    private TextView user_id;
    private TextView ad_count;
    private CheckBox checkReady;
    private CheckBox canUsePhoneState;
    private CheckBox canUseAndroidId;
    private CheckBox canUseAppList;

    private CheckBox canUseLocation;
    private TextView customAndroidId;
    private TextView customOAID;
    private TextView customLocation;
    private TextView customIMEI;
    private CheckBox disableHide;
    private CheckBox native_image_res;
    private CheckBox enablecustom;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View configView = inflater.inflate(R.layout.configuration_layout, container, false);

        loadUI(configView);


        Button updateBtn = configView.findViewById(R.id.update);
        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String testNo = textNoView.getText().toString();
                updateConfiguration(testNo);


            }
        });

        return configView;
    }

    private void loadUI(View view) {
        textNoView = view.findViewById(R.id.testNo);
        appIdView = view.findViewById(R.id.appIdText);
        appKeyView = view.findViewById(R.id.appKeyText);
        final CheckBox bidTest = view.findViewById(R.id.bidTest);

        bidTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                S2SBiddingUtils.isTest = bidTest.isChecked();
            }
        });
        splashLayout = view.findViewById(R.id.splash_id_layout);
        rewardLayout = view.findViewById(R.id.reward_id_layout);
        interstitialLayout = view.findViewById(R.id.interstitial_id_layout);
        unifiedNativeLayout = view.findViewById(R.id.unified_native_id_layout);
        newInterstitialLayout = view.findViewById(R.id.new_interstitial_id_layout);


        halfSplash = view.findViewById(R.id.halfSplash);
        showWithoutAppInfo = view.findViewById(R.id.show_without_appInfo);

        halfInterstitial = view.findViewById(R.id.halfInterstitial);
        closeSplash = view.findViewById(R.id.closeSplash);
        disableHide = view.findViewById(R.id.disableHide);

        playingLoad = view.findViewById(R.id.playing_load);

        enablecustom = view.findViewById(R.id.enable_custom_device_ctl);
        native_image_res = view.findViewById(R.id.native_image_res);

        newInstance = view.findViewById(R.id.new_instance);
        checkReady = view.findViewById(R.id.check_ready);

        adult = view.findViewById(R.id.adult);
        personOn = view.findViewById(R.id.personOn);
        gdpr = view.findViewById(R.id.gdpr);
        coppa = view.findViewById(R.id.coppa);
        conf_env = view.findViewById(R.id.conf_env);

        canUseAndroidId = view.findViewById(R.id.canUseAndroidId);
        canUseLocation = view.findViewById(R.id.canUseLocation);
        canUsePhoneState = view.findViewById(R.id.canUsePhoneState);
        canUseAppList = view.findViewById(R.id.canUseAppList);

        customAndroidId = view.findViewById(R.id.customAndroidId);
        customOAID = view.findViewById(R.id.customOAID);
        customLocation = view.findViewById(R.id.customLocation);
        customIMEI = view.findViewById(R.id.customIMEI);


        scene_id = view.findViewById(R.id.scene_id);
        scene_desc = view.findViewById(R.id.scene_desc);

        age = view.findViewById(R.id.age);
        user_id = view.findViewById(R.id.user_id);

        ad_count = view.findViewById(R.id.ad_count);

        adult.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WindAds.sharedAds().setAdult(isChecked);
            }
        });

        SeekBar seekBar_w = view.findViewById(R.id.widget_width);
        TextView widget_width_tw = view.findViewById(R.id.widget_width_value);
        TextView widget_height_tw = view.findViewById(R.id.widget_height_value);

        seekBar_w.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                NativeAdDemoRender.widget_width = i;
                widget_width_tw.setText(String.valueOf(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar_w.setProgress(NativeAdDemoRender.widget_width);

        SeekBar seekBar_h = view.findViewById(R.id.widget_height);

        seekBar_h.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                NativeAdDemoRender.widget_height = i;
                widget_height_tw.setText(String.valueOf(i));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar_h.setProgress(NativeAdDemoRender.widget_height);

        personOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                WindAds.sharedAds().setPersonalizedAdvertisingOn(isChecked);
            }
        });

        gdpr.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("WindSDK", "gdpr onCheckedChanged: " + checkedId);
                if (checkedId == R.id.gdpr_unknown) {
                    WindAds.sharedAds().setUserGDPRConsentStatus(WindConsentStatus.UNKNOWN);
                } else if (checkedId == R.id.gdpr_accept) {
                    WindAds.sharedAds().setUserGDPRConsentStatus(WindConsentStatus.ACCEPT);
                } else if (checkedId == R.id.gdpr_denied) {
                    WindAds.sharedAds().setUserGDPRConsentStatus(WindConsentStatus.DENIED);
                }
            }
        });

        coppa.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("coppa", "gdpr onCheckedChanged: " + checkedId);
                if (checkedId == R.id.coppa_unknown) {
                    WindAds.sharedAds().setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.Unknown);
                } else if (checkedId == R.id.coppa_restricted_no) {
                    WindAds.sharedAds().setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.NO);
                } else if (checkedId == R.id.coppa_restricted_yes) {
                    WindAds.sharedAds().setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.YES);
                }
            }
        });
        conf_env.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("sigmob", "config Env onCheckedChanged: " + checkedId);

                WindAdOptions options = WindAds.sharedAds().getOptions();
                if (options != null) {
                    HashMap<String, String> extData = options.getExtData();
                    S2SBiddingUtils.isNormal = false;
                    if (checkedId == R.id.env_default) {
                        if (extData != null) {
                            extData.remove(SIGDEMO_CONF_URL);
                        }
                    } else if (checkedId == R.id.env_mock) {
                        if (extData != null) {
                            extData.put(SIGDEMO_CONF_URL, CONFIG_URL_MOCK);
                        }
                    } else if (checkedId == R.id.env_normal) {
                        if (extData != null) {
                            extData.put(SIGDEMO_CONF_URL, CONFIG_URL_NORMAL);
                        }
                        S2SBiddingUtils.isNormal = true;
                    } else if (checkedId == R.id.env_test) {
                        if (extData != null) {
                            extData.put(SIGDEMO_CONF_URL, CONFIG_URL_TEST);
                        }
                    }

                }

            }
        });


    }

    @Override
    public void onStart() {
        super.onStart();
        loadConfiguration();
    }


    private JSONObject fetchConfigFromServer(Integer id) throws JSONException {

        JSONObject jsonObject = null;
        URL url = null;

        if (id == 0) {

            jsonObject = new JSONObject("{\"msg\":\"\",\"code\":0,\"data\":{\"id\":93,\"appId\":2354,\"appKey\":\"\",\"slotIds\":[{\"adSlotId\":\"e87d16ff5d3\",\"adType\":1,\"bidType\":0},{\"adSlotId\":\"e98817b82f7\",\"adType\":4,\"bidType\":0},{\"adSlotId\":\"e94a8d4b3bb\",\"adType\":2,\"bidType\":0},{\"adSlotId\":\"eef6faa3802\",\"adType\":1,\"bidType\":0},{\"adSlotId\":\"ed2dbff98c9\",\"adType\":5,\"bidType\":0},{\"adSlotId\":\"ec488de9a1b\",\"adType\":1,\"bidType\":1},{\"adSlotId\":\"ec488f21a6d\",\"adType\":4,\"bidType\":1},{\"adSlotId\":\"efe19ad9fd4\",\"adType\":5,\"bidType\":1},{\"adSlotId\":\"efbb3636e92\",\"adType\":1,\"bidType\":1},{\"adSlotId\":\"efbb368fd16\",\"adType\":4,\"bidType\":1},{\"adSlotId\":\"efe19aa94d8\",\"adType\":2,\"bidType\":1},{\"adSlotId\":\"\",\"adType\":1,\"bidType\":0}],\"udid\":\"\",\"displayName\":\"adx安卓+广告主审核测试\"}}");
        } else {
            try {
                url = new URL("http://qatool.sigmob.cn/getSdkParam?id=" + id);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection connection = null;
            if (url != null) {

                try {
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(3000);
                    connection.setReadTimeout(3000);
                    connection.connect();
                    int statusCode = connection.getResponseCode();
                    StringBuffer sbf = new StringBuffer();

                    if (statusCode >= 200 && statusCode < 300) {
                        InputStream inputStream = connection.getInputStream();

                        InputStream is = connection.getInputStream();
                        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                        String strRead = null;
                        while ((strRead = reader.readLine()) != null) {
                            sbf.append(strRead);
                            sbf.append("\r\n");
                        }
                        reader.close();
                        jsonObject = new JSONObject(sbf.toString());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (connection != null) {
                        connection.disconnect();
                    }
                }

            }
        }

        return jsonObject;
    }

    private boolean updateConfigurationByJson(JSONObject configJson) throws JSONException {

        if (configJson == null) {
            return false;
        }

        int code = configJson.optInt("code", -1);

        if (code != 0) {
            return false;
        }

        JSONObject dataJson = configJson.getJSONObject("data");

        appIdView.setText(dataJson.optString("appId"));
        String appKey = dataJson.optString("appKey");
        if (!TextUtils.isEmpty(appKey)) {
            appKeyView.setText(appKey);
        }
        splashLayout.removeAllViews();
        rewardLayout.removeAllViews();
        interstitialLayout.removeAllViews();
        unifiedNativeLayout.removeAllViews();
        newInterstitialLayout.removeAllViews();

        JSONArray array = dataJson.optJSONArray("slotIds");
        if (array != null && array.length() > 0) {
            for (int i = 0; i < array.length(); i++) {

                JSONObject slotId = array.getJSONObject(i);
                if (slotId != null) {
                    int ad_type = slotId.optInt("adType", -1);
                    String adSlotId = slotId.optString("adSlotId");

                    EditText editText = new EditText(rewardLayout.getContext());
                    editText.setText(adSlotId + "_" + ad_type);
                    editText.setEms(10);
                    editText.setMaxLines(1);
                    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(5, 5, 5, 0);

                    if (ad_type == 1) {
                        rewardLayout.addView(editText, layoutParams);
                    } else if (ad_type == 2) {
                        splashLayout.addView(editText, layoutParams);
                    } else if (ad_type == 4) {
                        interstitialLayout.addView(editText, layoutParams);
                    } else if (ad_type == 5) {
                        unifiedNativeLayout.addView(editText, layoutParams);
                    } else if (ad_type == 6) {
                        newInterstitialLayout.addView(editText, layoutParams);
                    }
                }
            }
        }

        onSave(configJson);

        return true;
    }

    private void updateConfiguration(String id) {

        Integer testNo = null;


        try {
            testNo = Integer.valueOf(id);
        } catch (Throwable throwable) {
            onSave(null);
            Toast.makeText(getActivity(), "无效测试ID:" + id, Toast.LENGTH_SHORT).show();
            return;
        }

        if (testNo != null) {
            Toast.makeText(getActivity(), "获取测试ID:" + id + "的配置参数", Toast.LENGTH_SHORT).show();

            UpdateConfigurationTask task = new UpdateConfigurationTask(testNo);

            task.executeOnExecutor(Executors.newFixedThreadPool(1));
        }
    }

    private void loadConfiguration() {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("setting", 0);
        textNoView.setText(sharedPreferences.getString(Constants.CONF_TEST_NO, "测试ID"));
        appIdView.setText(sharedPreferences.getString(Constants.CONF_APP_ID, ""));
        appKeyView.setText(sharedPreferences.getString(Constants.CONF_APP_KEY, "appKey"));

        scene_id.setText(sharedPreferences.getString(Constants.CONF_SCENE_ID, ""));
        scene_desc.setText(sharedPreferences.getString(Constants.CONF_SCENE_DESC, ""));
        age.setText(sharedPreferences.getString(Constants.CONF_AGE, ""));
        user_id.setText(sharedPreferences.getString(Constants.CONF_USER_ID, ""));
        ad_count.setText(String.valueOf(sharedPreferences.getInt(Constants.CONF_AD_COUNT, 3)));

        try {
            int adcount = Integer.parseInt(ad_count.getText().toString());

            Constants.ad_count = adcount;

        } catch (Throwable th) {
        }
        canUseLocation.setChecked(sharedPreferences.getBoolean(Constants.CONF_CAN_LOCATION, true));
        canUseAndroidId.setChecked(sharedPreferences.getBoolean(Constants.CONF_CAN_ANDROIDID, true));
        canUsePhoneState.setChecked(sharedPreferences.getBoolean(Constants.CONF_CAN_PHONESTATE, true));
        canUseAppList.setChecked(sharedPreferences.getBoolean(Constants.CONF_CAN_APPLIST, true));

        customAndroidId.setText(sharedPreferences.getString(Constants.CONF_CUSTOM_ANDROIDID, ""));
        customLocation.setText(sharedPreferences.getString(Constants.CONF_CUSTOM_LOCATION, ""));
        customOAID.setText(sharedPreferences.getString(Constants.CONF_CUSTOM_OAID, ""));
        customIMEI.setText(sharedPreferences.getString(Constants.CONF_CUSTOM_IMEI, ""));

        halfSplash.setChecked(sharedPreferences.getBoolean(Constants.CONF_HALF_SPLASH, false));
        showWithoutAppInfo.setChecked(sharedPreferences.getBoolean(Constants.CONF_SHOW_WITHOUT_APPINFO, false));
        halfInterstitial.setChecked(sharedPreferences.getBoolean(Constants.CONF_HALF_INTERSTITIAL, false));
        closeSplash.setChecked(sharedPreferences.getBoolean(Constants.CONF_CLOSE_SPLASH, false));
        disableHide.setChecked(sharedPreferences.getBoolean(Constants.CONF_DISABLE_AUTOHIDEAD, false));
        enablecustom.setChecked(sharedPreferences.getBoolean(Constants.CONF_CUSTOM_DEVICE, false));


        playingLoad.setChecked(sharedPreferences.getBoolean(Constants.CONF_PLAYING_LOAD, false));
        native_image_res.setChecked(sharedPreferences.getBoolean(Constants.CONF_NATIVE_IMAGE_RES, false));

        native_image_res.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Constants.native_image_res = b;
            }
        });
        newInstance.setChecked(sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false));
        checkReady.setChecked(sharedPreferences.getBoolean(Constants.CONF_CHECK_READY, false));

        adult.setChecked(sharedPreferences.getBoolean(Constants.CONF_ADULT, true));
        personOn.setChecked(sharedPreferences.getBoolean(Constants.CONF_PERSONALIZED, true));

        String gdpr_str = sharedPreferences.getString(Constants.CONF_GDPR, "0");

        switch (gdpr_str) {
            case "0":
                gdpr.check(R.id.gdpr_unknown);
                break;
            case "1":
                gdpr.check(R.id.gdpr_accept);
                break;
            case "2":
                gdpr.check(R.id.gdpr_denied);
                break;
        }

        int coppa_str = sharedPreferences.getInt(Constants.CONF_COPPA, 0);

        switch (coppa_str) {
            case 0:
                coppa.check(R.id.coppa_unknown);
                break;
            case 1:
                coppa.check(R.id.coppa_restricted_yes);
                break;
            case 2:
                coppa.check(R.id.coppa_restricted_no);
                break;
        }

        int conf_url = sharedPreferences.getInt(Constants.CONF_URL, 0);

        switch (conf_url) {
            case 0: {
                conf_env.check(R.id.env_default);
            }
            break;
            case 1: {
                conf_env.check(R.id.env_normal);
            }
            break;
            case 2: {
                conf_env.check(R.id.env_test);
            }
            break;
            case 3: {
                conf_env.check(R.id.env_mock);
            }
            break;
        }

        String configJson = sharedPreferences.getString(Constants.CONF_JSON, "");
        if (!TextUtils.isEmpty(configJson)) {
            try {
                JSONObject jsonObject = new JSONObject(configJson);
                updateConfigurationByJson(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void onSave(JSONObject configJson) {

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("setting", 0);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Constants.CONF_TEST_NO, String.valueOf(textNoView.getText()));
        editor.putString(Constants.CONF_APP_ID, String.valueOf(appIdView.getText()));
        editor.putString(Constants.CONF_APP_KEY, String.valueOf(appKeyView.getText()));

        if (configJson != null) {
            editor.putString(Constants.CONF_JSON, configJson.toString());
        }
        editor.putBoolean(Constants.CONF_CLOSE_SPLASH, closeSplash.isChecked());


        editor.putString(Constants.CONF_CUSTOM_ANDROIDID, String.valueOf(customAndroidId.getText()));
        editor.putString(Constants.CONF_CUSTOM_LOCATION, String.valueOf(customLocation.getText()));
        editor.putString(Constants.CONF_CUSTOM_OAID, String.valueOf(customOAID.getText()));
        editor.putString(Constants.CONF_CUSTOM_IMEI, String.valueOf(customIMEI.getText()));

        editor.putString(Constants.CONF_APP_KEY, String.valueOf(appKeyView.getText()));

        editor.putBoolean(Constants.CONF_CAN_ANDROIDID, canUseAndroidId.isChecked());
        editor.putBoolean(Constants.CONF_CAN_LOCATION, canUseLocation.isChecked());
        editor.putBoolean(Constants.CONF_CAN_PHONESTATE, canUsePhoneState.isChecked());
        editor.putBoolean(Constants.CONF_CAN_APPLIST, canUseAppList.isChecked());

        editor.putBoolean(Constants.CONF_HALF_SPLASH, halfSplash.isChecked());
        editor.putBoolean(Constants.CONF_SHOW_WITHOUT_APPINFO, showWithoutAppInfo.isChecked());

        editor.putBoolean(Constants.CONF_HALF_INTERSTITIAL, halfInterstitial.isChecked());
        editor.putBoolean(Constants.CONF_PLAYING_LOAD, playingLoad.isChecked());
        editor.putBoolean(Constants.CONF_NATIVE_IMAGE_RES, native_image_res.isChecked());
        editor.putBoolean(Constants.CONF_CUSTOM_DEVICE, enablecustom.isChecked());

        editor.putBoolean(Constants.CONF_NEW_INSTANCE, newInstance.isChecked());
        editor.putBoolean(Constants.CONF_CHECK_READY, checkReady.isChecked());
        editor.putBoolean(Constants.CONF_DISABLE_AUTOHIDEAD, disableHide.isChecked());

        editor.putBoolean(Constants.CONF_ADULT, adult.isChecked());
        editor.putBoolean(Constants.CONF_PERSONALIZED, personOn.isChecked());
        editor.putInt(Constants.CONF_AD_COUNT, Integer.parseInt(ad_count.getText().toString()));

        Constants.user_age = age.getText().toString();

        try {
            int user_age = Integer.parseInt(Constants.user_age);
            WindAds.sharedAds().setUserAge(user_age);

        } catch (Throwable th) {

        }

        Constants.scene_id = scene_id.getText().toString();
        Constants.scene_desc = scene_desc.getText().toString();
        Constants.user_id = user_id.getText().toString();
        try {

            int adcount = Integer.parseInt(ad_count.getText().toString());

            Constants.ad_count = adcount;

        } catch (Throwable th) {
        }

        editor.putString(Constants.CONF_SCENE_ID, scene_id.getText().toString());
        editor.putString(Constants.CONF_SCENE_DESC, scene_desc.getText().toString());
        editor.putString(Constants.CONF_AGE, age.getText().toString());
        editor.putString(Constants.CONF_USER_ID, user_id.getText().toString());
        int gdprCheckedId = gdpr.getCheckedRadioButtonId();
        int coppaCheckedId = coppa.getCheckedRadioButtonId();
        int confEnvCheckedId = conf_env.getCheckedRadioButtonId();


        if (gdprCheckedId == R.id.gdpr_unknown) {
            editor.putString(Constants.CONF_GDPR, "0");
        } else if (gdprCheckedId == R.id.gdpr_accept) {
            editor.putString(Constants.CONF_GDPR, "1");
        } else if (gdprCheckedId == R.id.gdpr_denied) {
            editor.putString(Constants.CONF_GDPR, "2");
        }

        if (coppaCheckedId == R.id.coppa_unknown) {
            editor.putInt(Constants.CONF_COPPA, 0);
        } else if (coppaCheckedId == R.id.coppa_restricted_yes) {
            editor.putInt(Constants.CONF_COPPA, 1);
        } else if (coppaCheckedId == R.id.coppa_restricted_no) {
            editor.putInt(Constants.CONF_COPPA, 2);
        }

        if (confEnvCheckedId == R.id.env_default) {
            editor.putInt(Constants.CONF_URL, 0);
        } else if (confEnvCheckedId == R.id.env_normal) {
            editor.putInt(Constants.CONF_URL, 1);
        } else if (confEnvCheckedId == R.id.env_test) {
            editor.putInt(Constants.CONF_URL, 2);
        } else if (confEnvCheckedId == R.id.env_mock) {
            editor.putInt(Constants.CONF_URL, 3);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getActivity().onStateNotSaved();
        }

        editor.apply();
    }

    private class UpdateConfigurationTask extends AsyncTask<Void, Void, JSONObject> {

        Integer testNo = 0;

        UpdateConfigurationTask(Integer id) {
            testNo = id;
        }

        @Override
        protected JSONObject doInBackground(Void... voids) {

            try {
                return fetchConfigFromServer(testNo);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {

            try {
                if (jsonObject != null) {
                    Log.d("WindSDK", "jsonObject:" + jsonObject.toString());
                }
                boolean result = updateConfigurationByJson(jsonObject);
                if (result) {
                    Toast.makeText(getActivity(), "加载测试ID:" + testNo + "的配置参数成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "加载测试ID:" + testNo + "的配置参数失败", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
