package com.sigmob.sdk.base.views;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.AdStackManager;

public class AdLogoView extends RelativeLayout {


    private final int layoutType;
    private ImageView mlogoView;
    private TextView mTextView;

    private static float MIN_FONT_SIZE_MDPI = 8.0f;
    private static final float GLOBAL_SCALE = 1.0f;

    public AdLogoView(Context context, int type) {
        super(context);


        layoutType = type;
        mlogoView = new ImageView(context);
        mlogoView.setId(ClientMetadata.generateViewId());

        mTextView = new TextView(context);
        mTextView.setTextColor(Color.parseColor("#B9B9B9"));

//        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (int) (MIN_FONT_SIZE_MDPI * GLOBAL_SCALE));

//        mTextView.setTextSize(14);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);

        int insetBottom = Dips.dipsToIntPixels(5, getContext());

        RelativeLayout.LayoutParams logoLayoutParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, Dips.dipsToIntPixels(16, context));
        RelativeLayout.LayoutParams logoTextLayoutParams = new RelativeLayout.LayoutParams(WRAP_CONTENT, Dips.dipsToIntPixels(16, context));

        mTextView.setGravity(Gravity.CENTER);
        mlogoView.setScaleType(ImageView.ScaleType.FIT_START);
        mlogoView.setAdjustViewBounds(true);
        mlogoView.setMaxWidth(Dips.dipsToIntPixels(40, context));
        mlogoView.setMinimumWidth(Dips.dipsToIntPixels(16, context));
        if (type == 1) {

            logoTextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            logoTextLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            logoTextLayoutParams.setMargins(insetBottom, 0, 0, insetBottom * 2);
            logoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            logoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            logoLayoutParams.setMargins(0, 0, insetBottom, insetBottom * 2);

        } else {

            logoLayoutParams.setMargins(insetBottom, 0, 0, 0);
            logoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            logoLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            logoTextLayoutParams.setMargins(0, 0, 0, 0);
            logoTextLayoutParams.addRule(RelativeLayout.RIGHT_OF, mlogoView.getId());
            logoTextLayoutParams.addRule(RelativeLayout.ALIGN_BOTTOM, mlogoView.getId());
        }

        mlogoView.setBackgroundColor(Color.alpha(0));

        addView(mlogoView, logoLayoutParams);

        addView(mTextView, logoTextLayoutParams);

    }


    public void showAdLogo(String url) {//https://n.sigmob.cn/icon/sig_logo1.png

        AdStackManager.getImageManger().getBitmap(url, new ImageManager.BitmapLoadedListener() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                int insetBottom = Dips.dipsToIntPixels(3, getContext());

                if (bitmap != null) {

                    if (layoutType != 1 && bitmap.getWidth() > bitmap.getHeight() * 1.5f) {
                        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mlogoView.getLayoutParams();
                        layoutParams.setMargins(-insetBottom * (bitmap.getWidth() / bitmap.getHeight()), 0, 0, 0);
                    }
                    mlogoView.setImageBitmap(bitmap);
                } else {

                }
            }

            @Override
            public void onBitmapLoadFailed() {

            }
        });

    }

    public void showAdLogo(Bitmap Bitmap) {
        if (Bitmap != null) {
            mlogoView.setImageBitmap(Bitmap);
        }
    }


    @Override
    public void setOnClickListener(OnClickListener l) {
        if (mlogoView != null) {
            mlogoView.setOnClickListener(l);
        }
    }

    public void showAdText(String adText) {

        try {
            mTextView.setText(adText);
        } catch (Throwable th) {
            SigmobLog.e("showAdText error", th);
        }

    }


}
