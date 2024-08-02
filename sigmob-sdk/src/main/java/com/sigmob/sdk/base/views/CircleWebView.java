package com.sigmob.sdk.base.views;

import static com.sigmob.sdk.base.WindConstants.ENABLEFILE;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Base64;
import android.webkit.WebSettings;

import com.czhj.sdk.common.utils.ReflectionUtil;
import com.sigmob.sdk.base.WindConstants;

public class CircleWebView extends BaseWebView {
    public CircleWebView(Context context) {
        this(context, null);
    }

    public CircleWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context);
        webSetting(getSettings());
        init(context);
    }


    private void webSetting(WebSettings webSettings) {

        try {
            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(webSettings, new String(Base64.decode(WindConstants.ENABLEJS, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class, true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {


            ReflectionUtil.MethodBuilder methodBuilder = new ReflectionUtil.MethodBuilder(webSettings, new String(Base64.decode(ENABLEFILE, Base64.NO_WRAP)));
            methodBuilder.addParam(boolean.class, true);
            methodBuilder.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        webSettings.setDomStorageEnabled(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setBuiltInZoomControls(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setSupportZoom(true);
        webSettings.setDefaultTextEncodingName("UTF-8");
        webSettings.setBlockNetworkImage(false);//解决图片不显示
        webSettings.setBlockNetworkLoads(false);
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            webSettings.setMediaPlaybackRequiresUserGesture(false);
        }
        webSettings.setLoadsImagesAutomatically(true);
//        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            webSettings.setAllowUniversalAccessFromFileURLs(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            webSettings.setSafeBrowsingEnabled(false);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }
    }

    public void enablePlugins(final boolean enabled) {
        // SigmobAndroid 4.3 and above has no concept of plugin states
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2) {
            return;
        }

        if (enabled) {
            getSettings().setPluginState(WebSettings.PluginState.ON);
        } else {
            getSettings().setPluginState(WebSettings.PluginState.OFF);
        }
    }

    private float vRadius = 40;
    private int vWidth;
    private int vHeight;
    private int x;
    private int y;
    private Paint paint1;
    private Paint paint2;

    private void init(Context context) {
        paint1 = new Paint();
        paint1.setColor(Color.WHITE);
        paint1.setAntiAlias(true);
        paint1.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        //
        paint2 = new Paint();
        paint2.setXfermode(null);
    }

    public void setRadius(float radius) {
        vRadius = radius;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        vWidth = getMeasuredWidth();
        vHeight = getMeasuredHeight();
    }

    @Override
    public void draw(Canvas canvas) {

        x = this.getScrollX();
        y = this.getScrollY();
        Bitmap bitmap = Bitmap.createBitmap(x + vWidth, y + vHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas2 = new Canvas(bitmap);
        super.draw(canvas2);
        drawLeftUp(canvas2);
        drawRightUp(canvas2);
//        drawLeftDown(canvas2);
//        drawRightDown(canvas2);
        canvas.drawBitmap(bitmap, 0, 0, paint2);
        bitmap.recycle();
    }

    private void drawLeftUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(x, vRadius);
        path.lineTo(x, y);
        path.lineTo(vRadius, y);
        path.arcTo(new RectF(x, y, x + vRadius * 2, y + vRadius * 2), -90, -90);
        path.close();
        canvas.drawPath(path, paint1);
    }

    private void drawRightUp(Canvas canvas) {
        Path path = new Path();
        path.moveTo(x + vWidth, y + vRadius);
        path.lineTo(x + vWidth, y);
        path.lineTo(x + vWidth - vRadius, y);
        path.arcTo(new RectF(x + vWidth - vRadius * 2, y, x + vWidth,
                y + vRadius * 2), -90, 90);
        path.close();
        canvas.drawPath(path, paint1);
    }


    private void drawLeftDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(x, y + vHeight - vRadius);
        path.lineTo(x, y + vHeight);
        path.lineTo(x + vRadius, y + vHeight);
        path.arcTo(new RectF(x, y + vHeight - vRadius * 2,
                x + vRadius * 2, y + vHeight), 90, 90);
        path.close();
        canvas.drawPath(path, paint1);
    }


    private void drawRightDown(Canvas canvas) {
        Path path = new Path();
        path.moveTo(x + vWidth - vRadius, y + vHeight);
        path.lineTo(x + vWidth, y + vHeight);
        path.lineTo(x + vWidth, y + vHeight - vRadius);
        path.arcTo(new RectF(x + vWidth - vRadius * 2, y + vHeight
                - vRadius * 2, x + vWidth, y + vHeight), 0, 90);
        path.close();
        canvas.drawPath(path, paint1);
    }

}
