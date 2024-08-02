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
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MraidStorageManager {

    private static Map<String, String> sessionStorage = new HashMap<>();

    private Map<String, ValueChangeListener> localValueChange = new HashMap<>();

    private Map<String, ValueChangeListener> sessionValueChange = new HashMap<>();

    private Context context;

    MraidStorageManager(Context context) {
        this.context = context;
    }

    public void setItem(int type, String key, String value) {
        if (type == 1) {//localStorage
            String o = (String) MraidLocalStorage.get(context, key, "");
            if (!o.equals(value)) {

                ValueChangeListener valueChangeListener = localValueChange.get(key);
                if (valueChangeListener != null) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("key", key);
                        json.put("newValue", value);
                        json.put("oldValue", o);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    valueChangeListener.valueChange(json);
                }
            }

            MraidLocalStorage.put(context, key, value);
        } else {//sessionStorage
            String s = sessionStorage.get(key) == null ? "" : sessionStorage.get(key);
            if (!s.equals(value)) {

                ValueChangeListener valueChangeListener = sessionValueChange.get(key);
                if (valueChangeListener != null) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("key", key);
                        json.put("newValue", value);
                        json.put("oldValue", s);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    valueChangeListener.valueChange(json);
                }
            }

            sessionStorage.put(key, value);
        }
    }

    public String getItem(int type, String key) {
        if (type == 1) {
            return (String) MraidLocalStorage.get(context, key, "");
        } else {
            return sessionStorage.get(key) == null ? "" : sessionStorage.get(key);
        }
    }

    public void removeItem(int type, String key) {
        if (type == 1) {
            String o = (String) MraidLocalStorage.get(context, key, "");
            if (!TextUtils.isEmpty(o)) {
                ValueChangeListener valueChangeListener = localValueChange.get(key);
                if (valueChangeListener != null) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("key", key);
                        json.put("newValue", "");
                        json.put("oldValue", o);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    valueChangeListener.valueChange(json);
                }
            }

            MraidLocalStorage.remove(context, key);
        } else {

            String s = sessionStorage.get(key);
            if (!TextUtils.isEmpty(s)) {
                ValueChangeListener valueChangeListener = sessionValueChange.get(key);
                if (valueChangeListener != null) {
                    JSONObject json = new JSONObject();
                    try {
                        json.put("key", key);
                        json.put("newValue", "");
                        json.put("oldValue", s);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    valueChangeListener.valueChange(json);
                }
            }

            sessionStorage.remove(key);
        }
    }

    public void clear(int type) {
        if (type == 1) {
            Map<String, String> all = (Map<String, String>) MraidLocalStorage.getAll(context);
            if (all != null && all.size() > 0) {
                for (Map.Entry<String, String> entry : all.entrySet()) {
                    ValueChangeListener valueChangeListener = localValueChange.get(entry.getKey());
                    if (valueChangeListener != null) {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("key", entry.getKey());
                            json.put("newValue", "");
                            json.put("oldValue", entry.getValue());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        valueChangeListener.valueChange(json);
                    }
                }
            }

            MraidLocalStorage.clear(context);
        } else {

            if (sessionStorage != null && sessionStorage.size() > 0) {
                for (Map.Entry<String, String> entry : sessionStorage.entrySet()) {
                    ValueChangeListener valueChangeListener = sessionValueChange.get(entry.getKey());
                    if (valueChangeListener != null) {
                        JSONObject json = new JSONObject();
                        try {
                            json.put("key", entry.getKey());
                            json.put("newValue", "");
                            json.put("oldValue", entry.getValue());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        valueChangeListener.valueChange(json);
                    }
                }

                sessionStorage.clear();
            }
        }
    }

    public int length(int type) {
        if (type == 1) {
            return MraidLocalStorage.getAll(context).size();
        } else {
            return sessionStorage.size();
        }
    }

    public void addEventListener(int type, String key, ValueChangeListener listener) {
        if (type == 1) {//localStorage//sessionStorage
            localValueChange.put(key, listener);
        } else {
            sessionValueChange.put(key, listener);
        }
    }

    public interface ValueChangeListener {
        void valueChange(JSONObject object);
    }

}
