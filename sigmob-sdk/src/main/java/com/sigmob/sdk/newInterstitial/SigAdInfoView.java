package com.sigmob.sdk.newInterstitial;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.utils.ResourceUtil;
import com.sigmob.sdk.base.common.AdStackManager;

public class SigAdInfoView extends RelativeLayout {

    private ImageView appIcon;
    private TextView appTitle;
    private TextView appDescription;
    private SigAdPrivacyInfoView adPrivacyInfo;

    private boolean isSmall = false;

    public SigAdInfoView(Context context) {
        super(context);
        init(context);
    }

    public SigAdInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        isSmall = attrs.getAttributeBooleanValue(null, "sig_isSmall", false);
        init(context);
    }

    public SigAdInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        String layoutName  =  isSmall?"sig_ad_app_info_small_layout": "sig_ad_app_info_layout";
        View view = inflate(context,ResourceUtil.getLayoutId(context,layoutName), this );
        appIcon = view.findViewById(ResourceUtil.getId(context,"sig_app_icon"));
        appTitle = view.findViewById(ResourceUtil.getId(context,"sig_ad_title"));
        appDescription = view.findViewById(ResourceUtil.getId(context,"sig_ad_desc"));
        adPrivacyInfo = view.findViewById(ResourceUtil.getId(context, "sig_ad_privacy_info"));
    }


    public SigAdPrivacyInfoView getAdPrivacyInfo() {
        return adPrivacyInfo;
    }

    public void setAppInfoView(String iconUrl, String title, String desc) {

        if(!TextUtils.isEmpty(iconUrl)) {
            AdStackManager.getImageManger().load(iconUrl).into(appIcon);
        }
        if(!TextUtils.isEmpty(title)){
            appTitle.setText(title);
        }
        if(!TextUtils.isEmpty(desc)){
            appDescription.setText(desc);
        }
    }




}
