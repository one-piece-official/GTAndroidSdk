package com.gt.sdk;

public enum WindMillConsentStatus {

    UNKNOWN(0),
    ACCEPT(1),
    DENIED(2);

    private int mValue;

    WindMillConsentStatus(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
