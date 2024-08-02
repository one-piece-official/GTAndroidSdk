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
import com.sigmob.sdk.base.models.BaseAdUnit;
import com.sigmob.sdk.base.utils.ViewUtil;

import java.lang.ref.WeakReference;

public class OvalButton extends View {
    private Paint paint;
    private RectF rect;
    private Paint textPaint;
    private int offset;
    private int strokeWidth;


    private WeakReference<BaseAdUnit> unitWeakReference;
    private String text = "";

    public OvalButton(Context context) {
        super(context);
        init(context);
    }

    public OvalButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public OvalButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public void setText(String text) {
        this.text = text;
        invalidate();
    }

    private void init(Context context){
        ViewUtil.observerLayout(this, new Runnable() {
            @Override
            public void run() {
                init();
            }
        });
    }

    private void init() {
        if (paint == null){
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

        }
        rect = new RectF(strokeWidth, strokeWidth, getWidth() - strokeWidth, getHeight() - strokeWidth);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (paint != null) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.BLACK);
            paint.setAlpha((int) (255 * 0.4f));
            canvas.drawRoundRect(rect, rect.right / 2, rect.right / 2, paint);

            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.WHITE);
            paint.setAlpha((int) (255 * 0.3f));
            canvas.drawRoundRect(rect, rect.right / 2, rect.right / 2, paint);

            float startx = (getWidth() - textPaint.measureText(text)) / 2;

            canvas.drawText(text, startx, rect.bottom / 2 + (textPaint.getFontMetrics().bottom - textPaint.getFontMetrics().top) / 2 - textPaint.getFontMetrics().bottom, textPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }
}
