package com.sigmob.sdk.base.common;

import static com.sigmob.sdk.base.common.ADEvent.AD_AD_VIDEO_START;
import static com.sigmob.sdk.base.common.ADEvent.AD_CLICK;
import static com.sigmob.sdk.base.common.ADEvent.AD_CLICK_SKIP;
import static com.sigmob.sdk.base.common.ADEvent.AD_CLOSE_CARD_CLOSE;
import static com.sigmob.sdk.base.common.ADEvent.AD_CLOSE_CARD_SHOW;
import static com.sigmob.sdk.base.common.ADEvent.AD_COMPANION_CLICK;
import static com.sigmob.sdk.base.common.ADEvent.AD_COMPLETE;
import static com.sigmob.sdk.base.common.ADEvent.AD_FINISH;
import static com.sigmob.sdk.base.common.ADEvent.AD_FOUR_ELEMENTS_CLOSE;
import static com.sigmob.sdk.base.common.ADEvent.AD_FOUR_ELEMENTS_SHOW;
import static com.sigmob.sdk.base.common.ADEvent.AD_MOTION_CLICK;
import static com.sigmob.sdk.base.common.ADEvent.AD_MUTE;
import static com.sigmob.sdk.base.common.ADEvent.AD_PLAY_COMPLETE;
import static com.sigmob.sdk.base.common.ADEvent.AD_PLAY_QUARTER;
import static com.sigmob.sdk.base.common.ADEvent.AD_PLAY_THREE_QUARTERS;
import static com.sigmob.sdk.base.common.ADEvent.AD_PLAY_TWO_QUARTERS;
import static com.sigmob.sdk.base.common.ADEvent.AD_REWARD;
import static com.sigmob.sdk.base.common.ADEvent.AD_ROTATION;
import static com.sigmob.sdk.base.common.ADEvent.AD_SHOW;
import static com.sigmob.sdk.base.common.ADEvent.AD_SHOW_SKIP;
import static com.sigmob.sdk.base.common.ADEvent.AD_SKIP;
import static com.sigmob.sdk.base.common.ADEvent.AD_START;
import static com.sigmob.sdk.base.common.ADEvent.AD_UNMUTE;
import static com.sigmob.sdk.base.common.ADEvent.AD_VCLOSE;
import static com.sigmob.sdk.base.common.ADEvent.AD_VIDEO_CLICK;

import android.text.TextUtils;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.track.BaseMacroCommon;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;

import java.util.HashMap;

class RewardVideoViewAbilitySession implements ExternalViewabilitySession {


    private int mDuration = 0;
    private boolean mIsForceShow;
    private int mShow_Skip_Time;
    private int mEndTime = 0;


    @Override
    public boolean createDisplaySession(BaseAdUnit adUnit) {

        return true;
    }


    private boolean isTruncation() {
        return mEndTime > 0 && mEndTime * 1000 < mDuration;
    }

    private String convertSecondString(int millis) {

        if (millis == 0) return "0";
        return String.valueOf(millis / 1000);
    }

    private String convertSecondFloatString(int millis) {

        if (millis == 0) return "0";
        return String.format("%.2f", millis / 1000.0f);
    }

    @Override
    public boolean recordDisplayEvent(BaseAdUnit adUnit, String event, int playheadMillis) {
        try {
            switch (event) {

                case AD_START: {
                    eventRecord(adUnit, PointCategory.START, playheadMillis, null);
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._SETCLOSETIME_, String.valueOf(mEndTime));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, "0");
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VIDEOTIME_, convertSecondString(mDuration));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._COMPLETED_, "0");
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYFIRSTFRAME_, "0");
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._IS_TRUNCATION_, isTruncation() ? "1" : "0");

                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_START);

                }
                break;
                case AD_SKIP: {
                    eventRecordSkip(adUnit, PointCategory.SKIP, playheadMillis);
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        if (mDuration > 0 && playheadMillis > 0) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf((playheadMillis * 100 / getEndTime())));
                        } else {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "0");

                        }
                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_SKIP);

                }
                break;
                case AD_CLOSE_CARD_SHOW: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    eventRecord(adUnit, PointCategory.CLOSECARD, playheadMillis, PointCategory.SHOW);
                }
                break;
                case AD_CLOSE_CARD_CLOSE: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    eventRecord(adUnit, PointCategory.CLOSECARD, playheadMillis, PointCategory.CLOSE);
                }
                break;
                case AD_FOUR_ELEMENTS_SHOW: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    eventRecord(adUnit, PointCategory.FOURELEMENTS, playheadMillis, PointCategory.SHOW);
                }
                break;
                case AD_FOUR_ELEMENTS_CLOSE: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    eventRecord(adUnit, PointCategory.FOURELEMENTS, playheadMillis, PointCategory.CLOSE);
                }
                break;
                case AD_SHOW: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }

                    eventRecord(adUnit, PointCategory.ENDCARD, playheadMillis, AD_SHOW);
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        if (mDuration > 0 && playheadMillis > 0) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf((playheadMillis * 100 / getEndTime())));
                        } else {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "0");
                        }
                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit, AD_SHOW);
                }
                break;
                case AD_CLICK: {

                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    String is_final_click = adUnit.getClickCommon().is_final_click ? Constants.SUCCESS : Constants.FAIL;

                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        baseMacroCommon.addMarcoKey("_SLD_", adUnit.getClickCommon().sld);

                        baseMacroCommon.addMarcoKey("_AX_", "0");
                        baseMacroCommon.addMarcoKey("_AY_", "0");
                        baseMacroCommon.addMarcoKey("_AW_", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                        baseMacroCommon.addMarcoKey("_AH_", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));


                        if (adUnit.getTemplateId()>0){
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TEMPLATE_, String.valueOf(adUnit.getTemplateId()));
                        }

                        if (!TextUtils.isEmpty(adUnit.getClickCommon().click_area)) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKAREA_, String.valueOf(adUnit.getClickCommon().click_area));
                        }
                        if (!TextUtils.isEmpty(adUnit.getClickCommon().click_scene)) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKSCENE_, String.valueOf(adUnit.getClickCommon().click_scene));
                        }

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._FINALCLICK_, is_final_click);


                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        if (mDuration > 0 && playheadMillis > 0) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf((playheadMillis * 100 / getEndTime())));
                        } else {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "0");

                        }
                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_CLICK);
                }
                break;
                case AD_MOTION_CLICK: {

                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    String is_final_click = adUnit.getClickCommon().is_final_click ? Constants.SUCCESS : Constants.FAIL;
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();

                    if (baseMacroCommon instanceof SigMacroCommon) {
                        baseMacroCommon.addMarcoKey("_SLD_", adUnit.getClickCommon().sld);

                        baseMacroCommon.addMarcoKey("_AX_", "0");
                        baseMacroCommon.addMarcoKey("_AY_", "0");
                        baseMacroCommon.addMarcoKey("_AW_", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                        baseMacroCommon.addMarcoKey("_AH_", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));

                        if ("5".equals(adUnit.getClickCommon().sld)) {
                            ((SigMacroCommon) baseMacroCommon).updateClickMarco("-999", "-999", "-999", "-999");

                            baseMacroCommon.addMarcoKey("_TURNX_", adUnit.getClickCommon().turn_x);
                            baseMacroCommon.addMarcoKey("_TURNY_", adUnit.getClickCommon().turn_y);
                            baseMacroCommon.addMarcoKey("_TURNZ_", adUnit.getClickCommon().turn_z);
                            baseMacroCommon.addMarcoKey("_TURNTIME_", adUnit.getClickCommon().turn_time);
                        } else if ("2".equals(adUnit.getClickCommon().sld)) {
                            ((SigMacroCommon) baseMacroCommon).updateClickMarco("-999", "-999", "-999", "-999");

                            baseMacroCommon.addMarcoKey("_XMAXACC_", adUnit.getClickCommon().x_max_acc);
                            baseMacroCommon.addMarcoKey("_YMAXACC_", adUnit.getClickCommon().y_max_acc);
                            baseMacroCommon.addMarcoKey("_ZMAXACC_", adUnit.getClickCommon().z_max_acc);
                        } else {
                            ((SigMacroCommon) baseMacroCommon).updateClickMarco(adUnit.getClickCommon().down, adUnit.getClickCommon().up);
                        }

                        if (adUnit.getTemplateId()>0){
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TEMPLATE_, String.valueOf(adUnit.getTemplateId()));
                        }

                        if (!TextUtils.isEmpty(adUnit.getClickCommon().click_area)) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKAREA_, String.valueOf(adUnit.getClickCommon().click_area));
                        }
                        if (!TextUtils.isEmpty(adUnit.getClickCommon().click_scene)) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKSCENE_, String.valueOf(adUnit.getClickCommon().click_scene));
                        }
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._FINALCLICK_, is_final_click);

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        if (mDuration > 0 && playheadMillis > 0) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf((playheadMillis * 100 / getEndTime())));
                        } else {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "0");

                        }
                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_CLICK);

                    HashMap<String, String> options = new HashMap<>();

                    options.put("template_id", adUnit.getClickCommon().template_id);
                    options.put("sld", adUnit.getClickCommon().sld);
                    options.put("adarea_x", "0");
                    options.put("adarea_y", "0");
                    options.put("adarea_w", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                    options.put("adarea_h", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));
                    if ("5".equals(adUnit.getClickCommon().sld)) {
                        options.put("turn_x", adUnit.getClickCommon().turn_x);
                        options.put("turn_y", adUnit.getClickCommon().turn_y);
                        options.put("turn_z", adUnit.getClickCommon().turn_z);
                        options.put("turn_time", adUnit.getClickCommon().turn_time);
                    } else if ("2".equals(adUnit.getClickCommon().sld)) {
                        options.put("x_max_acc", adUnit.getClickCommon().x_max_acc);
                        options.put("y_max_acc", adUnit.getClickCommon().y_max_acc);
                        options.put("z_max_acc", adUnit.getClickCommon().z_max_acc);
                    }

                    options.put("click_area", adUnit.getClickCommon().click_area);
                    options.put("click_scene", adUnit.getClickCommon().click_scene);
                    options.put("cwidth", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                    options.put("cheight", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));

                    options.put("is_final_click", is_final_click);
                    options.put("coordinate", ((SigMacroCommon) baseMacroCommon).getCoordinate());

                    PointEntitySigmobUtils.SigmobTracking(adUnit.getClickCommon().click_scene, PointCategory.CLICK, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {
                            if (pointEntityBase instanceof PointEntitySigmob) {
                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;

                                entitySigmob.setOptions(options);
                            }
                        }
                    });

                }
                break;

                case AD_COMPANION_CLICK: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    String is_final_click = adUnit.getClickCommon().is_final_click ? Constants.SUCCESS : Constants.FAIL;

                    if (baseMacroCommon instanceof SigMacroCommon) {
                        baseMacroCommon.addMarcoKey("_SLD_", adUnit.getClickCommon().sld);

                        baseMacroCommon.addMarcoKey("_AX_", "0");
                        baseMacroCommon.addMarcoKey("_AY_", "0");
                        baseMacroCommon.addMarcoKey("_AW_", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                        baseMacroCommon.addMarcoKey("_AH_", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));

                        if (adUnit.getTemplateId()>0){
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TEMPLATE_, String.valueOf(adUnit.getTemplateId()));
                        }

                        if (!TextUtils.isEmpty(adUnit.getClickCommon().click_area)) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKAREA_, String.valueOf(adUnit.getClickCommon().click_area));
                        }
                        if (!TextUtils.isEmpty(adUnit.getClickCommon().click_scene)) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKSCENE_, String.valueOf(adUnit.getClickCommon().click_scene));
                        }
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._FINALCLICK_, is_final_click);


                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        if (mDuration > 0 && playheadMillis > 0) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf((playheadMillis * 100 / getEndTime())));
                        } else {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "0");

                        }
                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            AD_COMPANION_CLICK);
                }
                break;
                case AD_VIDEO_CLICK: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    String is_final_click = adUnit.getClickCommon().is_final_click ? Constants.SUCCESS : Constants.FAIL;

                    if (baseMacroCommon instanceof SigMacroCommon) {
                        baseMacroCommon.addMarcoKey("_SLD_", adUnit.getClickCommon().sld);

                        baseMacroCommon.addMarcoKey("_AX_", "0");
                        baseMacroCommon.addMarcoKey("_AY_", "0");
                        baseMacroCommon.addMarcoKey("_AW_", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                        baseMacroCommon.addMarcoKey("_AH_", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));

                        if (adUnit.getTemplateId()>0){
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TEMPLATE_, String.valueOf(adUnit.getTemplateId()));
                        }

                        if (!TextUtils.isEmpty(adUnit.getClickCommon().click_area)) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKAREA_, String.valueOf(adUnit.getClickCommon().click_area));
                        }
                        if (!TextUtils.isEmpty(adUnit.getClickCommon().click_scene)) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKSCENE_, String.valueOf(adUnit.getClickCommon().click_scene));
                        }
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._FINALCLICK_, is_final_click);


                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        if (mDuration > 0 && playheadMillis > 0) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf((playheadMillis * 100 / getEndTime())));
                        } else {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "0");
                        }
                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            AD_VIDEO_CLICK);
                }
                break;
                case AD_SHOW_SKIP: {

                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._SHOWSKIPTIME_, convertSecondString(playheadMillis));
                        if (mDuration > 0 && playheadMillis > 0) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf((playheadMillis * 100 / getEndTime())));
                        } else {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "0");

                        }
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VIDEOTIME_, convertSecondString(mDuration));

                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_SHOW_SKIP);
                }
                break;
                case AD_COMPLETE: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }

                    eventRecordSkip(adUnit, PointCategory.COMPLETE, playheadMillis);
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "100");
                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_COMPLETE);
                }
                break;
                case AD_REWARD: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    eventRecordSkip(adUnit, PointCategory.REWARD, playheadMillis);
                }
                break;
                case AD_FINISH: {
                    if (playheadMillis == 0) {
                        playheadMillis = getEndTime();
                    }
                    eventRecordSkip(adUnit, PointCategory.FINISH, playheadMillis);
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        if (mDuration > 0 && playheadMillis > 0) {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf((playheadMillis * 100 / getEndTime())));
                        } else {
                            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "0");

                        }
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._COMPLETED_, "1");
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYLASTFRAME_, "1");
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            AD_FINISH);
                }
                break;
                case AD_VCLOSE: {
                    eventRecord(adUnit, PointCategory.VCLOSE, playheadMillis, null);
                }
                break;
                case AD_MUTE: {
                    eventRecord(adUnit, PointCategory.SILENT, playheadMillis, Constants.SUCCESS);
                }
                break;
                case AD_UNMUTE: {
                    eventRecord(adUnit, PointCategory.SILENT, playheadMillis, Constants.FAIL);
                }
                break;
                case AD_ROTATION: {
                    if (ClientMetadata.getInstance() != null) {
                        eventRecord(adUnit, PointCategory.SCREENSWITCH, playheadMillis, ClientMetadata.getInstance().getRotation());
                    }
                }
                break;
                case AD_AD_VIDEO_START: {
                    eventRecord(adUnit, PointCategory.PLAY, playheadMillis, "0");

                }
                break;
                case AD_PLAY_QUARTER: {
                    eventRecord(adUnit, PointCategory.PLAY, playheadMillis, "0.25");
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "25");
                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_PLAY_QUARTER);
                }
                break;
                case AD_PLAY_TWO_QUARTERS: {
                    eventRecord(adUnit, PointCategory.PLAY, playheadMillis, "0.50");
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "50");
                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_PLAY_TWO_QUARTERS);
                }
                break;
                case AD_PLAY_THREE_QUARTERS: {
                    eventRecord(adUnit, PointCategory.PLAY, playheadMillis, "0.75");
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "75");
                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_PLAY_THREE_QUARTERS);
                }
                break;
                case AD_PLAY_COMPLETE: {
                    eventRecord(adUnit, PointCategory.PLAY, playheadMillis, "0.85");
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, convertSecondString(playheadMillis));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, "85");
                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            AD_PLAY_COMPLETE);
                }
                break;
                case AD_CLICK_SKIP: {
                    eventRecordSkip(adUnit, event, playheadMillis);
                }
                break;
                default: {
                    eventRecordSkip(adUnit, event, playheadMillis);

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            event);
                }
                break;
            }
        } catch (Throwable throwable) {
            SigmobLog.e("recordDisplayEvent ", throwable);
        }

        return true;
    }

    private int getEndTime() {
        if (mEndTime > 0 && mEndTime * 1000 < mDuration) {
            return mEndTime * 1000;
        }
        return mDuration;
    }

    @Override
    public boolean endDisplaySession(BaseAdUnit adUnit) {
        SigmobTrackingRequest.sendTrackings(
                adUnit, ADEvent.AD_CLOSE);
        eventRecord(adUnit, PointCategory.AD_CLOSE, getEndTime(), null);

        return true;
    }


    @Override
    public boolean ondDisplayShowSkip(BaseAdUnit adUnit, boolean isForceShow, int playheadMillis) {

        mIsForceShow = isForceShow;
        mShow_Skip_Time = playheadMillis;

        return true;
    }


    @Override
    public boolean onVideoPrepared(BaseAdUnit adUnit, int duration, int end_time) {

        mDuration = duration;
        mEndTime = adUnit.getEndTime();
        BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
        if (baseMacroCommon instanceof SigMacroCommon) {
            ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VIDEOTIME_, convertSecondString(mDuration));
        }
        return true;
    }

    private void eventRecordSkip(BaseAdUnit adUnit, String event, int playheadMillis) {

        eventRecord(adUnit, event, playheadMillis, null);
    }


    private void eventRecord(final BaseAdUnit adUnit, final String event, final int playheadMillis, String sub) {

        PointEntitySigmobUtils.SigmobTracking(event, sub, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {
                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;

                    entitySigmob.setVtime(convertSecondFloatString(mDuration));
                    entitySigmob.setSkip_show_time(convertSecondFloatString(mShow_Skip_Time));

                    entitySigmob.setCurrent_time(convertSecondFloatString(playheadMillis));
                    entitySigmob.setPlay_process(String.valueOf(playheadMillis * 1.0 / getEndTime()));
                    if (event.equals(PointCategory.START)) {
                        entitySigmob.setScene_id(adUnit.getAd_scene_id());
                        entitySigmob.setScene_desc(adUnit.getAd_scene_desc());
                        entitySigmob.setBid_token(adUnit.getBid_token());
                    }


                    entitySigmob.setPlay_time(convertSecondString(playheadMillis));
                    entitySigmob.setSet_close_time(String.format("%d", mEndTime));
                    entitySigmob.setIs_truncation(isTruncation() ? Constants.SUCCESS : Constants.FAIL);
                    entitySigmob.setIs_force(mIsForceShow ? Constants.SUCCESS : Constants.FAIL);
                }
            }
        });


    }

}
