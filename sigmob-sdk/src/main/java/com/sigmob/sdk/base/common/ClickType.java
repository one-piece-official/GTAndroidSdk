package com.sigmob.sdk.base.common;

public enum ClickType {
    Button(1),
    FullScreen(2);

    private final int mType;

    ClickType(int type) {
        mType = type;
    }

    public int getValue() {
        return mType;
    }
}
