package com.gt.sdk.base.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.czhj.sdk.common.utils.Dips;

public class WringArcView extends View {
    private Paint paint;
    private Path path;
    private int offset;
    private RectF rectF;
    private RectF leftRect;
    private RectF rightRect;

    public WringArcView(Context context) {
        super(context);
    }

    public WringArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WringArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {
        paint = new Paint();
        path = new Path();

        int strokeWidth = Dips.dipsToIntPixels(1, getContext());
        paint.setStrokeWidth(strokeWidth);
        offset = (int) ((getWidth() / (100f * strokeWidth)) * strokeWidth * 20);


        int width = getWidth() - offset;
        int height = getHeight();

        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);

        rectF = new RectF(offset, height / 2.2f, width / 2, height - height / 2.2f);
        leftRect = new RectF(offset, rectF.top, rectF.right, rectF.bottom);
        rightRect = new RectF(rectF.right + leftRect.width(), rectF.top, rectF.right + rectF.width() + leftRect.width(), rectF.bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawOvalInRectLeft(canvas, rectF);
        drawOvalInRectRight(canvas, rectF);
    }

    private void drawOvalInRectLeft(Canvas canvas, RectF rectf) {
        path.reset();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        path.addArc(leftRect, 90, 180);
        canvas.drawPath(path, paint);
    }

    private void drawOvalInRectRight(Canvas canvas, RectF rectf) {
        path.reset();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        path.addArc(rightRect, 90, -180);
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }
}
