package com.sigmob.sdk.base.utils;

import static android.content.Context.MODE_MULTI_PROCESS;
import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;

import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.WindConstants;

public final class WindPrefsUtils {

//    private static final String DEFAULT_PREFERENCE_NAME = "com.Sigmob.Settings";

    private static String DEFAULT_PREFERENCE_NAME = "com." + WindConstants.SDK_FOLDER + ".Settings";

    private WindPrefsUtils() {
    }

    public static SharedPreferences getPrefs() {

        return SDKContext.getApplicationContext().
                getSharedPreferences(DEFAULT_PREFERENCE_NAME, MODE_PRIVATE | MODE_MULTI_PROCESS);
    }

    public static String getStringValueWithKey(String key,String defaultValue) {

        return SDKContext.getApplicationContext().
                getSharedPreferences(DEFAULT_PREFERENCE_NAME,
                        MODE_PRIVATE | MODE_MULTI_PROCESS).
                getString(key,defaultValue);
    }

    public static int getIntValueWithKey(String key,int defaultValue) {

        return SDKContext.getApplicationContext().
                getSharedPreferences(DEFAULT_PREFERENCE_NAME,
                        MODE_PRIVATE | MODE_MULTI_PROCESS).
                getInt(key,defaultValue);
    }
    public static SharedPreferences getPrefs(final String preferenceName) {


        return SDKContext.getApplicationContext()
                .getSharedPreferences(preferenceName, MODE_PRIVATE | MODE_MULTI_PROCESS);
    }

}
