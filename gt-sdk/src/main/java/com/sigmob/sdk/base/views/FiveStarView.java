package com.sigmob.sdk.base.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.View;

public class FiveStarView extends View {

    private final float mRating;
    Paint paint=new Paint();
    public FiveStarView(Context context, float rating) {
        super(context);
        mRating = rating;
        // TODO Auto-generated constructor stub
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int height=getHeight();
        int count = (int)(mRating);
        float outR=height/3.0f;
        boolean isFirst = true;
        float inR=outR*sin(18)/sin(180-36-18);

        paint.setAntiAlias(true);
        paint.setColor(Color.parseColor("#FFA500"));
        for (int i = count; i > 0 ; i--) {
            if(!isFirst){
                canvas.rotate(18);
                canvas.translate(outR*2.2f, 0 );
            }else {
                isFirst = false;
                canvas.translate(outR+2, height/2);
            }
            canvas.rotate(-18);
            Path path = getCompletePath(outR, inR);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, paint);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, paint);
        }
        
        if(mRating>count){
            float rating =  (mRating-count)+0.25f;
            if(rating>=1f){

                canvas.rotate(18);
                canvas.translate(outR*2.2f, 0 );
                canvas.rotate(-18);
                Path path = getCompletePath(outR, inR);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.parseColor("#FFA500"));
                canvas.drawPath(path, paint);
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);

            }else if(rating>=0.5f){
                canvas.rotate(18);
                canvas.translate(outR*2.2f, 0 );
                canvas.rotate(-18);
                Path path = getCompletePath(outR, inR);
                paint.setColor(Color.parseColor("#FFA500"));
                paint.setStyle(Paint.Style.STROKE);
                canvas.drawPath(path, paint);
                path = getHalfPath(outR, inR);
                paint.setColor(Color.parseColor("#FFA500"));
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(path, paint);
            }else {
                canvas.rotate(18);
                canvas.translate(outR*2.2f, 0 );
                canvas.rotate(-18);
                Path path = getCompletePath(outR, inR);
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.parseColor("#FFA500"));
                canvas.drawPath(path, paint);
            }

        }

        for (int i = (int)(5.0f-mRating); i > 0 ; i--) {
            canvas.rotate(18);
            canvas.translate(outR*2.2f, 0 );
            canvas.rotate(-18);
            Path path = getCompletePath(outR, inR);
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#FFA500"));
            canvas.drawPath(path, paint);
        }
    }

    private Path getHalfPath(float outR, float inR) {
        Path path;
        path=new Path();
        path.moveTo(outR*cos(72*4), outR*sin(72*4));
        path.lineTo(inR*cos(72*1+36), inR*sin(72*1+36));
        path.lineTo(outR*cos(72*2), outR*sin(72*2));
        path.lineTo(inR*cos(72*2+36), inR*sin(72*2+36));
        path.lineTo(outR*cos(72*3), outR*sin(72*3));
        path.lineTo(inR*cos(72*3+36), inR*sin(72*3+36));

        path.close();
        return path;
    }

    private Path getCompletePath(float outR, float inR) {
        Path path=new Path();
        float x = 0;
        float y = 0;

        x = outR*cos(72*0);
        y = outR*sin(72*0);
        path.moveTo(x, y);
        x =  inR*cos(72*0+36);
        y =  inR*sin(72*0+36);
        path.lineTo(x, y);
        x = outR*cos(72*1);
        y = outR*sin(72*1);
        path.lineTo(x, y);
        x =  inR*cos(72*1+36);
        y =  inR*sin(72*1+36);
        path.lineTo(x, y);
        x = outR*cos(72*2);
        y = outR*sin(72*2);
        path.lineTo(x, y);
        x =  inR*cos(72*2+36);
        y =  inR*sin(72*2+36);
        path.lineTo(x, y);
        x = outR*cos(72*3);
        y = outR*sin(72*3);
        path.lineTo(x, y);
        x =  inR*cos(72*3+36);
        y =  inR*sin(72*3+36);
        path.lineTo(x, y);
        x = outR*cos(72*4);
        y = outR*sin(72*4);
        path.lineTo(x, y);
        x =  inR*cos(72*4+36);
        y =  inR*sin(72*4+36);
        path.lineTo(x, y);
        path.close();
        return path;
    }




    float cos(int num){
        return (float) Math.cos(num*Math.PI/180);
    }

    float sin(int num) {
        return (float) Math.sin(num * Math.PI / 180);
    }

}
