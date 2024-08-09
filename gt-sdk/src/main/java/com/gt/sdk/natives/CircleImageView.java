package com.gt.sdk.natives;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

public class CircleImageView extends ImageView {
    private BitmapShader bitmapShader;
    private float width;
    private float height;
    protected float radius;
    protected boolean isCircle = false;
    private Paint paint;
    private Matrix matrix;
    private RectF rectF;

    public CircleImageView(Context context) {
        this(context, null);

    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        paint = new Paint();
        paint.setAntiAlias(true);   //设置抗锯齿
        matrix = new Matrix();      //初始化缩放矩阵
        radius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, context.getResources().getDisplayMetrics());
    }

    public void setCircle(boolean circle) {
        isCircle = circle;
    }

    /**
     * 测量控件的宽高，并获取其内切圆的半径
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        width = Math.min(width, height);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        bitmapShader = initBitmapShader();

        if (bitmapShader == null) {
            super.onDraw(canvas);
        } else {
            paint.setShader(bitmapShader);//将着色器设置给画笔
            if (isCircle) {
                canvas.drawCircle(width / 2, width / 2, width / 2, paint);//使用画笔在画布上画圆
            } else {
                canvas.drawRoundRect(rectF, radius, radius, paint);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        rectF = new RectF(0, 0, getHeight(), getHeight());
    }

    /**
     * 获取ImageView中资源图片的Bitmap，利用Bitmap初始化图片着色器,通过缩放矩阵将原资源图片缩放到铺满整个绘制区域，避免边界填充
     */
    private BitmapShader initBitmapShader() {
        BitmapDrawable drawable = (BitmapDrawable) getDrawable();
        if (drawable != null) {
            Bitmap bitmap = drawable.getBitmap();
            if (bitmap != null) {
                BitmapShader bitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                float scale = Math.max(width / bitmap.getWidth(), width / bitmap.getHeight());
                matrix.setScale(scale, scale);//将图片宽高等比例缩放，避免拉伸
                bitmapShader.setLocalMatrix(matrix);
                return bitmapShader;
            }
        }
        return null;
    }
}
