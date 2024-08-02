package com.sigmob.sdk.base.common;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.exceptions.IntentNotResolvableException;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.toolbox.StringUtil;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.SigmobRes;
import com.sigmob.sdk.base.SigmobWebViewClient;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.IntentActions;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.views.BaseWebView;
import com.sigmob.sdk.base.views.Drawables;
import com.sigmob.sdk.videoAd.BaseAdActivity;

import java.io.Serializable;
import java.util.List;


public class LandPageViewController extends BaseAdViewController {

    BaseWebView mLandPageView;
    private int mRequestedOrientation;
    private BaseAdUnit mAdUnit;
    private RelativeLayout mActionBarView;
    private TextView mTitleView;
    private ImageView mCloseText;
    private String coordinate;

    public LandPageViewController(Activity activity, BaseAdUnit baseAdUnit, Bundle intentExtras, Bundle savedInstanceState, String mBroadcastIdentifier, BaseAdViewControllerListener baseAdViewControllerListener) {
        super(activity, mBroadcastIdentifier, baseAdViewControllerListener);
//        mAdUnit = AdStackManager.getPlayAdUnit();
        mAdUnit = baseAdUnit;

        if (intentExtras != null) {
            Serializable LandPage = intentExtras.getSerializable(BaseAdActivity.LAND_PAGE_URL);
            if (LandPage instanceof BaseAdUnit) {
                mAdUnit = (BaseAdUnit) LandPage;
            }
        }

        if (mAdUnit != null) {
//            int display_orientation = mAdUnit.getAd().display_orientation;
//            switch (display_orientation) {
//                case 1: {
//                    mRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
//                }
//                break;
//                case 2: {
//                    mRequestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
//                }
//                break;
//                default: {
//                    mRequestedOrientation = intentExtras.getInt(WindConstants.REQUESTED_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_BEHIND);
//                }
//            }
//            if (mAdUnit.getAd_type() != AdFormat.UNIFIED_NATIVE) {
//                getBaseAdViewControllerListener().onSetRequestedOrientation(mRequestedOrientation);
//            }
            getActivity().requestWindowFeature(Window.FEATURE_ACTION_BAR);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            broadcastAction(IntentActions.ACTION_INTERSTITIAL_FAIL);

            getBaseAdViewControllerListener().onFinish();
        }

        // Solid black background
//        getLayout().setBackgroundColor(Color.BLACK);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    private void createLandPageWebView() {

        if (mLandPageView == null) {
            try {
                mLandPageView = new BaseWebView(getActivity());
                mLandPageView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

                mLandPageView.setAdUnit(mAdUnit);
                mLandPageView.setWebViewClient(new SigmobWebViewClient() {

                    @Override
                    public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            PointEntitySigmobUtils.SigmobError("h5_error","landpage",errorResponse.getStatusCode(),request.getUrl().toString(),null,null,mAdUnit,null);
                        }
                    }


                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        PointEntitySigmobUtils.SigmobError("h5_error","landpage",errorCode,failingUrl+" error:" +description,null,null,mAdUnit,null);

                    }

                    @Override
                    public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                        SigmobLog.i("onReceivedError:" + error.toString());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                PointEntitySigmobUtils.SigmobError("h5_error","landpage",0,request.getUrl()+" error:" +error.getDescription(),null,null,mAdUnit,null);
                            }
                        }
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        try {
                            Context context = view.getContext();
                            Uri uri = Uri.parse(url);
                            String scheme = uri.getScheme();

                            if (StringUtil.scheme().equalsIgnoreCase(scheme)) {
                                String host = uri.getHost();
                                if (!TextUtils.isEmpty(host) && ("track".equals(host) || "active".equals(host))) {
                                    String data = uri.getQueryParameter("data");
                                    String event = uri.getQueryParameter("event");
                                    if (!TextUtils.isEmpty(event) && !TextUtils.isEmpty(data)) {
                                        final String urlValue = new String(Base64.decode(data, Base64.DEFAULT), "utf-8");
                                        //上报track
                                        PointEntitySigmobUtils.SigmobTracking(host, event, mAdUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                                            @Override
                                            public void onAddExtra(Object pointEntityBase) {
                                                if (pointEntityBase instanceof PointEntitySigmob) {
                                                    PointEntitySigmob entitySigMob = (PointEntitySigmob) pointEntityBase;
                                                    entitySigMob.setUrl(urlValue);
                                                }
                                            }
                                        });
                                    }
                                    return true;
                                }
                            }

                            if (!TextUtils.isEmpty(scheme) && !scheme.equals("http") && !scheme.equals("https")) {

                                List<String> scheme_white_list = mAdUnit.getAdSetting() != null ? mAdUnit.getAdSetting().scheme_white_list : null;
                                if (scheme_white_list != null && scheme_white_list.size() > 0) {
                                    for (int i = 0; i < scheme_white_list.size(); i++) {
                                        String sc = scheme_white_list.get(i);
                                        if (url.startsWith(sc) || sc.equals("*")) {//通配符
                                            IntentUtil.launchApplicationUrl(context, Uri.parse(url));
                                            PointEntitySigmobUtils.eventRecord(ClickUIType.LAND_PAGE, PointCategory.CLICK, mAdUnit, Constants.SUCCESS, url, coordinate);
                                            return true;
                                        }
                                    }
                                }

                                return true;
                            } else {
                                SigmobLog.i("load Url: " + url);
                                view.loadUrl(url);
                            }
                        } catch (IntentNotResolvableException e) {
                            SigmobLog.e(e.getMessage());
                        } catch (Throwable t) {
                            SigmobLog.e(t.getMessage());
                        }
                        return true;
                    }

                });
                mLandPageView.setWebChromeClient(new WebChromeClient() {

                    @Override
                    public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                        ConsoleMessage.MessageLevel level = consoleMessage.messageLevel();
                        switch (level){
                            case ERROR:{
                                SigmobLog.e("onConsoleMessage "+ consoleMessage.message());
                                PointEntitySigmobUtils.SigmobError("h5_error","landpage",0,consoleMessage.message(),null,null,mAdUnit,null);
                            }break;
                        }
                        return false;
                    }

                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        // 检查标题是否为空或等于URL
                        if (title == null || title.isEmpty() || title.startsWith("http") || title.length()>10) {
                            // 使用默认标题或其他逻辑
                            if (mTitleView != null) {
                                mTitleView.setVisibility(View.GONE);
                            }
                            return;
                        }


                        if (mTitleView != null) {
                            mTitleView.setVisibility(View.VISIBLE);
                            mTitleView.setText(title);
                        }
                    }
                });
            } catch (Throwable throwable) {
                SigmobLog.e(throwable.getMessage());
                mBaseAdViewControllerListener.onFinish();
            }
        }

        final LinearLayout.LayoutParams adViewLayout = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        LinearLayout linearLayout = new LinearLayout(getActivity());

        linearLayout.addView(mLandPageView, 0, adViewLayout);

        mBaseAdViewControllerListener.onSetContentView(linearLayout);

        if (mAdUnit.getAd() != null && mAdUnit.getAd().ad_setting != null) {
            if (!mAdUnit.getAd().ad_setting.disable_download_listener) {
                mLandPageView.setDownloadListener(new DownloadListener() {
                    @Override
                    public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                        DownloadAPK.downloadApk(url, mAdUnit);
                        PointEntitySigmobUtils.eventRecord(ClickUIType.LAND_PAGE, PointCategory.CLICK, mAdUnit, Constants.FAIL, url, coordinate);

                        SigmobLog.d("onDownloadStart() called with: url = [" + url + "], userAgent = [" + userAgent + "], contentDisposition = [" + contentDisposition + "], mimetype = [" + mimetype + "], contentLength = [" + contentLength + "]");
                    }
                });
            }
        }

        mLandPageView.setOnTouchListener(new View.OnTouchListener() {

            MotionEvent downEvent = null;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                SigmobLog.d(event.toString());
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downEvent = MotionEvent.obtain(event);

                } else if (event.getAction() == MotionEvent.ACTION_UP) {

                    if (downEvent == null) {
                        coordinate = SigMacroCommon.getCoordinate(event, event, true);
                    } else {
                        coordinate = SigMacroCommon.getCoordinate(downEvent, event, true);
                    }
                }

                return false;
            }
        });

    }

    private void eventResult(final String sub_category, String cate, BaseAdUnit adUnit, final String coordinate) {

        PointEntitySigmobUtils.SigmobTracking(cate, sub_category, adUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
            @Override
            public void onAddExtra(Object pointEntityBase) {
                if (pointEntityBase instanceof PointEntitySigmob) {
                    ((PointEntitySigmob) pointEntityBase).setCoordinate(coordinate);
                }

            }
        });

    }


    private void createActionBarCustomView() {

        if (mActionBarView == null) {
            final RelativeLayout.LayoutParams adViewLayout = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            adViewLayout.addRule(RelativeLayout.CENTER_IN_PARENT);


            int width = Dips.dipsToIntPixels(20, mContext);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, width);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.setMargins(width / 2, width / 2, 0, 0);

            mCloseText = new ImageView(mContext);
            mCloseText.setImageBitmap(Drawables.BACK.getBitmap());
            mCloseText.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mCloseText.setClickable(true);
            mCloseText.setId(ClientMetadata.generateViewId());
            mCloseText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getBaseAdViewControllerListener().onFinish();
                }
            });

            mActionBarView = new RelativeLayout(getContext());
            mActionBarView.setLayoutParams(adViewLayout);
            mActionBarView.setBackgroundColor(Color.WHITE);
            mActionBarView.addView(mCloseText, layoutParams);

            mTitleView = new TextView(mContext);
            mTitleView.setTextColor(Color.BLACK);
            mTitleView.setTextSize(18);
            mTitleView.setVisibility(View.GONE);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            mActionBarView.addView(mTitleView, params);
        }
    }

    private void setCustomActionBar() {
        ActionBar.LayoutParams lp = new ActionBar.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            createActionBarCustomView();
            actionBar.setCustomView(mActionBarView, lp);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
            actionBar.setDisplayShowCustomEnabled(true);
            actionBar.setDisplayShowHomeEnabled(false);
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    @Override
    public void onCreate() {

        int style = SigmobRes.getSig_land_theme();
        if (style > 0) {
            getActivity().getTheme().applyStyle(style, true);

        }

        setCustomActionBar();

        createLandPageWebView();

        MaterialMeta material = mAdUnit.getMaterial();
        String landPage = material.landing_page;
        String url = mAdUnit.getMacroCommon().macroProcess(landPage);
        if (!TextUtils.isEmpty(mAdUnit.getLandUrl())) {
            mLandPageView.loadUrl(mAdUnit.getLandUrl());
        } else {
            mLandPageView.loadUrl(url);
        }

        broadcastAction(IntentActions.ACTION_LANDPAGE_SHOW);

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

        if (mLandPageView != null) {
            mLandPageView.resumeTimers();
        }
    }

    @Override
    public void onDestroy() {
        broadcastAction(IntentActions.ACTION_LANDPAGE_DISMISS);

        if (mCloseText != null) {
            mCloseText.setOnClickListener(null);
            mCloseText = null;

        }
        if (mLandPageView != null) {
            mLandPageView.destroy();
            mLandPageView = null;
        }

        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

    }


    @Override
    public boolean backButtonEnabled() {
        if (mLandPageView.canGoBack()) {
            mLandPageView.goBack();
            return false;
        }
        return true;
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onBackPressed() {

    }
}
