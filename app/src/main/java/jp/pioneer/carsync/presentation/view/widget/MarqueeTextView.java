package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;

/**
 * Created by NSW00_007906 on 2017/08/02.
 */

public class MarqueeTextView extends AppCompatTextView {

    private boolean mIsMarqueeStarted;

    public MarqueeTextView(Context context) {
        this(context, null);
    }

    public MarqueeTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarqueeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        setMarqueeRepeatLimit(-1);
        setFocusable(true);
        setFocusableInTouchMode(true);
        setSingleLine(true);
    }

    @Override
    public boolean isSelected() {
        // Always return true. because marquee animation stop when return false.
        return true;
    }

    public void startMarquee() {
        if (mIsMarqueeStarted) {
            return;
        }
        mIsMarqueeStarted = true;
        setEllipsize(TextUtils.TruncateAt.MARQUEE);
        invalidate();
    }

    public void stopMarquee() {
        if (!mIsMarqueeStarted) {
            return;
        }
        mIsMarqueeStarted = false;
        setEllipsize(TextUtils.TruncateAt.END);
        invalidate();
    }
}
