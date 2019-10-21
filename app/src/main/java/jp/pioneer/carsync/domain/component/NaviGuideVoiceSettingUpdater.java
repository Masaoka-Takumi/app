package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;

/**
 * ナビガイド音声設定更新.
 */
public interface NaviGuideVoiceSettingUpdater {

    /**
     * ナビガイド音声設定.
     *
     * @param enabled 有効か否か {@code true}:ナビガイド音声設定有効 {@code false}:ナビガイド音声設定無効
     */
    void setNaviGuideVoice(boolean enabled);

    /**
     * ナビガイド音声設定.
     *
     * @param setting  設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    void setNaviGuideVoiceVolume(@NonNull NaviGuideVoiceVolumeSetting setting);
}
