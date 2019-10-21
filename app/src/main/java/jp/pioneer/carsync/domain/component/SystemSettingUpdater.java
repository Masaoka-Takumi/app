package jp.pioneer.carsync.domain.component;

import android.support.annotation.NonNull;

import jp.pioneer.carsync.domain.model.AttMuteSetting;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType;

/**
 * システム設定更新.
 */
public interface SystemSettingUpdater {

    /**
     * BEEP TONE設定.
     *
     * @param enabled 有効か否か {@code true}:BEEP TONE設定有効 {@code false}:BEEP TONE設定無効
     */
    void setBeepTone(boolean enabled);

    /**
     * ATT/MUTE設定.
     *
     * @param setting ATT/MUTE設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    void setAttMute(@NonNull AttMuteSetting setting);

    /**
     * 距離単位設定.
     *
     * @param setting 距離単位設定内容
     * @throws NullPointerException {@code setting}がnull
     */
    void setDistanceUnit(@NonNull DistanceUnit setting);

    /**
     * DEMO設定.
     *
     * @param enabled 有効か否か {@code true}:DEMO設定有効 {@code false}:DEMO設定無効
     */
    void setDemo(boolean enabled);

    /**
     * POWER SAVE設定.
     *
     * @param enabled 有効か否か {@code true}:POWER SAVE設定有効 {@code false}:POWER SAVE設定無効
     */
    void setPowerSave(boolean enabled);

    /**
     * BT Audio設定.
     *
     * @param enabled 有効か否か {@code true}:BT Audio設定有効 {@code false}:BT Audio設定無効
     */
    void setBtAudio(boolean enabled);

    /**
     * Pandora設定.
     *
     * @param enabled 有効か否か {@code true}:Pandora設定有効 {@code false}:Pandora設定無効
     */
    void setPandora(boolean enabled);

    /**
     * Spotify設定.
     *
     * @param enabled 有効か否か {@code true}:Spotify設定有効 {@code false}:Spotify設定無効
     */
    void setSpotify(boolean enabled);

    /**
     * AUX設定.
     *
     * @param enabled 有効か否か {@code true}:AUX設定有効 {@code false}:AUX設定無効
     */
    void setAux(boolean enabled);

    /**
     * 99App自動起動設定.
     *
     * @param enabled 有効か否か {@code true}:99App自動起動設定有効 {@code false}:99App自動起動設定無効
     */
    void setAppAutoStart(boolean enabled);

    /**
     * USB AUTO設定.
     *
     * @param enabled 有効か否か {@code true}:USB AUTO設定有効 {@code false}:USB AUTO設定無効
     */
    void setUsbAuto(boolean enabled);

    /**
     * DISP OFF設定.
     *
     * @param enabled 有効か否か {@code true}:DISP OFF設定有効 {@code false}:DISP OFF設定無効
     */
    void setDisplayOff(boolean enabled);

    /**
     * ステアリングリモコン設定.
     *
     * @param type ステアリングリモコン設定種別
     * @throws NullPointerException {@code type}がnull
     */
    void setSteeringRemoteControl(@NonNull SteeringRemoteControlSettingType type);

    /**
     * AUTO PI設定.
     *
     * @param enabled 有効か否か {@code true}:AUTO PI設定有効 {@code false}:AUTO PI設定無効
     */
    void setAutoPi(boolean enabled);
}
