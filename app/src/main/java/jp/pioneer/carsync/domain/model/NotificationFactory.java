package jp.pioneer.carsync.domain.model;

import com.annimon.stream.Stream;

import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import jp.pioneer.carsync.application.content.AppSharedPreference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 通知ファクトリ.
 */
public class NotificationFactory {
    @Inject AppSharedPreference mPreference;
    @Inject Map<MessagingApp, Provider<? extends AbstractNotification>> mNotificationProviders;

    /**
     * コンストラクタ
     */
    @Inject
    public NotificationFactory() {
    }

    /**
     * 通知情報生成.
     * <p>
     * ステータスバーの通知情報から {@link Notification} を生成する.
     *
     * @param sbn ステータスバーの通知情報
     * @return 通知情報 {@link Notification} . 通知対象のアプリケーションではない場合はnull.
     * @throws NullPointerException {@code sbn} がnull
     */
    @Nullable
    public Notification create(@NonNull StatusBarNotification sbn) {
        checkNotNull(sbn);

        MessagingApp messagingApp = MessagingApp.fromPackageNameNoThrow(sbn.getPackageName());
        if (messagingApp == null) {
            return null;
        }

        if (!createReadMessageApp().contains(sbn.getPackageName())) {
            return null;
        }

        Notification notification = mNotificationProviders.get(messagingApp).get().setStatusBarNotification(sbn);
        if (!notification.isReadTarget()) {
            return null;
        }
        return notification;
    }

    private List<String> createReadMessageApp() {
        return Stream.of(mPreference.getReadNotificationApps())
                .map(app -> app.packageName)
                .toList();
    }
}
