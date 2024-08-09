package com.gt.sdk.base.videoplayer;

public interface VideoPlayerStatusListener {
     void OnStateChange(VIDEO_PLAYER_STATE state);
     void OnProgressUpdate(long position, long duration);
}
