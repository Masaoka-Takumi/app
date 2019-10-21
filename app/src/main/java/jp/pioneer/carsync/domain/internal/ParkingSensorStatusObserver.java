package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.ParkingSensorStatusChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * パーキングセンサーステータス監視.
 * <p>
 * パーキングセンサーステータスが変わった場合に{@link ParkingSensorStatusChangeEvent}を発行する。
 */
public class ParkingSensorStatusObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mSerialVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public ParkingSensorStatusObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mSerialVersion = statusHolder.getParkingSensorStatus().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long serialVersion = statusHolder.getParkingSensorStatus().getSerialVersion();
        if (serialVersion != mSerialVersion) {
            mSerialVersion = serialVersion;
            mEventBus.post(new ParkingSensorStatusChangeEvent());
        }
    }
}
