package com.gt.sdk.base.common;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
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
import com.gt.sdk.base.activity.BaseAdActivity;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;
import com.gt.sdk.base.view.BaseWebView;
import com.gt.sdk.base.view.Drawables;

import java.io.Serializable;
import java.util.List;


public class LandPageViewController extends BaseAdViewController {

    BaseWebView mLandPageView;
    private int mRequestedOrientation;
    private BaseAdUnit mAdUnit;
    private RelativeLayout mActionBarView;
    private TextView mTitleView;
    private ImageView mCloseText;

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
            getActivity().requestWindowFeature(Window.FEATURE_ACTION_BAR);
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            broadcastAction(IntentActions.ACTION_LAND_PAGE_SHOW_FAIL);
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

                mLandPageView.setWebViewClient(new WebViewClient() {

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        try {
                            Context context = view.getContext();
                            Uri uri = Uri.parse(url);
                            String scheme = uri.getScheme();

                            if (!TextUtils.isEmpty(scheme) && !scheme.equals("http") && !scheme.equals("https")) {
                                IntentUtil.launchApplicationUrl(context, Uri.parse(url));
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
                        switch (level) {
                            case ERROR: {
                                SigmobLog.e("onConsoleMessage " + consoleMessage.message());
                            }
                            break;
                        }
                        return false;
                    }

                    @Override
                    public void onReceivedTitle(WebView view, String title) {
                        // 检查标题是否为空或等于URL
                        if (title == null || title.isEmpty() || title.startsWith("http") || title.length() > 10) {
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

        mLandPageView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                DownloadAPK.downloadApk(url, mAdUnit);
                SigmobLog.d("onDownloadStart() called with: url = [" + url + "], userAgent = [" + userAgent + "], contentDisposition = [" + contentDisposition + "], mimetype = [" + mimetype + "], contentLength = [" + contentLength + "]");
            }
        });
    }

    private void createActionBarCustomView() {

        if (mActionBarView == null) {
            final RelativeLayout.LayoutParams adViewLayout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
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

        String landPage = mAdUnit.getLanding_page();
        String url = mAdUnit.getMacroCommon().macroProcess(landPage);
        if (!TextUtils.isEmpty(mAdUnit.getLandUrl())) {
            mLandPageView.loadUrl(mAdUnit.getLandUrl());
        } else {
            mLandPageView.loadUrl(url);
        }
        broadcastAction(IntentActions.ACTION_LAND_PAGE_SHOW);
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
        broadcastAction(IntentActions.ACTION_LAND_PAGE_DISMISS);

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
