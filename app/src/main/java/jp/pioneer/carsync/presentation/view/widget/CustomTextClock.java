package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextClock;

import jp.pioneer.carsync.R;

/**
 * HOME画面用のカスタムTextClock
 */

public class CustomTextClock extends TextClock {
    private  final float MIN_TEXT_SIZE = getResources().getDimension(R.dimen.home_clock_text_size_min);
    private  final float MAX_TEXT_SIZE = getResources().getDimension(R.dimen.home_clock_text_size_max);

    public CustomTextClock(Context context) {
        super(context);
    }

    public CustomTextClock(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);
        resize();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        resize();
    }

    private void resize() {

        Paint paint = new Paint();

        int viewWidth = this.getWidth();
        int viewHeight = this.getHeight();

        float textSize = MAX_TEXT_SIZE;

        paint.setTextSize(textSize);

        Paint.FontMetrics fm = paint.getFontMetrics();
        //2行分の高さ
        float textHeight = (Math.abs(fm.ascent))*2 + (Math.abs(fm.descent))/2;

        float textWidth = paint.measureText("00");
        while (viewWidth < textWidth | viewHeight < textHeight){
            if (MIN_TEXT_SIZE >= textSize){
                textSize = MIN_TEXT_SIZE;
                break;
            }

            textSize = textSize - 2;

            paint.setTextSize(textSize);

            fm = paint.getFontMetrics();
            textHeight = (Math.abs(fm.ascent))*2 + (Math.abs(fm.descent))/2;
            textWidth = paint.measureText("00");
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

}
