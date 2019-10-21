package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * イルミ設定監視.
 * <p>
 * イルミ設定関連の情報が更新された場合にイベントを発行する。
 *
 * @see IlluminationSettingStatusChangeEvent
 * @see IlluminationSettingChangeEvent
 */
public class IlluminationSettingObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mSettingStatusVersion;
    private long mSettingVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public IlluminationSettingObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mSettingStatusVersion = statusHolder.getIlluminationSettingStatus().getSerialVersion();
        mSettingVersion = statusHolder.getIlluminationSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long settingStatusVersion = statusHolder.getIlluminationSettingStatus().getSerialVersion();
        if (settingStatusVersion != mSettingStatusVersion) {
            mSettingStatusVersion = settingStatusVersion;
            mEventBus.post(new IlluminationSettingStatusChangeEvent());
        }

        long settingVersion = statusHolder.getIlluminationSetting().getSerialVersion();
        if (settingVersion != mSettingVersion) {
            mSettingVersion = settingVersion;
            mEventBus.post(new IlluminationSettingChangeEvent());
        }
    }
}
