package com.gt.sdk.natives;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.common.utils.ResourceUtil;

public class SigAppView extends RelativeLayout {
    private TextView mName;
    private TextView mCta;
    private ViewGroup iconLayout;
    private CircleImageView mIcon;

    public SigAppView(Context context) {
        super(context);
        View.inflate(context, getLayoutId(), this);
        mName = findViewById(ResourceUtil.getId(getContext(), "sig_app_name"));
        mCta = findViewById(ResourceUtil.getId(getContext(), "sig_app_cta"));
        iconLayout = findViewById(ResourceUtil.getId(getContext(), "sig_app_icon"));
        mIcon = new CircleImageView(context);
        mIcon.setCircle(false);
        mIcon.setScaleType(ImageView.ScaleType.CENTER_CROP);
        iconLayout.addView(mIcon, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        mCta.setOnClickListener(l);
    }

    public void initData(String iconUrl, String name, String cta) {
        mName.setText(name);
        mCta.setText(cta);
        ImageManager.with(getContext()).load(iconUrl).into(mIcon);
    }

    private int getLayoutId() {
        return ResourceUtil.getLayoutId(getContext(), "sig_app_layout");
    }

    public View getCtaView() {
        return mCta;
    }
}
