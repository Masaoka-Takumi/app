package jp.pioneer.carsync.domain.interactor;

import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.NotificationProvider;
import jp.pioneer.carsync.domain.model.Notification;
import jp.pioneer.carsync.domain.model.NotificationFactory;

/**
 * 通知読み上げ一覧取得.
 * <p>
 * ユーザーが選択した通知読み上げアプリ（{@link AppSharedPreference#getReadNotificationApps()}）の
 * 読み上げ対象の通知一覧を取得する。
 * 通知一覧は投稿時刻（{@link StatusBarNotification#getPostTime()}）で降順ソートされている。
 */
public class GetReadNotificationList {
    @Inject NotificationProvider mInteractor;
    @Inject NotificationFactory mFactory;
    @Inject AppSharedPreference mPreference;

    /**
     * コンストラクタ.
     */
    @Inject
    public GetReadNotificationList() {
    }

    /**
     * 実行.
     *
     * @return 通知リスト
     */
    @NonNull
    public List<Notification> execute() {
        List<Notification> result = new ArrayList<>();
        if (!mPreference.isReadNotificationEnabled()) {
            return result;
        }

        StatusBarNotification[] sbns = mInteractor.getStatusBarNotifications((lhs, rhs) ->
            (int) (rhs.getPostTime() - lhs.getPostTime())
        );

        return Stream.of(sbns)
                .map(sbn -> mFactory.create(sbn))
                .filter(sbn -> !(sbn == null))
                .collect(Collectors.toList());
    }
}
