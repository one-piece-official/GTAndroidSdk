package com.sigmob.sigmob;

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
import android.widget.TextView;
import android.widget.Toast;

import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindConsentStatus;

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
    private TextView appIdView;
    private TextView appKeyView;
    private TextView bidTokenView;

    private LinearLayout splashLayout;
    private LinearLayout rewardLayout;
    private LinearLayout interstitialLayout;
    private LinearLayout unifiedNativeLayout;

    private CheckBox halfSplash;
    private CheckBox mediation;
    private CheckBox selfLogo;
    private CheckBox gdpr;
    private CheckBox adult;
    private CheckBox personOn;

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
        bidTokenView = view.findViewById(R.id.bidTokenText);

        splashLayout = view.findViewById(R.id.splash_id_layout);
        rewardLayout = view.findViewById(R.id.reward_id_layout);
        interstitialLayout = view.findViewById(R.id.interstitial_id_layout);
        unifiedNativeLayout = view.findViewById(R.id.unified_native_id_layout);


        halfSplash = view.findViewById(R.id.halfSplash);
        mediation = view.findViewById(R.id.mediation);
        selfLogo = view.findViewById(R.id.appHide);

        gdpr = view.findViewById(R.id.gdpr);
        adult = view.findViewById(R.id.adult);
        personOn = view.findViewById(R.id.personOn);

        gdpr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    WindAds.sharedAds().setUserGDPRConsentStatus(WindConsentStatus.ACCEPT);
                    com.xmlywind.windad.WindAds.sharedAds().setUserGDPRConsentStatus(com.xmlywind.windad.WindConsentStatus.ACCEPT);
                } else {
                    WindAds.sharedAds().setUserGDPRConsentStatus(WindConsentStatus.DENIED);
                    com.xmlywind.windad.WindAds.sharedAds().setUserGDPRConsentStatus(com.xmlywind.windad.WindConsentStatus.DENIED);
                }
            }
        });
        adult.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    WindAds.sharedAds().setAdult(true);
                    com.xmlywind.windad.WindAds.sharedAds().setAdult(true);
                } else {
                    WindAds.sharedAds().setAdult(false);
                    com.xmlywind.windad.WindAds.sharedAds().setAdult(false);
                }
            }
        });
        personOn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    WindAds.sharedAds().setPersonalizedAdvertisingOn(true);
                    com.xmlywind.windad.WindAds.sharedAds().setPersonalizedAdvertisingOn(true);
                } else {
                    WindAds.sharedAds().setPersonalizedAdvertisingOn(false);
                    com.xmlywind.windad.WindAds.sharedAds().setPersonalizedAdvertisingOn(false);
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
        String appKey = dataJson.optString("appKey");
        if (!TextUtils.isEmpty(appKey)) {
            appKeyView.setText(appKey);
        }
        splashLayout.removeAllViews();
        rewardLayout.removeAllViews();
        interstitialLayout.removeAllViews();
        unifiedNativeLayout.removeAllViews();

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
        appIdView.setText(sharedPreferences.getString(Constants.CONF_APP_ID, "appId"));
        appKeyView.setText(sharedPreferences.getString(Constants.CONF_APP_KEY, "appKey"));
        bidTokenView.setText(sharedPreferences.getString(Constants.CONF_BID_TOKEN, "bidToken"));

        selfLogo.setChecked(sharedPreferences.getBoolean(Constants.CONF_SELF_LOGO, false));
        halfSplash.setChecked(sharedPreferences.getBoolean(Constants.CONF_HALF_SPLASH, false));
        mediation.setChecked(sharedPreferences.getBoolean(Constants.USE_MEDIATION, false));

        gdpr.setChecked(sharedPreferences.getBoolean(Constants.CONF_GDPR, false));
        adult.setChecked(sharedPreferences.getBoolean(Constants.CONF_ADULT, true));
        personOn.setChecked(sharedPreferences.getBoolean(Constants.CONF_PERSONALIZED, true));

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
        editor.putString(Constants.CONF_BID_TOKEN, String.valueOf(bidTokenView.getText()));

        editor.putString(Constants.CONF_JSON, configJson.toString());

        editor.putBoolean(Constants.CONF_SELF_LOGO, selfLogo.isChecked());
        editor.putBoolean(Constants.CONF_HALF_SPLASH, halfSplash.isChecked());
        editor.putBoolean(Constants.USE_MEDIATION, mediation.isChecked());

        editor.putBoolean(Constants.CONF_GDPR, gdpr.isChecked());
        editor.putBoolean(Constants.CONF_ADULT, adult.isChecked());
        editor.putBoolean(Constants.CONF_PERSONALIZED, personOn.isChecked());

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

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}
