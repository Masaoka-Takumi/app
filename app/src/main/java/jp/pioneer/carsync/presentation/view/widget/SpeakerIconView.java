package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

import jp.pioneer.carsync.R;

/**
 * Created by tsuyosh on 16/05/09.
 */
public class SpeakerIconView extends AppCompatImageView {
    private static final int[] ON_STATE = {R.attr.state_on};

    private boolean mOn;

    public SpeakerIconView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SpeakerIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SpeakerIconView(Context context) {
        super(context);
    }

    public boolean isOn() {
        return mOn;
    }

    public void setOn(boolean on) {
        if (mOn == on) return;
        mOn = on;
        refreshDrawableState();
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        // state_pressedのdrawableがちゃんと表示されるように修正
        // @see https://code.google.com/p/android/issues/detail?id=172067#c21
        invalidate();
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (mOn) mergeDrawableStates(state, ON_STATE);
        return state;
    }
}
