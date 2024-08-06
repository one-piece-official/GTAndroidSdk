package com.gt.sdk.api;

public interface GtInitCallback {
    void onSuccess();

    void onFail(int code, String msg);
}
