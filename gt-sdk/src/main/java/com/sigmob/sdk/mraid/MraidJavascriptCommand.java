// Copyright 2018-2019 Sigmob, Inc.
// Licensed under the Sigmob SDK License Agreement
// http://www.sigmob.com/legal/sdk-license-agreement/

package com.sigmob.sdk.mraid;


import com.sigmob.sdk.base.models.PlacementType;

public enum MraidJavascriptCommand {
    CLOSE("close"),
    EXPAND("expand") {
        @Override
        boolean requiresClick(final PlacementType placementType) {
            return placementType == PlacementType.INLINE;
        }
    },
    USE_CUSTOM_CLOSE("usecustomclose"),
    OPEN("open") {
        @Override
        boolean requiresClick(final PlacementType placementType) {
            return true;
        }
    },
    feedBack("feedback") {
        @Override
        boolean requiresClick(final PlacementType placementType) {
            return true;
        }
    },
    UNLOAD("unload"),
    OPENFOURELEMENTS("openFourElements"),
    RESIZE("resize") {
        @Override
        boolean requiresClick(final PlacementType placementType) {
            return true;
        }
    },
    SET_ORIENTATION_PROPERTIES("setOrientationProperties"),
    PLAY_VIDEO("playVideo") {
        @Override
        boolean requiresClick(final PlacementType placementType) {
            return placementType == PlacementType.INLINE;
        }
    },
    STORE_PICTURE("storePicture") {
        @Override
        boolean requiresClick(final PlacementType placementType) {
            return true;
        }
    },
    CREATE_CALENDAR_EVENT("createCalendarEvent") {
        @Override
        boolean requiresClick(final PlacementType placementType) {
            return true;
        }
    },
    VPAID("vpaid") {
        @Override
        boolean requiresClick(final PlacementType placementType) {
            return true;
        }
    },
    EXTENSION("extension") {
        @Override
        boolean requiresClick(final PlacementType placementType) {
            return true;
        }
    },
//    MOTIONVIEW("motionView") {
//        @Override
//        boolean requiresClick(final PlacementType placementType) {
//            return true;
//        }
//    },
//    MOTION("motion") {
//        @Override
//        boolean requiresClick(final PlacementType placementType) {
//            return true;
//        }
//    },

    UNSPECIFIED("");

    private final String mJavascriptString;

    MraidJavascriptCommand(String javascriptString) {
        mJavascriptString = javascriptString;
    }

    public static MraidJavascriptCommand fromJavascriptString(String string) {
        for (MraidJavascriptCommand command : MraidJavascriptCommand.values()) {
            if (command.mJavascriptString.equals(string)) {
                return command;
            }
        }

        return UNSPECIFIED;
    }

    public String toJavascriptString() {
        return mJavascriptString;
    }

    boolean requiresClick(PlacementType placementType) {
        return false;
    }
}
