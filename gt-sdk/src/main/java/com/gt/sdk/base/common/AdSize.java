package com.gt.sdk.base.common;

import android.os.Parcel;

/**
 * created by lance on   2022/9/16 : 9:36 上午
 */
public class AdSize {

    private int width;
    private int height;

    public AdSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    protected AdSize(Parcel in) {
        width = in.readInt();
        height = in.readInt();
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
