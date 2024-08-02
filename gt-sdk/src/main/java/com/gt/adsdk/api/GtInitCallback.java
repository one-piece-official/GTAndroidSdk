package com.gt.adsdk.api;

public interface GtInitCallback {
    void onSuccess();

    void onFail(int code, String msg);
}
