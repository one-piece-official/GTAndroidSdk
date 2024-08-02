package com.sigmob.sdk.mraid;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.SDKContext;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.utils.ViewUtil;
import com.sigmob.sdk.videocache.CacheListener;
import com.sigmob.sdk.videoplayer.VIDEO_PLAYER_STATE;
import com.sigmob.sdk.videoplayer.VideoPlayerStatusListener;

import org.json.JSONObject;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MraidVpaid extends MraidObject implements MraidObject.VpaidBridgeListener, CacheListener {

    private MraidVideoAdView mraidVideoAdView;
    public static final int PLAY_STATE_IDLE = 0;
    public static final int PLAY_STATE_PLAY = 1;
    public static final int PLAY_STATE_PAUSE = 2;
    public static final int PLAY_STATE_FAIL = 3;
    public static final int PLAY_STATE_STOP = 4;

    public static final int LOAD_STATE_IDLE = 0;
    public static final int LOAD_STATE_PREPARE = 1;
    public static final int LOAD_STATE_PLAYABLE = 2;
    public static final int LOAD_STATE_PLAYTHROUGHOK = 4;
    public static final int LOAD_STATE_STALLED = 8;
    private String mp4Url;

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {

    }

    @Override
    public void onCacheUnavailable(String url, Throwable throwable) {
        SigmobLog.e("url",throwable);

        if (mraidVpaidListener != null){
            mraidVpaidListener.OnError(uniqueId,0,throwable.getMessage());
        }
    }

    public interface MraidVpaidListener {
        void OnReady(String uniqueId, long duration, int width, int height);

        void OnPlayStateChange(String uniqueId, int state);

        void OnLoadStateChange(String uniqueId, int state);

        void OnProgressUpdate(String uniqueId, long position, long duration);

        void OnPlayEnd(String uniqueId, long position);

        void OnError(String uniqueId, int code, String message);

    }

    public MraidVpaid(String uniqueId) {
        super(uniqueId);
    }

    private MraidVpaidListener mraidVpaidListener;

    public void setMraidVpaidListener(MraidVpaidListener mraidVpaidListener) {
        this.mraidVpaidListener = mraidVpaidListener;
    }

    @Override
    public View getView() {
        return mraidVideoAdView;
    }

    @Override
    public void destroy() {
        if (mraidVideoAdView != null) {
            ViewUtil.removeFromParent(mraidVideoAdView);
            mraidVideoAdView.removeAllViews();
            mraidVideoAdView.destroy();
            mraidVideoAdView = null;
        }
        if (mp4Url != null){
            AdStackManager.getHttProxyCacheServer().unregisterCacheListener(this,mp4Url);
        }
    }

    @Override
    public void OnVpaidInit(Context context, JSONObject args) {
        mraidVideoAdView = new MraidVideoAdView(context);
        mraidVideoAdView.setVideoPlayerStatusListener(new VideoPlayerStatusListener() {
            @Override
            public void OnStateChange(VIDEO_PLAYER_STATE state) {

                SigmobLog.d("VIDEO_PLAYER_STATE change: " + state);
                switch (state) {

                    case STATE_PREPARED: {

                        if (mraidVpaidListener != null) {
                            long duration = mraidVideoAdView.getDuration();
                            int width = mraidVideoAdView.getVideoWidth();
                            int height = mraidVideoAdView.getVideoHeight();
                            mraidVpaidListener.OnReady(uniqueId, duration, width, height);
                        }
                        if (mraidVpaidListener != null) {
                            mraidVpaidListener.OnLoadStateChange(uniqueId, LOAD_STATE_PLAYABLE);
                        }

                        if (mraidVpaidListener != null) {
                            mraidVpaidListener.OnLoadStateChange(uniqueId, LOAD_STATE_PLAYTHROUGHOK);
                        }
                    }
                    break;
                    case STATE_ERROR: {
                        if (mraidVpaidListener != null) {

                            int code = mraidVideoAdView.getErrorCode();
                            String msg = mraidVideoAdView.getErrorMessage();
                            mraidVpaidListener.OnError(uniqueId, code, msg);
                        }
                    }
                    break;

                    case STATE_PAUSE: {
                        if (mraidVpaidListener != null) {
                            mraidVpaidListener.OnPlayStateChange(uniqueId, PLAY_STATE_PAUSE);
                        }
                    }
                    break;
                    case STATE_PLAYING: {
                        if (mraidVpaidListener != null) {
                            mraidVpaidListener.OnPlayStateChange(uniqueId, PLAY_STATE_PLAY);
                        }
                    }
                    break;
                    case STATE_AUTO_COMPLETE: {
                        if (mraidVpaidListener != null) {
                            mraidVpaidListener.OnProgressUpdate(uniqueId, mraidVideoAdView.getDuration(), mraidVideoAdView.getDuration());
                            mraidVpaidListener.OnPlayEnd(uniqueId, mraidVideoAdView.getDuration());
                        }
                    }
                    break;
                    case STATE_PREPARING: {
                        if (mraidVpaidListener != null) {
                            mraidVpaidListener.OnLoadStateChange(uniqueId, LOAD_STATE_PREPARE);
                        }
                    }
                    break;
                    case STATE_BUFFERING_START: {
                        if (mraidVpaidListener != null) {
                            mraidVideoAdView.goOnPlayOnPause();
                            mraidVpaidListener.OnLoadStateChange(uniqueId, LOAD_STATE_STALLED);
                        }
                    }
                    break;
                    case STATE_BUFFERING_END: {
                        if (mraidVpaidListener != null) {
                            mraidVideoAdView.goOnPlayOnResume();
                            mraidVpaidListener.OnLoadStateChange(uniqueId, LOAD_STATE_PLAYABLE);
                        }
                    }
                    break;
                    case STATE_STOP: {
                        if (mraidVpaidListener != null) {
                            mraidVpaidListener.OnPlayStateChange(uniqueId, PLAY_STATE_STOP);
                        }
                    }
                    break;
                    default: {

                    }
                    break;
                }
            }

            @Override
            public void OnProgressUpdate(long position, long duration) {
                if (mraidVpaidListener != null) {
                    mraidVpaidListener.OnProgressUpdate(uniqueId, position, duration);
                }

            }
        });
    }

    @Override
    public void OnVpaidAssetURL(JSONObject args) {

        if (mraidVideoAdView != null) {
            String url = args.optString("URL");
            boolean proxy = args.optBoolean("proxy", false);

            if (proxy && !TextUtils.isEmpty(url) && url.startsWith("http")){
                Uri uri =  Uri.parse(url);
                if (uri != null && !"127.0.0.1".equalsIgnoreCase(uri.getHost())){
                    url = AdStackManager.getHttProxyCacheServer().getProxyUrl(url);
                }
                listenerCacheUnavailable(uniqueId,url);
            }

            mraidVideoAdView.setUp(url);
            mraidVideoAdView.startPreloading();
        }
    }

    private void listenerCacheUnavailable(String uniqueId,String url){
        Uri uri =  Uri.parse(url);
        if (uri != null && "127.0.0.1".equalsIgnoreCase(uri.getHost())){
            Pattern URL_PATTERN = Pattern.compile("/(.*)");

            Matcher matcher = URL_PATTERN.matcher(uri.getPath());
            if (matcher.find()){
                mp4Url  = matcher.group(1);
                AdStackManager.getHttProxyCacheServer().registerCacheListener(this,mp4Url);
            }

        }

    }

    @Override
    public void OnVpaidPlay(JSONObject args) {
        if (mraidVideoAdView != null) {
            mraidVideoAdView.goOnPlayOnResume();
        }
    }

    @Override
    public void OnVpaidReplay(JSONObject args) {
        if (mraidVideoAdView != null) {
            mraidVideoAdView.seekTo(0);
            mraidVideoAdView.goOnPlayOnResume();
        }
    }

    @Override
    public void OnVpaidPause(JSONObject args) {
        if (mraidVideoAdView != null) {
            mraidVideoAdView.goOnPlayOnPause();
        }
    }

    @Override
    public void OnVpaidStop(JSONObject args) {
        if (mraidVideoAdView != null) {
            mraidVideoAdView.stopVideo();
        }
    }

    @Override
    public void OnVpaidMuted(JSONObject args) {
        if (mraidVideoAdView != null) {
            boolean muted = args.optBoolean("muted", false);

            mraidVideoAdView.setMute(muted);
        }
    }

    @Override
    public void OnVpaidSeek(JSONObject args) {

        if (mraidVideoAdView != null) {
            double seek = args.optDouble("seekTime", 0);

            mraidVideoAdView.seekTo((int) seek * 1000);
        }
    }

    @Override
    public void OnVpaidFrame(JSONObject args) {

        if (mraidVideoAdView != null) {
            JSONObject frame = args.optJSONObject("frame");

            int top = (int) frame.optDouble("x", 0);
            int left = (int) frame.optDouble("y", 0);
            int width = (int) frame.optDouble("w", -1);
            int height = (int) frame.optDouble("h", -1);
            int realwidth = width;
            int realheight = height;
            if (width > 0) {
                realwidth = Dips.dipsToIntPixels(width, SDKContext.getApplicationContext());
            }

            if (height > 0) {
                realheight = Dips.dipsToIntPixels(height, SDKContext.getApplicationContext());
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(realwidth, realheight);
//            layoutParams.topMargin = Dips.dipsToIntPixels(top, SDKContext.getApplicationContext());
//            layoutParams.leftMargin = Dips.dipsToIntPixels(left, SDKContext.getApplicationContext());
            mraidVideoAdView.setX(Dips.dipsToIntPixels(top, SDKContext.getApplicationContext()));
            mraidVideoAdView.setY(Dips.dipsToIntPixels(left, SDKContext.getApplicationContext()));
            mraidVideoAdView.setLayoutParams(layoutParams);
            mraidVideoAdView.requestLayout();
        }
    }
}
