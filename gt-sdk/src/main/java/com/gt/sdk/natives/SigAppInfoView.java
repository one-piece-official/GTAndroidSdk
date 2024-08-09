package com.gt.sdk.natives;

import android.content.Context;
import android.graphics.Paint;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.utils.ResourceUtil;

public class SigAppInfoView extends RelativeLayout {
    private TextView nameView;
    private TextView pruductView;
    private TextView verView;
    private TextView developerView;
    private TextView privacyView;
    private TextView permissionsView;

    public SigAppInfoView(Context context) {
        super(context);

        View.inflate(context, getLayoutId(), this);

        nameView = findViewById(ResourceUtil.getId(getContext(), "sig_app_info_name"));
        verView = findViewById(ResourceUtil.getId(getContext(), "sig_app_info_ver"));

        developerView = findViewById(ResourceUtil.getId(getContext(), "sig_app_info_dev"));
        privacyView = findViewById(ResourceUtil.getId(getContext(), "sig_app_info_privacy"));
        permissionsView = findViewById(ResourceUtil.getId(getContext(), "sig_app_info_permissions"));
        pruductView = findViewById(ResourceUtil.getId(getContext(), "sig_app_info_product"));

        verView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        verView.getPaint().setAntiAlias(true);//抗锯齿
        nameView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        nameView.getPaint().setAntiAlias(true);//抗锯齿

        developerView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        developerView.getPaint().setAntiAlias(true);//抗锯齿
        privacyView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        privacyView.getPaint().setAntiAlias(true);//抗锯齿
        permissionsView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        permissionsView.getPaint().setAntiAlias(true);//抗锯齿
        pruductView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        pruductView.getPaint().setAntiAlias(true);//抗锯齿
    }

    public void initData(String ver, String developerName) {
//        if (!TextUtils.isEmpty(ver)) {
//            verView.setText(ver);
//        }
//        if (!TextUtils.isEmpty(developerName)) {
//            developerView.setText(developerName);
//        }
    }

    private int getLayoutId() {
        return ResourceUtil.getLayoutId(getContext(), "sig_app_info_layout");
    }

}
