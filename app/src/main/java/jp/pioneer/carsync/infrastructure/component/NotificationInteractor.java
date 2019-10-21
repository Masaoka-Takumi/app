package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.provider.Settings;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Comparator;

import javax.inject.Inject;
import javax.inject.Singleton;

import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.domain.component.NotificationProvider;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * 通知相互作用者.
 * <p>
 * {@link NotificationProvider}と{@link NotificationListener}の間を取り持つ。
 * {@link NotificationListener}の利用者となる{@link NotificationListenerService}が
 * システムから起動されるため、本クラスはシングルトンとなっている。
 */
@Singleton
public class NotificationInteractor implements NotificationProvider, NotificationListener {
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    @Inject Context mContext;
    @Inject @ForDomain StatusHolder mStatusHolder;
    private NotificationListenerService mNotificationListenerService;
    private WeakReference<OnConnectedListener> mOnConnectedListener;
    private WeakReference<OnPostedListener> mOnPostedListener;
    private WeakReference<OnRemovedListener> mOnRemovedListener;

    /**
     * コンストラクタ.
     */
    @Inject
    public NotificationInteractor() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attachNotificationListenerService(@NonNull NotificationListenerService service) {
        mNotificationListenerService = checkNotNull(service);
        if(!mStatusHolder.getAppStatus().isConnectNotificationListenerService){
            mStatusHolder.getAppStatus().isConnectNotificationListenerService = true;

            Optional.ofNullable(getOnConnectedListener())
                    .ifPresent(OnConnectedListener::onConnected);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detachNotificationListenerService() {
        mNotificationListenerService = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotificationPosted(@NonNull StatusBarNotification sbn) {
        checkNotNull(sbn);

        Optional.ofNullable(getOnPostedListener())
                .ifPresent(listener -> listener.onPosted(sbn));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotificationRemoved(@NonNull StatusBarNotification sbn) {
        checkNotNull(sbn);

        Optional.ofNullable(getOnRemovedListener())
                .ifPresent(listener -> listener.onRemoved(sbn));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isGrantReadNotification() {
        String rawListeners = getEnabledNotificationListeners();
        if (TextUtils.isEmpty(rawListeners)) {
            return false;
        }

        // パッケージ名/サービスクラス名:パッケージ名/サービスクラス名:…の形式で
        // 格納されているので、：で分割してパッケージ名で始まっているか否かで判定する。
        String packageName = mContext.getPackageName();
        return Stream.of(rawListeners.split(":"))
                .anyMatch(listener -> listener.startsWith(packageName));
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public StatusBarNotification[] getStatusBarNotifications(@Nullable Comparator<StatusBarNotification> comparator) {
        if (mNotificationListenerService == null) {
            return new StatusBarNotification[0];
        }

        StatusBarNotification[] sbns = mNotificationListenerService.getActiveNotifications();
        if (comparator != null) {
            Arrays.sort(sbns, comparator);
        }

        return sbns;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnConnectedListener(@Nullable OnConnectedListener listener) {
        if (listener == null) {
            mOnConnectedListener = null;
        } else {
            mOnConnectedListener = new WeakReference<>(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnPostedListener(@Nullable OnPostedListener listener) {
        if (listener == null) {
            mOnPostedListener = null;
        } else {
            mOnPostedListener = new WeakReference<>(listener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnRemovedListener(@Nullable OnRemovedListener listener) {
        if (listener == null) {
            mOnRemovedListener = null;
        } else {
            mOnRemovedListener = new WeakReference<>(listener);
        }
    }

    private OnConnectedListener getOnConnectedListener(){
        return (mOnConnectedListener == null) ? null : mOnConnectedListener.get();
    }

    private OnPostedListener getOnPostedListener() {
        return (mOnPostedListener == null) ? null : mOnPostedListener.get();
    }

    private OnRemovedListener getOnRemovedListener() {
        return (mOnRemovedListener == null) ? null : mOnRemovedListener.get();
    }

    @VisibleForTesting
    String getEnabledNotificationListeners(){
        return Settings.Secure.getString(mContext.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
    }

}
