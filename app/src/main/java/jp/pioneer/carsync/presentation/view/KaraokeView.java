package jp.pioneer.carsync.presentation.view;

/**
 * Created by NSW00_008320 on 2018/01/30.
 */

public interface KaraokeView {

    /**
     * カラオケ設定
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か

     */
    void setMicrophoneSettingEnabled(boolean isSupported,
                              boolean isEnabled);
    /**
     * カラオケ設定
     *
     * @param setting 設定内容
     */
    void setMicrophoneSetting(boolean setting);

    /**
     * マイク音量設定
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param min 設定可能最小値
     * @param max 設定可能最大値
     * @param curr 設定値
     */
    void setMicVolumeSetting(boolean isSupported,
                             boolean isEnabled,
                             int min,
                             int max,
                             int curr);

    /**
     * ボーカルキャンセル設定
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setVocalCancelSetting(boolean isSupported,
                               boolean isEnabled,
                               boolean setting);
}
