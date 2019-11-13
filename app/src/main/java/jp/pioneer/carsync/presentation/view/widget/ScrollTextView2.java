package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import java.util.ArrayList;

import timber.log.Timber;

public class ScrollTextView2 extends AppCompatTextView {
    private static final int DELAY_TIME = 3000;
    private static final int BLANK_TIME = 200;
    // scrolling feature
    private Scroller mSlr;

    // milliseconds for a round of scrolling
    private int mRndDuration = 10000;
    private int mScrollSpeed = 500;
    // the X offset when paused
    private int mXPaused = 0;

    // whether it's being paused
    private boolean mPaused = true;
    private int mCount = 0;
    private int mGravity = 0;
    private Handler mHandler = null;
    private ArrayList<String> mStringArrayList = new ArrayList<>();
    private int mDisplayCount = 0;
    private Runnable mRunnableResumeScroll = new Runnable() {
        @Override
        public void run() {
            resumeScroll();
        }
    };
    private Runnable mRunnableBlankText = new Runnable() {
        @Override
        public void run() {
            Timber.d("mRunnableBlankText");
            setText("");
            mHandler.postDelayed(mRunnableNextText,BLANK_TIME);
        }
    };
    private Runnable mRunnableNextText = new Runnable() {
        @Override
        public void run() {
            Timber.d("mRunnableNextText");
            if(mDisplayCount<mStringArrayList.size()) {
                setText(mStringArrayList.get(mDisplayCount));
                startScroll();
            }else{
                if(mStringArrayList.size()>0) {
                    setText(mStringArrayList.get(mDisplayCount % (mStringArrayList.size())));
                    startNotScroll();
                }
            }
            mDisplayCount++;
        }
    };
    /*
     * constructor
     */
    public ScrollTextView2(Context context) {
        this(context, null);
    }

    /*
     * constructor
     */
    public ScrollTextView2(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    /*
     * constructor
     */
    public ScrollTextView2(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // customize the TextView
        init();
    }

    public void setDefaultGravity(int gravity) {
        mGravity = gravity;
    }

    public int getDisplayCount() {
        return mDisplayCount;
    }

    public void setDisplayCount(int displayCount) {
        mDisplayCount = displayCount;
    }

    public void setStringArrayList(ArrayList<String> stringArrayList) {
        Timber.d("setStringArrayList");
        mStringArrayList = stringArrayList;
        mDisplayCount = 0;
        mHandler.postDelayed(mRunnableNextText,BLANK_TIME);
        //setText(mStringArrayList.get(0));
        //startScroll();
    }

    public void restartDisplay(ArrayList<String> stringArrayList) {
        Timber.d("restartDisplay");
        mStringArrayList = stringArrayList;
        setText("");
        mHandler.postDelayed(mRunnableNextText,BLANK_TIME);
    }

    private void init() {
        mHandler = new Handler(Looper.getMainLooper());
        setSingleLine();
        setEllipsize(null);
        setVisibility(INVISIBLE);
        mGravity = this.getGravity();
        setHorizontallyScrolling(true);
        this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        mSlr = new Scroller(this.getContext(), new LinearInterpolator());
        mXPaused = 0;

    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacks(mRunnableResumeScroll);
        mHandler.removeCallbacks(mRunnableBlankText);
        mHandler.removeCallbacks(mRunnableNextText);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //BackGroundからの復帰で再レイアウトされるため
        int textLen = calculateTextLen();
        if (textLen <= getWidth()) {
            setVisibility(VISIBLE);
            setScroller(mSlr);
            if (mGravity == Gravity.CENTER) {
                mSlr.startScroll(mXPaused, 0, -(getWidth() - textLen) / 2, 0, 0);
            } else if (mGravity == (Gravity.RIGHT | Gravity.CENTER_VERTICAL)) {
                mSlr.startScroll(mXPaused, 0, -(getWidth() - textLen), 0, 0);
            } else {
                mSlr.startScroll(mXPaused, 0, 0, 0, 0);
            }
        }
    }

    /**
     * begin to scroll the text from the original position
     */
    public void startScroll() {
        Timber.d("startScroll");
        mSlr.abortAnimation();
        mHandler.removeCallbacks(mRunnableResumeScroll);
        mHandler.removeCallbacks(mRunnableBlankText);
        mHandler.removeCallbacks(mRunnableNextText);
        // begin from the very right side
        //mXPaused = -1 * getWidth();
        mXPaused = 0;
        mCount = 0;
        // assume it's paused
        mPaused = true;
        this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        setVisibility(VISIBLE);
        setScroller(mSlr);
        int textLen = calculateTextLen();
        if (getWidth() == 0) return;
        if (textLen <= getWidth()) {
            if (mGravity == Gravity.CENTER) {
                mSlr.startScroll(mXPaused, 0, -(getWidth() - textLen) / 2, 0, 0);
            } else if (mGravity == (Gravity.RIGHT | Gravity.CENTER_VERTICAL)) {
                mSlr.startScroll(mXPaused, 0, -(getWidth() - textLen), 0, 0);
            } else {
                mSlr.startScroll(mXPaused, 0, 0, 0, 0);
            }
            invalidate();

            mHandler.postDelayed(mRunnableBlankText, DELAY_TIME);
            return;
        }
        mSlr.startScroll(mXPaused, 0, 0, 0, 0);
        invalidate();
        //if (getWidth() == 0) return;
        mHandler.postDelayed(mRunnableResumeScroll, DELAY_TIME);
    }
    public void startNotScroll() {
        Timber.d("startNotScroll");
        mSlr.abortAnimation();
        mHandler.removeCallbacks(mRunnableResumeScroll);
        mHandler.removeCallbacks(mRunnableBlankText);
        mHandler.removeCallbacks(mRunnableNextText);
        // begin from the very right side
        //mXPaused = -1 * getWidth();
        mXPaused = 0;
        mCount = 0;
        // assume it's paused
        mPaused = true;
        this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        setVisibility(VISIBLE);
        setScroller(mSlr);
        int textLen = calculateTextLen();
        if (getWidth() == 0) return;
        if (textLen <= getWidth()) {
            if (mGravity == Gravity.CENTER) {
                mSlr.startScroll(mXPaused, 0, -(getWidth() - textLen) / 2, 0, 0);
            } else if (mGravity == (Gravity.RIGHT | Gravity.CENTER_VERTICAL)) {
                mSlr.startScroll(mXPaused, 0, -(getWidth() - textLen), 0, 0);
            } else {
                mSlr.startScroll(mXPaused, 0, 0, 0, 0);
            }
            invalidate();
            mHandler.postDelayed(mRunnableBlankText, DELAY_TIME);
            return;
        }
        mSlr.startScroll(mXPaused, 0, 0, 0, 0);
        invalidate();
        //if (getWidth() == 0) return;
        mHandler.postDelayed(mRunnableBlankText, DELAY_TIME);

    }
    /**
     * resume the scroll from the pausing point
     */
    public void resumeScroll() {
        Timber.d("resumeScroll");
        //if (!mPaused) return;

        // Do not know why it would not scroll sometimes
        // if setHorizontallyScrolling is called in constructor.
        setHorizontallyScrolling(true);

        // use LinearInterpolator for steady scrolling

        int scrollingLen = calculateScrollingLen();
        int distance;
        if (mCount == 0) {
            distance = scrollingLen - (getWidth() + mXPaused);
        } else {
            mXPaused = -1 * getWidth();
            distance = getWidth();
        }
        //int distance = scrollingLen - (getWidth() + mXPaused);
/*        int duration = (new Double(mRndDuration * distance * 1.00000
                / scrollingLen)).intValue();*/
        int duration = (int) (1000f * distance / mScrollSpeed);
        setVisibility(VISIBLE);
        mSlr.startScroll(mXPaused, 0, distance, 0, duration);
        invalidate();
        mCount++;
        if (mCount > 1) {
            mPaused = true;
        } else {
            mPaused = false;
        }
    }

    /**
     * calculate the length of the text in pixel
     *
     * @return the Text length in pixels
     */
    private int calculateTextLen() {
        TextPaint tp = getPaint();
        String strTxt = getText().toString();
        int mt = (int) tp.measureText(strTxt);
        return mt;
    }

    /**
     * calculate the scrolling length of the text in pixel
     *
     * @return the scrolling length in pixels
     */
    private int calculateScrollingLen() {
        TextPaint tp = getPaint();
        Rect rect = new Rect();
        String strTxt = getText().toString();
        tp.getTextBounds(strTxt, 0, strTxt.length(), rect);
        int scrollingLen = rect.width() + getWidth();
        rect = null;
        return scrollingLen;
    }

    /**
     * pause scrolling the text
     */
    public void pauseScroll() {
        if (null == mSlr) return;

        if (mPaused)
            return;

        mPaused = true;

        // abortAnimation sets the current X to be the final X,
        // and sets isFinished to be true
        // so current position shall be saved
        mXPaused = mSlr.getCurrX();

        mSlr.abortAnimation();
    }

    @Override
    /*
     * override the computeScroll to restart scrolling when finished so as that
     * the text is scrolled forever
     */
    public void computeScroll() {
        super.computeScroll();

        if (null == mSlr) return;

        if (mSlr.isFinished() && (!mPaused)) {
            this.resumeScroll();
        }
        if(mCount>0&&mSlr.isFinished() && (mPaused)){
            //mDisplayCount++;
            mHandler.postDelayed(mRunnableBlankText,DELAY_TIME);
/*            if(mDisplayCount<mStringArrayList.size()) {
                setText(mStringArrayList.get(mDisplayCount));
                mHandler.postDelayed(mRunnableBlankText,DELAY_TIME);

            }else{
                if(mStringArrayList.size()>0) {
                    setText(mStringArrayList.get(mDisplayCount % (mStringArrayList.size())));

                    mHandler.postDelayed(()->startNotScroll(),DELAY_TIME);
                }
            }*/
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        super.setText(text, type);
    }

    public int getRndDuration() {
        return mRndDuration;
    }

    public void setRndDuration(int duration) {
        this.mRndDuration = duration;
    }

    public boolean isPaused() {
        return mPaused;
    }
}