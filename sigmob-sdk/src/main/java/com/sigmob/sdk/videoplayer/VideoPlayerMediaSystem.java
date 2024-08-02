package com.sigmob.sdk.videoplayer;

import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Handler;
import android.view.Surface;

import com.czhj.sdk.common.ThreadPool.ThreadPoolFactory;
import com.czhj.sdk.logger.SigmobLog;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by Nathen on 2017/11/8.
 * 实现系统的播放引擎
 */
public class VideoPlayerMediaSystem extends VideoPlayerMediaInterface implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener, MediaPlayer.OnSeekCompleteListener, MediaPlayer.OnErrorListener, MediaPlayer.OnInfoListener, MediaPlayer.OnVideoSizeChangedListener {

    private static final String TAG = "VideoPlayerMediaSystem";
    public MediaPlayer mediaPlayer;
    private boolean isPrepareAsync;

    public VideoPlayerMediaSystem(VideoPlayerListener videoPlayerListener) {
        super(videoPlayerListener);
    }

    public void prepareAsync() {
        prepare();
        isPrepareAsync = true;
    }

    @Override
    public void prepare() {

        if (isPrepareAsync && mediaPlayer != null && SAVED_SURFACE != null) {
            isPrepareAsync = false;
            mediaPlayer.setSurface(new Surface(SAVED_SURFACE));
            return;
        }
        release();

        handler = new Handler();
        mMediaHandler = ThreadPoolFactory.BackgroundThreadPool.getInstance().getIOHandler();
        mMediaHandler.post(new Runnable() {
            @Override
            public void run() {

                try {
                    VideoPlayerDataSource videoPlayerDataSource = mVideoPlayerListener.getVideoPlayerDataSource();

                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

                    mediaPlayer.setLooping(videoPlayerDataSource.looping);
                    mediaPlayer.setOnPreparedListener(VideoPlayerMediaSystem.this);
                    mediaPlayer.setOnCompletionListener(VideoPlayerMediaSystem.this);
                    mediaPlayer.setOnBufferingUpdateListener(VideoPlayerMediaSystem.this);
                    mediaPlayer.setScreenOnWhilePlaying(true);
                    mediaPlayer.setOnSeekCompleteListener(VideoPlayerMediaSystem.this);
                    mediaPlayer.setOnErrorListener(VideoPlayerMediaSystem.this);
                    mediaPlayer.setOnInfoListener(VideoPlayerMediaSystem.this);
                    mediaPlayer.setOnVideoSizeChangedListener(VideoPlayerMediaSystem.this);

                    Class<MediaPlayer> clazz = MediaPlayer.class;
                    Method method = clazz.getDeclaredMethod("setDataSource", String.class, Map.class);
                    method.invoke(mediaPlayer, videoPlayerDataSource.getCurrentUrl().toString(), videoPlayerDataSource.headerMap);
                    mediaPlayer.prepareAsync();
                    if (SAVED_SURFACE != null) {
                        isPrepareAsync = false;
                        mediaPlayer.setSurface(new Surface(SAVED_SURFACE));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    handler.post(new Runnable() {

                        @Override
                        public void run() {
                            if (mVideoPlayerListener != null){
                                mVideoPlayerListener.onError(0, 0);
                            }
                        }
                    });

                }

            }
        });
    }

    @Override
    public void start() {
        if (mMediaHandler == null) return;
        mMediaHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer == null) return;
                mediaPlayer.start();
            }
        });
    }

    @Override
    public void pause() {
        if (mMediaHandler == null || mediaPlayer == null) return;
        mMediaHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer == null) return;
                mediaPlayer.pause();
            }
        });
    }

    @Override
    public boolean isPlaying() {
        if (mediaPlayer == null) return false;
        return mediaPlayer.isPlaying();
    }

    @Override
    public void seekTo(final long time) {
        if (mMediaHandler == null) return;
        mMediaHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mediaPlayer == null) return;
                    mediaPlayer.seekTo((int) time);
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void release() {//not perfect change you later
        if (mMediaHandler != null && mediaPlayer != null) {//不知道有没有妖孽
            final MediaPlayer tmpMediaPlayer = mediaPlayer;

            mMediaHandler.post(new Runnable() {
                @Override
                public void run() {
                    tmpMediaPlayer.setSurface(null);
                    tmpMediaPlayer.release();
                    mMediaHandler = null;
                }
            });
            mediaPlayer = null;
        }
    }

    //TODO 测试这种问题是否在threadHandler中是否正常，所有的操作mediaplayer是否不需要thread，挨个测试，是否有问题
    @Override
    public long getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        } else {
            return 0;
        }
    }

    @Override
    public int getVideoWidth() {

        if (mediaPlayer != null) {
            mediaPlayer.getVideoWidth();
        }
        return 0;
    }

    @Override
    public int getVideoHeight() {
        if (mediaPlayer != null) {
            mediaPlayer.getVideoHeight();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        } else {
            return 0;
        }
    }

    @Override
    public void setVolume(final float leftVolume) {
        if (mMediaHandler == null) return;
        mMediaHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer == null) return;

                mediaPlayer.setVolume(leftVolume, leftVolume);
            }
        });
    }

    @Override
    public void setSpeed(float speed) {
        if (mediaPlayer == null) return;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            PlaybackParams pp = mediaPlayer.getPlaybackParams();
            pp.setSpeed(speed);
            mediaPlayer.setPlaybackParams(pp);
        }

    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (handler == null) return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoPlayerListener == null) return;
                mVideoPlayerListener.onPrepared();//如果是mp3音频，走这里
            }
        });
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (handler == null) return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoPlayerListener == null) return;
                mVideoPlayerListener.onAutoCompletion();
            }
        });
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, final int percent) {
        if (handler == null) return;
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoPlayerListener == null) return;
                mVideoPlayerListener.setBufferProgress(percent);
            }
        });
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        if (handler == null) return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoPlayerListener == null) return;
                mVideoPlayerListener.onSeekComplete();
            }
        });
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, final int what, final int extra) {
        if (handler == null) return false;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoPlayerListener == null) return;
                mVideoPlayerListener.onError(what, extra);
            }
        });
        return true;
    }

    @Override
    public boolean onInfo(MediaPlayer mediaPlayer, final int what, final int extra) {
        if (handler == null || mVideoPlayerListener == null) return false;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoPlayerListener == null) return;
                mVideoPlayerListener.onInfo(what, extra);
            }
        });
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, final int width, final int height) {
        if (handler == null) return;

        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mVideoPlayerListener == null) return;
                mVideoPlayerListener.onVideoSizeChanged(width, height);
            }
        });
    }

    @Override
    public void setSurface(Surface surface) {
//        SigmobLog.d("setSurface() called with: surface = [" + surface + "]");
        if (mediaPlayer == null) return;

        mediaPlayer.setSurface(surface);
    }

    @Override
    public void setLooping(boolean loop) {
        if (mediaPlayer == null) return;

        mediaPlayer.setLooping(loop);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (mVideoPlayerListener == null) return;

//        SigmobLog.d("onSurfaceTextureAvailable() called with: surface = [" + surface + "], width = [" + width + "], height = [" + height + "]");
        if (SAVED_SURFACE == null) {
            SAVED_SURFACE = surface;
            prepare();
        } else {
            mVideoPlayerListener.setSurfaceTexture(SAVED_SURFACE);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//        SigmobLog.d("onSurfaceTextureSizeChanged() called with: surface = [" + surface + "], width = [" + width + "], height = [" + height + "]");
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//        SigmobLog.d("onSurfaceTextureDestroyed() called with: surface = [" + surface + "]");

//        try {
//            if (mediaPlayer != null) {
//                mediaPlayer.stop();
//                mediaPlayer.release();
//                mediaPlayer = null;
//            }
//        } catch (Throwable e) {
//
//        }
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }
}
