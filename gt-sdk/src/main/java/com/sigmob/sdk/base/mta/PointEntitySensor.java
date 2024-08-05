package com.sigmob.sdk.base.mta;


public final class PointEntitySensor extends PointEnitySigmobBase {

    private String motion_before;
    private String motion_after;
    private String motion_interval;

    public String getMotion_before() {
        return motion_before;
    }

    public void setMotion_before(String motion_before) {
        this.motion_before = motion_before;
    }

    public String getMotion_after() {
        return motion_after;
    }

    public void setMotion_after(String motion_after) {
        this.motion_after = motion_after;
    }

    public String getMotion_interval() {
        return motion_interval;
    }

    public void setMotion_interval(String motion_interval) {
        this.motion_interval = motion_interval;
    }

}
