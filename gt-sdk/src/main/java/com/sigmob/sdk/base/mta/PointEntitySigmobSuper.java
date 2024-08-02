package com.sigmob.sdk.base.mta;

import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.mta.PointEntitySuper;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.windad.WindAds;

import java.util.List;

public class PointEntitySigmobSuper extends PointEntitySuper {
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
    public String appId() {
        return WindAds.sharedAds().getAppId();
    }


    @Override
    public String getSdkversion() {
        return WindConstants.SDK_VERSION;
    }
}
