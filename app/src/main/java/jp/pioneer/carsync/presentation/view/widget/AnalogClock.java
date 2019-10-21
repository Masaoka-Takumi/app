package jp.pioneer.carsync.presentation.view.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * アナログ時計
 * <p>
 * android.widget.AnalogClockのdeprecated箇所を修正し、99Dream向けにカスタムしたもの
 * ※当Viewは縦横比1:1でないと表示崩れがあるため注意。
 */
@RemoteViews.RemoteView
public class AnalogClock extends View {
    private final Handler mHandler = new Handler();

    private Calendar mCalendar;
    private Paint mPaint = new Paint();
    private RectF mRectHour, mRectMin, mRectSec;

    private int mWidth;
    private int mHeight;
    private int mColor = Color.argb(255, 0, 0, 0);

    private boolean mAttached = false;
    private boolean mTickerStopped = false;
    private boolean mChanged = false;

    private float mHour;
    private float mMinute;
    private float mSecond;

    /**
     * Constructor
     *
     * @param context Context
     */
    public AnalogClock(Context context) {
        super(context);
    }

    /**
     * Constructor
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public AnalogClock(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public void setColor(int color) {
        mColor = ContextCompat.getColor(getContext(), color);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);

            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
        }

        mCalendar = new GregorianCalendar();
        mTickerStopped = false;
        mTicker.run();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        float hScale = 1.0f;
        float vScale = 1.0f;

        if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mWidth) {
            hScale = (float) widthSize / (float) mWidth;
        }

        if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mHeight) {
            vScale = (float) heightSize / (float) mHeight;
        }

        float scale = Math.min(hScale, vScale);

        setMeasuredDimension(resolveSizeAndState((int) (mWidth * scale), widthMeasureSpec, 0),
                resolveSizeAndState((int) (mHeight * scale), heightMeasureSpec, 0));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChanged = true;

        mWidth = w;
        mHeight = h;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        boolean changed = mChanged;
        if (changed) {
            mChanged = false;
        }

        int x = mWidth / 2;
        int y = mHeight / 2;
        float r = x < y ? x : y;
        float pr = 8.0f;

        mPaint.setAntiAlias(true);

        // Dial
        mPaint.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawCircle(x, y, r, mPaint);
        canvas.save();

        // Hands (Hour, Minute, Second)
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.argb(255, 255, 255, 255));

        canvas.rotate(mHour / 12 * 360 + mMinute / 60 * 360 / 12, x, y);
        if (mRectHour == null) {
            mRectHour = new RectF(r - 5.0f, r * (1.0f - 0.6f), r + 5.0f, r + pr * 0.5f);
        }
        canvas.drawRoundRect(mRectHour, 30, 30, mPaint);
        canvas.restore();
        canvas.save();

        canvas.rotate(mMinute / 60 * 360, x, y);
        if (mRectMin == null) {
            mRectMin = new RectF(r - 5.0f, r * (1.0f - 0.8f), r + 5.0f, r + pr * 0.5f);
        }
        canvas.drawRoundRect(mRectMin, 30, 30, mPaint);
        canvas.restore();
        canvas.save();

        mPaint.setColor(mColor);

        canvas.rotate(mSecond / 60 * 360, x, y);
        if (mRectSec == null) {
            mRectSec = new RectF(r - 2.0f, r * (1.0f - 0.9f), r + 2.0f, r + pr * 0.5f + 20.0f);
        }
        canvas.drawRoundRect(mRectSec, 30, 30, mPaint);
        canvas.restore();
        canvas.save();

        // Center point
        canvas.drawCircle(x, y, pr, mPaint);
        canvas.restore();
    }

    private void updateContentDescription(Calendar calendar) {
        final int flags = DateUtils.FORMAT_SHOW_TIME;
        String contentDescription = DateUtils.formatDateTime(getContext(), calendar.getTimeInMillis(), flags);
        setContentDescription(contentDescription);
    }

    /**
     * ブロードキャスト受信処理
     * <p>
     * タイムゾーンの変更を検知する。
     */
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Intent.ACTION_TIMEZONE_CHANGED)) {
                String tz = intent.getStringExtra("time-zone");
                mCalendar = new GregorianCalendar();
                mCalendar.setTimeZone(TimeZone.getTimeZone(tz));
            }
        }
    };

    /**
     * 時刻の計算
     * <p>
     * Intent.ACTION_TIME_TICKでは1sec単位の経過を図れないため、
     * Handlerにて時刻の管理を行う。
     */
    private final Runnable mTicker = new Runnable() {
        public void run() {
            if (mTickerStopped) {
                return;
            }

            mCalendar.setTimeInMillis(System.currentTimeMillis());
            mHour = mCalendar.get(Calendar.HOUR_OF_DAY) % 12;
            mMinute = mCalendar.get(Calendar.MINUTE);
            mSecond = mCalendar.get(Calendar.SECOND);

            mChanged = true;

            updateContentDescription(mCalendar);

            invalidate();
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            mHandler.postAtTime(mTicker, next);
        }
    };
}
