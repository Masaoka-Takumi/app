package jp.pioneer.carsync.infrastructure.component;

import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import javax.inject.Inject;

import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.di.component.AppComponent;
import timber.log.Timber;

/**
 * NotificationListenerServiceの実装.
 * <p>
 * 通知へのアクセスをユーザーが許可することで、本サービスがNotificationManagerServiceより
 * バインドサービスで起動され、以降は端末起動時に起動されるようになる。拒否に変更すると、
 * アンバインドされサービスが停止する。
 * アプリ内から本サービスを起動したところで通知を取得することは出来ない。
 * システムから起動されるため、自身のインスタンスを{@link NotificationListener}に
 * 通知することにより上位と連携を取っている。
 */
public class NotificationListenerServiceImpl extends NotificationListenerService {
    @Inject NotificationListener mNotificationListener;

    /**
     * コンストラクタ.
     */
    public NotificationListenerServiceImpl() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.i("onCreate()");
        getAppComponent().inject(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Timber.i("onDestroy()");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onListenerConnected() {
        Timber.i("onListenerConnected()");
        mNotificationListener.attachNotificationListenerService(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onListenerDisconnected() {
        Timber.i("onListenerDisconnected()");
        mNotificationListener.detachNotificationListenerService();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Timber.i("onNotificationPosted() sbn = " + sbn);
        // 現実的にnullが渡されるとは思えないが念のため
        if (sbn != null) {
            mNotificationListener.onNotificationPosted(sbn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Timber.i("onNotificationRemoved() sbn = " + sbn);
        // 現実的にnullが渡されるとは思えないが念のため
        if (sbn != null) {
            mNotificationListener.onNotificationRemoved(sbn);
        }
    }

    private AppComponent getAppComponent() {
        return App.getApp(this).getAppComponent();
    }
}
