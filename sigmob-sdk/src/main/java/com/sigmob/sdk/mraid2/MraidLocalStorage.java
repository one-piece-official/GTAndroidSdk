/*
 * Copyright © 2017 Hubcloud.com.cn. All rights reserved.
 * SharedPreferencesUtils.java
 * AdHubSDK
 * Use of this source code is governed by a BSD-style license that can be
 * found in the LICENSE file.
 *
 */

package com.sigmob.sdk.mraid2;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.Map;
import java.util.Set;

public class MraidLocalStorage {

    // File name store on internal storage.
    private static final String FILE_NAME = "mraid_storage";

    // Save data.
    public static void put(Context context, String key, Object obj) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (sp != null) {
            Editor editor = sp.edit();

            if (obj instanceof Boolean) {
                editor.putBoolean(key, (Boolean) obj);
            } else if (obj instanceof Float) {
                editor.putFloat(key, (Float) obj);
            } else if (obj instanceof Integer) {
                editor.putInt(key, (Integer) obj);
            } else if (obj instanceof Long) {
                editor.putLong(key, (Long) obj);
            } else if (obj instanceof String) {
                editor.putString(key, (String) obj);
            } else if (obj instanceof Set) {
                // A workaround to fix putStringSet bug.
                editor.remove(key);
                editor.putStringSet(key, (Set<String>) obj);
            }
            editor.apply();
        }
    }

    // Fetch data.
    public static Object get(Context context, String key, Object defaultObj) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (sp == null)
            return null;

        if (defaultObj instanceof Boolean) {
            return sp.getBoolean(key, (Boolean) defaultObj);
        } else if (defaultObj instanceof Float) {
            return sp.getFloat(key, (Float) defaultObj);
        } else if (defaultObj instanceof Integer) {
            return sp.getInt(key, (Integer) defaultObj);
        } else if (defaultObj instanceof Long) {
            return sp.getLong(key, (Long) defaultObj);
        } else if (defaultObj instanceof String) {
            return sp.getString(key, (String) defaultObj);
        } else if (defaultObj instanceof Set) {
            return sp.getStringSet(key, (Set<String>) defaultObj);
        }
        return null;
    }


    // Fetch data.
    public static String getString(Context context, String key) {
        if (context != null) {
            SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
            if (sp == null) {
                return null;
            }
            return sp.getString(key, null);
        }
        return null;
    }

    // Delete data.
    public static void remove(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (sp != null) {
            Editor editor = sp.edit();
            editor.remove(key);
            editor.apply();
        }
    }

    // Return all the key-value pairs.
    public static Map<String, ?> getAll(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (sp != null)
            return sp.getAll();

        return null;
    }

    // Clear all data.
    public static void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        if (sp != null) {
            Editor editor = sp.edit();
            editor.clear();
            editor.apply();
        }
    }

    // Check if we have specialised key.
    public static boolean contains(Context context, String key) {
        SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
        return sp != null && sp.contains(key);
    }

}
