package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;

/**
 * ナビガイド音声音量設定インターフェイス.
 */
public interface GuidanceVolumeDialogView {

    /**
     * 音量設定.
     *
     * @param setting 設定内容
     */
    void setVolumeSetting(NaviGuideVoiceVolumeSetting setting);

    /**
     * ダイアログクローズ.
     */
    void callbackClose();
}
