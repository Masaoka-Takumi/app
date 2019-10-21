package jp.pioneer.carsync.domain.internal;

import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;

import com.annimon.stream.Optional;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.NotificationProvider;
import jp.pioneer.carsync.domain.event.NotificationListenerServiceConnectedEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationPostedEvent;
import jp.pioneer.carsync.domain.event.ReadNotificationRemovedEvent;
import jp.pioneer.carsync.domain.model.NotificationFactory;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 通知読み上げリスナー.
 * <p>
 * ユーザーが選択した通知読み上げ対象アプリの通知が投稿、または、削除された場合、イベントをポストする。
 *
 * @see ReadNotificationPostedEvent
 * @see ReadNotificationRemovedEvent
 */
public class ReadNotificationListener implements NotificationProvider.OnPostedListener, NotificationProvider.OnRemovedListener, NotificationProvider.OnConnectedListener {
    @Inject EventBus mEventBus;
    @Inject NotificationProvider mProvider;
    @Inject NotificationFactory mFactory;
    @Inject AppSharedPreference mPreference;

    /**
     * コンストラクタ
     */
    @Inject
    public ReadNotificationListener() {
    }

    /**
     * 初期化
     */
    public void initialize() {
        mProvider.setOnConnectedListener(this);
        mProvider.setOnPostedListener(this);
        mProvider.setOnRemovedListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onConnected() {
        mEventBus.post(new NotificationListenerServiceConnectedEvent());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPosted(@NonNull StatusBarNotification sbn) {
        checkNotNull(sbn);

        if (!mPreference.isReadNotificationEnabled()) {
            return;
        }

        Optional.ofNullable(mFactory.create(sbn))
                .ifPresent(notification -> mEventBus.post(new ReadNotificationPostedEvent(notification)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRemoved(@NonNull StatusBarNotification sbn) {
        checkNotNull(sbn);

        if (!mPreference.isReadNotificationEnabled()) {
            return;
        }

        Optional.ofNullable(mFactory.create(sbn))
                .ifPresent(notification -> mEventBus.post(new ReadNotificationRemovedEvent(notification)));
    }
}
