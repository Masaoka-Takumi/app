package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.AudioSettingChangeEvent;
import jp.pioneer.carsync.domain.event.AudioSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.event.CustomEqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.event.EqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.event.EqualizerTypeChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * Audio設定監視.
 * <p>
 * Audio設定関連の情報が更新された場合にイベントを発行する。
 *
 * @see AudioSettingStatusChangeEvent
 * @see AudioSettingChangeEvent
 * @see EqualizerSettingChangeEvent
 * @see CustomEqualizerSettingChangeEvent
 */
public class AudioSettingObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mSettingStatusVersion;
    private long mSettingVersion;
    private long mEqualizerSettingVersion;
    private long mCustomEqualizerSettingVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public AudioSettingObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mSettingStatusVersion = statusHolder.getAudioSettingStatus().getSerialVersion();
        mSettingVersion = statusHolder.getAudioSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long settingStatusVersion = statusHolder.getAudioSettingStatus().getSerialVersion();
        if (settingStatusVersion != mSettingStatusVersion) {
            mSettingStatusVersion = settingStatusVersion;
            mEventBus.post(new AudioSettingStatusChangeEvent());
        }

        long settingVersion = statusHolder.getAudioSetting().getSerialVersion();
        if (settingVersion != mSettingVersion) {
            mSettingVersion = settingVersion;
            mEventBus.post(new AudioSettingChangeEvent());
        }

        long equalizerSettingVersion = statusHolder.getAudioSetting().equalizerSetting.getSerialVersion();
        if (equalizerSettingVersion != mEqualizerSettingVersion) {
            mEqualizerSettingVersion = equalizerSettingVersion;
            mEventBus.post(new EqualizerTypeChangeEvent());
        }

        long customEqualizerSettingVersion = statusHolder.getAudioSetting().customEqualizerSetting.getSerialVersion();
        if (customEqualizerSettingVersion != mCustomEqualizerSettingVersion) {
            mCustomEqualizerSettingVersion = customEqualizerSettingVersion;
            mEventBus.post(new CustomEqualizerSettingChangeEvent());
        }
    }
}
