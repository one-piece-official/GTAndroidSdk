package com.sigmob.sdk.newInterstitial;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.common.utils.ResourceUtil;
import com.sigmob.sdk.base.blurkit.BlurKit;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.views.Drawables;
import com.sigmob.sdk.base.views.OvalButton;

public class NewInterstitialEndCardView extends RelativeLayout {

    private ImageView sigAdClose;
    private ImageView sigAppIcon;
    private TextView sigAdTitle;
    private TextView sigAdDesc;
    private Button cta_button;
    private ImageView endCardImageView;
    private SigAdPrivacyInfoView adPrivacyInfo;
    private OvalButton feedBack;
    private View sigAdRLClose;


    public NewInterstitialEndCardView(Context context) {
        super(context);
        init(context);
    }

    public NewInterstitialEndCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public NewInterstitialEndCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        View view = inflate(context, ResourceUtil.getLayoutId(context, "sig_new_interstitial_endcard_layout"), this);

        sigAdRLClose = view.findViewById(ResourceUtil.getId(context, "sig_ad_rl_close"));

        sigAdClose = view.findViewById(ResourceUtil.getId(context, "sig_ad_close"));
        if (sigAdClose != null) {
            sigAdClose.setImageBitmap(Drawables.CLOSE_OLD.getBitmap());
        }
        sigAppIcon = view.findViewById(ResourceUtil.getId(context, "sig_app_icon"));
        sigAdTitle = view.findViewById(ResourceUtil.getId(context, "sig_ad_title"));
        sigAdDesc = view.findViewById(ResourceUtil.getId(context, "sig_ad_desc"));
        cta_button = view.findViewById(ResourceUtil.getId(context, "sig_cta_button"));
        endCardImageView = view.findViewById(ResourceUtil.getId(context, "sig_endCard_image"));
        adPrivacyInfo = view.findViewById(ResourceUtil.getId(context, "sig_ad_privacy_info"));

    }

    public void showFeedback(View.OnClickListener onClickListener) {
        if (feedBack == null) {
            Context context = getContext();
            feedBack = new OvalButton(context);
            feedBack.setText("反馈");
            feedBack.setOnClickListener(onClickListener);
            feedBack.setId(ClientMetadata.generateViewId());
//            int padding = Dips.dipsToIntPixels(0, context);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(Dips.dipsToIntPixels(45, context), Dips.dipsToIntPixels(30, context));
            layoutParams.addRule(RelativeLayout.LEFT_OF, sigAdRLClose.getId());
            layoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, sigAdRLClose.getId());
//            layoutParams.setMargins(0, 0, 0, 0);
            ViewParent parent = sigAdRLClose.getParent();
            if (parent != null) {
                ((ViewGroup) parent).addView(feedBack, layoutParams);
            }
        }
    }

    public void setupEndCardView(String iconUrl, String imageUrl, String title, String description, String ctaTitle) {

        if (!TextUtils.isEmpty(iconUrl)) {
            AdStackManager.getImageManger().load(iconUrl).into(sigAppIcon);
        }
        if (!TextUtils.isEmpty(imageUrl) || !TextUtils.isEmpty(iconUrl)) {

            String url = TextUtils.isEmpty(imageUrl) ? iconUrl : imageUrl;
            AdStackManager.getImageManger().getBitmap(url, new ImageManager.BitmapLoadedListener() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap) {
                    if (bitmap != null) {
                        BlurKit.init(getContext());
                        Bitmap copy = bitmap.copy(bitmap.getConfig(), true);
                        Bitmap blur = BlurKit.getInstance().blur(copy, 25);
                        if (blur != null) {
                            endCardImageView.setImageBitmap(blur);
                        }
                    }

                }

                @Override
                public void onBitmapLoadFailed() {

                }
            });

        }
        if (!TextUtils.isEmpty(title)) {
            sigAdTitle.setText(title);
        }
        if (!TextUtils.isEmpty(description)) {
            sigAdDesc.setText(description);
        }
        if (!TextUtils.isEmpty(ctaTitle)) {
            cta_button.setText(ctaTitle);
        }

    }

    public SigAdPrivacyInfoView getAdPrivacyInfo() {
        return adPrivacyInfo;
    }

    public Button getCTAButton() {
        return cta_button;
    }

    public View getCloseButton() {
        return sigAdClose;
    }

}
