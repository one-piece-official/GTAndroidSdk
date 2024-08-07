package com.gt.sdk.base.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;

import com.czhj.sdk.common.utils.Dips;

public class ArcProgressBar extends View {

    private Paint outPaint, leftPaint, rightPaint, arrowPaint;
    private Path leftPath, rightPath, leftArrowPath, rightArrowPath;
    private RectF arcRect;
    private float lineWidth, dltAngle, arrowSize;
    private PointF arcCenter;

    private int designHeight;
    private PointF leftArcPoint;
    private PointF rightArcPoint;
    private PathMeasure leftPathMeasure;
    private PathMeasure rightPathMeasure;
    private float _process = 0.2f;
    private Path progressPath;

    public ArcProgressBar(Context context) {
        super(context);

        outPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outPaint.setColor(Color.LTGRAY);
        outPaint.setStyle(Paint.Style.STROKE);

        leftPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        leftPaint.setColor(Color.WHITE);
        leftPaint.setStyle(Paint.Style.STROKE);

        rightPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rightPaint.setColor(Color.WHITE);
        rightPaint.setStyle(Paint.Style.STROKE);

        arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrowPaint.setColor(Color.LTGRAY);
        arrowPaint.setStyle(Paint.Style.FILL);
        progressPath = new Path(); // 创建一个新的路径 leftProgressPath，用于保存进度弧形部分


        leftPath = new Path();
        rightPath = new Path();
        leftArrowPath = new Path();
        rightArrowPath = new Path();
        leftPathMeasure = new PathMeasure();
        rightPathMeasure = new PathMeasure();

        obseverLayout();
    }

    public ArcProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        obseverLayout();

    }

    public ArcProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        obseverLayout();

    }

    private void obseverLayout() {
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                ArcProgressBar.this.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                init();
            }
        });
    }

    private void init() {


        int h = getHeight();
        int w = getWidth();
        if (h == 0 || w == 0) return;
        int px = Dips.dipsToIntPixels(1, getContext());
        lineWidth = Math.max((int) (h / (100f * px) * 3 * px), 2 * px); // Set your line width
        designHeight = (int) (h * (h / (100f * px) * (0.4f / (h / (100f * px)))));
        dltAngle = (float) (Math.PI / 4); // Set your angle
        arrowSize = lineWidth * 3; // Set your arrow size

        leftPaint.setStrokeWidth(lineWidth);
        outPaint.setStrokeWidth(lineWidth);
        rightPaint.setStrokeWidth(lineWidth);

        float radius = h / 2f - arrowSize * h / designHeight;
        arcCenter = new PointF(w / 2f, h / 2f);
        arcRect = new RectF(arcCenter.x - radius, arcCenter.y - radius, arcCenter.x + radius, arcCenter.y + radius);
        leftArcPoint = new PointF(arcCenter.x - (float) (Math.cos(dltAngle) * (arcRect.width() / 2)), arcCenter.y - (float) (Math.sin(dltAngle) * (arcRect.height() / 2)));
        rightArcPoint = new PointF(arcCenter.x + (float) (Math.cos(dltAngle) * (arcRect.width() / 2)), arcCenter.y - (float) (Math.sin(dltAngle) * (arcRect.height() / 2)));

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        obseverLayout();

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (arcRect == null) return;
        double v = Math.toDegrees(dltAngle);
        float v1 = (float) (180 - 2 * v);
        // Draw outer arc
        canvas.drawArc(arcRect, (float) (180 + v), v1, false, outPaint);

        // Draw left arc
        leftPath.reset();
        leftPath.lineTo(0, 0);
        progressPath.reset();
        progressPath.lineTo(0, 0);
        leftPath.arcTo(arcRect, -90, -v1 / 2, true);
        leftPathMeasure.setPath(leftPath, false);
        float length = leftPathMeasure.getLength();
        leftPathMeasure.getSegment(0, length * _process, progressPath, true);
        canvas.drawPath(progressPath, leftPaint);

        // Draw right arc
        rightPath.reset();
        rightPath.lineTo(0, 0);
        progressPath.reset();
        progressPath.lineTo(0, 0);

        rightPath.arcTo(arcRect, -90, v1 / 2, true);
        rightPathMeasure.setPath(rightPath, false);
        length = rightPathMeasure.getLength();
        rightPathMeasure.getSegment(0, length * _process, progressPath, true);
        canvas.drawPath(progressPath, rightPaint);

        if (_process == 1) {
            arrowPaint.setColor(Color.WHITE);
        } else {
            arrowPaint.setColor(Color.LTGRAY);
        }
        // Draw left arrow
        leftArrowPath.reset();
        leftArrowPath.lineTo(0, 0);

        drawLeftArrow(leftArrowPath, arcCenter, leftArcPoint, arrowSize);
        canvas.drawPath(leftArrowPath, arrowPaint);

        // Draw right arrow
        rightArrowPath.reset();
        rightArrowPath.lineTo(0, 0);
        drawRightArrow(rightArrowPath, arcCenter, rightArcPoint, arrowSize);
        canvas.drawPath(rightArrowPath, arrowPaint);
    }

    private void drawLeftArrow(Path path, PointF center, PointF arcPoint, float arrowSize) {
        float k = (arcPoint.y - center.y) / (arcPoint.x - center.x);

        PointF p1 = new PointF(arcPoint.x + 0.5f * arrowSize * (float) Math.sin(Math.PI / 2 - Math.atan(k)), arcPoint.y + 0.5f * arrowSize * (float) Math.cos(Math.PI / 2 - Math.atan(k)));
        PointF p2 = new PointF(arcPoint.x - 0.5f * arrowSize * (float) Math.sin(Math.PI / 2 - Math.atan(k)), arcPoint.y - 0.5f * arrowSize * (float) Math.cos(Math.PI / 2 - Math.atan(k)));
        PointF p3 = new PointF(p2.x - arrowSize * (float) Math.sin(Math.PI / 3 - Math.atan(1 / k)), p2.y + arrowSize * (float) Math.cos(Math.PI / 3 - Math.atan(1 / k)));

        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.close();
    }

    public void updateProcess(float process) {
        _process = process;
        invalidate();
    }

    private void drawRightArrow(Path path, PointF center, PointF arcPoint, float arrowSize) {
        float k = (arcPoint.y - center.y) / (arcPoint.x - center.x);

        PointF p1 = new PointF(arcPoint.x + 0.5f * arrowSize * (float) Math.sin(Math.PI / 2 - Math.atan(k)), arcPoint.y + 0.5f * arrowSize * (float) Math.cos(Math.PI / 2 - Math.atan(k)));
        PointF p2 = new PointF(arcPoint.x - 0.5f * arrowSize * (float) Math.sin(Math.PI / 2 - Math.atan(k)), arcPoint.y - 0.5f * arrowSize * (float) Math.cos(Math.PI / 2 - Math.atan(k)));
        PointF p3 = new PointF(p2.x + arrowSize * (float) Math.sin(Math.PI * 2 / 3 + Math.atan(1 / k)), p2.y + arrowSize * (float) Math.cos(Math.PI * 2 / 3 + Math.atan(1 / k)));

        path.moveTo(p1.x, p1.y);
        path.lineTo(p2.x, p2.y);
        path.lineTo(p3.x, p3.y);
        path.close();
    }
}
