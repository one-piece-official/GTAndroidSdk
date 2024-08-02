package com.sigmob.sdk.mraid2;

import java.util.HashMap;

public interface MraidBridgeMotionListener {

    void notifyMotionEvent(String uniqueId, String type, String event, HashMap<String, Object> args);
}
