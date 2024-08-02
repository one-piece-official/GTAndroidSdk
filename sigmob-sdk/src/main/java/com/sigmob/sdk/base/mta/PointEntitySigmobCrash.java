package com.sigmob.sdk.base.mta;

import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.mta.PointEntityCrash;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.windad.WindAds;

import java.util.List;

public class PointEntitySigmobCrash extends PointEntityCrash {

    public static PointEntitySigmobCrash WindCrash(String crash) {

        PointEntitySigmobCrash sigmobCrash = new PointEntitySigmobCrash();
        sigmobCrash.setAc_type(PointType.SIGMOB_CRASH);
        sigmobCrash.setCategory(PointCategory.CRASH);
        sigmobCrash.setCrashMessage(crash);
        return sigmobCrash;
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