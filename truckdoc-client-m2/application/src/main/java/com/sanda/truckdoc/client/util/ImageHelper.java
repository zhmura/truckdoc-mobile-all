package com.sanda.truckdoc.client.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by astra on 17.06.2015.
 */
public class ImageHelper {

    public static void drawWaterMark(final Bitmap src, String text) {
        Canvas canvas = new Canvas(src);
        canvas.drawBitmap(src, 0, 0, null);
        Paint paint = new Paint();
        paint.setColor(Color.RED);
        paint.setTextSize(18);
        paint.setAntiAlias(true);
        paint.setUnderlineText(true);
        canvas.drawText(text, 20, 25, paint);
    }
}
