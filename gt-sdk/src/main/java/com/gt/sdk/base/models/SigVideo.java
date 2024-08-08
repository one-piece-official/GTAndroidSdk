package com.gt.sdk.base.models;

import java.io.Serializable;

public class SigVideo implements Serializable {
    int width;
    int height;
    String url;
    String thumbUrl;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public String getVideoUrl() {
        return url;
    }
}
