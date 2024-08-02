package com.sigmob.sdk.videoAd;

import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.views.CreativeResource;

import java.io.Serializable;


public class VideoCompanionAdConfig implements Serializable {
    private static final long serialVersionUID = 0L;

    private final int mWidth;
    private final int mHeight;
    private final CreativeResource mCreativeResource;
    private final int mActionType;
    private String downloadUrl;
    private VideoCompanionAdClickListenner companionAdClickListenner;
    private String mClickThroughUrl;
    private String mDeepLinkUrl;
    private int duration;
    private boolean isValidClick = false;
    private BaseVideoConfig mVideoConfig;

    VideoCompanionAdConfig(
            int width,
            int height,
            int actionType,
            String clickThroughUrl,
            String deepLinkUrl,
            CreativeResource creativeResource) {

        mWidth = width;
        mHeight = height;
        mActionType = actionType;
        mDeepLinkUrl = deepLinkUrl;
        mClickThroughUrl = clickThroughUrl;
        mCreativeResource = creativeResource;
    }

    public CreativeResource getCreativeResource() {
        return mCreativeResource;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDeepLinkUrl() {
        return mDeepLinkUrl;
    }

    public void setDeepLinkUrl(String mDeepLinkUrl) {
        this.mDeepLinkUrl = mDeepLinkUrl;
    }

    public void setCompanionAdClickListenner(VideoCompanionAdClickListenner companionAdClickListenner) {
        this.companionAdClickListenner = companionAdClickListenner;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setVideoConfig(BaseVideoConfig videoConfig) {
        mVideoConfig = videoConfig;
    }




//
//    private void downloadAPK(final Context context, final BaseAdUnit adUnit, String realClickUrl, String coordinate, final ClickUIType clickUIType) {
//
//
//        clickTrackingHandle(clickUIType, adUnit, realClickUrl, coordinate);
//
//        mVideoConfig.handleDownload(adUnit, realClickUrl, clickUIType, coordinate, false);
//        downloadUrl = realClickUrl;
//
//        if (companionAdClickListenner != null) {
//            companionAdClickListenner.onHandleClickSuccess();
//        }
//    }


    private void handleUrlAction(final BaseAdUnit adUnit, final String realClickUrl, final String coordinate, final ClickUIType clickUIType) {




//
//        UrlHandler urlHandler = new UrlHandler.Builder()
//                .withSupportedUrlActions(
//                        UrlAction.IGNORE_ABOUT_SCHEME,
//                        UrlAction.OPEN_WITH_BROWSER,
//                        UrlAction.FOLLOW_DEEP_LINK)
//                .withResultActions(new UrlHandler.ResultActions() {
//
//                    @Override
//                    public void urlHandlingSucceeded(String url,
//                                                     UrlAction urlAction) {
//                        SigmobLog.d("urlHandlingFailed: " + urlAction.name() + " url: " + url);
//
//                        if (!adUnit.getAd().forbiden_parse_landingpage) {
//                            PointEntitySigmobUtils.eventTargetURL(adUnit,urlAction.name(), url);
//                        }
//
//
//                        switch (urlAction){
//
//                        }
//
//                        if (UrlAction.FOLLOW_DEEP_LINK == urlAction) {
//
//                            SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK);
//
//                            PointEntitySigmobUtils.eventRecord(clickUIType, PointCategory.CLICK, adUnit, Constants.SUCCESS, url, coordinate, duration);
//                        } else {
//                            if (!TextUtils.isEmpty(mDeepLinkUrl)) {
//                                SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK_FAIL);
//                            }
//                            PointEntitySigmobUtils.eventRecord(clickUIType, PointCategory.CLICK, adUnit, Constants.FAIL, url, coordinate, duration);
//
//                        }
//
//                        if (companionAdClickListenner != null) {
//                            companionAdClickListenner.onHandleClickSuccess();
//                        }
//
//                    }
//
//                    @Override
//                    public void urlHandlingFailed(String url,
//                                                  UrlAction urlAction) {
//                        SigmobLog.d("urlHandlingFailed: " + urlAction.name() + " url: " + url);
//
//                        if (urlAction == UrlAction.NOOP){
//                            return;
//                        }
//
//                        if (!adUnit.getAd().forbiden_parse_landingpage) {
//                            PointEntitySigmobUtils.eventTargetURL(adUnit,urlAction.name(),url);
//
//                        }
//
//                        if (UrlAction.FOLLOW_DEEP_LINK == urlAction) {
//
//                            SigmobTrackingRequest.sendTrackings(adUnit, ADEvent.AD_OPEN_DEEPLINK_FAIL);
//                            PointEntitySigmobUtils.eventRecord(clickUIType, PointCategory.CLICK, adUnit, Constants.FAIL, url, coordinate, duration);
//
//                            deeplinkFailbackHandle(adUnit, clickUIType, coordinate);
//                        }
//                    }
//                })
//                .withoutSigmobBrowser(adUnit.isSkipSigmobBrowser())
//                .withAdunit(adUnit)
//                .withoutResolvedUrl(adUnit.getAd().forbiden_parse_landingpage)
//                .build();
//        urlHandler.handleUrl(SDKContext.getApplicationContext());

    }

//    private void deeplinkFailbackHandle(final BaseAdUnit adUnit, ClickUIType clickUIType, String coordinate) {
//        if (TextUtils.isEmpty(mClickThroughUrl)) {
//
//            if (companionAdClickListenner != null) {
//                companionAdClickListenner.onHandleClickFailed();
//            }
//            return;
//        }
//
//        final String jumpUrl = adUnit.getMacroCommon().macroProcess(mClickThroughUrl);
//        PointEntitySigmobUtils.eventRecord(clickUIType, PointCategory.CLICK, adUnit, Constants.FAIL, jumpUrl, coordinate, duration);
//
//        if (mActionType == InterActionType.BrowserType) {
//            UrlResolutionTask.getResolvedUrl(jumpUrl, new UrlResolutionTask.UrlResolutionListener() {
//                @Override
//                public void onSuccess(String resolvedUrl) {
//
//                    if (!adUnit.getAd().forbiden_parse_landingpage) {
//                        PointEntitySigmobUtils.eventTargetURL(adUnit,UrlAction.OPEN_WITH_BROWSER.name(), resolvedUrl);
//
//                    }
//                    try {
//                        IntentUtil.launchApplicationUrl(SDKContext.getApplicationContext(), Uri.parse(resolvedUrl));
//                        if (companionAdClickListenner != null) {
//                            companionAdClickListenner.onHandleClickSuccess();
//                        }
//
//                    } catch (Throwable e) {
//                        SigmobLog.e(e.getMessage());
//                        if (companionAdClickListenner != null) {
//                            companionAdClickListenner.onHandleClickFailed();
//                        }
//                    }
//
//
//                }
//
//                @Override
//                public void onFailure(String message, Throwable throwable) {
//                    if (!adUnit.getAd().forbiden_parse_landingpage) {
//                        PointEntitySigmobUtils.eventTargetURL(adUnit,UrlAction.OPEN_WITH_BROWSER.name(), jumpUrl);
//
//                    }
//                    try {
//                        IntentUtil.launchApplicationUrl(SDKContext.getApplicationContext(), Uri.parse(jumpUrl));
//                        if (companionAdClickListenner != null) {
//                            companionAdClickListenner.onHandleClickSuccess();
//                        }
//
//                    } catch (Exception e) {
//                        SigmobLog.e(e.getMessage());
//                        if (companionAdClickListenner != null) {
//                            companionAdClickListenner.onHandleClickFailed();
//                        }
//                    }
//
//                }
//            });
//        } else {
//            mVideoConfig.handleDownload( adUnit, jumpUrl, clickUIType, coordinate, false);
//            if (companionAdClickListenner != null) {
//                companionAdClickListenner.onHandleClickSuccess();
//            }
//        }
//    }


//
//    /**
//     * Called when the companion ad is clicked. Handles forwarding the user to the specified click
//     * through uri.
//     */
//    public void handleClick(final Context context,
//                            final String webViewClickThroughUrl, final ClickUIType clickUIType, final BaseAdUnit adUnit, final String coordinate) {
//        Preconditions.NoThrow.checkNotNull(context);
//
//
//        handleAction(context, mClickThroughUrl, clickUIType, adUnit, coordinate);
//    }


//    public void handleDownload(final Context context, final BaseAdUnit adUnit, String realClickUrl, final String coordinate, final ClickUIType clickUIType) {
//
//        boolean isDownloading = DownloadAPK.isDownloading( downloadUrl);
//
//        if (isDownloading) {
//            try {
//                SigToast.makeText(context, "当前正在努力下载，请稍等", Toast.LENGTH_LONG).show();
//            } catch (Throwable throwable) {
//
//            }
//            return;
//        }
//        realClickUrl = adUnit.getMacroCommon().macroProcess(realClickUrl);
//
//        if (adUnit.subInteractionType() == 1) {
//
//            JsonRequest request = new JsonRequest(realClickUrl, new JsonRequest.Listener() {
//                @Override
//                public void onSuccess(JSONObject response) {
//
//                    try {
//
//                        SigmobLog.d("GDTConvertRequest response " + response);
//                        int ret = response.getInt("ret");
//                        if (ret == 0) {
//
//                            String link = response.getJSONObject("data").getString("dstlink");
//                            String clickid = response.getJSONObject("data").getString("clickid");
//
//                            BaseMacroCommon rewardVideoMacroCommon = adUnit.getMacroCommon();
//                            if (rewardVideoMacroCommon instanceof SigMacroCommon) {
//                                ((SigMacroCommon) rewardVideoMacroCommon).addMarcoKey(_CLICKID_, clickid);
//                            }
//                            downloadAPK(context, adUnit, link, coordinate, clickUIType);
//                        } else {
//                            PointEntitySigmobUtils.eventRecord(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit);
//                            PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, response.toString(), adUnit);
//
//                        }
//                    } catch (Throwable e) {
//                        SigmobLog.e(e.getMessage());
//                        PointEntitySigmobUtils.eventRecord(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit);
//                        PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, e.getMessage(), adUnit);
//                    }
//                }
//
//                @Override
//                public void onErrorResponse(VolleyError error) {
//                    PointEntitySigmobUtils.eventRecord(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit);
//                    PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, error.getMessage(), adUnit);
//                    SigmobLog.e(error.getMessage());
//                }
//            }, 1);
//
//            RequestQueue queue = Networking.getRequestQueue();
//            if (queue == null) {
//                PointEntitySigmobUtils.eventRecord(PointCategory.DOWNLOAD_START, Constants.FAIL, adUnit);
//                PointEntitySigmobUtils.SigmobError(PointCategory.DOWNLOAD_START, 0, "request queue is null", adUnit);
//            } else {
//                request.setTag("GDTRequestConvert");
//                queue.add(request);
//            }
//
//        } else {
//            downloadAPK(context, adUnit, realClickUrl, coordinate, clickUIType);
//        }
//    }


//
//    public void handleAction(final Context context,
//                             String realClickUrl,
//                             final ClickUIType clickUIType,
//                             final BaseAdUnit adUnit,
//                             final String coordinate) {
//
//        isValidClick = true;
//
//        if (!TextUtils.isEmpty(mDeepLinkUrl)) {
//            realClickUrl = mDeepLinkUrl;
//            realClickUrl = adUnit.getMacroCommon().macroProcess(realClickUrl);
//
//
//            handleUrlAction(adUnit,realClickUrl, coordinate, clickUIType);
//
//        } else if (!TextUtils.isEmpty(realClickUrl)) {
//
//            if (mActionType == DownloadType || mActionType == DownloadOpenDeepLinkType) {
//                clickTrackingHandle(clickUIType, adUnit, realClickUrl, coordinate);
//                if (companionAdClickListenner != null) {
//                    companionAdClickListenner.onHandleClickSuccess();
//                }
//                mVideoConfig.handleDownload( adUnit, realClickUrl, null, coordinate, false);
//            } else if (mActionType == BrowserType) {
//                handleUrlAction(adUnit, realClickUrl, coordinate, clickUIType);
//            } else {
//                SigmobLog.e("not support interActionType");
//                if (companionAdClickListenner != null) {
//                    companionAdClickListenner.onHandleClickFailed();
//                }
//            }
//
//        } else {
//            SigmobLog.e("landing page is null");
//            if (companionAdClickListenner != null) {
//                companionAdClickListenner.onHandleClickFailed();
//            }
//        }
//
//    }

    public interface VideoCompanionAdClickListenner {
        void onHandleClickSuccess();

        void onHandleClickFailed();
    }
}
