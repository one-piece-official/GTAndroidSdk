package com.sigmob.sdk.base.services;

public enum SensorADEvent {

    SENSOR_EVENT_BEGIN(1),
    SENSOR_EVENT_END(2),
    SENSOR_EVENT_PUSH(3);


    private final int mValue;

    SensorADEvent(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
