package com.sigmob.sdk.base.views;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TitleDescriptionView extends LinearLayout {
    private TextView mTitleView;
    private TextView mDescriptionView;

    public TitleDescriptionView(Context context) {
        super(context);
        init();
    }

    public TitleDescriptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public TitleDescriptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mTitleView = new TextView(getContext());
        mTitleView.setId(View.generateViewId());
        mDescriptionView = new TextView(getContext());

        setOrientation(LinearLayout.VERTICAL);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        mTitleView.setTextColor(Color.WHITE);
        mTitleView.setSingleLine();
        mTitleView.setGravity(Gravity.CENTER);
        mTitleView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        mDescriptionView.setSingleLine();
        mDescriptionView.setTextColor(Color.WHITE);
        mDescriptionView.setGravity(Gravity.CENTER);
        mDescriptionView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        addView(mTitleView, lp);
        LayoutParams lp2 = new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(mDescriptionView, lp2);

    }

    public void setTitle(String title) {
        mTitleView.setText(title);
    }

    public void setDescription(String description) {
        mDescriptionView.setText(description);
    }

}
