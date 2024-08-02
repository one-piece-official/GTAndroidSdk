package com.sigmob.sdk;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WindAdLog {



    public static void i(String tag, String log) {
        Log.i(tag,log);
    }
    public static void iLog(String tag, String log) {
        String property = System.getProperty("line.separator");
        try {
            if (log.startsWith("{")) {
                log = new JSONObject(log).toString(4);
            } else if (log.startsWith("[")) {
                log = new JSONArray(log).toString(4);
            }
        } catch (JSONException e2) {
        }
        String str3 = "╔═══════════════════════════════════════════════════════════════════════════════════════";
        String[] split = log.split(property);
        for (int i2 = 0; i2 < split.length; i2++) {
            str3 = (str3 + "\n") + "║ " + split[i2];
        }
        Log.i(tag, " \n".concat(String.valueOf(str3 + "\n╚═══════════════════════════════════════════════════════════════════════════════════════")));
    }

}
