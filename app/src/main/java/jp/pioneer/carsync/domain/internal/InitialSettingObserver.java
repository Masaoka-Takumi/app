package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.InitialSettingChangeEvent;
import jp.pioneer.carsync.domain.event.InitialSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * 初期設定監視.
 * <p>
 * 初期設定関連の情報が更新された場合にイベントを発行する。
 *
 * @see InitialSettingStatusChangeEvent
 * @see InitialSettingChangeEvent
 */
public class InitialSettingObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mStatusVersion;
    private long mSettingVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public InitialSettingObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mStatusVersion = statusHolder.getInitialSettingStatus().getSerialVersion();
        mSettingVersion = statusHolder.getInitialSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long statusVersion = statusHolder.getInitialSettingStatus().getSerialVersion();
        if (statusVersion != mStatusVersion) {
            mStatusVersion = statusVersion;
            mEventBus.post(new InitialSettingStatusChangeEvent());
        }

        long settingVersion = statusHolder.getInitialSetting().getSerialVersion();
        if (settingVersion != mSettingVersion) {
            mSettingVersion = settingVersion;
            mEventBus.post(new InitialSettingChangeEvent());
        }
    }
}
