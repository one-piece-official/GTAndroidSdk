package com.sigmob.sdk.videoAd;

public enum VideoTrackingEvent {

    START("start"),
    FIRST_QUARTILE("firstQuartile"),
    MIDPOINT("midpoint"),
    THIRD_QUARTILE("thirdQuartile"),
    COMPLETE("complete"),
    COMPANION_AD_VIEW("companionAdView"),
    COMPANION_AD_CLICK("companionAdClick"),
    FINISH("finish"),
    SHOW("show"),
    CLICK("click"),
    UNKNOWN("");

    private final String name;

    VideoTrackingEvent( final String name) {
        this.name = name;
    }


    public static VideoTrackingEvent fromString( final String name) {
        if (name == null) {
            return UNKNOWN;
        }

        for (VideoTrackingEvent event : VideoTrackingEvent.values()) {
            if (name.equals(event.getName())) {
                return event;
            }
        }

        return UNKNOWN;
    }


    private String getName() {
        return name;
    }

}
