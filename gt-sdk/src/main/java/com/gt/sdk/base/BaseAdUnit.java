package com.gt.sdk.base;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.text.TextUtils;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Md5Util;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.toolbox.StringUtil;
import com.czhj.wire.Wire;
import com.gt.sdk.base.common.BaseAdConfig;
import com.gt.sdk.base.common.SessionManager;
import com.gt.sdk.base.common.SigAdTracker;
import com.gt.sdk.base.common.SigMacroCommon;
import com.gt.sdk.base.models.SeatBid;


import java.io.File;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class BaseAdUnit implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TAG = "BaseAdUnit";

    private String adslot_id;

    private String camp_id;

    private String crid;

    private SeatBid ad;

    private int ad_type;

    private long create_time;

    private String video_md5;

    private String endcard_md5;

    private String ad_source_channel;

    private String request_id;

    private String load_id;

    private String bid_token;

    private String adx_id;


    private transient List imageUrlList;

    private String nativeTtitle;

    private String nativeDesc;

    private String nativeIconUrl;

    private SigVideo nativeVideo;

    private HashMap<String, List<SigAdTracker>> adTrackersMap;

    private SigMacroCommon macroCommon;

    private VideoStatusCommon videoCommon;
    private ClickCommon clickCommon;
    private String uuid;

    public SlotAdSetting slotAdSetting;
    private double adPercent = -1f;
    private double realAdPercent = -1f;

    private String ad_scene_id;
    private String ad_scene_desc;
    private Long downloadId;
    private AndroidMarket mCustomAndroidMarket;
    private String mCustomDeeplink;
    private String mCustomLandPageUrl;

    private boolean isHalfInterstitial;
    private transient AdAppInfo adAppInfo;
    private String rv_callback_url;
    private List<FractionalProgressAdTracker> download_trackers;
    private String downloadUrl;
    private String apkName;
    private String apkPackageName;

    private transient DownloadTask downloadTask;

    private boolean useDownloadedApk = false;

    public void setDownloadTask(DownloadTask task) {
        this.downloadTask = task;
    }

    public DownloadTask getDownloadTask() {
        return downloadTask;
    }

    public boolean isHalfInterstitial() {
        return isHalfInterstitial;
    }

    public void setHalfInterstitial(boolean halfInterstitial) {
        isHalfInterstitial = halfInterstitial;
    }

    public String getAd_scene_id() {
        return ad_scene_id;
    }

    public String getAd_scene_desc() {
        return ad_scene_desc;
    }

    private transient SessionManager mSessionManager;

    private int adWidth;
    private int adHeight;

    private transient BaseAdConfig adConfig;


    private boolean isDislikeReported = false;

    public BaseAdUnit() {

    }


    public int getFloor() {

        NativeAdSetting nativeAdSetting = getNativeAdSetting();
        if (nativeAdSetting != null) {
            return Wire.get(nativeAdSetting.media_expected_floor, 0);
        }
        return 0;
    }


    public void dislikeReport() {
        isDislikeReported = true;
    }

    public boolean isDislikeReported() {
        return isDislikeReported;
    }

    private boolean record = true;

    public boolean isRecord() {
        return record;
    }

    public void setRecord(boolean record) {
        this.record = record;
    }

    public void setAd_scene_id(String ad_scene_id) {
        this.ad_scene_id = ad_scene_id;
    }

    public void setAd_scene_desc(String ad_scene_desc) {
        this.ad_scene_desc = ad_scene_desc;
    }

    public String uid;
    public int expiration_time;
    public BiddingResponse bidding_response;

    public static BaseAdUnit adUnit(SeatBid ad, String request_id, LoadAdRequest adRequest, String uid) {

        BaseAdUnit baseAdUnit = null;
        try {
            MaterialMeta materialMeta = ad.materials.get(0);
            if (materialMeta != null) {

                baseAdUnit = new BaseAdUnit();

                baseAdUnit.create_time = System.currentTimeMillis();

                baseAdUnit.adslot_id = ad.adslot_id;
                baseAdUnit.ad_type = ad.ad_type;
                baseAdUnit.ad = ad;

                baseAdUnit.crid = ad.crid;
                baseAdUnit.camp_id = ad.camp_id;
                baseAdUnit.request_id = request_id;
                baseAdUnit.endcard_md5 = materialMeta.endcard_md5;
                baseAdUnit.video_md5 = materialMeta.video_md5;

                baseAdUnit.load_id = adRequest.getLoadId();
                baseAdUnit.ad_source_channel = ad.ad_source_channel;
                if (materialMeta.creative_type == CreativeType.CreativeTypeVideo_Html_Snippet.getCreativeType() || materialMeta.creative_type == CreativeType.CreativeTypeVideo_transparent_html.getCreativeType()) {
                    if (materialMeta.html_snippet != null && materialMeta.html_snippet.size() > 10) {
//                        SigmobFileUtil.writeToBuffer(materialMeta.html_snippet.toByteArray(),baseAdUnit.getEndCardIndexPath());
                    }
                }
                baseAdUnit.slotAdSetting = slotAdSetting;

                baseAdUnit.adRequest = adRequest;
                baseAdUnit.scene = scene;
                baseAdUnit.uid = uid;
                baseAdUnit.expiration_time = expiration_time;
                if (ad.bidding_response != null) {
                    baseAdUnit.bidding_response = ad.bidding_response;
                } else {
                    baseAdUnit.bidding_response = bidding_response;
                }

                baseAdUnit.useDownloadedApk = Wire.get(slotAdSetting.use_downloaded_apk, false);

                initAdTrackerMap(baseAdUnit);

                if (ad.ad_track_macro != null) {
                    baseAdUnit.getMacroCommon().setServerMacroMap(ad.ad_track_macro);
                }
                if (baseAdUnit.ad_type == 5) {
                    ResponseNativeAd nativeAd = baseAdUnit.getNativeAd();
                    if (nativeAd != null) {
                        if (nativeAd.type == 1) {
                            baseAdUnit.getNativeVideo();
                        } else {
                            baseAdUnit.getImageUrlList();
                        }
                    }
                }
            }

        } catch (Throwable throwable) {
            SigmobLog.e("adUnit error", throwable);
        }

        return baseAdUnit;
    }

    private LoadAdRequest adRequest;
    public Template scene;

    public Template getScene() {
        return scene;
    }

    public LoadAdRequest getAdRequest() {
        return adRequest;
    }

    private static void initAdTrackerMap(BaseAdUnit adUnit) {
        List<Tracking> tracks = adUnit.getAd_tracking();
        adUnit.adTrackersMap = new HashMap<>();
        for (Tracking adTracking : tracks) {
            List<SigAdTracker> trackers = createTrackersForUrls(adTracking.tracking_url, adTracking.tracking_event_type, adUnit.request_id, adUnit.getTrackingRetryNum());
            adUnit.adTrackersMap.put(adTracking.tracking_event_type, trackers);
        }
    }

    private static boolean checkFileMD5(String path, String md5) {

        String fileMD5 = Md5Util.fileMd5(path);
        SigmobLog.d("path: [ " + path + " ] calc [ " + fileMD5 + " ] origin " + md5);

        return fileMD5 != null && fileMD5.equalsIgnoreCase(md5);

    }

    public WXProgramRes getWXProgramRes() {
        Ad ad = getAd();
        if (ad != null && ad.wx_program_res != null) {
            return ad.wx_program_res;
        }
        return null;
    }

    public void setDownloadQuarterTrack(List<FractionalProgressAdTracker> tracks) {
        download_trackers = tracks;
    }

    public List<FractionalProgressAdTracker> getDownloadQuarterTrack() {
        return download_trackers;
    }

    public AndroidMarket getAndroidMarket() {

        AndroidMarket market = null;
        MaterialMeta material = getMaterial();
        if (material != null) {
            market = material.android_market;
        }

        if (market == null) {
            if (mCustomAndroidMarket != null) {
                return mCustomAndroidMarket;
            }
        }
        return market;
    }

    public String getRvCallBackUrl() {
        return rv_callback_url;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public static String getTAG() {
        return TAG;
    }

    public static List<SigAdTracker> createTrackersForUrls(final List<String> urls, String event, String request_id, Integer retryNum) {
        Preconditions.NoThrow.checkNotNull(urls);

        final List<SigAdTracker> trackers = new ArrayList<>();
        for (String url : urls) {
            SigAdTracker sigAdTracker = new SigAdTracker(url, event, request_id);
            sigAdTracker.setRetryNum(retryNum);
            trackers.add(sigAdTracker);
        }
        return trackers;
    }

    public boolean isDownloadDialog() {
        MaterialMeta materialMeta = getMaterial();
        if (materialMeta != null) {
            return materialMeta.download_dialog;
        }
        return false;
    }

    public BaseAdConfig getAdConfig() {
        if (adConfig == null) {
            switch (getAd_type()) {
                case AdFormat.FULLSCREEN_VIDEO:
                case AdFormat.REWARD_VIDEO: {
                    return adConfig = BaseVideoConfig.getAdConfig(this);
                }
                case SPLASH: {
                    return adConfig = SplashAdConfig.getAdConfig(this);
                }
                case AdFormat.UNIFIED_NATIVE: {
                    return adConfig = NativeAdConfig.getAdConfig(this);
                }
                case AdFormat.DRIFT: {
//                    return DriftAdConfig.getAdConfig(this);
                }
                case AdFormat.NEW_INTERSTITIAL: {
                    return adConfig = InterstitialConfig.getAdConfig(this);
                }
            }
        }
        return adConfig;
    }

    public SessionManager getSessionManager() {
        return mSessionManager;
    }

    public void setSessionManager(SessionManager sessionManager) {
        mSessionManager = sessionManager;
    }

    public void setAdSize(int width, int height) {
        this.adWidth = width;
        this.adHeight = height;
        getMacroCommon().addMarcoKey(SigMacroCommon._WIDTH_, String.valueOf(width));
        getMacroCommon().addMarcoKey(SigMacroCommon._HEIGHT_, String.valueOf(height));

    }

    public VideoStatusCommon getVideoCommon() {
        if (videoCommon == null) {
            videoCommon = new VideoStatusCommon();
        }
        return videoCommon;
    }

    public ClickCommon getClickCommon() {
        if (clickCommon == null) {
            clickCommon = new ClickCommon();
        }
        return clickCommon;
    }

    public String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    public SlotAdSetting getSlotAdSetting() {
        return slotAdSetting;
    }

    public NativeAdSetting getNativeAdSetting() {
        if (slotAdSetting != null) {
            return slotAdSetting.native_setting;
        }
        return null;
    }

    public boolean noHasDownloadDialog() {
        return (getInteractionType() != InterActionType.DownloadType && getInteractionType() != InterActionType.DownloadOpenDeepLinkType) || getadPrivacy() == null || !isDownloadDialog();
    }

    public String getLoad_id() {
        return load_id;
    }

    public void setLoad_id(String load_id) {
        this.load_id = load_id;
    }


    public String getAdx_id() {
        return adx_id;
    }

    public void setAdx_id(String adx_id) {
        this.adx_id = adx_id;
    }

    public String getBid_token() {
        return bid_token;
    }

    public void setBid_token(String bid_token) {
        this.bid_token = bid_token;
    }

    public SigMacroCommon getMacroCommon() {
        if (macroCommon == null) {
            macroCommon = new SigMacroCommon();
            String video_url = getVideo_url();
            if (!TextUtils.isEmpty(video_url)) {
                try {
                    String url = URLEncoder.encode(video_url, "UTF-8");
                    if (!TextUtils.isEmpty(url)) {
                        macroCommon.addMarcoKey(SigMacroCommon._VURL_, url);
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return macroCommon;
    }

    public void setMacroCommon(SigMacroCommon macroCommon) {
        this.macroCommon = macroCommon;
    }

    public int getTrackingRetryNum() {

        if (slotAdSetting != null) {
            return Wire.get(slotAdSetting.retry_count, 0);
        }
        return 0;
    }

    public boolean isDisablexRequestWith() {
        if (slotAdSetting != null) {
            return Wire.get(slotAdSetting.disable_x_requested_with, false);
        }
        return false;
    }

    public String getCTAText() {
        MaterialMeta materialMeta = getMaterial();
        String ctaText = null;
        if (materialMeta != null) {
            ctaText = materialMeta.button_text;
        }
        return !TextUtils.isEmpty(ctaText) ? ctaText : getInteractionType() != InterActionType.DownloadType ? "查看详情" : "立即下载";
    }

    public boolean isEndCardIndexExist() {

        if (!TextUtils.isEmpty(getEndcard_url()) && (getCreativeType() == CreativeType.CreativeTypeVideo_Tar.getCreativeType() || getCreativeType() == CreativeType.CreativeTypeVideo_Tar_Companion.getCreativeType())) {
            return new File(getEndCardIndexPath()).exists();
        } else {
            return true;
        }
    }

    public boolean checkEndCardZipValid() {

        if (TextUtils.isEmpty(getEndcard_url()) || TextUtils.isEmpty(endcard_md5)) return true;

        return checkFileMD5(getEndCardZipPath(), getEndcard_md5());

    }

    public boolean checkVideoValid() {

        if (TextUtils.isEmpty(getVideo_url()) || TextUtils.isEmpty(video_md5)) return true;

        return checkFileMD5(getVideoPath(), getVideo_OriginMD5());

    }

    public boolean isVideoExist() {

        if (TextUtils.isEmpty(getVideo_url())) return true;
        String path = getVideoPath();

        boolean result = new File(path).exists();

        SigmobLog.d("isVideoExist path :" + path + " isExist: " + result);
        return result;
    }

    public File getAdPrivacyTemplateFile() {
        AdPrivacy privacy = getadPrivacy();
        if (privacy != null) {
            String privacy_template_url = privacy.privacy_template_url;
            if (!TextUtils.isEmpty(privacy_template_url)) {
                String fileName = Md5Util.md5(privacy_template_url);
                File sigHtmlDir = SigmobFileUtil.getSigHtmlDir(SigmobFileUtil.SigHtmlPrivacyDir);
                File destFile = new File(sigHtmlDir, fileName + ".html");
                return destFile;
            }
        }
        return null;
    }

    public String getSplashFilePath() {

        if (CreativeType.CreativeTypeSplashVideo.getCreativeType() == getMaterial().creative_type) {
            return SigmobFileUtil.getSplashCachePath() + File.separator + Md5Util.md5(getMaterial().video_url);
        } else {
            return SigmobFileUtil.getSplashCachePath() + File.separator + Md5Util.md5(getMaterial().image_src);

        }
    }

    public String getSplashURL() {

        if (CreativeType.CreativeTypeSplashVideo.getCreativeType() == getMaterial().creative_type) {
            return getMaterial().video_url;
        } else {
            return getMaterial().image_src;
        }
    }

    public String getEndCardIndexPath() {
        //TODO get SDK root Path,  EndCard name use md5 with resource Path
        return getEndCardDirPath() + "endcard.html";
    }

    public String getEndCardDirPath() {

        return SigmobFileUtil.getVideoCachePath() + String.format("/%s/", getEndcard_md5());
    }

    public String getEndCardZipPath() {

        //TODO get SDK root Path,  EndCard name use md5 with resource Path
        return SigmobFileUtil.getVideoCachePath() + String.format("/%s.tgz", endcard_md5);
    }

    public String getVideo_url() {

        if (ad != null && ad.materials.size() > 0) {

            if (ad_type == 5) {
                SigVideo video = getNativeVideo();
                if (video != null) return StringUtil.getUrl(video.url);
            } else {
                MaterialMeta materialMeta = ad.materials.get(0);
                if (materialMeta != null) {
                    return StringUtil.getUrl(materialMeta.video_url);
                }
            }

        }
        return null;
    }

    public String getProxyVideoUrl() {

        String url = getVideo_url();
        if (!TextUtils.isEmpty(url)) {
            return AdStackManager.getHttProxyCacheServer().getProxyUrl(url);
        }
        return url;
    }

    public File getVideoProxyFile() {

        String url = getVideo_url();
        if (!TextUtils.isEmpty(url)) {
            return AdStackManager.getHttProxyCacheServer().getCacheFile(url);
        }
        return null;
    }

    public String getVideoPath() {

        File file = getVideoProxyFile();
        if (file != null) {
            return file.getAbsolutePath();
        }
        return null;
    }

    public String getVideoTmpPath() {

        return SigmobFileUtil.getCachePath() + String.format("/%s.mp4.tmp", getVideo_md5());
    }

    public String getAdslot_id() {
        return adslot_id;
    }

    public void setAdslot_id(String adslot_id) {
        this.adslot_id = adslot_id;
    }

    public String getCamp_id() {
        return camp_id;
    }

    public void setCamp_id(String camp_id) {
        this.camp_id = camp_id;
    }

    public String getCrid() {
        return crid;
    }

    public void setCrid(String crid) {
        this.crid = crid;
    }

    public Ad getAd() {
        return ad;
    }

    public void setAd(Ad ad) {
        this.ad = ad;
    }

    public ResponseNativeAd getNativeAd() {
        ResponseNativeAd nativeAd = null;
        if (ad != null && ad.materials != null && ad.materials.size() > 0) {
            nativeAd = ad.materials.get(0).native_ad;
        }
        return nativeAd;
    }

    public AdSetting getAdSetting() {
        if (ad != null) {
            return ad.ad_setting;
        }
        return null;
    }

    public SplashAdSetting getSplashAdSetting() {
        SlotAdSetting adSetting = getSlotAdSetting();
        if (adSetting != null) {
            return adSetting.splash_setting;
        }
        return null;
    }

    public RvAdSetting getRvAdSetting() {
        if (slotAdSetting != null) {
            return slotAdSetting.rv_setting;
        }
        return null;
    }

    public boolean getFullClickOnVideo() {
        RvAdSetting setting = getRvAdSetting();

        if (setting != null) {
            return setting.full_click_on_video;
        }
        return false;

    }

    public ClickAreaSetting getClickAreaSetting() {
        RvAdSetting setting = getRvAdSetting();

        if (setting.click_setting == null) {
            ClickAreaSetting.Builder builder = new ClickAreaSetting.Builder();
            builder.bottom = 0.1f;
            builder.right = 0.1f;
            builder.top = 0.1f;
            builder.left = 0.1f;

            return builder.build();
        }


        if (setting != null) {
            return setting.click_setting;
        }
        return null;
    }

    public Integer getAdExpiredTime() {
        if (ad != null && ad.expired_time != null) {
            return ad.expired_time * 1000;
        }
        return 0;

    }

    public MaterialMeta getMaterial() {
        if (ad != null && ad.materials != null && ad.materials.size() > 0) {
            return ad.materials.get(0);
        }
        return null;
    }

    public List<SigImage> getImageUrlList() {
        ResponseNativeAd responseNativeAd = getNativeAd();
        if (imageUrlList == null) {
            imageUrlList = new ArrayList();
            if (responseNativeAd != null) {
                if (responseNativeAd.type != 1) {
                    for (ResponseAsset asset : responseNativeAd.assets) {
                        ResponseAssetImage image = asset.image;
                        if (image != null) {
                            SigImage sigImage = new SigImage(image.url, image.w, image.h);
                            if (adPercent < 0 && image.w > 0 && image.h > 0) {
                                adPercent = image.w * 1.0f / image.h;
                            }
                            imageUrlList.add(sigImage);
                        }
                    }
                }
            }
        }
        return imageUrlList;
    }

    public SigVideo getNativeVideo() {
        ResponseNativeAd responseNativeAd = getNativeAd();
        if (nativeVideo == null) {
            if (responseNativeAd != null) {
                if (responseNativeAd.type == 1) {
                    for (ResponseAsset asset : responseNativeAd.assets) {

                        ResponseAssetVideo video = asset.video;
                        if (video != null) {
                            if (nativeVideo == null) {
                                nativeVideo = new SigVideo();
                            }
                            nativeVideo.url = video.url;
                            nativeVideo.height = video.h;
                            nativeVideo.width = video.w;
                            if (adPercent < 0 && video.h > 0 && video.w > 0) {
                                adPercent = video.w * 1.0f / video.h;
                            }
                        }
                        ResponseAssetImage image = asset.image;
                        if (image != null) {
                            if (nativeVideo == null) {
                                nativeVideo = new SigVideo();
                            }
                            nativeVideo.thumbUrl = image.url;
                        }
                    }
                }
            }
        }
        return nativeVideo;
    }

    public int getAd_type() {
        return ad_type;
    }

    public void setAd_type(int ad_type) {
        this.ad_type = ad_type;
    }

    public long getCreate_time() {
        return create_time;
    }

    public void setCreate_time(long create_time) {
        this.create_time = create_time;
    }

    public String getVideo_md5() {
        if (!TextUtils.isEmpty(video_md5)) return video_md5;
        return Md5Util.md5(getVideo_url());
    }

    public void setVideo_md5(String video_md5) {
        this.video_md5 = video_md5;
    }

    public String getVideo_OriginMD5() {
        return video_md5;
    }

    public String getEndcard_md5() {
        if (!TextUtils.isEmpty(endcard_md5)) return endcard_md5;

        return Md5Util.md5(getCrid());
    }

    public void setEndcard_md5(String endcard_md5) {
        this.endcard_md5 = endcard_md5;
    }

    public String getEndCard_OriginMD5() {
        return endcard_md5;
    }

    public String getAd_source_channel() {
        return ad_source_channel;
    }

    public void setAd_source_channel(String ad_source_channel) {
        this.ad_source_channel = ad_source_channel;
    }

    public String getRequestId() {
        return request_id;
    }

    public void setRequest_id(String request_id) {
        this.request_id = request_id;
    }


    public String getEndcard_url() {
        MaterialMeta materialMeta = getMaterial();
        if (materialMeta != null) {
            return materialMeta.endcard_url;
        }
        return null;
    }

    public List<Tracking> getAd_tracking() {

        if (ad != null) {
            return ad.ad_tracking;
        }
        return null;
    }

    public List<SigAdTracker> getAdTracker(String event) {
        if (adTrackersMap != null) {
            return adTrackersMap.get(event);
        }
        return null;
    }

    public String getAd_source_logo() {

        if (ad != null) {
            return ad.ad_source_logo;
        }
        return null;
    }

    public void setCustomAndroidMarket(AndroidMarket mCustomAndroidMarket) {
        this.mCustomAndroidMarket = mCustomAndroidMarket;
    }

    public void setCustomLandPageUrl(String mCustomLandPageUrl) {
        this.mCustomLandPageUrl = mCustomLandPageUrl;
    }

    public void setCustomDeeplink(String mCustomDeeplink) {
        this.mCustomDeeplink = mCustomDeeplink;
    }

    public String getLanding_page() {

        String loading_page = null;
        if (getMaterial() != null) {
            loading_page = getMaterial().landing_page;
        }

        if (TextUtils.isEmpty(loading_page)) {
            if (!TextUtils.isEmpty(mCustomLandPageUrl)) {
                return mCustomLandPageUrl;
            }
        }

        return loading_page;
    }

    public int getCreativeType() {
        if (getMaterial() != null) {
            return getMaterial().creative_type;
        }
        return 0;
    }

    public String getHtmlData() {
        if (getMaterial() == null || getMaterial().html_snippet == null && getMaterial().html_snippet.size() < 10)
            return null;
        return getMaterial().html_snippet.utf8();
    }

    public String getCloseCardHtmlData() {
        if (getMaterial() == null || getMaterial().closecard_html_snippet == null && getMaterial().closecard_html_snippet.size() < 10)
            return null;
        return getMaterial().closecard_html_snippet.utf8();
    }

    public String getHtmlUrl() {
        if (getMaterial() == null) return null;
        return getMaterial().html_url;
    }

    public CreativeResource.Type getCreativeResourceType() {

        if (!TextUtils.isEmpty(getEndcard_url()) && (getCreativeType() == CreativeType.CreativeTypeVideo_Tar.getCreativeType() || getCreativeType() == CreativeType.CreativeTypeVideo_Tar_Companion.getCreativeType())) {
            return CreativeResource.Type.NATIVE_RESOURCE;
        } else if (!TextUtils.isEmpty(getHtmlData())) {
            return CreativeResource.Type.HTML_RESOURCE;
        } else if (!TextUtils.isEmpty(getHtmlUrl())) {
            return CreativeResource.Type.URL_RESOURCE;
        } else {
            return CreativeResource.Type.NATIVE_RESOURCE;
        }
    }

    public String resourcePath() {
        if (!TextUtils.isEmpty(getEndcard_url()) && (getCreativeType() == CreativeType.CreativeTypeVideo_Tar.getCreativeType() || getCreativeType() == CreativeType.CreativeTypeVideo_Tar_Companion.getCreativeType())) {
            return getEndCardIndexPath();
        } else if (!TextUtils.isEmpty(getHtmlData())) {
            return getHtmlData();
        } else {
            return getHtmlUrl();
        }
    }

    public boolean getInvisibleAdLabel() {
        if (getAd_type() == SPLASH) {
            SplashAdSetting adSetting = getSplashAdSetting();
            if (adSetting != null) {
                return adSetting.invisible_ad_label;
            }
        } else if (getAd_type() == REWARD_VIDEO || getAd_type() == FULLSCREEN_VIDEO) {
            RvAdSetting adSetting = getRvAdSetting();
            if (adSetting != null) {
                return adSetting.invisible_ad_label;
            }
        }

        return false;
    }


    public int getPlayMode() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return getMaterial().play_mode;
        }
        return 0;
    }

    public String getProductId() {
        if (getAd() != null) {
            return getAd().product_id;
        }
        return null;
    }

    public String getAdLogo() {

        return getAd_source_logo();

    }

    public String getTitle() {
        MaterialMeta materialMeta = getMaterial();
        if (materialMeta != null) {
            return materialMeta.title;
        }
        return null;
    }

    public String getDesc() {
        MaterialMeta materialMeta = getMaterial();
        if (materialMeta != null) {
            return materialMeta.desc;
        }
        return null;
    }

    public String getIconUrl() {
        MaterialMeta materialMeta = getMaterial();
        if (materialMeta != null) {
            return materialMeta.icon_url;
        }
        return null;
    }

    public boolean isNativeAdH5() {

        return true;
    }

    public SingleNativeAdSetting getSingleNativeSetting() {
        AdSetting adSetting = getAdSetting();
        if (adSetting != null) {
            return adSetting.single_native_setting;
        }
        return null;
    }

    public AdPrivacy getadPrivacy() {

        MaterialMeta materialMeta = getMaterial();
        if (materialMeta != null) {
            return materialMeta.ad_privacy;
        }
        return null;
    }

    public String getVideoThumbUrl() {

        if (nativeVideo != null) {
            return nativeVideo.thumbUrl;
        }
        return null;
    }

    public String getAppName() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return material.app_name;
        }
        return null;
    }


    public String getCompanyName() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            return adPrivacy.privacy_template_info.get("app_company");
        }
        return null;
    }

    public String getPrivacyAppName() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            return adPrivacy.privacy_template_info.get("app_name");
        }
        return null;
    }

    public String getAppVersion() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            return adPrivacy.privacy_template_info.get("app_version");
        }
        return null;
    }

    public String getPermissionsUrl() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            return adPrivacy.privacy_template_info.get("app_permission_url");
        }
        return null;
    }

    public String getPermissions() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            String app_permission = adPrivacy.privacy_template_info.get("app_permission");
            return app_permission;
        }
        return null;
    }

    public String getPrivacyAgreementUrl() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            return adPrivacy.privacy_template_info.get("app_privacy_url");
        }
        return null;
    }

    public String getPrivacyAgreement() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            return adPrivacy.privacy_template_info.get("app_privacy_text");
        }
        return null;
    }

    public String getDescription() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            return adPrivacy.privacy_template_info.get("app_func");
        }
        return null;
    }

    public String getDescriptionUrl() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            return adPrivacy.privacy_template_info.get("app_func_url");
        }
        return null;
    }

    public double getAdPercent() {
        if (adPercent > 0) {
            return adPercent;
        }
        if (realAdPercent > 0) {
            return realAdPercent;
        }
        return 16 / 9.0f;

    }

    public void updateRealAdPercent(double adPercent) {
        realAdPercent = adPercent;
    }

    public boolean isSkipSigmobBrowser() {
//        if (ad_type == SPLASH || ad_type == AdFormat.UNIFIED_NATIVE) {
//            return false;
//        }

        AdSetting adSetting = getAdSetting();
        if (adSetting != null) {
            if (adSetting.in_app) {
                return false;
            }
        }

        return true;
    }

    public String getVid() {
        if (getAd() != null) {
            return getAd().vid;
        }
        return null;
    }

    public int getEndcardCloseImage() {
        RvAdSetting adSetting = getRvAdSetting();
        if (adSetting != null) {
            return adSetting.endcard_close_image;
        }
        return 0;
    }

    public int getSkipSeconds() {

        if (ad_type == AdFormat.NEW_INTERSTITIAL) {
            InterstitialSetting adSetting = getNewInterstitialSetting();
            if (adSetting != null) {
                return adSetting.show_skip_seconds;
            }
        } else {
            RvAdSetting adSetting = getRvAdSetting();
            if (adSetting != null) {
                return Wire.get(adSetting.skip_seconds, 0);
            }
        }
        return -1;
    }

    public int getSkipPercent() {

        RvAdSetting adSetting = getRvAdSetting();
        if (adSetting != null) {
            return Wire.get(adSetting.skip_percent, 0);
        }
        return -1;

    }

    public float getFinishedTime() {
        RvAdSetting adSetting = getRvAdSetting();
        if (adSetting != null) {
            return adSetting.finished;
        }
        return 1.0f;
    }

    public boolean getDisableAutoLoad() {
        if (ad_type == AdFormat.NEW_INTERSTITIAL) {
            InterstitialSetting adSetting = getNewInterstitialSetting();
            if (adSetting != null) {
                return adSetting.disable_auto_load;
            }
        } else {
            RvAdSetting adSetting = getRvAdSetting();
            if (adSetting != null) {
                return adSetting.disable_auto_load;
            }
        }
        return false;


    }

    public int getRewardSeconds() {
        RvAdSetting adSetting = getRvAdSetting();
        if (adSetting != null) {
            return Wire.get(adSetting.reward_seconds, 0);
        }
        return 0;
    }

    public int getRewardPercent() {
        RvAdSetting adSetting = getRvAdSetting();
        if (adSetting != null) {
            return Wire.get(adSetting.reward_percent, 0);
        }
        return 0;
    }

    public int getChargeSeconds() {
        RvAdSetting adSetting = getRvAdSetting();
        if (adSetting != null) {
            return Wire.get(adSetting.charge_seconds, 0);
        }
        return 0;
    }

    public int getChargePercent() {
        RvAdSetting adSetting = getRvAdSetting();
        if (adSetting != null) {
            return Wire.get(adSetting.charge_percent, 0);
        }
        return 0;
    }

    public int getConfirmDialog() {
        RvAdSetting adSetting = getRvAdSetting();
        if (adSetting != null) {
            return Wire.get(adSetting.confirm_dialog, 0);
        }
        return 0;
    }

    public int getRewardStyle() {

        RvAdSetting adSetting = getRvAdSetting();
        if (adSetting != null) {
            return Wire.get(adSetting.reward_style, 0);
        }
        return 0;
    }

    public int getEndTime() {
        if (ad_type == AdFormat.NEW_INTERSTITIAL) {

            return 0;
        } else {
            RvAdSetting adSetting = getRvAdSetting();
            if (adSetting != null) {
                return adSetting.end_time;
            }
        }
        return 0;
    }

    public boolean isUse_floating_btn() {
        SplashAdSetting adSetting = getSplashAdSetting();
        if (adSetting != null) {
            return adSetting.use_floating_btn;
        }
        return false;
    }

    public boolean enable_full_click() {
        SplashAdSetting adSetting = getSplashAdSetting();
        if (adSetting != null) {
            return adSetting.enable_full_click;
        }
        return false;
    }

    public void setDownloadId(Long downloadId) {
        this.downloadId = downloadId;
    }

    public Long getDownloadId() {
        return downloadId;
    }

    public String getDeeplinkUrl() {
        String deeplink = null;
        MaterialMeta material = getMaterial();
        if (material != null) {
            deeplink = material.deeplink_url;
        }

        if (TextUtils.isEmpty(deeplink)) {
            if (!TextUtils.isEmpty(mCustomDeeplink)) {
                return mCustomDeeplink;
            }
        }

        return deeplink;
    }

    public int getsubInteractionType() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return material.sub_interaction_type;
        }
        return 0;
    }

    public boolean isClickAutoCloseSplash() {
        return false;
    }

    public int getInteractionType() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return material.interaction_type;
        }
        return 1;
    }

    public String getLandUrl() {
        return landUrl;
    }

    private String landUrl;

    public void setUrl(String url) {
        landUrl = url;
    }

    private boolean catchVideo = false;

    public void setCatchVideo(boolean catchVideo) {
        this.catchVideo = catchVideo;
    }

    public boolean isCatchVideo() {
        return catchVideo;
    }

    public int getDuration() {
        int endTime = getEndTime();
        if (endTime > 0) {
            return endTime * 1000;
        } else {
            return 33333;
        }
    }


    public int getTemplateType() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return material.template_type;
        }
        return 0;
    }

    public int getTemplateId() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return material.template_id;
        }

        return 0;
    }

    public String getMainImage() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return material.image_src;
        }
        return null;
    }

    public InterstitialSetting getNewInterstitialSetting() {
        SlotAdSetting adSetting = getSlotAdSetting();
        if (adSetting != null) {
            return adSetting.interstitial_setting;
        }
        return null;
    }

    public String getCreativeTitle() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return material.creative_title;
        }
        return null;
    }

    public String getEndCardImageUrl() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return material.endcard_image_src;
        }
        return null;
    }

    public int getIsMute() {
        if (ad_type == AdFormat.NEW_INTERSTITIAL) {
            InterstitialSetting adSetting = getNewInterstitialSetting();
            if (adSetting != null) {
                return adSetting.if_mute;
            }
        } else {
            RvAdSetting adSetting = getRvAdSetting();
            if (adSetting != null) {
                return adSetting.if_mute;
            }
        }
        return 0;
    }

    public boolean hasEndCard() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return material.has_endcard;
        }
        return false;
    }

    public AdAppInfo getAdAppInfo() {

        AdPrivacy adPrivacy = getadPrivacy();
        if (adAppInfo == null && adPrivacy != null) {

            try {
                adAppInfo = new AdAppInfo() {

                    @Override
                    public String getAppName() {
                        return BaseAdUnit.this.getPrivacyAppName();
                    }

                    @Override

                    public String getAuthorName() {
                        return BaseAdUnit.this.getCompanyName();
                    }

                    @Override

                    public String getPermissionsUrl() {
                        return BaseAdUnit.this.getPermissionsUrl();
                    }

                    @Override

                    public String getPermissions() {
                        return BaseAdUnit.this.getPermissions();
                    }

                    @Override
                    public String getPrivacyAgreement() {
                        return BaseAdUnit.this.getPrivacyAgreement();
                    }

                    @Override
                    public String getPrivacyAgreementUrl() {
                        return BaseAdUnit.this.getPrivacyAgreementUrl();
                    }

                    @Override
                    public String getVersionName() {
                        return BaseAdUnit.this.getAppVersion();
                    }

                    @Override
                    public String getDescriptionUrl() {
                        return BaseAdUnit.this.getDescriptionUrl();
                    }

                    @Override
                    public String getDescription() {
                        return BaseAdUnit.this.getDescription();
                    }

                    @Override
                    public int getAppSize() {
                        return BaseAdUnit.this.getAppSize();
                    }


                    @Override
                    public String toString() {
                        return String.format("appName %s \n AuthorName %s \n  versionName %s \n permissionsUrl %s \n permissions %s \n" + "privacyAgreementUrl %s \n privacyAgreement %s \n descriptionUrl %s \n description %s \n  appsize %d", getAppName(), getAuthorName(), getVersionName(), getPermissionsUrl(), getPermissions(), getPrivacyAgreementUrl(), getPrivacyAgreement(), getDescriptionUrl(), getDescription(), getAppSize());
                    }
                };

            } catch (Throwable t) {

            }
        }

        return adAppInfo;

    }

    private int getAppSize() {
        AdPrivacy adPrivacy = getadPrivacy();
        if (adPrivacy != null && adPrivacy.privacy_template_info != null) {
            String appSize = adPrivacy.privacy_template_info.get("app_size");
            if (!TextUtils.isEmpty(appSize)) {
                try {
                    return Integer.parseInt(appSize);
                } catch (Throwable t) {

                }
            }
        }
        return 0;
    }

    public boolean canOpen() {

        String productId = apkPackageName;

        if (TextUtils.isEmpty(productId)) {
            productId = getProductId();
        }

        if (!TextUtils.isEmpty(productId)) {
            Intent launchIntentForPackage = SDKContext.getApplicationContext().getPackageManager().getLaunchIntentForPackage(productId);
            return launchIntentForPackage != null;

        }
        return false;
    }

    public void setRvCallBackUrl(String rv_callback_url) {
        this.rv_callback_url = rv_callback_url;
    }

    public int getSensitivity() {
        AdSetting adSetting = getAdSetting();
        if (adSetting != null) {
            return adSetting.sensitivity;
        }
        return 0;
    }

    public boolean canUseDownloadApk() {
        return useDownloadedApk;
    }

    public void enableUseDownloadApk(boolean enable) {
        useDownloadedApk = enable;
    }


    public int getButtonColor() {

        MaterialMeta material = getMaterial();
        if (material != null && material.button_color != null) {
            return Color.argb((int) (material.button_color.alpha * 255), material.button_color.red, material.button_color.green, material.button_color.blue);
        }
        return Color.parseColor("#FF5A57");
    }

    public int getDisplay_orientation() {
        Ad ad = getAd();
        if (ad != null) {
            return Wire.get(ad.display_orientation, 0);
        }
        return 0;
    }

    public long getWidgetId(int index) {
        MaterialMeta material = getMaterial();
        if (material != null) {
            List<Widget> widgets = material.Widget_list;
            if (widgets != null && !widgets.isEmpty()) {
                Widget widget = widgets.get(index);
                if (widget != null) {
                    return widget.widget_id;
                }
            }
        }
        return 0;
    }

    public int getClickType() {
        MaterialMeta material = getMaterial();
        if (material != null) {
            return Wire.get(material.click_type, 0);
        }
        return 0;
    }

    public int getApkDownloadType() {
        SlotAdSetting slotAdSetting = getSlotAdSetting();

        if (slotAdSetting != null) {
            return Wire.get(slotAdSetting.apk_download_type, 0);
        }
        return 0;
    }

    public String getApkMd5() {

        MaterialMeta material = getMaterial();
        if (material != null) {
            return Wire.get(material.apk_md5, null);
        }
        return null;
    }


    public void setApkName(String apkName) {
        this.apkName = apkName;
    }

    public String getApkName() {
        return apkName;
    }

    public void setDownloadUrl(String url) {
        this.downloadUrl = url;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setApkPackageName(String packageName) {
        this.apkPackageName = packageName;
    }

    public String getApkPackageName() {
        return apkPackageName;
    }

    public boolean isResumableDownload() {
        if (slotAdSetting != null) {
            return Wire.get(slotAdSetting.resumable_download, false);
        }
        return false;
    }

    public boolean canInstall(String apkName) {

        boolean flag = !TextUtils.isEmpty(getApkMd5()) || getDownloadTask() != null || getDownloadId() != null;

        if (!canUseDownloadApk() || TextUtils.isEmpty(apkName) || !flag) {
            return false;
        }
        File file = new File(SigmobFileUtil.getDownloadAPKPathFile(SDKContext.getApplicationContext()), apkName);
        if (file != null && file.exists()) {
            PackageInfo info = ClientMetadata.getPackageInfoWithUri(SDKContext.getApplicationContext(), file.getAbsolutePath());
            if (info != null) {
                return true;
            }
        }

        return false;
    }

    public void destroy() {
        if (this.adConfig != null) {
            this.adConfig.destroy();
            this.adConfig = null;
        }
        if (mSessionManager != null) {
            mSessionManager.endDisplaySession();
        }
    }

    public boolean isExpiredAd() {

        long expireTime = getAdExpiredTime();
        if (expireTime == 0 || create_time == 0) {
            return false;
        }
        long time = System.currentTimeMillis() - create_time;
        boolean result = time > expireTime;

        return result;

    }

    public int getBP() {
        if (ad != null) {
            return Wire.get(ad.bid_price, 0);
        }
        return 0;
    }

    public int getBidEcpm() {
        if (ad != null) {
            BiddingResponse biddingResponse = ad.bidding_response;
            if (biddingResponse != null) {
                return Wire.get(biddingResponse.ecpm, 0);
            }
        }
        return 0;
    }
}

