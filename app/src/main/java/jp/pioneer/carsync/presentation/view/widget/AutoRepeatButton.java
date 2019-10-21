package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import jp.pioneer.carsync.R;

import timber.log.Timber;

/**
 * Created by nakano on 6/21/16.
 * AutoRepeat + CustomButton
 * 参照：com.pioneer.alternativeremote.view.CustomButton
 * ★注意: outside の判断は、LayoutやAndroid Versionによって、誤判の可能性があり、
 * 　　それに STATE_UP_OUT、STATE_MOV_OUT を送出し、処理は外に任せる
 */
public class AutoRepeatButton extends AppCompatButton {
    public static final int ACTION_ID_OPEN = 1;
    public static final int ACTION_ID_CLOSE = 0;

    public static final int STATE_IDLE = 0;    //　待機
    public static final int STATE_OPENING = 0x01; // ACTION_DOWN
    public static final int STATE_ACTIVE = 0x02; // 長押し中 (postDelayed した)
    public static final int STATE_DONE = 0x04; // ACTION_UP inside
    public static final int STATE_UP_OUT = 0x68; // ACTION_UP outside ★★★ 誤判注意
    public static final int STATE_MOV_OUT = 0x80; // ACTION_MOVE outside ★★★ 誤判注意
    public static final int STATE_CANCEL = 0x08; // ACTION_CANCEL 等

    private static boolean DEFAULT_DEBUG_MODE = false;
    private static final int DEFAULT_INITIAL_DELAY = 200;
    private static final int DEFAULT_REPEAT_INTERVAL = 200;

    private boolean debugMode = DEFAULT_DEBUG_MODE;
    private int initialRepeatDelay = DEFAULT_INITIAL_DELAY;
    private int repeatIntervalInMilliseconds = DEFAULT_REPEAT_INTERVAL;

    private int _myState = STATE_IDLE;

    private int callbackCount = 0;  // debugログ出力制御のため

    private MyNotifyListener myNotifyListener;

    public AutoRepeatButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
        commonConstructorCode();
    }

    public AutoRepeatButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
        commonConstructorCode();
    }

    public AutoRepeatButton(Context context) {
        super(context);
        commonConstructorCode();
    }

    private void init(Context context, AttributeSet attrs) {
        if (debugMode) Timber.d("AutoRepeatButton init");

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.AutoRepeatButton);
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);

            if (attr == R.styleable.AutoRepeatButton_initial_delay) {
                initialRepeatDelay = a.getInt(attr, AutoRepeatButton.DEFAULT_INITIAL_DELAY);
            } else if (attr == R.styleable.AutoRepeatButton_repeat_interval) {
                repeatIntervalInMilliseconds = a.getInt(attr, DEFAULT_REPEAT_INTERVAL);
            } else if (attr == R.styleable.AutoRepeatButton_debug_mode) {
                debugMode = a.getBoolean(attr, DEFAULT_DEBUG_MODE);
            }
        }
        a.recycle();
    }

    private int getMyState() {
        return _myState;
    }

    private void setMyState(int state) {
        int oldState = this._myState;
        this._myState = state;

        if ((oldState == STATE_OPENING) && (state == STATE_CANCEL)) {
            if (debugMode) Timber.d("AutoRepeatButton myState Changed new = STATE_CANCEL  old = STATE_OPENING");
        }

        if (oldState != state) {
            if (debugMode) Timber.d("AutoRepeatButton myState Changed new = %xh  old = %xh", state, oldState);
            if (myNotifyListener != null) {
                myNotifyListener.onTouchStateChanged(this, state, oldState);
            }
        }
    }

    private Runnable repeatClickWhileButtonHeldRunnable = new Runnable() {
        @Override
        public void run() {
            if (debugMode) Timber.d("AutoRepeatButton run");
            if (debugMode) Timber.d("  AutoRepeatButton performLongClick in run");
            callbackCount--;
            performLongClick();

            //Schedule the next repetitions of the click action, using a faster repeat
            // interval than the initial repeat delay interval.
            postDelayed(repeatClickWhileButtonHeldRunnable, repeatIntervalInMilliseconds);
            setMyState(STATE_ACTIVE);
            callbackCount++;
        }
    };

    private void commonConstructorCode() {
        if (debugMode) Timber.d("AutoRepeatButton commonConstructorCode");

        this.myNotifyListener = null;
        setMyState(STATE_IDLE);

        this.setOnTouchListener(new OnTouchListener() {
            private Rect rect;    // Variable rect to hold the bounds of the view

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (debugMode) Timber.d("AutoRepeatButton onTouch ACTION_DOWN");

                    // Construct a rect of the view's bounds
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());

                    // Just to be sure that we removed all callbacks,
                    //  which should have occurred in the ACTION_UP
                    cancel(false, STATE_IDLE);

                    if (debugMode) Timber.d("  AutoRepeatButton 1st performLongClick");
                    setMyState(STATE_OPENING);
                    performLongClick();

                    // Schedule the start of repetitions after a one half second delay.
                    postDelayed(repeatClickWhileButtonHeldRunnable, initialRepeatDelay);
                    setMyState(STATE_ACTIVE);
                    callbackCount++;
                } else if (action == MotionEvent.ACTION_UP) {
                    v.getHitRect(rect);
                    if (rect.contains(
                            Math.round(v.getX() + event.getX()),
                            Math.round(v.getY() + event.getY()))) {
                        if (debugMode) Timber.d("AutoRepeatButton onTouch ACTION_UP inside");
                        // inside
                        cancel(false, STATE_IDLE);

                        //Perform the present repetition of the click action provided by the user
                        // in setOnClickListener().
                        performClick();
                        setMyState(STATE_DONE);
                    } else {
                        if (debugMode) Timber.d("AutoRepeatButton onTouch ACTION_UP outside");
                        // outside?
                        setMyState(STATE_UP_OUT);   // ★★★ 誤判注意
                        cancel(true, STATE_IDLE);
                    }
                } else if (action == MotionEvent.ACTION_CANCEL) {
                    if (debugMode) Timber.d("AutoRepeatButton onTouch ACTION_CANCEL");
                    cancel(true, STATE_CANCEL);
                } else if (action == MotionEvent.ACTION_MOVE) {
                    v.getHitRect(rect);
                    if (rect.contains(
                            Math.round(v.getX() + event.getX()),
                            Math.round(v.getY() + event.getY()))) {
                        if (debugMode) Timber.d("AutoRepeatButton onTouch ACTION_MOVE inside");
                        // inside
                    } else {
                        if (debugMode) Timber.d("AutoRepeatButton onTouch ACTION_MOVE outside");
                        // outside?
                        setMyState(STATE_MOV_OUT);  // ★★★ 誤判注意
                        cancel(true, STATE_IDLE);
                    }
                } else if (action == MotionEvent.ACTION_OUTSIDE) {
                    if (debugMode) Timber.d("AutoRepeatButton onTouch ACTION_OUTSIDE");
                    // outside
                    cancel(true, STATE_CANCEL);
                }

                // Returning true here prevents performClick() from getting called
                //  in the usual manner, which would be redundant, given that we are
                //  already calling it above.
                return true;
            }

        });
    }

    private void cancel(boolean shouldSetMyState, int state) {
        if (callbackCount != 0) {
            if (debugMode) Timber.d("AutoRepeatButton cancel callbackCount=%d", callbackCount);
        }
        removeCallbacks(repeatClickWhileButtonHeldRunnable);
        callbackCount = 0;

        if (shouldSetMyState) {
            setMyState(state);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            if (callbackCount != 0) {
                if (debugMode) Timber.d("AutoRepeatButton setEnabled false callbackCount = %d", callbackCount);
            }
            cancel(true, STATE_CANCEL);
        }

        super.setEnabled(enabled);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
//        if(debugMode) Timber.d("AutoRepeatButton onAttachedToWindow");
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        if(debugMode) Timber.d("AutoRepeatButton onDetachedFromWindow");
    }

    /**
     * 参照：com.pioneer.alternativeremote.view.CustomButton
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        // state_pressedのdrawableがちゃんと表示されるように修正
        // @see https://code.google.com/p/android/issues/detail?id=172067#c21
        invalidate();
    }

    public void setMyNotifyListener(MyNotifyListener listener) {
        this.myNotifyListener = listener;
    }

    /**
     * This interface defines the type of state messages I want to communicate to my owner
     */
    public interface MyNotifyListener {
        void onTouchStateChanged(AutoRepeatButton arButton, int newState, int lastState);
    }

}
