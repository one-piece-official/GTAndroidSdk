package com.sigmob.sdk.newInterstitial;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.czhj.sdk.common.utils.ResourceUtil;
import com.sigmob.sdk.base.common.AdStackManager;

public class SigAdPrivacyInfoView extends LinearLayout {


    private ImageView mAdLogo;
    private View mPrivacyView;
    private View mPrivacyLl;

    private LinearLayout viewLayout;
    private TextView mPrivacyAdText;

    public SigAdPrivacyInfoView(Context context) {
        super(context);
        init(context);
    }

    public SigAdPrivacyInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SigAdPrivacyInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        View view = inflate(context, ResourceUtil.getLayoutId(context, "sig_ad_privacy_layout"), this);
        mAdLogo = view.findViewById(ResourceUtil.getId(context, "sig_ad_privacy_ad_logo"));
        mPrivacyView = view.findViewById(ResourceUtil.getId(context, "sig_ad_privacy_view"));
        mPrivacyLl = view.findViewById(ResourceUtil.getId(context, "sig_ad_privacy_ll"));
        mPrivacyAdText = view.findViewById(ResourceUtil.getId(context, "sig_ad_privacy_ad_text"));
    }

    public View getPrivacyLl() {
        return mPrivacyLl;
    }

    public TextView getPrivacyAdText() {
        return mPrivacyAdText;
    }

    public void setupView(String logoUrl, boolean isShow) {
        if (!TextUtils.isEmpty(logoUrl)) {
            AdStackManager.getImageManger().load(logoUrl).into(mAdLogo);
        }
        if (isShow) {

            mPrivacyView.setVisibility(View.VISIBLE);
        } else {
            mPrivacyView.setVisibility(View.GONE);
        }
    }
}
