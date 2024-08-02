package com.sigmob.sdk.newInterstitial;

import static com.sigmob.sdk.base.common.ADEvent.AD_CHARGE;
import static com.sigmob.sdk.base.common.ADEvent.AD_CLICK;
import static com.sigmob.sdk.base.common.ADEvent.AD_COMPLETE;
import static com.sigmob.sdk.base.common.ADEvent.AD_FOUR_ELEMENTS_CLOSE;
import static com.sigmob.sdk.base.common.ADEvent.AD_FOUR_ELEMENTS_SHOW;
import static com.sigmob.sdk.base.common.ADEvent.AD_HIDE;
import static com.sigmob.sdk.base.common.ADEvent.AD_NATIVE_VIDEO_RESTART;
import static com.sigmob.sdk.base.common.ADEvent.AD_PLAY_QUARTER;
import static com.sigmob.sdk.base.common.ADEvent.AD_PLAY_THREE_QUARTERS;
import static com.sigmob.sdk.base.common.ADEvent.AD_PLAY_TWO_QUARTERS;
import static com.sigmob.sdk.base.common.ADEvent.AD_SHOW_CLOSE;
import static com.sigmob.sdk.base.common.ADEvent.AD_SHOW_SKIP;
import static com.sigmob.sdk.base.common.ADEvent.AD_SKIP;
import static com.sigmob.sdk.base.common.ADEvent.AD_START;
import static com.sigmob.sdk.base.common.ADEvent.AD_VCLOSE;
import static com.sigmob.sdk.base.common.ADEvent.AD_VIDEO_LINK;
import static com.sigmob.sdk.base.common.ADEvent.AD_VIDEO_PAUSE;
import static com.sigmob.sdk.base.common.ADEvent.AD_VIDEO_START;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.track.BaseMacroCommon;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.ExternalViewabilitySession;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;

import java.util.HashMap;

public class InterstitialAdViewAbilitySession implements ExternalViewabilitySession {


    @Override
    public boolean createDisplaySession(BaseAdUnit adUnit) {
        return true;
    }

    @Override
    public boolean recordDisplayEvent(BaseAdUnit adUnit, String event, int currentPosition) {
        try {

            switch (event) {

                case AD_START: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_START);

                    eventRecord(adUnit, PointCategory.START, null, null);
                }
                break;

                case AD_VCLOSE: {
                    eventRecord(adUnit, PointCategory.VCLOSE, null, null);
                }
                break;

                case AD_HIDE: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_HIDE);

                    eventRecord(adUnit, PointCategory.AD_HIDE, null, null);

                }
                break;
                case AD_CHARGE: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            AD_CHARGE);

                    eventRecord(adUnit, PointCategory.CHARGE, null, null);

                }
                break;

                case AD_SHOW_SKIP: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            AD_SHOW_SKIP);

                    eventRecord(adUnit, PointCategory.SHOW_SKIP, null, null);

                }
                break;
                case AD_SKIP: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            AD_SKIP);

                    eventRecord(adUnit, PointCategory.SKIP, null, null);

                }
                break;

                case AD_SHOW_CLOSE: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            AD_SHOW_CLOSE);

                    eventRecord(adUnit, PointCategory.SHOW_CLOSE, null, null);
                }
                break;
                case AD_VIDEO_LINK: {
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VIDEOTIME_, String.valueOf(adUnit.getVideoCommon().video_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEGINTIME_, String.valueOf(adUnit.getVideoCommon().begin_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, String.valueOf(adUnit.getVideoCommon().end_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYFIRSTFRAME_, String.valueOf(adUnit.getVideoCommon().is_first));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYLASTFRAME_, String.valueOf(adUnit.getVideoCommon().is_last));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._SCENE_, String.valueOf(adUnit.getVideoCommon().scene));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TYPE_, String.valueOf(adUnit.getVideoCommon().type));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEHAVIOR_, String.valueOf(adUnit.getVideoCommon().is_auto_play));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._STATUS_, String.valueOf(adUnit.getVideoCommon().status));

                    }
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_VIDEO_LINK, true);

                    HashMap<String, String> options = new HashMap<>();
                    options.put("video_time", String.valueOf(adUnit.getVideoCommon().video_time));
                    options.put("begin_time", String.valueOf(adUnit.getVideoCommon().begin_time));
                    options.put("end_time", String.valueOf(adUnit.getVideoCommon().end_time));
                    options.put("is_first", String.valueOf(adUnit.getVideoCommon().is_first));
                    options.put("scene", String.valueOf(adUnit.getVideoCommon().scene));
                    options.put("type", String.valueOf(adUnit.getVideoCommon().type));
                    options.put("is_auto_play", String.valueOf(adUnit.getVideoCommon().is_auto_play));
                    options.put("status", String.valueOf(adUnit.getVideoCommon().status));

                    eventRecord(adUnit, PointCategory.VIDEO_LINK, null, options);
                }
                break;
                case AD_NATIVE_VIDEO_RESTART: {
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VIDEOTIME_, String.valueOf(adUnit.getVideoCommon().video_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEGINTIME_, String.valueOf(adUnit.getVideoCommon().begin_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, String.valueOf(adUnit.getVideoCommon().end_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYFIRSTFRAME_, String.valueOf(adUnit.getVideoCommon().is_first));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYLASTFRAME_, String.valueOf(adUnit.getVideoCommon().is_last));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._SCENE_, String.valueOf(adUnit.getVideoCommon().scene));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TYPE_, String.valueOf(adUnit.getVideoCommon().type));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEHAVIOR_, String.valueOf(adUnit.getVideoCommon().is_auto_play));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._STATUS_, String.valueOf(adUnit.getVideoCommon().status));
                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_NATIVE_VIDEO_RESTART);

                    HashMap<String, String> options = new HashMap<>();
                    options.put("video_time", String.valueOf(adUnit.getVideoCommon().video_time));
                    options.put("begin_time", String.valueOf(adUnit.getVideoCommon().begin_time));
                    options.put("end_time", String.valueOf(adUnit.getVideoCommon().end_time));
                    options.put("is_first", String.valueOf(adUnit.getVideoCommon().is_first));
                    options.put("scene", String.valueOf(adUnit.getVideoCommon().scene));
                    options.put("type", String.valueOf(adUnit.getVideoCommon().type));
                    options.put("is_auto_play", String.valueOf(adUnit.getVideoCommon().is_auto_play));
                    options.put("status", String.valueOf(adUnit.getVideoCommon().status));

                    eventRecord(adUnit, PointCategory.VIDEO_RESTART, null, options);
                }
                break;
                case AD_VIDEO_START: {
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VIDEOTIME_, String.valueOf(adUnit.getVideoCommon().video_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEGINTIME_, String.valueOf(adUnit.getVideoCommon().begin_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, String.valueOf(adUnit.getVideoCommon().end_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYFIRSTFRAME_, String.valueOf(adUnit.getVideoCommon().is_first));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYLASTFRAME_, String.valueOf(adUnit.getVideoCommon().is_last));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._SCENE_, String.valueOf(adUnit.getVideoCommon().scene));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TYPE_, String.valueOf(adUnit.getVideoCommon().type));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEHAVIOR_, String.valueOf(adUnit.getVideoCommon().is_auto_play));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._STATUS_, String.valueOf(adUnit.getVideoCommon().status));
                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_VIDEO_START, false);

                    HashMap<String, String> options = new HashMap<>();
                    options.put("video_time", String.valueOf(adUnit.getVideoCommon().video_time));
                    options.put("begin_time", String.valueOf(adUnit.getVideoCommon().begin_time));
                    options.put("end_time", String.valueOf(adUnit.getVideoCommon().end_time));
                    options.put("is_first", String.valueOf(adUnit.getVideoCommon().is_first));
                    options.put("scene", String.valueOf(adUnit.getVideoCommon().scene));
                    options.put("type", String.valueOf(adUnit.getVideoCommon().type));
                    options.put("is_auto_play", String.valueOf(adUnit.getVideoCommon().is_auto_play));
                    options.put("status", String.valueOf(adUnit.getVideoCommon().status));

                    eventRecord(adUnit, PointCategory.VIDEO_START, null, options);

                }
                break;
                case AD_VIDEO_PAUSE: {
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VIDEOTIME_, String.valueOf(adUnit.getVideoCommon().video_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEGINTIME_, String.valueOf(adUnit.getVideoCommon().begin_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, String.valueOf(adUnit.getVideoCommon().end_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYFIRSTFRAME_, String.valueOf(adUnit.getVideoCommon().is_first));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYLASTFRAME_, String.valueOf(adUnit.getVideoCommon().is_last));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._SCENE_, String.valueOf(adUnit.getVideoCommon().scene));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TYPE_, String.valueOf(adUnit.getVideoCommon().type));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEHAVIOR_, String.valueOf(adUnit.getVideoCommon().is_auto_play));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._STATUS_, String.valueOf(adUnit.getVideoCommon().status));

                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_VIDEO_PAUSE, true);

                    HashMap<String, String> options = new HashMap<>();
                    options.put("video_time", String.valueOf(adUnit.getVideoCommon().video_time));
                    options.put("begin_time", String.valueOf(adUnit.getVideoCommon().begin_time));
                    options.put("end_time", String.valueOf(adUnit.getVideoCommon().end_time));
                    options.put("is_first", String.valueOf(adUnit.getVideoCommon().is_first));
                    options.put("scene", String.valueOf(adUnit.getVideoCommon().scene));
                    options.put("type", String.valueOf(adUnit.getVideoCommon().type));
                    options.put("is_auto_play", String.valueOf(adUnit.getVideoCommon().is_auto_play));
                    options.put("status", String.valueOf(adUnit.getVideoCommon().status));

                    eventRecord(adUnit, PointCategory.VIDEO_PAUSE, null, options);
                }
                break;
                case AD_PLAY_QUARTER: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_PLAY_QUARTER);
                    eventRecord(adUnit, PointCategory.PLAY, "0.25", null);

                }
                break;
                case AD_PLAY_TWO_QUARTERS: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_PLAY_TWO_QUARTERS);
                    eventRecord(adUnit, PointCategory.PLAY, "0.50", null);


                }
                break;
                case AD_PLAY_THREE_QUARTERS: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_PLAY_THREE_QUARTERS);
                    eventRecord(adUnit, PointCategory.PLAY, "0.75", null);
                }
                break;
                case AD_COMPLETE: {


                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VIDEOTIME_, String.valueOf(adUnit.getVideoCommon().video_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEGINTIME_, String.valueOf(adUnit.getVideoCommon().begin_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._ENDTIME_, String.valueOf(adUnit.getVideoCommon().end_time));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYFIRSTFRAME_, String.valueOf(adUnit.getVideoCommon().is_first));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYLASTFRAME_, String.valueOf(adUnit.getVideoCommon().is_last));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._SCENE_, String.valueOf(adUnit.getVideoCommon().scene));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TYPE_, String.valueOf(adUnit.getVideoCommon().type));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._BEHAVIOR_, String.valueOf(adUnit.getVideoCommon().is_auto_play));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._STATUS_, String.valueOf(adUnit.getVideoCommon().status));

                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_COMPLETE, true);

                    HashMap<String, String> options = new HashMap<>();
                    options.put("video_time", String.valueOf(adUnit.getVideoCommon().video_time));
                    options.put("begin_time", String.valueOf(adUnit.getVideoCommon().begin_time));
                    options.put("end_time", String.valueOf(adUnit.getVideoCommon().end_time));
                    options.put("is_first", String.valueOf(adUnit.getVideoCommon().is_first));
                    options.put("scene", String.valueOf(adUnit.getVideoCommon().scene));
                    options.put("type", String.valueOf(adUnit.getVideoCommon().type));
                    options.put("is_auto_play", String.valueOf(adUnit.getVideoCommon().is_auto_play));
                    options.put("status", String.valueOf(adUnit.getVideoCommon().status));

                    eventRecord(adUnit, PointCategory.COMPLETE, null, options);
                }
                break;
                case AD_CLICK: {
                    String is_final_click = adUnit.getClickCommon().is_final_click ? Constants.SUCCESS : Constants.FAIL;
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TEMPLATE_, String.valueOf(adUnit.getTemplateId()));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKAREA_, String.valueOf(adUnit.getClickCommon().click_area));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKSCENE_, String.valueOf(adUnit.getClickCommon().click_scene));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._FINALCLICK_, is_final_click);
                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_CLICK);

                    HashMap<String, String> options = new HashMap<>();
                    options.put("click_area", adUnit.getClickCommon().click_area);
                    options.put("click_scene", adUnit.getClickCommon().click_scene);
                    options.put("template_id", String.valueOf(adUnit.getTemplateId()));
                    
                    options.put("cwidth", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                    options.put("cheight", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));

                    options.put("is_final_click", is_final_click);
                    options.put("coordinate", ((SigMacroCommon) baseMacroCommon).getCoordinate());

                    eventRecord(adUnit, adUnit.getClickCommon().click_scene, PointCategory.CLICK, options);
                }
                break;
                case AD_FOUR_ELEMENTS_SHOW: {

                    eventRecord(adUnit, PointCategory.FOURELEMENTS, PointCategory.SHOW, null);
                }
                break;
                case AD_FOUR_ELEMENTS_CLOSE: {

                    eventRecord(adUnit, PointCategory.FOURELEMENTS, PointCategory.CLOSE, null);
                }
                break;


            }
        } catch (Throwable e) {

        }
        return true;
    }

    @Override
    public boolean ondDisplayShowSkip(BaseAdUnit adUnit, boolean isForceShow, int playheadMillis) {
        return false;
    }

    @Override
    public boolean onVideoPrepared(BaseAdUnit adUnit, int duration, int endTime) {
        return false;
    }

    @Override
    public boolean endDisplaySession(BaseAdUnit adUnit) {
        SigmobTrackingRequest.sendTrackings(
                adUnit, ADEvent.AD_CLOSE);
        eventRecord(adUnit, PointCategory.AD_CLOSE, null, null);

        return true;
    }

    private void eventRecord(BaseAdUnit adUnit, final String event, String sub, final HashMap<String, String> options) {

        PointEntitySigmobUtils.SigmobTracking(event, sub, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {
                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;

                    entitySigmob.setOptions(options);
                }
            }
        });
    }
}
