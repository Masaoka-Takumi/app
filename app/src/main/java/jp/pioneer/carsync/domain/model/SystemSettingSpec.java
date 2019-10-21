package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.EnumSet;
import java.util.Set;

/**
 * システム設定スペック.
 */
public class SystemSettingSpec {
    /** AUX設定対応.*/
    public boolean auxSettingSupported;
    /** Spotify設定対応.*/
    public boolean spotifySettingSupported;
    /** Pandora設定対応.*/
    public boolean pandoraSettingSupported;
    /** BT Audio設定対応.*/
    public boolean btAudioSettingSupported;
    /** POWER SAVE設定対応.*/
    public boolean powerSaveSettingSupported;
    /** DEMO設定対応.*/
    public boolean demoSettingSupported;
    /** ATT/MUTE設定対応.*/
    public boolean attMuteSettingSupported;
    /** BEEP TONE設定対応.*/
    public boolean beepToneSettingSupported;
    /** DISP OFF設定対応.*/
    public boolean displayOffSettingSupported;
    /** distance Unit設定有効.*/
    public boolean distanceUnitSettingSupported;
    /** AUTO PI設定対応.*/
    public boolean autoPiSettingSupported;
    /** ステアリングリモコン設定対応.*/
    public boolean steeringRemoteControlSettingSupported;
    /** USB AUTO設定対応.*/
    public boolean usbAutoSettingSupported;
    /** 99APP自動起動設定対応.*/
    public boolean appAutoStartSettingSupported;
    /** サポートステアリングリモコン設定群. */
    public Set<SteeringRemoteControlSettingType> supportedSteeringRemoteControlSettings;

    /**
     * コンストラクタ.
     */
    public SystemSettingSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        auxSettingSupported = false;
        spotifySettingSupported = false;
        pandoraSettingSupported = false;
        btAudioSettingSupported = false;
        powerSaveSettingSupported = false;
        attMuteSettingSupported = false;
        beepToneSettingSupported = false;
        displayOffSettingSupported = false;
        distanceUnitSettingSupported = false;
        autoPiSettingSupported = false;
        steeringRemoteControlSettingSupported = false;
        usbAutoSettingSupported = false;
        appAutoStartSettingSupported = false;
        supportedSteeringRemoteControlSettings = EnumSet.noneOf(SteeringRemoteControlSettingType.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("auxSettingSupported", auxSettingSupported)
                .add("spotifySettingSupported", spotifySettingSupported)
                .add("pandoraSettingSupported", pandoraSettingSupported)
                .add("btAudioSettingSupported", btAudioSettingSupported)
                .add("powerSaveSettingSupported", powerSaveSettingSupported)
                .add("attMuteSettingSupported", attMuteSettingSupported)
                .add("beepToneSettingSupported", beepToneSettingSupported)
                .add("displayOffSettingSupported", displayOffSettingSupported)
                .add("distanceUnitSettingSupported", distanceUnitSettingSupported)
                .add("autoPiSettingSupported", autoPiSettingSupported)
                .add("steeringRemoteControlSettingSupported", steeringRemoteControlSettingSupported)
                .add("usbAutoSettingSupported", usbAutoSettingSupported)
                .add("appAutoStartSettingSupported", appAutoStartSettingSupported)
                .add("supportedSteeringRemoteControlSettings", supportedSteeringRemoteControlSettings)
                .toString();
    }
}
