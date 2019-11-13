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

/**
 * ScrollTextView
 */

public class ScrollTextView extends AppCompatTextView {
    private static final int DELAY_TIME = 1000;
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
    private Runnable mRunnableResumeScroll = new Runnable() {
        @Override
        public void run() {
            resumeScroll();
        }
    };
    /*
    * constructor
    */
    public ScrollTextView(Context context) {
        this(context, null);
    }

    /*
    * constructor
    */
    public ScrollTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    /*
    * constructor
    */
    public ScrollTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // customize the TextView
        init();
    }

    public void setDefaultGravity(int gravity) {
        mGravity = gravity;
    }

    private void init(){
        mHandler = new Handler(Looper.getMainLooper());
        setSingleLine();
        setEllipsize(null);
        setVisibility(INVISIBLE);
        mGravity = this.getGravity();
        setHorizontallyScrolling(true);
        this.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        mSlr = new Scroller(this.getContext(), new LinearInterpolator());
        mXPaused = 0;

    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacks(mRunnableResumeScroll);
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
        mSlr.abortAnimation();
        mHandler.removeCallbacks(mRunnableResumeScroll);
        // begin from the very right side
        //mXPaused = -1 * getWidth();
        mXPaused = 0;
        mCount = 0;
        // assume it's paused
        mPaused = true;
        this.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        setVisibility(VISIBLE);
        setScroller(mSlr);
        int textLen = calculateTextLen();

        if(textLen<=getWidth()) {
            if(mGravity == Gravity.CENTER){
                mSlr.startScroll(mXPaused, 0, -(getWidth() - textLen)/2, 0, 0);
            }else if(mGravity == (Gravity.RIGHT|Gravity.CENTER_VERTICAL)){
                mSlr.startScroll(mXPaused, 0, -(getWidth() - textLen), 0, 0);
            }else{
                mSlr.startScroll(mXPaused, 0, 0, 0, 0);
            }
            invalidate();
            return;
        }
        mSlr.startScroll(mXPaused, 0, 0, 0, 0);
        invalidate();
        if(getWidth()==0)return;
        mHandler.postDelayed(mRunnableResumeScroll, DELAY_TIME);
    }

    /**
     * resume the scroll from the pausing point
     */
    public void resumeScroll() {

        //if (!mPaused) return;

        // Do not know why it would not scroll sometimes
        // if setHorizontallyScrolling is called in constructor.
        setHorizontallyScrolling(true);

        // use LinearInterpolator for steady scrolling

        int scrollingLen = calculateScrollingLen();
        int distance;
        if(mCount == 0){
            distance = scrollingLen - (getWidth() + mXPaused);
        }else{
            mXPaused = -1 * getWidth();
            distance = getWidth();
        }
        //int distance = scrollingLen - (getWidth() + mXPaused);
/*        int duration = (new Double(mRndDuration * distance * 1.00000
                / scrollingLen)).intValue();*/
        int duration = (int)(1000f*distance/mScrollSpeed);
        setVisibility(VISIBLE);
        mSlr.startScroll(mXPaused, 0, distance, 0, duration);
        invalidate();
        mCount++;
        if(mCount>1) {
            mPaused = true;
        }else{
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
        int mt = (int)tp.measureText(strTxt);
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