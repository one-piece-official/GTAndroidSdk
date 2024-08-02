package com.sigmob.sdk.mraid;

import android.content.Context;
import android.view.View;

import org.json.JSONObject;

public abstract class MraidObject {
    protected String uniqueId;

    protected MraidObject(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    String parentId;

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public abstract View getView();

    public interface VpaidBridgeListener {
        void OnVpaidInit(Context context, JSONObject args);

        void OnVpaidAssetURL(JSONObject args);

        void OnVpaidPlay(JSONObject args);

        void OnVpaidReplay(JSONObject args);

        void OnVpaidPause(JSONObject args);

        void OnVpaidStop(JSONObject args);

        void OnVpaidMuted(JSONObject args);

        void OnVpaidSeek(JSONObject args);

        void OnVpaidFrame(JSONObject args);
    }

    public abstract void destroy();
}
