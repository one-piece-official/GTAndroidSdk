package com.sigmob.sdk.base.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.czhj.sdk.common.ClientMetadata;
import com.czhj.sdk.common.utils.Dips;
import com.czhj.sdk.common.utils.ImageManager;
import com.czhj.sdk.logger.SigmobLog;
import com.sigmob.sdk.base.common.AdStackManager;
import com.sigmob.sdk.base.models.BaseAdUnit;

public class CompanionAdsWidget extends RelativeLayout {

    private final ImageView mIconView;
    private final TextView mActionView;
    private final TextView mTitletextView;
    private final int mStyleType;
    private TextView mRatingtextView;
    private TextView mReviewtextView;

    private FiveStarView mFiveStarView;
    private CompanionAdsWidget self = null;
    int wheight;
    private int mAnimate_type = 0;

    private boolean willBeShow;
    private int mClickType;
    private int margins;

    private boolean isShowed;
    private boolean isHide;
    private boolean isPropertyShow;

    public LinearLayout getFourElementsLayout() {
        return mFourElementsLayout;
    }

    private LinearLayout mFourElementsLayout;

    private BaseAdUnit mAdUnit;

    public CompanionAdsWidget(Context context, BaseAdUnit adUnit, final String title, String actionStr, float rating, String desc, int animType, int styleType, final String iconUrl, final int buttonColor,
                              final int buttonTextColor, int click_type, final int barColor,
                              final int barAlp, final float height) {
        super(context);

        wheight = Dips.asIntPixels(height, context);
        margins = Dips.asIntPixels(10, context);

        mAdUnit = adUnit;

        mAnimate_type = animType;

        mStyleType = styleType;

        mTitletextView = new TextView(context);
        mTitletextView.setText(title);
        mTitletextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        mTitletextView.setTextColor(Color.BLACK);
        mTitletextView.setSingleLine();
        mTitletextView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
        mTitletextView.setEllipsize(TextUtils.TruncateAt.END);
        mTitletextView.setGravity(Gravity.CENTER_VERTICAL);
        mTitletextView.setId(ClientMetadata.generateViewId());
        mTitletextView.setPadding(margins / 6, 0, 0, 0);

        mClickType = click_type;
        mActionView = new TextView(context);
        mActionView.setId(ClientMetadata.generateViewId());
        mActionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        mActionView.setTextColor(buttonTextColor);
        mActionView.setGravity(Gravity.CENTER);
        mTitletextView.setSingleLine();
        mActionView.setEllipsize(TextUtils.TruncateAt.END);
        mActionView.setText(actionStr);


        int margins = Dips.asIntPixels(5, context);
        GradientDrawable gd = new GradientDrawable();//创建drawable
        gd.setColor(buttonColor);
        gd.setCornerRadius(margins * 6);

        mIconView = new CircleImageView(context);
        mIconView.setScaleType(ImageView.ScaleType.FIT_START);
        mIconView.setId(ClientMetadata.generateViewId());

        ((CircleImageView) mIconView).isCircle = false;
        ((CircleImageView) mIconView).radius = margins * 2;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mActionView.setBackground(gd);
        } else {
            mActionView.setBackgroundDrawable(gd);
        }

        mActionView.setId(ClientMetadata.generateViewId());

        GradientDrawable bargd = new GradientDrawable();//创建drawable

        bargd.setCornerRadius(margins * 2);
        bargd.setColor(barColor);
        bargd.setAlpha(barAlp);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(bargd);
        } else {
            setBackgroundDrawable(bargd);
        }

        self = this;

        if (mStyleType == 1) {
            addRatingStarView(context, rating);
        } else {
            addDescTextView(context, desc);
        }

        if (adUnit.getMaterial() != null && adUnit.getMaterial().ad_privacy != null) {
            addFourElementsLayout(context);
        }

        setupUILayout(context);
        mIconView.setVisibility(INVISIBLE);
        loadIconImageView(iconUrl);
        hideView();
    }

    public void addFourElementsLayout(final Context context) {
        mFourElementsLayout = new LinearLayout(context);
        mFourElementsLayout.setOrientation(LinearLayout.HORIZONTAL);
        mFourElementsLayout.setId(ClientMetadata.generateViewId());
        int margins = Dips.asIntPixels(2, context);
        String[] text = {"名称", "版本", "开发者", "权限", "隐私协议", "介绍"};
        for (int i = 0; i < text.length; i++) {
            TextView textView = new TextView(context);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 8);
            textView.setText(text[i]);
            textView.setTextColor(Color.GRAY);
//            textView.setBackgroundColor(Color.GRAY);
            textView.setAlpha(0.7f);
            textView.getPaint().setAntiAlias(true);
            textView.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
            textView.setPadding(margins / 2, 0, margins / 2, 0);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, margins, 0);
            mFourElementsLayout.addView(textView, layoutParams);

        }
    }

    public void addRatingStarView(Context context, float rating) {

        mRatingtextView = new TextView(context);
        mRatingtextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        mRatingtextView.setId(ClientMetadata.generateViewId());
        mRatingtextView.setGravity(Gravity.CENTER);
        mRatingtextView.setTextColor(Color.GRAY);
        if (rating > 0) {
            mRatingtextView.setText(String.format("%.1f", rating));
        }

        mFiveStarView = new FiveStarView(context, rating);

    }


    public void addDescTextView(Context context, String desc) {
        mReviewtextView = new TextView(context);
        mReviewtextView.setId(ClientMetadata.generateViewId());
        mReviewtextView.setTextColor(Color.GRAY);
        mReviewtextView.setSingleLine();
        mReviewtextView.setGravity(Gravity.CENTER);
        mReviewtextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        mReviewtextView.setEllipsize(TextUtils.TruncateAt.END);
        mReviewtextView.setText(desc);

    }

    public boolean isShowed() {
        return isShowed;
    }

    @Override
    public void setOnTouchListener(OnTouchListener l) {
        if (mClickType == 1) {
            mActionView.setOnTouchListener(l);
        } else {
            super.setOnTouchListener(l);
        }
    }

    @Override
    public int getVisibility() {
        int vis = super.getVisibility();

        return vis;
    }

    public boolean isHide() {
        return isHide;
    }

    @Override
    public void setVisibility(int visibility) {

        if (visibility == GONE) {
            super.setVisibility(visibility);
            willBeShow = false;
        } else if (visibility == VISIBLE) {
            willBeShow = true;
            Animation animation = self.getAnimation();
            if (animation != null || isPropertyShow) {
                return;
            }
            if (mIconView.getVisibility() == VISIBLE) {
                startAnimator();
            }
        } else if (visibility == INVISIBLE) {
            Animation animation = self.getAnimation();
            if (animation != null) {
                return;
            }
            startUpToDownAnimator();

        } else {
            super.setVisibility(visibility);

        }
    }

    private void startAnimator() {

        switch (mAnimate_type) {
            case 1: {

                startCustomAnimator();
            }
            break;
            case 2: {
                startDownToUpAnimator();
            }
            break;
            case 3: {
                showView();
            }
            break;
            default: {
                startDownToUpAnimator();
            }
            break;

        }
    }

    private void startSpringAnimator() {

        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofFloat("scaleX", 0.3f, 0.5f, 1f);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofFloat("scaleY", 0.3f, 0.5f, 1f);

        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(this, pvh1, pvh2);
        animator.setInterpolator(new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                SigmobLog.e("input: " + input);
                return (float) (Math.pow(2, -10 * input) * Math.sin((input - 0.4f / 3f) * (2 * Math.PI) / 0.4f) + 1);

            }
        });
        showView();

        animator.setDuration(2000);
        animator.start();

    }

    @SuppressLint("ObjectAnimatorBinding")
    private void startCustomAnimator() {

        Keyframe k0 = Keyframe.ofFloat(0f, 0.0f); //第一个参数为“何时”，第二个参数为“何地”
        Keyframe k1 = Keyframe.ofFloat(0.5f, 0.95f);
        Keyframe k2 = Keyframe.ofFloat(0.6f, 0.90f);
        Keyframe k3 = Keyframe.ofFloat(0.7f, 1.0f);
        Keyframe k4 = Keyframe.ofFloat(0.8f, 0.95f);
        Keyframe k5 = Keyframe.ofFloat(1.0f, 1.0f);
        PropertyValuesHolder pvh1 = PropertyValuesHolder.ofKeyframe("scaleX", k0, k1, k2, k3, k4, k5);
        PropertyValuesHolder pvh2 = PropertyValuesHolder.ofKeyframe("scaleY", k0, k1, k2, k3, k4, k5);

        ObjectAnimator scaleAnimator = ObjectAnimator.ofPropertyValuesHolder(this, pvh1, pvh2);

        scaleAnimator.setDuration(1000);
        showView();
        isPropertyShow = true;
        scaleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                isHide = false;
                isPropertyShow = false;
                self.clearAnimation();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                isHide = false;
                isPropertyShow = false;

                self.clearAnimation();
            }

        });
        scaleAnimator.start();

    }


    private void showView() {
        isShowed = true;
        super.setVisibility(VISIBLE);
    }

    private void hideView() {
        super.setVisibility(INVISIBLE);
    }

    private void startDownToUpAnimator() {

        SigmobLog.d("startDownToUpAnimator");

        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 1f, Animation.RELATIVE_TO_PARENT, 0f);

        translateAnimation.setInterpolator(new LinearInterpolator());
        translateAnimation.setDuration(500);
        showView();
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isHide = false;

                self.clearAnimation();
                SigmobLog.d("startDownToUpAnimator end");

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        self.startAnimation(translateAnimation);

    }

    private void startUpToDownAnimator() {


        SigmobLog.d("startUpToDownAnimator");
        TranslateAnimation translateAnimation = new TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 0f, Animation.RELATIVE_TO_PARENT, 1f);

        translateAnimation.setInterpolator(new LinearInterpolator());
        translateAnimation.setDuration(500);
        translateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                isHide = true;
                self.clearAnimation();
                hideView();
                SigmobLog.d("startUpToDownAnimator end");

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        self.startAnimation(translateAnimation);

    }


    public void loadIconImageView(String logoUrl) {

        if (!TextUtils.isEmpty(logoUrl)) {

            AdStackManager.getImageManger().getBitmap(logoUrl, new ImageManager.BitmapLoadedListener() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap) {
                    mIconView.setImageBitmap(bitmap);
                    mIconView.setVisibility(VISIBLE);
                    if (willBeShow) {
                        startAnimator();
                    }
                }

                @Override
                public void onBitmapLoadFailed() {

                }
            });

        }
    }

    private void setupUILayout(Context context) {

        int iconHeight = Dips.asIntPixels(60, context);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(iconHeight, iconHeight);
        layoutParams.addRule(CENTER_VERTICAL);
        layoutParams.setMargins(margins, margins / 4, 0, 0);
        mIconView.setId(ClientMetadata.generateViewId());
        addView(mIconView, layoutParams);


        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (iconHeight / 1.8f));
        layoutParams.setMargins(0, 0, margins, 0);
        layoutParams.addRule(CENTER_VERTICAL);
        layoutParams.addRule(ALIGN_PARENT_RIGHT);
        mActionView.setPadding(margins, 0, margins, 0);
        addView(mActionView, layoutParams);


        LinearLayout myLayout = new LinearLayout(context);
        myLayout.setOrientation(LinearLayout.VERTICAL);
//        myLayout.setBackgroundColor(Color.YELLOW);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(margins / 2, 0, 0, 0);
        layoutParams.addRule(CENTER_VERTICAL);
        layoutParams.addRule(RIGHT_OF, mIconView.getId());
        layoutParams.addRule(LEFT_OF, mActionView.getId());
        addView(myLayout, layoutParams);

        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        myLayout.addView(mTitletextView, layoutParams);

        if (mRatingtextView != null) {

            RelativeLayout layout = new RelativeLayout(context);
//            layout.setBackgroundColor(Color.GREEN);

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layout.addView(mRatingtextView, layoutParams);

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RIGHT_OF, mRatingtextView.getId());
            layoutParams.addRule(ALIGN_TOP, mRatingtextView.getId());
            layoutParams.addRule(ALIGN_BOTTOM, mRatingtextView.getId());
            layout.addView(mFiveStarView, layoutParams);

            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            myLayout.addView(layout, layoutParams);

        } else if (mReviewtextView != null) {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            myLayout.addView(mReviewtextView, layoutParams);
        }

        if (mFourElementsLayout != null) {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(ALIGN_BOTTOM, mIconView.getId());
            myLayout.addView(mFourElementsLayout, layoutParams);
        }

    }

//    private void setupUILayout(Context context) {
//
//        int iconHeight = Dips.asIntPixels(60, context);
//
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(iconHeight, iconHeight);
//        layoutParams.addRule(CENTER_VERTICAL);
//        layoutParams.setMargins(margins, margins / 4, 0, 0);
//        addView(mIconView, layoutParams);
//
//        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.setMargins(margins / 2, 0, 0, 0);
//        layoutParams.addRule(ALIGN_TOP, mIconView.getId());
//        layoutParams.addRule(RIGHT_OF, mIconView.getId());
//        layoutParams.addRule(LEFT_OF, mActionView.getId());
////        mTitletextView.setBackgroundColor(Color.RED);
//        addView(mTitletextView, layoutParams);
//
//        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.setMargins(margins / 2, 0, 0, 0);
//        layoutParams.addRule(RIGHT_OF, mIconView.getId());
//        layoutParams.addRule(ALIGN_BOTTOM, mIconView.getId());
//        layoutParams.addRule(LEFT_OF, mActionView.getId());
////        mLayout.setBackgroundColor(Color.BLUE);
//        addView(mFourElementsLayout, layoutParams);
//
//
//        if (mRatingtextView != null) {
//
//            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.setMargins(0, 0, margins / 2, 0);
//            layoutParams.addRule(ALIGN_LEFT, mTitletextView.getId());
//            layoutParams.addRule(BELOW, mTitletextView.getId());
//            layoutParams.addRule(ABOVE, mFourElementsLayout.getId());
//            layoutParams.addRule(RIGHT_OF, mIconView.getId());
////            mRatingtextView.setBackgroundColor(Color.RED);
//            addView(mRatingtextView, layoutParams);
//
//            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.addRule(RIGHT_OF, mRatingtextView.getId());
//            layoutParams.addRule(LEFT_OF, mActionView.getId());
//            layoutParams.addRule(ALIGN_TOP, mRatingtextView.getId());
//            layoutParams.addRule(ALIGN_BOTTOM, mRatingtextView.getId());
////            mFiveStarView.setBackgroundColor(Color.BLUE);
//            addView(mFiveStarView, layoutParams);
//
//        } else if (mReviewtextView != null) {
//            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            layoutParams.setMargins(0, 0, 0, 0);
//            layoutParams.addRule(ALIGN_LEFT, mTitletextView.getId());
//            layoutParams.addRule(LEFT_OF, mActionView.getId());
//            layoutParams.addRule(BELOW, mTitletextView.getId());
//            layoutParams.addRule(ABOVE, mFourElementsLayout.getId());
//            layoutParams.addRule(RIGHT_OF, mIconView.getId());
////            mReviewtextView.setBackgroundColor(Color.BLUE);
//            addView(mReviewtextView, layoutParams);
//        }
//
//        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) (iconHeight / 1.8f));
//        layoutParams.setMargins(0, 0, margins, 0);
//        layoutParams.addRule(CENTER_VERTICAL);
//        layoutParams.addRule(ALIGN_PARENT_RIGHT);
//        mActionView.setPadding(margins, 0, margins, 0);
//        addView(mActionView, layoutParams);
//
//    }


}
