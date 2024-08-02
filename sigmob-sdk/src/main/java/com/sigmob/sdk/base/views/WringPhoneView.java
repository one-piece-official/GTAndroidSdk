package com.sigmob.sdk.base.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


import com.czhj.sdk.common.utils.Dips;

public class WringPhoneView extends View {
    private Paint paint;
    private Path path;
    private int offset;
    private RectF holeRect2;
    private RectF holeRect1;
    private RectF mainRect;
    private int rx1;
    private int rx2;

    public WringPhoneView(Context context) {
        super(context);

    }

    public WringPhoneView(Context context,  AttributeSet attrs) {
        super(context, attrs);

    }

    public WringPhoneView(Context context,  AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void init() {

        paint = new Paint();
        path = new Path();
        int width = getWidth();
        int height = getHeight();
        int strokeWidth = Dips.dipsToIntPixels(1, getContext());
        paint.setStrokeWidth(strokeWidth);
        rx1 = (int) ((width / (100f * strokeWidth)) * 15);
        rx2 = (int) ((width / (100f * strokeWidth)) * 6);

        offset = Math.max((int) ((width / (100f * strokeWidth)) * strokeWidth * 3), strokeWidth * 2);


        float h = height * 0.4f;
        float w = h * 10 / 16;
        float x = (width - w) / 2;
        float y = (height - h) / 2;
        mainRect = new RectF(x, y, x + w, y + h);
        holeRect1 = new RectF(mainRect.left + offset, mainRect.top + offset, mainRect.right - offset, mainRect.bottom - offset * 3);
        holeRect2 = new RectF(mainRect.left + (mainRect.width() - offset * 2) / 2.2f, mainRect.bottom - offset * 2, mainRect.right - (mainRect.width() - offset * 2) / 2.2f, mainRect.bottom - offset);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawPhone(canvas);

    }

    private void drawPhone(Canvas canvas) {

        path.reset();
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        path.addRoundRect(mainRect, rx1, rx1, Path.Direction.CW);
        path.addRoundRect(holeRect1, rx2, rx2, Path.Direction.CCW);
        path.addRoundRect(holeRect2, rx2, rx2, Path.Direction.CCW);
        path.close();
        canvas.drawPath(path, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }
}
