package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.ParkingSensorSettingChangeEvent;
import jp.pioneer.carsync.domain.event.ParkingSensorSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * パーキングセンサー設定監視.
 * <p>
 * パーキングセンサー設定関連の情報が更新された場合にイベントを発行する。
 *
 * @see ParkingSensorSettingStatusChangeEvent
 * @see ParkingSensorSettingChangeEvent
 */
public class ParkingSensorSettingObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mStatusVersion;
    private long mSettingVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public ParkingSensorSettingObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mStatusVersion = statusHolder.getParkingSensorSettingStatus().getSerialVersion();
        mSettingVersion = statusHolder.getParkingSensorSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long statusVersion = statusHolder.getParkingSensorSettingStatus().getSerialVersion();
        if (statusVersion != mStatusVersion) {
            mStatusVersion = statusVersion;
            mEventBus.post(new ParkingSensorSettingStatusChangeEvent());
        }

        long settingVersion = statusHolder.getParkingSensorSetting().getSerialVersion();
        if (settingVersion != mSettingVersion) {
            mSettingVersion = settingVersion;
            mEventBus.post(new ParkingSensorSettingChangeEvent());
        }
    }
}
