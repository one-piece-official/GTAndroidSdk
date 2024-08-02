package com.sigmob.sdk.mraid2;

import android.text.TextUtils;

import java.util.HashMap;

public class MRAID2ControllerCache {


    private HashMap<String, Mraid2Controller> cache = new HashMap<>();
    private static final MRAID2ControllerCache instance = new MRAID2ControllerCache();

    private MRAID2ControllerCache() {

    }

    public static MRAID2ControllerCache getInstance() {
        return instance;
    }


    public void addController(String uuid, Mraid2Controller value) {
        if (TextUtils.isEmpty(uuid)) return;
        cache.put(uuid, value);
    }

    public Mraid2Controller pop(String uuid) {
        if (TextUtils.isEmpty(uuid)) return null;
        Mraid2Controller mraid2Controller = cache.get(uuid);
        cache.remove(uuid);
        return mraid2Controller;
    }

    public Mraid2Controller getController(String uuid) {

        if (TextUtils.isEmpty(uuid)) return null;
        Mraid2Controller mraid2Controller = cache.get(uuid);
        return mraid2Controller;
    }
}
