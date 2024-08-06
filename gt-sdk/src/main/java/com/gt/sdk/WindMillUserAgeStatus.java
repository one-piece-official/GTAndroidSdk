package com.gt.sdk;

public enum WindMillUserAgeStatus {

    WindAgeRestrictedStatusUnknown(0),
    WindAgeRestrictedStatusYES(1),
    WindAgeRestrictedStatusNO(2);

    private int mValue;

    WindMillUserAgeStatus(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
