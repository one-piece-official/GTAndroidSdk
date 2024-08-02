package com.sigmob.sdk.base.models;

public class CurrentAppOrientation {
    private final String orientation ;
    private final boolean locked;

    public CurrentAppOrientation(String orientation, boolean locked) {
        this.orientation = orientation;
        this.locked = locked;
    }

    @Override
    public String toString() {
        return "\"appOrientation\"={" +
                "\"orientation\"=\"" + orientation + '"' +
                ", \"locked\"=" + locked +
                '}';
    }
}
