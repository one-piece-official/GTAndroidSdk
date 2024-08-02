package com.sigmob.sdk.base.models;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.volley.toolbox.StringUtil;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;

public class MraidEnv {
    public final String version = "2.0";
    public final String sdk = StringUtil.decode(StringUtil.s);
    public final String sdkVersion = WindConstants.SDK_VERSION;
    public final String appId = ClientMetadata.getInstance().getAppPackageName();
    public final String ifa = ClientMetadata.getInstance().getAdvertisingId();
    public final Boolean limitAdTracking = ClientMetadata.getInstance().getLimitAdTrackingEnabled();
    public final Boolean coppa = PrivacyManager.getInstance().getAge_restricted() == 1;
}
