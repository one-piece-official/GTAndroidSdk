package com.sigmob.sdk.videoplayer;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.czhj.sdk.common.utils.IntentUtil;
import com.czhj.sdk.common.utils.ResourceUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.nativead.SigAdVideoStatusListener;
import com.sigmob.sdk.nativead.SigAdView;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAds;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Nathen on 16/7/30.
 */
public class VideoAdView extends RelativeLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener, VideoPlayerListener {

    public static final String TAG = "VideoPlayerView";
    public static final int STATE_IDLE = -1;
    public static final int STATE_NORMAL = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARING_CHANGING_URL = 2;
    public static final int STATE_PREPARED = 3;
    public static final int STATE_PLAYING = 4;
    public static final int STATE_PAUSE = 5;
    public static final int STATE_AUTO_COMPLETE = 6;
    public static final int STATE_ERROR = 7;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_PARENT = 1;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_FILL_SCROP = 2;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_ORIGINAL = 3;
    public static final int VIDEO_IMAGE_DISPLAY_TYPE_ADAPTER = 0;//DEFAULT

    public static final int THRESHOLD = 80;
    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    public static boolean TOOL_BAR_EXIST = true;
    public static int FULLSCREEN_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
    public static int NORMAL_ORIENTATION = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    public static boolean SAVE_PROGRESS = false;
    public static boolean WIFI_TIP_DIALOG_SHOWED = true;
    public static int VIDEO_IMAGE_DISPLAY_TYPE = 0;
    public VideoAdView currentVideoAdView;
    public int state = -1;
    public int screen = -1;
    public VideoPlayerDataSource videoPlayerDataSource;
    public int widthRatio = 0;
    public int heightRatio = 0;
    public Class mediaInterfaceClass;
    public boolean isVideoClickAble = true;
    public VideoPlayerMediaInterface mediaInterface;
    public int positionInList = -1;//很想干掉它
    public int videoRotation = 0;
    public int seekToManulPosition = -1;
    public long seekToInAdvance = 0;
    public ImageView startImg;
    public ImageView fullscreenImg;
    public ImageView soundImg;
    public boolean preloading = false;
    protected long gobakFullscreenTime = 0;//这个应该重写一下，刷新列表，新增列表的刷新，不打断播放，应该是个flag
    protected Timer UPDATE_PROGRESS_TIMER;
    protected int mScreenWidth;
    protected int mScreenHeight;
    protected AudioManager mAudioManager;
    protected ProgressTimerTask mProgressTimerTask;
    protected boolean mTouchingProgressBar;
    VideoPlayerTextureView textureView;
    private ViewGroup textureViewContainer, startLayout, fullscreenLayout, mReplayLayout, soundLayout, mBigReplayLayout, mBackLayout;
    private ViewGroup mAppContainer;
    private ProgressBar progressBar;
    private ImageView mThumbView;
    public AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {//是否新建个class，代码更规矩，并且变量的位置也很尴尬
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    releaseAllVideos();
                    SigmobLog.i("AUDIOFOCUS_LOSS [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    try {
//                        VideoAdView player = currentVideoAdView;

//                        if (player != null && player.state == VideoPlayerView.STATE_PLAYING) {
//                            player.startButton.performClick();
//                        }
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                    SigmobLog.i("AUDIOFOCUS_LOSS_TRANSIENT [" + this.hashCode() + "]");
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    break;
            }
        }
    };
    private SigAdVideoStatusListener mVideoAdStatusListener;
    private boolean isVideoMute = true;
    private OnVideoAdViewListener videoAdViewListener;
    private ImageView mBlurImageView;
    private int mVideoHeight;
    private int mVideoWidth;
    private VolumeKeyReceiver keyReceiver;
    private boolean mEnableVideoThumbImage = false;
    private View mToplayout;
    private boolean isLoading;
    private View mBottomlayout;


    public VideoAdView(Context context) {
        super(context.getApplicationContext());
        init(context.getApplicationContext());
    }

    public VideoAdView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public int getLayoutId() {
        return ResourceUtil.getLayoutId(getContext(), "sig_video_player_layout");
    }

    public void init(Context context) {
        View.inflate(context, getLayoutId(), this);

        soundLayout = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_sound_rl"));
        startLayout = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_start_rl"));
        fullscreenLayout = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_fullscreen_rl"));
        mReplayLayout = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_replay_rl"));

        soundImg = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_sound_btn"));
        startImg = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_start_btn"));
        fullscreenImg = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_fullscreen_btn"));

        textureViewContainer = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_surface_container"));
        mAppContainer = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_app_container"));
        progressBar = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_bottom_progress"));

        mBigReplayLayout = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_big_replay"));

        mThumbView = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_thumb"));
        mBlurImageView = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_blurImageView"));

        mBackLayout = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_back_rl"));
        mToplayout = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_layout_top"));

        mBottomlayout = findViewById(ResourceUtil.getId(getContext(), "sig_native_video_layout_bottom"));

        mReplayLayout.setOnClickListener(this);
        soundLayout.setOnClickListener(this);
        startLayout.setOnClickListener(this);
        fullscreenLayout.setOnClickListener(this);
        mBigReplayLayout.setOnClickListener(this);

        mScreenWidth = getContext().getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getContext().getResources().getDisplayMetrics().heightPixels;


        keyReceiver = new VolumeKeyReceiver(new VolumeChangeListener() {

            @Override
            public void onVolumeChanged(int volume) {
                if (volume == 0) {
                    soundImg.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_mute"));
                } else {
                    if (isVideoMute) {
                        soundImg.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_mute"));
                    } else {
                        soundImg.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_unmute"));
                    }
                }
            }
        });
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.media.VOLUME_CHANGED_ACTION");
        IntentUtil.registerReceiver(context,keyReceiver, intentFilter);

        state = STATE_IDLE;
    }

    public void destroy() {
        releaseAllVideos();
        cancelProgressTimer();
        if (keyReceiver != null) {
            getContext().unregisterReceiver(keyReceiver);
            keyReceiver = null;
        }

    }

    public Bitmap getTextureBitmap() {
        if (textureView != null) {
            return textureView.getBitmap();
        }
        return null;
    }

    private void initMediaPlayerInterface() {
        try {

            if (mediaInterface != null) mediaInterface.release();

            if (mediaInterfaceClass == null) {
                this.mediaInterface = new VideoPlayerMediaSystem(this);
            } else {
                Constructor<VideoPlayerMediaInterface> constructor = null;
                constructor = mediaInterfaceClass.getConstructor(VideoAdView.class);
                this.mediaInterface = constructor.newInstance(this);
            }


        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public void setRotation(float rotation) {

//        super.setRotation(rotation);

        textureView.setRotation(rotation);
    }

    private void setParentViewVisible(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    public ImageView getBlurImageView() {
        return mBlurImageView;
    }

    public void showFullScreenButton(boolean show) {
        if (show) {
            setParentViewVisible(fullscreenLayout, VISIBLE);
        } else {
            setParentViewVisible(fullscreenLayout, INVISIBLE);
        }
    }

    public void showButton(VideoAdButton button, boolean isShow) {
        switch (button) {
            case START: {
                setParentViewVisible(startLayout, isShow ? VISIBLE : INVISIBLE);
            }
            break;
            case RETRY: {
                setParentViewVisible(mReplayLayout, isShow ? VISIBLE : INVISIBLE);
            }
            break;
            case VOLUME: {
                setParentViewVisible(soundLayout, isShow ? VISIBLE : INVISIBLE);
            }
            break;
            case FULLSCREEN: {
                setParentViewVisible(fullscreenLayout, isShow ? VISIBLE : INVISIBLE);
            }
            break;
            case BIGRETRY: {
                setParentViewVisible(mBigReplayLayout, isShow ? VISIBLE : INVISIBLE);
            }
            break;
            case BACK: {

                setParentViewVisible(mBackLayout, isShow ? VISIBLE : INVISIBLE);
            }
            break;
        }
    }

    public int getVideoSurferViewHeight() {

        if (textureView != null) {

            return textureView.getHeight();
        }
        return 0;
    }

    public void setUp(String url) {
        setUp(new VideoPlayerDataSource(url, null), SigAdView.SCREEN_NORMAL);
    }

    public void setUp(String url, String title) {
        setUp(new VideoPlayerDataSource(url, title), SigAdView.SCREEN_NORMAL);
    }

    public void setUp(String url, String title, int screen) {
        setUp(new VideoPlayerDataSource(url, title), screen);
    }

    public void setUp(VideoPlayerDataSource videoPlayerDataSource, int screen) {
        setUp(videoPlayerDataSource, screen, null);
    }

    public void setUp(String url, String title, int screen, Class mediaInterfaceClass) {
        setUp(new VideoPlayerDataSource(url, title), screen, mediaInterfaceClass);
    }

    public void setUp(VideoPlayerDataSource videoPlayerDataSource, int screen, Class mediaInterfaceClass) {
        if ((System.currentTimeMillis() - gobakFullscreenTime) < 200) return;

        this.videoPlayerDataSource = videoPlayerDataSource;
        this.screen = screen;
        onStateNormal();
        this.mediaInterfaceClass = mediaInterfaceClass;
    }

    public void setMediaInterface(Class mediaInterfaceClass) {
        reset();
        this.mediaInterfaceClass = mediaInterfaceClass;
    }

    @Override
    public void onClick(View v) {
        onTouch(v, null);
    }

    public void setSoundChange(boolean isMute) {

        isVideoMute = isMute;
        if (isMute) {
            if (mediaInterface != null) {
                mediaInterface.setVolume(0);
            }
            soundImg.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_mute"));
        } else {
            if (mediaInterface != null) {
                mediaInterface.setVolume(1.0f);
            }
            soundImg.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_unmute"));
        }

    }

    public ImageView getThumbView() {
        return mThumbView;
    }

    public ViewGroup getAppContainer() {
        return mAppContainer;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {


        if (event == null || event.getAction() == MotionEvent.ACTION_UP) {

            if (v == fullscreenLayout) {
                switch (screen) {
                    case SigAdView.SCREEN_FULLSCREEN: {
                        gotoScreenNormal();
                    }
                    break;
                    case SigAdView.SCREEN_NORMAL: {
                        gotoScreenFullscreen();

                    }
                    break;
                }
            } else if (v == soundLayout) {
                setSoundChange(!isVideoMute);
            } else if (v == mReplayLayout || v == mBigReplayLayout) {

                if (mVideoAdStatusListener != null) {
                    mVideoAdStatusListener.onVideoRestart();
                }
                startVideo();

            } else if (v == startLayout) {

                if (state == STATE_NORMAL) {
                    if (!WIFI_TIP_DIALOG_SHOWED && !videoPlayerDataSource.getCurrentUrl().toString().startsWith("file") && !
                            videoPlayerDataSource.getCurrentUrl().toString().startsWith("/") &&
                            !SigUtils.isWifiConnected(getContext())) {//这个可以放到std中
                        showWifiDialog();
                        return true;
                    }
                    startVideo();
                } else if (state == STATE_PLAYING) {
                    SigmobLog.d("pauseVideo [" + this.hashCode() + "] ");
                    mediaInterface.pause();
                    onStatePause();
                } else if (state == STATE_PAUSE) {
                    if (mVideoAdStatusListener != null) {
                        mVideoAdStatusListener.onVideoResume();
                    }

                    mediaInterface.start();
                    onStatePlaying();
                } else if (state == STATE_AUTO_COMPLETE) {
                    startVideo();
                } else if (state == STATE_PREPARED) {
                    mediaInterface.start();
                    onStatePlaying();
                    if (mVideoAdStatusListener != null) {
                        mVideoAdStatusListener.onVideoStart();
                    }
                }
                return true;
            }
        }


        return false;
    }

    public void onStateNormal() {
        SigmobLog.d("onStateNormal " + "stat" + state + " [" + this.hashCode() + "] ");
        state = STATE_NORMAL;

        setParentViewVisible(mThumbView, VISIBLE);
        setParentViewVisible(startLayout, View.VISIBLE);
        cancelProgressTimer();
        if (mediaInterface != null) mediaInterface.release();
    }

    public void onStatePreparing() {
        SigmobLog.d("onStatePreparing " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARING;
        resetProgressAndTime();
    }


    public void enableVideoThumbImage(boolean enable) {

        mEnableVideoThumbImage = enable;

    }

    @Override
    public VideoPlayerDataSource getVideoPlayerDataSource() {
        return videoPlayerDataSource;
    }

    public void onPrepared() {
        SigmobLog.d("onPrepared " + " [" + this.hashCode() + "] ");
        state = STATE_PREPARED;
        setSoundChange(isVideoMute);

        if (mVideoAdStatusListener != null) {
            mVideoAdStatusListener.onVideoLoad();
        }

        if (!preloading) {
            if (mVideoAdStatusListener != null) {
                mVideoAdStatusListener.onVideoStart();
            }

            SigmobLog.d("mediaInterface start");
            onStatePlaying();
            mediaInterface.start();//这里原来是非线程
            preloading = false;
        } else {
            if (mEnableVideoThumbImage) {
                mThumbView.setImageBitmap(textureView.getBitmap());
            }
        }

        if (videoPlayerDataSource.getCurrentUrl().toString().toLowerCase().contains("mp3") ||
                videoPlayerDataSource.getCurrentUrl().toString().toLowerCase().contains("wma") ||
                videoPlayerDataSource.getCurrentUrl().toString().toLowerCase().contains("aac") ||
                videoPlayerDataSource.getCurrentUrl().toString().toLowerCase().contains("m4a") ||
                videoPlayerDataSource.getCurrentUrl().toString().toLowerCase().contains("wav")) {
            onStatePlaying();
        }
    }

    public void startPreloading() {
        preloading = true;
        startVideo();
    }

    /**
     * 如果STATE_PREPARED就播放，如果没准备完成就走正常的播放函数startVideo();
     */
    public void startVideoAfterPreloading() {
        if (state == STATE_PREPARED) {
            mediaInterface.start();
        } else {
            preloading = false;
            startVideo();
        }
    }

    public void onStatePlaying() {
        SigmobLog.d("onStatePlaying " + " [" + this.hashCode() + "] ");
        setParentViewVisible(mThumbView, INVISIBLE);
        setParentViewVisible(startLayout, View.INVISIBLE);
        if (state == STATE_PREPARED) {//如果是准备完成视频后第一次播放，先判断是否需要跳转进度。
            if (seekToInAdvance != 0) {
                mediaInterface.seekTo(seekToInAdvance);
                seekToInAdvance = 0;
            } else {

            }
        }
        state = STATE_PLAYING;
        startProgressTimer();
    }

    public void onStatePause() {
        SigmobLog.i("onStatePause " + " [" + this.hashCode() + "] ");
        state = STATE_PAUSE;
        setParentViewVisible(startLayout, View.VISIBLE);

        if (mVideoAdStatusListener != null) {
            mVideoAdStatusListener.onVideoPause();
        }
        cancelProgressTimer();
    }

    public void onStateError() {
        SigmobLog.d("onStateError " + " [" + this.hashCode() + "] ");
        state = STATE_ERROR;
        cancelProgressTimer();
        if (screen == SigAdView.SCREEN_FULLSCREEN) {
            setParentViewVisible(startLayout, VISIBLE);
        } else {
            setParentViewVisible(mReplayLayout, VISIBLE);
        }
    }

    public void onStateAutoComplete() {
        SigmobLog.i("onStateAutoComplete " + " [" + this.hashCode() + "] ");
        state = STATE_AUTO_COMPLETE;
        cancelProgressTimer();
        if (progressBar != null) {
            progressBar.setProgress(100);
        }
    }


    public void onInfo(int what, int extra) {
        SigmobLog.d("onInfo what - " + what + " extra - " + extra);
        switch (what) {
            case MEDIA_INFO_VIDEO_RENDERING_START: {
                if (state == VideoAdView.STATE_PREPARED
                        || state == VideoAdView.STATE_PREPARING_CHANGING_URL) {

                    onStatePlaying();//真正的prepared，本质上这是进入playing状态。
                }
            }
            break;
            case MEDIA_INFO_BUFFERING_START: {
                if (state == VideoAdView.STATE_PLAYING) {
                    isLoading = true;
                    mediaInterface.pause();
                    onStatePause();
                }
                SigmobLog.d("MEDIA_INFO_BUFFERING_START");
            }
            break;
            case MEDIA_INFO_BUFFERING_END: {
                if (isLoading) {
                    isLoading = false;
                    mediaInterface.start();
                    onStatePlaying();//真正的prepared，本质上这是进入playing状态。
                }
                SigmobLog.d("MEDIA_INFO_BUFFERING_END");
            }
            break;

        }
    }

    public void onError(int what, int extra) {
        SigmobLog.e("onError " + what + " - " + extra + " [" + this.hashCode() + "] ");

        if (mVideoAdStatusListener != null) {
            mVideoAdStatusListener.onVideoError(WindAdError.ERROR_SIGMOB_PLAY_VIDEO);
        }

        if (what != 38 && extra != -38 && what != -38 && extra != 38 && extra != -19) {
            onStateError();
            mediaInterface.release();
        }
    }

    public void onAutoCompletion() {
        Runtime.getRuntime().gc();
        if (mVideoAdStatusListener != null) {
            mVideoAdStatusListener.onVideoCompleted();
        }
        SigmobLog.d("onAutoCompletion " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();
        dismissBrightnessDialog();
        dismissProgressDialog();
        dismissVolumeDialog();
        onStateAutoComplete();
//        mediaInterface.release();
        Window window = SigUtils.getWindow(getContext());
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        resetProgressAndTime();
        currentVideoAdView = null;
    }

    /**
     * 多数表现为中断当前播放
     */
    public void reset() {
        SigmobLog.d("reset " + " [" + this.hashCode() + "] ");

        cancelProgressTimer();
        resetProgressAndTime();
        dismissBrightnessDialog();
        dismissProgressDialog();
        dismissVolumeDialog();
        onStateNormal();
        textureViewContainer.removeAllViews();


        Window window = SigUtils.getWindow(getContext());
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mediaInterface != null) mediaInterface.release();
    }

    public void setState(int state) {
        setState(state, 0, 0);
    }

    public void setState(int state, int urlMapIndex, int seekToInAdvance) {//后面两个参数干嘛的
        switch (state) {
            case STATE_NORMAL:
                onStateNormal();
                break;
            case STATE_PREPARING:
                onStatePreparing();
                break;
            case STATE_PREPARING_CHANGING_URL:
                changeUrl(urlMapIndex, seekToInAdvance);
                break;
            case STATE_PLAYING:
                onStatePlaying();
                break;
            case STATE_PAUSE:
                onStatePause();
                break;
            case STATE_ERROR:
                onStateError();
                break;
            case STATE_AUTO_COMPLETE:
                onStateAutoComplete();
                break;
        }
    }

    public void setScreen(int screen) {//特殊的个别的进入全屏的按钮在这里设置  只有setup的时候能用上
        switch (screen) {
            case SigAdView.SCREEN_NORMAL:
                setScreenNormal();
                break;
            case SigAdView.SCREEN_FULLSCREEN:
                setScreenFullscreen();
                break;
            case SigAdView.SCREEN_TINY:
                setScreenTiny();
                break;
        }
    }

    public void startVideo() {
        SigmobLog.d("startVideo [" + this.hashCode() + "] ");
        setCurrentVideoAdView(this);

        try {
//            onStateNormal();

            initMediaPlayerInterface();

            setParentViewVisible(mReplayLayout, INVISIBLE);
            setParentViewVisible(mBigReplayLayout, INVISIBLE);

        } catch (Throwable e) {
            e.printStackTrace();
        }
//        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
//        mAudioManager.requestAudioFocus(onAudioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        Window window = SigUtils.getWindow(getContext());
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        addTextureView();

        onStatePreparing();
    }

    public void changeUrl(String url, String title, long seekToInAdvance) {
        changeUrl(new VideoPlayerDataSource(url, title), seekToInAdvance);
    }

    public void changeUrl(int urlMapIndex, long seekToInAdvance) {
        state = STATE_PREPARING_CHANGING_URL;
        this.seekToInAdvance = seekToInAdvance;
        videoPlayerDataSource.currentUrlIndex = urlMapIndex;
        mediaInterface.setSurface(null);
        mediaInterface.release();
        mediaInterface.prepare();
    }

    public void changeUrl(VideoPlayerDataSource videoPlayerDataSource, long seekToInAdvance) {
        state = STATE_PREPARING_CHANGING_URL;
        this.seekToInAdvance = seekToInAdvance;
        this.videoPlayerDataSource = videoPlayerDataSource;
        mediaInterface.setSurface(null);
        mediaInterface.release();
        mediaInterface.prepare();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (screen == SigAdView.SCREEN_FULLSCREEN || screen == SigAdView.SCREEN_TINY) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            return;
        }
        if (widthRatio != 0 && heightRatio != 0) {
            int specWidth = MeasureSpec.getSize(widthMeasureSpec);
            int specHeight = (int) ((specWidth * (float) heightRatio) / widthRatio);
            setMeasuredDimension(specWidth, specHeight);

            int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(specWidth, MeasureSpec.EXACTLY);
            int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(specHeight, MeasureSpec.EXACTLY);
            getChildAt(0).measure(childWidthMeasureSpec, childHeightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }

    }

    public void addTextureView() {
//        SigmobLog.d("addTextureView [" + this.hashCode() + "] ");
        if (textureView != null) textureViewContainer.removeView(textureView);
        textureView = new VideoPlayerTextureView(getContext().getApplicationContext());
        textureView.setSurfaceTextureListener(mediaInterface);

        FrameLayout.LayoutParams layoutParams =
                new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        Gravity.CENTER);
        textureViewContainer.addView(textureView, layoutParams);
    }

    public void clearFloatScreen() {
//        JZUtils.showStatusBar(getContext());
//        JZUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);
//        JZUtils.showSystemUI(getContext());

        Window window = SigUtils.getWindow(getContext());
        if (window != null) {
            ViewGroup vg = (ViewGroup) window.findViewById(android.R.id.content);

            vg.removeView(this);
        }

//        if (mediaInterface != null) mediaInterface.release();
//        currentVideoAdView = null;
    }

    public void onVideoSizeChanged(int width, int height) {
        mVideoHeight = height;
        mVideoWidth = width;
//        SigmobLog.d("onVideoSizeChanged " + " [" + this.hashCode() + "] ");
        if (textureView != null) {
            if (videoRotation != 0) {
                textureView.setRotation(videoRotation);
            }
            textureView.setVideoSize(width, height);
        }
    }

    @Override
    public void setSurfaceTexture(SurfaceTexture saved_surface) {
        if (textureView != null) {
            textureView.setSurfaceTexture(saved_surface);
        }
    }

    public void startProgressTimer() {
//        SigmobLog.d("startProgressTimer: " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();
        UPDATE_PROGRESS_TIMER = new Timer();
        mProgressTimerTask = new ProgressTimerTask();
        UPDATE_PROGRESS_TIMER.schedule(mProgressTimerTask, 0, 300);
    }

    public void cancelProgressTimer() {
        if (UPDATE_PROGRESS_TIMER != null) {
            UPDATE_PROGRESS_TIMER.cancel();
            UPDATE_PROGRESS_TIMER = null;
        }
        if (mProgressTimerTask != null) {
            mProgressTimerTask.cancel();
            mProgressTimerTask = null;
        }
    }

    public void onProgress(int progress, long position, long duration) {
//        SigmobLog.d("onProgress: progress=" + progress + " position=" + position + " duration=" + duration);
        if (!mTouchingProgressBar) {
            if (seekToManulPosition != -1) {
                if (seekToManulPosition > progress) {
                    return;
                } else {
                    seekToManulPosition = -1;//这个关键帧有没有必要做
                }
            } else {
                if (progress != 0 && progressBar != null) progressBar.setProgress(progress);
            }
        }

        if (mVideoAdStatusListener != null) {
            mVideoAdStatusListener.onProgressUpdate(position, duration);
        }

    }

    public void setBufferProgress(int bufferProgress) {
//        SigmobLog.d("setBufferProgress() called with: bufferProgress = [" + bufferProgress + "]");
//        if (bufferProgress != 0) progressBar.setSecondaryProgress(bufferProgress);
    }

    public void resetProgressAndTime() {
        if (progressBar != null) {
            progressBar.setProgress(0);
        }

    }

    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        if (state == STATE_PLAYING ||
                state == STATE_PAUSE) {
            try {
                position = mediaInterface.getCurrentPosition();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                return position;
            }
        }
        return position;
    }

    public long getDuration() {
        long duration = 0;
        try {
            if (mediaInterface != null){
                duration = mediaInterface.getDuration();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        cancelProgressTimer();
        ViewParent vpdown = getParent();
        while (vpdown != null) {
            vpdown.requestDisallowInterceptTouchEvent(true);
            vpdown = vpdown.getParent();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        SigmobLog.d("bottomProgress onStopTrackingTouch [" + this.hashCode() + "] ");
        startProgressTimer();
        ViewParent vpup = getParent();
        while (vpup != null) {
            vpup.requestDisallowInterceptTouchEvent(false);
            vpup = vpup.getParent();
        }
        if (state != STATE_PLAYING &&
                state != STATE_PAUSE) return;
        long time = seekBar.getProgress() * getDuration() / 100;
        seekToManulPosition = seekBar.getProgress();
        mediaInterface.seekTo(time);
        SigmobLog.d("seekTo " + time + " [" + this.hashCode() + "] ");
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            //设置这个progres对应的时间，给textview
            long duration = getDuration();
        }
    }

    public void cloneAJzvd(ViewGroup vg) {
        try {
            Constructor<VideoAdView> constructor = (Constructor<VideoAdView>) VideoAdView.this.getClass().getConstructor(Context.class);
            VideoAdView videoAdView = constructor.newInstance(getContext());
            videoAdView.setId(getId());
            vg.addView(videoAdView);
            videoAdView.setUp(videoPlayerDataSource.cloneMe(), SigAdView.SCREEN_NORMAL, mediaInterfaceClass);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public void gotoScreenFullscreen() {

        if (videoAdViewListener != null) {
            videoAdViewListener.onScreenFullscreen();
        }

        setScreenFullscreen();

//        SigUtils.hideStatusBar(getContext());
//        SigUtils.setRequestedOrientation(getContext(), FULLSCREEN_ORIENTATION);
//        SigUtils.hideSystemUI(getContext());//华为手机和有虚拟键的手机全屏时可隐藏虚拟键 issue:1326
        fullscreenImg.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_small"));

    }

    public ViewGroup getSigAdView() {
        ViewGroup vg = (ViewGroup) getParent();

        do {
            if (vg instanceof SigAdView) {
                break;
            }
            vg = (ViewGroup) getParent();

        } while (vg != null);


        return vg;
    }

    public void setVideoAdViewListener(OnVideoAdViewListener videoAdViewListener) {
        this.videoAdViewListener = videoAdViewListener;
    }

    public void gotoScreenNormal() {//goback本质上是goto
        gobakFullscreenTime = System.currentTimeMillis();//退出全屏
        if (videoAdViewListener != null) {
            videoAdViewListener.onScreenNormal();
        }

        setScreenNormal();//这块可以放到jzvd中
//        SigUtils.showStatusBar(getContext());
//        SigUtils.setRequestedOrientation(getContext(), NORMAL_ORIENTATION);
//        SigUtils.showSystemUI(getContext());
        fullscreenImg.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_fullscreen"));

    }

    public void setScreenNormal() {//TODO 这块不对呀，还需要改进，设置flag之后要设置ui，不设置ui这么写没意义呀
        screen = SigAdView.SCREEN_NORMAL;
    }

    public void setScreenFullscreen() {
        screen = SigAdView.SCREEN_FULLSCREEN;
    }

    public void setScreenTiny() {
        screen = SigAdView.SCREEN_TINY;
    }

    //    //重力感应的时候调用的函数，、、这里有重力感应的参数，暂时不能删除
    public void autoFullscreen(float x) {//TODO写道demo中
        if (currentVideoAdView != null
                && (state == STATE_PLAYING || state == STATE_PAUSE)
                && screen != SigAdView.SCREEN_FULLSCREEN
                && screen != SigAdView.SCREEN_TINY) {
//            if (x > 0) {
//                JZUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            } else {
//                JZUtils.setRequestedOrientation(getContext(), ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
//            }
            gotoScreenFullscreen();
        }
    }

    //TODO 是否有用
    public void onSeekComplete() {

    }

    public void showWifiDialog() {
    }

    public void showProgressDialog(float deltaX,
                                   String seekTime, long seekTimePosition,
                                   String totalTime, long totalTimeDuration) {
    }

    public void dismissProgressDialog() {

    }

    public void showVolumeDialog(float deltaY, int volumePercent) {

    }

    public void dismissVolumeDialog() {

    }

    public void showBrightnessDialog(int brightnessPercent) {

    }

    public void dismissBrightnessDialog() {

    }

    public Context getApplicationContext() {//这个函数必要吗
        Context context = getContext();
        if (context != null) {
            Context applicationContext = context.getApplicationContext();
            if (applicationContext != null) {
                return applicationContext;
            }
        }
        return context;
    }

    public void setVideoAdStatusListener(SigAdVideoStatusListener adVideoStatusListener) {
        mVideoAdStatusListener = adVideoStatusListener;
    }

    public void setHolderImageResource(int resId) {

    }

    public void goOnPlayOnResume() {
        if (currentVideoAdView != null && currentVideoAdView.state != VideoAdView.STATE_NORMAL) {
            if (currentVideoAdView.state == VideoAdView.STATE_PAUSE) {
                if (mVideoAdStatusListener != null) {
                    mVideoAdStatusListener.onVideoResume();
                }
                currentVideoAdView.onStatePlaying();
                currentVideoAdView.mediaInterface.start();
            } else if (currentVideoAdView.state == VideoAdView.STATE_PREPARED) {
                if (mVideoAdStatusListener != null) {
                    mVideoAdStatusListener.onVideoStart();
                }
                currentVideoAdView.mediaInterface.start();
                currentVideoAdView.onStatePlaying();
            }
        } else {
            startVideo();
        }
    }


    public void goOnPlayOnPause() {
        if (currentVideoAdView != null) {
            if (currentVideoAdView.state == VideoAdView.STATE_AUTO_COMPLETE ||
                    currentVideoAdView.state == VideoAdView.STATE_NORMAL ||
                    currentVideoAdView.state == VideoAdView.STATE_PREPARING ||
                    currentVideoAdView.state == VideoAdView.STATE_ERROR) {
                releaseAllVideos();
            } else if (currentVideoAdView.state != STATE_PAUSE) {
                currentVideoAdView.onStatePause();
                currentVideoAdView.mediaInterface.pause();
            }
        }
    }


    public void releaseAllVideos() {
        SigmobLog.i("releaseAllVideos");
        if (currentVideoAdView != null) {
            currentVideoAdView.reset();
            currentVideoAdView = null;
        }

    }

    public void setCurrentVideoAdView(VideoAdView videoAdView) {
//        if (currentVideoAdView != null) {
//            currentVideoAdView.reset();
//        }

        currentVideoAdView = videoAdView;
    }

    public void setTextureViewRotation(int rotation) {
        if (currentVideoAdView != null && currentVideoAdView.textureView != null) {
            currentVideoAdView.textureView.setRotation(rotation);
        }
    }

    public void setVideoImageDisplayType(int type) {
        VideoAdView.VIDEO_IMAGE_DISPLAY_TYPE = type;
        if (currentVideoAdView != null && currentVideoAdView.textureView != null) {
            currentVideoAdView.textureView.requestLayout();
        }
    }

    public void setBackClickListener(OnClickListener onClickListener) {
        if (mBackLayout != null) {
            mBackLayout.setOnClickListener(onClickListener);
        }
    }

    public View getTopLayoutView() {
        return mToplayout;
    }

    public View getBottomLayoutView() {
        return mBottomlayout;
    }

    private interface VolumeChangeListener {
        /**
         * 系统媒体音量变化
         *
         * @param volume
         */
        void onVolumeChanged(int volume);
    }

    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (state == STATE_PLAYING || state == STATE_PAUSE) {
//                Log.v(TAG, "onProgressUpdate " + "[" + this.hashCode() + "] ");
                WindAds.sharedAds().getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        long position = getCurrentPositionWhenPlaying();
                        long duration = getDuration();
                        int progress = (int) (position * 100 / (duration == 0 ? 1 : duration));
                        onProgress(progress, position, duration);
                    }
                });
            }
        }
    }

    private class VolumeKeyReceiver extends BroadcastReceiver {


        VolumeChangeListener volumeChangeListener;

        public VolumeKeyReceiver(VolumeChangeListener listener) {
            volumeChangeListener = listener;
        }

        @Override

        public void onReceive(Context context, Intent intent) {

            if (VOLUME_CHANGED_ACTION.equals(intent.getAction())
                    && (intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1) == AudioManager.STREAM_MUSIC)) {
                if (volumeChangeListener != null) {
                    AudioManager mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);

                    int volume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    if (volume >= 0) {
                        volumeChangeListener.onVolumeChanged(volume);
                    }
                }
            }
        }
    }
}
