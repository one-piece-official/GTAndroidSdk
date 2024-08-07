package com.gt.sdk.base.view.gif;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused"})
class GifHeader {

    int bgColor;

    // Background color index.
    int bgIndex;

    GifFrame currentFrame;

    int frameCount = 0;

    List<GifFrame> frames = new ArrayList<>();

    int[] gct = null;

    // 1 : global color table flag.
    boolean gctFlag;

    // 2-4 : color resolution.
    // 5 : gct sort flag.
    // 6-8 : gct size.
    int gctSize;

    // Full image height.
    int height;

    int loopCount = 0;

    // Pixel aspect ratio.
    int pixelAspect;

    int status = GifDecoder.STATUS_OK;

    // Logical screen size.
    // Full image width.
    int width;

    public int getHeight() {
        return height;
    }

    public int getNumFrames() {
        return frameCount;
    }

    /**
     * Global status code of GIF data parsing.
     */
    public int getStatus() {
        return status;
    }

    public int getWidth() {
        return width;
    }
}