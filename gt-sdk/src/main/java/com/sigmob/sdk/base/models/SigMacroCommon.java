package com.sigmob.sdk.base.models;

import android.text.TextUtils;
import android.view.MotionEvent;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.track.BaseMacroCommon;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.TouchLocation;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;

public class SigMacroCommon extends BaseMacroCommon {

    public static final String _BEGINTIME_ = "_BEGINTIME_";
    public static final String _PLAYFIRSTFRAME_ = "_PLAYFIRSTFRAME_";
    public static final String _SCENE_ = "_SCENE_";
    public static final String _TYPE_ = "_TYPE_";
    public static final String _BEHAVIOR_ = "_BEHAVIOR_";
    public static final String _STATUS_ = "_STATUS_";
    public static final String _SLOTWIDTH_ = "_SLOTWIDTH_";
    public static final String _SLOTHEIGHT_ = "_SLOTHEIGHT_";
    public static final String _WIDTH_ = "_WIDTH_";
    public static final String _HEIGHT_ = "_HEIGHT_";
    public static final String _DOWNX_ = "_DOWNX_";
    public static final String _DOWNY_ = "_DOWNY_";
    public static final String _UPX_ = "_UPX_";
    public static final String _UPY_ = "_UPY_";
    public static final String _COMPLETED_ = "_COMPLETED_";
    public static final String _PROGRESS_ = "_PROGRESS_";
    public static final String _VIDEOTIME_ = "_VIDEOTIME_";
    public static final String _ENDTIME_ = "_ENDTIME_";
    public static final String _CLICKID_ = "_CLICKID_";
    public static final String _PLAYLASTFRAME_ = "_PLAYLASTFRAME_";
    public static final String _SHOWSKIPTIME_ = "_SHOWSKIPTIME_";
    public static final String _VMD5_ = "_VMD5_";
    public static final String _VURL_ = "_VURL_";
    public static final String _ADSCENE_ = "_ADSCENE_";
    public static final String _ADSCENEID_ = "_ADSCENEID_";
    public static final String _SETCLOSETIME_ = "_SETCLOSETIME_";
    public static final String _IS_TRUNCATION_ = "_ISTRUNCATION_";
    //    public static final String _AUTOCLICK_ = "_AUTOCLICK_";
    public static final String _CLICKAREA_ = "_CLICKAREA_";
    public static final String _CLICKSCENE_ = "_CLICKSCENE_";
    public static final String _FINALCLICK_ = "_FINALCLICK_";

    public static final String _CURRENCY_ = "_CURRENCY_";
    public static final String _PUBLISHERPRICE_ = "_PUBLISHERPRICE_";
    public static final String _HIGHESTLOSSPRICE_ = "_HIGHESTLOSSPRICE_";
    public static final String _TEMPLATE_ = "_TEMPLATE_";
    private static final long serialVersionUID = 1L;

    private static String getMacroValue(String macroName) {

        try {
            switch (macroName) {
                case _TYPE_:
                    return "1"; //播放类型。1 - 第一次播放;2 - 暂停后继续播放;3 - 重新开始播放。
                case _SCENE_:
                    return "0"; //播放场景。1 - 在广告曝光区域播放;2 - 全屏竖屏、只展示视频;3 - 全屏竖屏、屏幕上方展示 视频、下方展示广告推广目标 网页；4 - 全屏横屏、只展示视频;0 - 其它开发者自定义场景
                case _STATUS_:
                    return "0"; //播放状态:0 - 正常播放;1 - 视频加载中;2 - 播放错误。
                case _BEHAVIOR_:
                    return "2"; //播放行为:1 - 自动播放;2 - 点击播放
                case _BEGINTIME_:
                    return "0";//视频播放开始时间，单位为 秒。如果视频从头开始播放， 则为0。
                case _SLOTWIDTH_:
                case _WIDTH_:
                    return String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip());
                case _SLOTHEIGHT_:
                case _HEIGHT_:
                    return String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip());
                default:
                    return "unFind";
            }
        } catch (Throwable e) {
            return "unFind";
        }

    }

    public void updateClickMarco(TouchLocation down, TouchLocation up) {

        TouchLocation _down = down;
        TouchLocation _up = up;

        if (_down == null && _up == null) {
            return;
        } else if (_up == null) {

            _up = _down;
        } else if (_down == null) {
            _down = _up;

        }

        addMarcoKey(SigMacroCommon._DOWNX_, String.valueOf(Dips.pixelsToIntDips(_down.getX(), SDKContext.getApplicationContext())));
        addMarcoKey(SigMacroCommon._DOWNY_, String.valueOf(Dips.pixelsToIntDips(_down.getY(), SDKContext.getApplicationContext())));
        addMarcoKey(SigMacroCommon._UPX_, String.valueOf(Dips.pixelsToIntDips(_up.getX(), SDKContext.getApplicationContext())));
        addMarcoKey(SigMacroCommon._UPY_, String.valueOf(Dips.pixelsToIntDips(_up.getY(), SDKContext.getApplicationContext())));

    }


    public String getCoordinate() {
        return String.format("%s,%s,%s,%s", getMarcoKey(SigMacroCommon._DOWNX_), getMarcoKey(SigMacroCommon._DOWNY_), getMarcoKey(SigMacroCommon._UPX_), getMarcoKey(SigMacroCommon._UPY_));
    }

    public static String getCoordinate(MotionEvent down, MotionEvent up, boolean isRaw) {
        if (isRaw) {
            return String.format("%d,%d,%d,%d", Dips.pixelsToIntDips(down.getRawX(), SDKContext.getApplicationContext()),
                    Dips.pixelsToIntDips(down.getRawY(), SDKContext.getApplicationContext()),
                    Dips.pixelsToIntDips(up.getRawX(), SDKContext.getApplicationContext()),
                    Dips.pixelsToIntDips(up.getRawX(), SDKContext.getApplicationContext()));

        } else {
            return String.format("%d,%d,%d,%d", Dips.pixelsToIntDips(down.getX(), SDKContext.getApplicationContext()),
                    Dips.pixelsToIntDips(down.getY(), SDKContext.getApplicationContext()),
                    Dips.pixelsToIntDips(up.getX(), SDKContext.getApplicationContext()),
                    Dips.pixelsToIntDips(up.getY(), SDKContext.getApplicationContext()));
        }
    }

    public void updateClickMarco(MotionEvent down, MotionEvent up, boolean isRaw) {
        if (down == null) {
            down = up;
        }
        if (isRaw) {
            addMarcoKey(SigMacroCommon._DOWNX_, String.valueOf(Dips.pixelsToIntDips(down.getRawX(), SDKContext.getApplicationContext())));
            addMarcoKey(SigMacroCommon._DOWNY_, String.valueOf(Dips.pixelsToIntDips(down.getRawY(), SDKContext.getApplicationContext())));
            addMarcoKey(SigMacroCommon._UPX_, String.valueOf(Dips.pixelsToIntDips(up.getRawX(), SDKContext.getApplicationContext())));
            addMarcoKey(SigMacroCommon._UPY_, String.valueOf(Dips.pixelsToIntDips(up.getRawY(), SDKContext.getApplicationContext())));

        } else {
            addMarcoKey(SigMacroCommon._DOWNX_, String.valueOf(Dips.pixelsToIntDips(down.getX(), SDKContext.getApplicationContext())));
            addMarcoKey(SigMacroCommon._DOWNY_, String.valueOf(Dips.pixelsToIntDips(down.getY(), SDKContext.getApplicationContext())));
            addMarcoKey(SigMacroCommon._UPX_, String.valueOf(Dips.pixelsToIntDips(up.getX(), SDKContext.getApplicationContext())));
            addMarcoKey(SigMacroCommon._UPY_, String.valueOf(Dips.pixelsToIntDips(up.getY(), SDKContext.getApplicationContext())));
        }
    }

    public void updateClickMarco(String dx, String dy, String ux, String uy) {

        addMarcoKey(SigMacroCommon._DOWNX_, dx);
        addMarcoKey(SigMacroCommon._DOWNY_, dy);
        addMarcoKey(SigMacroCommon._UPX_, ux);
        addMarcoKey(SigMacroCommon._UPY_, uy);
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
