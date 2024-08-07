package com.gt.sdk.base.common;

import android.text.TextUtils;
import android.view.MotionEvent;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.track.BaseMacroCommon;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.GtAdSdk;

public class SigMacroCommon extends BaseMacroCommon {
    public static final String _WIDTH_ = "_WIDTH_";
    public static final String _HEIGHT_ = "_HEIGHT_";

    public static final String _SBZMX_ = "_SBZMX_";
    public static final String _SBZMY_ = "_SBZMY_";
    public static final String _SBZCX_ = "_SBZCX_";
    public static final String _SBZCY_ = "_SBZCY_";

    public static final String _AZMX_ = "_AZMX_";
    public static final String _AZMY_ = "_AZMY_";
    public static final String _AZCX_ = "_AZCX_";
    public static final String _AZCY_ = "_AZCY_";

    public static final String _ABZMX_ = "_ABZMX_";
    public static final String _ABZMY_ = "_ABZMY_";
    public static final String _ABZCX_ = "_ABZCX_";
    public static final String _ABZCY_ = "_ABZCY_";

    public static final String _DPLINK_ = "_DPLINK_";
    public static final String _CLICK_ID_ = "_CLICK_ID_";
    public static final String _PRICE_ = "_PRICE_";

    public static final String __SLD__ = "__SLD__";

    public static final String __X_MAX_ACC__ = "__X_MAX_ACC__";
    public static final String __Y_MAX_ACC__ = "__Y_MAX_ACC__";
    public static final String __Z_MAX_ACC__ = "__Z_MAX_ACC__";

    public static final String __TURN_X__ = "__TURN_X__";
    public static final String __TURN_Y__ = "__TURN_Y__";
    public static final String __TURN_Z__ = "__TURN_Z__";

    public static final String __TURN_TIME__ = "__TURN_TIME__";

    public static final String _DPNAME_ = "_DPNAME_";
    public static final String _LATITUDE_ = "_LATITUDE_";
    public static final String _LONGITUDE_ = "__TURN_Z__";

    private static final long serialVersionUID = 1L;

    private static String getMacroValue(String macroName) {
        try {
            switch (macroName) {
                case _WIDTH_:
                    return String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip());
                case _HEIGHT_:
                    return String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip());
                default:
                    return "unFind";
            }
        } catch (Throwable e) {
            return "unFind";
        }
    }


    public void updateClickMarco(MotionEvent down, MotionEvent up) {
        MotionEvent _down = down;
        MotionEvent _up = up;
        if (_down == null && _up == null) {
            return;
        } else if (_up == null) {
            _up = _down;
        } else if (_down == null) {
            _down = _up;
        }

        addMarcoKey(SigMacroCommon._SBZMX_, String.valueOf(Dips.pixelsToIntDips(_down.getX(), GtAdSdk.sharedAds().getContext())));
        addMarcoKey(SigMacroCommon._SBZMY_, String.valueOf(Dips.pixelsToIntDips(_down.getY(), GtAdSdk.sharedAds().getContext())));
        addMarcoKey(SigMacroCommon._SBZCX_, String.valueOf(Dips.pixelsToIntDips(_up.getX(), GtAdSdk.sharedAds().getContext())));
        addMarcoKey(SigMacroCommon._SBZCY_, String.valueOf(Dips.pixelsToIntDips(_up.getY(), GtAdSdk.sharedAds().getContext())));

        addMarcoKey(SigMacroCommon._AZMX_, String.valueOf(Dips.pixelsToIntDips(_down.getX(), GtAdSdk.sharedAds().getContext())));
        addMarcoKey(SigMacroCommon._AZMY_, String.valueOf(Dips.pixelsToIntDips(_down.getY(), GtAdSdk.sharedAds().getContext())));
        addMarcoKey(SigMacroCommon._AZCX_, String.valueOf(Dips.pixelsToIntDips(_up.getX(), GtAdSdk.sharedAds().getContext())));
        addMarcoKey(SigMacroCommon._AZCY_, String.valueOf(Dips.pixelsToIntDips(_up.getY(), GtAdSdk.sharedAds().getContext())));

        addMarcoKey(SigMacroCommon._ABZMX_, String.valueOf(Dips.pixelsToIntDips(_down.getRawX(), GtAdSdk.sharedAds().getContext())));
        addMarcoKey(SigMacroCommon._ABZMY_, String.valueOf(Dips.pixelsToIntDips(_down.getRawY(), GtAdSdk.sharedAds().getContext())));
        addMarcoKey(SigMacroCommon._ABZCX_, String.valueOf(Dips.pixelsToIntDips(_up.getRawX(), GtAdSdk.sharedAds().getContext())));
        addMarcoKey(SigMacroCommon._ABZCY_, String.valueOf(Dips.pixelsToIntDips(_up.getRawY(), GtAdSdk.sharedAds().getContext())));
    }

    @Override
    public String replaceWithDefault(String key) {
        String value = super.replaceWithDefault(key);
        SigmobLog.d("macroProcess() called with:" + "[" + key + "]" + "[" + value + "]");
        if (!TextUtils.isEmpty(value) && !value.equals("unFind")) {
            return value;
        } else {
            try {
                value = getMacroValue(key);
                SigmobLog.d("macroProcess() called with: " + "[" + key + "]" + "[" + value + "]");

                if (!TextUtils.isEmpty(value) && !value.equals("unFind")) {
                    return value;
                }
            } catch (Throwable e) {
                SigmobLog.e(e.getMessage());
            }
        }
        return null;
    }

}
