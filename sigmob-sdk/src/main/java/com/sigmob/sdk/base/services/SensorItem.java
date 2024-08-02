package com.sigmob.sdk.base.services;

public class SensorItem implements Cloneable {
    public float[] gravity;
    public float[] gyroscope;
    public float[] magneticField;
    public float[] accelerometer;
    public float[] light;
    public float[] linear_acceleration;
    public long timestamp;


    @Override
    protected SensorItem clone() throws CloneNotSupportedException {
        return (SensorItem) super.clone();

    }


}
