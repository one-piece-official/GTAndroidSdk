package com.sigmob.sdk.newInterstitial;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.utils.ResourceUtil;

public class NewInterstitialAdView extends RelativeLayout {

    private View mContentView;

    public NewInterstitialAdView(Context context) {
        super(context);
    }

    public void init(int resId) {
        mContentView = inflate(getContext(),resId, this);
    }

    public ViewGroup getMainAdContainer() {
        if (mContentView != null) {
            return mContentView.findViewById(ResourceUtil.getId(getContext(),"sig_ad_container"));
        }
        return null;
    }

    public SigAdInfoView getAdInfView() {
        if (mContentView != null) {
            return mContentView.findViewById(ResourceUtil.getId(getContext(), "sig_app_info"));
        }
        return null;
    }


    public NewInterstitialHeaderView getHeaderView() {
        if (mContentView != null) {
            return mContentView.findViewById(ResourceUtil.getId(getContext(),"sig_ad_header"));
        }
        return null;
    }

    public Button getCTAButton() {
        if (mContentView != null) {
            return mContentView.findViewById(ResourceUtil.getId(getContext(),"sig_cta_button"));
        }
        return null;
    }

}
