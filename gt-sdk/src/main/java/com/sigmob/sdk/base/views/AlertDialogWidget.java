package com.sigmob.sdk.base.views;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.utils.Dips;
import com.sigmob.sdk.videoAd.DialogConfig;

public class AlertDialogWidget extends RelativeLayout {
    private final int mShadowWidth;
    private final int mShadowOffset;
    private final int mDialogHeight;
    private final int mTitleHeight;
    private final int mButtonWidth;
    private final int mDialogWidth;
    private final int mButtonHeight;
    private final int mMessageHeight;
    private Button mCancelButton;
    private Button mConfirmButton;
    private int mTime;
    private TextView mTextView;

    private DialogConfig mDialogConfig = null;

    public AlertDialogWidget(final Context context, DialogConfig dialogConfig) {
        super(context);
        mDialogConfig = dialogConfig;
        mShadowWidth = Dips.dipsToIntPixels(10, context);
        mShadowOffset = Dips.dipsToIntPixels(16, context);
        mDialogHeight = Dips.dipsToIntPixels(227, context);
        mDialogWidth = (int) ((float) mDialogHeight * 16 / 9.0);

        mTitleHeight = Dips.dipsToIntPixels(25, context);

        mMessageHeight = Dips.dipsToIntPixels(25, context);
        mButtonWidth = Dips.dipsToIntPixels(100, context);
        mButtonHeight = mTitleHeight + mShadowOffset;

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT);

//        setBackgroundColor(Color.parseColor("#50000000"));
        setLayoutParams(layoutParams);
        createDialogView(context);

    }

    public void setduration(int time) {
        mTime = time;
        if (mTextView != null)
            mTextView.setText(mDialogConfig.getContext().replace("_SEC_", String.valueOf(mTime)));
    }

    public interface OnAlertDiaglogWidgetListener {
        void onCancel();

        void onConfirm();
    }

    private TextView createTitle(Context context) {

        TextView textView = new TextView(context);
        textView.setText(mDialogConfig.getTitle());
        textView.setTextColor(Color.BLACK);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setGravity(Gravity.CENTER);

        return textView;

    }

    private TextView createMessage(Context context) {

        mTextView = new TextView(context);
        mTextView.setTextColor(Color.BLACK);
        mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        mTextView.setGravity(Gravity.CENTER);

        return mTextView;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
    }

    private Button createCancelButton(Context context) {

        Button button = new Button(context);

        button.setText(mDialogConfig.getCancel());
        button.setTextColor(Color.parseColor("#ff999999"));

        button.setBackgroundColor(0);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
        layoutParams.setMargins(0, mShadowOffset / 2, 0, 0);

        button.setGravity(Gravity.CENTER);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

        button.setLayoutParams(layoutParams);
        return button;
    }

    private Button createConfirmButton(Context context) {

        Button button = new Button(context);
        button.setText(mDialogConfig.getClose());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(MATCH_PARENT, mButtonHeight);
        layoutParams.setMargins(mShadowWidth * 5, mShadowWidth * 2, mShadowWidth * 5, 0);
        button.setLayoutParams(layoutParams);

        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(0);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);

        ShadowDrawable.setShadowDrawable(button, DrawableConstants.BlueColor, mButtonHeight / 2,
                Color.parseColor("#66000000"), 0
                , 0, 0);


        return button;
    }

    private void createDialogView(Context context) {


        RelativeLayout relativeLayout = new RelativeLayout(context);

        LinearLayout linearLayout = new LinearLayout(context);

        linearLayout.setOrientation(LinearLayout.VERTICAL);

        LayoutParams layoutParams = new LayoutParams(MATCH_PARENT, mTitleHeight);
        layoutParams.setMargins(mShadowOffset, mShadowWidth * 2, mShadowOffset, 0);
        linearLayout.addView(createTitle(context), layoutParams);

        layoutParams = new LayoutParams(MATCH_PARENT, mMessageHeight);
        layoutParams.setMargins(mShadowOffset, mShadowWidth, mShadowOffset, 0);
        linearLayout.addView(createMessage(context), layoutParams);


        ShadowDrawable.setShadowDrawable(linearLayout, Color.parseColor("#FFFFFF"), mShadowWidth,
                Color.parseColor("#66000000"), 0
                , 0, 0);


        layoutParams = new RelativeLayout.LayoutParams(mDialogWidth, mDialogHeight);
        layoutParams.setMargins(mShadowOffset, 0, mShadowOffset, 0);
        layoutParams.addRule(CENTER_IN_PARENT);

        relativeLayout.setLayoutParams(layoutParams);
        relativeLayout.addView(linearLayout, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        mConfirmButton = createConfirmButton(context);
        linearLayout.addView(mConfirmButton);
        mCancelButton = createCancelButton(context);
        linearLayout.addView(mCancelButton);
        linearLayout.setGravity(Gravity.CENTER);


        addView(relativeLayout);

    }

    public void setDialogListener(final OnAlertDiaglogWidgetListener listener) {

        if (mCancelButton != null) {
            mCancelButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCancel();
                }
            });
        }

        if (mConfirmButton != null) {
            mConfirmButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onConfirm();
                }
            });
        }
    }
}
