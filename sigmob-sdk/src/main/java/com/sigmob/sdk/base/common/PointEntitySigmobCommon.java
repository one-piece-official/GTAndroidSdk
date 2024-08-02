package com.sigmob.sdk.base.common;

import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.mta.PointEntityCommon;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.windad.WindAds;

import java.util.List;

public class PointEntitySigmobCommon extends PointEntityCommon {


    private String is_custom_imei;
    private String is_custom_android_id;
    private String is_custom_oaid;

    public String getIs_custom_imei() {
        return is_custom_imei;
    }

    public void setIs_custom_imei(String is_custom_imei) {
        this.is_custom_imei = is_custom_imei;
    }

    public String getIs_custom_android_id() {
        return is_custom_android_id;
    }

    public void setIs_custom_android_id(String is_custom_android_id) {
        this.is_custom_android_id = is_custom_android_id;
    }

    public String getIs_custom_oaid() {
        return is_custom_oaid;
    }

    public void setIs_custom_oaid(String is_custom_oaid) {
        this.is_custom_oaid = is_custom_oaid;
    }


    @Override
    public DeviceContext getDeviceContext() {
        return SDKContext.getDeviceContext();
    }

    @Override
    public boolean isAcTypeBlock() {
        if (!PrivacyManager.getInstance().canCollectPersonalInformation()) {
            return true;
        }
        List<Integer> blackList = WindSDKConfig.getInstance().getLogBlackList();

        for (Integer acType : blackList) {

            if (getAc_type().equals(String.valueOf(acType))) {

                SigmobLog.e("black ac type " + getAc_type());
                return true;
            }
        }

        return false;
    }


    @Override
    public String getSdkversion() {
        return WindConstants.SDK_VERSION;
    }


    @Override
    public String appId() {
        return WindAds.sharedAds().getAppId();
    }
}
