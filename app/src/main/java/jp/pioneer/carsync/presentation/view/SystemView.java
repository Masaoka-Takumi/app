package jp.pioneer.carsync.presentation.view;

import android.support.annotation.Nullable;

import jp.pioneer.carsync.domain.model.AttMuteSetting;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;

/**
 * システム設定画面のinterface.
 */
public interface SystemView {

    /**
     * Initial Setting有効設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     */
    void setInitialSettings(boolean isSupported,
                            boolean isEnabled);

    /**
     * Beep Tone設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setBeepToneSetting(boolean isSupported,
                            boolean isEnabled,
                            boolean setting);

    /**
     * Auto PI設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setAutoPiSetting(boolean isSupported,
                          boolean isEnabled,
                          boolean setting);

    /**
     * AUX設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setAuxSetting(boolean isSupported,
                       boolean isEnabled,
                       boolean setting);

    /**
     * BT Audio設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setBtAudioSetting(boolean isSupported,
                           boolean isEnabled,
                           boolean setting);

    /**
     * Pandora設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setPandoraSetting(boolean isSupported,
                           boolean isEnabled,
                           boolean setting);

    /**
     * Spotify設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setSpotifySetting(boolean isSupported,
                           boolean isEnabled,
                           boolean setting);

    /**
     * Power Save Mode設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setPowerSaveModeSetting(boolean isSupported,
                                 boolean isEnabled,
                                 boolean setting);

    /**
     * 99App自動起動設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setAppAutoLaunchSetting(boolean isSupported,
                                 boolean isEnabled,
                                 boolean setting);

    /**
     * USB AUTO設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setUsbAutoSetting(boolean isSupported,
                           boolean isEnabled,
                           boolean setting);

    /**
     * Display OFF設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setDisplayOffSetting(boolean isSupported,
                              boolean isEnabled,
                              boolean setting);

    /**
     * ATT/MUTE設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setAttMuteSetting(boolean isSupported,
                           boolean isEnabled,
                           @Nullable AttMuteSetting setting);
    /**
     * 距離単位設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setDistanceUnit(boolean isSupported,
                         boolean isEnabled,
                         @Nullable DistanceUnit setting);

    /**
     * 時刻表示設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setTimeFormatSetting(boolean isSupported,
                              boolean isEnabled,
                              @Nullable TimeFormatSetting setting);
}
