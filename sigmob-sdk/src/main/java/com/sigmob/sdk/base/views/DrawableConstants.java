package com.sigmob.sdk.base.views;

import android.graphics.Color;
import android.graphics.Paint;

public class DrawableConstants {


    public static final int BlueColor = Color.rgb(77,130,239);
    public static final int GrayColor = Color.rgb(140,141,142);

    public static class ProgressBar {
        public static final int HEIGHT_DIPS = 2;
        public static final int NUGGET_WIDTH_DIPS = 4;

        public static final int BACKGROUND_COLOR = Color.WHITE;
        public static final int BACKGROUND_ALPHA = 128;
        public static final Paint.Style BACKGROUND_STYLE = Paint.Style.FILL;

        public static final int PROGRESS_COLOR = BlueColor;
        public static final int PROGRESS_ALPHA = 255;
        public static final Paint.Style PROGRESS_STYLE = Paint.Style.FILL;
    }

    public static class CloseButton {
        public static final int WIDGET_HEIGHT_DIPS = 60;
        public static final int EDGE_PADDING = 0;
        public static final int IMAGE_PADDING_DIPS = 5;

        public static final int STROKE_COLOR = Color.WHITE;
        public static final float STROKE_WIDTH = 8f;
        public static final Paint.Cap STROKE_CAP = Paint.Cap.ROUND;

    }

}
