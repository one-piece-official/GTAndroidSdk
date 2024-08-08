package com.gt.sdk.base.splash;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.czhj.sdk.common.utils.FileUtil;
import com.czhj.sdk.common.utils.ImageTypeUtil;
import com.czhj.sdk.logger.SigmobLog;
import com.gt.sdk.base.common.BaseBroadcastReceiver;
import com.gt.sdk.base.models.BaseAdUnit;
import com.gt.sdk.base.models.IntentActions;
import com.gt.sdk.base.view.gif.GifImageView2;

import java.util.Arrays;
import java.util.List;

public class SplashAdImageContentView extends SplashAdContentView {

    private final GifImageView2 imageView;

    @Override
    public void onPause() {
        super.onPause();
        if (imageView != null) {
            imageView.stopAnimation();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (imageView != null) {
            imageView.startAnimation();
        }
    }

    public SplashAdImageContentView(Context context) {
        super(context);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        imageView = new GifImageView2(context);
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        setBackgroundColor(Color.WHITE);

        addView(imageView, layoutParams);
    }

    @Override
    public boolean loadResource(BaseAdUnit adUnit) {
        if (adUnit == null || adUnit.getSplashFilePath() == null) {
            SigmobLog.e("adUnit or splashFilePath is null");
            return false;
        }
        String filePath = adUnit.getSplashFilePath();
        List<String> supportImageType = Arrays.asList("git", "jpeg", "jpg", "png", "bmp", "webp", "tif");

        final String fileType = ImageTypeUtil.getFileType(filePath);

        if (TextUtils.isEmpty(fileType)) return false;

        if (fileType.equals("gif")) {
            imageView.setBytes(FileUtil.readBytes(filePath));
            imageView.startAnimation();
            BaseBroadcastReceiver.broadcastAction(getContext(), adUnit.getUuid(), IntentActions.ACTION_SPLASH_PLAY);
            return true;
        } else {
            if (supportImageType.contains(fileType)) {
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);
                if (bitmap != null) {
                    BaseBroadcastReceiver.broadcastAction(getContext(), adUnit.getUuid(), IntentActions.ACTION_SPLASH_PLAY);
                    imageView.setImageBitmap(bitmap);
                    return true;
                }
            }
        }
        return false;
    }

}