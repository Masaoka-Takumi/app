package jp.pioneer.carsync.domain.model;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import javax.inject.Inject;

import static android.app.Notification.EXTRA_TITLE;
import static android.app.Notification.EXTRA_TEXT;

/**
 * 標準的なNotificationの実装.
 * <p>
 * 多くの場合これで十分なNotificationの実装。
 */
public class DefaultNotificationImpl extends AbstractNotification {
    /**
     * コンストラクタ.
     */
    @Inject
    public DefaultNotificationImpl() {
    }

    /**
     * {@inheritDoc}
     */
    public boolean isReadTarget() {
        // 通知の種類を判断できるandroid.app.Notification#categoryはAPI Level21以降なので、
        // 他で区別するものがなければ読み上げ対象となる。
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public String getTitle() {
        String title = getNotification().extras.getString(EXTRA_TITLE);
        return TextUtils.isEmpty(title) ? "" : title;
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public String getText() {
        String text = getNotification().extras.getString(EXTRA_TEXT);
        return TextUtils.isEmpty(text) ? "" : text;
    }
}
