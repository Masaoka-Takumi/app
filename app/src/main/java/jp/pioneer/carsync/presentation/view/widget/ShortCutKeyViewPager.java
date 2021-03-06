package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.annimon.stream.Optional;

public class ShortCutKeyViewPager extends ViewPager{
    // スワイプに必要な移動距離
    private static final float SWIPE_THRESHOLD = 50.0f;
    private float initialXValue;
    private boolean directionRightOnly;
    private boolean directionLeftOnly;
    private boolean isPagingEnabled = false;
    private GestureDetector  mSwipeDetector;
    private ShortCutKeyViewPager.OnGestureListener mListener;
    public ShortCutKeyViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);

        mSwipeDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                //Timber.d("onDown:action = " + e.getAction());
                return true;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                //Timber.d("onScroll:action = " + e1.getAction());
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                //Timber.d("onFling:diffX = " + diffX + ",diffY = " + diffY);
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    // 縦
                    if (e1.getX() - e2.getX() < -SWIPE_THRESHOLD) {         // 右へフリック
                        Optional.ofNullable(mListener).ifPresent(ShortCutKeyViewPager.OnGestureListener::onSwipeRight);
                    } else if (e1.getX() - e2.getX() > SWIPE_THRESHOLD) {   // 左へフリック
                        Optional.ofNullable(mListener).ifPresent(ShortCutKeyViewPager.OnGestureListener::onSwipeLeft);
                    }
                }
                return true;
            }
        });
        mSwipeDetector.setIsLongpressEnabled(false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // ここでtrueを返すとイベントはここで終了
        // ここでfalseを返すと子ViewのonClickやらonLongClickやら
        //Timber.d("onTouchEvent:action = " + event.getAction());
        if (!this.IsSwipeAllowed(event)) return false;

/*        if (mSwipeDetector != null && mSwipeDetector.onTouchEvent(event)) {
            return true;
        }*/
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        //Timber.d("onInterceptTouchEvent:action = " + event.getAction());
        // タッチされたらまずonInterceptTouchEventが呼ばれる
        // ここでtrueを返せば親ViewのonTouchEvent
        // ここでfalseを返せば子ViewのonClickやらonLongClickやら
        if (!this.IsSwipeAllowed(event)) return false;

/*        if (event.getAction() == MotionEvent.ACTION_DOWN ||event.getAction() == MotionEvent.ACTION_CANCEL || event.getAction() == MotionEvent.ACTION_UP) {
            mSwipeDetector.onTouchEvent(event);
            return false;
        }
       if (mSwipeDetector != null && mSwipeDetector.onTouchEvent(event)) {
            return true;
        }*/
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

    public void setPagingEnabled(boolean b) {
        this.isPagingEnabled = b;
    }

    private boolean IsSwipeAllowed(MotionEvent event) {
        if (!this.directionRightOnly&&!this.directionLeftOnly) return true;

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            initialXValue = event.getX();
            return true;
        }

        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            try {
                float diffX = event.getX() - initialXValue;
                if (diffX > 0 && directionRightOnly) {
                    // swipe from left to right detected
                    return false;
                }
                if (diffX < 0 && directionLeftOnly) {
                    // swipe from right to left detected
                    return false;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return true;
    }

    public void setAllowedSwipeRightOnly(boolean directionRightOnly) {
        this.directionRightOnly = directionRightOnly;
    }

    public void setAllowedSwipeLeftOnly(boolean directionLeftOnly) {
        this.directionLeftOnly = directionLeftOnly;
    }
    /**
     * 通知先設定
     *
     * @param listener OnGestureListener
     */
    public void setOnGestureListener(ShortCutKeyViewPager.OnGestureListener listener) {
        mListener = listener;
    }

    /**
     * 通知先削除
     */
    public void removeOnGestureListener() {
        mListener = null;
    }

    /**
     * ジェスチャー機能通知リスナー
     */
    public interface OnGestureListener {

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