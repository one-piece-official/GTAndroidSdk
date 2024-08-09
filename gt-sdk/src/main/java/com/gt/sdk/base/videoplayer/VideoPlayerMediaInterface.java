package com.gt.sdk.base.videoplayer;

import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.view.Surface;
import android.view.TextureView;

/**
 * Created by Nathen on 2017/11/7.
 * 自定义播放器
 */
public abstract class VideoPlayerMediaInterface implements TextureView.SurfaceTextureListener {

    public SurfaceTexture SAVED_SURFACE;
    public Handler mMediaHandler;
    public Handler handler;
    public VideoPlayerListener mVideoPlayerListener;


    public VideoPlayerMediaInterface(VideoPlayerListener videoPlayerListener) {
        this.mVideoPlayerListener = videoPlayerListener;
    }

    public abstract void start();

    public abstract void prepareAsync();

    public abstract void prepare();

    public abstract void pause();

    public abstract boolean isPlaying();

    public abstract void seekTo(long time);

    public abstract void release();

    public abstract long getCurrentPosition();

    public abstract int getVideoWidth();

    public abstract int getVideoHeight();

    public abstract long getDuration();

    public abstract void setVolume(float leftVolume);

    public abstract void setSpeed(float speed);

    public abstract void setSurface(Surface surface);

    public abstract void setLooping(boolean loop);
}
