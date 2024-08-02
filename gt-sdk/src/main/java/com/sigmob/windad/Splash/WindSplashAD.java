package com.sigmob.windad.Splash;

import static com.czhj.sdk.common.models.AdStatus.AdStatusClose;
import static com.czhj.sdk.common.models.AdStatus.AdStatusLoading;
import static com.czhj.sdk.common.models.AdStatus.AdStatusNone;
import static com.czhj.sdk.common.models.AdStatus.AdStatusReady;

import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.models.AdStatus;
import com.czhj.sdk.common.utils.AdLifecycleManager;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.WindBaseAd;
import com.sigmob.sdk.base.models.rtb.BiddingResponse;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.splash.SplashAdManager;
import com.sigmob.windad.WindAdError;

import java.util.Map;

public final class WindSplashAD extends WindBaseAd implements WindSplashADListener, AdLifecycleManager.LifecycleListener {

    public AdStatus adStatus = AdStatusNone;
    private WindSplashADListener mSplashADListener;
    private ViewGroup mViewGroup;
    private int mFetchDelay = 5;
    private RelativeLayout splashLY;

    private boolean disableAutoHideAd;
    private boolean isCloseToOut = false;
    private boolean isLoadAndShow;

    private SplashAdManager mSplashAd;
    private Handler mHandler;

    public WindSplashAD(WindSplashAdRequest adRequest, WindSplashADListener adListener) {

        super(adRequest, false);
        mSplashADListener = adListener;

        mHandler = new Handler(Looper.getMainLooper());


        mSplashAd = new SplashAdManager(adRequest, this);
        mFetchDelay = adRequest.getFetchDelay();
        disableAutoHideAd = adRequest.isDisableAutoHideAd();
    }


    public void destroy() {

        SigmobLog.i(String.format("splash ad  %s is Destroy", mRequest != null ? mRequest.getPlacementId() : "null"));

        if (mSplashAd != null) {
            mSplashAd.destroy();
            mHandler.removeCallbacksAndMessages(null);
            if (splashLY != null) {
                splashLY.setVisibility(View.GONE);
                splashLY.removeAllViews();
                splashLY = null;
            }
            if (mViewGroup != null) {
                mViewGroup.removeAllViews();
                mViewGroup = null;
            }
            mSplashADListener = null;
            mViewGroup = null;
        }
    }

    @Override
    public boolean loadAd(String bid_token) {

        super.loadAd(bid_token);
        return load();
    }

    private void initView() {
        if (mViewGroup != null) {
            splashLY = new RelativeLayout(mViewGroup.getContext());
            splashLY.setVisibility(View.INVISIBLE);
            splashLY.setId(ClientMetadata.generateViewId());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            mViewGroup.addView(splashLY, layoutParams);
        }

    }

    private void privateShow() {


        if (mSplashAd == null) {
            onSplashError(WindAdError.ERROR_SIGMOB_SPLASH_NOT_READY, getPlacementId());
            return;
        }

        initView();

        if (splashLY != null) {
            splashLY.setVisibility(View.VISIBLE);
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mSplashAd.showSplashAd(splashLY);
            }
        });

        adStatus = AdStatus.AdStatusPlaying;
    }


    private boolean load() {

        if (!loadAdFilter()) {
            return false;
        }

        AdLifecycleManager.getInstance().addLifecycleListener(this);

        adStatus = AdStatusLoading;
        if (!mSplashAd.isReady()) {
            sendRequestEvent();
        }
        mSplashAd.loadAd(getBid_token(), getBidFloor(), getCurrency(), mFetchDelay, false);
        return true;
    }

    public boolean isReady() {
        return adStatus == AdStatusReady && mSplashAd.isReady();
    }

    public void show(ViewGroup adContainer) {

        if (isLoadAndShow) {
            return;
        }

        if (adStatus != AdStatusReady) {
            onSplashError(WindAdError.ERROR_SIGMOB_SPLASH_NOT_READY, getPlacementId());
            return;
        }
        if (adContainer == null) {
            WindAdError adError = WindAdError.ERROR_SIGMOB_ADCONTAINER_IS_NULL;
            onSplashAdShowError(adError, getPlacementId());
            return;
        }

        mViewGroup = adContainer;


        privateShow();
    }


    private void hideNavigationBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {


            if (mViewGroup != null) {
                Activity activity = ViewUtil.getActivityFromViewTop(mViewGroup);
                if (activity != null) {
                    Window _window = activity.getWindow();
                    WindowManager.LayoutParams params = _window.getAttributes();
                    params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;
                    _window.setAttributes(params);
                    _window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); //隐藏状态栏
                }
            }
        }
    }


    private void onSplashError(final WindAdError error, final String placementId) {
        SigmobLog.e("onSplashError: " + error + " :placementId: " + placementId);
        if (!isCloseToOut) {
            mHandler.removeMessages(0x001);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mSplashADListener != null) {
                        isCloseToOut = true;
                        mSplashADListener.onSplashAdLoadFail(error, placementId);
                    }
                }
            });
            onDestroy();
        }
    }


    private void onDestroy() {

        if (!disableAutoHideAd) {
            if (splashLY != null) {
                splashLY.setVisibility(View.GONE);
                splashLY.removeAllViews();
                splashLY = null;
            }
            if (mViewGroup != null) {
                mViewGroup.removeAllViews();
                mViewGroup = null;
            }
        }
    }

    public boolean loadAd() {
        isLoadAndShow = false;
        super.loadAd();
        return load();
    }

    public void loadAndShow(ViewGroup adContainer) {
        if (adContainer == null) {
            WindAdError adError = WindAdError.ERROR_SIGMOB_ADCONTAINER_IS_NULL;
            onAdLoadFail(adError);
            return;
        }

        super.loadAd();

        this.mViewGroup = adContainer;
        isLoadAndShow = true;
        load();
    }

    public void loadAndShow(String bidToken, ViewGroup adContainer) {
        if (adContainer == null) {
            WindAdError adError = WindAdError.ERROR_SIGMOB_ADCONTAINER_IS_NULL;
            onAdLoadFail(adError);
            return;
        }

        super.loadAd(bidToken);

        this.mViewGroup = adContainer;
        isLoadAndShow = true;
        load();
    }

    @Override
    public void onSplashAdShow(String placementId) {

        if (mSplashADListener != null) {
            mSplashADListener.onSplashAdShow(placementId);
        }
    }

    @Override
    public void onSplashAdLoadSuccess(String placementId) {
        adStatus = AdStatusReady;


        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSplashADListener != null) {
                    mSplashADListener.onSplashAdLoadSuccess(placementId);
                }
                if (isLoadAndShow) {
                    privateShow();
                }
            }
        });


    }

    @Override
    public void onSplashAdLoadFail(WindAdError error, String placementId) {
        adStatus = AdStatusNone;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSplashADListener != null) {
                    mSplashADListener.onSplashAdLoadFail(error, placementId);
                }
            }
        });

    }

    @Override
    public void onSplashAdClick(String placementId) {
        if (mSplashADListener != null) {
            mSplashADListener.onSplashAdClick(placementId);
        }
    }

    @Override
    public void onSplashAdClose(String placementId) {
        adStatus = AdStatusClose;

        if (mSplashADListener != null) {
            mSplashADListener.onSplashAdClose(placementId);
        }
        onDestroy();
    }

    @Override
    public void onSplashAdSkip(String placementId) {

        if (mSplashADListener != null) {
            mSplashADListener.onSplashAdSkip(placementId);
        }
    }

    @Override
    public void onSplashAdShowError(WindAdError error, String placementId) {
        adStatus = AdStatusNone;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSplashADListener != null) {
                    mSplashADListener.onSplashAdShowError(error, placementId);
                }
            }
        });
    }

    @Override
    protected void onAdLoadFail(WindAdError adError) {
        adStatus = AdStatusNone;

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mSplashADListener != null) {
                    mSplashADListener.onSplashAdLoadFail(adError, getPlacementId());
                }
            }
        });

    }

    @Override
    public String getEcpm() {
        if (mSplashAd != null) {
            return mSplashAd.getEcpm();
        }
        return null;
    }

    @Override
    protected Map<String, BiddingResponse> getBidInfo() {
        if (mSplashAd != null) {
            return mSplashAd.getBidInfo();
        }
        return null;
    }

    @Override
    protected void doMacro(String key, String value) {
        if (mSplashAd != null) {
            mSplashAd.doMacro(key, value);
        }
    }

    @Override
    public void onCreate(Activity activity) {

    }

    @Override
    public void onStart(Activity activity) {

    }

    @Override
    public void onPause(Activity activity) {
        if (mSplashAd != null) {
            mSplashAd.onPause(activity);
        }
    }

    @Override
    public void onResume(Activity activity) {
        if (mSplashAd != null) {
            mSplashAd.onResume(activity);
        }
    }

    @Override
    public void onStop(Activity activity) {

    }

    @Override
    public void onDestroy(Activity activity) {

    }
}

