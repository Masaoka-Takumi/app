package jp.pioneer.carsync.presentation.controller;

import android.content.Context;
import android.view.Gravity;
import android.view.View;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.presentation.view.widget.MessagingToast;

/**
 * 楽曲切り替わり通知Toast表示を管理するクラス
 */

public class SongChangeToastController implements MessagingToast.OnDetachedToastListener{
    @Inject Context mContext;
    private OnToastHiddenListener mListener;
    private MessagingToast mToast;

    /**
     * コンストラクタ
     */
    @Inject
    public SongChangeToastController() {
    }

    /**
     * {@link SongChangeToastController.OnToastHiddenListener}の設定
     *
     * @param listener OnToastHiddenListener
     */
    public void setOnToastHiddenListener(OnToastHiddenListener listener) {
        mListener = listener;
    }

    /**
     * Toastの表示
     *
     * @param mediaInfo 表示する通知内容
     * @param view         表示する通知のレイアウト
     */
    public void show(AndroidMusicMediaInfo mediaInfo, View view) {
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = new MessagingToast(mContext);
        mToast.setOnDetachedToastListener(this);
        mToast.setTag(mediaInfo);
        mToast.show(view, Gravity.TOP | Gravity.FILL_HORIZONTAL);
    }

    @Override
    public void onDetachedToast(MessagingToast toast) {
        if (mToast == toast) {
            mToast = null;
        }

        if (mListener != null) {
            mListener.onToastHidden((AndroidMusicMediaInfo) toast.getTag());
        }
    }

    /**
     * 通知の非表示
     */
    public void hideNotification() {
        if (mToast != null) {
            mToast.cancel();
        }
    }

    /**
     * Toastが非表示になったことを通知するリスナー
     */
    public interface OnToastHiddenListener {
        /**
         * 非表示通知
         *
         * @param mediaInfo 表示していた通知内容
         */
        void onToastHidden(AndroidMusicMediaInfo mediaInfo);
    }
}
