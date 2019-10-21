package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.LiveSimulationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SoundFxSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.SuperTodorokiSettingChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;

/**
 * Sound FX設定監視.
 * <p>
 * Sound FX設定関連の情報が更新された場合にイベントを発行する。
 *
 * @see SoundFxSettingStatusChangeEvent
 * @see SoundFxSettingChangeEvent
 * @see LiveSimulationSettingChangeEvent
 * @see SuperTodorokiSettingChangeEvent
 */
public class SoundFxSettingObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mStatusVersion;
    private long mSettingVersion;
    private long mLiveSimulationSettingVersion;
    private SuperTodorokiSetting mSuperTodorokiSetting;

    /**
     * コンストラクタ
     */
    @Inject
    public SoundFxSettingObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mStatusVersion = statusHolder.getSoundFxSettingStatus().getSerialVersion();
        mSettingVersion = statusHolder.getSoundFxSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long statusVersion = statusHolder.getSoundFxSettingStatus().getSerialVersion();
        if (statusVersion != mStatusVersion) {
            mStatusVersion = statusVersion;
            mEventBus.post(new SoundFxSettingStatusChangeEvent());
        }

        long settingVersion = statusHolder.getSoundFxSetting().getSerialVersion();
        if (settingVersion != mSettingVersion) {
            mSettingVersion = settingVersion;
            mEventBus.post(new SoundFxSettingChangeEvent());
        }

        long liveSimulationSettingVersion = statusHolder.getSoundFxSetting().liveSimulationSetting.getSerialVersion();
        if (liveSimulationSettingVersion != mLiveSimulationSettingVersion) {
            mLiveSimulationSettingVersion = liveSimulationSettingVersion;
            mEventBus.post(new LiveSimulationSettingChangeEvent());
        }

        SuperTodorokiSetting superTodorokiSetting = statusHolder.getSoundFxSetting().superTodorokiSetting;
        if (superTodorokiSetting != mSuperTodorokiSetting) {
            mSuperTodorokiSetting = superTodorokiSetting;
            mEventBus.post(new SuperTodorokiSettingChangeEvent());
        }
    }
}
