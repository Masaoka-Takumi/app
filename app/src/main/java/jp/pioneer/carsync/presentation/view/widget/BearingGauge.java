package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import jp.pioneer.carsync.R;

/**
 * 方位表示
 */

public class BearingGauge extends View {
    private static final int BEARING_VISIBLE_RANGE = 165; //表示する方位の範囲
    private static final float BEARING_UNIT = 7.5f; //1目盛り単位の方位
    // 方位 0 ~ 360
    private float bearingLevel = 0;
    private Paint mPaintLine, mPaintLineL, mPaintLineN, mPaintText;
    private float mTextHeight, mTextHeightStart, mDivisionHeight, mDivisionHeightLarge, mDivisionHeightMargin;

    public BearingGauge(Context context) {
        super(context);
        init();
    }

    public BearingGauge(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BearingGauge(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setBearingLevel(float bearingLevel) {
        this.bearingLevel = bearingLevel;
        invalidate();
    }

    private void init() {
        Resources res = getResources();
        mPaintLine = new Paint();
        mPaintLine.setColor(Color.WHITE);
        mPaintLine.setStrokeWidth(res.getDimension(R.dimen.bearing_division_width));
        mPaintLine.setAlpha(255 * 50 / 100);
        mPaintLineL = new Paint();
        mPaintLineL.setColor(Color.WHITE);
        mPaintLineL.setStrokeWidth(res.getDimension(R.dimen.bearing_division_width));
        mPaintLineN = new Paint();
        mPaintLineN.setColor(Color.RED);
        mPaintLineN.setStrokeWidth(res.getDimension(R.dimen.bearing_division_width));
        mPaintText = new Paint();
        mPaintText.setColor(Color.WHITE);
        mPaintText.setTypeface(Typeface.create("sans-serif", Typeface.BOLD));
        mPaintText.setTextSize(res.getDimension(R.dimen.bearing_division_label_font_size));
        mPaintText.setTextAlign(Paint.Align.CENTER);
        mPaintText.setAlpha(255 * 75 / 100);
        mTextHeight = res.getDimension(R.dimen.bearing_division_label_font_size);
        mTextHeightStart = res.getDimension(R.dimen.bearing_division_height_start);
        mDivisionHeight = res.getDimension(R.dimen.bearing_division_label_font_size);
        mDivisionHeightLarge = res.getDimension(R.dimen.bearing_division_large_height);
        mDivisionHeightMargin = res.getDimension(R.dimen.bearing_division_height_margin);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        int width = canvas.getWidth();
        float firstLevel = bearingLevel - BEARING_VISIBLE_RANGE / 2;
        float lastLevel = bearingLevel + BEARING_VISIBLE_RANGE / 2;
        float firstDivision = 0;
        for (float f = (float)Math.floor(firstLevel); f < lastLevel; f = f + 0.5f) {
            float value = f;
            if(f<0)value = -f;
            if (value % BEARING_UNIT == 0) {
                firstDivision = f;
                break;
            }
        }
        for (float f = firstDivision; f < lastLevel; f = f + BEARING_UNIT) {
            float x = (f - firstLevel) * width / BEARING_VISIBLE_RANGE;
            canvas.drawLine(x, mTextHeightStart + mDivisionHeightMargin, x, mTextHeightStart + mDivisionHeightMargin + mDivisionHeight, mPaintLine);
            float level = f;
            if (f < 0) {
                level = f + 360;
            } else if (f >= 360) {
                level = f - 360;
            }
            if (level == 0) {
                canvas.drawLine(x, mTextHeightStart, x, mTextHeightStart + mDivisionHeightLarge, mPaintLineN);
                canvas.drawText(getResources().getString(R.string.speed_meter_bearing_north), x, mTextHeight, mPaintText);
            } else if (level == 90) {
                canvas.drawLine(x, mTextHeightStart, x, mTextHeightStart + mDivisionHeightLarge, mPaintLineL);
                canvas.drawText(getResources().getString(R.string.speed_meter_bearing_east), x, mTextHeight, mPaintText);
            } else if (level == 180) {
                canvas.drawLine(x, mTextHeightStart, x, mTextHeightStart + mDivisionHeightLarge, mPaintLineL);
                canvas.drawText(getResources().getString(R.string.speed_meter_bearing_south), x, mTextHeight, mPaintText);
            } else if (level == 270) {
                canvas.drawLine(x, mTextHeightStart, x, mTextHeightStart + mDivisionHeightLarge, mPaintLineL);
                canvas.drawText(getResources().getString(R.string.speed_meter_bearing_west), x, mTextHeight, mPaintText);
            }
        }
    }
}
