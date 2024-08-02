package com.gt.adsdk.api;

import android.location.Location;

import java.util.List;

public abstract class GtCustomController {
    public GtCustomController() {
    }

    public boolean canReadLocation() {
        return true;
    }

    public Location getLocation() {
        return null;
    }

    public boolean canUsePhoneState() {
        return true;
    }

    public String getImei() {
        return "";
    }

    public boolean canUseAndroidId() {
        return true;
    }

    public String getAndroidId() {
        return "";
    }


    public boolean canUseWifiState() {
        return true;
    }

    public String getMacAddress() {
        return "";
    }

    public boolean canUseWriteExternal() {
        return true;
    }

    public boolean canReadInstalledPackages() {
        return true;
    }

    public List<String> getInstalledPackages() {
        return null;
    }

    public String getOaid() {
        return "";
    }
}
