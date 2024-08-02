package com.windmill.demo;


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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.windmill.sdk.WindMillAd;
import com.windmill.sdk.WindMillConsentStatus;
import com.windmill.sdk.WindMillUserAgeStatus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Executors;


public class ConfigurationFragment extends Fragment {

    private TextView textNoView;
    private TextView userIdView;
    private TextView appIdView;

    private LinearLayout splashLayout;
    private LinearLayout rewardLayout;
    private LinearLayout interstitialLayout;
    private LinearLayout unifiedNativeLayout;
    private LinearLayout bannerLayout;


    private CheckBox sdkRelease;

    private CheckBox halfSplash;
    private CheckBox selfLogo;

    private CheckBox playingLoad;
    private CheckBox newInstance;

    private CheckBox adult;
    private CheckBox personOn;
    private CheckBox sdkLog;
    private RadioGroup gdpr;
    private RadioGroup coppa;

    private CheckBox location;
    private CheckBox phoneState;
    private CheckBox androidId;
    private CheckBox wifiState;
    private CheckBox writeExternal;
    private CheckBox appList;
    private CheckBox permissionRecordAudio;

    private TextView locationView;
    private TextView phoneStateView;
    private TextView macAddressView;
    private TextView androidIdView;
    private TextView oaidView;

    private JSONObject deviceInfo = new JSONObject();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        userIdView = view.findViewById(R.id.userIdText);
        appIdView = view.findViewById(R.id.appIdText);

        splashLayout = view.findViewById(R.id.splash_id_layout);
        rewardLayout = view.findViewById(R.id.reward_id_layout);
        interstitialLayout = view.findViewById(R.id.interstitial_id_layout);
        unifiedNativeLayout = view.findViewById(R.id.unified_native_id_layout);
        bannerLayout = view.findViewById(R.id.banner_id_layout);

        sdkRelease = view.findViewById(R.id.sdk_is_release);

        halfSplash = view.findViewById(R.id.halfSplash);
        selfLogo = view.findViewById(R.id.appHide);

        playingLoad = view.findViewById(R.id.playing_load);
        newInstance = view.findViewById(R.id.new_instance);


        adult = view.findViewById(R.id.adult);
        personOn = view.findViewById(R.id.personOn);
        sdkLog = view.findViewById(R.id.start_sdk_log);
        gdpr = view.findViewById(R.id.gdpr);
        coppa = view.findViewById(R.id.coppa);

        adult.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "adult onCheckedChanged: " + isChecked);
                WindMillAd.sharedAds().setAdult(isChecked);
            }
        });

        personOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "personOn onCheckedChanged: " + isChecked);
                WindMillAd.sharedAds().setPersonalizedAdvertisingOn(isChecked);
            }
        });

        sdkLog.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "sdkLog onCheckedChanged: " + isChecked);
                WindMillAd.sharedAds().setDebugEnable(isChecked);
            }
        });

        gdpr.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("lance", "gdpr onCheckedChanged: " + checkedId);
                if (checkedId == R.id.gdpr_unknown) {
                    WindMillAd.sharedAds().setUserGDPRConsentStatus(WindMillConsentStatus.UNKNOWN);
                } else if (checkedId == R.id.gdpr_accept) {
                    WindMillAd.sharedAds().setUserGDPRConsentStatus(WindMillConsentStatus.ACCEPT);
                } else if (checkedId == R.id.gdpr_denied) {
                    WindMillAd.sharedAds().setUserGDPRConsentStatus(WindMillConsentStatus.DENIED);
                }
            }
        });

        coppa.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.d("lance", "coppa onCheckedChanged: " + checkedId);
                if (checkedId == R.id.coppa_unknown) {
                    WindMillAd.sharedAds().setIsAgeRestrictedUser(WindMillUserAgeStatus.WindAgeRestrictedStatusUnknown);
                } else if (checkedId == R.id.coppa_restricted_no) {
                    WindMillAd.sharedAds().setIsAgeRestrictedUser(WindMillUserAgeStatus.WindAgeRestrictedStatusNO);
                } else if (checkedId == R.id.coppa_restricted_yes) {
                    WindMillAd.sharedAds().setIsAgeRestrictedUser(WindMillUserAgeStatus.WindAgeRestrictedStatusYES);
                }
            }
        });

        location = view.findViewById(R.id.is_use_location);
        phoneState = view.findViewById(R.id.is_use_phoneState);
        androidId = view.findViewById(R.id.is_use_androidId);
        wifiState = view.findViewById(R.id.is_use_wifiState);
        writeExternal = view.findViewById(R.id.is_use_writeExternal);
        appList = view.findViewById(R.id.is_use_appList);
        permissionRecordAudio = view.findViewById(R.id.is_use_permissionRecordAudio);

        locationView = view.findViewById(R.id.location_text);
        phoneStateView = view.findViewById(R.id.imei_text);
        macAddressView = view.findViewById(R.id.macAddress_text);
        androidIdView = view.findViewById(R.id.androidId_text);
        oaidView = view.findViewById(R.id.oaid_text);

        location.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "location onCheckedChanged: " + isChecked);
                try {
                    deviceInfo.put("isCanUseLocation", isChecked);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        phoneState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "phoneState onCheckedChanged: " + isChecked);
                try {
                    deviceInfo.put("isCanUsePhoneState", isChecked);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        androidId.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "androidId onCheckedChanged: " + isChecked);
                try {
                    deviceInfo.put("isCanUseAndroidId", isChecked);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        wifiState.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "wifiState onCheckedChanged: " + isChecked);
                try {
                    deviceInfo.put("isCanUseWifiState", isChecked);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        writeExternal.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "writeExternal onCheckedChanged: " + isChecked);
                try {
                    deviceInfo.put("isCanUseWriteExternal", isChecked);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        appList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "appList onCheckedChanged: " + isChecked);
                try {
                    deviceInfo.put("isCanUseAppList", isChecked);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        permissionRecordAudio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d("lance", "permissionRecordAudio onCheckedChanged: " + isChecked);
                try {
                    deviceInfo.put("isCanUsePermissionRecordAudio", isChecked);
                } catch (JSONException e) {
                    e.printStackTrace();
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

        splashLayout.removeAllViews();
        rewardLayout.removeAllViews();
        interstitialLayout.removeAllViews();
        unifiedNativeLayout.removeAllViews();
        bannerLayout.removeAllViews();

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
                    } else if (ad_type == 7) {
                        bannerLayout.addView(editText, layoutParams);
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
        userIdView.setText(sharedPreferences.getString(Constants.CONF_USER_ID, "userId"));
        appIdView.setText(sharedPreferences.getString(Constants.CONF_APP_ID, "appId"));

        sdkRelease.setChecked(sharedPreferences.getBoolean(Constants.CONF_SDK_RELEASE, false));

        selfLogo.setChecked(sharedPreferences.getBoolean(Constants.CONF_SELF_LOGO, false));
        halfSplash.setChecked(sharedPreferences.getBoolean(Constants.CONF_HALF_SPLASH, false));

        playingLoad.setChecked(sharedPreferences.getBoolean(Constants.CONF_PLAYING_LOAD, false));
        newInstance.setChecked(sharedPreferences.getBoolean(Constants.CONF_NEW_INSTANCE, false));

        adult.setChecked(sharedPreferences.getBoolean(Constants.CONF_ADULT, true));
        personOn.setChecked(sharedPreferences.getBoolean(Constants.CONF_PERSONALIZED, true));
        sdkLog.setChecked(sharedPreferences.getBoolean(Constants.CONF_SDK_LOG, true));

        String gdpr_str = sharedPreferences.getString(Constants.CONF_GDPR, "0");

        switch (gdpr_str) {
            case "0":
                ((RadioButton) gdpr.findViewById(R.id.gdpr_unknown)).setChecked(true);
                break;
            case "1":
                ((RadioButton) gdpr.findViewById(R.id.gdpr_accept)).setChecked(true);
                break;
            case "2":
                ((RadioButton) gdpr.findViewById(R.id.gdpr_denied)).setChecked(true);
                break;
        }

        int coppa_str = sharedPreferences.getInt(Constants.CONF_COPPA, 0);

        switch (coppa_str) {
            case 0:
                ((RadioButton) coppa.findViewById(R.id.coppa_unknown)).setChecked(true);
                break;
            case 1:
                ((RadioButton) coppa.findViewById(R.id.coppa_restricted_yes)).setChecked(true);
                break;
            case 2:
                ((RadioButton) coppa.findViewById(R.id.coppa_restricted_no)).setChecked(true);
                break;
        }


        String custom_info = sharedPreferences.getString(Constants.CONF_CUSTOM_DEVICE_INFO, null);
        Log.d("lance", "------------custom_info-----------:" + custom_info);
        if (!TextUtils.isEmpty(custom_info)) {
            try {
                JSONObject custom_json = new JSONObject(custom_info);
                location.setChecked(custom_json.optBoolean("isCanUseLocation"));
                phoneState.setChecked(custom_json.optBoolean("isCanUsePhoneState"));
                androidId.setChecked(custom_json.optBoolean("isCanUseAndroidId"));
                wifiState.setChecked(custom_json.optBoolean("isCanUseWifiState"));
                writeExternal.setChecked(custom_json.optBoolean("isCanUseWriteExternal"));
                appList.setChecked(custom_json.optBoolean("isCanUseAppList"));
                permissionRecordAudio.setChecked(custom_json.optBoolean("isCanUsePermissionRecordAudio"));

                locationView.setText(custom_json.optString("getLocation"));
                phoneStateView.setText(custom_json.optString("getDevImei"));
                androidIdView.setText(custom_json.optString("getAndroidId"));
                macAddressView.setText(custom_json.optString("getMacAddress"));
                oaidView.setText(custom_json.optString("getDevOaid"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            location.setChecked(true);
            phoneState.setChecked(true);
            androidId.setChecked(true);
            wifiState.setChecked(true);
            writeExternal.setChecked(true);
            appList.setChecked(true);
            permissionRecordAudio.setChecked(true);
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
        editor.putString(Constants.CONF_USER_ID, String.valueOf(userIdView.getText()));
        editor.putString(Constants.CONF_APP_ID, String.valueOf(appIdView.getText()));

        editor.putString(Constants.CONF_JSON, configJson.toString());

        editor.putBoolean(Constants.CONF_SDK_RELEASE, sdkRelease.isChecked());

        editor.putBoolean(Constants.CONF_SELF_LOGO, selfLogo.isChecked());
        editor.putBoolean(Constants.CONF_HALF_SPLASH, halfSplash.isChecked());

        editor.putBoolean(Constants.CONF_PLAYING_LOAD, playingLoad.isChecked());
        editor.putBoolean(Constants.CONF_NEW_INSTANCE, newInstance.isChecked());

        editor.putBoolean(Constants.CONF_ADULT, adult.isChecked());
        editor.putBoolean(Constants.CONF_PERSONALIZED, personOn.isChecked());
        editor.putBoolean(Constants.CONF_SDK_LOG, sdkLog.isChecked());

        if (gdpr.getCheckedRadioButtonId() == R.id.gdpr_unknown) {
            editor.putString(Constants.CONF_GDPR, "0");
        } else if (gdpr.getCheckedRadioButtonId() == R.id.gdpr_accept) {
            editor.putString(Constants.CONF_GDPR, "1");
        } else if (gdpr.getCheckedRadioButtonId() == R.id.gdpr_denied) {
            editor.putString(Constants.CONF_GDPR, "2");
        }

        if (coppa.getCheckedRadioButtonId() == R.id.coppa_unknown) {
            editor.putInt(Constants.CONF_COPPA, 0);
        } else if (coppa.getCheckedRadioButtonId() == R.id.coppa_restricted_yes) {
            editor.putInt(Constants.CONF_COPPA, 1);
        } else if (coppa.getCheckedRadioButtonId() == R.id.coppa_restricted_no) {
            editor.putInt(Constants.CONF_COPPA, 2);
        }

        try {
            deviceInfo.put("isCanUseLocation", location.isChecked());
            deviceInfo.put("isCanUsePhoneState", phoneState.isChecked());
            deviceInfo.put("isCanUseAndroidId", androidId.isChecked());
            deviceInfo.put("isCanUseWifiState", wifiState.isChecked());
            deviceInfo.put("isCanUseWriteExternal", writeExternal.isChecked());
            deviceInfo.put("isCanUseAppList", appList.isChecked());
            deviceInfo.put("isCanUsePermissionRecordAudio", permissionRecordAudio.isChecked());

            deviceInfo.put("getLocation", locationView.getText());
            deviceInfo.put("getDevImei", phoneStateView.getText());
            deviceInfo.put("getAndroidId", androidIdView.getText());
            deviceInfo.put("getMacAddress", macAddressView.getText());
            deviceInfo.put("getDevOaid", oaidView.getText());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("lance", "-------save-----deviceInfo-----------:" + deviceInfo.toString());

        editor.putString(Constants.CONF_CUSTOM_DEVICE_INFO, deviceInfo.toString());

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
                    Log.d("lance", "jsonObject:" + jsonObject.toString());
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
