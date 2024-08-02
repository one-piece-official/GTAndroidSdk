package com.sigmob.sdk.base.models;

import android.graphics.Rect;

import java.util.List;

public class ExposureChange {

    float exposedPercentage;
    Rect visibleRectangle;
    List occlusionRectangles;

    public ExposureChange(float exposedPercentage, Rect visibleRectangle, List occlusionRectangles) {
        this.exposedPercentage = exposedPercentage;
        this.visibleRectangle = visibleRectangle;
        this.occlusionRectangles = occlusionRectangles;
    }

    @Override
    public String toString() {
        return "\"exposureChange\"={" +
                "\"exposedPercentage\"=" + exposedPercentage +
                ", \"visibleRectangle\"={" +
                "\"x\"="+visibleRectangle.left + "," +
                "\"y\"=" +visibleRectangle.top+ "," +
                "\"width\"="+visibleRectangle.width()+ "," +
                "\"height\"="+visibleRectangle.height()+ "}" +
                ", \"occlusionRectangles\"=" + "[]" +
                '}';
    }
}
