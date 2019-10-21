package jp.pioneer.carsync.presentation.view;

import android.support.annotation.Nullable;

import jp.pioneer.carsync.domain.model.LocalSetting;

public interface HdRadioSettingView {

    /**
     * Local設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setLocalSetting(boolean isSupported,
                         boolean isEnabled,
                         @Nullable LocalSetting setting);

    /**
     * Seek設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setSeekSetting(boolean isSupported,
                        boolean isEnabled,
                        boolean setting);

    /**
     * Blending設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setBlendingSetting(boolean isSupported,
                            boolean isEnabled,
                            boolean setting);

    /**
     * ActiveRadio設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setActiveRadioSetting(boolean isSupported,
                               boolean isEnabled,
                               boolean setting);
}
