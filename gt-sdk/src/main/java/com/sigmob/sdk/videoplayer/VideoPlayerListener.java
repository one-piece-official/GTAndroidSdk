package com.sigmob.sdk.videoplayer;

import android.graphics.SurfaceTexture;

public interface VideoPlayerListener {

    VideoPlayerDataSource getVideoPlayerDataSource();

    void onPrepared();

    void onAutoCompletion();

    void setBufferProgress(int percent);

    void onSeekComplete();

    void onError(int what, int extra);

    void onInfo(int what, int extra);

    void onVideoSizeChanged(int width, int height);

    void setSurfaceTexture(SurfaceTexture saved_surface);
}
