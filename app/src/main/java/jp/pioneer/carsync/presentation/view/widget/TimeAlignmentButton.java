package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.IntDef;
import android.support.v7.widget.AppCompatButton;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TextAppearanceSpan;
import android.util.AttributeSet;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import jp.pioneer.carsync.R;

/**
 * Time Alignmentのモード設定ボタン
 * Created by tsuyosh on 16/05/02.
 */
public class TimeAlignmentButton extends AppCompatButton {
    private static final int[] ON_STATE = {R.attr.state_on};

    public static final int TA_MODE_INITIAL = 0;
    public static final int TA_MODE_ATA = 1;
    public static final int TA_MODE_CUSTOM = 2;
    public static final int TA_MODE_OFF = 3;

    private static final String TEXT_PREFIX = "";

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TA_MODE_INITIAL, TA_MODE_ATA, TA_MODE_CUSTOM, TA_MODE_OFF})
    @interface TimeAlignmentMode {}

    private int mTaMode;

    private boolean mOn;

    private ColorStateList mValueColorList;

    private TextAppearanceSpan mLabelTextAppearanceSpan;
    private SpannableStringBuilder mSpannableStringBuilder;

    public TimeAlignmentButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    public TimeAlignmentButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeAlignmentButton(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimeAlignmentButton, defStyleAttr, defStyleRes);
        mTaMode = a.getInt(R.styleable.TimeAlignmentButton_taMode, TA_MODE_OFF);
        mValueColorList = a.getColorStateList(R.styleable.TimeAlignmentButton_valueColor);
        a.recycle();

        mSpannableStringBuilder = new SpannableStringBuilder(TEXT_PREFIX);
        mLabelTextAppearanceSpan = new TextAppearanceSpan(getContext(), R.style.TimeAlignmentButton_LabelTextAppearance);
        mSpannableStringBuilder.setSpan(
                mLabelTextAppearanceSpan,
                0, TEXT_PREFIX.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        applyValueColor();
        applyTaMode();
    }

    public void setValueColorList(ColorStateList list) {
        mValueColorList = list;
        applyValueColor();
    }

    private void applyValueColor() {
        setTextColor(mValueColorList);
        buildText();
    }

    public int getTaMode() {
        return mTaMode;
    }

    public void setTaMode(@TimeAlignmentMode int mode) {
        if (mTaMode == mode) return;
        mTaMode = mode;
        applyTaMode();
    }

    private void applyTaMode() {
        buildText();
        setOn(mTaMode != TA_MODE_OFF);
    }

    private void buildText() {
        String text;
        Context context = getContext();
        switch(mTaMode) {
            case TA_MODE_INITIAL:
                text = context.getString(R.string.set_222);
                break;
            case TA_MODE_ATA:
                text = context.getString(R.string.set_220);
                break;
            case TA_MODE_CUSTOM:
                text = context.getString(R.string.set_221);
                break;
            case TA_MODE_OFF:
                text = context.getString(R.string.set_223);
                break;
            default:
                return;
        }
        int start = TEXT_PREFIX.length();
        int end = mSpannableStringBuilder.length();
        mSpannableStringBuilder.replace(start, end, text);
        setText(mSpannableStringBuilder);
    }

    private void setOn(boolean on) {
        if (mOn == on) return;
        mOn = on;
        refreshDrawableState();
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (mOn) {
            mergeDrawableStates(state, ON_STATE);
        }
        return state;
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        // state_pressedのdrawableがちゃんと表示されるように修正
        // @see https://code.google.com/p/android/issues/detail?id=172067#c21
        invalidate();
    }
}
