package jp.pioneer.carsync.infrastructure.component;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

/**
 * 通知リスナー.
 * <p>
 * {@link NotificationInteractor}と{@link NotificationListenerService}間のインターフェース。
 */
public interface NotificationListener {
    /**
     * {@link NotificationListenerService}のアタッチ.
     * <p>
     * {@link NotificationListenerService#onListenerConnected()}時に呼ぶこと。
     *
     * @param service 通知リスナーサービス.
     * @throws NullPointerException {@code service}がnull
     */
    void attachNotificationListenerService(@NonNull NotificationListenerService service);

    /**
     * {@link NotificationListenerService}のデタッチ.
     * <p>
     * {@link NotificationListenerService#onListenerDisconnected()}時に呼ぶこと。
     *
     */
    void detachNotificationListenerService();

    /**
     * 通知投稿ハンドラ.
     * <p>
     * {@link NotificationListenerService#onNotificationPosted(StatusBarNotification)}時に呼ぶこと。
     *
     * @param sbn 通知
     * @throws NullPointerException {@code sbn}がnull
     */
    void onNotificationPosted(@NonNull StatusBarNotification sbn);

    /**
     * 通知削除ハンドラ.
     * <p>
     * {@link NotificationListenerService#onNotificationRemoved(StatusBarNotification)} 時に呼ぶこと。
     *
     * @param sbn 通知
     * @throws NullPointerException {@code sbn}がnull
     */
    void onNotificationRemoved(@NonNull StatusBarNotification sbn);
}
