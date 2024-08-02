package com.sigmob.sdk.base.common;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.czhj.sdk.common.Constants;
import com.czhj.sdk.common.utils.AppPackageUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.wire.Wire;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.rtb.AndroidMarket;
import com.sigmob.sdk.base.models.rtb.WXProgramRes;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.videoAd.InterActionType;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public enum UrlAction {


    /* 0 */ IGNORE_ABOUT_SCHEME(false) {
        @Override
        public String getHandleUrL(BaseAdUnit adUnit) {
            return null;
        }

        @Override
        public boolean shouldTryHandlingUri(final Uri uri, int interActionType) {
            return "about".equalsIgnoreCase(uri.getScheme());
        }

        @Override
        protected void performAction(
                final Context context, final Uri uri,
                final UrlHandler urlHandler,
                BaseAdUnit adUnit) {
            SigmobLog.d("Link to about page ignored.");
        }
    },

    /* 1 */  MINI_PROGRAM(false) {
        @Override
        public String getHandleUrL(BaseAdUnit adUnit) {
            if (adUnit != null && adUnit.getWXProgramRes() != null) {
                return adUnit.getWXProgramRes().wx_app_path;
            }
            return null;
        }

        @Override
        public boolean shouldTryHandlingUri(final Uri uri, int interActionType) {
            final String scheme = uri.getScheme();
            return !("HTTP".equalsIgnoreCase(scheme) || "HTTPS".equalsIgnoreCase(scheme)) && interActionType == InterActionType.MiniProgramType;
        }

        @Override
        protected void performAction(
                final Context context, final Uri uri,
                final UrlHandler urlHandler,
                BaseAdUnit adUnit) throws Exception {

            if (adUnit.getInteractionType() != InterActionType.MiniProgramType) {
                throw new Exception("performAction interaction_type is not right with " + adUnit.getInteractionType());
            }

            WXProgramRes wxProgramRes = adUnit.getWXProgramRes();
            if (wxProgramRes != null) {
                try {
//                    IWXAPI api = WXAPIFactory.createWXAPI(this, appId, false);
//
//                    WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
//                    req.userName = "gh_263f360557f8"; // 填小程序原始id
//                    req.path = "pages/technology/technology.html";                  ////拉起小程序页面的可带参路径，不填默认拉起小程序首页，对于小游戏，可以只传入 query 部分，来实现传参效果，如：传入 "?foo=bar"。
//                    req.miniprogramType = WXLaunchMiniProgram.Req.MINIPTOGRAM_TYPE_RELEASE;// 可选打开 开发版，体验版和正式版
//
//                    boolean sendReq = api.sendReq(req);

                    Class factory = Class.forName("com.tencent.mm.opensdk.openapi.WXAPIFactory");
                    Method createWXAPI = factory.getMethod("createWXAPI", Context.class, String.class);
                    createWXAPI.setAccessible(true);
                    Object api = createWXAPI.invoke(factory, context, wxProgramRes.wx_app_id);

                    Class req = Class.forName("com.tencent.mm.opensdk.modelbiz.WXLaunchMiniProgram$Req");
                    Object obj = req.newInstance();

                    Field userName = req.getDeclaredField("userName");
                    userName.setAccessible(true);
                    userName.set(obj, wxProgramRes.wx_app_username);

                    Field path = req.getDeclaredField("path");
                    path.setAccessible(true);
                    path.set(obj, wxProgramRes.wx_app_path);

                    Field miniprogramType = req.getDeclaredField("miniprogramType");
                    miniprogramType.setAccessible(true);
                    miniprogramType.set(obj, 0);

                    Method sendReq = api.getClass().getMethod("sendReq", obj.getClass().getSuperclass());
                    sendReq.setAccessible(true);
                    boolean isSendReq = (boolean) sendReq.invoke(api, obj);
                    Log.d("lance", "openB:isSendReq " + isSendReq);
                    if (!isSendReq) {
                        throw new Exception("get mini_program error: " + uri.toString());
                    }
                } catch (Throwable th) {
                    SigmobLog.e("get mini_program error " + th.getMessage());
                    throw new Exception("get mini_program error: " + uri.toString());
                }
            }
        }
    },

    /* 2 */ FOLLOW_DEEP_LINK(true) {
        @Override
        public String getHandleUrL(BaseAdUnit adUnit) {
            return adUnit == null ? null : adUnit.getDeeplinkUrl();
        }

        @Override
        public boolean shouldTryHandlingUri(final Uri uri, int interActionType) {
            final String scheme = uri.getScheme();
            if (interActionType == InterActionType.FastAppType) {
                return ("HTTP".equalsIgnoreCase(scheme) || "HTTPS".equalsIgnoreCase(scheme) || "HAP".equalsIgnoreCase(scheme));
            } else {
                return !("HTTP".equalsIgnoreCase(scheme) || "HTTPS".equalsIgnoreCase(scheme));

            }

        }

        @Override
        protected void performAction(
                final Context context, final Uri uri,
                final UrlHandler urlHandler,
                BaseAdUnit adUnit)
                throws Exception {

            if (adUnit.getInteractionType() == InterActionType.FastAppType) {
                String fastAppPackageName = SDKContext.getFastAppPackageName();
                IntentUtil.launchApplicationUrl(context, uri, fastAppPackageName);
            } else {
                IntentUtil.launchApplicationUrl(context, uri);
            }
        }
    },

    /* 2 */ FOLLOW_PACKAGE_NAME(true) {
        @Override
        public String getHandleUrL(BaseAdUnit adUnit) {
            return adUnit == null ? null : TextUtils.isEmpty(adUnit.getApkPackageName())?adUnit.getProductId():adUnit.getApkPackageName();
        }

        @Override
        public boolean shouldTryHandlingUri(final Uri uri, int interActionType) {
            return interActionType == InterActionType.DownloadType;
        }

        @Override
        protected void performAction(
                final Context context, final Uri uri,
                final UrlHandler urlHandler,
                final BaseAdUnit adUnit)
                throws Exception {

            if (adUnit.getsubInteractionType() == 2 || !TextUtils.isEmpty(adUnit.getApkPackageName())) {
                Intent shortcutIntent;
                if (!TextUtils.isEmpty(adUnit.getApkPackageName())) {
                    shortcutIntent = context.getPackageManager().
                            getLaunchIntentForPackage(adUnit.getApkPackageName());
                }else {
                    shortcutIntent = context.getPackageManager().
                            getLaunchIntentForPackage(adUnit.getProductId());
                }

                if (shortcutIntent != null) {
                    AdStackManager.setClickAdUnit(adUnit);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            BaseAdUnit clickAdUnit = AdStackManager.getClickAdUnit();
                            if (clickAdUnit != null) {
                                PointEntitySigmobUtils.SigmobTracking(PointCategory.OPEN_PKG, Constants.FAIL, adUnit);
                                AdStackManager.setClickAdUnit(null);
                            }
                        }
                    }, 3000);

                    IntentUtil.launchApplicationIntent(context, shortcutIntent);
                    return;
                }
            }
            throw new Exception("can't launch application for packageName" + adUnit.getProductId());

        }
    },

    /* 3 */ MARKET_SCHEME(false) {
        @Override
        public String getHandleUrL(BaseAdUnit adUnit) {
            if (adUnit != null && adUnit.getAndroidMarket() != null) {
                return adUnit.getAndroidMarket().market_url;
            }
            return null;
        }

        @Override
        public boolean shouldTryHandlingUri(final Uri uri, int interActionType) {
            final String scheme = uri.getScheme();
            return !("HTTP".equalsIgnoreCase(scheme) || "HTTPS".equalsIgnoreCase(scheme));
        }

        @Override
        protected void performAction(
                final Context context, final Uri uri,
                final UrlHandler urlHandler,
                BaseAdUnit adUnit) throws Exception {

            AndroidMarket androidMarket = adUnit.getAndroidMarket();
            if (androidMarket != null) {


                int type = Wire.get(androidMarket.type, 0);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (!TextUtils.isEmpty(androidMarket.appstore_package_name)) {
                    try {
                        PackageInfo packageInfo = AppPackageUtil.getPackageManager(context).getPackageInfo(androidMarket.appstore_package_name, 0);
                        if (packageInfo != null) {
                            intent.setPackage(androidMarket.appstore_package_name);
                        }
                    } catch (Throwable th) {
                        SigmobLog.e("get store package error " + th.getMessage());
                    }
                }
                Uri parse = Uri.parse(androidMarket.market_url);
                intent.setData(parse);
                if (type == 1) {
                    MiMarketManager.DirectMailStatusReceiver directMailStatusReceiver = new MiMarketManager.DirectMailStatusReceiver();
                    if (directMailStatusReceiver != null) {
                        directMailStatusReceiver.register(SDKContext.getApplicationContext(), adUnit);
                    }

                    Activity topActivity = SDKContext.getTopActivity();

                    if (topActivity == null) {
                        topActivity = SDKContext.getLastActivity();
                    }
                    if (topActivity != null) {
                        topActivity.startActivity(intent);
                    } else {
                        context.startActivity(intent);
                    }
                } else {
                    IntentUtil.launchApplicationIntent(context, intent);
                }
            }
        }
    },


    /* 4 */ DOWNLOAD_APK(true) {
        @Override
        public String getHandleUrL(BaseAdUnit adUnit) {
            return adUnit == null ? null : adUnit.getLanding_page();
        }

        @Override
        public boolean shouldTryHandlingUri(final Uri uri, int interActionType) {
            final String scheme = uri.getScheme();
            return ("HTTP".equalsIgnoreCase(scheme) || "HTTPS".equalsIgnoreCase(scheme));
        }

        @Override
        protected void performAction(
                final Context context, final Uri uri,
                final UrlHandler urlHandler,
                BaseAdUnit adUnit) throws Exception {

            if (adUnit.getInteractionType() != InterActionType.DownloadType
                    && adUnit.getInteractionType() != InterActionType.DownloadOpenDeepLinkType) {
                throw new Exception("Could not handle download Scheme url: " + uri);
            }
        }
    },


    /* 5 */ OPEN_WITH_BROWSER(true) {
        @Override
        public String getHandleUrL(BaseAdUnit adUnit) {
            return adUnit == null ? null : adUnit.getLanding_page();
        }

        @Override
        public boolean shouldTryHandlingUri(final Uri uri, int interActionType) {
            final String scheme = uri.getScheme();
            return ("HTTP".equalsIgnoreCase(scheme) || "HTTPS".equalsIgnoreCase(scheme));
        }

        @Override
        protected void performAction(
                final Context context, final Uri uri,
                final UrlHandler urlHandler,
                BaseAdUnit adUnit)
                throws Exception {

            if (adUnit.getInteractionType() == InterActionType.FastAppType) {
                String fastAppPackageName = SDKContext.getFastAppPackageName();
                IntentUtil.launchApplicationUrl(context, uri, fastAppPackageName);
            } else {
                if (urlHandler.shouldSkipShowSigmobBrowser()) {
                    final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                intent.setClassName("com.android.browser","com.android.browser.BrowserActivity");
                    IntentUtil.launchApplicationIntent(context, intent);
                } else {
                    AdStackManager.addAdUnit(adUnit);
                    AdActivity.startActivity(context, AdActivity.class, adUnit.getUuid());
                }
            }

        }
    },


    /* This is essentially an "unspecified" value for UrlAction. */
    NOOP(false) {
        @Override
        public String getHandleUrL(BaseAdUnit adUnit) {
            return null;
        }

        @Override
        public boolean shouldTryHandlingUri(final Uri uri, int interActionType) {
            return false;
        }

        @Override
        protected void performAction(
                final Context context, final Uri uri,
                final UrlHandler urlHandler,
                final BaseAdUnit adUnit) {
        }
    };

    private final boolean mRequiresUserInteraction;

    UrlAction(boolean requiresUserInteraction) {
        mRequiresUserInteraction = requiresUserInteraction;
    }

    public void handleUrl(
            UrlHandler urlHandler,
            final Context context,
            final Uri destinationUri,
            final boolean fromUserInteraction,
            BaseAdUnit adUnit)
            throws Exception {

        SigmobLog.d("Ad event URL: " + destinationUri);
        if (mRequiresUserInteraction && !fromUserInteraction) {
            throw new Exception("Attempted to handle action without user " +
                    "interaction.");
        } else {
            performAction(context, destinationUri, urlHandler, adUnit);
        }
    }

    public abstract String getHandleUrL(BaseAdUnit adUnit);

    public abstract boolean shouldTryHandlingUri(final Uri uri, final int interActionType);

    protected abstract void performAction(
            final Context context, final Uri uri,
            final UrlHandler urlHandler,
            final BaseAdUnit adUnit)
            throws Exception;
}
