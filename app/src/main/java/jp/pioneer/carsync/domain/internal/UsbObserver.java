package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.UsbInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * USBソース監視.
 * <p>
 * USBソース関連の情報が更新された場合にイベントを発行する。
 *
 * @see UsbInfoChangeEvent
 */
public class UsbObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mInfoVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public UsbObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mInfoVersion = statusHolder.getCarDeviceMediaInfoHolder().usbMediaInfo.getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long infoVersion = statusHolder.getCarDeviceMediaInfoHolder().usbMediaInfo.getSerialVersion();
        if (infoVersion != mInfoVersion) {
            mInfoVersion = infoVersion;
            mEventBus.post(new UsbInfoChangeEvent());
        }
    }
}