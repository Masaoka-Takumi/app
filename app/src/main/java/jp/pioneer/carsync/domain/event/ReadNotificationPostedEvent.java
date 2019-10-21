package jp.pioneer.carsync.domain.event;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.Notification;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ユーザーが選択した通知読み上げ対象アプリの通知が投稿された際に発生するイベント.
 */
public class ReadNotificationPostedEvent {
    /** 投稿された通知. */
    @NonNull public final Notification notification;

    /**
     * コンストラクタ.
     *
     * @param notification 投稿された通知
     * @throws NullPointerException {@code notification}がnull
     */
    public ReadNotificationPostedEvent(@NonNull Notification notification) {
        this.notification = checkNotNull(notification);
    }
}
