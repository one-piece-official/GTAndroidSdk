package com.sigmob.sdk.nativead;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.models.SigImage;
import com.sigmob.sdk.base.models.SigVideo;
import com.sigmob.windad.natives.AdAppInfo;
import com.sigmob.windad.natives.NativeADEventListener;
import com.sigmob.windad.natives.WindNativeAdData;

import java.util.List;

public class WindNativeAdUnitObject implements WindNativeAdData {

    private final String mTitle;
    private final String mDescription;
    private final String mIconUrl;
    private final List<SigImage> mImageList;
    private final SigVideo mVideo;

    private final int mAdViewModel;
    private final SigmobNativeAdRender mSigmobNativeAdRender;
    private AdAppInfo adAppInfo;

    public WindNativeAdUnitObject(BaseAdUnit adUnit) {

        this.mTitle = adUnit.getTitle();
        this.mDescription = adUnit.getDesc();
        this.mIconUrl = adUnit.getIconUrl();
        this.mImageList = adUnit.getImageUrlList();
        this.mAdViewModel = adUnit.getNativeAd().type;
        this.adAppInfo = adUnit.getAdAppInfo();
        this.mVideo = adUnit.getNativeVideo();
        this.mSigmobNativeAdRender = new SigmobNativeAdRender();
        this.mSigmobNativeAdRender.initAdData(adUnit, this);
    }


    @Override
    public AdAppInfo getAdAppInfo() {
        return adAppInfo;
    }

    @Override
    public String getCTAText() {
        if (mSigmobNativeAdRender != null) {
            return mSigmobNativeAdRender.getCTAText();
        }
        return "";
    }


    public String getEcpm(){
        if (mSigmobNativeAdRender != null) {
            return mSigmobNativeAdRender.getEcpm();
        }
        return "";
    }
    /**
     * 获取Sigmob logo，图片大小：80*80
     *
     * @return bitmap
     */
    public Bitmap getAdLogo() {

        return mSigmobNativeAdRender.getAdLogo();
    }

    /**
     * 获取广告标题
     *
     * @return
     */
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String getDesc() {
        return mDescription;
    }


    /**
     * 获取广告来源
     *
     * @return
     */
    public String getSource() {

        return "sigmob";
    }

    /**
     * 广告图标Image
     *
     * @return
     */
    public String getIconUrl() {

        return mIconUrl;
    }

    @Override
    public int getAdPatternType() {
        return mAdViewModel;
    }

    @Override
    public void bindImageViews(List<ImageView> imageViews, int defaultImageRes) {
        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.bindImageViews(imageViews, defaultImageRes);
        }
    }

    @Override
    public void bindViewForInteraction(View view, List<View> clickableViews, List<View> creativeViewList, View disLikeView, NativeADEventListener nativeAdEventListener) {
        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.registerViewForInteraction(view, clickableViews, creativeViewList, disLikeView, nativeAdEventListener);
        }
    }

    @Override
    public void bindMediaView(ViewGroup mediaLayout, NativeADMediaListener nativeADMediaListener) {
        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.bindMediaView(mediaLayout, nativeADMediaListener);
        }
    }

    @Override
    public void bindMediaViewWithoutAppInfo(ViewGroup mediaLayout, NativeADMediaListener nativeADMediaListener) {
        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.bindMediaViewWithoutAppInfo(mediaLayout, nativeADMediaListener);
        }
    }


    /**
     * 广告图片Image list
     *
     * @return
     */
    public List<SigImage> getImageList() {

        return mImageList;
    }

    @Override
    public View getWidgetView(int width,int height) {
        if (mSigmobNativeAdRender != null) {
            return mSigmobNativeAdRender.getWidgetView(width,height);
        }
        return null;
    }

    /**
     * 广告图片Image list
     *
     * @return
     */
    public int getVideoWidth() {
        if (mVideo != null){
            return mVideo.getWidth();
        }
        return 0;
    }

    public int getVideoHeight() {
        if (mVideo != null){
            return mVideo.getHeight();
        }
        return 0;
    }
    public View getAdView() {
        if (mSigmobNativeAdRender != null) {
            return mSigmobNativeAdRender.getAdView();
        }
        return null;
    }

    public double getVideoDuration() {
        if (mSigmobNativeAdRender != null) {
            return mSigmobNativeAdRender.getVideoDuration();
        }
        return 0.0;
    }

    public double getVideoProgress() {
        if (mSigmobNativeAdRender != null) {
            return mSigmobNativeAdRender.getVideoProgress();
        }
        return 0.0;
    }

    public int getAdViewWidth() {
        if (mSigmobNativeAdRender != null) {
            return mSigmobNativeAdRender.getAdViewWidth();
        }
        return 0;
    }

    public int getAdViewHeight() {
        if (mSigmobNativeAdRender != null) {
            return mSigmobNativeAdRender.getAdViewHeight();
        }
        return 0;
    }

    /*
     *   1: 单视频、2: 单图、3: 多图(指3张图片)
     */
    public int getAdViewModel() {
        return mAdViewModel;
    }

    public void destroy() {

        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.destroy();
        }

    }

    @Override
    public void startVideo() {
        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.startVideo();
        }
    }


    @Override
    public void pauseVideo() {
        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.pauseVideo();
        }
    }

    @Override
    public void resumeVideo() {
        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.resumeVideo();
        }
    }

    @Override
    public void stopVideo() {
        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.stopVideo();
        }
    }

    @Override
    public void setDislikeInteractionCallback(Activity activity, DislikeInteractionCallback dislikeInteractionCallback) {

        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.setDislikeInterActionCallBack(dislikeInteractionCallback);
        }
    }

    public void unRegisterViewForInteraction() {
        if (mSigmobNativeAdRender != null) {
            mSigmobNativeAdRender.unRegisterViewForInteraction();
        }
    }


}
