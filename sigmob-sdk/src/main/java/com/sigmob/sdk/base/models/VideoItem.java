package com.sigmob.sdk.base.models;

public class VideoItem {
    public final String url;
    public final int width;
    public final int height;

    public VideoItem(String url, int width, int height) {
        this.url = url;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "\"video\":{" +
                "\"url\"=\"" + url + '"' +
                ", \"width\"=" + width +
                ", \"height\"=" + height +
                '}';
    }


}
