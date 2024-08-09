package com.gt.sdk.natives;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ViewUtil;
import com.gt.sdk.base.common.SingleMotionManger;
import com.gt.sdk.base.view.MotionView;
import com.gt.sdk.base.view.ShakeNewView;
import com.gt.sdk.base.view.SlopeView;
import com.gt.sdk.base.view.SwingView;
import com.gt.sdk.base.view.WringView;

import java.util.Map;

public class SigmobMotionView extends RelativeLayout implements ViewTreeObserver.OnGlobalLayoutListener {

    private final int offset;
    private MotionView mWidgetView;
    private TextView mTextView;
    private SingleMotionManger.Motion motion;
    private boolean isClicked = false;
    private MotionActionListener actionListener;
    private String title;
    private boolean isAttachedToWindow;
    private static MotionView mActionView;
    private static SingleMotionManger.Motion mActionMotion;
    private int max_size;
    private int min_size;
    private boolean needUpdate;
    private Boolean isStart = null;

    public SigmobMotionView(Context context) {
        super(context);
        max_size = Dips.dipsToIntPixels(100, context);
        min_size = Dips.dipsToIntPixels(60, context);
        offset = Dips.dipsToIntPixels(5, getContext());
    }

    public boolean initWidgetView(int widgetId, int sensitivity) {
        switch (widgetId) {
            case 138757: {
                addShakeView(sensitivity);
            }
            break;
            case 138733: {
                addSlopView(sensitivity);
            }
            break;
            case 138758: {
                addWringView(sensitivity);
            }
            break;
            case 138731: {
                addSwingView(sensitivity);
            }
            break;
            default: {
//                if (WindConstants.IS_DEBUG) {
//                    addSwingView(sensitivity);
//                }
            }
        }
        if (mWidgetView != null) {
            LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
            mWidgetView.setId(ClientMetadata.generateViewId());
            addView(mWidgetView, layoutParams);

            mTextView = new TextView(getContext());
            mTextView.setText(title);
            mTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
            mTextView.setTextColor(Color.WHITE);
            mTextView.setTypeface(Typeface.DEFAULT_BOLD);
            mTextView.setId(ClientMetadata.generateViewId());

            return true;
        }
        return false;
    }

    private void observerView() {
        needUpdate = true;
        SigmobMotionView.this.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    private void addWringView(int sensitivity) {

        mWidgetView = new WringView(getContext());
        title = "扭动或点击前往";
        motion = new SingleMotionManger.Motion(getContext(), new SingleMotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null) {
                    if (isClicked) return;
                    isClicked = true;
                    Number turn_x = info.get("turn_x");
                    Number turn_y = info.get("turn_y");
                    Number turn_z = info.get("turn_z");
                    Number turn_time = info.get("turn_time");
                    mWidgetView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handleMotionClick();
                        }
                    }, 400);
                }
            }
        }, SingleMotionManger.MotionType.WRING);
        motion.setLevel(sensitivity);
    }

    private void addShakeView(int sensitivity) {
        mWidgetView = new ShakeNewView(getContext());
        title = "摇一摇或点击前往";
        motion = new SingleMotionManger.Motion(getContext(), new SingleMotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {

            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                Number x_max_acc = info.get("x_max_acc");
                Number y_max_acc = info.get("y_max_acc");
                Number z_max_acc = info.get("z_max_acc");

                mWidgetView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (isClicked) return;
                        handleMotionClick();

                    }
                }, 400);


            }
        }, SingleMotionManger.MotionType.SHAKE);
        motion.setLevel(sensitivity);
    }

    private void addSlopView(int sensitivity) {

        mWidgetView = new SlopeView(getContext());
        title = "前倾或点击前往";

        motion = new SingleMotionManger.Motion(getContext(), new SingleMotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {
                if (mWidgetView instanceof SlopeView) {
                    ((SlopeView) mWidgetView).updateScreen(progress);

                }
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null) {
                    Number x_max_acc = info.get("x_max_acc");
                    Number y_max_acc = info.get("y_max_acc");
                    Number z_max_acc = info.get("z_max_acc");

                    mWidgetView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (isClicked) return;
                            handleMotionClick();
                        }
                    }, 400);
                }
            }
        }, SingleMotionManger.MotionType.SLOPE);
        motion.setLevel(sensitivity);
    }

    private void addSwingView(int sensitivity) {

        mWidgetView = new SwingView(getContext());
        title = "晃动或点击前往";
        motion = new SingleMotionManger.Motion(getContext(), new SingleMotionManger.MotionListener() {
            @Override
            public void onMotionStart() {

            }

            @Override
            public void onMotionUpdate(float progress) {
                if (mWidgetView instanceof SwingView) {
                    ((SwingView) mWidgetView).updateProcess(progress);
                }
            }

            @Override
            public void onMotionEnd(Map<String, Number> info) {
                if (info != null) {
                    if (isClicked) return;
                    isClicked = true;

                    Number x_max_acc = info.get("x_max_acc");
                    Number y_max_acc = info.get("y_max_acc");
                    Number z_max_acc = info.get("z_max_acc");

                    mWidgetView.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            handleMotionClick();
                        }
                    }, 400);
                }
            }
        }, SingleMotionManger.MotionType.SWING);

        motion.setLevel(sensitivity);
    }

    private void handleMotionClick() {
        if (actionListener != null) {
            actionListener.onAction();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttachedToWindow = true;
        observerView();


    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        needUpdate = true;
    }

    protected void onStart() {

        if (!isAttachedToWindow || mWidgetView.getVisibility() != VISIBLE) {
            return;
        }
        if (mActionView != null && mActionView != mWidgetView) {
            mActionView.stopAnimator();
            mActionView = null;
        }
        if (mActionMotion != null && mActionMotion != motion) {
            mActionMotion.pause();
            mActionMotion = null;
        }

        isClicked = false;
        if (motion != null && isAttachedToWindow && ViewUtil.isViewVisible(this)) {
            if (mActionMotion != motion) {
                mActionMotion = motion;
                motion.start();
            }
            if (mWidgetView != null && mWidgetView != mActionView && mWidgetView.getVisibility() == VISIBLE) {
                mActionView = mWidgetView;
                mWidgetView.startAnimator();
            }
        }

    }

    public void onPause() {
        if (mWidgetView != null) {
            mWidgetView.stopAnimator();
        }

        if (motion != null) {
            motion.pause();
        }
        if (mActionView != null && mActionView == mWidgetView) {
            mActionView = null;
        }
        if (mActionMotion != null && mActionMotion == motion) {
            mActionMotion = null;
        }

    }

    public void destroy() {

        if (mActionView != null && mActionView == mWidgetView) {
            mActionView = null;
        }
        if (mActionMotion != null && mActionMotion == motion) {
            mActionMotion = null;
        }

        if (mWidgetView != null) {
            mWidgetView.stopAnimator();
            ViewUtil.removeFromParent(mWidgetView);
            mWidgetView = null;
        }
        if (mTextView != null) {
            ViewUtil.removeFromParent(mTextView);
            mTextView = null;
        }

        if (motion != null) {
            motion.destroy();
            motion = null;
        }

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttachedToWindow = false;
        needUpdate = false;
        SigmobMotionView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);

        onPause();
    }


    public void setMotionActionListener(MotionActionListener listener) {
        actionListener = listener;
    }

    @Override
    public void onGlobalLayout() {

        if (!needUpdate) {
            return;
        }
        needUpdate = false;
        int size = Math.min(SigmobMotionView.this.getWidth(), SigmobMotionView.this.getHeight());
        ViewUtil.removeFromParent(mTextView);
        if (size < min_size) {
            if (mWidgetView != null) {
                mWidgetView.stopAnimator();
                mWidgetView.setVisibility(INVISIBLE);
            }
            if (motion != null) {
                motion.pause();
            }
            isStart = false;
            return;
        }

        if (mWidgetView != null) {
            ViewGroup.LayoutParams layoutParams = mWidgetView.getLayoutParams();
            if (layoutParams != null && layoutParams.width != size) {
                layoutParams.width = size;
                layoutParams.height = size;
                mWidgetView.setLayoutParams(layoutParams);
            }
            if (isStart != null && !isStart) {
                mWidgetView.startAnimator();
                mWidgetView.setVisibility(VISIBLE);
                if (motion != null) {
                    motion.start();
                }
            }


            if (size < max_size) {
                return;
            }

            int[] childLocation = new int[2];
            int[] parentLocation = new int[2];
            SigmobMotionView.this.getLocationOnScreen(parentLocation);
            mWidgetView.getLocationOnScreen(childLocation);
            int relativeY = childLocation[1] - parentLocation[1];
            int top = SigmobMotionView.this.getTop() + size + relativeY;
            ViewGroup parent = (ViewGroup) SigmobMotionView.this.getParent();
            if (parent != null && mTextView != null) {
                ViewUtil.removeFromParent(mTextView);
                FrameLayout.LayoutParams titlelayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                titlelayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
                titlelayoutParams.setMargins(0, top + offset, 0, 0);
                parent.addView(mTextView, titlelayoutParams);
            }
        }
    }
}
