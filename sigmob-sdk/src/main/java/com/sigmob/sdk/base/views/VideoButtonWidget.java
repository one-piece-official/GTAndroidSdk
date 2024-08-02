package com.sigmob.sdk.base.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.models.BaseAdUnit;

import java.io.File;

public class VideoButtonWidget extends RelativeLayout {
    private ImageView mImageView;
    private int mWidgetHeight;

    public VideoButtonWidget(final Context context) {
        super(context);
        mWidgetHeight = Dips.dipsToIntPixels(22, context);
        int widget = Dips.dipsToIntPixels(15, context);
        int roundRadius = mWidgetHeight / 2; //圆角半径

        createImageView(widget);

        final RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(Color.BLACK);
        gd.setStroke(1, Color.WHITE);
        gd.setCornerRadius(roundRadius);
        gd.setAlpha((int) (0.4f * 255.0f));
        setBackground(gd);
        setLayoutParams(layoutParams);

    }

    private void createImageView(int width) {

        mImageView = new ImageView(getContext());

        final RelativeLayout.LayoutParams iconLayoutParams = new RelativeLayout.LayoutParams(width, width);

        iconLayoutParams.addRule(CENTER_IN_PARENT);

        mImageView.setImageBitmap(Drawables.CLOSE_NEW.getBitmap());
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mImageView.setImageAlpha((int) (0.8f * 255.0f));
        addView(mImageView, iconLayoutParams);
    }

    public void updateCloseButtonIcon(BaseAdUnit adUnit) {

        if (adUnit != null && adUnit.getEndcardCloseImage() == 1) {
            mImageView.setImageBitmap(Drawables.CLOSE_NEW.getBitmap());
        } else {
            GradientDrawable gd = new GradientDrawable();//创建drawable
            gd.setColor(Color.TRANSPARENT);
            setBackground(gd);
            RelativeLayout.LayoutParams iconLayoutParams = new RelativeLayout.LayoutParams(mWidgetHeight, mWidgetHeight);
            iconLayoutParams.addRule(CENTER_IN_PARENT);
            mImageView.setLayoutParams(iconLayoutParams);
            mImageView.setImageBitmap(Drawables.CLOSE_OLD.getBitmap());
        }
    }

    public void updateButtonIcon(final Bitmap bitmap) {
        mImageView.setImageBitmap(bitmap);
    }

    public void updateButtonIcon(final int resId) {

        mImageView.setImageResource(resId);
    }

    public void updateButtonIcon(final String imageUrl) {

        String tempUrl = imageUrl.toLowerCase();
        if (tempUrl.startsWith("http://") || tempUrl.startsWith("https://")) {

            updateButtonIconURL(imageUrl);

        } else if (tempUrl.startsWith("file://")) {

            try {
                mImageView.setImageURI(Uri.fromFile(new File(imageUrl)));
            } catch (Throwable e) {
                SigmobLog.e(e.getMessage());
            }
        }
    }

    private void updateButtonIconURL(final String imageUrl) {

        AdStackManager.getImageManger().getBitmap(imageUrl, new ImageManager.BitmapLoadedListener() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap) {
                if (bitmap != null) {
                    mImageView.setImageBitmap(bitmap);
                } else {
                    SigmobLog.d(String.format("%s returned null bitmap", imageUrl));
                }
            }

            @Override
            public void onBitmapLoadFailed() {

            }
        });

    }


//    public void setOnTouchListenerToContent(View.OnTouchListener onTouchListener) {
//        mImageView.setOnTouchListener(onTouchListener);
//    }

}
