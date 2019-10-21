package jp.pioneer.carsync.presentation.view;

import jp.pioneer.carsync.domain.model.IlluminationColor;

/**
 * テーマ設定画面のinterface
 */

public interface ThemeView {

    /**
     * テーマ設定.
     *
     * @param isEnabled 設定可能か否か
     */
    void setThemeSetting(boolean isEnabled);

    /**
     * UIカラー設定.
     *
     * @param isEnabled 設定可能か否か
     */
    void setUiColorSetting(boolean isEnabled);

    /**
     * イルミネーションカラー設定(共通).
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     */
    void setIlluminationSetting(boolean isSupported,
                                boolean isEnabled);

    /**
     * イルミネーションカラー設定(Display).
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     */
    void setDisplayIlluminationSetting(boolean isSupported,
                                       boolean isEnabled);

    /**
     * イルミネーションカラー設定(Key).
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     */
    void setKeyIlluminationSetting(boolean isSupported,
                                   boolean isEnabled);

    /**
     * デュアルイルミネーションカラー設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setDualIlluminationSetting(boolean isSupported,
                                    boolean isEnabled,
                                    IlluminationColor setting);

    /**
     * ディマー設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     */
    void setDimmerSetting(boolean isSupported,
                          boolean isEnabled);

    /**
     * 輝度設定(共通).
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param min 設定可能最小値
     * @param max 設定可能最大値
     * @param curr 現在設定値
     */
    void setBrightnessSetting(boolean isSupported,
                              boolean isEnabled,
                              int min,
                              int max,
                              int curr);

    /**
     * 輝度設定(Display).
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param min 設定可能最小値
     * @param max 設定可能最大値
     * @param curr 現在設定値
     */
    void setDisplayBrightnessSetting(boolean isSupported,
                                     boolean isEnabled,
                                     int min,
                                     int max,
                                     int curr);

    /**
     * 輝度設定(Key).
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param min 設定可能最小値
     * @param max 設定可能最大値
     * @param curr 現在設定値
     */
    void setKeyBrightnessSetting(boolean isSupported,
                                 boolean isEnabled,
                                 int min,
                                 int max,
                                 int curr);

    /**
     * 蛍の光設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setIlluminationEffectSetting(boolean isSupported,
                                      boolean isEnabled,
                                      boolean setting);

    /**
     * BGV連動設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setBgvLinkedSetting(boolean isSupported,
                             boolean isEnabled,
                             boolean setting);
}
