package jp.pioneer.carsync.domain.internal;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.event.NaviGuideVoiceSettingChangeEvent;
import jp.pioneer.carsync.domain.model.StatusHolder;

/**
 * ナビガイド音声設定監視.
 * <p>
 * ナビガイド音声設定が変わった場合に{@link NaviGuideVoiceSettingChangeEvent}を発行する。
 */
public class NaviGuideVoiceSettingObserver implements StatusObserver {
    @Inject EventBus mEventBus;
    private long mSerialVersion;

    /**
     * コンストラクタ
     */
    @Inject
    public NaviGuideVoiceSettingObserver() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(StatusHolder statusHolder) {
        mSerialVersion = statusHolder.getNaviGuideVoiceSetting().getSerialVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStatusUpdate(StatusHolder statusHolder) {
        long serialVersion = statusHolder.getNaviGuideVoiceSetting().getSerialVersion();
        if (serialVersion != mSerialVersion) {
            mSerialVersion = serialVersion;
            mEventBus.post(new NaviGuideVoiceSettingChangeEvent());
        }
    }
}
