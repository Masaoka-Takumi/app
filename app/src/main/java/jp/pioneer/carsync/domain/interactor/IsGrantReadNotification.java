package jp.pioneer.carsync.domain.interactor;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.NotificationProvider;

/**
 * 通知へのアクセスが許可されているか否か取得.
 * <p>
 * ユーザーがAndroidの設定で通知へのアクセスを許可しないと通知情報を取得出来ないので、
 * 通知へのアクセスが許可されていない場合、設定画面へ誘導すること。
 * 通知へのアクセス設定画面へは、以下のインテントで遷移出来る。
 * <pre>{@code
 *  Intent intent = new Intent();
 *  intent.setAction("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
 * }</pre>
 */
public class IsGrantReadNotification {
    @Inject NotificationProvider mNotificationProvider;

    /**
     * コンストラクタ.
     */
    @Inject
    public IsGrantReadNotification() {
    }

    /**
     * 実行.
     *
     * @return {@code true}:許可されている。{@code false}:許可されていない。
     */
    public boolean execute() {
        return mNotificationProvider.isGrantReadNotification();
    }
}
