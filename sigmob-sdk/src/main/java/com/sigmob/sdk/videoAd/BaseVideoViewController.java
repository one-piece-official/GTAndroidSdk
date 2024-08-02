package com.sigmob.sdk.videoAd;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;
import static com.sigmob.sdk.base.WindConstants.ENABLEKEEPON;
import static com.sigmob.sdk.base.WindConstants.ENABLESCREENLOCKDISPLAYAD;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.BaseAdViewController;
import com.sigmob.sdk.base.common.BaseAdViewControllerListener;


public abstract class BaseVideoViewController extends BaseAdViewController {

    protected BaseVideoViewController(Activity activity, String broadcastIdentifier, BaseAdViewControllerListener baseAdViewControllerListener) {
        super(activity, broadcastIdentifier, baseAdViewControllerListener);
    }

    public void onCreate() {
        mBaseAdViewControllerListener.onSetContentView(mLayout);
    }

    protected void optionSetting(Context context, int orientation, Bundle intentExtras) {
        try {

            optionAdSize(context.getApplicationContext(), orientation, intentExtras);

            boolean enablekeepon = intentExtras.getBoolean(ENABLEKEEPON, false);
            boolean enablescreenlockdisplayad = intentExtras.getBoolean(ENABLESCREENLOCKDISPLAYAD, false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {


                if (enablekeepon) {
                    getActivity().setTurnScreenOn(true);
                }
                if (enablescreenlockdisplayad) {

                    getActivity().setShowWhenLocked(true);
                    getActivity().setTurnScreenOn(true);

                }
                if (enablekeepon) {
                    getActivity().getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
                }

                if (enablescreenlockdisplayad) {

                    getActivity().getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED | FLAG_TURN_SCREEN_ON);
                }


            } else {

                if (enablekeepon) {
                    getActivity().getWindow().addFlags(FLAG_KEEP_SCREEN_ON);
                }

                if (enablescreenlockdisplayad) {

                    getActivity().getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED | FLAG_TURN_SCREEN_ON);
                }


            }
        } catch (Throwable th) {
            SigmobLog.e("optionSetting error",th);
        }
    }


    protected void videoCompleted(boolean shouldFinish) {
        if (shouldFinish) {
            mBaseAdViewControllerListener.onFinish();
        }
    }

}
