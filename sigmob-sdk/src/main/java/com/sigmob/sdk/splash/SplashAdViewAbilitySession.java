package com.sigmob.sdk.splash;

import static com.sigmob.sdk.base.common.ADEvent.AD_CLICK;
import static com.sigmob.sdk.base.common.ADEvent.AD_FOUR_ELEMENTS_CLOSE;
import static com.sigmob.sdk.base.common.ADEvent.AD_FOUR_ELEMENTS_SHOW;
import static com.sigmob.sdk.base.common.ADEvent.AD_SKIP;
import static com.sigmob.sdk.base.common.ADEvent.AD_START;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.track.BaseMacroCommon;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.ExternalViewabilitySession;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;

import java.util.HashMap;

public class SplashAdViewAbilitySession implements ExternalViewabilitySession {


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
                case AD_SKIP: {
                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            AD_SKIP);

                    eventRecord(adUnit, PointCategory.SKIP, null, null);
                }
                break;

                case AD_CLICK: {
                    String is_final_click = adUnit.getClickCommon().is_final_click ? Constants.SUCCESS : Constants.FAIL;
                    BaseMacroCommon baseMacroCommon = adUnit.getMacroCommon();
                    if (baseMacroCommon instanceof SigMacroCommon) {
                        
                        baseMacroCommon.addMarcoKey("_SLD_", adUnit.getClickCommon().sld);

                        baseMacroCommon.addMarcoKey("_AX_", adUnit.getClickCommon().adarea_x);
                        baseMacroCommon.addMarcoKey("_AY_", adUnit.getClickCommon().adarea_y);
                        baseMacroCommon.addMarcoKey("_AW_", adUnit.getClickCommon().adarea_w);
                        baseMacroCommon.addMarcoKey("_AH_", adUnit.getClickCommon().adarea_h);

                        if ( "5".equals(adUnit.getClickCommon().sld)) {
                            ((SigMacroCommon) baseMacroCommon).updateClickMarco("-999", "-999", "-999", "-999");

                            baseMacroCommon.addMarcoKey("_TURNX_", adUnit.getClickCommon().turn_x);
                            baseMacroCommon.addMarcoKey("_TURNY_", adUnit.getClickCommon().turn_y);
                            baseMacroCommon.addMarcoKey("_TURNZ_", adUnit.getClickCommon().turn_z);
                            baseMacroCommon.addMarcoKey("_TURNTIME_", adUnit.getClickCommon().turn_time);
                        }else  if ( "2".equals(adUnit.getClickCommon().sld)) {
                            ((SigMacroCommon) baseMacroCommon).updateClickMarco("-999", "-999", "-999", "-999");

                            baseMacroCommon.addMarcoKey("_XMAXACC_", adUnit.getClickCommon().x_max_acc);
                            baseMacroCommon.addMarcoKey("_YMAXACC_", adUnit.getClickCommon().y_max_acc);
                            baseMacroCommon.addMarcoKey("_ZMAXACC_", adUnit.getClickCommon().z_max_acc);
                        } else {
                            ((SigMacroCommon) baseMacroCommon).updateClickMarco(adUnit.getClickCommon().down, adUnit.getClickCommon().up);
                        }

                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._TEMPLATE_, String.valueOf(adUnit.getTemplateId()));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKAREA_, String.valueOf(adUnit.getClickCommon().click_area));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._CLICKSCENE_, String.valueOf(adUnit.getClickCommon().click_scene));
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._FINALCLICK_, is_final_click);

                    }

                    SigmobTrackingRequest.sendTrackings(
                            adUnit,
                            ADEvent.AD_CLICK);


                    HashMap<String, String> options = new HashMap<>();

                    options.put("template_id", adUnit.getClickCommon().template_id);
                    options.put("sld", adUnit.getClickCommon().sld);
                    options.put("adarea_x", adUnit.getClickCommon().adarea_x);
                    options.put("adarea_y", adUnit.getClickCommon().adarea_y);
                    options.put("adarea_w", adUnit.getClickCommon().adarea_w);
                    options.put("adarea_h", adUnit.getClickCommon().adarea_h);
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
            SigmobLog.e(e.getMessage());
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
