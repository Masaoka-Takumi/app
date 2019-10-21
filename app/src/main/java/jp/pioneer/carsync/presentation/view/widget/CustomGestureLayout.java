package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.annimon.stream.Optional;

/**
 * ジェスチャーView
 * <p>
 * 再生画面でのジェスチャー機能を制御する。
 */
public class CustomGestureLayout extends LinearLayout implements View.OnTouchListener {
    // スワイプに必要な移動距離
    private static final float SWIPE_THRESHOLD = 50.0f;

    private GestureDetector mLongPressDetector, mSwipeDetector;
    private boolean mIsSeek = false;
    private OnGestureListener mListener;

    /**
     * Constructor
     *
     * @param context Context
     */
    public CustomGestureLayout(Context context) {
        super(context, null);
    }

    /**
     * Constructor
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public CustomGestureLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mLongPressDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                mIsSeek = true;
                Optional.ofNullable(mListener).ifPresent(listener -> listener.onStartSeek(e));
            }
        });
        mSwipeDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();

                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // 縦
                    if (e1.getX() - e2.getX() < -SWIPE_THRESHOLD) {         // 右へフリック
                        Optional.ofNullable(mListener).ifPresent(OnGestureListener::onSwipeRight);
                    } else if (e1.getX() - e2.getX() > SWIPE_THRESHOLD) {   // 左へフリック
                        Optional.ofNullable(mListener).ifPresent(OnGestureListener::onSwipeLeft);
                    }
                } else {
                    // 横
                    if (e1.getY() - e2.getY() < -SWIPE_THRESHOLD) {  // 下へフリック
                        Optional.ofNullable(mListener).ifPresent(OnGestureListener::onSwipeDown);
                    } else if (e1.getY() - e2.getY() > SWIPE_THRESHOLD) {   // 上へフリック
                        Optional.ofNullable(mListener).ifPresent(OnGestureListener::onSwipeUp);
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mSwipeDetector != null && mSwipeDetector.onTouchEvent(ev)) {
            return true;
        }
        if (mLongPressDetector != null && mLongPressDetector.onTouchEvent(ev)) {
            return true;
        }
        if (mIsSeek) {
            return this.onTouch(this, ev);
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                Optional.ofNullable(mListener).ifPresent(listener -> listener.onSeek(event));
                return false;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsSeek = false;
                Optional.ofNullable(mListener).ifPresent(OnGestureListener::onEndSeek);
                return true;
        }
        return false;
    }

    /**
     * 通知先設定
     *
     * @param listener OnGestureListener
     */
    public void setOnSeekGestureListener(OnGestureListener listener) {
        mListener = listener;
    }

    /**
     * 通知先削除
     */
    public void removeOnSeekGestureListener() {
        mListener = null;
    }

    /**
     * ジェスチャー機能通知リスナー
     */
    public interface OnGestureListener {
        /**
         * シーク機能開始通知
         */
        void onStartSeek(MotionEvent ev);

        /**
         * シーク機能経過通知
         *
         * @param ev MotionEvent
         */
        void onSeek(MotionEvent ev);

        /**
         * シーク機能終了通知
         */
        void onEndSeek();

        /**
         * 上スワイプ機能通知
         * <p>
         * ※タッチ位置から下方向にスワイプした場合
         */
        void onSwipeUp();

        /**
         * 下スワイプ機能通知
         * <p>
         * ※タッチ位置から上方向にスワイプした場合
         */
        void onSwipeDown();

        /**
         * 左スワイプ機能通知
         * <p>
         * ※タッチ位置から右方向にスワイプした場合
         */
        void onSwipeLeft();

        /**
         * 右スワイプ機能通知
         * <p>
         * ※タッチ位置から左方向にスワイプした場合
         */
        void onSwipeRight();
    }
}