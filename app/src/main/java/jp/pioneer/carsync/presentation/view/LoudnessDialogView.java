package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.LoudnessSetting;

/**
 * Loudness設定インターフェイス.
 */
public interface LoudnessDialogView {
    /**
     * Loudness設定.
     *
     * @param setting 設定内容
     */
    void setLoudnessSetting(LoudnessSetting setting);

    /**
     * ダイアログクローズ.
     */
    void callbackClose();
}
