package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;

import javax.inject.Inject;

import static android.app.Notification.EXTRA_TEXT;

/**
 * Viberの通知.
 */
public class ViberNotification extends DefaultNotificationImpl {
    /**
     * コンストラクタ
     */
    @Inject
    public ViberNotification() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isReadTarget() {
        if ("missed_call".equals(getStatusBarNotification().getTag())) {
            /*
             * 通知情報TAGが"missed_call"である場合、これを除外する。
             */
            return false;
        }

        return super.isReadTarget();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public String getText() {
        /*
         * getStatusBarNotification().getNotification().extras.getString(android.app.Notification.EXTRA_TEXT);
         * 上記での本文取得を行った場合、Mapの中身がStringで   はなくSpannableStringのため、Nullになってしまう
         */
        Object obj = getNotification().extras.get(EXTRA_TEXT);
        if (obj != null) {
            return obj.toString();
        } else {
            return "";
        }
    }
}
