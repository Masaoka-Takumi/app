package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.SystemSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SystemSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SystemSettingSpec;

/**
 * システム設定監視.
 * <p>
 * システム設定関連の情報が更新された場合にイベントを発行する
 *
 * @see SystemSettingStatusChangeEvent
 * @see SystemSettingChangeEvent
 */
public class SystemSettingObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    private long mStatusVersion;
    private long mSettingVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public SystemSettingObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mStatusVersion = statusHolder.getSystemSettingStatus().getSerialVersion();
        mSettingVersion = statusHolder.getSystemSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long statusVersion = statusHolder.getSystemSettingStatus().getSerialVersion();
        if (statusVersion != mStatusVersion) {
            mStatusVersion = statusVersion;
            mEventBus.post(new SystemSettingStatusChangeEvent());
        }

        long settingVersion = statusHolder.getSystemSetting().getSerialVersion();
        if (settingVersion != mSettingVersion) {
            CarDeviceSpec spec = statusHolder.getCarDeviceSpec();
            SystemSettingSpec systemSpec = spec.systemSettingSpec;
            if(systemSpec.distanceUnitSettingSupported){
                mPreference.setDistanceUnit(statusHolder.getSystemSetting().distanceUnit);
            }
            mSettingVersion = settingVersion;
            mEventBus.post(new SystemSettingChangeEvent());
        }
    }
}
