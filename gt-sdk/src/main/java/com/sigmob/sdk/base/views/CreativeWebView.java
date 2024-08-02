package com.sigmob.sdk.base.views;

import static com.sigmob.sdk.base.WindConstants.ENABLEFILE;
import static com.sigmob.sdk.base.WindConstants.SIGMOBHTML;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.network.Networking;
import com.czhj.sdk.common.utils.Md5Util;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.common.utils.ReflectionUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.SigmobRes;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.utils.SigmobFileUtil;

import java.io.File;

public class CreativeWebView extends BaseWebView {
    private final AdLogoView mlogoView;



    private static float MIN_FONT_SIZE_MDPI = 30 / 3.0f;
    private static final float GLOBAL_SCALE = 1.0f;
    public interface WebViewClickListener {
        void onWebViewClick(MotionEvent downEvent, MotionEvent upEvent);
    }

    private WebViewClickListener mWebViewClickListener;

    private CreativeWebView(Context context, String adLogoUrl, boolean showAdLogo, boolean invisible_ad_label) {
        super(context);

        disableScrollingAndZoom();

        try {


            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(getSettings(), new String( Base64.decode(WindConstants.ENABLEJS, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class,true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            

            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(getSettings(), new String( Base64.decode(ENABLEFILE, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class,true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        enablePlugins(true);

        setBackgroundColor(Color.TRANSPARENT);

        if (showAdLogo) {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);


            mlogoView = new AdLogoView(getContext().getApplicationContext(), 1);
            mlogoView.showAdLogo(adLogoUrl);
            mlogoView.showAdText(SigmobRes.getAd());

            addView(mlogoView, layoutParams);

        } else {
            mlogoView = null;
        }

        setOnTouchListener(new WebViewOnTouchListener());
    }

    void loadData(String data) {
        String fileName = Md5Util.md5(data);
        File file = SigmobFileUtil.dataToFile(data, fileName + ".html");
        if (file != null && !TextUtils.isEmpty(file.getAbsolutePath())) {
            loadUrl(SIGMOBHTML+"://" + file.getAbsolutePath());
        } else {
        loadDataWithBaseURL(Networking.getBaseUrlScheme() + "://" + "localhost" + "/",
                data, "text/html", "utf-8", null);
        }

    }

    public void setLogoClickListener(OnClickListener listener) {
        if(mlogoView != null){
            mlogoView.setOnClickListener(listener);
        }
    }

    public void setWebViewClickListener(WebViewClickListener webViewClickListener) {
        mWebViewClickListener = webViewClickListener;
    }

    private void disableScrollingAndZoom() {
        setHorizontalScrollBarEnabled(false);
        setHorizontalScrollbarOverlay(false);
        setVerticalScrollBarEnabled(false);
        setVerticalScrollbarOverlay(false);
        getSettings().setSupportZoom(false);
        setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
    }

    /**
     * Creates and populates a webview.
     *
     * @param context the context.
     * @return a fully populated webview
     */

    public static CreativeWebView createView(final Context context, final String adLogoUrl, boolean showAdLogo, boolean invisible_ad_label) {
        Preconditions.NoThrow.checkNotNull(context);

        CreativeWebView webView = new CreativeWebView(context, adLogoUrl, showAdLogo, invisible_ad_label);

        return webView;
    }

    /**
     * Custom on touch listener to easily detect clicks on the entire WebView.
     */
    class WebViewOnTouchListener implements View.OnTouchListener {
        private boolean mClickStarted;
        MotionEvent downEvent;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mClickStarted = true;
                    downEvent = MotionEvent.obtain(event);
                    break;
                case MotionEvent.ACTION_UP:
                    if (!mClickStarted) {
                        return false;
                    }
                    mClickStarted = false;
                    if (mWebViewClickListener != null) {
                        mWebViewClickListener.onWebViewClick(downEvent, event);
                    }
            }

            return false;
        }
    }

    @Override
    public void destroy() {

        SigmobLog.d("CreativeWebView destroy() callSigmobLog.d( \"CreativeWebView destroy() called\");ed");
        super.destroy();
        removeAllViews();


    }
}
