package jp.pioneer.carsync.domain.component;

import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Comparator;

/**
 * 通知プロバイダー.
 */
public interface NotificationProvider {
    /**
     * 通知へのアクセスが許可されているか否か取得
     *
     * @return {@code true}:許可されている。{@code false}:許可されていない。
     */
    boolean isGrantReadNotification();

    /**
     * 通知一覧取得
     *
     * @param comparator コンパレータ。通知一覧の並びを決定する。nullを指定した場合、OSから取得した並びとなる。
     * @return 通知一覧。一個もない場合、空配列。
     */
    @NonNull
    StatusBarNotification[] getStatusBarNotifications(@Nullable Comparator<StatusBarNotification> comparator);

    /**
     * 接続時に呼ばれるリスナー設定.
     * <p>
     * 設定可能なリスナーは1個。後勝ち。
     * 弱参照で保持するので、利用者はリスナーを強参照で保持すること。
     *
     * @param listener リスナー
     */
    void setOnConnectedListener(@Nullable OnConnectedListener listener);

    /**
     * 通知投稿時に呼ばれるリスナー設定.
     * <p>
     * 設定可能なリスナーは1個。後勝ち。
     * 弱参照で保持するので、利用者はリスナーを強参照で保持すること。
     *
     * @param listener リスナー
     */
    void setOnPostedListener(@Nullable OnPostedListener listener);

    /**
     * 通知削除時に呼ばれるリスナー設定.
     * <p>
     * 設定可能なリスナーは1個。後勝ち。
     * 弱参照で保持するので、利用者はリスナーを強参照で保持すること。
     *
     * @param listener リスナー
     */
    void setOnRemovedListener(@Nullable OnRemovedListener listener);

    /**
     * 接続時に呼ばれるリスナー.
     *
     * @see #setOnConnectedListener(OnConnectedListener)
     */
    interface OnConnectedListener {
        /**
         * 接続ハンドラ.
         */
        void onConnected();
    }

    /**
     * 通知投稿時に呼ばれるリスナー.
     *
     * @see #setOnPostedListener(OnPostedListener)
     */
    interface OnPostedListener {
        /**
         * 通知投稿ハンドラ.
         *
         * @param sbn 通知内容
         * @throws NullPointerException {@code sbn}がnull
         */
        void onPosted(@NonNull StatusBarNotification sbn);
    }

    /**
     * 通知削除時に呼ばれるリスナー.
     *
     * @see #setOnRemovedListener(OnRemovedListener)
     */
    interface OnRemovedListener {
        /**
         * 通知削除ハンドラ.
         *
         * @param sbn 通知内容
         * @throws NullPointerException {@code sbn}がnull
         */
        void onRemoved(@NonNull StatusBarNotification sbn);
    }
}
