package com.sigmob.sdk.base.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import com.czhj.sdk.common.utils.Dips;


public class SkipButtonView extends View {


    private Paint paint;
    private RectF rect;
    private Paint textPaint;
    private int offset;
    private int timer = 15;
    private int strokeWidth;

    public SkipButtonView(Context context) {
        super(context);
    }

    public SkipButtonView(Context context,  AttributeSet attrs) {
        super(context, attrs);
    }

    public SkipButtonView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        strokeWidth = Dips.asIntPixels(1, getContext());
        paint.setStrokeWidth(strokeWidth);
        paint.setAlpha((int) (255 * 0.5f));

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                12, getResources().getDisplayMetrics()));
        offset = Dips.asIntPixels(10, getContext());

        rect = new RectF(strokeWidth, strokeWidth, getWidth() - strokeWidth, getHeight() - strokeWidth);

    }

    public void updateTimer(int timer) {
        this.timer = timer;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);


        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setAlpha((int) (255 * 0.5f));
        canvas.drawRoundRect(rect, rect.right / 2, rect.right / 2, paint);


        canvas.drawText("跳过", offset, rect.bottom / 2 + (textPaint.getFontMetrics().bottom - textPaint.getFontMetrics().top) / 2 - textPaint.getFontMetrics().bottom, textPaint);

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        strokeWidth = Dips.asIntPixels(1, getContext());
        paint.setStrokeWidth(strokeWidth);
        paint.setAlpha((int) (255 * 0.5f));
        canvas.drawRoundRect(rect, rect.right / 2, rect.right / 2, paint);

        float startx = offset + textPaint.measureText("跳过") + offset / 2;
        canvas.drawLine(startx + strokeWidth * 2, strokeWidth * 2, startx + strokeWidth * 2, rect.bottom - strokeWidth * 2, paint);

        if (timer > 9) {
            canvas.drawText(String.valueOf(timer), startx + offset / 2, rect.bottom / 2 + (textPaint.getFontMetrics().bottom - textPaint.getFontMetrics().top) / 2 - textPaint.getFontMetrics().bottom, textPaint);
        } else {
            canvas.drawText(String.valueOf(timer), startx + offset, rect.bottom / 2 + (textPaint.getFontMetrics().bottom - textPaint.getFontMetrics().top) / 2 - textPaint.getFontMetrics().bottom, textPaint);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }
}
