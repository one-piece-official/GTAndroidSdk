package com.sigmob.sdk.base.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;


public class DashLineView extends View {

    private Paint mPaint;
    private Path mPath;

    public DashLineView(Context context) {
        super(context);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.DKGRAY);
        // 需要加上这句，否则画不出东西
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setPathEffect(new DashPathEffect(new float[] {10, 5}, 0));

        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        mPath.reset();
        int height =  getHeight();
        int width = getWidth();
        mPath.moveTo(0, 0);
        mPath.lineTo(0, getHeight());
        canvas.drawPath(mPath, mPaint);
    }
}

