package com.sigmob.sdk.base.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;


import com.czhj.sdk.common.utils.Dips;

public class SwingPhoneView extends View {

    private Paint paint;
    private RectF rectF;
    private float progressHeight;
    private float leftProgressWidth;
    private float rightProgressWidth;
    private int cornerRadius;
    private int strokeWidth;
    private int homeWidth;
    private int homeHeight;
    private int homeBottomOffset;
    private int width;
    private int height;
    private Path path;
    private AnimatorSet animationSet;
    private boolean isRunning;

    public SwingPhoneView(Context context) {
        super(context);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        path = new Path();
        rectF = new RectF();
        obseverLayout();
    }

    public SwingPhoneView(Context context, AttributeSet attrs) {
        super(context, attrs);
        obseverLayout();
    }

    public SwingPhoneView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obseverLayout();
    }

    private void obseverLayout() {
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                SwingPhoneView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                init();
            }
        });
    }

    private void init() {


        width = getWidth();
        height = getHeight();

        if (width == 0 || height == 0) return;


        paint.setStyle(Paint.Style.STROKE);
        strokeWidth = Dips.dipsToIntPixels(1, getContext());
        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.WHITE);
        cornerRadius = (int) ((width / (75f * strokeWidth)) * Dips.dipsToIntPixels(5.0f, getContext()));

        setLayerType(LAYER_TYPE_HARDWARE, null);
        homeWidth = Math.max((int) ((width / (100f*0.25f * strokeWidth)) * strokeWidth * 11), 3 * strokeWidth);
        homeHeight = Math.max((int) (((width / (100f*0.25f * strokeWidth)) * 3 * strokeWidth)), 2 * strokeWidth);
        homeBottomOffset = Math.max(((int) ((width / (100f*0.25f * strokeWidth)) * 11 * strokeWidth)), 5 * strokeWidth);
        addTransformAnimation();

    }

    private Animator getPortraitRotationAnimation(int angle) {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator rotation = ObjectAnimator.ofFloat(this, "rotation", 0, angle, 0);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, -height * 0.6f, 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate( ValueAnimator animation) {
                progressHeight = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(1000);
        animatorSet.playTogether(rotation, valueAnimator);
        return animatorSet;
    }

    private Animator getFlipLeftAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator flipLeft = ObjectAnimator.ofFloat(this, "rotationY", 0, 45, 0);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, width * 0.6f, 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                leftProgressWidth = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(1000);
        animatorSet.playTogether(flipLeft, valueAnimator);
        return animatorSet;
    }

    private Animator getFlipRightAnimation() {
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator flipLeft = ObjectAnimator.ofFloat(this, "rotationY", 0, -45, 0);
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, -width * 0.6f, 0);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate( ValueAnimator animation) {
                rightProgressWidth = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        animatorSet.setInterpolator(new DecelerateInterpolator());
        animatorSet.setDuration(1000);
        animatorSet.playTogether(flipLeft, valueAnimator);
        return animatorSet;
    }

    private void addTransformAnimation() {
        if (animationSet != null){
            animationSet.cancel();
        }

        animationSet = new AnimatorSet();
        Animator rotationLeft = getPortraitRotationAnimation(-45);
        Animator rotationRight = getPortraitRotationAnimation(45);

        Animator flipLeft = getFlipLeftAnimation();
        Animator flipRight = getFlipRightAnimation();
        animationSet.playSequentially(rotationLeft, rotationRight, flipLeft, flipRight);
        animationSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart( Animator animator) {

            }

            @Override
            public void onAnimationEnd( Animator animator) {

               if(animationSet != null && isRunning) {
                   animationSet.start();
               }
            }

            @Override
            public void onAnimationCancel( Animator animator) {

            }

            @Override
            public void onAnimationRepeat( Animator animator) {

            }
        });
        if (isRunning) {
            animationSet.start();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (width == 0 || height == 0) return;

        // Draw round frame
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);

        path.reset();
        rectF.set(strokeWidth, strokeWidth, width - strokeWidth, height - strokeWidth);
        path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CCW);
        canvas.drawPath(path, paint);
        canvas.clipPath(path);

        // Draw background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.GRAY);
        paint.setAlpha((int) (255 * 0.8f));
        canvas.drawRect(0, 0, width, height, paint);

        // Draw portrait progress
        paint.setColor(Color.LTGRAY);
        paint.setAlpha(255);
        canvas.drawRect(0, height + progressHeight, width, height, paint);

        // Draw left landscape progress
        canvas.drawRect(-width * 0.6f + leftProgressWidth, 0, leftProgressWidth, height, paint);

        // Draw right landscape progress
        canvas.drawRect(width + rightProgressWidth, 0, width * 1.6f + rightProgressWidth, height, paint);

        // Draw line at the bottom
        paint.setStyle(Paint.Style.FILL);

        int lineX = (width - homeWidth) / 2;
        int lineY = height - homeBottomOffset;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            canvas.drawRoundRect(lineX, lineY, lineX + homeWidth, lineY + homeHeight, homeHeight, homeHeight, paint);
        }else {
            canvas.drawRect(lineX, lineY, lineX + homeWidth, lineY + homeHeight, paint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        obseverLayout();
    }

    public void startAnimation() {
        isRunning = true;
       if (animationSet != null){
           animationSet.start();
       }
    }

    public void stopAnimation() {
        isRunning = false;

        if (animationSet != null){
            animationSet.cancel();
        }
    }
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }
}