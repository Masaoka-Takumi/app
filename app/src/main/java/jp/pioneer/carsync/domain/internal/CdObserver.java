package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.CdInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * CDソース監視.
 * <p>
 * CDソース関連の情報が更新された場合にイベントを発行する。
 *
 * @see CdInfoChangeEvent
 */
public class CdObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mInfoVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public CdObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mInfoVersion = statusHolder.getCarDeviceMediaInfoHolder().cdInfo.getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long infoVersion = statusHolder.getCarDeviceMediaInfoHolder().cdInfo.getSerialVersion();
        if (infoVersion != mInfoVersion) {
            mInfoVersion = infoVersion;
            mEventBus.post(new CdInfoChangeEvent());
        }
    }
}
