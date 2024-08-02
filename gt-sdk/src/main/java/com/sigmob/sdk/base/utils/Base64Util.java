package com.sigmob.sdk.base.utils;

import android.util.Base64;

public class Base64Util {
    public static String decodeBase64String(String str) {
        if (str == null) {
            return null;
        }
        return new String(Base64.decode(str, Base64.NO_WRAP));
    }

}
