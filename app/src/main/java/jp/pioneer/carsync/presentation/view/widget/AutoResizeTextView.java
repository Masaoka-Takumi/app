package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;
import android.util.TypedValue;

import jp.pioneer.carsync.R;

/**
 * TextViewの縦・横幅にぴったり納まるようにフォントサイズを自動調整するカスタムビュー(BtAudio DeviceName表示用)
 */

public class AutoResizeTextView extends AppCompatTextView {
    private  final float MIN_TEXT_SIZE = getResources().getDimension(R.dimen.bt_device_name_text_min);
    private  final float MAX_TEXT_SIZE = getResources().getDimension(R.dimen.bt_device_name_text_max);


    public AutoResizeTextView(Context context) {
        super(context);
    }

    public AutoResizeTextView(Context context, AttributeSet attrs) {
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

        //    適当に大きめの数値からスタート
        float textSize = MAX_TEXT_SIZE;

        paint.setTextSize(textSize);

        Paint.FontMetrics fm = paint.getFontMetrics();
        float textHeight = Math.abs(fm.ascent) + Math.abs(fm.descent);

        float textWidth = paint.measureText(this.getText().toString());
        if(textWidth>viewWidth){
            textHeight = Math.abs(fm.ascent)*2 + Math.abs(fm.descent)*2;
        }
        boolean isMultiLine = false;
        while (viewWidth < textWidth | viewHeight < textHeight){
            if (MIN_TEXT_SIZE >= textSize){
                textSize = MIN_TEXT_SIZE;
                break;
            }

            textSize--;

            paint.setTextSize(textSize);

            fm = paint.getFontMetrics();
            if(textWidth > viewWidth&&(Math.abs(fm.ascent)+ Math.abs(fm.descent)) * 2<viewHeight){
                isMultiLine = true;
            }
            if(isMultiLine){
                textHeight = Math.abs(fm.ascent) * 2 + Math.abs(fm.descent) * 2;
                textWidth = paint.measureText(this.getText().toString())/2;
            }else{
                textHeight = Math.abs(fm.ascent) + Math.abs(fm.descent);
                textWidth = paint.measureText(this.getText().toString());
            }
        }
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
    }

}