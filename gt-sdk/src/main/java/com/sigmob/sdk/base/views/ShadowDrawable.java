package com.sigmob.sdk.base.views;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

public class ShadowDrawable extends Drawable {
    private final Paint mPaint;
    private final int mShadowRadius;
    private final int mShape;
    private final int mShapeRadius;
    private final int mOffsetX;
    private final int mOffsetY;
    private final int[] mBgColor;
    private RectF mRect;

    private final static int SHAPE_ROUND = 1;

    private ShadowDrawable(int shape, int[] bgColor, int shapeRadius, int shadowColor, int shadowRadius, int offsetX, int offsetY) {
        this.mShape = shape;
        this.mBgColor = bgColor;
        this.mShapeRadius = shapeRadius;
        this.mShadowRadius = shadowRadius;
        this.mOffsetX = offsetX;
        this.mOffsetY = offsetY;
        mPaint = new Paint();
        mPaint.setColor(Color.TRANSPARENT);
        mPaint.setAntiAlias(true);
        mPaint.setShadowLayer(shadowRadius, offsetX, offsetY, shadowColor);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
    }

    @Override
    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        mRect = new RectF(left + mShadowRadius - mOffsetX, top + mShadowRadius - mOffsetY, right - mShadowRadius - mOffsetX,
                bottom - mShadowRadius - mOffsetY);
    }

    @Override
    public void draw( Canvas canvas) {
        Paint newPaint = new Paint();
        if (mBgColor != null) {
            if (mBgColor.length == 1) {
                newPaint.setColor(mBgColor[0]);
            } else {
                newPaint.setShader(new LinearGradient(mRect.left, mRect.height() / 2, mRect.right, mRect.height() / 2, mBgColor,
                        null, Shader.TileMode.CLAMP));
            }
        }
        newPaint.setAntiAlias(true);
        if (mShape == SHAPE_ROUND) {
            canvas.drawRoundRect(mRect, mShapeRadius, mShapeRadius, mPaint);
            canvas.drawRoundRect(mRect, mShapeRadius, mShapeRadius, newPaint);
        } else {
            canvas.drawCircle(mRect.centerX(), mRect.centerY(), Math.min(mRect.width(), mRect.height())/ 2, mPaint);
            canvas.drawCircle(mRect.centerX(), mRect.centerY(), Math.min(mRect.width(), mRect.height())/ 2, newPaint);

        }
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter( ColorFilter colorFilter) {
        mPaint.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public static void setShadowDrawable(View view, int bgColor, int shapeRadius, int shadowColor, int shadowRadius, int offsetX, int offsetY) {
        ShadowDrawable drawable = new Builder()
                .setBgColor(bgColor)
                .setShapeRadius(shapeRadius)
                .setShadowColor(shadowColor)
                .setShadowRadius(shadowRadius)
                .setOffsetX(offsetX)
                .setOffsetY(offsetY)
                .builder();
        view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(drawable);
        }else {
            view.setBackgroundDrawable(drawable);
        }
    }

    public static class Builder {
        private int mShape;
        private int mShapeRadius;
        private int mShadowColor;
        private int mShadowRadius;
        private int mOffsetX = 0;
        private int mOffsetY = 0;
        private int[] mBgColor;

        Builder() {
            mShape = ShadowDrawable.SHAPE_ROUND;
            mShapeRadius = 12;
            mShadowColor = Color.parseColor("#4d000000");
            mShadowRadius = 18;
            mOffsetX = 0;
            mOffsetY = 0;
            mBgColor = new int[1];
            mBgColor[0] = Color.TRANSPARENT;
        }

        Builder setShape(int mShape) {
            this.mShape = mShape;
            return this;
        }

        Builder setShapeRadius(int ShapeRadius) {
            this.mShapeRadius = ShapeRadius;
            return this;
        }

        Builder setShadowColor(int shadowColor) {
            this.mShadowColor = shadowColor;
            return this;
        }

        Builder setShadowRadius(int shadowRadius) {
            this.mShadowRadius = shadowRadius;
            return this;
        }

        Builder setOffsetX(int OffsetX) {
            this.mOffsetX = OffsetX;
            return this;
        }

        Builder setOffsetY(int OffsetY) {
            this.mOffsetY = OffsetY;
            return this;
        }

        Builder setBgColor(int BgColor) {
            this.mBgColor[0] = BgColor;
            return this;
        }

        Builder setBgColor(int[] BgColor) {
            this.mBgColor = BgColor;
            return this;
        }

        ShadowDrawable builder() {
            return new ShadowDrawable(mShape, mBgColor, mShapeRadius, mShadowColor, mShadowRadius, mOffsetX, mOffsetY);
        }
    }
}
