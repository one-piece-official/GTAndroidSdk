package com.sigmob.windad;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.sigmob.windad.WindAdError.ERROR_SIGMOB_INIT_FAIL;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.Sigmob;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmobError;
import com.sigmob.windad.consent.ConsentStatus;
import com.sigmob.windad.consent.WindAdConsentInformation;

import java.util.logging.Level;


public class WindAds {

    public static final String TRANS_ID = "trans_id";
    public static final String REWARD_TYPE = "reward_type";
    public static final String SERVER_ARRIVED = "server_arrived";
    private static volatile WindAds gInstance;

    private volatile boolean isInit = false;
    private WindAdOptions mOptions;

    public static final String AD_SCENE_ID = Constants.AD_SCENE_ID;
    public static final String AD_SCENE_DESC = Constants.AD_SCENE_DESC;

    public static final String ADX_ID = "_adx_id";

    public static final String CNY = "CNY";
    public static final String USD = "USD";

    //竞胜价格 (单位: 分)
    public static final String AUCTION_PRICE = "AUCTION_PRICE";
    //最高失败出价
    public static final String HIGHEST_LOSS_PRICE = "HIGHEST_LOSS_PRICE";
    //币种，默认CNY，美元：USD
    public static final String CURRENCY = "CURRENCY";

    public static final String LOSS_REASON = "LOSS_REASON";
    public static final String ADN_ID = "ADN_ID";

    private boolean mDebugEnable;
    private Handler mHandler;
    private OnInitializationListener mOnInitializationListener;
    private boolean isAdult = true;
    private int windConsentStatus;
    private boolean isPersonalizedAdvertisingOn = true;
    private int windAgeRestrictedStatus;
    private int userAge;

    private WindAds() {
        this.mOptions = null;
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static WindAds sharedAds() {
        if (gInstance == null) {
            synchronized (WindAds.class) {
                if (gInstance == null) {
                    WindAds ad = new WindAds();
                    ad.setDebugEnable(true);
                    gInstance = ad;
                }
            }
        }
        return gInstance;
    }

    public static void requestPermission(Activity activity) {
        if (activity != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                boolean read_phone_state = ClientMetadata.isPermissionGranted(activity, READ_PHONE_STATE);
                boolean access_fine_location = ClientMetadata.isPermissionGranted(activity, ACCESS_FINE_LOCATION);

                if (!read_phone_state || !access_fine_location) {
                    activity.requestPermissions(new String[]{READ_PHONE_STATE, ACCESS_FINE_LOCATION}, PERMISSION_GRANTED);
                }
            }
        }
    }

    /*
     *  user id -1 is invalid value, default value is -1
     */
    public static String getUserId() {
        return ClientMetadata.getUserId();
    }

    public static void setUserId(String userId) {
        ClientMetadata.setUserId(userId);
    }

    public static String getVersion() {
        return WindConstants.SDK_VERSION;
    }


    public Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler(Looper.getMainLooper());
        }
        return mHandler;
    }


    public String getWindUid() {
        try {
            return ClientMetadata.getUid();
        } catch (Exception e) {
            e.printStackTrace();
            return "Please initialize the SDK properly first";
        }
    }

    public boolean isInit() {
        return isInit;
    }

    public WindAdOptions getOptions() {
        return mOptions;
    }


    public String getSDKToken() {
        if (isInit) {
            return Sigmob.getInstance().getSDKToken();
        }
        return null;
    }

    public boolean startWithOptions(Context context, WindAdOptions options) {
        return startWithOptions(context, options, null);
    }

    private void notifyInitError(String error) {
        SigmobLog.e("startWithOptions " + error);

        if (mOnInitializationListener != null) {
            mOnInitializationListener.OnInitializationFail(error);
        }
        throw new RuntimeException(error);

    }

    private void notifyInitSuccess() {
        if (mOnInitializationListener != null) {
            mOnInitializationListener.OnInitializationSuccess();
        }
    }

    public int getCommonVersion() {
        try {
            return Constants.getVersion();

        } catch (Throwable t) {

        }

        return Constants.SDK_VERSION;
    }

    protected int getSIGSDKVERSION() {
        return Constants.SIG_VERSION;
    }

    public synchronized boolean startWithOptions(Context context, WindAdOptions options,
                                                 OnInitializationListener onInitializationListener) {

        mOnInitializationListener = onInitializationListener;

        if (!isInit) {
            try {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    notifyInitError("Wind SDK Only Support SigmobAndroid API 18+");
                    return false;
                }

                if (context == null) {
                    notifyInitError("context is null ");
                    return false;
                }

                if (options == null) {
                    notifyInitError("WindAdOptions is null ");
                    return false;
                }

                if (TextUtils.isEmpty(options.getAppId())) {
                    notifyInitError("appId is empty ");
                    return false;
                }
//                if (getSIGSDKVERSION() != WindConstants.SIG_VERSION) {
//                    notifyInitError("Your SIG COMMON Lib version must be equal or higher" +
//                            " than " + WindConstants.SIG_VERSION + " ,current Version :[ "
//                            + getSIGSDKVERSION() + " ]");
//                    return false;
//                }
                if (getCommonVersion() < WindConstants.COMMON_VERSION) {
                    notifyInitError("Your COMMON Lib version must be equal or higher" +
                            " than " + WindConstants.COMMON_VERSION + " ,current Version :[ "
                            + getCommonVersion() + " ]");
                    return false;
                }
                mOptions = options;

                SDKContext.init(context.getApplicationContext());
                loadPrivacyInfo();
                Sigmob.getInstance().init();

                isInit = true;
                SigmobLog.i("init appId: " + options.getAppId());

            } catch (Throwable th) {
                SigmobLog.e("startWithOptions fail", th);
            }

        } else {
            SigmobLog.i("already init appId: " + options.getAppId());
        }
        notifyInitSuccess();
        return true;
    }


    public int getAgeRestrictedStatus() {
        return windAgeRestrictedStatus;
    }

    public int getUserGDPRConsentStatus() {
        return windConsentStatus;
    }

    private void loadUserGDPRConsentStatus() {

        if (windConsentStatus == WindConsentStatus.UNKNOWN) {
            try {
                windConsentStatus = PrivacyManager.getInstance().getGDPRConsentStatus();

            } catch (Throwable th) {

            }

            if (windConsentStatus == WindConsentStatus.UNKNOWN) {

                try {
                    Context context = SDKContext.getApplicationContext();
                    ConsentStatus consentStatus = WindAdConsentInformation.getInstance(context).getConsentStatus();
                    switch (consentStatus) {
                        case ACCEPT: {
                            windConsentStatus = WindConsentStatus.ACCEPT;
                        }
                        break;
                        case DENIED: {
                            windConsentStatus = WindConsentStatus.DENIED;
                        }
                        break;
                        case UNKNOWN: {

                        }
                        break;
                    }

                } catch (Throwable th) {

                }
            }
        }

        PrivacyManager.getInstance().setGDPRConsentStatus(windConsentStatus, false);

    }

    private void loadCOPPA() {
        try {

            if (windAgeRestrictedStatus == WindAgeRestrictedUserStatus.Unknown) {
                windAgeRestrictedStatus = PrivacyManager.getInstance().getAge_restricted();
            } else {
                PrivacyManager.getInstance().setAge_restricted(windAgeRestrictedStatus, false);
            }

            if (userAge == 0) {
                userAge = PrivacyManager.getInstance().getUserAge();
            } else {
                PrivacyManager.getInstance().setUserAge(userAge, false);
            }

        } catch (Throwable th) {

        }
    }

    private void loadPrivacyInfo() {
        try {

            loadCOPPA();
            loadUserGDPRConsentStatus();

            PrivacyManager.getInstance().setPersonalizedAdvertisingOn(isPersonalizedAdvertisingOn, false);
            PrivacyManager.getInstance().setAdult(isAdult, false);

        } catch (Throwable th) {
            SigmobLog.e("loadPrivacyInfo", th);

        }
    }

    public boolean isDebugEnable() {
        return mDebugEnable;
    }

    public void setDebugEnable(boolean enable) {
        mDebugEnable = enable;
        if (WindConstants.IS_DEBUG) {
            SigmobLog.setSdkHandlerLevel(enable ? Level.FINE : Level.SEVERE);
        } else {
            SigmobLog.setSdkHandlerLevel(enable ? Level.INFO : Level.SEVERE);
        }
    }

    /**
     * 初始化SDK配置
     */
    public void debugDeviceID() {
        boolean findId = false;
        try {
            String deviceId = ClientMetadata.getInstance().getDeviceId();
            if (!TextUtils.isEmpty(deviceId)) {
                SigmobLog.i(String.format("debug device Type: IMEI,  ID => %s", deviceId));
                findId = true;
            }

            String gaid = ClientMetadata.getInstance().getAdvertisingId();
            if (!TextUtils.isEmpty(gaid)) {
                SigmobLog.i(String.format("debug device Type: gaid, ID => %s", gaid));
                findId = true;
            }

            String oaid = ClientMetadata.getInstance().getOAID();
            if (!TextUtils.isEmpty(oaid)) {
                SigmobLog.i(String.format("debug device Type oaid, ID => %s", gaid));
                findId = true;
            }
        } catch (Throwable t) {

        }
        if (!findId) {
            SigmobLog.e(String.format("can't find any can be used debug valid Device Type"));
        }
    }


    public void setAdult(boolean isAdult) {
        this.isAdult = isAdult;
        SigmobLog.i("Windads -> setAdult " + isAdult);

        if (isInit) {
            PrivacyManager.getInstance().setAdult(isAdult, true);
        }
    }

    public void setUserGDPRConsentStatus(int windConsentStatus) {

        this.windConsentStatus = windConsentStatus;

        if (isInit) {
            PrivacyManager.getInstance().setGDPRConsentStatus(windConsentStatus, true);
        }

    }

    public void setPersonalizedAdvertisingOn(boolean isPersonalizedAdvertisingOn) {
        this.isPersonalizedAdvertisingOn = isPersonalizedAdvertisingOn;
        SigmobLog.i("Windads -> setPersonalized " + isPersonalizedAdvertisingOn);

        if (isInit) {
            PrivacyManager.getInstance().setPersonalizedAdvertisingOn(isPersonalizedAdvertisingOn, true);
        }
    }

    public boolean isAdult() {
        return isAdult;
    }

    public boolean isPersonalizedAdvertisingOn() {
        return isPersonalizedAdvertisingOn;
    }

    public void setIsAgeRestrictedUser(int windAgeRestrictedStatus) {
        this.windAgeRestrictedStatus = windAgeRestrictedStatus;
        if (isInit) {
            PrivacyManager.getInstance().setAge_restricted(windAgeRestrictedStatus, true);
        }
    }

    public int getUserAge() {
        return userAge;
    }

    public void setUserAge(int userAge) {
        this.userAge = userAge;
        if (isInit) {
            PrivacyManager.getInstance().setUserAge(userAge, true);
        }
    }

    public String getAppId() {
        if (mOptions != null) {
            return mOptions.getAppId();
        }
        return null;
    }

    public void setOAIDCertFileName(String fileName) {
        ClientMetadata.setOaidCertFileName(fileName);
    }

    public static void setOAIDCertPem(String certPem) {
        try {
            ClientMetadata.setOAIDCertPem(certPem);
        } catch (Throwable th) {
            SigmobLog.e("not support OAID Module");
        }
    }

    public String getAppKey() {
        if (mOptions != null) {
            return mOptions.getAppKey();
        }
        return null;
    }

}
