package com.sigmob.sdk.base.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.czhj.sdk.common.utils.Dips;
import com.sigmob.sdk.base.utils.ViewUtil;

public class SlopeViewV2 extends View {
    // 定义画笔和绘制属性
    private Paint mBorderPaint;
    private Paint mScreenPaint;
    private RectF mBorderRect;
    private RectF mScreenRect;

    // 设置Z轴最大绝对值
    private float currentAbsoluteZ = 0f;

    private Float preX;

    private float strokeWidth = 3f;
    private Paint mHomePaint;
    private float arrowHeight;
    private PointF point;
    private Path path;
    private Paint paint;
    private int height;
    private int width;
    private float phoneHeight;
    private float offset;
    private PointF point2;
    private float heightRatio;
    private float widthratio;

    public SlopeViewV2(Context context) {
        super(context);
        init(context);
    }

    public SlopeViewV2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SlopeViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ViewUtil.observerLayout(this,new Runnable(){
            @Override
            public void run() {
                init();
            }
        });
    }

    private void init() {
        if (mBorderPaint == null){
            mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mBorderPaint.setColor(Color.WHITE);
            mBorderPaint.setStyle(Paint.Style.STROKE);
            strokeWidth = Dips.dipsToIntPixels(strokeWidth, getContext());
            mBorderPaint.setStrokeWidth(strokeWidth);

            mScreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mScreenPaint.setColor(Color.LTGRAY);
            mScreenPaint.setStyle(Paint.Style.FILL);

            mHomePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mHomePaint.setColor(Color.WHITE);
            mHomePaint.setStyle(Paint.Style.FILL);

            int desginHeight = Dips.dipsToIntPixels(92, getContext());
            heightRatio = (height * 1.f / desginHeight);
            arrowHeight = heightRatio * Dips.dipsToIntPixels(11, getContext());
            mBorderRect = new RectF();
            mScreenRect = new RectF();
            point = new PointF(getWidth() / 2, 0);
            point2 = new PointF(getWidth() / 2, arrowHeight);

            path = new Path();
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);


            phoneHeight = height - arrowHeight * 3;
            offset = strokeWidth / 2;
        }

    }

    private void startAnimator() {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1.f);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                updateScreen((Float) valueAnimator.getAnimatedValue());
            }
        });
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setDuration(2000);
        animator.start();
    }

    private void createArrowPath(Path path, PointF point) {
        double angle = Math.PI / 4;
        float leftX = (float) (point.x - arrowHeight * Math.tan(angle));
        float leftY = point.y + arrowHeight;
        float rightX = (float) (point.x + arrowHeight * Math.tan(angle));
        float rightY = point.y + arrowHeight;

        path.moveTo(leftX, leftY);
        path.lineTo(point.x, point.y);
        path.lineTo(rightX, rightY);
        path.lineTo(point.x, point.y + arrowHeight / 2);
        path.close();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (paint == null){
            return;
        }
        path.reset();
        createArrowPath(path, point);
        canvas.drawPath(path, paint);
        path.reset();
        createArrowPath(path, point2);
        canvas.drawPath(path, paint);
        //board

        float startx = strokeWidth;
        float starty = height - phoneHeight - offset;
        mBorderRect.set(startx, starty, width - strokeWidth, starty + phoneHeight - offset * 2);
        canvas.drawRoundRect(mBorderRect, offset, offset, mBorderPaint);

        //screen
        float screenTop = currentAbsoluteZ > 0.98f ? strokeWidth - offset : (1 - currentAbsoluteZ) * phoneHeight + strokeWidth;
        Log.d("", "onDraw: " + screenTop);
        float top = starty + screenTop;
        float bottom = starty + phoneHeight + strokeWidth;

        if (currentAbsoluteZ > 0.2f) {
            bottom = starty + phoneHeight - strokeWidth;
        }
        mScreenRect.set(startx + offset, top, width - strokeWidth, bottom);
        canvas.drawRect(mScreenRect, mScreenPaint);

        //home
        float lineHeight = Math.min(heightRatio * Dips.dipsToIntPixels(3.f, getContext()), strokeWidth);
        float lineWidth = width > 0 ? width / 5 : strokeWidth;
        float lineX = (width - lineWidth) / 2;
        float lineY = starty + phoneHeight - lineHeight - strokeWidth;
        canvas.drawRect(lineX, lineY, lineX + lineWidth, lineY + lineHeight, mHomePaint);

    }

    public void updateScreen(float absoluteZ) {
        if (paint == null) {
            return;
        }
        currentAbsoluteZ = Math.max(0.2f, absoluteZ);
        currentAbsoluteZ = Math.min(1.0f, currentAbsoluteZ);

        ObjectAnimator animation = ObjectAnimator.ofFloat(this, "rotationX", currentAbsoluteZ * 40, absoluteZ * 40);
        animation.setDuration(50);
        animation.start();
        paint.setAlpha((int) (255 * currentAbsoluteZ));
        invalidate(); // 重绘视图
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        height = h;
        width = w;
        init();
    }
}
