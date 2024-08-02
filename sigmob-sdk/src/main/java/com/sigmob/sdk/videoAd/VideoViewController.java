package com.sigmob.sdk.videoAd;


import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.sigmob.sdk.base.common.PointEntitySigmobUtils.SigmobError;
import static com.sigmob.sdk.base.common.PointEntitySigmobUtils.touchEventRecord;
import static com.sigmob.sdk.base.models.IntentActions.ACTION_INTERSTITIAL_VOPEN;
import static com.sigmob.sdk.base.views.DrawableConstants.BlueColor;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.track.BaseMacroCommon;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.Preconditions;
import com.czhj.sdk.common.utils.ResourceUtil;
import com.czhj.sdk.common.utils.ViewUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.czhj.volley.toolbox.ImageLoader;
import com.czhj.volley.toolbox.StringUtil;
import com.sigmob.sdk.base.ClickUIType;
import com.sigmob.sdk.base.SigmobRes;
import com.sigmob.sdk.base.SigmobWebViewClient;
import com.sigmob.sdk.base.WindConstants;
import com.sigmob.sdk.base.common.ADEvent;
import com.sigmob.sdk.base.common.AdFormat;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.common.BaseAdViewControllerListener;
import com.sigmob.sdk.base.common.ClickType;
import com.sigmob.sdk.base.common.CreativeType;
import com.sigmob.sdk.base.common.ExternalViewabilitySessionManager;
import com.sigmob.sdk.base.common.MotionManger;
import com.sigmob.sdk.base.common.OnSigAdClickListener;
import com.sigmob.sdk.base.common.PointEntitySigmobUtils;
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.ClickCommon;
import com.sigmob.sdk.base.models.IntentActions;
import com.sigmob.sdk.base.models.SigMacroCommon;
import com.sigmob.sdk.base.models.rtb.ClickAreaSetting;
import com.sigmob.sdk.base.models.rtb.CompanionEndcard;
import com.sigmob.sdk.base.models.rtb.MaterialMeta;
import com.sigmob.sdk.base.mta.PointCategory;
import com.sigmob.sdk.base.mta.PointEntitySigmob;
import com.sigmob.sdk.base.views.AdLogoView;
import com.sigmob.sdk.base.views.AlertDialogWidget;
import com.sigmob.sdk.base.views.CompanionAdsWidget;
import com.sigmob.sdk.base.views.CreativeWebView;
import com.sigmob.sdk.base.views.DownloadDialog;
import com.sigmob.sdk.base.views.Drawables;
import com.sigmob.sdk.base.views.OvalButton;
import com.sigmob.sdk.base.views.RecommendDialog;
import com.sigmob.sdk.base.views.ShakeNewView;
import com.sigmob.sdk.base.views.SlopeView;
import com.sigmob.sdk.base.views.SwingView;
import com.sigmob.sdk.base.views.VideoButtonWidget;
import com.sigmob.sdk.base.views.VideoProgressBarWidget;
import com.sigmob.sdk.base.views.WringView;
import com.sigmob.sdk.nativead.DisLikeDialog;
import com.sigmob.sdk.videocache.CacheListener;
import com.sigmob.sdk.videoplayer.BaseVideoAdView;
import com.sigmob.sdk.videoplayer.VIDEO_PLAYER_STATE;
import com.sigmob.sdk.videoplayer.VideoPlayerStatusListener;
import com.sigmob.windad.natives.WindNativeAdData;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VideoViewController extends BaseVideoViewController implements AdStackManager.AdCacheVideoListener, OnSigAdClickListener, CacheListener {
    static final String AD_CONFIG = "video_config";
    private static final String CURRENT_POSITION = "current_position";
    private static final String VIDEO_FINISHED = "video_finished";
    private static final String COMPANIONAD_VISABLE = "companionAd_visable";
    private static final long VIDEO_PROGRESS_TIMER_CHECKER_DELAY = 50;
    private static final int SEEKER_POSITION_NOT_INITIALIZED = -1;
    /**
     * SigmobAndroid WebViews supposedly have padding on each side of 10 dp. However, through empirical
     * testing, the number is actually closer to 8 dp. Increasing the width and height of the
     * WebView by this many dp will make the images inside not get cut off. This also prevents the
     * image from being scrollable.
     */

    private static final int DEFAULT_VIDEO_DURATION_FOR_CLOSE_BUTTON = 0;
    private static int mInteractionType = 0;
    private final int padding;
    String coordinate;
    private AdLogoView adLogoView;
    private ImageView videoImageView;
    private RelativeLayout mVideoViewLayout;
    private int mInsetBottom = 0;
    private boolean isSteamPlay = false;
    private int mWaitShowCount = 0;
    private String endcard_loading_state = "undone";
    private List<String> duration_seq = new ArrayList<>();
    private List<String> video_time_seq = new ArrayList<>();
    private long mShowWaitTimeStamp;
    private String skip_state = "none";
    private boolean isEndCardtouched;
    private Handler mHandle = new Handler();
    private boolean isAutoRemoveVideoView = true;
    private AlertDialogWidget mDialogWidget;
    private String placementId;
    private int animate_delay_secs;
    private boolean isShowCompanionAds;
    private BaseVideoConfig mVideoConfig;
    private int mRequestedOrientation;
    private BaseVideoAdView mVideoView;
    private BaseAdUnit mAdUnit = null;
    private View mCompanionAdView;//endCard
    private ExternalViewabilitySessionManager mExternalViewabilitySessionManager;
    private MediaMetadataRetriever mMediaMetadataRetriever = new MediaMetadataRetriever();

    private VideoProgressBarWidget mProgressBarWidget;
    private VideoButtonWidget mCloseButtonWidget;
    private OvalButton mSkipButtonWidget;
//    private AdInfoDialog adInfoDialog;
    private VideoCompanionAdConfig mVideoCompanionAdConfig;


    private int mShowCloseButtonDelay = DEFAULT_VIDEO_DURATION_FOR_CLOSE_BUTTON;
    private int mSeekerPositionOnPause;
    private boolean mIsVideoFinishedPlaying;
    private boolean mVideoError;
    private int mDuration;
    private boolean isCompanionAdVisable = false;
    private RelativeLayout waitRelativeLayout;
    /**
     * For when the video is closing.
     */
    private boolean mIsClosing = false;
    private VideoButtonWidget mSoundButtonWidget;
    private boolean isMute = false;
    private boolean mIsRewarded = false;
    private ImageLoader.ImageContainer mImageContainer;
    private CompanionAdsWidget mCompanionAdsWidget;//伴随条
    private boolean isProgressBarWidgetShow = false;
    private boolean isTouched;
    private boolean isError;
    private int mCurrentPosition;
    /*
     * 当用户点击关闭时，如果主广告之前没有点击行为且未弹出过closeCard，则在用户点击关闭按钮时弹出closeCard。
     * closeCard上的关闭按钮为关闭closeCard回到主广告的endCard
     */
    private RecommendDialog recommendDialog;
    private boolean isDialogShow = false;
    private DownloadDialog downloadDialog;
    private boolean isElementDialogShow = false;
    private OvalButton endcardFeedBack;
    private OvalButton videoAdFeedBack;
    private MotionManger.Motion motion;
    private View actionView;
    private boolean isClicked;
    private DisLikeDialog mDislikeDialog;
    private OvalButton mRewardTips;
    private boolean mIsCharge;
    private boolean isSkipShow;
    private boolean isComplete;
    private boolean isStoped;
    private String cacheMp4Url;

    public VideoViewController(final Activity activity,
                               BaseAdUnit baseAdUnit, final Bundle intentExtras,
                               final Bundle savedInstanceState,
                               final String broadcastIdentifier,
                               final BaseAdViewControllerListener baseVideoViewControllerListener)
            throws IllegalStateException {
        super(activity, broadcastIdentifier, baseVideoViewControllerListener);

        mAdUnit = baseAdUnit;

        mVideoConfig = (BaseVideoConfig) mAdUnit.getAdConfig();
        padding = Dips.dipsToIntPixels(10, mContext);

        mVideoConfig.initFourElements(getActivity(), mAdUnit, new DownloadDialog.onPrivacyClickListener() {
            @Override
            public void onCloseClick() {
                isElementDialogShow = false;
                isClicked = false;
                videoResume();
                if (motion != null) {
                    motion.start();
                }
//                getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_CLOSE, (int) mVideoView.getCurrentPositionWhenPlaying());
            }

            @Override
            public void onButtonClick(String url, String clickCoordinate) {//执行点击逻辑
                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
                mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_APPINFO;
                mAdUnit.getClickCommon().is_final_click = true;
            }

            @Override
            public void onShowSuccess() {
                isElementDialogShow = true;
                isClicked = true;
                videoPause();
                if (motion != null) {
                    motion.pause();
                }
//                getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_SHOW, (int) mVideoView.getCurrentPositionWhenPlaying());
            }
        });

        mVideoConfig.setOnAdClickListener(this);

        placementId = mAdUnit.getAdslot_id();

        mInteractionType = mAdUnit.getInteractionType();

        getActivity().requestWindowFeature(Window.FEATURE_NO_TITLE);
        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        optionSetting(activity, mRequestedOrientation, intentExtras);

        BaseMacroCommon baseMacroCommon = mAdUnit.getMacroCommon();
        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYLASTFRAME_, "1");

        isAutoRemoveVideoView = mAdUnit.getMaterial().creative_type != CreativeType.CreativeTypeVideo_transparent_html.getCreativeType();
        if (savedInstanceState != null) {
            mSeekerPositionOnPause = savedInstanceState.getInt(CURRENT_POSITION, SEEKER_POSITION_NOT_INITIALIZED);
            mIsVideoFinishedPlaying = savedInstanceState.getBoolean(VIDEO_FINISHED, false);
            isCompanionAdVisable = savedInstanceState.getBoolean(COMPANIONAD_VISABLE, false);
        }

        if (adSize == null) {
            int SigMobTheme = SigmobRes.getSig_base_theme();
            if (SigMobTheme != 0) {
                getActivity().setTheme(SigMobTheme);
            }

            getLayout().setBackgroundColor(Color.BLACK);
        } else {
            // Solid black background
            getLayout().setBackgroundColor(Color.TRANSPARENT);
        }

//        loadCompanionAdView();

        mVideoViewLayout = new RelativeLayout(mContext);
        mVideoViewLayout.setBackgroundColor(Color.BLACK);
        mVideoView = createVideoView(mContext, View.VISIBLE);
        final RelativeLayout.LayoutParams adViewLayout = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.FILL_PARENT, WRAP_CONTENT);
        adViewLayout.addRule(RelativeLayout.CENTER_IN_PARENT);

        mVideoViewLayout.addView(mVideoView, adViewLayout);

        getLayout().addView(mVideoViewLayout, new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));

        if (isSteamPlay) {

            addWaitLoadingView(mContext);
            showProgressDialog();

        }

        addAdlogoView();

        mVideoViewLayout.setClickable(true);
        mVideoViewLayout.setOnTouchListener(new View.OnTouchListener() {

            MotionEvent downEvent = null;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if (mAdUnit.getFullClickOnVideo()) {

                    if (mDialogWidget == null || mDialogWidget.getVisibility() != View.VISIBLE) {
                        if (event.getAction() == MotionEvent.ACTION_UP) {
                            if (downEvent == null) {
                                downEvent = event;
                            }

                            handleVideoClick(downEvent, event);

                        } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            downEvent = MotionEvent.obtain(event);
                        }
                    }
                } else {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (mAdUnit.getMaterial().enable_collapse_tool_bar && mCompanionAdsWidget != null && mCompanionAdsWidget.isShowed()) {
                            if (mCompanionAdsWidget.isHide()) {
                                mCompanionAdsWidget.setVisibility(View.VISIBLE);
                            } else {
                                mCompanionAdsWidget.setVisibility(View.INVISIBLE);
                            }
                        }

                        PointEntitySigmobUtils.touchEventRecord(mAdUnit, event, "useless_video_click", false);
                    }
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        downEvent = MotionEvent.obtain(event);

                    }

                }
                return true;
            }
        });
    }

    private void handleVideoClick(MotionEvent downEvent, MotionEvent upEvent) {
        boolean isValidClick = false;

        mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_MATERIAL;
        mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_AD;

        final ClickAreaSetting setting = mAdUnit.getClickAreaSetting();
        final float left = setting.left * ClientMetadata.getInstance().getDisplayMetrics().widthPixels;
        final float right = (1 - setting.right) * ClientMetadata.getInstance().getDisplayMetrics().widthPixels;
        final float top = setting.top * ClientMetadata.getInstance().getDisplayMetrics().heightPixels;
        final float bottom = (1 - setting.bottom) * ClientMetadata.getInstance().getDisplayMetrics().heightPixels;


        if ((left < upEvent.getRawX() && upEvent.getRawX() < right) &&
                (top < upEvent.getRawY() && upEvent.getRawY() < bottom)) {
            BaseMacroCommon baseMacroCommon = mAdUnit.getMacroCommon();
            isValidClick = true;


            if (baseMacroCommon instanceof SigMacroCommon) {
                ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf((int) getPlayProcess() * 100));
                ((SigMacroCommon) baseMacroCommon).updateClickMarco(downEvent, upEvent, true);
                coordinate = ((SigMacroCommon) baseMacroCommon).getCoordinate();

            }
            if (mVideoCompanionAdConfig == null) {
                loadCompanionAdView();
            }

            mVideoConfig.handleUrlAction(ClickUIType.VIDEO_CLICK, coordinate, true);

        }
        touchEventRecord(mAdUnit, upEvent, "useless_video_click", isValidClick);
    }

    public static int getDefaultVideoDurationForCloseButton() {
        return DEFAULT_VIDEO_DURATION_FOR_CLOSE_BUTTON;
    }

    public void addWaitLoadingView(Context context) {
        waitRelativeLayout = new RelativeLayout(context);

        ImageView imageView = new ImageView(context);
        imageView.setImageBitmap(Drawables.LOADING.getBitmap());
        imageView.setId(ClientMetadata.generateViewId());
        RotateAnimation animation;
        int magnify = 10000;
        int toDegrees = 360;
        int duration = 800;
        toDegrees *= magnify;
        duration *= magnify;
        animation = new RotateAnimation(0, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(duration);
        LinearInterpolator lir = new LinearInterpolator();
        animation.setInterpolator(lir);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);

        RelativeLayout.LayoutParams imageViewLP = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
//        imageViewLP.setMargins(0, Dips.dipsToIntPixels(16, getContext()), 0, 0);
        imageViewLP.addRule(RelativeLayout.CENTER_IN_PARENT);
        imageView.setAnimation(animation);

//        TextView textView = new TextView(context);
//        textView.setText("LOADING...");
//        textView.setTextColor(Color.DKGRAY);
//        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
//        RelativeLayout.LayoutParams textViewLP = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
//        textViewLP.setMargins(0, Dips.dipsToIntPixels(8, getContext()), 0, 0);
//
//        textViewLP.addRule(RelativeLayout.BELOW, imageView.getId());
//        textViewLP.addRule(RelativeLayout.CENTER_HORIZONTAL);
        waitRelativeLayout.addView(imageView, imageViewLP);
//        waitRelativeLayout.addView(textView, textViewLP);
        waitRelativeLayout.setVisibility(View.INVISIBLE);

//        GradientDrawable bargd = new GradientDrawable();//创建drawable
//
//        bargd.setCornerRadius(Dips.dipsToIntPixels(4, getContext()) * 2);
//        bargd.setColor(Color.WHITE);
//        bargd.setAlpha((int) (255 * 0.7f));
//
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//            waitRelativeLayout.setBackground(bargd);
//        } else {
//            waitRelativeLayout.setBackgroundDrawable(bargd);
//        }
//        RelativeLayout.LayoutParams waitLP = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(100, getContext()), Dips.dipsToIntPixels(100, getContext()));
        RelativeLayout.LayoutParams waitLP = new RelativeLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        waitLP.addRule(RelativeLayout.CENTER_IN_PARENT);
        getLayout().addView(waitRelativeLayout, waitLP);
    }

    public void addAdlogoView() {
        adLogoView = new AdLogoView(getContext().getApplicationContext(), 0);

        adLogoView.setId(ClientMetadata.generateViewId());
        RelativeLayout.LayoutParams adLogoViewLP = new RelativeLayout.LayoutParams(WRAP_CONTENT, Dips.dipsToIntPixels(16, getContext()));

        if (mCompanionAdsWidget != null) {

            adLogoViewLP.addRule(RelativeLayout.ALIGN_BOTTOM, mCompanionAdsWidget.getId());
        } else {
            adLogoViewLP.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            adLogoViewLP.setMargins(0, 0, 0, padding);
        }

        try {

            adLogoView.showAdLogo(mAdUnit.getAd_source_logo());
            if (!mAdUnit.getInvisibleAdLabel()) {
                adLogoView.showAdText(SigmobRes.getAd());
            }

        } catch (Throwable th) {

        }

        getLayout().addView(adLogoView, adLogoViewLP);

    }

    @Override
    public BaseAdUnit getAdUnit() {
        return mAdUnit;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        try {
            outState.putBoolean(VIDEO_FINISHED, mIsVideoFinishedPlaying);
            outState.putInt(CURRENT_POSITION, mSeekerPositionOnPause);
            outState.putBoolean(COMPANIONAD_VISABLE, isCompanionAdVisable);
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
    }

    private void showProgressDialog() {

        if (waitRelativeLayout == null || waitRelativeLayout.getVisibility() == View.VISIBLE)
            return;

        mShowWaitTimeStamp = System.currentTimeMillis();
        waitRelativeLayout.setVisibility(View.VISIBLE);
        skip_state = "loading";

        if (++mWaitShowCount > 2) {
            showSkip((int) mVideoView.getCurrentPositionWhenPlaying(), true);

        } else {

            mHandle.postDelayed(new Runnable() {
                @Override
                public void run() {

                    mHandle.removeCallbacksAndMessages(null);
                    showSkip((int) mVideoView.getCurrentPositionWhenPlaying(), true);

                }
            }, 5000);
        }
    }

    @SuppressLint("DefaultLocale")
    private void dismissProgressDialog() {

        if (waitRelativeLayout != null && waitRelativeLayout.getVisibility() == View.VISIBLE) {

            duration_seq.add(String.format("%d", System.currentTimeMillis() - mShowWaitTimeStamp));
            video_time_seq.add(String.format("%.2f", (int) mVideoView.getCurrentPositionWhenPlaying() / 1000.0f));
            skip_state = "play";

            mHandle.removeCallbacksAndMessages(null);
            waitRelativeLayout.setVisibility(View.INVISIBLE);
        }

    }

    public ExternalViewabilitySessionManager getExternalViewabilitySessionManager() {

        if (mExternalViewabilitySessionManager == null) {
            mExternalViewabilitySessionManager = new ExternalViewabilitySessionManager();
            mExternalViewabilitySessionManager.createDisplaySession(getAdUnit());
        }
        return mExternalViewabilitySessionManager;
    }


    private View.OnClickListener onFeedBackListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Activity activity = getActivity();
            if (activity != null && mAdUnit != null) {
                mDislikeDialog = new DisLikeDialog(activity, mAdUnit);

                if (mDislikeDialog != null) {
                    mDislikeDialog.showDislikeDialog();
                }
                mDislikeDialog.setDislikeInteractionCallback(new WindNativeAdData.DislikeInteractionCallback() {
                    @Override
                    public void onShow() {
                        videoPause();
                        if (motion != null) {
                            motion.pause();
                            ;
                        }

                    }

                    @Override
                    public void onSelected(int position, String value, boolean enforce) {

                        videoResume();
                        if (mDislikeDialog != null) {
                            mDislikeDialog.dismiss();
                            mDislikeDialog.destroy();
                            mDislikeDialog = null;
                        }
                        if (motion != null) {
                            motion.start();
                        }
                    }

                    @Override
                    public void onCancel() {
                        videoResume();
                        if (mDislikeDialog != null) {
                            mDislikeDialog.dismiss();
                            mDislikeDialog.destroy();
                            mDislikeDialog = null;
                        }
                        if (motion != null) {
                            motion.start();
                        }
                    }
                });
            }
        }
    };

    private void addFeedBack(Context context, int visibility) {

        if (videoAdFeedBack == null) {
            videoAdFeedBack = new OvalButton(context);
            videoAdFeedBack.setText("反馈");
            videoAdFeedBack.setId(ClientMetadata.generateViewId());
            videoAdFeedBack.setOnClickListener(onFeedBackListener);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(45, context), Dips.dipsToIntPixels(22, context));
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.setMargins(padding, padding * 2, 0, 0);
            getLayout().addView(videoAdFeedBack, layoutParams);
        }
        if (videoAdFeedBack != null) {
            videoAdFeedBack.setVisibility(visibility);
        }
    }

    private void addRewardTips(Context context, int visibility) {

        if (mAdUnit.getRewardStyle() == 1) {

            mRewardTips = new OvalButton(context);

            int remainRewardTime = getRemainRewardTime();
            if (remainRewardTime > 0)
                mRewardTips.setText(remainRewardTime + "s后获取奖励");
            else{
                mRewardTips.setText("已获得奖励");
                makeIsReward(false);
            }
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(97, context), Dips.dipsToIntPixels(22, context));
            layoutParams.addRule(RelativeLayout.RIGHT_OF, videoAdFeedBack.getId());
            layoutParams.setMargins(padding, padding * 2, 0, 0);
            getLayout().addView(mRewardTips, layoutParams);

        }


    }

    private void addMotionView() {
        int viewId = (int) mAdUnit.getWidgetId(0);
        switch (viewId) {
            case 138757: {
                addShakeView();
            }
            break;
            case 138733: {
                addSlopView();
            }
            break;
            case 138758: {
                addWringView();
            }
            break;
            case 138731: {
                addSwingView();
            }
            break;
            default: {

            }
        }

    }

    private void addShakeView() {
        actionView = new ShakeNewView(getContext());
        int size = Dips.dipsToIntPixels(100, getContext());
        int bottomMargin = Dips.dipsToIntPixels(145, getContext());
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(size, size);
        ll.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ll.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ll.setMargins(0, 0, 0, bottomMargin);

        motion = new MotionManger.ShakeMotion(getContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {

            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null) {

                    Number x_max_acc = info.get("x_max_acc");
                    Number y_max_acc = info.get("y_max_acc");
                    Number z_max_acc = info.get("z_max_acc");


                    if (x_max_acc != null) {
                        mAdUnit.getClickCommon().x_max_acc = String.valueOf(x_max_acc.intValue());
                    }
                    if (y_max_acc != null) {
                        mAdUnit.getClickCommon().y_max_acc = String.valueOf(y_max_acc.intValue());
                    }
                    if (z_max_acc != null) {
                        mAdUnit.getClickCommon().z_max_acc = String.valueOf(z_max_acc.intValue());
                    }
                    actionView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isClicked) return;
                            mAdUnit.getClickCommon().sld = "2";
                            handleMotionClick();
                        }
                    }, 400);
                }

            }
        });
        motion.setLevel(mAdUnit.getSensitivity());
        motion.start();
        getLayout().addView(actionView, ll);
    }

    private void handleMotionClick() {
        isClicked = true;
        mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_COMPONENT;
        mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_AD;
        mAdUnit.getClickCommon().is_final_click = true;

        mVideoConfig.handleUrlAction(ClickUIType.MOTION, null, true);
    }

    private void addWringView() {

        actionView = new WringView(getContext());
        int size = Dips.dipsToIntPixels(100, getContext());

        int bottomMargin = Dips.dipsToIntPixels(145, getContext());
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(size, size);
        ll.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ll.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ll.setMargins(0, 0, 0, bottomMargin);
        motion = new MotionManger.OrientationMotion(getContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null) {
                    if (isClicked) return;
                    isClicked = true;
                    Number turn_x = info.get("turn_x");
                    Number turn_y = info.get("turn_y");
                    Number turn_z = info.get("turn_z");
                    Number turn_time = info.get("turn_time");

                    if (turn_x != null) {
                        mAdUnit.getClickCommon().turn_x = String.valueOf(turn_x.intValue());
                    }
                    if (turn_y != null) {
                        mAdUnit.getClickCommon().turn_y = String.valueOf(turn_y.intValue());
                    }
                    if (turn_z != null) {
                        mAdUnit.getClickCommon().turn_z = String.valueOf(turn_z.intValue());
                    }
                    mAdUnit.getClickCommon().turn_time = String.valueOf(turn_time);

                    actionView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAdUnit.getClickCommon().sld = "5";
                            handleMotionClick();
                        }
                    }, 400);
                }
            }
        }, MotionManger.OrientationMotionType.WRING);
        motion.setLevel(mAdUnit.getSensitivity());
        motion.start();
        getLayout().addView(actionView, ll);
    }

    private void addSwingView() {

        actionView = new SwingView(getContext());
        int size = Dips.dipsToIntPixels(100, getContext());

        int bottomMargin = Dips.dipsToIntPixels(145, getContext());
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(size, size);
        ll.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ll.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ll.setMargins(0, 0, 0, bottomMargin);
        motion = new MotionManger.OrientationMotion(getContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {
                if (actionView instanceof SwingView) {
                    ((SwingView) actionView).updateProcess(progress);
                }
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null) {
                    if (isClicked) return;
                    isClicked = true;

                    Number x_max_acc = info.get("x_max_acc");
                    Number y_max_acc = info.get("y_max_acc");
                    Number z_max_acc = info.get("z_max_acc");


                    if (x_max_acc != null) {
                        mAdUnit.getClickCommon().x_max_acc = String.valueOf(x_max_acc.intValue());
                    }
                    if (y_max_acc != null) {
                        mAdUnit.getClickCommon().y_max_acc = String.valueOf(y_max_acc.intValue());
                    }
                    if (z_max_acc != null) {
                        mAdUnit.getClickCommon().z_max_acc = String.valueOf(z_max_acc.intValue());
                    }
                    actionView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mAdUnit.getClickCommon().sld = "2";

                            handleMotionClick();
                        }
                    }, 400);
                }
            }
        }, MotionManger.OrientationMotionType.SWING);

        motion.setLevel(mAdUnit.getSensitivity());
        motion.start();
        getLayout().addView(actionView, ll);
    }

    private void listenerCacheUnavailable(String url){
        Uri uri =  Uri.parse(url);
        if (uri != null && uri.getHost().equalsIgnoreCase("127.0.0.1")){
            Pattern URL_PATTERN = Pattern.compile("/(.*)");

            Matcher matcher = URL_PATTERN.matcher(uri.getPath());
            if (matcher.find()){
                 cacheMp4Url  = matcher.group(1);

                AdStackManager.getHttProxyCacheServer().registerCacheListener(this,cacheMp4Url);
            }

        }

    }
    private void addSlopView() {

        actionView = new SlopeView(getContext());
        int size = Dips.dipsToIntPixels(92, getContext());

        int bottomMargin = Dips.dipsToIntPixels(145, getContext());
        RelativeLayout.LayoutParams ll = new RelativeLayout.LayoutParams(size, size);
        ll.addRule(RelativeLayout.CENTER_HORIZONTAL);
        ll.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        ll.setMargins(0, 0, 0, bottomMargin);

        motion = new MotionManger.OrientationMotion(getContext(), new MotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {
                if (actionView instanceof SlopeView) {
                    ((SlopeView) actionView).updateScreen(progress);

                }
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null) {
                    Number x_max_acc = info.get("x_max_acc");
                    Number y_max_acc = info.get("y_max_acc");
                    Number z_max_acc = info.get("z_max_acc");


                    if (x_max_acc != null) {
                        mAdUnit.getClickCommon().x_max_acc = String.valueOf(x_max_acc.intValue());
                    }
                    if (y_max_acc != null) {
                        mAdUnit.getClickCommon().y_max_acc = String.valueOf(y_max_acc.intValue());
                    }
                    if (z_max_acc != null) {
                        mAdUnit.getClickCommon().z_max_acc = String.valueOf(z_max_acc.intValue());
                    }
                    actionView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isClicked) return;
                            mAdUnit.getClickCommon().sld = "2";
                            handleMotionClick();
                        }
                    }, 400);
                }
            }
        }, MotionManger.OrientationMotionType.SLOPE);
        motion.setLevel(mAdUnit.getSensitivity());
        motion.start();
        getLayout().addView(actionView, ll);
    }

    private void addEndCardFeedBack(Context context, int visibility) {

//        if (videoAdFeedBack == null && !TextUtils.isEmpty(WindSDKConfig.getInstance().getFeedbackUrl())) {
        if (endcardFeedBack == null) {

            endcardFeedBack = new OvalButton(context);
            endcardFeedBack.setText("反馈");
            endcardFeedBack.setOnClickListener(onFeedBackListener);
            endcardFeedBack.setId(ClientMetadata.generateViewId());
            int padding = Dips.dipsToIntPixels(3, mContext);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(45, context), Dips.dipsToIntPixels(30, context));
            layoutParams.addRule(RelativeLayout.LEFT_OF, mCloseButtonWidget.getId());
            layoutParams.addRule(RelativeLayout.ALIGN_TOP,mCloseButtonWidget.getId());
            layoutParams.setMargins(0, -padding, 0, 0);
            getLayout().addView(endcardFeedBack, layoutParams);

        }
        if (endcardFeedBack != null) {
            endcardFeedBack.setVisibility(visibility);
        }
    }

    @Override
    public void onCreate() {

        super.onCreate();

        MaterialMeta materialMeta = mAdUnit.getMaterial();

        mInsetBottom = ClientMetadata.getInstance().getInsetBottom();

        // Viewability measurements
        if (!isCompanionAdVisable) {


            if (mVideoConfig == null) {
                throw new IllegalStateException("BaseVideoConfig does not have a video disk path");
            }

            // Progress bar overlaying bottom of video view
            if (isProgressBarWidgetShow)
                addProgressBarWidget(getContext(), View.INVISIBLE);


            if (materialMeta.has_companion_endcard != null && materialMeta.has_companion_endcard && materialMeta.companion != null) {

                addCompanionAdsWidget(getContext(), View.INVISIBLE);
                animate_delay_secs = materialMeta.companion.show_delay_secs;
            }
            addFeedBack(getContext(), View.VISIBLE);
            addSoundButtonWidget(getContext(), View.VISIBLE);
            addSkipButtonWidget(getContext(), View.INVISIBLE);

            final List<FractionalProgressAdTracker> trackers =
                    new ArrayList<>();
            trackers.add(new FractionalProgressAdTracker(
                    ADEvent.AD_START, 0.0f));
            trackers.add(new FractionalProgressAdTracker(
                    ADEvent.AD_PLAY_QUARTER, 0.25f));
            trackers.add(new FractionalProgressAdTracker(
                    ADEvent.AD_PLAY_TWO_QUARTERS, 0.5f));
            trackers.add(new FractionalProgressAdTracker(
                    ADEvent.AD_PLAY_THREE_QUARTERS, 0.75f));
//        trackers.add(new FractionalProgressAdTracker(MessageType.QUARTILE_EVENT,
//                ADEvent.AD_PLAY_COMPLETE, 0.85f));
            mVideoConfig.addFractionalTrackers(trackers);

            broadcastAction(ACTION_INTERSTITIAL_VOPEN);

        } else {

            makeCloseInteractable();

        }
        addAlertDialogWidget(getContext(), View.INVISIBLE);


        addMotionView();
    }

    private void videoResume() {

        try {

            if (!mIsVideoFinishedPlaying) {
                int currentPosition = (int) mVideoView.getCurrentPositionWhenPlaying();
                if (mSeekerPositionOnPause > 0 && currentPosition == 0) {
                    SigmobLog.d("video seek to " + mSeekerPositionOnPause);
                    mVideoView.seekTo(mSeekerPositionOnPause);
                }

                if ((mDialogWidget == null || mDialogWidget.getVisibility() != View.VISIBLE) && !isElementDialogShow) {
                    mVideoView.goOnPlayOnResume();
                }

                if (mSeekerPositionOnPause != SEEKER_POSITION_NOT_INITIALIZED) {
                    mVideoConfig.handleResume(getContext(), mSeekerPositionOnPause);
                }
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

    }

    @Override
    public void onResume() {
        if (!isCompanionAdVisable && mDislikeDialog == null) videoResume();
        if (mCompanionAdView != null && mCompanionAdView instanceof CreativeWebView) {
            ((CreativeWebView) mCompanionAdView).resumeTimers();
        }
        isClicked = false;

        if (mDislikeDialog == null && motion != null) {
            motion.start();
        }
    }

    private void videoPause() {
        try {


            if (!mIsVideoFinishedPlaying) {
                mVideoView.goOnPlayOnPause();
                SigmobLog.i("videoView.pause()");
                mSeekerPositionOnPause = (int) mVideoView.getCurrentPositionWhenPlaying();
                getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_PAUSE, (int) mVideoView.getCurrentPositionWhenPlaying());
                mVideoConfig.handlePause(getContext(), mSeekerPositionOnPause);
            }
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

    }

    @Override
    public void onPause() {
        if (mIsClosing) {
            broadcastAction(IntentActions.ACTION_INTERSTITIAL_DISMISS);
            return;
        }
        if (motion != null) {
            motion.pause();
        }
        if (!isCompanionAdVisable) videoPause();
    }

    public String getPlacementId() {
        return placementId;
    }

    @Override
    public void onDestroy() {


        try {
            SigmobLog.d("VideoViewController onDestroy() called");

            AdStackManager.shareInstance().removeAdCacheVideoListener(this);
            if (!mIsClosing) {
                broadcastAction(IntentActions.ACTION_INTERSTITIAL_DISMISS);
            }

            if (cacheMp4Url != null){
                AdStackManager.getHttProxyCacheServer().unregisterCacheListener(this,cacheMp4Url);
            }

            if (videoAdFeedBack != null) {
                videoAdFeedBack.setOnClickListener(null);
                ViewUtil.removeFromParent(videoAdFeedBack);
            }
            if (endcardFeedBack != null) {
                endcardFeedBack.setOnClickListener(null);
                ViewUtil.removeFromParent(endcardFeedBack);
            }
            if (mDislikeDialog != null) {
                mDislikeDialog.setDislikeInteractionCallback(null);
                mDislikeDialog.dismiss();
                mDislikeDialog.destroy();
            }
            mVideoConfig.destroy();

//            if (adInfoDialog != null) {
//                adInfoDialog.dismiss();
//                adInfoDialog.destroy();
//            }
            if (mImageContainer != null) {
                mImageContainer.cancelRequest();
                mImageContainer = null;
            }
            if (mExternalViewabilitySessionManager != null) {
//                mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_VCLOSE, 0);
                mExternalViewabilitySessionManager.endDisplaySession();
                mExternalViewabilitySessionManager = null;
            }

            if (mVideoCompanionAdConfig != null) {
                mVideoCompanionAdConfig.setCompanionAdClickListenner(null);
            }
            if (mMediaMetadataRetriever != null) {
                mMediaMetadataRetriever.release();
            }

            if (mCloseButtonWidget != null)
                mCloseButtonWidget.setOnTouchListener(null);

            if (mSkipButtonWidget != null)
                mSkipButtonWidget.setOnTouchListener(null);

            if (mSoundButtonWidget != null)
                mSoundButtonWidget.setOnTouchListener(null);

            if (mVideoView != null)
                mVideoView.destroy();

            if (mCompanionAdView != null && mCompanionAdView instanceof CreativeWebView) {
                ((CreativeWebView) mCompanionAdView).setWebViewClickListener(null);
                ((CreativeWebView) mCompanionAdView).setLogoClickListener(null);
            }
            if (recommendDialog != null) {
                recommendDialog.dismiss();
                recommendDialog.destroy();
                recommendDialog = null;
            }

            if (motion != null) {
                motion.destroy();
                motion = null;
            }
            if (mAdUnit != null){
                mAdUnit.destroy();
            }
            mCompanionAdView = null;
            mProgressBarWidget = null;
            super.onDestroy();

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }

    }

    private float getPlayProcess() {
        int duration = getDuration();
        if (duration > 0) {
            return (int) mVideoView.getCurrentPositionWhenPlaying() / (float) getDuration();

        }
        return 0;
    }

    @Override
    public void onConfigurationChanged(final Configuration newConfig) {

        if (mVideoConfig != null && mVideoCompanionAdConfig == null) {
            mVideoCompanionAdConfig = BaseVideoConfig.getVideoCompanionAd(mAdUnit);
            mVideoCompanionAdConfig.setVideoConfig(mVideoConfig);
        }

        if (getExternalViewabilitySessionManager() != null) {
            getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_ROTATION, (int) mVideoView.getCurrentPositionWhenPlaying());
        }

    }

    public boolean shouldBeSkipable() {


        try {
            long currentPosition = (int) mVideoView.getCurrentPositionWhenPlaying();

            if (mAdUnit.getSkipSeconds() > -1) {
                return currentPosition / 1000.0f + 0.3f >= mAdUnit.getSkipSeconds();
            } else if (getDuration() >0){
                return (getDuration() / 1000.0f* mAdUnit.getSkipPercent()*0.01f) < currentPosition / 1000.0f;
            }

        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
        return true;

    }


    public boolean isCharge() {


        try {
            boolean isCharge = false;
            if (mAdUnit.getAd_type() == AdFormat.REWARD_VIDEO){
                long currentPosition = (int) mVideoView.getCurrentPositionWhenPlaying();

                if (mAdUnit.getChargeSeconds() > -1) {
                    isCharge = currentPosition / 1000.0f + 0.3f >= mAdUnit.getChargeSeconds();
                } else if (getDuration() > 0) {
                    isCharge = currentPosition * 100 / getDuration() >= mAdUnit.getChargePercent();
                }
            }

            return isCharge;
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
        return true;

    }

    @Override
    public void onBackPressed() {

    }

    // Enable the device's back button when the video close button has been displayed
    @Override
    public boolean backButtonEnabled() {
        return false;
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            getBaseAdViewControllerListener().onFinish();
        }
    }

    @Override
    public void onStart() {

    }

    private void adjustSkipOffset() {

        try {
            int videoDuration = getDuration();
            // Override if skipoffset attribute is specified in VAST
            mShowCloseButtonDelay = mVideoConfig.getSkipOffsetMillis(videoDuration);
        } catch (Throwable e) {
            SigmobLog.e(e.getMessage());
        }
    }

    public boolean shouldBeShowCompanionAds() {


        if (mCompanionAdsWidget != null && mCompanionAdsWidget.isShowed()) {
            return true;
        }

        try {

            long currentPosition = (int) mVideoView.getCurrentPositionWhenPlaying();

            boolean showAble = currentPosition / 1000 >= animate_delay_secs;
            if (showAble) {
                SigmobLog.d("showAble CompanionAds");
            }
            return showAble;
        } catch (Throwable e) {
            SigmobLog.e("shouldBeShowCompanionAds", e);
        }
        return true;

    }

    public void showCompanionAds() {

        if (isShowCompanionAds) return;

        if (mCompanionAdsWidget != null) {
//            mCompanionAdsWidget.bringToFront();
            mCompanionAdsWidget.setVisibility(View.VISIBLE);
            isShowCompanionAds = true;
        }
    }

    private ImageView createVideoImageView(Context context) {

        ImageView imageView = new ImageView(context);

        imageView.setLayoutParams(new RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        return imageView;

    }

    private BaseVideoAdView createVideoView(final Context context, int initialVisibility) {
        if (mAdUnit.getProxyVideoUrl() == null) {
            throw new IllegalStateException("BaseVideoConfig does not have a video disk path");
        }

        final BaseVideoAdView videoView = new BaseVideoAdView(context);


        videoView.setVideoPlayerStatusListener(new VideoPlayerStatusListener() {
            @Override
            public void OnStateChange(VIDEO_PLAYER_STATE state) {

                SigmobLog.i("video player state change " + state);
                switch (state) {
                    case STATE_PREPARED: {
                        mDuration = (int) videoView.getDuration();
                        if (mDuration == 0) {
                            mDuration = mAdUnit.getDuration();
                        }
                        if (!isSkipShow){
                            mSkipButtonWidget.setText(String.valueOf((int)(getDuration()/1000.0f)));
                            mSkipButtonWidget.setVisibility(View.VISIBLE);
                        }
                        addRewardTips(getContext(), View.VISIBLE);
                        int width = videoView.getVideoWidth();
                        int height = videoView.getVideoHeight();
                        if (mVideoCompanionAdConfig != null) {
                            mVideoCompanionAdConfig.setDuration(getDuration());
                        }
                        if (isMute) {
                            int sigImageVideoMute = ResourceUtil.getDrawableId(getContext(), "sig_image_video_mute");
                            mSoundButtonWidget.updateButtonIcon(sigImageVideoMute);
                            mVideoView.setMute(true);
                        }
                        getExternalViewabilitySessionManager().onVideoPrepared(mDuration, mVideoConfig.getEndTime());

                        if (!mIsVideoFinishedPlaying && (mSeekerPositionOnPause == 0 || mSeekerPositionOnPause == SEEKER_POSITION_NOT_INITIALIZED)) {
                            BaseMacroCommon baseMacroCommon = mAdUnit.getMacroCommon();
                            if (baseMacroCommon instanceof SigMacroCommon) {
                                ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._COMPLETED_, "0");
                                ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._VIDEOTIME_, String.valueOf((getDuration() / 1000)));
                            }
//                        getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_START, (int)mVideoView.getCurrentPositionWhenPlaying());
                        }

                        if (isProgressBarWidgetShow)
                            mProgressBarWidget.calibrateAndMakeVisible(getDuration(), 0);

                    }
                    break;
                    case STATE_PLAYING: {

                    }
                    break;
                    case STATE_AUTO_COMPLETE: {
                        skip_state = "none";
                        stopVideoPlay(true);
                    }
                    break;
                    case STATE_STOP: {

                    }
                    break;
                    case STATE_BUFFERING_START: {
                        showProgressDialog();
                        videoView.goOnPlayOnPause();
                    }
                    break;
                    case STATE_BUFFERING_END: {
                        dismissProgressDialog();
                        videoView.goOnPlayOnResume();
                    }
                    break;
                    case STATE_PAUSE: {

                    }
                    break;
                    case STATE_ERROR: {
                        int code = videoView.getErrorCode();
                        String msg = videoView.getErrorMessage();
                        PointEntitySigmobUtils.SigmobError(PointCategory.VIDEO,code,msg,mAdUnit);

                        stopVideoPlay(false);
                    }
                    break;
                    default: {

                    }
                    break;
                }

            }

            @Override
            public void OnProgressUpdate(long position, long duration) {


                int videoLength = getDuration();
                updateRewardTips();
                if (showbeReward()) {
                    makeIsReward(false);
                }

                if(isCharge()){
                    makeIsCharge(false);
                }

                int time = getRemainRewardTime();
                if (mDialogWidget != null)
                    mDialogWidget.setduration(time);

                if (isProgressBarWidgetShow) {
                    int postion = (int) mVideoView.getCurrentPositionWhenPlaying();
                    mProgressBarWidget.updateProgress(postion);
                }

                if (position + 1000 < videoLength) {
                    if (shouldBeSkipable()){
                        makeSkipInteractable();
                    }else{
                        if (!isSkipShow){
                            int remind = (int)((videoLength - mVideoView.getCurrentPositionWhenPlaying()) / 1000.0f);
                            mSkipButtonWidget.setText(String.valueOf(remind));
                        }
                    }
                }

                if (shouldBeShowCompanionAds()) {
                    showCompanionAds();
                }

                //send
                final List<FractionalProgressAdTracker> trackersToTrack =
                        mVideoConfig.getUntriggeredTrackersBefore(position, videoLength);


                for (FractionalProgressAdTracker tracker : trackersToTrack) {

                    handleViewabilityQuartileEvent(tracker.getEvent());
                    tracker.setTracked();
                }
                if (position > videoLength) {
                    stopVideoPlay(true);
                }


            }
        });



        String proxyVideoUrl = mAdUnit.getProxyVideoUrl();

        try {

            if (!isAutoRemoveVideoView) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File file = new File(mAdUnit.getVideoPath());
                        if (file.exists()) {
                            mMediaMetadataRetriever.setDataSource(mAdUnit.getVideoPath());
                        } else {
                            mMediaMetadataRetriever.setDataSource(mAdUnit.getVideo_url(), new HashMap<String, String>());
                        }
                    }
                }).start();
                videoImageView = createVideoImageView(context);
            }

        } catch (Throwable throwable) {
            SigmobLog.e(throwable.getMessage());
        }

        videoView.setUp(proxyVideoUrl);
        listenerCacheUnavailable(proxyVideoUrl);

        videoView.setVisibility(initialVisibility);

        return videoView;

    }

    private void updateRewardTips() {

        if (mAdUnit.getAd_type() == AdFormat.FULLSCREEN_VIDEO) return;

        if (mRewardTips != null) {

            int remainRewardTime = getRemainRewardTime();
            if (remainRewardTime > 0)
                mRewardTips.setText(remainRewardTime + "s后获取奖励");
            else{
                mRewardTips.setText("已获得奖励");
            }
        }
    }


    private void addProgressBarWidget(final Context context, int initialVisibility) {
        mProgressBarWidget = new VideoProgressBarWidget(context);
        mProgressBarWidget.setAnchorId(mVideoView.getId());
        mProgressBarWidget.setVisibility(initialVisibility);
        getLayout().addView(mProgressBarWidget);
    }

    private String StringListToString(List<String> list) {

        if (list == null || list.size() == 0) return "";
        StringBuilder builder = new StringBuilder();
        int i = 0;
        builder.append('[');

        do {
            if (i != 0) {
                builder.append(',');
            }
            builder.append(list.get(i));
        } while (++i < list.size());
        builder.append(']');
        return builder.toString();

    }

    private void playloadPoint() {

        PointEntitySigmobUtils.SigmobTracking(PointCategory.PLAY_LOADING, null,
                mAdUnit, new PointEntitySigmobUtils.OnPointEntityExtraInfo() {
                    @Override
                    public void onAddExtra(Object pointEntityBase) {
                        if (pointEntityBase instanceof PointEntitySigmob) {
                            PointEntitySigmob entitySigmob = (PointEntitySigmob) pointEntityBase;

                            Map<String, String> option = new HashMap<>();

                            option.put("duration_seq", StringListToString(duration_seq));
                            option.put("video_time_seq", StringListToString(video_time_seq));
                            option.put("skip_state", skip_state);
                            option.put("video_duration", String.format("%.2f", getDuration() / 1000.0f));
                            option.put("endcard_loading_state", endcard_loading_state);
                            entitySigmob.setOptions(option);

                        }
                    }
                });

    }

    private void addAlertDialogWidget(final Context context, int initialVisibility) {
        if (mDialogWidget != null) {
            return;
        }
        mDialogWidget = new AlertDialogWidget(context, mVideoConfig.getDialogConfig());
        mDialogWidget.setVisibility(initialVisibility);
        getLayout().addView(mDialogWidget);
        mDialogWidget.setDialogListener(new AlertDialogWidget.OnAlertDiaglogWidgetListener() {
            @Override
            public void onConfirm() {

                mVideoView.goOnPlayOnResume();

                mDialogWidget.setVisibility(View.GONE);

                if (mCompanionAdsWidget != null && mCompanionAdsWidget.getFourElementsLayout() != null) {
                    mCompanionAdsWidget.getFourElementsLayout().setClickable(true);
                }
            }

            @Override
            public void onCancel() {

                if (!mIsVideoFinishedPlaying) {
                    getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_SKIP, (int) mVideoView.getCurrentPositionWhenPlaying());
                    mVideoConfig.handleSkip(mContext, (int) mVideoView.getCurrentPositionWhenPlaying(), getDuration(), mAdUnit);
                }
                mDialogWidget.setVisibility(View.GONE);

                if (mCompanionAdsWidget != null && mCompanionAdsWidget.getFourElementsLayout() != null) {
                    mCompanionAdsWidget.getFourElementsLayout().setClickable(true);
                }

                if (mVideoConfig.isEnableExitOnVideoClose()) {
                    getBaseAdViewControllerListener().onFinish();
                    return;
                }

                if (!isAutoRemoveVideoView) {
                    updateVideoImageView((int) mVideoView.getCurrentPositionWhenPlaying());
                }
                mVideoView.stopVideo();
                makeCloseInteractable();
                mIsVideoFinishedPlaying = true;

                // Show companion ad if available
                if (mExternalViewabilitySessionManager != null) {
                    mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_SHOW, 0);
                }

            }
        });

    }

    private void updateVideoImageView() {

        int postion = mVideoConfig.getSeekPostion();
        int duration = getDuration() - 2;
        if (postion == 99999999) {
            updateVideoImageView(0);
        } else if (postion == -99999999) {
            updateVideoImageView(duration);
        } else if (postion > 0) {
            updateVideoImageView(postion);
        } else {
            updateVideoImageView(postion + duration);
        }
    }

    private void addCompanionAdsWidget(final Context context, int initialVisibility) {

        MaterialMeta materialMeta = mAdUnit.getMaterial();
        CompanionEndcard companionEndcard = materialMeta.companion;

        if (companionEndcard == null
                || TextUtils.isEmpty(companionEndcard.icon_url)
                || TextUtils.isEmpty(companionEndcard.title)
                || (TextUtils.isEmpty(companionEndcard.desc) && companionEndcard.score < 1f)) {

            SigmobLog.e("ompanionEnd lose informations of UI Display");
            return;
        }

        int btnColor = BlueColor;
        int btnTextColor = Color.WHITE;
        int barColor = Color.WHITE;
        int barAlp = (int) (0.9 * 255);


        try {
            if (companionEndcard.button_color != null &&
                    companionEndcard.button_color.alpha > 0.01) {
                try {
                    btnColor = Color.argb((int) (companionEndcard.button_color.alpha * 255), companionEndcard.button_color.red, companionEndcard.button_color.green, companionEndcard.button_color.blue);
                } catch (Throwable throwable) {

                }
            }
            if (companionEndcard.button_text_color != null &&
                    companionEndcard.button_text_color.alpha > 0.01) {
                try {
                    btnTextColor = Color.argb((int) (companionEndcard.button_text_color.alpha * 255), companionEndcard.button_text_color.red, companionEndcard.button_text_color.green, companionEndcard.button_text_color.blue);
                } catch (Throwable throwable) {

                }
            }
            if (companionEndcard.bar_color != null &&
                    companionEndcard.bar_color.alpha > 0.01) {
                try {
                    barAlp = (int) (companionEndcard.bar_color.alpha * 255);
                    barColor = Color.rgb(companionEndcard.button_text_color.red, companionEndcard.button_text_color.green, companionEndcard.button_text_color.blue);
                } catch (Throwable throwable) {

                }
            }
        } catch (Throwable throwable) {

        }

        int clickType = 0;
        try {
            if (companionEndcard.click_type != null) {
                clickType = companionEndcard.click_type;
            }
        } catch (Throwable throwable) {

        }
        float height = 70.0f;


        String actionText = companionEndcard.button_text;
        if (TextUtils.isEmpty(actionText) || actionText.length() > 4) {

            if (getAdUnit().getInteractionType() == InterActionType.DownloadType) {
                actionText = "下载";
            } else {
                actionText = "详情";
            }
        }

        mCompanionAdsWidget = new CompanionAdsWidget(context, mAdUnit, companionEndcard.title, actionText,
                companionEndcard.score.floatValue(), companionEndcard.desc, companionEndcard.animate_type, mAdUnit.getMaterial().template_type, companionEndcard.icon_url, btnColor, btnTextColor, clickType, barColor, barAlp, height);

        mCompanionAdsWidget.setId(ClientMetadata.generateViewId());

        LinearLayout fourElementsLayout = mCompanionAdsWidget.getFourElementsLayout();
        if (fourElementsLayout != null) {
            fourElementsLayout.setClickable(true);
            fourElementsLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openFourElements(1);
                }
            });

        }

        int widthPx = context.getResources().getDisplayMetrics().widthPixels;
        int heightPx = context.getResources().getDisplayMetrics().heightPixels;


        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Math.max(widthPx, heightPx) * 3 / 5, Dips.asIntPixels(height, context));
        int margin = Dips.asIntPixels(8, context);

        if (adLogoView != null) {

            layoutParams.setMargins(margin, 0, margin, 0);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.ABOVE, adLogoView.getId());
        } else {
            int bottom = margin + mInsetBottom;

            layoutParams.setMargins(margin, 0, margin, bottom);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        }

        getLayout().addView(mCompanionAdsWidget, layoutParams);

        isShowCompanionAds = false;

        mCompanionAdsWidget.setClickable(true);
        mCompanionAdsWidget.setOnTouchListener(new View.OnTouchListener() {

            MotionEvent downEvent;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                SigmobLog.d("mCompanionAdsWidget click" + event.toString());
                if (event.getAction() == MotionEvent.ACTION_UP) {

                    PointEntitySigmobUtils.touchEventRecord(mAdUnit, event, ADEvent.AD_COMPANION_CLICK, true);

                    if (downEvent == null) {
                        downEvent = event;
                    }

                    BaseMacroCommon baseMacroCommon = mAdUnit.getMacroCommon();

                    if (baseMacroCommon instanceof SigMacroCommon) {
                        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PROGRESS_, String.valueOf(((int) getPlayProcess() * 100)));
                        ((SigMacroCommon) baseMacroCommon).updateClickMarco(downEvent, event, true);
                    }

                    coordinate = SigMacroCommon.getCoordinate(downEvent, event, true);
                    if (mVideoCompanionAdConfig == null) {
                        loadCompanionAdView();
                    }
                    mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_COMPANION;
                    mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_AD;
                    mVideoConfig.handleUrlAction(ClickUIType.COMPANION, coordinate, true);

                } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    downEvent = MotionEvent.obtain(event);

                }
                return true;
            }
        });
    }

    private boolean isValidClick;

    private void addCloseButtonWidget(final Context context, int initialVisibility) {
        if (mCloseButtonWidget == null) {
            mCloseButtonWidget = new VideoButtonWidget(context);
            mCloseButtonWidget.setId(ClientMetadata.generateViewId());
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(22, context), Dips.dipsToIntPixels(22, context));

            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.setMargins(padding, padding * 2, padding, padding);

            getLayout().addView(mCloseButtonWidget, layoutParams);
            if (mAdUnit != null && !TextUtils.isEmpty(mAdUnit.getCloseCardHtmlData()) && !isValidClick) {
                recommendDialog = new RecommendDialog(getActivity(), mAdUnit, mVideoConfig);
                recommendDialog.setOnCloseClickListener(new RecommendDialog.onCloseClickListener() {
                    @Override
                    public void onCloseClick() {
                        if (recommendDialog != null) {
                            recommendDialog.dismiss();
                            recommendDialog.destroy();
                            recommendDialog = null;
                        }
                        mIsClosing = true;
                        mVideoConfig.handleClose(getContext(), getDuration(), mAdUnit);
                        getBaseAdViewControllerListener().onFinish();
                    }
                });
            }

            final View.OnTouchListener closeOnTouchListener = new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {

                    if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                        if (!isValidClick && recommendDialog != null && !recommendDialog.isRenderFail() && !isDialogShow) {
                            recommendDialog.show();
                            isDialogShow = true;
                        } else {
                            mIsClosing = true;
                            mVideoConfig.handleClose(getContext(), getDuration(), mAdUnit);

//                    if(mVideoCompanionAdConfig != null && mCompanionAdView != null && mCompanionAdView.getVisibility() == View.VISIBLE){
//                        mVideoCompanionAdConfig.handleClose(getContext(), (int)mVideoView.getCurrentPositionWhenPlaying(),getAdUnit());
//                    }
                            getBaseAdViewControllerListener().onFinish();
                        }
                    }
                    return true;
                }
            };

            mCloseButtonWidget.setOnTouchListener(closeOnTouchListener);

            mCloseButtonWidget.updateCloseButtonIcon(mAdUnit);


            // Update custom close icon if specified in icon extensions
            final String customCloseIconUrl = mVideoConfig.getCustomCloseIconUrl();
            if (customCloseIconUrl != null) {
                mCloseButtonWidget.updateButtonIcon(customCloseIconUrl);
            }
        }
        if (mCloseButtonWidget != null) {
            mCloseButtonWidget.setVisibility(initialVisibility);
        }
        addEndCardFeedBack(getContext(), initialVisibility);


    }

//    private void addRuleLayoutWithLayoutParams(int position, RelativeLayout.LayoutParams layoutParams) {
//
//        int padding = Dips.dipsToIntPixels(10, mContext);
//
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
//        if (mSoundButtonWidget != null) {
//            layoutParams.addRule(position, mSoundButtonWidget.getId());
//            layoutParams.setMargins(0, padding * 2, 0, 0);
//        } else {
//            layoutParams.setMargins(padding, padding * 2, padding, padding);
//        }
//
//    }


    private void addSkipButtonWidget(final Context context, int initialVisibility) {

        mSkipButtonWidget = new OvalButton(context);
        mSkipButtonWidget.setId(ClientMetadata.generateViewId());
        mSkipButtonWidget.setVisibility(initialVisibility);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(22, context), Dips.dipsToIntPixels(22, context));
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        if (mSoundButtonWidget != null) {
            layoutParams.addRule(RelativeLayout.LEFT_OF, mSoundButtonWidget.getId());
            layoutParams.setMargins(0, padding * 2, 0, 0);
        }

        getLayout().addView(mSkipButtonWidget, layoutParams);

        mSkipButtonWidget.setEnabled(false);
        mSkipButtonWidget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_CLICK_SKIP, (int) mVideoView.getCurrentPositionWhenPlaying());

                    if (showbeReward()){
                        makeIsReward(false);
                    }
                    if (mIsRewarded  || mAdUnit.getConfirmDialog() == 2 || mAdUnit.getAd_type() == AdFormat.FULLSCREEN_VIDEO) {

                        getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_SKIP, (int) mVideoView.getCurrentPositionWhenPlaying());
                        mVideoConfig.handleSkip(mContext, (int) mVideoView.getCurrentPositionWhenPlaying(), getDuration(), mAdUnit);

                        stopVideoPlay(false);
                        
                    } else {
                        mVideoView.goOnPlayOnPause();
                        SigmobLog.i("videoView.pause()");

                        mDialogWidget.setduration(getRemainRewardTime());
                        mDialogWidget.setVisibility(View.VISIBLE);

                        /**
                         * 展示跳过弹窗的时候，四要素按钮不能被点击
                         */
                        if (mCompanionAdsWidget != null && mCompanionAdsWidget.getFourElementsLayout() != null) {
                            mCompanionAdsWidget.getFourElementsLayout().setClickable(false);
                        }

                }
            }
        });

        if ((mAdUnit.getSkipPercent() == 0 && mAdUnit.getSkipSeconds() < 0) || mAdUnit.getSkipSeconds() == 0) {
            showSkip(0, false);
        }

    }

    public void makeisComplete(){
        if(isComplete) return;
        isComplete = true;
        BaseMacroCommon baseMacroCommon = mAdUnit.getMacroCommon();

        ((SigMacroCommon) baseMacroCommon).addMarcoKey(SigMacroCommon._PLAYLASTFRAME_, "1");
        if (!mVideoError) {
            getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_COMPLETE, (int) mVideoView.getCurrentPositionWhenPlaying());
        }
    }
    public void stopVideoPlay(boolean isComplete) {


       if (isStoped) return;
       isStoped = true;
        dismissProgressDialog();

        if (isComplete){
            makeIsReward(isComplete);
            makeIsCharge(isComplete);
            makeisComplete();
        } else{
            if (mVideoConfig.isEnableExitOnVideoClose()) {
                getBaseAdViewControllerListener().onFinish();
                return;
            }

        }

        if (!isAutoRemoveVideoView) {

            if (isComplete) {
                updateVideoImageView();
            } else {
                updateVideoImageView((int) mVideoView.getCurrentPositionWhenPlaying());

            }

        }

        // Only fire the completion tracker if we hit all the progress marks. Some SigmobAndroid implementations
        // fire the completion event even if the whole video isn't watched.

        mVideoView.stopVideo();


        makeCloseInteractable();
        mIsVideoFinishedPlaying = true;

        // Show companion ad if available
        if (mExternalViewabilitySessionManager != null) {
            mExternalViewabilitySessionManager.recordDisplayEvent(ADEvent.AD_SHOW, 0);
        }
    }

    private void makeIsCharge(boolean isComplete) {
        if (mIsCharge || mAdUnit.getAd_type() == AdFormat.FULLSCREEN_VIDEO) return;
        mIsCharge = true;
        int position = isComplete ? (int) getDuration() : (int) mVideoView.getCurrentPositionWhenPlaying();
        getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_FINISH, position);
        mVideoConfig.handleFinish(getContext().getApplicationContext(), position, getDuration(), getAdUnit());

    }

    private void showSkip(int currentPosition, boolean isShowForce) {

        if (mSkipButtonWidget != null && !isSkipShow) {
            isSkipShow = true;
            mSkipButtonWidget.setText("跳过");
            mSkipButtonWidget.setEnabled(true);

            ValueAnimator valueAnimator = ValueAnimator.ofInt(Dips.dipsToIntPixels(22, getContext()), Dips.dipsToIntPixels(45, getContext())).setDuration(300);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (Integer)animation.getAnimatedValue();
                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mSkipButtonWidget.getLayoutParams();
                    layoutParams.width = value;
                    mSkipButtonWidget.setLayoutParams(layoutParams);
                    mSkipButtonWidget.invalidate();
                }
            });
            valueAnimator.start();

            mSkipButtonWidget.setVisibility(View.VISIBLE);
            getExternalViewabilitySessionManager().onVideoShowSkip(isShowForce, currentPosition);
            getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_SHOW_SKIP, currentPosition);

        }
    }

    private void updateVideoImageView(int seek) {

        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                  final  Bitmap bitmap = mMediaMetadataRetriever.getFrameAtTime(seek * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);

                  if (bitmap != null){
                      new Handler(Looper.getMainLooper()).post(new Runnable() {
                          @Override
                          public void run() {
                              videoImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                              videoImageView.setImageBitmap(bitmap);
                          }
                      });
                  }

                }
            }).start();

        } catch (RuntimeException e) {
            SigmobLog.e(e.getMessage());
        }
    }

    private void addSoundButtonWidget(final Context context, int initialVisibility) {
        mSoundButtonWidget = new VideoButtonWidget(context);
        mSoundButtonWidget.setId(ClientMetadata.generateViewId());
        mSoundButtonWidget.setVisibility(initialVisibility);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(22, context), Dips.dipsToIntPixels(22, context));

        layoutParams.addRule(RelativeLayout.ALIGN_BASELINE, videoAdFeedBack.getId());
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.setMargins(padding,padding*2, padding, 0);

        getLayout().addView(mSoundButtonWidget, layoutParams);

        final View.OnTouchListener soundOnTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {

                    if (isMute) {
                        getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_UNMUTE, (int) mVideoView.getCurrentPositionWhenPlaying());
                        int resId = ResourceUtil.getDrawableId(getContext(), "sig_image_video_unmute");
                        mSoundButtonWidget.updateButtonIcon(resId);
                        mVideoView.setMute(false);
                    } else {
                        getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_MUTE, (int) mVideoView.getCurrentPositionWhenPlaying());
                        int resId = ResourceUtil.getDrawableId(getContext(), "sig_image_video_mute");
                        mSoundButtonWidget.updateButtonIcon(resId);
                        mVideoView.setMute(true);
                    }
                    isMute = !isMute;

                }
                return true;
            }
        };

        mSoundButtonWidget.setOnTouchListener(soundOnTouchListener);
        if (mAdUnit.getIsMute() != 0) {
            int resId = ResourceUtil.getDrawableId(getContext(), "sig_image_video_mute");
            mSoundButtonWidget.updateButtonIcon(resId);
            mVideoView.setMute(true);
            isMute = true;
        } else {
            int resId = ResourceUtil.getDrawableId(getContext(), "sig_image_video_unmute");
            mSoundButtonWidget.updateButtonIcon(resId);
        }
    }

    /**
     * Creates and lays out the webview used to display the companion ad.
     *
     * @param context                The context.
     * @param videoCompanionAdConfig The data used to populate the view.
     * @return the populated webview
     */


    private View createCompanionAdView(final Context context,
                                       final VideoCompanionAdConfig videoCompanionAdConfig,
                                       int initialVisibility) {
        Preconditions.NoThrow.checkNotNull(context);

        if (videoCompanionAdConfig == null) {
            final View emptyView = new View(context);
            emptyView.setVisibility(View.INVISIBLE);
            return emptyView;
        }

        RelativeLayout relativeLayout = new RelativeLayout(context);
        relativeLayout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(
                        MATCH_PARENT,
                        MATCH_PARENT);


        getLayout().addView(relativeLayout, 0, layoutParams);

        CreativeWebView companionView = createCompanionAdWebView(context, videoCompanionAdConfig);

        companionView.setVisibility(initialVisibility);
        final LinearLayout.LayoutParams companionAdLayout = new LinearLayout.LayoutParams(MATCH_PARENT,
                MATCH_PARENT);
        relativeLayout.addView(companionView, companionAdLayout);

//        if (adInfoDialog == null) {
//            adInfoDialog = new AdInfoDialog(context, mAdUnit.getRequestId(), mAdUnit.getAdslot_id());
//        }
//
//        companionView.setLogoClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (adInfoDialog != null) {
//                    adInfoDialog.show();
//                }
//            }
//        });
        return companionView;
    }

    int getDuration() {

        if (mVideoView == null) {
            return 0;
        }

        if (mDuration > 0) {
            return mVideoConfig.getShowDuration(mDuration);
        } else {
            return mVideoConfig.getShowDuration((int) mVideoView.getDuration());

        }

    }


    private void makeCloseInteractable() {

        if (mRewardTips != null) {
            ViewUtil.removeFromParent(mRewardTips);
        }
        if (videoAdFeedBack != null) {
            ViewUtil.removeFromParent(videoAdFeedBack);
        }

        if (actionView != null) {
            ViewUtil.removeFromParent(actionView);
        }

        loadCompanionAdView();

        if (motion != null) {
            motion.destroy();
            motion = null;
        }


        if (!isAutoRemoveVideoView && videoImageView != null) {
            ViewParent parent = videoImageView.getParent();
            if (parent != null && parent instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) parent;
                group.removeView(videoImageView);
            }
            getLayout().addView(videoImageView, 0);
        }

        AdStackManager.shareInstance().removeAdCacheVideoListener(this);


        addCloseButtonWidget(getContext(), View.VISIBLE);

        if (adLogoView != null) {
            adLogoView.setVisibility(View.GONE);
        }
        playloadPoint();
        dismissProgressDialog();
        if (mCompanionAdView == null) {
            SigmobLog.e("endcard can't show " + mAdUnit.getEndCardIndexPath());
            HashMap<String, Object> map = new HashMap<>();
            map.put("error", "endcard can't show");
            broadcastAction(IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL, map);
            mBaseAdViewControllerListener.onFinish();
        }

        if (mCompanionAdView != null) {
            mCompanionAdView.setVisibility(View.VISIBLE);
            mCompanionAdView.bringToFront();
        }

        if (mVideoViewLayout != null) {
            mVideoViewLayout.setVisibility(View.INVISIBLE);
            broadcastAction(IntentActions.ACTION_REWARDED_VIDEO_CLOSE);
        }

        if (mSkipButtonWidget != null)
        {
            ViewUtil.removeFromParent(mSkipButtonWidget);
        }

        if (mSoundButtonWidget != null){
            ViewUtil.removeFromParent(mSoundButtonWidget);
        }

        if (isProgressBarWidgetShow){
            ViewUtil.removeFromParent(mProgressBarWidget);
        }
        if (mCompanionAdsWidget != null) {
            ViewUtil.removeFromParent(mCompanionAdsWidget);
        }
        isCompanionAdVisable = true;
    }

    void loadCompanionAdView() {

        if (mCompanionAdView != null || !mAdUnit.isEndCardIndexExist()) return;

        try {
            mVideoCompanionAdConfig = BaseVideoConfig.getVideoCompanionAd(mAdUnit);
            mVideoCompanionAdConfig.setVideoConfig(mVideoConfig);
            mCompanionAdView = createCompanionAdView(getActivity(),
                    mVideoCompanionAdConfig,
                    View.INVISIBLE);

            mVideoCompanionAdConfig.setDuration(getDuration());
            addCloseButtonWidget(getContext(), View.INVISIBLE);
        } catch (Throwable throwable) {
            mVideoCompanionAdConfig = null;
            SigmobLog.e(throwable.getMessage());
            HashMap<String, Object> map = new HashMap<>();
            map.put("error", throwable.getMessage());
            broadcastAction(IntentActions.ACTION_REWARDED_VIDEO_PLAYFAIL, map);
            mBaseAdViewControllerListener.onFinish();
        }
    }

    void handleViewabilityQuartileEvent(final String adEvent) {

        if (ADEvent.AD_START.equals(adEvent)) {
            broadcastAction(IntentActions.ACTION_INTERSTITIAL_SHOW);
            broadcastAction(IntentActions.ACTION_REWARDED_VIDEO_PLAY);
        }
        if (getExternalViewabilitySessionManager() != null) {
            getExternalViewabilitySessionManager().recordDisplayEvent(adEvent, (int) mVideoView.getCurrentPositionWhenPlaying());
        }
    }

    void makeSkipInteractable() {


        if (mAdUnit.getMaterial().disable_auto_deeplink) {//auto_deepLink 不能提前加载落地页
            loadCompanionAdView();
        }

        showSkip((int) mVideoView.getCurrentPositionWhenPlaying(), false);

        // Companion ad view, set to invisible initially to have it be drawn to calculate size
//        int timeMil = getDuration() - (int) mVideoView.getCurrentPositionWhenPlaying();
        int time = getRemainRewardTime();
        if (mDialogWidget != null)
            mDialogWidget.setduration(time);

        // Close button snapped to top-right corner of screen
        // Always add last to layout since it must be visible above all other views

    }

    int getRemainRewardTime() {
        try {
            int rewardSeconds = mAdUnit.getRewardSeconds();
            if (rewardSeconds > -1) {
                if(getDuration() >0 && rewardSeconds*1000>getDuration()){
                    rewardSeconds = (int) (getDuration()/1000.f);
                }
                return (int) (rewardSeconds - (mVideoView.getCurrentPositionWhenPlaying() / 1000.0f));
            } else if (getDuration() > 0) {
                return (int) ((getDuration()/1000.0f)*(mAdUnit.getRewardPercent()*0.01)- (mVideoView.getCurrentPositionWhenPlaying() / 1000.0f));
            }
        } catch (Throwable th) {

        }
        return -1;
    }

    boolean showbeReward() {
        int remainRewardTime = getRemainRewardTime();
        return remainRewardTime <= 0;
    }

    void makeIsReward(boolean isComplete) {
        if (mAdUnit.getAd_type() == AdFormat.FULLSCREEN_VIDEO) return;

        if (mIsRewarded) {
            return;
        }
        mIsRewarded = true;
        int position = isComplete ? (int) getDuration() : (int) mVideoView.getCurrentPositionWhenPlaying();
        broadcastAction(IntentActions.ACTION_REWARDED_VIDEO_COMPLETE);
        getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_REWARD, position);
        mVideoConfig.handleFinish(getContext().getApplicationContext(), position, getDuration(), getAdUnit());

    }


    private void openFourElements(final int type) {
        if (downloadDialog == null) {
            downloadDialog = new DownloadDialog(getActivity(), mAdUnit);
            downloadDialog.setOnPrivacyClickListener(new DownloadDialog.onPrivacyClickListener() {
                @Override
                public void onCloseClick() {
                    if (downloadDialog != null) {
                        downloadDialog.dismiss();
                        downloadDialog.destroy();
                        downloadDialog = null;
                        isElementDialogShow = false;
                    }
                    /**
                     * 四要素消失的时候恢复视频，如果非endCard页面
                     */
                    if (mVideoView != null && type == 1) {
                        mVideoView.goOnPlayOnResume();
                    }
                    if (motion != null) {
                        motion.start();
                    }

                    getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_CLOSE, (int) mVideoView.getCurrentPositionWhenPlaying());
                }

                @Override
                public void onButtonClick(String url, String clickCoordinate) {//执行点击逻辑
                    if (mVideoCompanionAdConfig == null) {
                        loadCompanionAdView();
                    }
                    mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
                    mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_APPINFO;
                    mVideoConfig.handleUrlFourAction(ClickUIType.ENDCARD, url, clickCoordinate, true);
                }

                @Override
                public void onShowSuccess() {
                    /**
                     * 四要素出来的时候暂停视频，如果非endCard页面
                     */
                    if (mVideoView != null && type == 1) {
                        mVideoView.goOnPlayOnPause();
                    }
                    if (motion != null) {
                        motion.pause();
                    }
                    getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_FOUR_ELEMENTS_SHOW, (int) mVideoView.getCurrentPositionWhenPlaying());
                }
            });
        }

        if (downloadDialog != null && downloadDialog.isRenderSuccess() && !isElementDialogShow) {
            downloadDialog.show();
            isElementDialogShow = true;
        }
    }

    private CreativeWebView createCompanionAdWebView(final Context context,
                                                     final VideoCompanionAdConfig videoCompanionAdConfig) {
        Preconditions.NoThrow.checkNotNull(context);
        Preconditions.NoThrow.checkNotNull(videoCompanionAdConfig);
        Preconditions.NoThrow.checkNotNull(videoCompanionAdConfig.getCreativeResource());


        final CreativeWebView companionView = CreativeWebView.createView(context,
//                videoCompanionAdConfig.getCreativeResource(),
                mAdUnit.getAd_source_logo(),
//                isAutoRemoveVideoView,
                false,
                mAdUnit.getInvisibleAdLabel());

        companionView.addJavascriptInterface(new SdkVersionJS(), "sigVersion");

        // For javascript, HTML, and IFrames, ignore the traditional clickthrough url and open all
        // new urls in the Sigmob Browser. For static images, use the clickthrough url specified in
        // the VAST document. These two handleClicks make it so that the correct behavior happens
        // in these special cases. onVastWebViewClick is called in both circumstances to fire the
        // click trackers.

        companionView.setWebViewClickListener(new CreativeWebView.WebViewClickListener() {

            @Override
            public void onWebViewClick(MotionEvent downEvent, MotionEvent upEvent) {

                if (mCloseButtonWidget.getVisibility() != View.VISIBLE || mIsClosing) {
                    SigmobLog.w("ignore invalid click");
                    return;
                }
                PointEntitySigmobUtils.touchEventRecord(mAdUnit, upEvent, PointCategory.ENDCARDCLICK, true);


                BaseMacroCommon baseMacroCommon = mAdUnit.getMacroCommon();
                if (baseMacroCommon instanceof SigMacroCommon) {
                    ((SigMacroCommon) baseMacroCommon).updateClickMarco(downEvent, upEvent, true);
                    coordinate = ((SigMacroCommon) baseMacroCommon).getCoordinate();
                }

                isEndCardtouched = true;

                if (mAdUnit.getMaterial().click_type == ClickType.FullScreen.getValue()
                        && !TextUtils.isEmpty(mAdUnit.getMaterial().landing_page)
                        && mAdUnit.getMaterial().creative_type != CreativeType.CreativeTypeVideo_EndCardURL.getCreativeType()) {
                    mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_MATERIAL;
                    mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_ENDCARD;
                    mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, coordinate, true);
                } else {
                }
            }
        });

        companionView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {


                if (!isEndCardtouched) return;

                if (TextUtils.isEmpty(mAdUnit.getMaterial().landing_page)
                        || mAdUnit.getInteractionType() == InterActionType.DownloadType) {
                    mAdUnit.setCustomLandPageUrl(url);
                }
                mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_COMPANION;
                mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_ENDCARD;
                mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, coordinate, true);

                SigmobLog.d("onDownloadStart() called with: url = [" + url + "], userAgent = [" + userAgent + "], contentDisposition = [" + contentDisposition + "], mimetype = [" + mimetype + "], contentLength = [" + contentLength + "]");
            }
        });

        companionView.setAdUnit(mAdUnit);

        companionView.setWebViewClient(new SigmobWebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                SigmobLog.d("onPageStarted: ");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                SigmobLog.d("onPageFinished: ");
                endcard_loading_state = "done";
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);

                if (!failingUrl.startsWith("http://")) {
                    endcard_loading_state = "error";
                }

            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    String host = request.getUrl().getHost();
                    if (!TextUtils.isEmpty(host) && host.equals("localhost")) {
                        endcard_loading_state = "error";

                    }
                }

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                try {
                    SigmobLog.d("load url " + url);
                    Uri uri = Uri.parse(url);
                    if (uri.getScheme().equalsIgnoreCase(uri.getScheme())) {
                        String host = uri.getHost();
                        if (!TextUtils.isEmpty(host)) {
                            if (("track".equals(host) || "active".equals(host))) {
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
                            } else if ("openFourElements".equals(host)) {//打开四要素
                                openFourElements(2);
                                return true;
                            }
                        }
                    }

                    if (mIsClosing) return true;

                    if (mAdUnit.getMaterial().disable_auto_deeplink) {//auto_deepLink

                        if (!isEndCardtouched) {
                            if (!TextUtils.isEmpty(url) && url.startsWith("http")) {
                                return false;//系统自己处理
                            } else {
                                return true;//啥也不干
                            }
                        }
                    }




                    if (mAdUnit.getMaterial().click_type == ClickType.Button.getValue()
                            || TextUtils.isEmpty(mAdUnit.getMaterial().landing_page)
                            || mAdUnit.getMaterial().creative_type == CreativeType.CreativeTypeVideo_EndCardURL.getCreativeType()) {

                        mAdUnit.getClickCommon().click_area = ClickCommon.CLICK_AREA_BTN;
                        mAdUnit.getClickCommon().click_scene = ClickCommon.CLICK_SCENE_ENDCARD;

                        if (mCloseButtonWidget.getVisibility() == View.VISIBLE && !TextUtils.isEmpty(uri.getScheme()) && !uri.getScheme().equals("http") && !uri.getScheme().equals("https")) {

                            if (StringUtil.scheme().equalsIgnoreCase(uri.getScheme())
                                    && !TextUtils.isEmpty(uri.getHost())
                                    && uri.getHost().equalsIgnoreCase("download")
                                    && !TextUtils.isEmpty(mAdUnit.getMaterial().landing_page)) {

                                mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, coordinate, true);

                            } else {

                                try {
                                    List<String> scheme_white_list = mAdUnit.getAdSetting() != null ? mAdUnit.getAdSetting().scheme_white_list : null;

                                    if (scheme_white_list != null && scheme_white_list.size() > 0) {
                                        for (int i = 0; i < scheme_white_list.size(); i++) {
                                            String scheme = scheme_white_list.get(i);
                                            if (url.startsWith(scheme) || scheme.equals("*")) {//通配符
                                                mAdUnit.setCustomDeeplink(url);
                                                mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, coordinate, true);
                                                return true;
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

//                                mAdUnit.setCustomDeeplink(url);
//                                mVideoConfig.handleUrlAction(ClickUIType.ENDCARD, coordinate, true);
                            }

                        } else if (!TextUtils.isEmpty(uri.getScheme()) && (uri.getScheme().equals("http") || uri.getScheme().equals("https"))) {

                            if (isTouched) {
//                              //无endCard(endCard是url)不再上报点击
//                                videoCompanionAdConfig.sendClickTracking(mAdUnit);
//                                PointEntitySigmobUtils.eventRecord(ClickUIType.ENDCARD, PointCategory.CLICK, mAdUnit, Constants.FAIL, url, coordinate, getDuration());
                                onHandleClick();
                                isTouched = false;
                            }

                            view.loadUrl(url);
                        }

                    } else if (mAdUnit.getMaterial().click_type == ClickType.FullScreen.getValue() && TextUtils.isEmpty(mAdUnit.getMaterial().landing_page)) {
                        return true;
                    }

                } catch (Throwable t) {
                    SigmobLog.e("webview");

                }

                return true;
            }
        });

        videoCompanionAdConfig.getCreativeResource().initializeWebView(companionView);

        companionView.addSigAndroidAPK(null);

        return companionView;

    }

    private void onHandleClick() {
//        //无endCard(endCard是url)不再上报点击
//        if (mAdUnit != null && mAdUnit.getMaterial().creative_type == CreativeType.CreativeTypeVideo_EndCardURL.getCreativeType()) {
//            return;
//        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                broadcastAction(IntentActions.ACTION_INTERSTITIAL_CLICK);
            }
        });

    }

    @Override
    public void cache_endCard_success(BaseAdUnit adUnit) {
        if (!adUnit.getUuid().equals(mAdUnit.getUuid())) return;

        if (adUnit.isEndCardIndexExist()) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    loadCompanionAdView();
                }
            });

        }


    }

    @Override
    public void cache_endCard_failed(BaseAdUnit adUnit) {

    }

    @Override
    public void onAdClick(boolean isRecord, ClickUIType type) {
        isValidClick = true;
        onHandleClick();

        if (!isRecord) {
            return;
        }

        switch (type) {
            case COMPANION: {

                ClickCommon clickCommon = mAdUnit.getClickCommon();
                PointEntitySigmobUtils.eventRecord(ClickUIType.COMPANION,
                        PointCategory.CLICK,
                        mAdUnit, clickCommon.isDeeplink,
                        clickCommon.clickUrl, clickCommon.clickCoordinate, getDuration());

                getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_COMPANION_CLICK, (int) mVideoView.getCurrentPositionWhenPlaying());

            }
            break;
            case VIDEO_CLICK: {
                ClickCommon clickCommon = mAdUnit.getClickCommon();
                PointEntitySigmobUtils.eventRecord(ClickUIType.VIDEO_CLICK,
                        PointCategory.CLICK,
                        mAdUnit, clickCommon.isDeeplink,
                        clickCommon.clickUrl, clickCommon.clickCoordinate, getDuration());

                getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_VIDEO_CLICK, (int) mVideoView.getCurrentPositionWhenPlaying());

            }
            break;
            case MOTION: {
                getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_MOTION_CLICK, (int) mVideoView.getCurrentPositionWhenPlaying());
            }
            break;
            default: {
                ClickCommon clickCommon = mAdUnit.getClickCommon();
                PointEntitySigmobUtils.eventRecord(ClickUIType.ENDCARD,
                        PointCategory.CLICK,
                        mAdUnit, clickCommon.isDeeplink,
                        clickCommon.clickUrl, clickCommon.clickCoordinate, getDuration());

                getExternalViewabilitySessionManager().recordDisplayEvent(ADEvent.AD_CLICK, (int) mVideoView.getCurrentPositionWhenPlaying());

            }
            break;
        }
    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }

    @Override
    public void onCacheUnavailable(String url, Throwable throwable) {
        SigmobLog.e("url",throwable);

        SigmobError(PointCategory.VIDEO,0,throwable.getMessage(),mAdUnit);
        stopVideoPlay(false);
    }


    static class SdkVersionJS extends Object {

        @JavascriptInterface
        public String getSdkVersion() {
            return WindConstants.SDK_VERSION;
        }

        @JavascriptInterface
        public int getInteractionType() {
            return mInteractionType;
        }
    }
}
