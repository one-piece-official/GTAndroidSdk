package com.sigmob.sdk.base.common;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.location.Location;
import android.os.Build;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.common.mta.DeviceContext;
import com.czhj.sdk.common.track.AdTracker;
import com.czhj.sdk.common.utils.AppPackageUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.VolleyError;
import com.czhj.volley.toolbox.DownloadItem;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.WindSDKConfig;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntityClick;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.mta.PointEntitySigmobError;
import com.sigmob.sdk.base.mta.PointEntitySigmobRequest;
import com.sigmob.sdk.base.mta.PointEntitySigmobSuper;
import com.sigmob.sdk.base.mta.PointType;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAdRequest;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindCustomController;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PointEntitySigmobUtils {


//    public static void TrackInit(String category) {
//
//        PointEntitySigmobCommon entityInit = new PointEntitySigmobCommon();
//        entityInit.setAc_type(PointType.SIGMOB_INIT);
//
//        entityInit.setCategory(category);
//
//        entityInit.commit();
//    }

    public static void TrackApp(String category) {

        PointEntitySigmobSuper entityInit = new PointEntitySigmobSuper();
        entityInit.setAc_type(PointType.ANTI_SPAM_TOUCH);

        entityInit.setCategory(category);
        entityInit.setSha1(ClientMetadata.getInstance().getApkSha1());
        entityInit.setMd5(ClientMetadata.getInstance().getApkMd5());

        entityInit.commit();
    }

    public static void TrackNativeLoadReady(LoadReadyItem item){
        if (item == null || (item.media_request_count == 0 && item.media_ready_count == 0)){
            return;
        }
        PointEntitySigmobSuper entity = new PointEntitySigmobSuper();
        entity.setAc_type(PointType.LOAD_READY);
        entity.setCategory("load_ready");
        Map<String, String> options = entity.getOptions();
        options.put("placement_id", String.valueOf(item.placement_id));
        options.put("feed_pre_request_count", String.valueOf(item.media_request_count));
        options.put("feed_pre_ready_count", String.valueOf(item.media_ready_count));
        entity.commit();

    }

    public static void SigmobInit(String category, OnPointEntityExtraInfo onPointEntityExtraInfo) {

        PointEntitySigmobCommon entityInit = new PointEntitySigmobCommon();
        entityInit.setAc_type(PointType.SIGMOB_INIT);

        entityInit.setCategory(category);
        if (!WindSDKConfig.getInstance().isDisableUpAppInfo()) {
            entityInit.setAppinfo_switch("1,1");
        } else {
            entityInit.setAppinfo_switch("0,0");
        }

        int x, y;

        DeviceContext deviceContext = SDKContext.getDeviceContext();
        Location location = deviceContext != null ? deviceContext.getLocation() : ClientMetadata.getInstance().getLocation();

        if (location != null) {
            y = 1;
        } else {
            y = 0;
        }

        if (!WindSDKConfig.getInstance().isDisable_up_location()) {
            x = 1;
        } else {
            x = 0;
            y = 0;
        }

        entityInit.setLocation_switch(x + "," + y);
        if (onPointEntityExtraInfo != null) {
            onPointEntityExtraInfo.onAddExtra(entityInit);
        }

        WindAdOptions options = WindAds.sharedAds().getOptions();
        if (options != null && WindAds.sharedAds().getOptions().getCustomController() != null) {
            WindCustomController controller = options.getCustomController();
            entityInit.setIs_custom_imei(controller.isCanUsePhoneState() ? "0" : "1");
            entityInit.setIs_custom_android_id(controller.isCanUseAndroidId() ? "0" : "1");
            entityInit.setIs_custom_oaid(TextUtils.isEmpty(controller.getDevOaid()) ? "0" : "1");
        } else {
            entityInit.setIs_custom_imei("0");
            entityInit.setIs_custom_android_id("0");
            entityInit.setIs_custom_oaid("0");
        }

        entityInit.commit();
    }

    public static void SigmobMRIADJS(String ac_type, BaseAdUnit adUnit, LoadAdRequest adRequest, OnPointEntityExtraInfo onPointEntityExtraInfo) {

        PointEntitySigmob entityInit = new PointEntitySigmob();
        entityInit.setAc_type(ac_type);

        if (onPointEntityExtraInfo != null) {
            onPointEntityExtraInfo.onAddExtra(entityInit);
        }

        updateADunitInfo(entityInit.getCategory(), entityInit.getSub_category(), adUnit, entityInit);

        if (adRequest != null) {
            entityInit.setLoad_id(adRequest.getLoadId());
            entityInit.setAdtype(String.valueOf(adRequest.getAdType()));
            entityInit.setScene_id(adRequest.getAdSceneId());
            entityInit.setScene_desc(adRequest.getAdSceneDesc());
            entityInit.setPlacement_id(adRequest.getPlacementId());
        }

        entityInit.commit();
    }

    public static void SigmobTracking(String category, String sub_category, BaseAdUnit adUnit) {

        SigmobTracking(category, sub_category, adUnit, null);
    }

    public static void SigmobTracking(String category, String sub_category, LoadAdRequest adRequest) {

        SigmobTracking(category, sub_category, null, null, adRequest, null);
    }

    public static void canOpenListTracking(String pkg_name,boolean isCanOpen) {


        PointEntitySigmobSuper entity = new PointEntitySigmobSuper();

        entity.setAc_type(PointType.SIGMOB_CANOPEN_APP);
        entity.setCategory(PointCategory.OPEN_APP);
        Map<String, String> option = new HashMap<>();
        option.put("app_pkg_name", pkg_name);
        option.put("can_op", isCanOpen?Constants.SUCCESS:Constants.FAIL);

        entity.setOptions(option);
        entity.commit();

    }
    public static void appInfoListTracking(PackageInfo info,int type) {

        if (info == null) return;
        PointEntitySigmobSuper entity = new PointEntitySigmobSuper();

        entity.setAc_type(PointType.SIGMOB_APP);
        entity.setCategory(PointCategory.APP);
        Map<String, String> option = new HashMap<>();
        try {
            String appName = String.valueOf(AppPackageUtil.getPackageManager(SDKContext.getApplicationContext()).
                    getApplicationLabel(info.applicationInfo));
            option.put(WindConstants.APP_NAME, appName);
        } catch (Throwable e) {
        }
        option.put(WindConstants.PACKAGE_NAME, info.packageName);

        option.put(WindConstants.UPDATE, String.valueOf(info.lastUpdateTime));
        option.put(WindConstants.APP_VERSION, info.versionName);
        option.put("source_type",String.valueOf(type));

        entity.setOptions(option);
        entity.commit();

    }

    public static void SigmobTracking(String category, String sub_category, BaseAdUnit adUnit, OnPointEntityExtraInfo onPointEntityExtraInfo) {
        SigmobTracking(category, sub_category, adUnit, null, null, onPointEntityExtraInfo);
    }

    public static void SigmobTracking(String category, String sub_category, BaseAdUnit adUnit, WindAdRequest adRequest, OnPointEntityExtraInfo onPointEntityExtraInfo) {
        SigmobTracking(category, sub_category, adUnit, adRequest, null, onPointEntityExtraInfo);
    }

    public static void SigmobTracking(String category, String sub_category, BaseAdUnit adUnit, LoadAdRequest adRequest, OnPointEntityExtraInfo onPointEntityExtraInfo) {
        SigmobTracking(category, sub_category, adUnit, null, adRequest, onPointEntityExtraInfo);
    }

    public static void SigmobTracking(String category, String sub_category, BaseAdUnit adUnit, WindAdRequest request, LoadAdRequest adRequest, final OnPointEntityExtraInfo onPointEntityExtraInfo) {
        SigmobTracking.getSigmobTracking(category)
                .setSub_category(sub_category)
                .setAdUnit(adUnit)
                .setWindAdRequest(request)
                .setLoadAdRequest(adRequest)
                .setOnPointEntityExtraInfo(new OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        if (onPointEntityExtraInfo != null) {
                            onPointEntityExtraInfo.onAddExtra(pointEntityBase);
                        }
                    }
                }).send();
    }

    public static void SigmobRequestTracking(String category, String sub_category, WindAdRequest adRequest,LoadAdRequest loadAdRequest, OnPointEntityExtraInfo onPointEntityExtraInfo) {

        PointEntitySigmobRequest entitySigmob = new PointEntitySigmobRequest();

        entitySigmob.setAc_type(PointType.SIGMOB_TRACKING);
        entitySigmob.setCategory(category);

        entitySigmob.setSub_category(sub_category);

        updateAdRequestInfo(category, category, entitySigmob, adRequest);
        updateLoadRequestInfo(category, category, entitySigmob, loadAdRequest);

        if (onPointEntityExtraInfo != null) {
            onPointEntityExtraInfo.onAddExtra(entitySigmob);
        }

        updatePointEntitySigmob(entitySigmob);
        entitySigmob.commit();

    }


    public static void eventTargetURL(BaseAdUnit adUnit, String actionType, final String downloadUrl) {

        PointEntitySigmobUtils.SigmobTracking(PointCategory.TARGET_URL, null, adUnit, new OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {
                    ((PointEntitySigmob) pointEntityBase).setFinal_url(downloadUrl);
                }

            }
        });
    }

    public static void eventRecord(String category, final String sub_category, BaseAdUnit adUnit) {
        PointEntitySigmobUtils.SigmobTracking(category, sub_category, adUnit, null);
    }

    protected static void updateADunitInfo(String category, String sub_category, BaseAdUnit adUnit, PointEntitySigmob entitySigmob) {
        if (adUnit != null) {
            try {
                entitySigmob.setAdtype(String.valueOf(adUnit.getAd_type()));
                entitySigmob.setCampaign_id(adUnit.getCamp_id());
                entitySigmob.setCreative_id(adUnit.getCrid());
                entitySigmob.setRequest_id(adUnit.getRequestId());
                entitySigmob.setPlacement_id(adUnit.getAdslot_id());
                entitySigmob.setLoad_id(adUnit.getLoad_id());
                entitySigmob.setVid(adUnit.getVid());
                entitySigmob.setScene_id(adUnit.getAd_scene_id());
                entitySigmob.setScene_desc(adUnit.getAd_scene_desc());
                entitySigmob.setPlay_mode(String.valueOf(adUnit.getPlayMode()));
                entitySigmob.setCreative_type(String.valueOf(adUnit.getCreativeType()));
                entitySigmob.setBid_token(adUnit.getBid_token());
                BiddingResponse bidding_response = adUnit.bidding_response;
                if (bidding_response != null) {
                    entitySigmob.setHb_price(bidding_response.ecpm);
                }
                entitySigmob.setPrice(adUnit.getAd().settlement_price_enc);
                if (!TextUtils.isEmpty(adUnit.getAd().product_id)) {
                    entitySigmob.setProduct_id(adUnit.getAd().product_id);
                }
                if (adUnit.getAdx_id()!= null) {
                    entitySigmob.setAdx_id(adUnit.getAdx_id());
                }

                entitySigmob.setTemplate_id(String.valueOf(adUnit.getTemplateId()));

                if (adUnit.getMaterial() != null) {
                    entitySigmob.setTemplate_type(adUnit.getMaterial().template_type);
                }

                if (TextUtils.isEmpty(entitySigmob.getTarget_url())) {
                    entitySigmob.setTarget_url(adUnit.getLanding_page());
                }

                if (adUnit.getWXProgramRes() != null) {
                    if (!TextUtils.isEmpty(adUnit.getWXProgramRes().wx_app_path)) {
                        String url = URLEncoder.encode(adUnit.getWXProgramRes().wx_app_path, "UTF-8");
                        entitySigmob.setWx_app_path(url);
                    }
                    if (!TextUtils.isEmpty(adUnit.getWXProgramRes().wx_app_username)) {
                        entitySigmob.setWx_app_username(adUnit.getWXProgramRes().wx_app_username);
                    }
                }

                entitySigmob.getOptions().put("apk_md5", adUnit.getApkMd5());

                entitySigmob.setAd_source_channel(adUnit.getAd_source_channel());

                if (TextUtils.isEmpty(entitySigmob.getShow_type()) && !TextUtils.isEmpty(category)
                        && (category.equals(PointCategory.REQUEST)
                        || category.equals(PointCategory.READY)
                        || category.equals(PointCategory.START)
                        || category.equals(PointCategory.ENDCARD)
                        || (!TextUtils.isEmpty(sub_category) && sub_category.equals(PointCategory.CLICK)))) {
                    if (adUnit.getAd_type() == AdFormat.FULLSCREEN_VIDEO) {
                        if (adUnit.isHalfInterstitial()) {
                            entitySigmob.setShow_type("2");
                        } else {
                            entitySigmob.setShow_type("1");
                        }
                    }
                }
            } catch (Throwable t) {

            }
        }
    }

    public static void SigmobError(String category, WindAdError adError, BaseAdUnit adUnit) {

        SigmobError(category,
                null,
                adError.getErrorCode(),
                adError.getMessage(),
                null,
                null,
                adUnit,
                null);
    }


    public static void SigmobError(String category, WindAdError adError, BaseAdUnit adUnit, LoadAdRequest adRequest) {


        SigmobError(category,
                null,
                adError.getErrorCode(),
                adError.getMessage(),
                null,
                adRequest,
                adUnit,
                null);
    }

    public static void SigmobError(String category, WindAdError adError, WindAdRequest adRequest) {

        SigmobError(category,
                null,
                adError.getErrorCode(),
                adError.getMessage(),
                adRequest,
                null,
                null,
                null);
    }

    public static void SigmobError(String category, String sub_category, int error_code,
                                   String error_msg, LoadAdRequest adRequest) {

        SigmobError(category,
                sub_category,
                error_code,
                error_msg,
                null,
                adRequest,
                null,
                null);
    }

    public static void SigmobError(String category,
                                   String sub_category,
                                   int error_code,
                                   String error_msg,
                                   WindAdRequest adRequest,
                                   LoadAdRequest loadAdRequest,
                                   BaseAdUnit adUnit,
                                   OnPointEntityExtraInfo onPointEntityExtraInfo) {

        PointEntitySigmobError entitySigmobError = PointEntitySigmobError.SigmobError(category, error_code, error_msg);

        entitySigmobError.setSub_category(sub_category);

        updateAdRequestInfo(category, category, entitySigmobError, adRequest);

        updateLoadRequestInfo(category, category, entitySigmobError, loadAdRequest);
        updateADunitInfo(category, category, adUnit, entitySigmobError);

        if (onPointEntityExtraInfo != null) {
            onPointEntityExtraInfo.onAddExtra(entitySigmobError);
        }

        entitySigmobError.commit();

    }

    protected static void updateLoadRequestInfo(String category, String sub_category, PointEntitySigmob entitySigmob, LoadAdRequest adRequest) {

        if (entitySigmob != null && adRequest != null) {


            entitySigmob.setPlacement_id(adRequest.getPlacementId());
            if (!TextUtils.isEmpty(adRequest.getBidToken())) {
                entitySigmob.setBid_token(adRequest.getBidToken());
            }

            if (!TextUtils.isEmpty(adRequest.getLoadId())) {
                entitySigmob.setLoad_id(adRequest.getLoadId());
            }

            if (!TextUtils.isEmpty(adRequest.getAdx_id())) {
                entitySigmob.setAdx_id(adRequest.getAdx_id());
            }

            entitySigmob.setAdtype(String.valueOf(adRequest.getAdType()));

            if (!TextUtils.isEmpty(adRequest.getRequestId())) {
                entitySigmob.setRequest_id(adRequest.getRequestId());
            }

            if (adRequest.getOptions() != null) {
                JSONObject jsonObject = new JSONObject(adRequest.getOptions());
                entitySigmob.setExtinfo(jsonObject.toString());
            }

            if (TextUtils.isEmpty(entitySigmob.getShow_type()) && !TextUtils.isEmpty(category)
                    && (category.equals(PointCategory.REQUEST)
                    || category.equals(PointCategory.READY)
                    || category.equals(PointCategory.START)
                    || category.equals(PointCategory.ENDCARD))
                    || (!TextUtils.isEmpty(sub_category) && sub_category.equals(PointCategory.CLICK))) {
                if (adRequest.getAdType() == AdFormat.FULLSCREEN_VIDEO) {
                    if (adRequest.isHalfInterstitial()) {
                        entitySigmob.setShow_type("2");
                    } else {
                        entitySigmob.setShow_type("1");
                    }
                }
            }
        }
    }

    protected static void updateAdRequestInfo(String category, String sub_category, PointEntitySigmob entitySigmob, WindAdRequest adRequest) {

        if (entitySigmob != null && adRequest != null) {

            entitySigmob.setPlacement_id(adRequest.getPlacementId());
            entitySigmob.setLoad_id(adRequest.getLoadId());
            entitySigmob.setAdx_id(adRequest.getAdxId());
            entitySigmob.setAdtype(String.valueOf(adRequest.getAdType()));

            if (adRequest.hasOptions()) {
                try {
                    JSONObject jsonObject = new JSONObject(adRequest.getOptions());
                    entitySigmob.setExtinfo(jsonObject.toString());
                } catch (Throwable t) {

                }

            }

            if (TextUtils.isEmpty(entitySigmob.getShow_type()) && !TextUtils.isEmpty(category)
                    && (category.equals(PointCategory.REQUEST)
                    || category.equals(PointCategory.READY)
                    || category.equals(PointCategory.START)
                    || category.equals(PointCategory.ENDCARD))
                    || (!TextUtils.isEmpty(sub_category) && sub_category.equals(PointCategory.CLICK))) {
                if (adRequest.getAdType() == AdFormat.FULLSCREEN_VIDEO) {
                    if (adRequest.isHalfInterstitial()) {
                        entitySigmob.setShow_type("2");
                    } else {
                        entitySigmob.setShow_type("1");
                    }
                }
            }

        }
    }

    public static void SigmobError(String category, int code, String message, BaseAdUnit adUnit) {

        SigmobError(category, code, message, adUnit, null);
    }

    public static void SigmobError(String category, WindAdError adError, BaseAdUnit adUnit, OnPointEntityExtraInfo onPointEntityExtraInfo) {

        if (adError != null) {
            String message = adError.getMessage();
            PointEntitySigmobError entitySigmobError = PointEntitySigmobError.SigmobError(category, adError.getErrorCode(), adError.getMessage());

            updateADunitInfo(category, category, adUnit, entitySigmobError);

            if (message != null && message.equals(WindAdError.ERROR_SIGMOB_VIDEO_FILE.getMessage()) && category != null && category.equals(PointCategory.LOAD) && adUnit != null) {
                entitySigmobError.getOptions().put("video_url", adUnit.getVideo_url());
            }
            if (onPointEntityExtraInfo != null) {
                onPointEntityExtraInfo.onAddExtra(entitySigmobError);
            }
            entitySigmobError.commit();

        }


    }

    public static void SigmobError(String category, int code, String message, BaseAdUnit adUnit, OnPointEntityExtraInfo onPointEntityExtraInfo) {

        PointEntitySigmobError entitySigmobError = PointEntitySigmobError.SigmobError(category, code, message);

        if (onPointEntityExtraInfo != null) {
            onPointEntityExtraInfo.onAddExtra(entitySigmobError);
        }
        updateADunitInfo(category, category, adUnit, entitySigmobError);

        entitySigmobError.commit();

    }


    public static void touchEventRecord(BaseAdUnit adUnit, MotionEvent event, String category, boolean isValidClick) {


        long startTime = event.getDownTime();
        long endTime = event.getEventTime();
        long touchTime = endTime - startTime;


        PointEntityClick pointEntityClick = new PointEntityClick();

        pointEntityClick.setAc_type(PointType.ANTI_SPAM_TOUCH);
        pointEntityClick.setCategory(category);

        pointEntityClick.setLocation(String.format("{x:%f,y:%f}", event.getRawX(), event.getRawY()));
        pointEntityClick.setClick_duration(String.valueOf(touchTime));
        pointEntityClick.setPressure(String.valueOf(event.getPressure()));
        pointEntityClick.setTouchSize(String.valueOf(event.getSize()));
        pointEntityClick.setTouchType(String.valueOf(event.getToolType(0)));
        pointEntityClick.setIs_valid_click(isValidClick ? Constants.SUCCESS : Constants.FAIL);


        if (adUnit != null) {
            pointEntityClick.setLoad_id(adUnit.getLoad_id());
        }

        pointEntityClick.commit();


    }

    public static void eventDownloadTracking(final DownloadItem item, final BaseAdUnit adUnit, final String error, final boolean isCache) {

        PointEntitySigmob entitySigmob = new PointEntitySigmob();

        entitySigmob.setAc_type(PointType.DOWNLOAD_TRACKING);
        entitySigmob.setIssuccess(String.valueOf(item.status));
        entitySigmob.setIscached(isCache ? Constants.SUCCESS : Constants.FAIL);
        entitySigmob.setDuration(String.valueOf(item.networkMs));
        entitySigmob.setFile_size(String.valueOf(item.size));
        entitySigmob.setFile_name(Base64.encodeToString(item.url.getBytes(), Base64.NO_WRAP));
        entitySigmob.setError_message(error);
        entitySigmob.setCategory(String.valueOf(item.type.getType()));
        updateADunitInfo(entitySigmob.getCategory(), entitySigmob.getSub_category(), adUnit, entitySigmob);
        entitySigmob.commit();

    }

    public static void eventTracking(AdTracker tracker, String url, BaseAdUnit adUnit, final VolleyError volleyError) {
        NetworkResponse response = null;
        if (volleyError != null) {
            response = volleyError.networkResponse;
        }
        if (response != null) {
            eventTracking(tracker, url, adUnit, response, null);
        } else {
            eventTracking(tracker, url, adUnit, response, new OnPointEntityExtraInfo() {
                @Override
                public void onAddExtra(Object pointEntityBase) {
                    if (pointEntityBase instanceof PointEntitySigmob) {
                        PointEntitySigmob pointEntitySigmob = (PointEntitySigmob) pointEntityBase;
                        pointEntitySigmob.setHttp_code("-1");
                        if (volleyError != null) {
                            pointEntitySigmob.setTime_spend(String.valueOf(volleyError.getNetworkTimeMs()));
                            pointEntitySigmob.setError_message(volleyError.getMessage());
                        } else {
                            pointEntitySigmob.setTime_spend(Constants.FAIL);
                        }

                    }
                }
            });
        }
    }

    protected static void updatePointEntitySigmob(PointEntitySigmob entitySigmob) {

        WindAdOptions options = WindAds.sharedAds().getOptions();
        if (options != null && WindAds.sharedAds().getOptions().getCustomController() != null) {
            WindCustomController controller = options.getCustomController();
            entitySigmob.setIs_custom_imei(controller.isCanUsePhoneState() ? "0" : "1");
            entitySigmob.setIs_custom_android_id(controller.isCanUseAndroidId() ? "0" : "1");
            entitySigmob.setIs_custom_oaid(TextUtils.isEmpty(controller.getDevOaid()) ? "0" : "1");
        } else {
            entitySigmob.setIs_custom_imei("0");
            entitySigmob.setIs_custom_android_id("0");
            entitySigmob.setIs_custom_oaid("0");
        }
    }

    public static void eventTracking(final AdTracker tracker, final String url, BaseAdUnit adUnit, final NetworkResponse response, final OnPointEntityExtraInfo onPointEntityExtraInfo) {


        SigmobTracking.getSigmobTracking(tracker.getEvent())
                .setAc_Type(PointType.SIGMOB_REPORT_TRACKING)
                .setAdUnit(adUnit)
                .setOnPointEntityExtraInfo(new OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {

                        if (pointEntityBase instanceof PointEntitySigmob) {
                            PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                            entitySigmob.setAc_type(PointType.SIGMOB_REPORT_TRACKING);
                            entitySigmob.setUrl(url);

                            if (tracker != null) {

                                if (tracker.getId() != null) {
                                    entitySigmob.setRetry(Constants.SUCCESS);
                                } else {
                                    entitySigmob.setRetry(Constants.FAIL);
                                }
                                entitySigmob.setCategory(tracker.getEvent());

                                entitySigmob.setRequest_id(tracker.getRequest_id());
                                if (tracker.getTimestamp() != 0) {
                                    entitySigmob.setTimestamp(String.valueOf(tracker.getTimestamp()));
                                }

                                entitySigmob.setSource(tracker.getSource());
                                String base64 = null;

                                if (response != null) {
                                    if (response.data != null) {
                                        base64 = Base64.encodeToString(response.data, Base64.NO_WRAP);

                                    }
                                    entitySigmob.setResponse(base64);
                                    entitySigmob.setHttp_code(String.valueOf(response.statusCode));
                                    entitySigmob.setTime_spend(String.valueOf(response.networkTimeMs));
                                    entitySigmob.setContent_type(response.headers.get("Content-Type"));
                                    entitySigmob.setContent_length(response.headers.get("Content-Length"));

                                }
                            }
                        }

                        if (onPointEntityExtraInfo != null) {
                            onPointEntityExtraInfo.onAddExtra(pointEntityBase);
                        }

                    }
                }).send();


    }

    public static void eventRecord(final BaseAdUnit adUnit, final String event, final PackageInfo info, final String subCate) {

        ThreadPoolFactory.BackgroundThreadPool.getInstance().submit(new Runnable() {
            @Override
            public void run() {

                PointEntitySigmobUtils.SigmobTracking(event, subCate, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        Context context = SDKContext.getApplicationContext();
                        if (pointEntityBase instanceof PointEntitySigmob) {

                            PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                            Map<String, String> option = new HashMap<>();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                try {
                                    option.put(WindConstants.ALLOW_INSTALL, context.getPackageManager().canRequestPackageInstalls() ? "1" : "0");
                                } catch (Throwable e) {
                                    SigmobLog.e(e.getMessage());
                                }
                            }
                            //packageInfo
                            if (info != null) {


                                ApplicationInfo appInfo = info.applicationInfo;
                                String appName = null;

                                try {
                                    appName = context.getPackageManager().getApplicationLabel(appInfo).toString();
                                    option.put(WindConstants.APP_NAME, appName);
                                } catch (Throwable e) {
                                }
                                option.put(WindConstants.PACKAGE_NAME, info.packageName);
                                option.put(WindConstants.UPDATE, String.valueOf(info.lastUpdateTime));
                                option.put(WindConstants.APP_VERSION, info.versionName);

                            }


                            entitySigmob.setOptions(option);
                        }

                    }
                });


            }
        });

    }

    public static void eventRecord(final String category, final BaseAdUnit adUnit,
                                   final String isDeepLink, final String targetUrl, final String coordinate) {
        eventRecord(null, category, adUnit, isDeepLink, targetUrl, coordinate, 0);
    }

    public static void eventRecord(ClickUIType clickUIType, final String sub_category, final BaseAdUnit adUnit,
                                   final String isDeepLink, final String targetUrl, final String coordinate) {
        eventRecord(clickUIType, sub_category, adUnit, isDeepLink, targetUrl, coordinate, 0);
    }

    public static void eventRecord(ClickUIType clickUIType, final String sub_category, final BaseAdUnit adUnit,
                                   final String isDeepLink, final String targetUrl, final String coordinate,
                                   final long duration) {
        eventRecord(clickUIType, sub_category, adUnit, isDeepLink, targetUrl, coordinate, duration, null);

    }

    public static void eventRecord(ClickUIType clickUIType, final String sub_category, final BaseAdUnit adUnit,
                                   final String isDeepLink, final String targetUrl, final String coordinate,
                                   final long duration, final JSONObject object) {

        String category = clickUIType == null ? sub_category : clickUIType.name().toLowerCase();

        PointEntitySigmobUtils.SigmobTracking(category, sub_category, adUnit, new OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {

                    Map<String, String> options = ((PointEntitySigmob) pointEntityBase).getOptions();

                    if (adUnit != null && sub_category != null && sub_category.equalsIgnoreCase(PointCategory.CLICK)) {
                        ((PointEntitySigmob) pointEntityBase).setScene_id(adUnit.getAd_scene_id());
                        ((PointEntitySigmob) pointEntityBase).setScene_desc(adUnit.getAd_scene_desc());

                        options.put("template_id", adUnit.getClickCommon().template_id);
                        options.put("sld", adUnit.getClickCommon().sld);
                        options.put("adarea_x", "0");
                        options.put("adarea_y", "0");
                        options.put("adarea_w", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                        options.put("adarea_h", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));
                        if ( adUnit.getClickCommon().sld != null && "5".equals(adUnit.getClickCommon().sld)) {
                            options.put("turn_x", adUnit.getClickCommon().turn_x);
                            options.put("turn_y", adUnit.getClickCommon().turn_y);
                            options.put("turn_z", adUnit.getClickCommon().turn_z);
                            options.put("turn_time", adUnit.getClickCommon().turn_time);
                        } else  if ( adUnit.getClickCommon().sld != null && "2".equals(adUnit.getClickCommon().sld)) {
                            options.put("x_max_acc", adUnit.getClickCommon().x_max_acc);
                            options.put("y_max_acc", adUnit.getClickCommon().y_max_acc);
                            options.put("z_max_acc", adUnit.getClickCommon().z_max_acc);
                        }

                        options.put("click_area", adUnit.getClickCommon().click_area);
                        options.put("click_scene", adUnit.getClickCommon().click_scene);
                        options.put("cwidth", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealWidthDip()));
                        options.put("cheight", String.valueOf(ClientMetadata.getInstance().getDeviceScreenRealHeightDip()));

                        options.put("is_final_click", adUnit.getClickCommon().is_final_click?Constants.SUCCESS:Constants.FAIL);
                    }

                    ((PointEntitySigmob) pointEntityBase).setIs_deeplink(isDeepLink);
                    ((PointEntitySigmob) pointEntityBase).setFinal_url(targetUrl);
                    ((PointEntitySigmob) pointEntityBase).setCoordinate(coordinate);
                    ((PointEntitySigmob) pointEntityBase).setVtime(String.format("%.2f", duration / 1000.0f));

                    if (object != null) {
                        for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                            String key = it.next();

                            try {
                                Object value = object.get(key);
                                if (value instanceof String) {
                                    options.put(key, (String) value);
                                } else {
                                    options.put(key, String.valueOf(value));
                                }
                            } catch (Throwable e) {
                                SigmobLog.e("log_data error" + object + " " + e.getMessage());
                            }

                        }
                    }
                }

            }
        });


    }


    public interface OnPointEntityExtraInfo {
        void onAddExtra(Object pointEntityBase);
    }

}
