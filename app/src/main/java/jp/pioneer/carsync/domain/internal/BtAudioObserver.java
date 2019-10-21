package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.BtAudioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.BtAudioInfoChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * BT Audioソース監視.
 * <p>
 * BT Audioソース関連の情報が更新された場合にイベントを発行する。
 *
 * @see BtAudioInfoChangeEvent
 * @see BtAudioFunctionSettingStatusChangeEvent
 */
public class BtAudioObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mInfoVersion;
    private long mFunctionSettingStatusVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public BtAudioObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mInfoVersion = statusHolder.getCarDeviceMediaInfoHolder().btAudioInfo.getSerialVersion();
        mFunctionSettingStatusVersion = statusHolder.getBtAudioFunctionSettingStatus().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long infoVersion = statusHolder.getCarDeviceMediaInfoHolder().btAudioInfo.getSerialVersion();
        if (infoVersion != mInfoVersion) {
            mInfoVersion = infoVersion;
            mEventBus.post(new BtAudioInfoChangeEvent());
        }

        long functionSettingStatusVersion = statusHolder.getBtAudioFunctionSettingStatus().getSerialVersion();
        if (functionSettingStatusVersion != mFunctionSettingStatusVersion) {
            mFunctionSettingStatusVersion = functionSettingStatusVersion;
            mEventBus.post(new BtAudioFunctionSettingStatusChangeEvent());
        }
    }
}
