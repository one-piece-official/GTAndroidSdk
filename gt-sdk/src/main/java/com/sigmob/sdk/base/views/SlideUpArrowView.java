package com.sigmob.sdk.base.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;


import com.czhj.sdk.common.utils.Dips;

public class SlideUpArrowView extends View {
    private int lineWidth;
    private int arrowHeight;
    private Paint paint;
    private Path path;

    public SlideUpArrowView(Context context) {
        super(context);
        init();
    }

    public SlideUpArrowView(Context context,  AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public SlideUpArrowView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {


//        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
//        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                SlideUpArrowView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                initLayout();
//
//            }
//        });

    }

    private void initLayout() {

        int width = getWidth();
        int height = getHeight();


        if (width == 0 || height == 0)
            return;

        lineWidth = Dips.dipsToIntPixels(5, getContext());

        arrowHeight = height / 2;
        path = new Path();


        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(lineWidth);
        paint.setColor(Color.WHITE);
        PointF point = new PointF(width / 2, arrowHeight);

        createArrowPath(path, point);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(path, paint);

    }

    private void createArrowPath(Path path, PointF point) {
        double angle = Math.PI / 3 * 2;
        float leftX = (float) (point.x - arrowHeight * Math.tan(angle));
        float leftY = point.y + arrowHeight;
        float rightX = (float) (point.x + arrowHeight * Math.tan(angle));
        float rightY = point.y + arrowHeight;

        path.moveTo(leftX, leftY);
        path.lineTo(point.x, point.y);
        path.lineTo(rightX, rightY);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        initLayout();
    }
}
