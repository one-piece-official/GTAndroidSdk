package com.gt.sdk.base.models;


import android.graphics.Color;
import android.text.TextUtils;

import com.czhj.sdk.common.models.AdFormat;
import com.czhj.sdk.common.utils.Md5Util;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.manager.WindSDKConfig;
import com.gt.sdk.base.LoadAdRequest;
import com.gt.sdk.base.common.ADEvent;
import com.gt.sdk.base.common.BaseAdConfig;
import com.gt.sdk.base.common.InterActionType;
import com.gt.sdk.base.common.SessionManager;
import com.gt.sdk.base.common.SigAdTracker;
import com.gt.sdk.base.common.SigMacroCommon;
import com.gt.sdk.base.models.rtb.Adm;
import com.gt.sdk.base.models.rtb.Bid;
import com.gt.sdk.base.models.rtb.Image;
import com.gt.sdk.base.models.rtb.Tracking;
import com.gt.sdk.base.models.rtb.Video;
import com.gt.sdk.base.splash.SplashAdConfig;
import com.gt.sdk.utils.GtFileUtil;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


public class BaseAdUnit implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final String TAG = "BaseAdUnit";

    private String adSlot_id;

    private int ad_type;

    private long create_time;

    private String video_md5;

    private boolean useDownloadedApk = false;

    private transient List<SigImage> imageUrlList;

    private SigVideo nativeVideo;

    private HashMap<String, List<SigAdTracker>> adTrackersMap;

    private SigMacroCommon macroCommon;

    private String uuid;

    private double adPercent = -1f;
    private double realAdPercent = -1f;

    private Long downloadId;
    private boolean isHalfInterstitial;
    private String downloadUrl;
    private String apkName;
    private String apkPackageName;

    public boolean isHalfInterstitial() {
        return isHalfInterstitial;
    }

    public void setHalfInterstitial(boolean halfInterstitial) {
        isHalfInterstitial = halfInterstitial;
    }

    private int adWidth;
    private int adHeight;

    public BaseAdUnit() {

    }

    private boolean record = true;

    public boolean isRecord() {
        return record;
    }

    public void setRecord(boolean record) {
        this.record = record;
    }

    private Bid mAd;
    private int price;
    private Tracking tracking;
    private int action;
    private String deeplink_url;
    private String landing_url;
    private Adm adm;
    private int protocol_type;
    private Video video;
    private Image image;
    private List<Image> imageList;

    private String app_name = "app_name";
    private String brand_name = "brand_name";
    private String package_name = "package_name";
    private String app_size = "app_size";
    private String app_version = "app_version";
    private String developer = "developer";
    private String privacy_url = "privacy_url";
    private String permission_url = "permission_url";

    private LoadAdRequest adRequest;
    private transient SessionManager mSessionManager;
    private transient BaseAdConfig adConfig;


    public String getBidId() {
        return bidId;
    }

    public String getLogId() {
        return logId;
    }

    public String getAdId() {
        return adId;
    }

    public String getImpId() {
        return impId;
    }

    public String getAdmId() {
        return admId;
    }

    private String bidId;//本次响应标识 ID，用于日志和后续调试
    private String logId;//用于记录日志或行为追踪
    private String adId;//生成的素材校验 id
    private String impId;//对应请求中的 imp 的 id
    private String admId;//序号
    private String request_id;
    private String load_id;

    private Map<String, String> privacyMap = new HashMap<>();

    public BaseAdUnit(Bid ad, String request_id, String bidId, LoadAdRequest adRequest) {
        try {
            this.bidId = bidId;
            this.request_id = request_id;
            this.load_id = adRequest.getLoadId();
            if (ad != null) {
                this.logId = ad.id;
                this.adId = ad.adid;
                this.impId = ad.impid;
                if (ad.adm != null) {
                    this.admId = ad.adm.id;
                }
            }

            this.adRequest = adRequest;
            this.mAd = ad;
            this.price = ad.price;
            this.tracking = ad.tracking;
            this.action = ad.action;
            this.deeplink_url = ad.target_url;
            this.landing_url = ad.landing_url;
            this.protocol_type = ad.protocol_type;
            this.adm = ad.adm;
            if (ad.adm != null) {
                this.video = ad.adm.video;
            }
            if (ad.adm != null) {
                this.imageList = ad.adm.img;
                if (imageList != null && !imageList.isEmpty()) {
                    image = imageList.get(0);
                }
            }
            privacyMap.put(app_name, ad.app_name);
            privacyMap.put(brand_name, ad.brand_name);
            privacyMap.put(package_name, ad.package_name);
            privacyMap.put(app_size, String.valueOf(ad.app_size));
            privacyMap.put(app_version, ad.app_version);
            privacyMap.put(developer, ad.developer);
            privacyMap.put(privacy_url, ad.privacy_url);
            privacyMap.put(permission_url, ad.permission_url);

            initAdTrackerMap(this.tracking);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initAdTrackerMap(Tracking tracking) {
        this.adTrackersMap = new HashMap<>();
        List<String> clickTrackers = tracking.click_trackers;
        if (clickTrackers != null && !clickTrackers.isEmpty()) {
            createTrack(ADEvent.AD_CLICK, clickTrackers);
        }
        List<String> impTrackers = tracking.imp_trackers;
        if (impTrackers != null && !impTrackers.isEmpty()) {
            createTrack(ADEvent.AD_SHOW, impTrackers);
        }
        List<String> dplkTrackers = tracking.dplk_trackers;
        if (dplkTrackers != null && !dplkTrackers.isEmpty()) {
            createTrack(ADEvent.AD_OPEN_DEEPLINK_SUCCESS, dplkTrackers);
        }
        List<String> dplkFailTrackers = tracking.dplk_fail_trackers;
        if (dplkFailTrackers != null && !dplkFailTrackers.isEmpty()) {
            createTrack(ADEvent.AD_OPEN_DEEPLINK_FAIL, dplkFailTrackers);
        }
        List<String> dplkTryTrackers = tracking.dplk_try_trackers;
        if (dplkTryTrackers != null && !dplkTryTrackers.isEmpty()) {
            createTrack(ADEvent.AD_OPEN_DEEPLINK_START, dplkTryTrackers);
        }
        List<String> playerStartTrackers = tracking.player_start_trackers;
        if (playerStartTrackers != null && !playerStartTrackers.isEmpty()) {
            createTrack(ADEvent.VIDEO_PLAY_START, playerStartTrackers);
        }
        List<String> playerEndTrackers = tracking.player_end_trackers;
        if (playerEndTrackers != null && !playerEndTrackers.isEmpty()) {
            createTrack(ADEvent.VIDEO_PLAY_END, playerEndTrackers);
        }
        List<String> player_valid_trackers = tracking.player_valid_trackers;
        if (player_valid_trackers != null && !player_valid_trackers.isEmpty()) {
            createTrack(ADEvent.AD_PLAY_PLAYER_VALID, player_valid_trackers);
        }
        List<String> player_first_quartile_trackers = tracking.player_first_quartile_trackers;
        if (player_first_quartile_trackers != null && !player_first_quartile_trackers.isEmpty()) {
            createTrack(ADEvent.AD_PLAY_ONE_QUARTERS, player_first_quartile_trackers);
        }
        List<String> player_midpoint_trackers = tracking.player_midpoint_trackers;
        if (player_midpoint_trackers != null && !player_midpoint_trackers.isEmpty()) {
            createTrack(ADEvent.AD_PLAY_TWO_QUARTERS, player_midpoint_trackers);
        }
        List<String> player_third_quartile_trackers = tracking.player_third_quartile_trackers;
        if (player_third_quartile_trackers != null && !player_third_quartile_trackers.isEmpty()) {
            createTrack(ADEvent.AD_PLAY_THREE_QUARTERS, player_third_quartile_trackers);
        }
        List<String> download_trackers = tracking.download_trackers;
        if (download_trackers != null && !download_trackers.isEmpty()) {
            createTrack(ADEvent.AD_DOWNLOAD_START, download_trackers);
        }
        List<String> downloaded_trackers = tracking.downloaded_trackers;
        if (downloaded_trackers != null && !downloaded_trackers.isEmpty()) {
            createTrack(ADEvent.AD_DOWNLOAD_FINISH, downloaded_trackers);
        }
        List<String> install_trackers = tracking.install_trackers;
        if (install_trackers != null && !install_trackers.isEmpty()) {
            createTrack(ADEvent.AD_INSTALL_START, install_trackers);
        }
        List<String> installed_trackers = tracking.installed_trackers;
        if (installed_trackers != null && !installed_trackers.isEmpty()) {
            createTrack(ADEvent.AD_INSTALL_FINISH, installed_trackers);
        }
        List<String> active_trackers = tracking.active_trackers;
        if (active_trackers != null && !active_trackers.isEmpty()) {
            createTrack(ADEvent.AD_INSTALL_OPEN, active_trackers);
        }
    }

    private void createTrack(String eventName, List<String> tracks) {
        List<SigAdTracker> trackers = createTrackersForUrls(tracks, eventName, this.request_id, getTrackingRetryNum());
        this.adTrackersMap.put(eventName, trackers);
    }

    public List<SigAdTracker> getAdTracker(String event) {
        if (adTrackersMap != null) {
            return adTrackersMap.get(event);
        }
        return null;
    }

    public LoadAdRequest getAdRequest() {
        return adRequest;
    }

    private static boolean checkFileMD5(String path, String md5) {
        String fileMD5 = Md5Util.fileMd5(path);
        SigmobLog.d("path: [ " + path + " ] calc [ " + fileMD5 + " ] origin " + md5);
        return fileMD5 != null && fileMD5.equalsIgnoreCase(md5);
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public static String getTAG() {
        return TAG;
    }

    public List<SigAdTracker> createTrackersForUrls(final List<String> urls, String event, String request_id, Integer retryNum) {
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
        return false;
    }

    public BaseAdConfig getAdConfig() {
        if (adConfig == null) {
            switch (getAd_type()) {
                case AdFormat.SPLASH: {
                    return adConfig = SplashAdConfig.getAdConfig(this);
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

    public String getUuid() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
        return uuid;
    }

    public String getLoad_id() {
        return load_id;
    }

    public SigMacroCommon getMacroCommon() {
        if (macroCommon == null) {
            macroCommon = new SigMacroCommon();
        }
        return macroCommon;
    }

    public void setMacroCommon(SigMacroCommon macroCommon) {
        this.macroCommon = macroCommon;
    }

    public int getTrackingRetryNum() {
        return 0;
    }

    public boolean isDisableXRequestWith() {
        return false;
    }

    public String getCTAText() {
        String ctaText = null;
        if (adm != null) {
            ctaText = adm.button;
        }
        return !TextUtils.isEmpty(ctaText) ? ctaText : getInteractionType() != InterActionType.DownloadType ? "查看详情" : "立即下载";
    }

    public File getAdPrivacyTemplateFile() {
        String privacy_template_url = WindSDKConfig.getInstance().getPrivacyUrl();
        if (!TextUtils.isEmpty(privacy_template_url)) {
            String fileName = Md5Util.md5(privacy_template_url);
            File sigHtmlDir = GtFileUtil.getPrivacyHtmlDir();
            File destFile = new File(sigHtmlDir, fileName + ".html");
            return destFile;
        }
        return null;
    }

    public String getSplashFilePath() {
        if (isVideoAd()) {
            return GtFileUtil.getSplashCachePath() + File.separator + Md5Util.md5(getVideo_url());
        }

        if (isImageAd()) {
            return GtFileUtil.getSplashCachePath() + File.separator + Md5Util.md5(getImage_url());
        }
        return "";
    }

    public boolean isVideoAd() {
        return video != null && !TextUtils.isEmpty(video.url);
    }

    public boolean isImageAd() {
        return image != null && !TextUtils.isEmpty(image.url);
    }

    public String getSplashURL() {
        if (video != null && !TextUtils.isEmpty(video.url)) {
            return video.url;
        }

        if (image != null && !TextUtils.isEmpty(image.url)) {
            return image.url;
        }
        return "";
    }

    public String getVideo_url() {
        if (video != null) {
            if (TextUtils.isEmpty(video.url)) {
                return video.url;
            }
        }
        return null;
    }

    public String getVideoCover_url() {
        if (video != null) {
            if (TextUtils.isEmpty(video.cover)) {
                return video.cover;
            }
        }
        return null;
    }

    public String getImage_url() {
        if (image != null) {
            if (TextUtils.isEmpty(image.url)) {
                return image.url;
            }
        }
        return null;
    }

    public String getVideoTmpPath() {
        return GtFileUtil.getCachePath() + String.format("/%s.mp4.tmp", getVideo_md5());
    }

    public String getAdSlot_id() {
        if (adRequest != null) {
            return adRequest.getCodeId();
        }
        return adSlot_id;
    }

    public Bid getAd() {
        return mAd;
    }

    public boolean getFullClickOnVideo() {
        return false;
    }

    public Integer getAdExpiredTime() {
        return 0;
    }

    public List<SigImage> getImageUrlList() {
        if (adm != null) {
            List<Image> images = adm.img;
            if (images != null && !images.isEmpty()) {
                for (int i = 0; i < images.size(); i++) {
                    Image image = images.get(i);
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
        return imageUrlList;
    }

    public SigVideo getNativeVideo() {
        if (nativeVideo == null) {
            if (video != null) {
                nativeVideo = new SigVideo();
                nativeVideo.url = video.url;
                nativeVideo.thumbUrl = video.cover;
                nativeVideo.height = video.h;
                nativeVideo.width = video.w;
                if (adPercent < 0 && video.h > 0 && video.w > 0) {
                    adPercent = video.w * 1.0f / video.h;
                }
            }
        }
        return nativeVideo;
    }

    public int getAd_type() {
        return adRequest.getAdType();
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

    public String getVideo_OriginMD5() {
        return video_md5;
    }

    public String getRequestId() {
        return request_id;
    }

    public String getAd_source_logo() {
        if (mAd != null) {
            return mAd.ad_logo;
        }
        return null;
    }

    public String getLanding_page() {
        String loading_page = null;
        if (mAd != null) {
            loading_page = mAd.landing_url;
        }
        return loading_page;
    }

    public int getCreativeType() {
        return action;
    }

    public String getAdLogo() {
        return getAd_source_logo();
    }

    public String getTitle() {
        if (adm != null) {
            return adm.title;
        }
        return null;
    }

    public String getDesc() {
        if (adm != null) {
            return adm.desc;
        }
        return null;
    }

    public String getIconUrl() {
        if (adm != null) {
            return adm.icon;
        }
        return null;
    }

    public Map<String, String> getAdPrivacy() {
        return privacyMap;
    }

    public String getVideoThumbUrl() {
        if (nativeVideo != null) {
            return nativeVideo.thumbUrl;
        }
        return null;
    }

    public String getAppName() {
        if (mAd != null) {
            return mAd.app_name;
        }
        return null;
    }

    public String getCompanyName() {
        if (mAd != null) {
            return mAd.developer;
        }
        return null;
    }

    public String getPrivacyAppName() {
        if (mAd != null) {
            return mAd.app_name;
        }
        return null;
    }

    public String getAppVersion() {
        if (mAd != null) {
            return mAd.app_version;
        }
        return null;
    }

    public String getPermissionsUrl() {
        if (mAd != null) {
            return mAd.permission_url;
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

    public boolean isSkipSelfBrowser() {
//        if (ad_type == SPLASH || ad_type == AdFormat.UNIFIED_NATIVE) {
//            return false;
//        }
        return true;
    }

    public boolean isUse_floating_btn() {
        return false;
    }

    public boolean enable_full_click() {
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
        if (mAd != null) {
            deeplink = mAd.target_url;
        }
        return deeplink;
    }

    public int getSubInteractionType() {
        if (mAd != null) {
            return mAd.protocol_type;
        }
        return 0;
    }

    public boolean isClickAutoCloseSplash() {
        return false;
    }

    public int getInteractionType() {
        if (mAd != null) {
            return mAd.action;
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

    public int getTemplateType() {
        return 0;
    }

    public int getTemplateId() {
        return 0;
    }

    public String getMainImage() {
        if (image != null) {
            return image.url;
        }
        return null;
    }

    public String getCreativeTitle() {
        if (adm != null) {
            return adm.button;
        }
        return null;
    }

    private int getAppSize() {
        if (mAd != null) {
            return mAd.app_size;
        }
        return 0;
    }

    public int getSensitivity() {
        return 0;
    }

    public boolean canUseDownloadApk() {
        return useDownloadedApk;
    }

    public void enableUseDownloadApk(boolean enable) {
        useDownloadedApk = enable;
    }

    public int getButtonColor() {
        return Color.parseColor("#FF5A57");
    }

    public int getDisplay_orientation() {
        return 0;
    }

    /**
     * 点击类型，1=按钮点击，2=全屏点击
     */
    public int getClickType() {
        return 0;
    }

    public int getApkDownloadType() {
        return 0;
    }

    public String getApkMd5() {
        if (mAd != null && !TextUtils.isEmpty(mAd.target_url)) {
            return Md5Util.md5(mAd.target_url);
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

    public void destroy() {
        if (this.adConfig != null) {
            this.adConfig.destroy();
            this.adConfig = null;
        }
        if (mSessionManager != null) {
            mSessionManager.endDisplaySession(this);
        }
    }

    public boolean isExpiredAd() {
        return false;
    }

    public int getPrice() {
        if (mAd != null) {
            return mAd.price;
        }
        return 0;
    }

    public Map<String, String> getPrivacyMap() {
        return privacyMap;
    }
}

