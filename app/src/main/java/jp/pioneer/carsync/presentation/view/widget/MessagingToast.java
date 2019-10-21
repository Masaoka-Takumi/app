package jp.pioneer.carsync.presentation.view.widget;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

/**
 * 読み上げ通知Toast
 */

public class MessagingToast extends Toast {

    private Context mContext = null;
    private OnDetachedToastListener mListener = null;
    private Object mTag;

    /**
     * コンストラクタ
     *
     * @param context Context
     */
    public MessagingToast(Context context) {
        super(context);
        mContext = context;
    }

    /**
     * {@link OnDetachedToastListener}の設定
     *
     * @param listener
     */
    public void setOnDetachedToastListener(OnDetachedToastListener listener) {
        mListener = listener;
    }

    /**
     * タグの設定
     *
     * @param tag {@link jp.pioneer.carsync.domain.model.Notification} 通知内容
     */
    public void setTag(Object tag) {
        mTag = tag;
    }

    /**
     * 設定したタグの取得
     *
     * @return {@link jp.pioneer.carsync.domain.model.Notification} 通知内容
     */
    public Object getTag() {
        return mTag;
    }

    /**
     * Toastの表示
     *
     * @param view 表示する通知のレイアウト
     * @param position 表示する位置
     */
    public void show(final View view,int position) {
        setGravity(position, 0, 0);
        setDuration(Toast.LENGTH_LONG);
        new FrameLayout(mContext) {
            {
                addView(view);
                setView(this);
            }

            @Override
            public void onDetachedFromWindow() {
                if (mListener != null) {
                    mListener.onDetachedToast(MessagingToast.this);
                }
                super.onDetachedFromWindow();
            }
        };
        super.show();
    }

    /**
     * Toastが非表示になったことを通知するリスナー
     */
    public interface OnDetachedToastListener {
        /**
         * 非表示通知
         *
         * @param toast 非表示になったToast
         */
        void onDetachedToast(MessagingToast toast);
    }
}
