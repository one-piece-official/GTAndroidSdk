package com.sigmob.sdk.splash;

public enum ImageType {
    ImageTypeUnknow(0),
    ImageTypeJPEG(1),
    ImageTypePNG(2),
    ImageTypeGIF(3),
    SMImageTypeWEBP(4),
    ImageTypeMP4(5);

    private int mImageType;

    ImageType(int type){
        mImageType = type;
    }


    public int getImageType() {
        return mImageType;
    }
}
