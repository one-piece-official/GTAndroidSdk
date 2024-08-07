package com.gt.sdk.base.view;


import android.content.Context;
import android.media.MediaPlayer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.czhj.sdk.logger.SigmobLog;

import java.io.IOException;

public class AdVideoPlayer extends RelativeLayout implements SurfaceHolder.Callback, MediaPlayer.OnBufferingUpdateListener {

    private MediaPlayer mMediaPlayer = null;
    private SurfaceHolder mSurfaceHolder;
    SurfaceView mVideoPlay;
    private String videoPath = "";
    private int videowidth;
    private int videoheight;
    private int mSeekerPositionOnPause;

    public AdVideoPlayer(Context context) {
        super(context);
        mVideoPlay = new SurfaceView(context);
        addView(mVideoPlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mSurfaceHolder = mVideoPlay.getHolder();
        mSurfaceHolder.addCallback(this);
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(true);
        mMediaPlayer.setOnBufferingUpdateListener(this);
    }

    public void setOnSeekCompleteListener(MediaPlayer.OnSeekCompleteListener onSeekCompleteListener) {
        mMediaPlayer.setOnSeekCompleteListener(onSeekCompleteListener);
    }

    public void setVideoPath(String videoPath) {
        this.videoPath = videoPath;
        play();
    }

    public void setOnPreparedListener(MediaPlayer.OnPreparedListener listener) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnPreparedListener(listener);
        }
    }

    public void setMeasure(int width, int height) {
        this.videowidth = width;
        this.videoheight = height;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int width = getDefaultSize(0, widthMeasureSpec);
        int height = getDefaultSize(0, heightMeasureSpec);
        int mVideoHeight;
        int mVideoWidth;
        if (height > width) {
            //竖屏
            if (this.videoheight > this.videowidth) {
                //如果视频资源是竖屏
                //占满屏幕
                mVideoHeight = height;
                mVideoWidth = width;
            } else {
                //如果视频资源是横屏
                //宽度占满，高度保存比例
                mVideoWidth = width;
                float r = this.videoheight / (float) this.videowidth;
                mVideoHeight = (int) (mVideoWidth * r);
            }
        } else {
            //横屏
            if (this.videoheight > this.videowidth) {
                //如果视频资源是竖屏
                //宽度占满，高度保存比例
                mVideoHeight = height;
                float r = this.videowidth / (float) this.videoheight;
                mVideoWidth = (int) (mVideoHeight * r);
            } else {
                //如果视频资源是横屏
                //占满屏幕
                mVideoHeight = height;
                mVideoWidth = width;
            }
        }
        if (this.videoheight == this.videowidth) {
            if (videoheight == -1) {
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
            } else {
                //没能获取到视频真实的宽高，自适应就可以了，什么也不用做
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

        } else {
            setMeasuredDimension(mVideoWidth, mVideoHeight);
        }
    }

    public void onPause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mSeekerPositionOnPause = mMediaPlayer.getCurrentPosition();
        }
    }

    public void onResume() {
        if (mMediaPlayer != null) {
            long position = mMediaPlayer.getCurrentPosition();
            if (position == 0) {
                mMediaPlayer.seekTo(mSeekerPositionOnPause);
            }
            mMediaPlayer.start();
        }
    }

    public void onDestroy() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(holder);    //设置显示视频显示在SurfaceView上
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    private void play() {
        try {
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(videoPath);
            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            SigmobLog.e(e.getMessage());
        }
    }


    public void start() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {

    }

    public void seek(int position) {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(position);
        }
    }

    public void stop() {
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
        }
    }

    public void pause() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
        }
    }

    public void muted(boolean mute) {
        if (mMediaPlayer != null) {
            int volume = mute ? 0 : 1;
            mMediaPlayer.setVolume(volume, volume);
        }
    }

    public void setVolume(int i) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(i, i);
        }
    }

}
