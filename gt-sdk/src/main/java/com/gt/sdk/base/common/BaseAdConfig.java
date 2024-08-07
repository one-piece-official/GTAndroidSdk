package com.gt.sdk.base.common;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.network.JsonRequest;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.common.utils.TouchLocation;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.RequestQueue;
import com.czhj.volley.VolleyError;
import com.czhj.wire.Wire;
import com.gt.sdk.base.BaseAdUnit;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.models.rtb.AndroidMarket;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.mta.PointEntitySigmobError;
import com.sigmob.sdk.base.network.SigmobTrackingRequest;
import com.sigmob.sdk.base.utils.SigmobFileUtil;
import com.sigmob.sdk.base.views.DownloadDialog;
import com.sigmob.sdk.videoAd.FractionalProgressAdTracker;
import com.sigmob.sdk.videoAd.InterActionType;

import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BaseAdConfig implements Serializable {

    private static final long serialVersionUID = 2L;

    // Viewability
    private final List<FractionalProgressAdTracker> mFractionalTrackers;
    protected Map<String, String> mExternalViewabilityTrackers;
    protected String mClickThroughUrl;
    protected String mNetworkMediaFileUrl;

    // Custom extensions
    protected String mDiskMediaFileUrl;
    protected String mCustomCtaText;
    protected String mCustomSkipText;
    protected String mCustomCloseIconUrl;
    // Sigmob-specific metadata
    protected String mDspCreativeId;
    protected int skipPosition = 1;
    private boolean isDialogShow;
    /**
     * Flag to indicate if the VAST xml document has explicitly set the orientation as opposed to
     * using the default.
     */
    private boolean mIsForceOrientationSet;
    private String downloadUrl;
    private boolean userBrowser;
    private OnSigAdClickListener mOnSigAdClickListener;
    private DownloadDialog downloadDialog;
    private WeakReference<Activity> contextWeakReference;
    private DownloadDialog.onPrivacyClickListener mOnPrivacyClickListener;
    private WeakReference<BaseAdUnit> adUnitWeakReference;
    private boolean isDownloadDialog;
    private int duration;
    public boolean mIsRecord;

    protected BaseAdConfig() {

        mFractionalTrackers = new ArrayList<>();

    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public static BaseAdConfig getAdConfig(BaseAdUnit adUnit) {
        BaseAdConfig baseAdConfig = new BaseAdConfig();
        baseAdConfig.initAdConfig(adUnit);
        return baseAdConfig;
    }

    public boolean isDownloadDialog() {
        return isDownloadDialog;
    }

    public String getDspCreativeId() {
        return mDspCreativeId;
    }

    public void setDspCreativeId(final String dspCreativeId) {
        mDspCreativeId = dspCreativeId;
    }

    public void initAdConfig(BaseAdConfig adUnit) {
        adUnitWeakReference = new WeakReference<>(adUnit);
    }

    public String getClickThroughUrl() {
        return mClickThroughUrl;
    }

    public void setClickThroughUrl(final String clickThroughUrl) {
        mClickThroughUrl = clickThroughUrl;
    }

    public void setOnAdClickListener(OnSigAdClickListener onClickListener) {
        mOnSigAdClickListener = onClickListener;
    }


    public void addFractionalTrackers(final List<FractionalProgressAdTracker> fractionalTrackers) {
        Preconditions.NoThrow.checkNotNull(fractionalTrackers, "fractionalTrackers cannot be null");
        mFractionalTrackers.addAll(fractionalTrackers);
        Collections.sort(mFractionalTrackers);
    }

    public String getCustomCtaText() {
        return mCustomCtaText;
    }

    public void setCustomCtaText(final String customCtaText) {
        if (customCtaText != null) {
            mCustomCtaText = customCtaText;
        }
    }


    public String getCustomSkipText() {
        return mCustomSkipText;
    }

    public void setCustomSkipText(final String customSkipText) {
        if (customSkipText != null) {
            mCustomSkipText = customSkipText;
        }
    }


    public String getCustomCloseIconUrl() {
        return mCustomCloseIconUrl;
    }

    public void setCustomCloseIconUrl(final String customCloseIconUrl) {
        if (customCloseIconUrl != null) {
            mCustomCloseIconUrl = customCloseIconUrl;
        }
    }


    public Map<String, String> getExternalViewabilityTrackers() {
        return mExternalViewabilityTrackers;
    }


    public boolean isCustomForceOrientationSet() {
        return mIsForceOrientationSet;
    }


    /**
     * Gets the String specified in the VAST document regarding the skip offset. This should be in
     * the form HH:MM:SS[.mmm] or n%. (e.g. 00:00:12, 00:00:12.345, 42%).
     *
     * @return String representation of the skip offset or {@code null} if not set.
     */


    /**
     * Called when the video starts playing.
     *
     * @param context         The context. Can be application or activity context.
     * @param contentPlayHead Current video playback time.
     */
    public void handleImpression(final Context context, int contentPlayHead, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");
        PointEntitySigmobUtils.eventRecord(PointCategory.START, null, adUnit);

        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_START);
    }


    public void handleClick(final Context context, TouchLocation down, TouchLocation up, ClickUIType clickUIType, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

    }

    public void handleClose(final Context context, final int contentPlayHead, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

    }

    public void handleSkip(final Context context, final int contentPlayHead, final BaseAdUnit adUnit) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");
    }

    /**
     * Called when there is a problem with the video. Refer to the possible
     * for a list of problems.
     *
     * @param context The context. Can be application or activity context.
     */
    public void handleError(Context context, String error, BaseAdUnit adUnit, String cate, int duration) {
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");

        PointEntitySigmobUtils.SigmobError(cate, duration, error, adUnit);

    }


    /**
     * Gets the skip offset in milliseconds. If the skip offset would be past the video duration,
     * this returns the video duration. Returns null when the skip offset is not set or cannot be parsed.
     *
     * @return The skip offset in milliseconds. Can return null.
     */


    public void initFourElements(final Activity context, final BaseAdUnit adUnit, final DownloadDialog.onPrivacyClickListener onPrivacyClickListener) {

        if (adUnit == null) return;

        if (context != null) {
            contextWeakReference = new WeakReference<>(context);
        }
        mOnPrivacyClickListener = onPrivacyClickListener;
    }

    private BaseAdUnit getBaseAdUnit() {
        return adUnitWeakReference == null ? null : adUnitWeakReference.get();
    }

    public boolean showDownloadDialog() {

        Activity context = contextWeakReference != null ? contextWeakReference.get() : null;

        if (getBaseAdUnit() == null) return false;


        if (context != null && getBaseAdUnit().getadPrivacy() != null) {
            if (downloadDialog == null) {

                downloadDialog = new DownloadDialog(context, getBaseAdUnit());
                downloadDialog.setOnPrivacyClickListener(new DownloadDialog.onPrivacyClickListener() {
                    @Override
                    public void onCloseClick() {

                        if (downloadDialog != null) {
                            downloadDialog.dismiss();
                            downloadDialog.destroy();
                            downloadDialog = null;
                            isDialogShow = false;
                        }
                        if (getBaseAdUnit() == null) return;

                        SessionManager sessionManager = getBaseAdUnit().getSessionManager();
                        if (sessionManager != null) {
                            sessionManager.recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_CLOSE, 0);
                        }
                        if (mOnPrivacyClickListener != null) {
                            mOnPrivacyClickListener.onCloseClick();
                        }
                    }

                    @Override
                    public void onButtonClick(String url, String clickCoordinate) {//执行点击逻辑
                        if (getBaseAdUnit() == null) return;
                        if (mOnPrivacyClickListener != null) {
                            mOnPrivacyClickListener.onButtonClick(url, clickCoordinate);
                        }
                        handleUrlFourAction(getBaseAdUnit().getClickCommon().clickUIType, url, clickCoordinate, true);

                    }

                    @Override
                    public void onShowSuccess() {
                        if (getBaseAdUnit() == null) return;

                        SessionManager sessionManager = getBaseAdUnit().getSessionManager();

                        if (sessionManager != null) {
                            sessionManager.recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_SHOW, 0);
                        }
                        if (mOnPrivacyClickListener != null && isDialogShow) {
                            mOnPrivacyClickListener.onShowSuccess();
                        }
                    }
                });
            }

        }

        if (downloadDialog != null && downloadDialog.isRenderSuccess() && !isDialogShow) {
            if (context != null) {
                boolean finishing = context.isFinishing();
                boolean destroyed = context.isDestroyed();
                if (!finishing && !destroyed) {
                    downloadDialog.show();
                    isDialogShow = true;
                    return true;
                }
            }
            isDialogShow = false;
            downloadDialog.dismiss();
            downloadDialog.destroy();
            downloadDialog = null;
            return true;
        }
        return false;
    }

    public boolean showFourElements() {

        if (getBaseAdUnit() != null && getBaseAdUnit().isDownloadDialog()) {
            return showDownloadDialog();
        }
        return false;
    }

    public void destroy() {
        mOnPrivacyClickListener = null;
        mOnSigAdClickListener = null;
        if (downloadDialog != null) {
            downloadDialog.dismiss();
            downloadDialog.destroy();
            downloadDialog = null;
            isDialogShow = false;
        }
    }

    public void handleFourDownload(final String realClickUrl) {

        boolean isDownloading = DownloadAPK.isDownloading(downloadUrl);

        BaseAdUnit baseAdUnit = getBaseAdUnit();
        if (baseAdUnit == null) return;

        if (isDownloading) {
            try {
                SigToast.makeText(SDKContext.getApplicationContext(), "正在下载", Toast.LENGTH_LONG).show();
            } catch (Throwable throwable) {

            }
            return;
        }
        String tempUrl = baseAdUnit.getLanding_page();
        if (!TextUtils.isEmpty(realClickUrl)) {
            tempUrl = realClickUrl;
        }
        tempUrl = baseAdUnit.getMacroCommon().macroProcess(tempUrl);

        if (baseAdUnit.getsubInteractionType() == 1) {

            final String finalRealClickUrl = tempUrl;
            JsonRequest request = new JsonRequest(finalRealClickUrl, new JsonRequest.Listener() {

                @Override
                public void onSuccess(JSONObject response) {

                    try {

                        BaseAdUnit baseAdUnit1 = getBaseAdUnit();
                        if (baseAdUnit1 == null) return;
                        SigmobLog.d("GDTConvertRequest response " + response);
                        int ret = response.getInt("ret");
                        if (ret == 0) {

                            String link = response.getJSONObject("data").getString("dstlink");
                            String clickid = response.getJSONObject("data").getString("clickid");

                            SigMacroCommon rewardVideoMacroCommon = getBaseAdUnit().getMacroCommon();
                            rewardVideoMacroCommon.addMarcoKey(_CLICKID_, clickid);

                            downloadUrl = link;
                            DownloadAPK.downloadApk(link, getBaseAdUnit());

                        } else {
                            PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                @Override
                                public void onAddExtra(Object pointEntityBase) {
                                    if (pointEntityBase instanceof PointEntitySigmob) {
                                        PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                        entitySigmob.setFinal_url(downloadUrl);
                                    }
                                }
                            });

                            PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, response.toString(), getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                @Override
                                public void onAddExtra(Object pointEntityBase) {
                                    if (pointEntityBase instanceof PointEntitySigmobError) {
                                        PointEntitySigmobError entitySigmob = (PointEntitySigmobError) pointEntityBase;
                                        entitySigmob.setFinal_url(downloadUrl);
                                    }
                                }
                            });

                        }
                    } catch (Throwable e) {
                        SigmobLog.e(e.getMessage());
                        PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                            @Override
                            public void onAddExtra(Object pointEntityBase) {
                                if (pointEntityBase instanceof PointEntitySigmob) {
                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                    entitySigmob.setFinal_url(downloadUrl);
                                }
                            }
                        });

                        PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, e.getMessage(), getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                            @Override
                            public void onAddExtra(Object pointEntityBase) {
                                if (pointEntityBase instanceof PointEntitySigmobError) {
                                    PointEntitySigmobError entitySigmob = (PointEntitySigmobError) pointEntityBase;
                                    entitySigmob.setFinal_url(downloadUrl);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    BaseAdUnit baseAdUnit1 = getBaseAdUnit();
                    if (baseAdUnit1 == null) return;
                    PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {
                            if (pointEntityBase instanceof PointEntitySigmob) {
                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                entitySigmob.setFinal_url(downloadUrl);
                            }
                        }
                    });

                    PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, error.getMessage(), getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {
                            if (pointEntityBase instanceof PointEntitySigmobError) {
                                PointEntitySigmobError entitySigmob = (PointEntitySigmobError) pointEntityBase;
                                entitySigmob.setFinal_url(downloadUrl);
                            }
                        }
                    });
                    SigmobLog.e(error.getMessage());
                }
            }, 1);

            RequestQueue queue = Networking.getSigRequestQueue();
            if (queue == null) {
                PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        if (pointEntityBase instanceof PointEntitySigmob) {
                            PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                            entitySigmob.setFinal_url(downloadUrl);
                        }
                    }
                });

                PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, "request queue is null", getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        if (pointEntityBase instanceof PointEntitySigmobError) {
                            PointEntitySigmobError entitySigmob = (PointEntitySigmobError) pointEntityBase;
                            entitySigmob.setFinal_url(downloadUrl);
                        }
                    }
                });

            } else {
                request.setTag("GDTRequestConvert");
                queue.add(request);
            }

        } else {
            downloadUrl = realClickUrl;
            DownloadAPK.downloadApk(realClickUrl, getBaseAdUnit());
        }


    }


    public static void handleMRAID2Download(final BaseAdUnit adUnit, final String url, boolean parse302) {


        final String tempUrl = adUnit.getMacroCommon().macroProcess(url);

        final String packageName = adUnit.getApkPackageName();
        if (adUnit.getsubInteractionType() == 2 || !TextUtils.isEmpty(packageName)) {
            String productId = adUnit.getProductId();

            if (!TextUtils.isEmpty(packageName)) {
                productId = packageName;
            }

            if (!TextUtils.isEmpty(productId)) {

                Intent shortcutIntent = SDKContext.getApplicationContext().getPackageManager().getLaunchIntentForPackage(productId);
                if (shortcutIntent != null) {
                    try {
                        AdStackManager.setClickAdUnit(adUnit);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                BaseAdUnit clickAdUnit = AdStackManager.getClickAdUnit();
                                if (clickAdUnit != null) {
                                    AdStackManager.setClickAdUnit(null);
                                    PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_PKG, Constants.FAIL, adUnit);
                                }
                            }
                        }, 3000);

                        IntentUtil.launchApplicationIntent(SDKContext.getApplicationContext(), shortcutIntent);
                        return;
                    } catch (Throwable e) {
                        SigmobLog.e("launch fail", e);
                    }
                }
            }
        }


        if (adUnit.getsubInteractionType() == 1 && !parse302) {
            JsonRequest request = new JsonRequest(tempUrl, new JsonRequest.Listener() {
                @Override
                public void onSuccess(JSONObject response) {

                    try {

                        SigmobLog.d("GDTConvertRequest response " + response);
                        int ret = response.getInt("ret");
                        if (ret == 0) {

                            String link = response.getJSONObject("data").getString("dstlink");
                            String clickid = response.getJSONObject("data").getString("clickid");

                            SigMacroCommon rewardVideoMacroCommon = adUnit.getMacroCommon();
                            rewardVideoMacroCommon.addMarcoKey(_CLICKID_, clickid);

                            DownloadAPK.downloadApk(link, adUnit);

                        } else {
                            PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                @Override
                                public void onAddExtra(Object pointEntityBase) {
                                    if (pointEntityBase instanceof PointEntitySigmob) {
                                        PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                        entitySigmob.setFinal_url(tempUrl);
                                    }
                                }
                            });

                            PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, response.toString(), adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                @Override
                                public void onAddExtra(Object pointEntityBase) {
                                    if (pointEntityBase instanceof PointEntitySigmobError) {
                                        PointEntitySigmobError entitySigmob = (PointEntitySigmobError) pointEntityBase;
                                        entitySigmob.setFinal_url(tempUrl);
                                    }
                                }
                            });

                        }
                    } catch (Throwable e) {
                        SigmobLog.e(e.getMessage());
                        PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                            @Override
                            public void onAddExtra(Object pointEntityBase) {
                                if (pointEntityBase instanceof PointEntitySigmob) {
                                    PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                    entitySigmob.setFinal_url(tempUrl);
                                }
                            }
                        });

                        PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, e.getMessage(), adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                            @Override
                            public void onAddExtra(Object pointEntityBase) {
                                if (pointEntityBase instanceof PointEntitySigmobError) {
                                    PointEntitySigmobError entitySigmob = (PointEntitySigmobError) pointEntityBase;
                                    entitySigmob.setFinal_url(tempUrl);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {

                    PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {
                            if (pointEntityBase instanceof PointEntitySigmob) {
                                PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                entitySigmob.setFinal_url(tempUrl);
                            }
                        }
                    });

                    PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, error.getMessage(), adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {
                            if (pointEntityBase instanceof PointEntitySigmobError) {
                                PointEntitySigmobError entitySigmob = (PointEntitySigmobError) pointEntityBase;
                                entitySigmob.setFinal_url(tempUrl);
                            }
                        }
                    });
                    SigmobLog.e(error.getMessage());
                }
            }, 1);

            RequestQueue queue = Networking.getSigRequestQueue();
            if (queue == null) {
                PointEntitySigmobUtils.SigmobTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        if (pointEntityBase instanceof PointEntitySigmob) {
                            PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                            entitySigmob.setFinal_url(tempUrl);
                        }
                    }
                });

                PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, "request queue is null", adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        if (pointEntityBase instanceof PointEntitySigmobError) {
                            PointEntitySigmobError entitySigmob = (PointEntitySigmobError) pointEntityBase;
                            entitySigmob.setFinal_url(tempUrl);
                        }
                    }
                });

            } else {
                request.setTag("GDTRequestConvert");
                queue.add(request);
            }

        } else {

//302 下载可能会导致多个不同文件下载
            DownloadAPK.downloadApk(tempUrl, adUnit);
//
//            if (parse302) {
//                UrlResolutionTask.getResolvedUrl(tempUrl, new UrlResolutionTask.UrlResolutionListener() {
//                    @Override
//                    public void onSuccess(String resolvedUrl) {
//                        DownloadAPK.downloadApk(tempUrl, adUnit);
//
//                    }
//
//                    @Override
//                    public void onFailure(String message, Throwable throwable) {
//                        DownloadAPK.downloadApk(tempUrl, adUnit);
//
//                    }
//                });
//            } else {
//            }
        }


    }

    public void handleLandPageShow(Context context, BaseAdUnit adUnit) {

        PointEntitySigmobUtils.SigmobTracking(PointCategory.LANDING_PAGE_SHOW, null, adUnit);
    }

    public void handleLandPageClose(Context context, BaseAdUnit adUnit) {

        PointEntitySigmobUtils.SigmobTracking(PointCategory.LANDING_PAGE_CLOSE, null, adUnit);
    }

    public void sendCompaionAdsClickTracking(final BaseAdUnit adUnit) {
        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_COMPANION_CLICK);
    }

    public void sendVideoClickTracking(final BaseAdUnit adUnit) {
        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_VIDEO_CLICK);
    }

    public void sendClickTracking(final BaseAdUnit adUnit) {
        SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_CLICK);

    }

    private void clickTrackingHandle(final ClickUIType clickUIType, final String url, final String coordinate, boolean isDeeplink) {


        switch (clickUIType) {
            case COMPANION: {
                getBaseAdUnit().getSessionManager().recordDisplayEvent(ADEvent.AD_COMPANION_CLICK, 0);
            }
            break;
            case VIDEO_CLICK: {
                getBaseAdUnit().getSessionManager().recordDisplayEvent(ADEvent.AD_VIDEO_CLICK, 0);
            }
            break;
            default: {
                getBaseAdUnit().getSessionManager().recordDisplayEvent(ADEvent.AD_CLICK, 0);
            }
            break;
        }
    }

    public void handleUrlAction(final ClickUIType clickUIType, final String coordinate, final boolean isRecord) {
        handleUrlAction(clickUIType, null, coordinate, isRecord, true);
    }

    public void handleUrlFourAction(final ClickUIType clickUIType, final String url, final String coordinate, final boolean isRecord) {
        handleUrlAction(clickUIType, url, coordinate, isRecord, false);
    }

    public void handleUrlAction(final ClickUIType clickUIType, final String url, final String coordinate, final boolean isRecord, final boolean showAppElement) {

        String clickUrl = mClickThroughUrl;
        if (!TextUtils.isEmpty(url)) {
            clickUrl = url;
        }

        getBaseAdUnit().getClickCommon().clickUIType = clickUIType;
        getBaseAdUnit().getClickCommon().clickCoordinate = coordinate;
        getBaseAdUnit().getClickCommon().clickUrl = clickUrl;

        UrlHandler urlHandler = new UrlHandler.Builder().withSupportedUrlActions(UrlAction.IGNORE_ABOUT_SCHEME, UrlAction.OPEN_WITH_BROWSER, UrlAction.MARKET_SCHEME, UrlAction.DOWNLOAD_APK, UrlAction.FOLLOW_PACKAGE_NAME, UrlAction.FOLLOW_DEEP_LINK, UrlAction.MINI_PROGRAM).withResultActions(new UrlHandler.ResultActions() {

            @Override
            public void urlHandlingSucceeded(final String url, UrlAction urlAction) {
                SigmobLog.d("urlHandlingSucceeded: " + urlAction.name() + " url: " + url);
                getBaseAdUnit().getClickCommon().is_final_click = true;

                if (!getBaseAdUnit().getAd().forbiden_parse_landingpage) {
                    PointEntitySigmobUtils.eventTargetURL(getBaseAdUnit(), urlAction.name(), url);
                }

                getBaseAdUnit().getClickCommon().isDeeplink = Constants.FAIL;

                switch (urlAction) {
                    case FOLLOW_PACKAGE_NAME:
                    case IGNORE_ABOUT_SCHEME:
                        break;
                    case MINI_PROGRAM:
                    case FOLLOW_DEEP_LINK: {
                        if (isRecord) {
                            SigmobTrackingRequest.sendTrackings(getBaseAdUnit(), ADEvent.AD_OPEN_DEEPLINK);
                            PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_DEEPLINK, null, getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                @Override
                                public void onAddExtra(Object pointEntityBase) {
                                    if (pointEntityBase instanceof PointEntitySigmob) {
                                        PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                        entitySigmob.setFinal_url(url);
                                        if (getBaseAdUnit().getInteractionType() == InterActionType.FastAppType) {
                                            Map<String, String> options = entitySigmob.getOptions();
                                            options.put("fast_pkg", SDKContext.getFastAppPackageName());

                                        }
                                    }

                                }
                            });
                        }
                        getBaseAdUnit().getClickCommon().isDeeplink = Constants.SUCCESS;
                    }
                    break;
                    case MARKET_SCHEME: {
                        if (isRecord) {
                            BaseAdUnit baseAdUnit = getBaseAdUnit();
                            if (baseAdUnit != null) {
                                AndroidMarket androidMarket = baseAdUnit.getAndroidMarket();
                                if (androidMarket != null) {
                                    String sub = Wire.get(androidMarket.type, 0) == 0 ? "market" : "mimarket";
                                    PointEntitySigmobUtils.SigmobTracking(PointCategory.APK_CLICK, sub, baseAdUnit);
                                    if (!TextUtils.isEmpty(androidMarket.app_package_name)) {
                                        try {
                                            File downloadAPKLogFile = new File(SigmobFileUtil.getDownloadAPKLogPath(), androidMarket.app_package_name + ".log");
                                            FileUtil.writeToCache(baseAdUnit, downloadAPKLogFile.getAbsolutePath());
                                        } catch (Throwable th) {
                                            SigmobLog.e("write ad info with package error " + th.getMessage());
                                        }
                                    }

                                }
                            }


                            PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_MARKET, null, getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                @Override
                                public void onAddExtra(Object pointEntityBase) {
                                    if (pointEntityBase instanceof PointEntitySigmob) {
                                        PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                        entitySigmob.setFinal_url(url);

                                        if (getBaseAdUnit().getAndroidMarket() != null) {
                                            Map<String, String> options = new HashMap<>();
                                            options.put("app_package_name", getBaseAdUnit().getAndroidMarket().app_package_name);
                                            options.put("store_package_name", getBaseAdUnit().getAndroidMarket().appstore_package_name);
                                            entitySigmob.setOptions(options);
                                        }
                                    }

                                }
                            });
                        }
                    }
                    break;
                    case DOWNLOAD_APK:
                        if (showAppElement && showFourElements()) {
                            getBaseAdUnit().getClickCommon().is_final_click = false;
                        } else {
                            handleFourDownload(url);
                        }
                        break;
                    case OPEN_WITH_BROWSER:
                        break;
                    case NOOP:
                        break;
                }

                getBaseAdUnit().setCustomDeeplink(null);
                getBaseAdUnit().setCustomAndroidMarket(null);
                getBaseAdUnit().setCustomLandPageUrl(null);
                if (mOnSigAdClickListener != null) {
                    mOnSigAdClickListener.onAdClick(isRecord, clickUIType);
                }
            }

            @Override
            public void urlHandlingFailed(final String url, UrlAction urlAction) {
                SigmobLog.d("urlHandlingFailed: " + urlAction.name() + " url: " + url);

                getBaseAdUnit().getClickCommon().isDeeplink = Constants.FAIL;

                switch (urlAction) {
                    case FOLLOW_PACKAGE_NAME:
                    case IGNORE_ABOUT_SCHEME:
                        break;
                    case MINI_PROGRAM:
                    case FOLLOW_DEEP_LINK: {
                        Log.d("lance", "打开小程序失败:" + urlAction);
                        if (isRecord) {
                            SigmobTrackingRequest.sendTrackings(getBaseAdUnit(), ADEvent.AD_OPEN_DEEPLINK_FAIL);

                            PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_DEEPLINK_FAILED, null, getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                @Override
                                public void onAddExtra(Object pointEntityBase) {
                                    if (pointEntityBase instanceof PointEntitySigmob) {
                                        PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                        entitySigmob.setFinal_url(url);
                                        if (getBaseAdUnit().getInteractionType() == InterActionType.FastAppType) {
                                            Map<String, String> options = entitySigmob.getOptions();
                                            options.put("fast_pkg", SDKContext.getFastAppPackageName());
                                        }
                                    }

                                }
                            });
                        }
                    }
                    break;
                    case MARKET_SCHEME: {
                        if (isRecord) {

                            PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_MARKET_FAILED, url, getBaseAdUnit(), new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                @Override
                                public void onAddExtra(Object pointEntityBase) {
                                    if (pointEntityBase instanceof PointEntitySigmob) {
                                        PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;
                                        entitySigmob.setFinal_url(url);

                                        Map<String, String> options = new HashMap<>();

                                        options.put("app_package_name", getBaseAdUnit().getAndroidMarket().app_package_name);
                                        options.put("store_package_name", getBaseAdUnit().getAndroidMarket().appstore_package_name);
                                        entitySigmob.setOptions(options);
                                    }
                                }
                            });
                        }
                    }
                    break;
                    case DOWNLOAD_APK:
                        break;
                    case OPEN_WITH_BROWSER:
                        break;
                    case NOOP: {

                        if (TextUtils.isEmpty(url) && !getBaseAdUnit().getAd().forbiden_parse_landingpage) {
                            PointEntitySigmobUtils.eventTargetURL(getBaseAdUnit(), urlAction.name(), url);
                        }
                        getBaseAdUnit().setCustomDeeplink(null);
                        getBaseAdUnit().setCustomAndroidMarket(null);
                        getBaseAdUnit().setCustomLandPageUrl(null);
                        if (mOnSigAdClickListener != null) {
                            mOnSigAdClickListener.onAdClick(isRecord, clickUIType);
                        }
                    }
                    break;
                }
            }
        }).withAdunit(getBaseAdUnit()).withoutSigmobBrowser(getBaseAdUnit().isSkipSigmobBrowser()).withoutResolvedUrl(getBaseAdUnit().getAd().forbiden_parse_landingpage).build();
        urlHandler.handleUrl(SDKContext.getApplicationContext(), url);

    }


    public List<FractionalProgressAdTracker> getUntriggeredTrackersBefore(long mCurrentPosition, long mDuration) {

        if (mDuration > 0 && mCurrentPosition >= 0) {
            float progressFraction = mCurrentPosition / (float) (mDuration);
            List<FractionalProgressAdTracker> untriggeredTrackers = new ArrayList<>();

            final FractionalProgressAdTracker fractionalTest = new FractionalProgressAdTracker(ADEvent.AD_PLAY_QUARTER, progressFraction);
            int fractionalTrackerCount = mFractionalTrackers.size();
            for (int i = 0; i < fractionalTrackerCount; i++) {
                FractionalProgressAdTracker tracker = mFractionalTrackers.get(i);
                if (tracker.compareTo(fractionalTest) > 0) {
                    break;
                }
                if (!tracker.isTracked()) {
                    untriggeredTrackers.add(tracker);
                }
            }

            return untriggeredTrackers;
        } else {
            return Collections.emptyList();
        }
    }

}

