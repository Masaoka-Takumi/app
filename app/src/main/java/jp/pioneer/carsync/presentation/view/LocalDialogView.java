package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.LocalSetting;

/**
 * Local設定インターフェイス.
 */
public interface LocalDialogView {
    /**
     * Local設定.
     *
     * @param setting 設定内容
     */
    void setLocalSetting(LocalSetting setting);

    /**
     * ダイアログクローズ.
     */
    void callbackClose();
}
