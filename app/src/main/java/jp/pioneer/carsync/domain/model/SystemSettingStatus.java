package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * システム設定ステータス.
 */
public class SystemSettingStatus extends SerialVersion {
    /** AUX設定有効.*/
    public boolean auxSettingEnabled;
    /** Spotify設定有効.*/
    public boolean spotifySettingEnabled;
    /** Pandora設定有効.*/
    public boolean pandoraSettingEnabled;
    /** BT Audio設定有効.*/
    public boolean btAudioSettingEnabled;
    /** POWER SAVE設定有効.*/
    public boolean powerSaveSettingEnabled;
    /** DEMO設定有効.*/
    public boolean demoSettingEnabled;
    /** ATT/MUTE設定有効.*/
    public boolean attMuteSettingEnabled;
    /** BEEP TONE設定有効.*/
    public boolean beepToneSettingEnabled;
    /** DISP OFF設定有効.*/
    public boolean displayOffSettingEnabled;
    /** distance Unit設定有効.*/
    public boolean distanceUnitSettingEnabled;
    /** AUTO PI設定有効.*/
    public boolean autoPiSettingEnabled;
    /** ステアリングリモコン設定有効.*/
    public boolean steeringRemoteControlSettingEnabled;
    /** USB AUTO設定有効.*/
    public boolean usbAutoSettingEnabled;
    /** 99APP自動起動設定有効.*/
    public boolean appAutoStartSettingEnabled;

    /**
     * コンストラクタ.
     */
    public SystemSettingStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        auxSettingEnabled = false;
        spotifySettingEnabled = false;
        pandoraSettingEnabled = false;
        btAudioSettingEnabled = false;
        powerSaveSettingEnabled = false;
        attMuteSettingEnabled = false;
        beepToneSettingEnabled = false;
        displayOffSettingEnabled = false;
        distanceUnitSettingEnabled = false;
        autoPiSettingEnabled = false;
        steeringRemoteControlSettingEnabled = false;
        usbAutoSettingEnabled = false;
        appAutoStartSettingEnabled = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("auxSettingEnabled", auxSettingEnabled)
                .add("spotifySettingEnabled", spotifySettingEnabled)
                .add("pandoraSettingEnabled", pandoraSettingEnabled)
                .add("btAudioSettingEnabled", btAudioSettingEnabled)
                .add("powerSaveSettingEnabled", powerSaveSettingEnabled)
                .add("attMuteSettingEnabled", attMuteSettingEnabled)
                .add("beepToneSettingEnabled", beepToneSettingEnabled)
                .add("displayOffSettingEnabled", displayOffSettingEnabled)
                .add("distanceUnitSettingEnabled", distanceUnitSettingEnabled)
                .add("autoPiSettingEnabled", autoPiSettingEnabled)
                .add("steeringRemoteControlSettingEnabled", steeringRemoteControlSettingEnabled)
                .add("usbAutoSettingEnabled", usbAutoSettingEnabled)
                .add("appAutoStartSettingEnabled", appAutoStartSettingEnabled)
                .toString();
    }
}
