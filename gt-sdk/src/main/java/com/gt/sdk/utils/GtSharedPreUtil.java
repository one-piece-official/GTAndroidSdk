package com.gt.sdk.utils;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.czhj.sdk.common.utils.Preconditions;

public final class GtSharedPreUtil {

    public static final String DEFAULT_PREFERENCE_NAME = "GtAdSettings";

    private GtSharedPreUtil() {

    }

    /**
     * key placementId
     * key channelId
     *
     * @param context
     * @return
     */
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
