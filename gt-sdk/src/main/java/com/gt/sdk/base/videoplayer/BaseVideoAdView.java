package com.gt.sdk.base.videoplayer;

import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_END;
import static android.media.MediaPlayer.MEDIA_INFO_BUFFERING_START;
import static android.media.MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_AUTO_COMPLETE;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_BUFFERING_END;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_BUFFERING_START;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_ERROR;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_IDLE;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_NORMAL;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_PAUSE;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_PLAYING;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_PREPARED;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_PREPARING;
import static com.gt.sdk.base.videoplayer.VIDEO_PLAYER_STATE.STATE_STOP;

import android.content.Context;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.natives.SigAdView;

import java.lang.reflect.Constructor;
import java.util.Timer;
import java.util.TimerTask;

public class BaseVideoAdView extends RelativeLayout implements VideoPlayerListener {

    VideoPlayerMediaInterface mediaInterface;
    Class mediaInterfaceClass;
    VideoPlayerTextureView textureView;

    public VideoPlayerDataSource videoPlayerDataSource;

    public VIDEO_PLAYER_STATE state = VIDEO_PLAYER_STATE.STATE_IDLE;
    private ViewGroup textureViewContainer;
    private int seekToInAdvance;
    private Timer update_progress_timer;
    private ProgressTimerTask progressTimerTask;
    private boolean wait_loading;
    private boolean isVideoStarted;
    private Handler mHandler;
    private boolean mIsMute;
    private boolean preloading;
    private int mVideoHeight;
    private int mVideoWidth;
    private float videoRotation;
    private VideoPlayerStatusListener statusListener;
    private int errorCode;
    private String errorMessage;
    private final static int what_wait = 6000;
    private boolean looping;

    public BaseVideoAdView(Context context) {
        super(context);

        mHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case what_wait: {
                        if (!wait_loading) {
                            wait_loading = true;
                            setState(STATE_BUFFERING_START);
                        }

                    }
                    break;
                }
            }
        };
        textureViewContainer = new FrameLayout(context);
        textureViewContainer.setBackgroundColor(Color.BLACK);
        addView(textureViewContainer, new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }


    public void setTextureViewContainer(ViewGroup textureViewContainer) {
        this.textureViewContainer = textureViewContainer;
    }

    public void startVideo() {
        SigmobLog.d("startVideo [" + this.hashCode() + "] ");

        try {
            initMediaPlayerInterface();

        } catch (Throwable e) {
            e.printStackTrace();
        }
        Window window = SigUtils.getWindow(getContext());
        if (window != null) {
            window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        addTextureView();
        onStatePreparing();
    }

    public void onStatePreparing() {
        SigmobLog.d("onStatePreparing " + " [" + this.hashCode() + "] ");
        setState(STATE_PREPARING);
//        mediaInterface.prepareAsync();

    }

    public void setUp(String url) {
        setUp(new VideoPlayerDataSource(url, null), SigAdView.SCREEN_NORMAL);
    }

    public void setUp(VideoPlayerDataSource videoPlayerDataSource, int screen) {
        this.videoPlayerDataSource = videoPlayerDataSource;
        onStateNormal();
    }

    public void onStateNormal() {
        SigmobLog.d("onStateNormal " + "stat" + state + " [" + this.hashCode() + "] ");
        setState(STATE_NORMAL);
        if (mediaInterface != null) mediaInterface.release();
    }


    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public int getVideoHeight() {
        return mVideoHeight;
    }

    public int getVideoWidth() {
        return mVideoWidth;
    }

    public void goOnPlayOnResume() {

        preloading = false;
        if (state != STATE_NORMAL && mediaInterface != null) {
            if (state == STATE_PAUSE || state == STATE_BUFFERING_END || state == STATE_AUTO_COMPLETE || state == STATE_PREPARED) {
                mediaInterface.start();
                onStatePlaying();
            }
        } else {
            startVideo();
        }
    }


    public void onStatePlaying() {
        SigmobLog.d("onStatePlaying " + " [" + this.hashCode() + "] ");
        if (state == STATE_PREPARED) {//如果是准备完成视频后第一次播放，先判断是否需要跳转进度。
            if (seekToInAdvance != 0) {
                mediaInterface.seekTo(seekToInAdvance);
                seekToInAdvance = 0;
            }
            if (!isVideoStarted) {
                mHandler.sendEmptyMessageDelayed(what_wait, 1500);
            }
        }

        setState(STATE_PLAYING);
        startProgressTimer();
    }

    public void setVideoPlayerStatusListener(VideoPlayerStatusListener listener) {

        statusListener = listener;
    }

    public void setState(VIDEO_PLAYER_STATE state) {
        this.state = state;

        if (statusListener != null) {
            statusListener.OnStateChange(state);
        }
    }

    public void startProgressTimer() {
        SigmobLog.d("startProgressTimer: " + " [" + this.hashCode() + "] ");
        cancelProgressTimer();
        update_progress_timer = new Timer();
        progressTimerTask = new ProgressTimerTask();
        update_progress_timer.schedule(progressTimerTask, 0, 300);
    }

    public long getCurrentPositionWhenPlaying() {
        long position = 0;
        if (state == STATE_PLAYING || state == STATE_PAUSE) {
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
            if (mediaInterface != null) {
                duration = mediaInterface.getDuration();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            return duration;
        }
        return duration;
    }

    public void setLooping(boolean looping) {
        try {
            if (mediaInterface != null) {
                mediaInterface.setLooping(looping);
                this.looping = looping;
            }
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    public boolean isLooping() {
        return looping;
    }


    public class ProgressTimerTask extends TimerTask {
        @Override
        public void run() {
            if (state == STATE_PLAYING || state == STATE_PAUSE) {
//                Log.v(TAG, "onProgressUpdate " + "[" + this.hashCode() + "] ");
                post(new Runnable() {
                    @Override
                    public void run() {
                        long position = getCurrentPositionWhenPlaying();
                        long duration = getDuration();
                        if (statusListener != null) {
                            statusListener.OnProgressUpdate(position, duration);
                        }
                    }
                });
            }
        }
    }

    /**
     * 多数表现为中断当前播放
     */
    public void reset() {
        SigmobLog.d("reset " + " [" + this.hashCode() + "] ");

        cancelProgressTimer();
        onStateNormal();
        textureViewContainer.removeAllViews();


        Window window = SigUtils.getWindow(getContext());
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (mediaInterface != null) mediaInterface.release();
    }

    public void setMute(boolean isMute) {

        mIsMute = isMute;
        if (isMute) {
            if (mediaInterface != null) {
                mediaInterface.setVolume(0);
            }
        } else {
            if (mediaInterface != null) {
                mediaInterface.setVolume(1.0f);
            }
        }

    }

    public void stopVideo() {
        setState(STATE_STOP);
        reset();
    }

    public void goOnPlayOnPause() {
        if (state == STATE_NORMAL || state == STATE_PREPARING || state == STATE_ERROR) {
            reset();
        } else if (state != STATE_PAUSE && mediaInterface != null) {
            onStatePause();
            mediaInterface.pause();
        }
    }

    public void onStatePause() {
        SigmobLog.i("onStatePause " + " [" + this.hashCode() + "] ");
        setState(STATE_PAUSE);

        startProgressTimer();
    }


    public void cancelProgressTimer() {
        if (update_progress_timer != null) {
            update_progress_timer.cancel();
        }
        if (progressTimerTask != null) {
            progressTimerTask.cancel();
        }
    }

    public void addTextureView() {
        SigmobLog.d("addTextureView [" + this.hashCode() + "] ");
        if (textureView != null) textureViewContainer.removeView(textureView);
        textureView = new VideoPlayerTextureView(getContext().getApplicationContext());

        textureView.setSurfaceTextureListener(mediaInterface);

        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, Gravity.CENTER);
        textureViewContainer.addView(textureView, layoutParams);
    }


    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        SigmobLog.d("onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

    }

    private void initMediaPlayerInterface() {
        try {

            if (mediaInterface != null) mediaInterface.release();

            if (mediaInterfaceClass == null) {
                this.mediaInterface = new VideoPlayerMediaSystem(this);
            } else {
                Constructor<VideoPlayerMediaInterface> constructor = null;
                constructor = mediaInterfaceClass.getConstructor(BaseVideoAdView.class);
                this.mediaInterface = constructor.newInstance(this);
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

    public void startPreloading() {
        preloading = true;
        startVideo();
    }

    public VideoPlayerDataSource getVideoPlayerDataSource() {
        return videoPlayerDataSource;
    }

    public void onPrepared() {
        SigmobLog.d("onPrepared " + " [" + this.hashCode() + "] ");
        setState(STATE_PREPARED);

        setMute(mIsMute);
        if (!preloading) {
            SigmobLog.d("mediaInterface start");
            onStatePlaying();
            mediaInterface.start();//这里原来是非线程
            preloading = false;
        }
    }


    public void seekTo(int seek) {
        if (state == STATE_PAUSE || state == STATE_PREPARED || state == STATE_AUTO_COMPLETE || state == STATE_PLAYING) {
            if (mediaInterface != null) {
                mediaInterface.seekTo(seek);
            }
        } else {
            seekToInAdvance = seek;
        }

    }

    public void onAutoCompletion() {

        setState(STATE_AUTO_COMPLETE);

    }

    @Override
    public void setBufferProgress(int percent) {

    }

    @Override
    public void onSeekComplete() {

    }

    @Override
    public void onError(int what, int extra) {
        errorCode = what;

        errorMessage = "" + extra;
        setState(STATE_ERROR);

    }

    @Override
    public void onInfo(int what, int extra) {

        SigmobLog.d("onInfo() called with: what = [" + what + "], extra = [" + extra + "]");
        switch (what) {
            case MEDIA_INFO_VIDEO_RENDERING_START: {

                if (!isVideoStarted) {
                    isVideoStarted = true;
                    mHandler.removeMessages(what_wait);
                }
                if (wait_loading) {
                    wait_loading = false;
                    setState(STATE_BUFFERING_END);
                }
            }
            break;
            case MEDIA_INFO_BUFFERING_END: {
                setState(STATE_BUFFERING_END);
            }
            break;
            case MEDIA_INFO_BUFFERING_START: {
                setState(STATE_BUFFERING_START);
            }
            break;
        }
    }

    @Override
    public void onVideoSizeChanged(int width, int height) {
        mVideoHeight = height;
        mVideoWidth = width;
        SigmobLog.d("onVideoSizeChanged " + " [" + this.hashCode() + "] ");
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

    public void destroy() {
        statusListener = null;
        reset();
    }
}
