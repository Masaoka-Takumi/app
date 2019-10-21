package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.ArrayList;
import java.util.List;

/**
 * システム設定.
 */
public class SystemSetting extends Setting {
    /** 車載機へのリクエスト状態. */
    public RequestStatus requestStatus;
    /** BEEP TONE設定ON. */
    public boolean beepToneSetting;
    /** ATT/MUTE設定. */
    public AttMuteSetting attMuteSetting;
    /** DEMO設定ON. */
    public boolean demoSetting;
    /** POWER SAVE設定ON. */
    public boolean powerSaveSetting;
    /** BT Audio設定ON. */
    public boolean btAudioSetting;
    /** Pandora設定ON. */
    public boolean pandoraSetting;
    /** Spotify設定ON. */
    public boolean spotifySetting;
    /** AUX設定ON. */
    public boolean auxSetting;
    /** 99APP自動起動設定ON. */
    public boolean appAutoStartSetting;
    /** USB AUTO設定ON. */
    public boolean usbAutoSetting;
    /** ステアリングリモコン設定. */
    public SteeringRemoteControlSettingType steeringRemoteControlSetting;
    /** AUTO PI設定ON. */
    public boolean autoPiSetting;
    /** DISP OFF設定ON. */
    public boolean displayOffSetting;
    /** 距離単位設定. */
    public DistanceUnit distanceUnit;
    /**
     * コンストラクタ.
     */
    public SystemSetting() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        requestStatus = RequestStatus.NOT_SENT;
        beepToneSetting = false;
        attMuteSetting = AttMuteSetting.MUTE;
        demoSetting = false;
        powerSaveSetting = false;
        btAudioSetting = false;
        pandoraSetting = false;
        spotifySetting = false;
        auxSetting = false;
        appAutoStartSetting = false;
        usbAutoSetting = false;
        steeringRemoteControlSetting = SteeringRemoteControlSettingType.OFF;
        autoPiSetting = false;
        displayOffSetting = false;
        distanceUnit = DistanceUnit.METER_KILOMETER;
        clear();
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("requestStatus", requestStatus)
                .add("beepToneSetting", beepToneSetting)
                .add("attMuteSetting", attMuteSetting)
                .add("demoSetting", demoSetting)
                .add("powerSaveSetting", powerSaveSetting)
                .add("btAudioSetting", btAudioSetting)
                .add("pandoraSetting", pandoraSetting)
                .add("spotifySetting", spotifySetting)
                .add("auxSetting", auxSetting)
                .add("appAutoStartSetting", appAutoStartSetting)
                .add("usbAutoSetting", usbAutoSetting)
                .add("steeringRemoteControlSetting", steeringRemoteControlSetting)
                .add("autoPiSetting", autoPiSetting)
                .add("displayOffSetting", displayOffSetting)
                .add("distanceUnit", distanceUnit)
                .toString();
    }

    /**
     * タグ.
     */
    public static class Tag {
        public static final String BEEP_TONE = "beep_tone";
        public static final String ATT_MUTE = "att_mute";
        public static final String DEMO = "demo";
        public static final String POWER_SAVE = "power_save";
        public static final String BT_AUDIO = "bt_audio";
        public static final String PANDORA = "pandora";
        public static final String SPOTIFY = "spotify";
        public static final String AUX = "aux";
        public static final String APP_AUTO_START = "app_auto_start";
        public static final String USB_AUTO = "usb_auto";
        public static final String STEERING_REMOTE_CONTROL = "steering_remote_control";
        public static final String AUTO_PI = "auto_pi";
        public static final String DISPLAY_OFF = "display_off";
        public static final String DISTANCE_UNIT = "distance_unit";

        /**
         * 全てのタグを取得.
         *
         * @return 全てのタグ名
         */
        public static List<String> getAllTags(){
            return new ArrayList<String>(){{
                add(BEEP_TONE);
                add(ATT_MUTE);
                add(DEMO);
                add(POWER_SAVE);
                add(BT_AUDIO);
                add(PANDORA);
                add(SPOTIFY);
                add(AUX);
                add(APP_AUTO_START);
                add(USB_AUTO);
                add(STEERING_REMOTE_CONTROL);
                add(AUTO_PI);
                add(DISPLAY_OFF);
                add(DISTANCE_UNIT);
            }};
        }
    }
}
