package com.sigmob.windad.natives;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sigmob.sdk.base.models.SigImage;
import com.sigmob.windad.WindAdError;

import java.util.List;

public interface WindNativeAdData {

    String getCTAText();

    String getTitle();

    String getDesc();

    Bitmap getAdLogo();

    String getIconUrl();

    String getEcpm();
    List<SigImage> getImageList();


    /**
     * 获取互动组件的View
     * @param width: 组件View的大小，不小于60dp且不大于100dp
     * @return View 互动组件View
     */
    View getWidgetView(int width, int height);

    /**
     * 获取广告样式
     *
     * @return 1: 单视频、2: 单图、3: 多图(指3张图片)
     */
    int getAdPatternType();


    /**
     * @param imageViews      展示广告的Images
     * @param defaultImageRes 渲染图片失败时默认的图片
     */
    void bindImageViews(List<ImageView> imageViews, int defaultImageRes);

    /**
     * @param view                  自渲染的根View
     * @param clickableViews        用于下载或者拨打电话的View
     * @param creativeViewList      用于下载或者拨打电话的View
     * @param disLikeView           dislike按钮
     * @param nativeAdEventListener 点击回调
     */
    void bindViewForInteraction(View view, List<View> clickableViews, List<View> creativeViewList,
                                View disLikeView, NativeADEventListener nativeAdEventListener);

    /**
     * @param mediaLayout           装video的容器
     * @param nativeADMediaListener video播放监听
     */
    void bindMediaView(ViewGroup mediaLayout, NativeADMediaListener nativeADMediaListener);

    void bindMediaViewWithoutAppInfo(ViewGroup mediaLayout, NativeADMediaListener nativeADMediaListener);


    AdAppInfo getAdAppInfo();


    void destroy();

    void startVideo();

    void pauseVideo();

    void resumeVideo();

    void stopVideo();

    int getVideoWidth();
    int getVideoHeight();

    /**
     * 信息流视频回调接口
     */
    interface NativeADMediaListener {

        //视频加载成功
        void onVideoLoad();

        //视频加载失败
        void onVideoError(final WindAdError error);

        //视频开始播放
        void onVideoStart();

        //视频暂停播放
        void onVideoPause();

        //视频继续播放
        void onVideoResume();

        //视频完成播放
        void onVideoCompleted();
    }

    /**
     * 建议传当前activity，否则可能会影响dislike对话框弹出
     *
     * @param activity
     * @param dislikeInteractionCallback
     */
    void setDislikeInteractionCallback(Activity activity, DislikeInteractionCallback dislikeInteractionCallback);

    /**
     * dislike回调接口
     */
    interface DislikeInteractionCallback {
        void onShow();

        void onSelected(final int position, final String value, final boolean enforce);

        void onCancel();
    }

}
