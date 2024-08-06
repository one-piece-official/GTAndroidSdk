package com.gt.sdk.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.utils.Preconditions;
import com.gt.sdk.GtConstants;

public final class GtSharedPreUtil {

    private final static String DEFAULT_PREFERENCE_NAME = "com." + GtConstants.SDK_FOLDER + ".sdk";

    private GtSharedPreUtil() {

    }

    public static SharedPreferences getSharedPreferences(final Context context) {
        Preconditions.NoThrow.checkNotNull(context);
        return context.getSharedPreferences(DEFAULT_PREFERENCE_NAME, MODE_PRIVATE);
    }

    public static SharedPreferences getSharedPreferences(final Context context, final String preferenceName) {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(preferenceName);
        return context.getSharedPreferences(preferenceName, MODE_PRIVATE);
    }


}
