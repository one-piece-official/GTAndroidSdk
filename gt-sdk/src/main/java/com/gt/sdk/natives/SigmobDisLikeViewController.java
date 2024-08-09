package com.gt.sdk.natives;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ResourceUtil;
import com.gt.sdk.base.common.BaseAdViewController;
import com.gt.sdk.base.common.BaseAdViewControllerListener;
import com.gt.sdk.base.models.BaseAdUnit;


/**
 * 为什么看到此广告的详情页
 **/
public class SigmobDisLikeViewController extends BaseAdViewController {

    private RelativeLayout container;//装下面落地页的容器
    private LinearLayout llContent;//装视频的容器
    private TextView textView;
    private BaseAdUnit mAdUnit;
    private String text = "此广告由SigMob提供，为了在应用程序上向您推荐展示出更加个性和实用的广告，" +
            "对您可能会接收到的一部分广告进行更具相关性的定制，从而使您在应用程序上有更好的用户体验。" +
            "SigMob非常重视数据安全，将努力采取合理的安全措施（包括技术方面和管理方面）来保护数据安全，防止数据信息被不正当使用或未经授权的情况下被访问。";

    public SigmobDisLikeViewController(final Activity context,
                                       BaseAdUnit baseAdUnit,
                                       final Bundle intentExtras,
                                       final Bundle savedInstanceState,
                                       final String broadcastIdentifier,
                                       final BaseAdViewControllerListener baseAdViewControllerListener) {
        super(context, broadcastIdentifier, baseAdViewControllerListener);
        mAdUnit = baseAdUnit;
    }

    public void onCreate() {
//        SigUtils.hideSystemUI(mActivity);
//        SigUtils.hideStatusBar(mActivity);
//        getActivity().getActionBar().hide();
        mLayout.removeAllViews();
        mLayout.setBackgroundColor(Color.WHITE);
        mBaseAdViewControllerListener.onSetContentView(mLayout);
        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        mLayout.addView(layout, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        container = new RelativeLayout(getContext());
        layout.addView(container, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Dips.dipsToIntPixels(50, getContext())));

        View view = new View(getContext());
        view.setBackgroundColor(Color.parseColor("#E0E6EC"));
        layout.addView(view, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Dips.dipsToIntPixels(1, getContext())));

        int pixels = Dips.dipsToIntPixels(10, getContext());
        ImageView imageView = new ImageView(getContext());
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBaseAdViewControllerListener != null){
                    mBaseAdViewControllerListener.onFinish();
                }
            }
        });
        imageView.setImageResource(ResourceUtil.getDrawableId(getContext(), "sig_image_video_back_left_black"));
        RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(pixels * 2, pixels * 2);
        ivParams.setMargins(pixels, 0, pixels, 0);
        ivParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        ivParams.addRule(RelativeLayout.CENTER_VERTICAL);
        container.addView(imageView, ivParams);

        TextView tv = new TextView(getContext());
        tv.setText("为什么看到此广告");
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        tv.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams tvParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        container.addView(tv, tvParams);

        llContent = new LinearLayout(getContext());
        layout.addView(llContent, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        textView = new TextView(getContext());
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setLineSpacing(2, 1.20f);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        params.setMargins(pixels / 2, pixels / 2, pixels / 2, pixels / 2);
        llContent.addView(textView, params);
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {

    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean backButtonEnabled() {
        if (mBaseAdViewControllerListener != null){
            mBaseAdViewControllerListener.onFinish();
        }
        return false;
    }

    @Override
    public void onStart() {

    }
}
