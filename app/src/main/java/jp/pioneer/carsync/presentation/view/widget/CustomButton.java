package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

/**
 * Created by tsuyosh on 16/05/02.
 */
public class CustomButton extends AppCompatButton {
    public CustomButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomButton(Context context) {
        super(context);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        // state_pressedのdrawableがちゃんと表示されるように修正
        // @see https://code.google.com/p/android/issues/detail?id=172067#c21
        invalidate();
    }
}
