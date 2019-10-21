package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import static android.app.Notification.EXTRA_TEXT;

/**
 * Hangoutsの通知.
 */
public class HangoutsNotification extends DefaultNotificationImpl {
    /**
     * コンストラクタ
     */
    @Inject
    public HangoutsNotification() {
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getText() {
        /*
         * getStatusBarNotification().getNotification().extras.getString(android.app.Notification.EXTRA_TEXT);
         * 上記での本文取得を行った場合、Mapの中身がStringではなくSpannableStringのため、Nullになってしまう
         */
        Object obj = getNotification().extras.get(EXTRA_TEXT);
        if (obj != null) {
            return obj.toString();
        } else {
            return "";
        }
    }
}
