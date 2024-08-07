package com.gt.sdk.base.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.czhj.sdk.common.utils.Dips;

public class SlideButtonView extends View {

    private Paint paint;
    private RectF rect;
    private float lineWidth;
    private String title, desc;
    private Paint textPaint;
    private Path gradientPath;
    private Paint gradientPaint;
    private Path path;
    private int offset;
    private int h, w;
    private float scale = 1;
    private int width, height;
    private int centerX, centerY;
    private ValueAnimator animator;
    private ValueAnimator scaleAnimator;

    public SlideButtonView(Context context) {
        super(context);
        init();

    }

    public SlideButtonView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public SlideButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        setBackgroundColor(Color.RED);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rect = new RectF();
        path = new Path();
        lineWidth = Dips.dipsToIntPixels(2, getContext());
        setBackgroundColor(Color.TRANSPARENT);


    }

    private void startAnimator() {
        if (animator != null) {
            animator.cancel();
        }
        animator = ValueAnimator.ofInt(-w, width + height);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                offset = (int) valueAnimator.getAnimatedValue();
                updateGradient();
                invalidate();
            }
        });
        animator.setDuration(800);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.RESTART);

        animator.start();

        if (scaleAnimator != null) {
            scaleAnimator.cancel();
        }

        scaleAnimator = ValueAnimator.ofFloat(1.0f, 1.15f);
        scaleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                scale = (float) valueAnimator.getAnimatedValue();
//                invalidate();
            }
        });
        scaleAnimator.setDuration(1000);
        scaleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        scaleAnimator.setRepeatMode(ValueAnimator.REVERSE);

        scaleAnimator.start();

    }

    public void setTitleAndDesc(String title, String desc) {
        this.title = title;
        this.desc = desc;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw the border


        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setAlpha((int) (255 * 0.8f));
        paint.setStrokeWidth(lineWidth);
        rect.set(lineWidth, lineWidth, width - lineWidth * 2, height - lineWidth * 2);
        path.addRoundRect(rect, centerY, centerY, Path.Direction.CCW);
        canvas.drawPath(path, paint);


        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAlpha((int) (255 * 0.3f));
        path.reset();
        path.addRoundRect(rect, rect.height() / 2, rect.height() / 2, Path.Direction.CCW);
        canvas.drawPath(path, paint);


        // Draw the "GO" button
        float radius = height * 0.3f * scale;
        float coX = (width - height * 0.3f * 2.0f);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(coX, height / 2, radius, paint);

        paint.setColor(Color.BLACK);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16 * scale, getResources().getDisplayMetrics()));
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        float goTextX = coX - paint.measureText("GO") / 1.5f;
        float goTextY = height / 2 + (paint.getFontMetrics().bottom - paint.getFontMetrics().top) / 2 - paint.getFontMetrics().bottom;
        canvas.drawText("GO", goTextX, goTextY, paint);

        // Draw the arrow (triangle) next to the "GO" text
        float arrowSize = paint.measureText("GO") * 0.4f;
        Path arrowPath = new Path();
        arrowPath.moveTo(goTextX + paint.measureText("GO") + arrowSize / 4.f, height / 2 - arrowSize / 2);
        arrowPath.lineTo(goTextX + paint.measureText("GO") + arrowSize, height / 2);
        arrowPath.lineTo(goTextX + paint.measureText("GO") + arrowSize / 4.f, height / 2 + arrowSize / 2);
        arrowPath.close();
        canvas.drawPath(arrowPath, paint);

        // Draw the title and description text
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        float titleX = 0;
        if (!TextUtils.isEmpty(title)) {

            textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 20, getResources().getDisplayMetrics())); // Adjust text size as needed
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);
            String tempTitle = title;

            titleX = (textPaint.getFontMetrics().bottom - textPaint.getFontMetrics().top) * 1.5f;
            for (int i = 5; i < title.length(); i++) {
                float v = titleX + textPaint.measureText(title, 0, i);
                if (v > rect.right - rect.left - titleX - radius) {
                    tempTitle = title.substring(0, i - 2) + "...";
                    break;
                }
            }
            float titleY = height / 2 - (textPaint.getFontMetrics().bottom - textPaint.getFontMetrics().top) - textPaint.getFontMetrics().top;
            canvas.drawText(tempTitle, titleX, titleY, textPaint);

        }


        if (!TextUtils.isEmpty(desc)) {
            textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16, getResources().getDisplayMetrics())); // Adjust text size as needed
            textPaint.setTypeface(Typeface.DEFAULT);
            float descX = titleX > 0 ? titleX : (textPaint.getFontMetrics().bottom - textPaint.getFontMetrics().top) * 1.5f;

            String tempDesc = desc;

            for (int i = 5; i < desc.length(); i++) {
                float v = titleX + textPaint.measureText(desc, 0, i);
                if (v > rect.right - rect.left - titleX - radius) {
                    tempDesc = desc.substring(0, i - 2) + "...";
                    break;
                }
            }
            float descY = height / 2 + (textPaint.getFontMetrics().bottom - textPaint.getFontMetrics().top);
            canvas.drawText(tempDesc, descX, descY, textPaint);
        }

        canvas.clipPath(path);
        canvas.drawPath(gradientPath, gradientPaint);

    }


    private void initGradient() {

        gradientPath = new Path();
        gradientPaint = new Paint();

        h = height;
        w = Dips.dipsToIntPixels(60, getContext());
        LinearGradient gradient = new LinearGradient(0, h, w, 0, new int[]{colorWithAlpha(Color.WHITE, 0.2f), colorWithAlpha(Color.WHITE, 0.5f), colorWithAlpha(Color.WHITE, 0.2f)}, new float[]{0.2f, 0.8f, 1f}, Shader.TileMode.MIRROR);
        gradientPaint.setShader(gradient);

    }

    private void updateGradient() {

        gradientPath.reset();
        gradientPath.moveTo(w / 2f + offset, 0);
        gradientPath.lineTo(w + offset, 0);
        gradientPath.lineTo(w / 2f + offset, h);
        gradientPath.lineTo(0 + offset, h);
        gradientPath.close();
    }

    private int colorWithAlpha(int color, float alpha) {
        return Color.argb((int) (Color.alpha(color) * alpha), Color.red(color), Color.green(color), Color.blue(color));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = (int) (h * 0.8f);
        centerX = w / 2;
        centerY = h / 2;
        initGradient();
        startAnimator();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (animator != null) {
            animator.cancel();
        }
        if (scaleAnimator != null) {
            scaleAnimator.cancel();
        }
    }
}
