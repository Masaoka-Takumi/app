package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

import timber.log.Timber;

/**
 * Created by NSW00_008316 on 2017/03/29.
 */

public class SwipeContainerView extends RelativeLayout {

    private GestureDetector mDetector;
    private OnSwipeChangeListener mListener;

    public SwipeContainerView(Context context) {
        super(context, null);
    }

    public SwipeContainerView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                Timber.d("onFling in SwipeContainerView");
                if (mListener != null && Math.abs(velocityX) > 2000) {
                    if (e1.getX() < e2.getX()) {
                        mListener.onRightFling();
                    } else if (e1.getX() > e2.getX()) {
                        mListener.onLeftFling();
                    }
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mDetector != null && mDetector.onTouchEvent(ev)) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    public void setOnSwipeChangeListener(OnSwipeChangeListener listener) {
        mListener = listener;
    }

    public interface OnSwipeChangeListener {

        void onLeftFling();

        void onRightFling();
    }
}
