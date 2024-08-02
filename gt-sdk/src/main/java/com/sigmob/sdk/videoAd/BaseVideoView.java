package com.sigmob.sdk.videoAd;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.VideoView;

import com.czhj.sdk.logger.SigmobLog;
import com.czhj.sdk.common.utils.Preconditions;
import com.sigmob.sdk.base.utils.ViewUtil;


import java.lang.reflect.Field;

public class BaseVideoView extends VideoView {

    private static final int MAX_VIDEO_RETRIES = 1;
    private static final int VIDEO_VIEW_FILE_PERMISSION_ERROR = Integer.MIN_VALUE;

    protected final MediaMetadataRetriever mMediaMetadataRetriever;
    private int videowidth;
    private int videoheight;

    public BaseVideoView( final Context context) {
        super(context);
        Preconditions.NoThrow.checkNotNull(context, "context cannot be null");
        mMediaMetadataRetriever = new MediaMetadataRetriever();

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
        if(height>width){
            //竖屏
            if(this.videoheight >this.videowidth){
                //如果视频资源是竖屏
                //占满屏幕
                mVideoHeight=height;
                mVideoWidth=width;
            }else {
                //如果视频资源是横屏
                //宽度占满，高度保存比例
                mVideoWidth=width;
                float r=this.videoheight /(float)this.videowidth;
                mVideoHeight= (int) (mVideoWidth*r);
            }
        }else {
            //横屏
            if(this.videoheight >this.videowidth){
                //如果视频资源是竖屏
                //宽度占满，高度保存比例
                mVideoHeight=height;
                float r=this.videowidth /(float)this.videoheight;
                mVideoWidth= (int) (mVideoHeight*r);
            }else {
                //如果视频资源是横屏
                //占满屏幕
                mVideoHeight=height;
                mVideoWidth=width;
            }
        }
        if(this.videoheight ==this.videowidth){
            if (videoheight == -1){
                setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.getSize(heightMeasureSpec));
            }else {
                super.onMeasure(widthMeasureSpec,heightMeasureSpec);
            }

        }else {
            setMeasuredDimension(mVideoWidth, mVideoHeight);
        }
    }

    /**
     * Called when the activity enclosing this view is destroyed. We do not want to continue this
     * task when the activity expecting the result no longer exists.
     */

    private void clearSurfaceViewMemoryLeak() {
            try {
                // 删除onClickListener
                this.setOnClickListener(null);
                this.setOnCompletionListener(null);
                this.setOnErrorListener(null);
                this.setOnPreparedListener(null);

                // 干掉View中的mContext变量
                Class clazz = View.class;
                Field field = clazz.getDeclaredField("mContext");
                if(field != null){
                    field.setAccessible(true);
                    field.set(this, null);
                }


            } catch (Throwable e) {
                 SigmobLog.e(e.getMessage());
            }
    }

    public void setVolume(float volume) {
        try {
            Class<?> forName = Class.forName("android.widget.VideoView");
            Field field = forName.getDeclaredField("mMediaPlayer");
            if(field != null){
                field.setAccessible(true);
                MediaPlayer mMediaPlayer = (MediaPlayer) field.get(this);
                mMediaPlayer.setVolume(volume, volume);
            }
        } catch (Throwable e) {
             SigmobLog.e(e.getMessage());
        }
    }

    private void fixSubtitleControllerLeak(MediaPlayer mediaPlayer,Context context) {

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 28) {
            Field declaredField;
            try {
                Class cls = Class.forName("android.media.MediaTimeProvider");
                Class cls2 = Class.forName("android.media.SubtitleController");
                Class cls3 = Class.forName("android.media.SubtitleController$Anchor");
                Class cls4 = Class.forName("android.media.SubtitleController$Listener");
                Object newInstance = cls2.getConstructor(new Class[]{Context.class, cls, cls4}).newInstance(new Object[]{context, null, null});
                declaredField = cls2.getDeclaredField("mHandler");
                declaredField.setAccessible(true);
                declaredField.set(newInstance, new Handler());
                declaredField.setAccessible(false);
                mediaPlayer.getClass().getMethod("setSubtitleAnchor", new Class[]{cls2, cls3}).invoke(mediaPlayer, new Object[]{newInstance, null});
            } catch (Throwable th) {
                SigmobLog.e( "setSubtitleController error: ", th);
            }
        }
    }



    public void onDestroy() {

        SigmobLog.d( "BaseVideoView onDestroy() called");
        suspend();
        stopPlayback();
        ViewUtil.removeFromParent(this);
        clearSurfaceViewMemoryLeak();

        super.destroyDrawingCache();
    }


}