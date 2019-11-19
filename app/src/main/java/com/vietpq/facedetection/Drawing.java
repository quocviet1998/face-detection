package com.vietpq.facedetection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class Drawing extends View {

    Paint paint;
    Path path;

    public Drawing(Context context){
        super(context);
        init();
    }

    public Drawing(Context context, AttributeSet attrs){
        super(context, attrs);
        init();
    }

    public Drawing(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs, defStyle);
        init();
    }

    private void init(){
        paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(10);
        paint.setStyle(Paint.Style.STROKE);
    }


    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawRect(200, 500, 200, 350, paint);
        canvas.drawRect(100, 100, 300, 400, paint);
    }
}
