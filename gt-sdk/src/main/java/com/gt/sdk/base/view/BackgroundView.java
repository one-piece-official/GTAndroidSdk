package com.gt.sdk.base.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.czhj.sdk.common.utils.Dips;

public class BackgroundView extends View {

    private Path path1, path2;
    private Paint paint;

    public BackgroundView(Context context) {
        super(context);
        path1 = new Path();
        path2 = new Path();
        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        paint.setAlpha((int) (255 * 0.6f));
        obseverLayout();
    }

    public BackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        obseverLayout();
    }

    public BackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obseverLayout();
    }

    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        paint.setColor(Color.BLACK);
        paint.setAlpha((int) (255 * 0.4f));
        paint.setStyle(Paint.Style.FILL);
        canvas.drawPath(path1, paint);

        paint.setColor(Color.WHITE);
        paint.setAlpha((int) (255 * 0.3f));
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(path1, paint);
        paint.setAlpha((int) (255 * 0.5f));

        canvas.drawPath(path2, paint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
    }

    private void obseverLayout() {
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                BackgroundView.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                init();
            }
        });
    }

    private void init() {
        int width = getWidth();
        int height = getWidth();

        if (width <= 0 || height <= 0) {
            return;
        }
        int size = Math.min(width, height);
        paint.reset();
        path1.reset();
        path2.reset();
        paint.setStrokeWidth(Dips.dipsToIntPixels(1, getContext()));
        path1.addCircle(width / 2.f, height / 2.f, size / 2 - Dips.dipsToIntPixels(1, getContext()), Path.Direction.CCW);
        path2.addCircle(width / 2.f, height / 2.f, size / 2 - Dips.dipsToIntPixels(10, getContext()), Path.Direction.CCW);

    }

}
