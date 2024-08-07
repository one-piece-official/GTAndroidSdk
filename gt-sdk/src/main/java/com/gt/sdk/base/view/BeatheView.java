package com.gt.sdk.base.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.czhj.sdk.common.utils.Dips;

public class BeatheView extends View {

    private static float line_width;
    private Paint paint;
    private Path path;
    private PointF center;
    private float radius;
    private float sizeWidth;
    private float sizeHeight;
    private boolean isShowBackgroundColor;
    private Paint backgroundPaint;

    public BeatheView(Context context) {
        super(context);
        init();
    }

    public BeatheView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public BeatheView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        backgroundPaint = new Paint();
        line_width = Dips.dipsToIntPixels(6, getContext());
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(line_width);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setColor(Color.BLACK);
        backgroundPaint.setStyle(Paint.Style.FILL);
        backgroundPaint.setAlpha((int) (255 * 0.2f));

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        sizeWidth = w;
        sizeHeight = h;
        calculatePath();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isShowBackgroundColor) {
            canvas.drawPath(path, backgroundPaint);
        }
        canvas.drawPath(path, paint);


    }

    private void calculatePath() {
        float angle = (float) Math.PI / 5;
        float bottom = sizeWidth / 2 * (float) Math.tan(angle);
        radius = bottom / (float) Math.sin(angle);
        center = new PointF(sizeWidth / 2, radius + Dips.dipsToIntPixels(30, getContext()));
        path = new Path();
        RectF rect = new RectF(center.x - radius, center.y - radius, center.x + radius, center.y + radius);
        path.addArc(rect, 0, -180);

    }

    public void showBackground(boolean isShow) {

        isShowBackgroundColor = isShow;
    }

    public float getLineWidth() {
        return paint.getStrokeWidth();
    }

    public void setLineWidth(float lineWidth) {
        paint.setStrokeWidth(lineWidth);
        invalidate();
    }

}
