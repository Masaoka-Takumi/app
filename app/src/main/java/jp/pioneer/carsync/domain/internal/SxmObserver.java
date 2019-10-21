package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.SxmInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * SiriusXMソース監視.
 * <p>
 * SiriusXM関連の情報が更新された場合にイベントを発行する。
 *
 * @see SxmInfoChangeEvent
 */
public class SxmObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mInfoVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public SxmObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mInfoVersion = statusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo.getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long infoVersion = statusHolder.getCarDeviceMediaInfoHolder().sxmMediaInfo.getSerialVersion();
        if (infoVersion != mInfoVersion) {
            mInfoVersion = infoVersion;
            mEventBus.post(new SxmInfoChangeEvent());
        }
    }
}
