package com.gt.sdk.base.view.gif;

class GifFrame {

    /**
     * Index in the raw buffer where we need to start reading to decode.
     */
    int bufferFrameStart;

    /**
     * Delay, in ms, to next frame.
     */
    int delay;

    /**
     * Disposal Method.
     */
    int dispose;

    /**
     * Control Flag.
     */
    boolean interlace;

    int ix, iy, iw, ih;

    /**
     * Local Color Table.
     */
    int[] lct;

    /**
     * Transparency Index.
     */
    int transIndex;

    /**
     * Control Flag.
     */
    boolean transparency;
}
