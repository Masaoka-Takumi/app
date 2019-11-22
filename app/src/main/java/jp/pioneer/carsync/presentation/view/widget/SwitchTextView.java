package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;

import java.util.ArrayList;

import timber.log.Timber;

public class SwitchTextView extends AppCompatTextView {
    private static final int DISPLAY_TIME = 4000;
    private static final int BLANK_TIME = 500;


    private int mGravity = 0;
    private Handler mHandler = null;
    private ArrayList<String> mStringArrayList = new ArrayList<>();
    private int mDisplayCount = 0;
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
            displayText();
        }
    };

    private void displayText(){
        mHandler.removeCallbacks(mRunnableBlankText);
        mHandler.removeCallbacks(mRunnableNextText);
        if(mStringArrayList.size()>0) {
            setText(mStringArrayList.get(mDisplayCount % (mStringArrayList.size())));
            int textLen = calculateTextLen();
            if (getWidth() == 0) return;
            if (textLen <= getWidth()) {
                this.setGravity(mGravity);
            }else{
                this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                this.setEllipsize(TextUtils.TruncateAt.END);
            }
        }else{
            setText("");
        }
        invalidate();
        mDisplayCount++;
        if(mStringArrayList.size()>=2) {
            mHandler.postDelayed(mRunnableBlankText, DISPLAY_TIME);
        }
    }
    /*
     * constructor
     */
    public SwitchTextView(Context context) {
        this(context, null);
    }

    /*
     * constructor
     */
    public SwitchTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    /*
     * constructor
     */
    public SwitchTextView(Context context, AttributeSet attrs, int defStyle) {
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
        displayText();
    }

    public void restartDisplay(ArrayList<String> stringArrayList) {
        Timber.d("restartDisplay");
        mStringArrayList = stringArrayList;
        if (mStringArrayList.size() >= 2) {
            setText("");
            mHandler.postDelayed(mRunnableNextText, BLANK_TIME);
        }else if (mStringArrayList.size()==1){
            displayText();
        }
    }

    public void setSingleText(CharSequence text) {
        mHandler.removeCallbacks(mRunnableBlankText);
        mHandler.removeCallbacks(mRunnableNextText);
        setText(text);
        int textLen = calculateTextLen();
        if (textLen <= getWidth()) {
            this.setGravity(mGravity);
        }else{
            this.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        }
        invalidate();
    }

    private void init() {
        mHandler = new Handler(Looper.getMainLooper());
        setSingleLine();
        setEllipsize(null);
        //setVisibility(INVISIBLE);
        mGravity = this.getGravity();
        setHorizontallyScrolling(true);

    }

    @Override
    protected void onDetachedFromWindow() {
        mHandler.removeCallbacks(mRunnableBlankText);
        mHandler.removeCallbacks(mRunnableNextText);
        super.onDetachedFromWindow();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        //BackGroundからの復帰で再レイアウトされるため

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

}