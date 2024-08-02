package com.sigmob.sdk.base.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public abstract class MotionView extends RelativeLayout {
    public MotionView(Context context) {
        super(context);
    }
    public MotionView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public MotionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
    public abstract void startAnimator();
    public abstract void stopAnimator();

}
