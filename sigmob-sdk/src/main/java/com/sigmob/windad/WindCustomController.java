package com.sigmob.windad;

import android.content.pm.PackageInfo;
import android.location.Location;

import java.util.List;

public abstract class WindCustomController {
    /**
     * 是否允许SDK主动使用地理位置信息
     *
     * @return true可以获取，false禁止获取。默认为true
     */
    public boolean isCanUseLocation() {
        return true;
    }

    /**
     * 当isCanUseLocation=false时，可传入地理位置信息，穿山甲sdk使用您传入的地理位置信息
     *
     * @return 地理位置参数
     */
    public Location getLocation() {
        return null;
    }

    /**
     * 是否允许SDK主动使用手机硬件参数，如：imei
     *
     * @return true可以使用，false禁止使用。默认为true
     */
    public boolean isCanUsePhoneState() {
        return true;
    }

    /**
     * 当isCanUsePhoneState=false时，可传入imei信息，穿山甲sdk使用您传入的imei信息
     *
     * @return imei信息
     */
    public String getDevImei() {
        return null;
    }

    /**
     * 开发者可以传入oaid
     *
     * @return oaid
     */
    public String getDevOaid() {
        return null;
    }

    /**
     * 是否允许SDK主动使用手机硬件参数，如：android
     *
     * @return true可以使用，false禁止使用。默认为true
     */
    public boolean isCanUseAndroidId() {
        return true;
    }

    /**
     * isCanUseAndroidId=false时，可传入android信息，ToBid使用您传入的android信息
     *
     * @return android信息
     */
    public String getAndroidId() {
        return null;
    }

    /**
     * isCanUseAppList=false时, 将关闭应用上传应用列表
     *
     * @return true可以使用，false 禁止使用
     */
    public boolean isCanUseAppList() {
        return true;
    }

    public List<PackageInfo> getInstallPackageInfoList(){
        return null;
    }
}
