package com.sigmob.sdk.base.network;

import static android.util.Base64.NO_WRAP;
import static com.czhj.sdk.common.models.ModelBuilderCreator.createDevice;
import static com.czhj.sdk.common.models.ModelBuilderCreator.createVersion;

import android.text.TextUtils;
import android.util.Base64;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.models.AdCache;
import com.czhj.sdk.common.models.AdSlot;
import com.czhj.sdk.common.models.App;
import com.czhj.sdk.common.models.BidRequest;
import com.czhj.sdk.common.models.Device;
import com.czhj.sdk.common.models.DeviceId;
import com.czhj.sdk.common.models.HeaderBidding;
import com.czhj.sdk.common.models.ModelBuilderCreator;
import com.czhj.sdk.common.models.Network;
import com.czhj.sdk.common.models.Privacy;
import com.czhj.sdk.common.models.User;
import com.czhj.sdk.common.models.Version;
import com.czhj.sdk.common.network.SigmobRequest;
import com.czhj.sdk.common.utils.AESUtil;
import com.czhj.sdk.common.utils.AppPackageUtil;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.common.utils.RomUtils;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.DefaultRetryPolicy;
import com.czhj.volley.NetworkResponse;
import com.czhj.volley.ParseError;
import com.czhj.volley.Request;
import com.czhj.volley.Response;
import com.czhj.volley.VolleyError;
import com.czhj.volley.toolbox.HttpHeaderParser;
import com.czhj.wire.Wire;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.PrivacyManager;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.common.AdLoadCheckUtil;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.CreativeType;
import com.sigmob.sdk.base.common.LoadCacheItem;
import com.sigmob.sdk.base.common.NativeLoadReadyRecordManager;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.LoadAdRequest;
import com.sigmob.sdk.base.models.rtb.Ad;
import com.sigmob.sdk.base.models.rtb.BidResponse;
import com.sigmob.sdk.base.models.rtb.NativeAdSetting;
import com.sigmob.sdk.base.models.rtb.SlotAdSetting;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmobError;
import com.sigmob.sdk.splash.ImageType;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AdsRequest extends SigmobRequest<BidResponse> {


    private final RequestFactory.LoadAdRequestListener mListener;
    private final LoadAdRequest adRequest;

    public AdsRequest(String url, final LoadAdRequest loadAdRequest,
                      final RequestFactory.LoadAdRequestListener listener) {

        super(url, Request.Method.POST, null);
        Preconditions.NoThrow.checkNotNull(listener);
        mListener = listener;
        adRequest = loadAdRequest;
        DefaultRetryPolicy retryPolicy = new DefaultRetryPolicy(
                DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        setRetryPolicy(retryPolicy);
        setShouldCache(false);

    }


    public RequestFactory.LoadAdRequestListener getListener() {
        return mListener;
    }

    public static BidRequest.Builder createBidRequest(LoadAdRequest loadAdRequest) {

        BidRequest.Builder builder = new BidRequest.Builder();

        try {
            App.Builder app = createApp();

            app.mraid1_version = createVersion("1.2").build();
            app.mraid2_version = createVersion("2.2").build();
            Device.Builder device = createDevice(SDKContext.getDeviceContext());
            DeviceId.Builder did = ModelBuilderCreator.createDeviceId(SDKContext.getDeviceContext());
            if (loadAdRequest != null) {
                did.user_id(loadAdRequest.getUserId());
            }
            RomUtils.RomInfo romInfo = RomUtils.getRomInfo();
            if (romInfo != null) {

                String osMarket = romInfo.getOsMarket();
                if (!TextUtils.isEmpty(osMarket)) {
                    int packageVersionCode = AppPackageUtil.getPackageVersionCode(SDKContext.getApplicationContext(), osMarket);
                    if (packageVersionCode != -1) {
                        device.market_version(packageVersionCode);
                    }
                }
            }

            try {
                int packageVersionCode = AppPackageUtil.getPackageVersionCode(SDKContext.getApplicationContext(), "com.huawei.hwid");
                if (packageVersionCode != -1) {
                    device.hms_version(packageVersionCode);
                }
            }catch (Throwable th){

            }

            device.did(did.build());

            Network.Builder network = ModelBuilderCreator.createNetwork();
            AdSlot.Builder addSlot = ModelBuilderCreator.createAdSlot();

            if (loadAdRequest != null) {

                addSlot.adslot_type.add(loadAdRequest.getAdType());

                if (loadAdRequest.getAdType() == AdFormat.UNIFIED_NATIVE) {
                    LoadCacheItem loadCacheItem = LoadCacheItem.loadCacheItem(loadAdRequest.getPlacementId());
                    if (loadCacheItem != null){
                        addSlot.media_request_count(loadCacheItem.media_request_count);
                        addSlot.media_ready_count(loadCacheItem.media_ready_count);
                        addSlot.req_interval_time(loadCacheItem.req_interval_time);
                        addSlot.pre_req_time(loadCacheItem.pre_req_time);
                    }

                    List<BaseAdUnit> adCacheList = AdStackManager.getNativeAdValidList(loadAdRequest.getPlacementId());
                    if (adCacheList != null) {
                        addSlot.cached_ad_size(adCacheList.size());
                    }
                }
                if (loadAdRequest.getAdType() == AdFormat.SPLASH) {
                    addSlot.material_type.add(ImageType.ImageTypeJPEG.getImageType());
                    addSlot.material_type.add(ImageType.ImageTypePNG.getImageType());
                    addSlot.material_type.add(ImageType.ImageTypeGIF.getImageType());
                    addSlot.creative_type.add(CreativeType.CreativeTypeImage.getCreativeType());
                    addSlot.creative_type.add(CreativeType.CreativeTypeSplashVideo.getCreativeType());

                } else {

                    if (loadAdRequest.getAdType() == AdFormat.REWARD_VIDEO || loadAdRequest.getAdType() == AdFormat.FULLSCREEN_VIDEO) {
                        addSlot.creative_type.add(CreativeType.CreativeTypeVideo_Tar.getCreativeType());
                        addSlot.creative_type.add(CreativeType.CreativeTypeVideo_Html_Snippet.getCreativeType());
                        addSlot.creative_type.add(CreativeType.CreativeTypeVideo_transparent_html.getCreativeType());
                        addSlot.creative_type.add(CreativeType.CreativeTypeVideo_EndCardURL.getCreativeType());
                        addSlot.creative_type.add(CreativeType.CreativeTypeMRAID.getCreativeType());
                        addSlot.creative_type.add(CreativeType.CreativeTypeMRAIDTWO.getCreativeType());

                    } else if (loadAdRequest.getAdType() == AdFormat.NEW_INTERSTITIAL) {

                        addSlot.creative_type.add(CreativeType.CreativeTypeMRAIDTWO.getCreativeType());
                        addSlot.creative_type.add(CreativeType.CreativeTypeNewInterstitial.getCreativeType());

                    }

                    Map<String, AdCache> historyAdCache = AdStackManager.shareInstance().getHistoryAdCache(loadAdRequest.getAdType());

                    if (historyAdCache != null) {
                        addSlot.ad_caches(historyAdCache);
                    }
                }
                ArrayList tempids = new ArrayList();
                tempids.add(6001);
                tempids.add(6002);
                addSlot.support_template_id(tempids);

                addSlot.sdk_strategy_index(1);

                if (!TextUtils.isEmpty(loadAdRequest.getPlacementId())) {
                    addSlot.adslot_id(loadAdRequest.getPlacementId());
                }

                addSlot.latest_crid(loadAdRequest.getLastCrid());
                addSlot.latest_camp_id(loadAdRequest.getLastCampid());
                addSlot.ad_count(loadAdRequest.getAdCount());
                addSlot.bidfloor(loadAdRequest.getBidFloor());
                if (loadAdRequest.getOptions() != null) {
                    builder.options(loadAdRequest.getOptions());
                }

                builder.ad_is_expired = loadAdRequest.isExpired();
                builder.request_scene_type = loadAdRequest.getRequest_scene_type();

                HeaderBidding.Builder headerBidding = new HeaderBidding.Builder();

                if (!TextUtils.isEmpty(loadAdRequest.getBidToken())) {
                    headerBidding.bid_token(loadAdRequest.getBidToken());
                }

                if (!TextUtils.isEmpty(loadAdRequest.getCurrency())) {
                    headerBidding.cur(loadAdRequest.getCurrency());
                }

                builder.header_bidding(headerBidding.build());
                if (!TextUtils.isEmpty(loadAdRequest.getLoadId())) {
                    if (builder.ext_options == null) {
                        builder.ext_options = new HashMap<>();
                    }
                    builder.ext_options.put("load_id", loadAdRequest.getLoadId());
                }
            } else {
                ArrayList tempids = new ArrayList();
                tempids.add(6001);
                tempids.add(6002);
                addSlot.material_type.add(ImageType.ImageTypeJPEG.getImageType());
                addSlot.material_type.add(ImageType.ImageTypePNG.getImageType());
                addSlot.material_type.add(ImageType.ImageTypeGIF.getImageType());
                addSlot.creative_type.add(CreativeType.CreativeTypeImage.getCreativeType());
                addSlot.creative_type.add(CreativeType.CreativeTypeSplashVideo.getCreativeType());
                addSlot.creative_type.add(CreativeType.CreativeTypeVideo_Tar.getCreativeType());
                addSlot.creative_type.add(CreativeType.CreativeTypeVideo_Html_Snippet.getCreativeType());
                addSlot.creative_type.add(CreativeType.CreativeTypeVideo_transparent_html.getCreativeType());
                addSlot.creative_type.add(CreativeType.CreativeTypeVideo_EndCardURL.getCreativeType());
                addSlot.creative_type.add(CreativeType.CreativeTypeMRAID.getCreativeType());
                addSlot.creative_type.add(CreativeType.CreativeTypeMRAIDTWO.getCreativeType());
                addSlot.creative_type.add(CreativeType.CreativeTypeNewInterstitial.getCreativeType());

                addSlot.support_template_id(tempids);
            }

            builder.device(createDevice(SDKContext.getDeviceContext()).build());

            builder.slots.add(addSlot.build());
            builder.user(createUser().build());
            builder.privacy(createPrivacy().build());


            builder.app(app.build());
            builder.device(device.build());

            builder.network(network.build());

            builder.req_timestamp = System.currentTimeMillis();

            if (!TextUtils.isEmpty(WindConstants.SDK_VERSION)) {
                Version.Builder version = ModelBuilderCreator.createVersion(WindConstants.SDK_VERSION);
                builder.sdk_version(version.build());
            }

            builder.disable_mediation = true;

            builder.wx_program_req(ModelBuilderCreator.createWXProgramReq().build());

            builder.disable_install_package = !AdLoadCheckUtil.canInstallPackage();

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

        return builder;
    }


    public static App.Builder createApp() {
        App.Builder appBuilder = ModelBuilderCreator.createApp();
        String fastAppPackageName = SDKContext.getFastAppPackageName();
        if (!TextUtils.isEmpty(fastAppPackageName)) {
            appBuilder.sdk_ext_cap.add(4);
        }
        appBuilder.app_id(WindAds.sharedAds().getAppId());
        return appBuilder;
    }

    public static User.Builder createUser() {
        User.Builder userBuilder = new User.Builder();

        userBuilder.is_minor = !PrivacyManager.getInstance().isAdult();
        userBuilder.disable_personalized_recommendation = !PrivacyManager.getInstance().isPersonalizedAdvertisingOn();
        userBuilder.change_recommendation_state = PrivacyManager.getInstance().changeRecommendationState();
        return userBuilder;
    }

    public static Privacy.Builder createPrivacy() {

        Privacy.Builder privacyBuilder = new Privacy.Builder();
        privacyBuilder.age(PrivacyManager.getInstance().getUserAge());
        privacyBuilder.child_protection(PrivacyManager.getInstance().getAge_restricted());

        int GDPRConSentStatus = 0;
        try {
            GDPRConSentStatus = PrivacyManager.getInstance().getGDPRConsentStatus();
        } catch (Throwable throwable) {

        }
        privacyBuilder.gdpr_consent(GDPRConSentStatus);

        return privacyBuilder;
    }

    @Override
    public Map<String, String> getHeaders() {
        Map<String, String> headers = super.getHeaders();

        if (WindConstants.ENCRYPT) {

            try {
                headers.put("agn", Base64.encodeToString(AESUtil.generateNonce(), NO_WRAP));
            } catch (NoSuchMethodError th) {
                headers.put("e", "1");
            }

        }

        headers.put("cp","1");
        return headers;
    }

    @Override
    public String getBodyContentType() {
        return "application/octet-stream";
    }

    @Override
    public byte[] getBody() {


        BidRequest.Builder builder = null;
        BidRequest request = null;
        try {

            if (adRequest != null) {


                builder = createBidRequest(adRequest);


                request = builder.build();
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

        if (request == null) {
            SigmobLog.e("builder Ads Post entry fail ");
            return null;
        }

        byte[] body;

        try {
            body = request.encode();
            body = DeflateUtils.compress(body);
        }catch (Throwable e) {
            getHeaders().remove("cp");
            body = request.encode();
        }

        try {

            if (WindConstants.ENCRYPT) {
                body = AESUtil.Encrypt(body, SigmobRequest.AESKEY);
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        return body;

    }


    @Override
    protected Response<BidResponse> parseNetworkResponse(final NetworkResponse networkResponse) {
        // NOTE: We never get status codes outside of {[200, 299], 304}. Those errors are sent to the
        // error listener.


        try {

            byte[] data;
            if (networkResponse.data == null) {
                return Response.error(new ParseError(networkResponse));
            }else{
                boolean cp = networkResponse.headers.containsKey("cp");
                if (cp && "1".equals(networkResponse.headers.get("cp"))){
                    data = DeflateUtils.decompress(networkResponse.data);
                }else {
                    data = networkResponse.data;
                }
            }

            BidResponse bidResponse = BidResponse.ADAPTER.decode(data);
            if (bidResponse != null) {
                return Response.success(bidResponse,  // Cast needed for Response generic.
                        HttpHeaderParser.parseCacheHeaders(networkResponse));

            } else {
                return Response.error(new ParseError(networkResponse));
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
            return Response.error(new ParseError(e));
        }
    }


    @Override
    protected void deliverResponse(final BidResponse bidResponse) {
        if (bidResponse != null) {

            if (adRequest.getAdType() == AdFormat.UNIFIED_NATIVE){

                LoadCacheItem loadCacheItem = LoadCacheItem.loadCacheItem(adRequest.getPlacementId());
                if (loadCacheItem != null) {
                    loadCacheItem.pre_req_time = System.currentTimeMillis();
                    loadCacheItem.load_time = loadCacheItem.pre_req_time;
                    loadCacheItem.media_ready_count = 0;
                    loadCacheItem.media_request_count = 0;
                    SlotAdSetting slotAdSetting = bidResponse.slot_ad_setting;

                    if (slotAdSetting != null){
                        NativeAdSetting nativeSetting = slotAdSetting.native_setting;
                        if (nativeSetting != null) {
                            loadCacheItem.req_pool_size = Wire.get(nativeSetting.ad_pool_size, 0);
                            loadCacheItem.req_interval_time = Wire.get(nativeSetting.req_interval_time,0);
                            loadCacheItem.media_expected_floor = Wire.get(nativeSetting.media_expected_floor,0);

                            NativeLoadReadyRecordManager.getInstance().update(Wire.get(nativeSetting.log_interval_time,0));
                        }
                    }
                }

                LoadCacheItem.addAdCacheLoadEvent(adRequest.getPlacementId(), loadCacheItem);

            }

            String uid = bidResponse.uid;
            if (!TextUtils.isEmpty(uid)) {
                if (WindConstants.ENCRYPT) {
                    try {
                        uid = AESUtil.DecryptStringServer(uid, SigmobRequest.AESKEY);
                    } catch (NoSuchMethodError error) {
                        uid = AESUtil.DecryptString(uid, SigmobRequest.AESKEY);
                    }
                }
                ClientMetadata.getInstance().setUid(uid);
            }

            if (!TextUtils.isEmpty(bidResponse.adx_id)) {
                adRequest.setAdx_id(bidResponse.adx_id);
            }
            adRequest.setRequestId(bidResponse.request_id);
            if (bidResponse.ads.size() > 0) {

                try {

                    ArrayList adUnits = new ArrayList();
                    for (int i = 0; i < bidResponse.ads.size(); i++) {
                        Ad ad = bidResponse.ads.get(i);
                        BaseAdUnit adUnit = BaseAdUnit.adUnit(ad,
                                bidResponse.request_id,
                                adRequest,
                                bidResponse.slot_ad_setting,
                                bidResponse.scene,
                                bidResponse.uid,
                                bidResponse.expiration_time,
                                bidResponse.bidding_response);

//                        BaseAdUnit adUnit = BaseAdUnit.adUnit(bidResponse, adRequest, ad);
                        adUnit.setAd_type(adRequest.getAdType());
                        adUnit.setAdx_id(adRequest.getAdx_id());
                        adUnit.setAdslot_id(adRequest.getPlacementId());
                        adUnit.setHalfInterstitial(adRequest.isHalfInterstitial());
                        adUnit.setRvCallBackUrl(bidResponse.rv_callback_url);
                        adUnits.add(adUnit);
                    }
                    if (adRequest.getAdType() == AdFormat.UNIFIED_NATIVE) {
                        LoadCacheItem loadCacheItem = LoadCacheItem.loadCacheItem(adRequest.getPlacementId());
                        if (loadCacheItem != null) {
                            AdStackManager.addCacheAdList(adRequest.getPlacementId(), adUnits, loadCacheItem.req_pool_size);
                        }


                    }
                    if (mListener != null){
                        mListener.onSuccess(adUnits, adRequest);
                    }

                    int adType = adRequest.getAdType();
                    if (adType == AdFormat.NEW_INTERSTITIAL
                            || adType == AdFormat.REWARD_VIDEO
                            || adType == AdFormat.FULLSCREEN_VIDEO) {
                        AdStackManager.addBidResponse(bidResponse.request_id, bidResponse);
                    }
                    return;
                } catch (Throwable e) {
                    SigmobLog.e("ads Response: error ", e);
                }
            } else {

                logServerError(adRequest.getAdType(), adRequest.getPlacementId(), bidResponse.request_id,
                        bidResponse.error_code.intValue(), bidResponse.error_message);
                mListener.onErrorResponse(bidResponse.error_code.intValue(), bidResponse.error_message, bidResponse.request_id, adRequest);
                return;
            }

        }else{
            if (adRequest.getAdType() == AdFormat.UNIFIED_NATIVE) {
                LoadCacheItem loadCacheItem = LoadCacheItem.loadCacheItem(adRequest.getPlacementId());
                loadCacheItem.load_time = loadCacheItem.pre_req_time;
            }

        }

        mListener.onErrorResponse(WindAdError.ERROR_SIGMOB_INFORMATION_LOSE.getErrorCode(), "bidResponse is null", null, adRequest);

    }

    private void logServerError(final int adType, final String placementId, final String request_id, int errorCode, String message) {
        PointEntitySigmobUtils.SigmobError(PointCategory.SERVER_ERROR, errorCode, message, null, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmobError) {
                    PointEntitySigmobError sigmobError = (PointEntitySigmobError) pointEntityBase;
                    sigmobError.setRequest_id(request_id);
                    sigmobError.setPlacement_id(placementId);
                    sigmobError.setAdType(String.valueOf(adType));
                }
            }
        });
    }

    @Override
    public void deliverError(VolleyError error) {
        if (adRequest.getAdType() == AdFormat.UNIFIED_NATIVE) {
            LoadCacheItem loadCacheItem = LoadCacheItem.loadCacheItem(adRequest.getPlacementId());
            if (loadCacheItem != null){
                loadCacheItem.load_time = loadCacheItem.pre_req_time;
            }
        }
        if (error instanceof ParseError) {
            mListener.onErrorResponse(WindAdError.ERROR_SIGMOB_INFORMATION_LOSE.getErrorCode(), error.getMessage(), null, adRequest);
        } else {
            SigmobLog.e(adRequest.getPlacementId() + " ERROR_SIGMOB_NETWORK " + error.getMessage());
            mListener.onErrorResponse(WindAdError.ERROR_SIGMOB_NETWORK.getErrorCode(), error.getMessage(), null, adRequest);
        }
    }

}

