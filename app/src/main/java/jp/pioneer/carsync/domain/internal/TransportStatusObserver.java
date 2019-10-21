package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.TransportStatusChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TransportStatus;

/**
 * 通信路の状態監視.
 * <p>
 * 通信路の状態が変わった場合に{@link TransportStatusChangeEvent}を発行する。
 */
public class TransportStatusObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private TransportStatus mTransportStatus;

    /**
     * コンストラクタ
     */
    @Inject
    public TransportStatusObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mTransportStatus = statusHolder.getTransportStatus();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        TransportStatus transportStatus = statusHolder.getTransportStatus();
        if (transportStatus != mTransportStatus) {
            mTransportStatus = transportStatus;
            mEventBus.post(new TransportStatusChangeEvent());
        }
    }
}
