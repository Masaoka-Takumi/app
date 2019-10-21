package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;

import jp.pioneer.carsync.presentation.util.FilterDesignDefaults;
import jp.pioneer.carsync.presentation.util.FilterDrawingUtil;
import jp.pioneer.carsync.presentation.util.FilterGraphGeometry;

import java.util.Arrays;

public class FilterFrequencyView extends View {

    private Paint mTextPaint;
    private float mDensity;

    public FilterFrequencyView (Context context) {
        super(context);
        init();
    }

    public FilterFrequencyView (Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init () {
        mDensity = getContext().getResources().getDisplayMetrics().density;

        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11.0f, getResources().getDisplayMetrics()));
        mTextPaint.setColor(Color.argb(127, 255, 255, 255));
        mTextPaint.setAntiAlias(true);
    }

    private int dpToPx (int dp) {
        return (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void setFrequencyStrings (@NonNull FilterGraphGeometry graphGeometry) {
        setFrequencyStrings(graphGeometry,
                FilterDesignDefaults.defaultPathBuilderGridPathSpec().primaryFrequencies,
                new String [] { "20", "100", "1k", "10k", "20k" });
    }

    public void setFrequencyStrings (@NonNull FilterGraphGeometry graphGeometry, int [] frequencies, String [] strings) {
        setFrequencyStrings(strings, FilterDrawingUtil.frequencyLocations(graphGeometry, frequencies));
    }

    private String [] mStrings;
    /** 各文字列の中心の x 座標 (dp) */
    private int [] mLocations;

    /**
     * @param strings 周波数文字列
     * @param locations 各文字列の中心の x 座標 (dp)
     *                  通常は {@link FilterDrawingUtil#frequencyLocations(FilterGraphGeometry, int[])} で取得する
     */
    public void setFrequencyStrings (String [] strings, int [] locations) {
        final int n = strings.length;
        if (n != locations.length)
            throw new IllegalArgumentException("string.length must match to locations.length");

        mStrings = Arrays.copyOf(strings, n);
        mLocations = Arrays.copyOf(locations, n);

        invalidate();
    }


    @Override
    protected void onDraw (Canvas canvas) {
        super.onDraw(canvas);

        if (mStrings == null)
            return;

        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float y = getHeight() - fm.descent;
        for (int i = 0; i < mStrings.length; i += 1) {
            String s = mStrings[i];
            int loc = mLocations[i];
            canvas.drawText(s, loc * mDensity, y, mTextPaint);
        }
    }

    public void setTypeface(Typeface tf) {
        if (mTextPaint.getTypeface() == tf) return;
        mTextPaint.setTypeface(tf);
        invalidate();
    }

    public void setLocations (int[] locations) {
        if (mLocations.length != locations.length)
            throw new IllegalArgumentException();
        mLocations = locations;
        invalidate();
    }
}