package com.gt.sdk.base.models;

public class SigImage {
    private String imageUrl;
    private int width;
    private int height;

    public SigImage(String imageUrl, int width, int height) {
        this.imageUrl = imageUrl;
        this.width = width;
        this.height = height;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getWidth() {
        return width;
    }


    public int getHeight() {
        return height;
    }

}
