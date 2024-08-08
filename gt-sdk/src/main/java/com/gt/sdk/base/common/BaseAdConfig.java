package com.gt.sdk.base.common;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.Toast;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.network.JsonRequest;
import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.RequestQueue;
import com.czhj.volley.VolleyError;
import com.gt.sdk.GtAdSdk;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.point.GtPointEntityAd;
import com.gt.sdk.base.models.point.PointCategory;
import com.gt.sdk.base.view.DownloadDialog;
import com.gt.sdk.utils.PointEntityUtils;

import org.json.JSONObject;

import java.io.Serializable;
import java.lang.ref.WeakReference;

public class BaseAdConfig implements Serializable {

    private static final long serialVersionUID = 2L;

    private boolean isDialogShow;
    private String downloadUrl;
    private DownloadDialog downloadDialog;
    private WeakReference<Activity> contextWeakReference;
    private DownloadDialog.onPrivacyClickListener mOnPrivacyClickListener;
    private final WeakReference<BaseAdUnit> adUnitWeakReference;

    protected BaseAdConfig(BaseAdUnit adUnit) {
        adUnitWeakReference = new WeakReference<>(adUnit);
    }

    private BaseAdUnit getBaseAdUnit() {
        return adUnitWeakReference.get();
    }

    public void initFourElements(Activity context, final BaseAdUnit adUnit, final DownloadDialog.onPrivacyClickListener onPrivacyClickListener) {
        if (adUnit == null) return;
        if (context != null) {
            contextWeakReference = new WeakReference<>(context);
        }
        mOnPrivacyClickListener = onPrivacyClickListener;
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
                            sessionManager.recordDisplayEvent(getBaseAdUnit(), ADEvent.AD_FOUR_ELEMENTS_CLOSE);
                        }
                        if (mOnPrivacyClickListener != null) {
                            mOnPrivacyClickListener.onCloseClick();
                        }
                    }

                    @Override
                    public void onButtonClick(String clickCoordinate) {//执行点击逻辑
                        if (getBaseAdUnit() == null) return;
                        if (mOnPrivacyClickListener != null) {
                            mOnPrivacyClickListener.onButtonClick(clickCoordinate);
                        }
                        BaseAdUnit baseAdUnit = getBaseAdUnit();
                        openDownload("");
                    }

                    @Override
                    public void onShowSuccess() {
                        if (getBaseAdUnit() == null) return;

                        SessionManager sessionManager = getBaseAdUnit().getSessionManager();

                        if (sessionManager != null) {
                            sessionManager.recordDisplayEvent(getBaseAdUnit(), ADEvent.AD_FOUR_ELEMENTS_SHOW);
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
        if (downloadDialog != null) {
            downloadDialog.dismiss();
            downloadDialog.destroy();
            downloadDialog = null;
            isDialogShow = false;
        }

        if (mOnPrivacyClickListener != null) {
            mOnPrivacyClickListener = null;
        }
    }

    public void openDownload(final String realClickUrl) {

        boolean isDownloading = DownloadAPK.isDownloading(downloadUrl);

        BaseAdUnit baseAdUnit = getBaseAdUnit();
        if (baseAdUnit == null) return;

        if (isDownloading) {
            try {
                Toast.makeText(GtAdSdk.sharedAds().getContext(), "正在下载", Toast.LENGTH_LONG).show();
            } catch (Throwable ignored) {

            }
            return;
        }
        String tempUrl = baseAdUnit.getLanding_page();
        if (!TextUtils.isEmpty(realClickUrl)) {
            tempUrl = realClickUrl;
        }
        tempUrl = baseAdUnit.getMacroCommon().macroProcess(tempUrl);

        if (baseAdUnit.getsubInteractionType() == 1) {

            JsonRequest request = new JsonRequest(tempUrl, new JsonRequest.Listener() {

                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        BaseAdUnit baseAdUnit1 = getBaseAdUnit();
                        if (baseAdUnit1 == null) return;
                        SigmobLog.d("GDTConvertRequest response " + response);
                        int ret = response.getInt("ret");
                        if (ret == 0) {
                            String link = response.getJSONObject("data").getString("dstlink");
                            String clickId = response.getJSONObject("data").getString("clickid");

                            SigMacroCommon macroCommon = getBaseAdUnit().getMacroCommon();
                            macroCommon.addMarcoKey(SigMacroCommon._CLICK_ID_, clickId);
                            downloadUrl = link;
                            DownloadAPK.downloadApk(link, getBaseAdUnit());
                        } else {
                            PointEntityUtils.GtTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, getBaseAdUnit(), new PointEntityUtils.OnPointEntityExtraInfo() {
                                @Override
                                public void onAddExtra(Object pointEntityBase) {
                                    if (pointEntityBase instanceof GtPointEntityAd) {
                                        GtPointEntityAd entityAd = (GtPointEntityAd) pointEntityBase;
                                        entityAd.setFinal_url(downloadUrl);
                                    }
                                }
                            });
                        }
                    } catch (Throwable e) {
                        SigmobLog.e(e.getMessage());
                    }
                }

                @Override
                public void onErrorResponse(VolleyError error) {
                    BaseAdUnit baseAdUnit1 = getBaseAdUnit();
                    if (baseAdUnit1 == null) return;
                    PointEntityUtils.GtTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, getBaseAdUnit(), new PointEntityUtils.OnPointEntityExtraInfo() {
                        @Override
                        public void onAddExtra(Object pointEntityBase) {
                            if (pointEntityBase instanceof GtPointEntityAd) {
                                GtPointEntityAd entityAd = (GtPointEntityAd) pointEntityBase;
                                entityAd.setFinal_url(downloadUrl);
                            }
                        }
                    });
                    SigmobLog.e(error.getMessage());
                }
            }, 1);

            RequestQueue queue = Networking.getSigRequestQueue();
            if (queue == null) {
                PointEntityUtils.GtTracking(PointCategory.DOWNLOAD_START, Constants.FAIL, getBaseAdUnit(), new PointEntityUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        if (pointEntityBase instanceof GtPointEntityAd) {
                            GtPointEntityAd entityAd = (GtPointEntityAd) pointEntityBase;
                            entityAd.setFinal_url(downloadUrl);
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

    private void openDeeplink(final BaseAdUnit adUnit, final String deepUrl, String landUrl) {
        String errorMessage = "";
        try {
            IntentUtil.launchApplicationUrl(GtAdSdk.sharedAds().getContext(), Uri.parse(deepUrl));
        } catch (Exception e) {
            errorMessage = e.getMessage();
        }
        /**
         * deeplink打点
         */
        try {
            if (adUnit != null) {
                String category;
                if (TextUtils.isEmpty(errorMessage)) {//成功
                    category = ADEvent.AD_OPEN_DEEPLINK_SUCCESS;
                } else {//失败
                    category = ADEvent.AD_OPEN_DEEPLINK_FAIL;
                }
            }
            if (!TextUtils.isEmpty(errorMessage)) {
                if (!TextUtils.isEmpty(landUrl)) {
                    openBrowser(adUnit, landUrl, true);
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void openBrowser(final BaseAdUnit adUnit, final String url, final boolean in_app) {
        try {
            if (in_app) {
                adUnit.setUrl(url);
                AdActivity.startActivity(GtAdSdk.sharedAds().getContext(), AdActivity.class, adUnit);
            } else {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                IntentUtil.launchApplicationIntent(GtAdSdk.sharedAds().getContext(), intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleUrlAction(BaseAdUnit adUnit, String url) {
        if (adUnit != null) {
            int interactionType = adUnit.getInteractionType();
            switch (interactionType) {
                case InterActionType.BrowserType://浏览器打开
                    openBrowser(adUnit, url, true);
                    break;
                case InterActionType.DownloadOpenDeepLinkType://浏览器打开
                    openDeeplink(adUnit, url, "");
                    break;
                case InterActionType.DownloadType://应用下载
                    openDownload(url);
                    break;
            }
        }
    }
}

