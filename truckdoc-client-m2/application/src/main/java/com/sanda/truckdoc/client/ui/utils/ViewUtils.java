package com.sanda.truckdoc.client.ui.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

/**
 * Created by astra on 29.10.2015.
 */
public class ViewUtils {

    public static int getColorRef(Context context, int resId) {
        final TypedValue value = new TypedValue();
        context.getResources().getValue(resId, value, true);

        if (value.type == TypedValue.TYPE_ATTRIBUTE) {
            final TypedArray attributes = context.getTheme().obtainStyledAttributes(new int[]{value.data});
            int dimension = attributes.getColor(0, 0);
            attributes.recycle();
            return dimension;
        } else {
            return context.getResources().getColor(resId);
        }
    }
}
