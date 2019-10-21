package jp.pioneer.carsync.domain.event;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.Notification;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * ユーザーが選択した通知読み上げ対象アプリの通知が削除された際に発生するイベント.
 */
public class ReadNotificationRemovedEvent {
    /** 削除された通知. */
    @NonNull public final Notification notification;

    /**
     * コンストラクタ.
     *
     * @param notification 削除された通知
     * @throws NullPointerException {@code notification}がnull
     */
    public ReadNotificationRemovedEvent(@NonNull Notification notification) {
        this.notification = checkNotNull(notification);
    }
}
