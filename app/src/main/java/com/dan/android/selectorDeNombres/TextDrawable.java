package com.dan.android.selectorDeNombres;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import com.dan.android.selectordenombres.R;


public class TextDrawable extends Drawable {

    private final String text;
    private final Paint paint;
    private final Context mContext;


    public TextDrawable(String text, Context c) {
        this.text = text;
        this.mContext = c;

        this.paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        //paint.setFakeBoldText(true);
        //paint.setShadowLayer(6f, 0, 0, Color.BLACK);
        paint.setStyle(Paint.Style.FILL);

        paint.setTextSize(c.getResources().getDimensionPixelSize(R.dimen.progress_text_size));
        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public void draw(Canvas canvas) {
        // the display area.
        Rect areaRect = new Rect(0, 0, mContext.getResources().getDimensionPixelSize(R.dimen.progress_rec_size), mContext.getResources().getDimensionPixelSize(R.dimen.progress_rec_size));

        //canvas.drawRect(areaRect, paint);

        RectF bounds = new RectF(areaRect);
        // measure text width
        bounds.right = paint.measureText(text, 0, text.length());
        // measure text height
        bounds.bottom = paint.descent() - paint.ascent();

        bounds.left += (areaRect.width() - bounds.right) / 2.0f;
        bounds.top += (areaRect.height() - bounds.bottom) / 2.0f;

        //paint.setColor(Color.BLACK);
        canvas.drawText(text, bounds.left, bounds.top - paint.ascent(), paint);





        //canvas.drawText(text, xPos, yPos, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}