package com.gt.sdk;

public class GtConstants {

    public static final String SDK_VERSION = "1.0.0";
    public static final String SDK_FOLDER = "gts";
    public static final boolean ENCRYPT = true;
    public static final String SUCCESS = "1";
    public static final String FAIL = "0";

    public static final String INIT_STATUS = "init_status";

    public static final String CUSTOM_INFO = "custom_info";

    public static final String APP_ID = "appId";
    public static final String PLACEMENT_ID = "placementId";

    public static final String APP_NAME = "appName";

    public static final String LOAD_ID = "loadId";
    public static final String IMAGE_TYPE = "imageType";
    public static final String AD_SIZE = "adSize";
    public static final String BANNER_TYPE = "bannerType";
    public static final String LOAD_TIMEOUT = "load_timeout";


    public static final String AD_SCENE_ID = "scene_id";
    public static final String AD_SCENE_DESC = "scene_desc";
    public static final String AD_WIDTH = "ad_key_width";
    public static final String AD_HEIGHT = "ad_key_height";
    public static final int AUTO_SIZE = 0;
    public static final String BID_FLOOR = "bidFloor";
    public static final String E_CPM = "eCpm";

    public static final String ENABLEJS = "c2V0SmF2YVNjcmlwdEVuYWJsZWQ=";
    public static final String ENABLEFILE = "c2V0QWxsb3dGaWxlQWNjZXNz";

    /**
     * bidding广告类型
     */
    public static final int AD_TYPE_NORMAL = -1;
    public static final int AD_TYPE_SERVER_BIDING = 0;
    public static final int AD_TYPE_CLIENT_BIDING = 1;

    /**
     * 开屏广告类型
     */
    public static final int AD_SPLASH_TYPE_NORMAL = 0;
    public static final int AD_SPLASH_TYPE_EYE = 1;

    /**
     * 原生广告类型
     */
    public static final int AD_NATIVE_TYPE_EXPRESS = 0;
    public static final int AD_NATIVE_TYPE_CUSTOM = 1;
    public static final int AD_NATIVE_TYPE_NORMAL = 1;
    public static final int AD_NATIVE_TYPE_DRAW = 2;
    public static final int AD_NATIVE_TYPE_UNKNOWN = -1;

    /**
     * 原生广告交互类型
     */
    public static final int INTERACTION_TYPE_UNKNOWN = 0;
    public static final int INTERACTION_TYPE_DOWNLOAD = 1;
    public static final int INTERACTION_TYPE_BROWSER = 2;

    /**
     * 插屏广告类型
     */
    public static final int AD_INTERSTITIAL_TYPE_HALF = 0;
    public static final int AD_INTERSTITIAL_TYPE_FULL = 1;
    public static final int AD_INTERSTITIAL_TYPE_NEW = 2;
    public static final int AD_INTERSTITIAL_TYPE_UNKNOWN = -1;

    /**
     * 基于ToBid_Sdk适配器的版本
     */
    public static final int TO_BID_CUSTOM_ADAPTER_VERSION_2 = 2;

}
