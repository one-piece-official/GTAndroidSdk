package com.gt.sdk.base.common;


import com.czhj.sdk.common.ClientMetadata;

public class SigmobRes {
    private static final String ad = "sig_ad";
    private static final String close = "sig_close";
    private static final String back = "sig_back";
    private static final String skipArgs1 = "sig_skip_args_1";
    private static final String skipArgs2 = "sig_skip_args_2";
    private static final String skipAdArgs = "sig_skip_ad_args";

    private static final String closeArgs = "sig_close_args";
    private static final String closeAdTitle = "sig_close_ad_title";
    private static final String closeAdMessage = "sig_close_ad_message";
    private static final String closeAdCancel = "sig_close_ad_cancel";
    private static final String closeAdOk = "sig_close_ad_ok";
    private static final String sig_custom_dialog = "sig_custom_dialog";
    private static final String sig_dialog_window_anim = "sig_dialog_window_anim";
    private static final String sig_transparent_style = "sig_transparent_style";
    private static final String sig_base_theme = "sig_base_theme";
    private static final String sig_land_theme = "sig_land_theme";
    private static final String sig_transparent_lang = "sig_transparent_lang";
    private static final String sig_custom_fullscreen_dialog = "sig_custom_fullscreen_dialog";

    public static int getSig_transparent_lang() {
        return ClientMetadata.getInstance().getStyleResources(SigmobRes.sig_transparent_lang);
    }

    public static int getSig_base_theme() {
        return ClientMetadata.getInstance().getStyleResources(SigmobRes.sig_base_theme);
    }

    public static int getSig_land_theme() {
        return ClientMetadata.getInstance().getStyleResources(SigmobRes.sig_land_theme);
    }

    public static int getSig_transparent_style() {
        return ClientMetadata.getInstance().getStyleResources(SigmobRes.sig_transparent_style);
    }

    public static int getSig_custom_dialog() {
        return ClientMetadata.getInstance().getStyleResources(SigmobRes.sig_custom_dialog);
    }

    public static int getSig_custom_download_dialog() {
        int mScreenWidth = ClientMetadata.getInstance().getDisplayMetrics().widthPixels;
        int mScreenHeight = ClientMetadata.getInstance().getDisplayMetrics().heightPixels;
        String styleName = mScreenWidth > mScreenHeight ? SigmobRes.sig_custom_fullscreen_dialog : SigmobRes.sig_custom_dialog;

        return ClientMetadata.getInstance().getStyleResources(styleName);
    }

    public static int getSig_dialog_window_anim() {
        return ClientMetadata.getInstance().getStyleResources(SigmobRes.sig_dialog_window_anim);
    }

    public static String getAd() {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.ad, "广告");
    }

    public static String getClose() {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.close, "跳过");
    }

    public static String getBack() {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.back, "返回");
    }

    public static String closeAdMessage() {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.closeAdMessage, "仅需再浏览 _SEC_ 秒广告，即可领取奖励");
    }

    public static String closeAdTitle() {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.closeAdTitle, "要放弃领取奖励吗?");
    }

    public static String closeAdCancel() {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.closeAdCancel, "继续观看");
    }

    public static String closeAdOk() {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.closeAdOk, "关闭广告");
    }

    public static String skipArgs1(Object... formatArgs) {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.skipArgs1, "跳过 %d", formatArgs);
    }

    public static String skipArgs2(Object... formatArgs) {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.skipArgs2, "%d 跳过", formatArgs);
    }

    public static String skipAdArgs(Object... formatArgs) {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.skipAdArgs, "跳过广告 %d", formatArgs);
    }

    public static String getClose(Object... formatArgs) {
        return ClientMetadata.getInstance().getStringResources(SigmobRes.closeArgs, "%s 跳过", formatArgs);
    }

}
