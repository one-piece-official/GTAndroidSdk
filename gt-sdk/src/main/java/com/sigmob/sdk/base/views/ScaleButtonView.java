package com.sigmob.sdk.base.views;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import com.czhj.sdk.common.utils.Dips;


public class ScaleButtonView extends View {
    private Paint borderPaint1, borderPaint2, borderPaint3, textPaint, arrowPaint;
    private RectF borderRect1, borderRect2, borderRect3;
    private Path arrowPath;
    private String title, description;
    private float centerX, centerY;
    private float titleWidth, titleHeight;
    private float arrowWidth, arrowHeight;
    private Handler handler = new Handler(Looper.getMainLooper());
    private int buttonColor = Color.parseColor("#FF5A57");
    private int strokeWidth;

    public ScaleButtonView(Context context) {
        super(context);
        init();
    }

    public ScaleButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ScaleButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setButtonColor(int color) {
        this.buttonColor = color;
    }

    private void init() {

        strokeWidth = Dips.dipsToIntPixels(2, getContext());
        borderPaint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint1.setStyle(Paint.Style.FILL);
        borderPaint1.setStrokeWidth(strokeWidth);

        borderPaint1.setColor(Color.WHITE);

        arrowWidth = Dips.dipsToIntPixels(20, getContext());
        borderPaint2 = new Paint(borderPaint1);
        borderPaint3 = new Paint(borderPaint1);
        borderPaint3.setStyle(Paint.Style.FILL);

        borderPaint3.setColor(buttonColor);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                20, getResources().getDisplayMetrics()));

        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.WHITE);
        arrowPaint.setAlpha((int) (255 * 0.5f));
        arrowPaint.setStyle(Paint.Style.FILL);

        arrowPath = new Path();
        borderRect1 = new RectF();
        borderRect2 = new RectF();
        borderRect3 = new RectF();
    }

    public void setTitleAndDesc(String title, String description) {
        this.title = title;
        this.description = description;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 4f;
        centerY = h / 3f;

        float borderRadius = h / 2f;

        borderRect1.set(centerX / 2, centerY / 2, w - centerX / 2, h - centerY / 2);
        borderRect2.set(centerX / 2, centerY / 2, w - centerX / 2, h - centerY / 2);
        borderRect3.set(centerX / 2, centerY / 2, w - centerX / 2, h - centerY / 2);


        if (!TextUtils.isEmpty(title)) {
            for (int i = 5; i < title.length(); i++) {
                float v = textPaint.measureText(title, 0, i);
                if (v > borderRect1.right - borderRect1.left - arrowWidth * 4) {
                    title = title.substring(0, i - 2) + "...";
                    break;
                }
            }

            titleWidth = textPaint.measureText(title);

            titleHeight = textPaint.descent() - textPaint.ascent();
        }

        arrowHeight = titleHeight * 0.6f;

        createArrowPath();
        startAnimation();

    }

    private void createArrowPath() {
        arrowPath.reset();
        arrowPath.moveTo(0, 0);
        arrowPath.lineTo(0, arrowHeight);
        arrowPath.lineTo(arrowHeight / 2, arrowHeight / 2);
        arrowPath.close();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw border layers
        canvas.drawRoundRect(borderRect1, borderRect1.height() / 2, borderRect1.height() / 2, borderPaint1);
        canvas.drawRoundRect(borderRect2, borderRect2.height() / 2, borderRect2.height() / 2, borderPaint2);
        canvas.drawRoundRect(borderRect3, borderRect3.height() / 2, borderRect3.height() / 2, borderPaint3);
        float arrowX = getWidth() / 2 + titleWidth / 2 + strokeWidth * 2;

        if (!TextUtils.isEmpty(title)) {
            // Draw title text
            float titleX = getWidth() / 2 - titleWidth / 2;
            float titleY = getHeight() / 2 - titleHeight / 2 - textPaint.ascent();
            canvas.drawText(title, titleX, titleY, textPaint);
            arrowX = titleX + textPaint.measureText(title) + strokeWidth * 2;
        }


        // Draw arrow
        float arrowY = getHeight() / 2 - arrowHeight / 2;
        canvas.save();
        canvas.translate(arrowX, arrowY);
        canvas.drawPath(arrowPath, arrowPaint);
        canvas.restore();
    }

    private void scaleRect(RectF source, RectF dest, float factor) {

        dest.left = source.left - factor;
        dest.top = source.top - factor;
        dest.right = source.right + factor;
        dest.bottom = source.bottom + factor;
    }

    private Animator borderAnimation(RectF rect, Paint paint, int duration, int delayTime) {

        AnimatorSet animatorSet = new AnimatorSet();
        RectF source = new RectF(rect.left, rect.top, rect.right, rect.bottom);
        ValueAnimator scale = ValueAnimator.ofFloat(0, Dips.dipsToIntPixels(20, getContext()));
        scale.setStartDelay(delayTime);
        scale.setDuration(duration);
//        scale.setRepeatCount(ValueAnimator.INFINITE);
//        scale.setRepeatMode(ValueAnimator.RESTART);
        scale.setInterpolator(new AccelerateInterpolator());
        scale.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                scaleRect(source, rect, (float) valueAnimator.getAnimatedValue());
                invalidate();
            }
        });

        ValueAnimator alpha = ValueAnimator.ofInt(255, 0);
        alpha.setStartDelay(delayTime);
        alpha.setDuration((long) (duration * 0.8f));
        alpha.setInterpolator(new AccelerateInterpolator());
        alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                paint.setAlpha((Integer) valueAnimator.getAnimatedValue());
                invalidate();
            }
        });

        animatorSet.playTogether(scale, alpha);

        return animatorSet;
    }

    public void startAnimation() {

        Animator scaleAnimator = borderAnimation(borderRect1, borderPaint1, 1000, 0);
        Animator scaleAnimator2 = borderAnimation(borderRect2, borderPaint2, 800, 400);

        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.playTogether(scaleAnimator, scaleAnimator2);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animator.start();
                    }
                }, 300);
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
        animatorSet.start();
    }

    public void stopAnimation() {
        borderRect1.set(0, 0, getWidth(), getHeight());
        borderRect2.set(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}
