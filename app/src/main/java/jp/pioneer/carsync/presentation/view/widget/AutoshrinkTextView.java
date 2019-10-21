package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;

import jp.pioneer.carsync.presentation.util.TextSizeUtil;

import me.grantland.widget.AutofitHelper;
import me.grantland.widget.BuildConfig;
import jp.pioneer.carsync.R;
import timber.log.Timber;

/**
 * Ver 0.5.1 Limit Font Size を Auto-Fit に吸収 by nakano on 2016/06/10
 * 「ポルトガル/ブラジル語のようなベースラインよりも下に突き出している文字が切れるケースがあります」指摘の対策。
 * limit_max_fontsize と limit_min_fontsize は AutoFit と見直し、↑障害を引き起こすpreShrinkTextSize()を解消した。
 * Ver 0.5.0 android-autofittextview に移行 by nakano on 2016/06/07
 * https://github.com/grantland/android-autofittextview
 *
 * @attr ref R.styleable.AutofitTextView_
 * auto_resize_font        default = true
 * limit_max_fontsize      default = true
 * limit_min_fontsize      default = false                     // 0.5.1
 * resize_font_unit        default = COMPLEX_UNIT_SP
 * resize_font_min         default = MIN_TEXT_SIZE = 14.0f
 * resize_font_max         default = MAX_TEXT_SIZE = 17.0f
 * indent_size             default = 0.0f
 * log_debug_message       default = CONSTAINT.CORE_ENABLE_RESIZE_FONT_LOG
 */
public class AutoshrinkTextView extends AppCompatTextView implements AutofitHelper.OnTextSizeChangeListener {
    private static final String TAG = AutoshrinkTextView.class.getSimpleName();

    private static final float MIN_TEXT_SIZE = 14.0f;
    private static final float MAX_TEXT_SIZE = 17.0f;

    private AutofitHelper mHelper;
    private boolean mLimitMaxFontsize;
    private boolean mLimitMinFontsize;
    private float mIndentSize;              // pixel
    private int _mFontSizeUnit = TypedValue.COMPLEX_UNIT_SP;

    private float mMaxSizeOfLimit = 0.0f;   // resize_font_max => pixel

    private boolean mLogDebugMessage;
    private boolean mBusy = true;

    /**
     * 切れなく表示に必要なサイズ
     * auto-fit 中、それは min よりも小さいと判明した時、setTextSize(min) し、その設定すべき値をここに残す。
     */
    private float mFontSizeShouldBe;            // pixel

    public AutoshrinkTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    public AutoshrinkTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, android.R.attr.textViewStyle);
    }

    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mHelper = AutofitHelper.create(this, attrs, defStyleAttr);
        mHelper.addOnTextSizeChangeListener(this);
        String fontUnit;
        float fontMin;
        float fontMax;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoshrinkTextView);

        boolean autoResizeFont = a.getBoolean(R.styleable.AutoshrinkTextView_auto_resize_font, true);
        setAutoResizeFont(autoResizeFont);

        boolean limitMaxFontsize = a.getBoolean(R.styleable.AutoshrinkTextView_limit_max_fontsize, true);

        boolean limitMinFontsize = a.getBoolean(R.styleable.AutoshrinkTextView_limit_min_fontsize, false);

        float indentSize = a.getFloat(R.styleable.AutoshrinkTextView_indent_size, 0);

        boolean logDebugMessage = a.getBoolean(R.styleable.AutoshrinkTextView_log_debug_message, BuildConfig.DEBUG);
        setLogDebugMessage(logDebugMessage);

        fontMin = a.getFloat(R.styleable.AutoshrinkTextView_resize_font_min, MIN_TEXT_SIZE);
        fontMax = a.getFloat(R.styleable.AutoshrinkTextView_resize_font_max, MAX_TEXT_SIZE);
        fontUnit = a.getString(R.styleable.AutoshrinkTextView_resize_font_unit);

        a.recycle();

        if (TextUtils.isEmpty(fontUnit)) {
            setFontSizeUnit(TypedValue.COMPLEX_UNIT_SP);
        } else if (fontUnit.equalsIgnoreCase("dp")) {
            setFontSizeUnit(TypedValue.COMPLEX_UNIT_DIP);
        } else if (fontUnit.equalsIgnoreCase("sp")) {
            setFontSizeUnit(TypedValue.COMPLEX_UNIT_SP);
        } else {
            if (isLogDebugMessage()) {
                Timber.d("SHRINK init: Unknown unit %s", fontUnit);
            }
            setFontSizeUnit(TypedValue.COMPLEX_UNIT_SP);
        }

        setMinTextSize(fontMin, getPixels(fontMin));
        setMaxSizeOfLimit(getPixels(fontMax));
        setLimitMaxFontsize(limitMaxFontsize);
        setLimitMinFontsize(limitMinFontsize);
        setIndentSize(getPixels(indentSize));

        setGravity(getGravity()); // to force CENTER_VERTICAL
    }

    @Override
    public void setTextSize(int unit, float size) {
        super.setTextSize(unit, size);
        if (mHelper != null) {
            mHelper.setTextSize(unit, size);
        }
    }

    @Override
    public void setLines(int lines) {
        super.setLines(lines);
        if (mHelper != null) {
            mHelper.setMaxLines(lines);
        }
    }

    @Override
    public void setMaxLines(int maxLines) {
        super.setMaxLines(maxLines);
        if (mHelper != null) {
            mHelper.setMaxLines(maxLines);
        }
    }

    /**
     * （Menu 画面）List にある時 Indent は最初表示されない障害の特別対策
     *
     * @return
     */
    @Override
    public boolean onPreDraw() {
        this.forceLayout();

        applyTextIndent();

        return (super.onPreDraw());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        // 縮小されても高さが変わらないように、最低でも「最大の textSize を使った時の高さ」を確保するようにする
        // 注意: 複数行には対応していない

        /*
         * XXX: AutofitHelper#getMaxTextSize() ではなく AutoshrinkHelper#getMaxSizeOfLimit() を使うべき?
         * 同じような値を別に保持しておく目的がよくわからない…
         */
        float maxTextSize = mHelper.getMaxTextSize();

        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            int height = TextSizeUtil.getIntegralHeight(this, maxTextSize);
            if (heightMode == MeasureSpec.AT_MOST)
                height = Math.min(height, MeasureSpec.getSize(heightMeasureSpec));
            if (height > getMeasuredHeight())
                setMeasuredDimension(getMeasuredWidth(), height);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        applyTextIndent();

        super.onLayout(changed, left, top, right, bottom);
    }

    public void applyTextIndent() {
        if (getIndentSize() <= 0) {
            return;
        }

        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp == null) {
            return;
        }

        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;

        float f = getIndentSize();
        int mySize = (int) f;

//        if(mHelper.isLogDebugMessage()) {
//            Locale locale = Locale.getDefault();
//            Timber.d("SHRINK onLayout Entry text=%s Locale=%s", getText(), locale.toString());
//        }

        mlp.setMargins(mySize, mlp.topMargin, mlp.rightMargin, mlp.bottomMargin);
        setLayoutParams(mlp);
    }


    /**
     * @param minSize pixels
     */
    public void setMinTextSize(float unit, float minSize) {
        mHelper.setMinTextSize((int) unit, minSize);
    }

    public float getPixels(float unit) {
        float pixels;
        final int myFontSizeUnit = getFontSizeUnit();

        // refer from TextView#setTextSize(int, float) implementation
        Context context = getContext();
        Resources rs;
        if (context != null) {
            rs = context.getResources();
        } else {
            rs = Resources.getSystem();
        }
        pixels = TypedValue.applyDimension(myFontSizeUnit, unit, rs.getDisplayMetrics());

        return pixels;
    }

    public boolean isAutoResizeFont() {
        return mHelper.isEnabled();
    }

    public void setAutoResizeFont(boolean autoResizeFont) {
        mHelper.setEnabled(autoResizeFont);
    }

    @Override
    public void onTextSizeChange(float textSize, float oldTextSize) {
    }

    @Override
    public void setGravity(int gravity) {
        // 縦方向は強制的に CENTER 固定とする。このクラスの用途的にこれで問題ないはず (問題になるようなら別のクラスを使うべき)
        gravity &= ~Gravity.VERTICAL_GRAVITY_MASK;
        gravity |= Gravity.CENTER_VERTICAL;

        super.setGravity(gravity);
    }


    public boolean isLimitMaxFontsize() {
        return mLimitMaxFontsize;
    }

    public void setLimitMaxFontsize(boolean limitMaxFontsize) {
        float textSize = this.getTextSize();
        float maxPixels = getMaxSizeOfLimit();
        if ((limitMaxFontsize) && (textSize > maxPixels)) {
            this.mLimitMaxFontsize = true; // limitMaxFontsize
            mHelper.setEnabled(true);
        }
    }

    public boolean isLimitMinFontsize() {
        return mLimitMinFontsize;
    }

    public void setLimitMinFontsize(boolean limitMinFontsize) {
        float textSize = this.getTextSize();
        if ((limitMinFontsize) && (textSize < mHelper.getMinTextSize())) {
            this.mLimitMinFontsize = true; // limitMinFontsize
            mHelper.setEnabled(true);
        }
    }

    public float getIndentSize() {
        return mIndentSize;
    }

    public void setIndentSize(float mIndentSize) {
        this.mIndentSize = mIndentSize;
    }

    public int getFontSizeUnit() {
        return _mFontSizeUnit;
    }

    public void setFontSizeUnit(int unit) {
        this._mFontSizeUnit = unit;
    }

    public boolean isLogDebugMessage() {
        return mLogDebugMessage;
    }

    public void setLogDebugMessage(boolean logDebugMessage) {
        this.mLogDebugMessage = logDebugMessage;
    }

    public float getMaxSizeOfLimit() {
        return mMaxSizeOfLimit;
    }

    public void setMaxSizeOfLimit(float mMaxSizeOfLimit) {
        this.mMaxSizeOfLimit = mMaxSizeOfLimit;
    }

    public float getFontSizeShouldBe() {
        return mFontSizeShouldBe;
    }

    public void setFontSizeShouldBe(float fontSizeShouldBe) {
        this.mFontSizeShouldBe = fontSizeShouldBe;
    }

    private void saveFontSizeShouldBe(float size) {
        setFontSizeShouldBe(size);
    }
}
