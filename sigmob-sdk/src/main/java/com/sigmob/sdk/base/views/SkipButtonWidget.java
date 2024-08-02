package com.sigmob.sdk.base.views;

import android.content.Context;
import android.os.Build;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.SigmobRes;


public class SkipButtonWidget extends RelativeLayout {
    private final OvalButton mSkipTextView;
    private final OvalButton mTimeTextView;
    //    private final int margins;
    int mWidgetHeight = 0;
    boolean isShowing = false;
    private int mTime;

    public SkipButtonWidget(Context context) {
        super(context);

        final LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mSkipTextView = new OvalButton(context);
        mTimeTextView = new OvalButton(context);

        setLayoutParams(layoutParams);
        final LayoutParams mTimeTextViewLP = new LayoutParams(mWidgetHeight, mWidgetHeight);

        mTimeTextViewLP.addRule(ALIGN_PARENT_LEFT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mTimeTextViewLP.addRule(ALIGN_PARENT_START);
        }

        mTimeTextView.setId(ClientMetadata.generateViewId());
        addView(mTimeTextView, mTimeTextViewLP);

    }

    public boolean isShowSkip() {
        return isShowing;
    }

    public void showSkip() {

        if (isShowing) return;
        isShowing = true;

        if (isShowing) {
            SigmobLog.d("show skip widget");
        }

        final LayoutParams mTimeTextViewLP2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, mWidgetHeight);
        mSkipTextView.setVisibility(VISIBLE);
        mTimeTextView.setVisibility(GONE);
        mSkipTextView.setPadding((int) (mWidgetHeight / 3.0f), 0, (int) (mWidgetHeight / 3.0f), 0);
        if (mTime > 0) {
            mSkipTextView.setText(SigmobRes.getClose(mTime));
        } else {
            mSkipTextView.setText(SigmobRes.getClose());

        }

        addView(mSkipTextView, mTimeTextViewLP2);

    }


    public int getTime() {
        return mTime;
    }

    public void updateDuration(final int time) {
        mTime = time;
        if (isShowing) {
            if (time > 0) {

//                if (time == 9) {
//                    int width = getMeasuredWidthAndState();
//                    if (width > 0 && width < 1000) {
//                        mSkipTextView.getLayoutParams().width = getMeasuredWidth();
//                    }
//                }
                mSkipTextView.setText(SigmobRes.getClose(mTime));
                if (mSkipTextView.getVisibility() != VISIBLE) {
                    mSkipTextView.setVisibility(VISIBLE);
                }

            }
        } else {
            if (time > 0) {
                mTimeTextView.setText(String.valueOf(time));
            }
        }

    }


}
