package com.sigmob.sdk.base.views;


import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;



import com.czhj.sdk.common.utils.Dips;

public class SlideUpView extends View {
    private float lineWidth;
    private Paint paint1, paint2, paint3;
    private Path path1, path2, path3;
    private PointF point1, point2, point3;
    private float arrowHeight;

    public SlideUpView(Context context) {
        super(context);
        init();
    }

    public SlideUpView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public SlideUpView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {

        lineWidth = Dips.dipsToIntPixels(5, getContext());
        arrowHeight = lineWidth * 2;
        paint1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint1.setStyle(Paint.Style.STROKE);
        paint1.setStrokeWidth(lineWidth);
        paint1.setColor(Color.WHITE);
        paint1.setAlpha((int) (255 * 1.0f));

        paint2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint2.setStyle(Paint.Style.STROKE);
        paint2.setStrokeWidth(lineWidth);
        paint2.setColor(Color.WHITE);
        paint2.setAlpha((int) (255 * 0.5f));

        paint3 = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint3.setStyle(Paint.Style.STROKE);
        paint3.setStrokeWidth(lineWidth);
        paint3.setColor(Color.WHITE);
        paint3.setAlpha((int) (255 * 0.3f));

        path1 = new Path();
        path2 = new Path();
        path3 = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float centerX = w / 2f;
        float centerY = h;
        float offset = Dips.dipsToIntPixels(13, getContext());

        point1 = new PointF(centerX, centerY + lineWidth);
        point2 = new PointF(centerX, centerY + lineWidth + offset);
        point3 = new PointF(centerX, centerY + lineWidth + offset * 2);

        updatePaths();
        startAnimation();
    }

    private void updatePaths() {
        path1.reset();
        path2.reset();
        path3.reset();

        createArrowPath(path1, point1);
        createArrowPath(path2, point2);
        createArrowPath(path3, point3);
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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        canvas.drawPath(path1, paint1);
        canvas.drawPath(path2, paint2);
        canvas.drawPath(path3, paint3);
    }

    public void startAnimation() {
        AnimatorSet animatorSet1 = createAnimatorSet(point1, paint1, 2);
        AnimatorSet animatorSet2 = createAnimatorSet(point2, paint2, 2);
        AnimatorSet animatorSet3 = createAnimatorSet(point3, paint3, 2);
        AnimatorSet animatorSet = new AnimatorSet();

        animatorSet.playTogether(animatorSet1, animatorSet2, animatorSet3);
        animatorSet.start();
//        animatorSet1.start();
//        animatorSet2.start();
//        animatorSet3.start();
    }

    private AnimatorSet createAnimatorSet(PointF point, Paint paint, int duration) {


        int alphaValue = paint.getAlpha();

        ValueAnimator transitionY = ValueAnimator.ofFloat(point.y, point.y - getHeight() - lineWidth);
        transitionY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate( ValueAnimator valueAnimator) {
                point.y = (float) valueAnimator.getAnimatedValue();
                updatePaths();
                invalidate();
            }
        });
        transitionY.setRepeatCount(ValueAnimator.INFINITE);

        ValueAnimator alpha = ValueAnimator.ofInt(alphaValue, 0);
        alpha.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate( ValueAnimator valueAnimator) {
                paint.setAlpha((int) valueAnimator.getAnimatedValue());
                invalidate();
            }
        });
        alpha.setRepeatCount(ValueAnimator.INFINITE);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(duration * 1000);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.playTogether(transitionY, alpha);

        return animatorSet;
    }

}
