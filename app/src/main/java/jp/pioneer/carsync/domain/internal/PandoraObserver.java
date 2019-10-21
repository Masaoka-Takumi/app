package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.PandoraInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * Pandoraソース監視.
 * <p>
 * Pandoraソース関連の情報が更新された場合にイベントを発行する。
 *
 * @see PandoraInfoChangeEvent
 */
public class PandoraObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mInfoVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public PandoraObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mInfoVersion = statusHolder.getCarDeviceMediaInfoHolder().pandoraMediaInfo.getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long infoVersion = statusHolder.getCarDeviceMediaInfoHolder().pandoraMediaInfo.getSerialVersion();
        if (infoVersion != mInfoVersion) {
            mInfoVersion = infoVersion;
            mEventBus.post(new PandoraInfoChangeEvent());
        }
    }
}
