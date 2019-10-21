package jp.pioneer.carsync.domain;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.internal.ReadNotificationListener;
import jp.pioneer.carsync.domain.internal.StatusUpdateListener;
import timber.log.Timber;

/**
 * Domain層の初期化者.
 * <p>
 * アプリケーション開始時にインスタンス化しておく必要があるものの処理。
 */
public class DomainInitializer {
    @Inject StatusUpdateListener mStatusUpdateListener;
    @Inject ReadNotificationListener mReadNotificationListener;

    /**
     * コンストラクタ.
     */
    @Inject
    public DomainInitializer() {
    }

    /**
     * 初期化.
     */
    public void initialize() {
        Timber.i("initialize()");

        mStatusUpdateListener.initialize();
        mReadNotificationListener.initialize();
    }
}
