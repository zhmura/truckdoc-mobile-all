package com.sanda.truckdoc.client.ui.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sanda.truckdoc.client.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

public class TwoLineTextView extends LinearLayout {

    private TextView mText1;
    private TextView mText2;

    @IntDef({VISIBLE, INVISIBLE, GONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Visibility {
    }

    public TwoLineTextView(Context context) {
        this(context, null, 0);
    }

    public TwoLineTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoLineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TwoLineTextView);
        String text1 = a.getString(R.styleable.TwoLineTextView_text1);
        String text2 = a.getString(R.styleable.TwoLineTextView_text2);
        int color1 = a.getColor(R.styleable.TwoLineTextView_color1,
                context.getResources().getColor(R.color.primary_text_default_material_dark));
        int color2 = a.getColor(R.styleable.TwoLineTextView_color1,
                context.getResources().getColor(R.color.secondary_text_default_material_dark));
        a.recycle();

        setOrientation(VERTICAL);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.widget_two_line_text_view, this, true);
        mText1 = (TextView) getChildAt(0);
        mText2 = (TextView) getChildAt(1);
        mText1.setText(text1);
        mText2.setText(text2);
    }

    public void setText1Visibility(@Visibility int visibility) {
        mText1.setVisibility(visibility);
    }

    public void setText2Visibility(@Visibility int visibility) {
        mText2.setVisibility(visibility);
    }
}
