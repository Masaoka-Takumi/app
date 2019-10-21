package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.PhoneSettingChangeEvent;
import jp.pioneer.carsync.domain.event.PhoneSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * Phone設定監視.
 * <p>
 * Phone設定関連の情報が更新された場合にイベントを発行する。
 *
 * @see PhoneSettingStatusChangeEvent
 * @see PhoneSettingChangeEvent
 */
public class PhoneSettingObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mStatusVersion;
    private long mSettingVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public PhoneSettingObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mStatusVersion = statusHolder.getPhoneSettingStatus().getSerialVersion();
        mSettingVersion = statusHolder.getPhoneSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long statusVersion = statusHolder.getPhoneSettingStatus().getSerialVersion();
        if (statusVersion != mStatusVersion) {
            mStatusVersion = statusVersion;
            mEventBus.post(new PhoneSettingStatusChangeEvent());
        }

        long settingVersion = statusHolder.getPhoneSetting().getSerialVersion();
        if (settingVersion != mSettingVersion) {
            mSettingVersion = settingVersion;
            mEventBus.post(new PhoneSettingChangeEvent());
        }
    }
}
