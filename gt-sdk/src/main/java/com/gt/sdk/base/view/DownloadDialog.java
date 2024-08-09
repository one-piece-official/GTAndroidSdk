package com.gt.sdk.base.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.Md5Util;
import com.czhj.sdk.common.utils.ViewUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.toolbox.StringUtil;
import com.gt.sdk.manager.WindSDKConfig;
import com.gt.sdk.base.common.SigMacroCommon;
import com.gt.sdk.base.common.SigmobRes;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.utils.GtFileUtil;

import org.json.JSONObject;

import java.io.File;
import java.util.Map;


/**
 * Create by lance on 2019/8/14/0014
 */
public class DownloadDialog extends Dialog implements DialogInterface.OnShowListener, DialogInterface.OnDismissListener {

    private onPrivacyClickListener privacyClickListener;
    private Map<String, String> privacyMap;
    private Context mContext;
    private Window window = null;
    private int mHeight;
    private int mWidth;
    private int mScreenWidth;
    private int mScreenHeight;
    private BaseWebView mWebView;
    private BaseAdUnit mAdUnit;
    private boolean isRenderSuccess = false;

    private ImageView closeView;
    private String htmlUrl = WindSDKConfig.getInstance().getAdUrl();
    private File htmlFile;
    private RelativeLayout mLayout;

//    @Override
//    protected void onStart() {
//        super.onStart();
//        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION//isShowNavigationBar
//                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                | View.SYSTEM_UI_FLAG_IMMERSIVE
//                | View.SYSTEM_UI_FLAG_FULLSCREEN;//isShowStatusBar
//        this.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
//    }

    @Override
    public void show() {
        super.show();
        View decorView = getWindow().getDecorView();
        ViewGroup.LayoutParams layoutParams = decorView.getLayoutParams();
        layoutParams.width = mWidth;
        layoutParams.height = mHeight;
        decorView.setLayoutParams(layoutParams);
    }

    public DownloadDialog(Context context, BaseAdUnit adUnit) {

        super(context, SigmobRes.getSig_custom_download_dialog());
        this.mContext = context.getApplicationContext();
        this.mAdUnit = adUnit;
        mWebView = getAdView();
        closeView = getCloseView();
        //计算mAdView应该显示的高度
        mScreenWidth = ClientMetadata.getInstance().getDisplayMetrics().widthPixels;
        mScreenHeight = ClientMetadata.getInstance().getDisplayMetrics().heightPixels;
        mHeight = mScreenHeight;
        mWidth = mScreenWidth;
    }

    public boolean isRenderSuccess() {
        return isRenderSuccess;
    }

    private ImageView getCloseView() {
        closeView = new ImageView(mContext);
        closeView.setImageBitmap(Drawables.CLOSE.getBitmap());
        closeView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        closeView.setImageAlpha((int) (0.5f * 255.0f));
        closeView.setClickable(true);
        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //关闭四要素
                if (privacyClickListener != null) {
                    privacyClickListener.onCloseClick();
                }
            }
        });

        return closeView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLayout = new RelativeLayout(this.getContext());
        mLayout.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

//        if (mWidth<mHeight){
//            int w = Dips.dipsToIntPixels(25, mContext);
//            mLayout.setPaddhing(0, w, 0, 0);
//        }

        mLayout.setBackgroundColor(Color.TRANSPARENT);

        setContentView(mLayout);
        this.setOnShowListener(this);
        this.setOnDismissListener(this);

        SigmobLog.i("DownloadDialog onCreate:" + mWidth + ":" + mHeight);

        if (mWebView != null) {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mLayout.addView(mWebView, params);
        }

        addCloseButton();
        //点击dialog以外的空白处是否隐藏
        setCanceledOnTouchOutside(true);
        //点击返回键取消
        setCancelable(true);
        //设置窗口显示
        windowDeploy();
    }

    private void addCloseButton() {
        if (closeView != null) {
            int width = Dips.dipsToIntPixels(18, mContext);
            final RelativeLayout.LayoutParams iconLayoutParams = new RelativeLayout.LayoutParams(width, width);
            iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//            iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//            iconLayoutParams.setMargins(width, width, 0, 0);
            iconLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            iconLayoutParams.setMargins(0, width / 2, width / 2, 0);
            mLayout.addView(closeView, iconLayoutParams);
        }
    }

    public BaseWebView getAdView() {

        if (mAdUnit != null) {

            privacyMap = mAdUnit.getPrivacyMap();

            String privacyUrl = WindSDKConfig.getInstance().getPrivacyUrl();

            if (!TextUtils.isEmpty(privacyUrl)) {
                String fileName = Md5Util.md5(privacyUrl);
                htmlFile = new File(GtFileUtil.getPrivacyHtmlDir(), fileName + ".html");
            }
        }

        if (htmlFile != null && htmlFile.exists() && privacyMap != null) {
            isRenderSuccess = true;
        } else {
            isRenderSuccess = false;
            return null;
        }

        mWebView = new BaseWebView(mContext);
        mWebView.enablePlugins(true);
        mWebView.setAdUnit(mAdUnit);
        mWebView.addSigAndroidAPK(null);
        mWebView.setBackgroundColor(Color.TRANSPARENT);
        mWebView.setWebChromeClient(new WebChromeClient() {
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
        });

        mWebView.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, final WebResourceRequest request) {
                return super.shouldInterceptRequest(view, request);
            }

            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {
                return super.shouldInterceptRequest(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                try {
                    SigmobLog.i("shouldOverrideUrlLoading:" + url);
                    Uri uri = Uri.parse(url);

                    if (StringUtil.scheme().equalsIgnoreCase(uri.getScheme())) {
                        String host = uri.getHost();
                        if (!TextUtils.isEmpty(host)) {
                            if (("closeFourElements".equals(host))) {
                                //关闭四要素
                                if (privacyClickListener != null) {
                                    privacyClickListener.onCloseClick();
                                }
                            } else if (("buttonClick".equals(host))) {

                                Map<String, String> paramMap = ClientMetadata.getQueryParamMap(uri);
                                String x = paramMap.get("x");
                                if (TextUtils.isEmpty(x)) {
                                    x = "0";
                                }

                                String y = paramMap.get("y");

                                if (TextUtils.isEmpty(y)) {
                                    y = "0";
                                }

                                String clickCoordinate = String.format("%s,%s,%s,%s", x, y, x, y);
                                SigMacroCommon baseMacroCommon = mAdUnit.getMacroCommon();
                                baseMacroCommon.updateClickMarco(x, y, x, y);

                                if (privacyClickListener != null) {
                                    privacyClickListener.onButtonClick(clickCoordinate);
                                }
                            }
                            return true;//啥也不干
                        }
                    }
                } catch (Throwable t) {
                    SigmobLog.e("DownloadDialog:" + t.getMessage());
                }

//                return false;//系统自己处理

                if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                    view.loadUrl(url);
                }
                return true;//都在系统内部打开
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                super.onReceivedHttpError(view, request, errorResponse);
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                SigmobLog.i("onReceivedError:" + error.toString());
                super.onReceivedError(view, request, error);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                SigmobLog.i("onReceivedSslError:" + error.toString());
                super.onReceivedSslError(view, handler, error);
            }
        });

        if (!TextUtils.isEmpty(htmlUrl)) {
            mWebView.loadUrl(htmlUrl);
        } else if (htmlFile != null && htmlFile.exists()) {
            mWebView.addJavascriptInterface(new PrivacyInfoJS(privacyMap), "sigPrivacy");
            mWebView.loadUrl("file://" + htmlFile.getAbsolutePath());
        } else {
            isRenderSuccess = false;
        }

        return mWebView;
    }


//    private String jsData = "(function() {\n" +
//            "    var sigmob = window.sigmob = {};\n" +
//            "\n" +
//            "    var privacyInfo = {};\n" +
//            "\n" +
//            "    sigmob.getPrivacyInfo = function() {\n" +
//            "        return privacyInfo\n" +
//            "    };\n" +
//            "\n" +
//            "    sigmob.closeFourElements = function() {\n" +
//            "        sigPrivacy.closeFourElements()\n" +
//            "    };\n" +
//            "\n" +
//            "    sigmob.buttonClick = function() {\n" +
//            "        sigPrivacy.buttonClick()\n" +
//            "    };\n" +
//            "\n" +
//            "    initPrivacyInfo = function() {\n" +
//            "        privacyInfo = sigPrivacy.getPrivacyInfo()\n" +
//            "    };\n" +
//            "\n" +
//            "    initPrivacyInfo();\n" +
//            "}());";

    private void windowDeploy() {
        window = getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM); //设置窗口显示位置

//            int SigMobDialogWindowAnim = SigmobRes.getSig_dialog_window_anim();
//            if (SigMobDialogWindowAnim != 0) {
//                window.setWindowAnimations(SigMobDialogWindowAnim); //设置窗口弹出动画
//            }
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
//            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
//            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.width = mWidth;
            lp.height = mHeight;
            window.setAttributes(lp);
        }
    }

    public void setOnPrivacyClickListener(onPrivacyClickListener clickListener) {
        this.privacyClickListener = clickListener;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        SigmobLog.i("DownloadDialog  onShow");
        if (privacyClickListener != null) {
            privacyClickListener.onShowSuccess();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        SigmobLog.i("DownloadDialog  onDismiss");
        if (privacyClickListener != null) {
            privacyClickListener.onCloseClick();
        }
    }

    public void destroy() {

        if (mWebView != null) {
            mWebView.destroy();
            mWebView = null;
        }

        if (closeView != null) {
            ViewUtil.removeFromParent(closeView);
            closeView = null;
        }
        if (mContext != null) {
            mContext = null;
        }
        if (privacyClickListener != null) {
            privacyClickListener = null;
        }
    }

    public interface onPrivacyClickListener {
        void onCloseClick();

        void onButtonClick(String clickCoordinate);

        void onShowSuccess();
    }

    static class PrivacyInfoJS extends Object {

        private Map<String, String> map;

        public PrivacyInfoJS(Map<String, String> privacy) {
            map = privacy;
        }

        @JavascriptInterface
        public String getPrivacyInfo() {
            try {
                if (map != null && map.size() > 0) {
                    JSONObject object = new JSONObject();
                    for (Map.Entry<String, String> entry : map.entrySet()) {
//                        Log.d("WindSDK", "key = " + entry.getKey() + ", value = " + entry.getValue());
                        object.put(entry.getKey(), entry.getValue());
                    }
                    return object.toString();
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }

    }
}
