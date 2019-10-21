package jp.pioneer.carsync.presentation.view;

import android.support.annotation.Nullable;

import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;

/**
 * Phone設定画面の抽象クラス.
 */
public interface PhoneSettingView {

    /**
     * デバイス設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     */
    void setDeviceSettings(boolean isSupported,
                           boolean isEnabled);

    /**
     * AutoPairing設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setAutoPairingSetting(boolean isSupported,
                               boolean isEnabled,
                               boolean setting);

    /**
     * 連絡帳アクセス設定.
     *
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setPhoneBookAccessibleSetting(boolean setting);

    /**
     * BT着信時パターン設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setIncomingCallPatternSetting(boolean isSupported,
                                       boolean isEnabled,
                                       @Nullable BtPhoneColor setting);

    /**
     * BT着信時カラー有効設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容
     */
    void setIncomingCallColorSetting(boolean isSupported,
                                     boolean isEnabled,
                                     @Nullable SphBtPhoneColorSetting setting);

    /**
     * AutoAnswer設定.
     *
     * @param isSupported 対応しているか否か
     * @param isEnabled 設定可能か否か
     * @param setting 設定内容 {@code true}:設定有効 {@code false}:設定無効
     */
    void setAutoAnswerSetting(boolean isSupported,
                              boolean isEnabled,
                              boolean setting);
}
