package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import java.util.EnumSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 車載機ステータス.
 */
@SuppressFBWarnings("UWF_NULL_FIELD")
public class CarDeviceStatus extends SerialVersion {
    /** 連携モードレベル. */
    public CarDeviceControlLevel controlLevel;
    /** ソース種別. */
    public MediaSourceType sourceType;
    /** ソース状態. */
    public MediaSourceStatus sourceStatus;
    /** 有効ソース群. */
    public Set<MediaSourceType> availableSourceTypes;
    /** SEEK STEP状態. */
    public TunerSeekStep seekStep;
    /** ATT状態. */
    public AttMode attMode;
    /** MUTE状態. */
    public MuteMode muteMode;
    /** リスト種別. */
    public ListType listType;
    /** Phone設定可能. */
    public boolean phoneSettingEnabled;
    /** オーディオ設定（AC2）可能. */
    public boolean ac2AudioSettingEnabled;
    /** オーディオ設定（JASPER）可能. */
    public boolean jasperAudioSettingEnabled;
    /** Function設定可能. */
    public boolean functionSettingEnabled;
    /** MIXTRAX設定可能. */
    public boolean mixtraxSettingEnabled;
    /** イルミ設定可能. */
    public boolean illuminationSettingEnabled;
    /** オーディオ設定可能. */
    public boolean audioSettingEnabled;
    /** システム設定可能. */
    public boolean systemSettingEnabled;
    /** Sound FX設定可能. */
    public boolean soundFxSettingEnabled;
    /** 初期設定可能. */
    public boolean initialSettingEnabled;
    /** ナビガイド音声設定可能. */
    public boolean naviGuideVoiceSettingEnabled;
    /** パーキングセンサー設定可能. */
    public boolean parkingSensorSettingEnabled;
    /** BT Audio Function有効. */
    public boolean btAudioFunctionSettingEnabled;
    /** DAB Function有効. */
    public boolean dabFunctionSettingEnabled;
    /** HD Radio Function有効. */
    public boolean hdRadioFunctionSettingEnabled;
    /** Radio Function有効. */
    public boolean tunerFunctionSettingEnabled;
    /** MIXTRAX設定状態. */
    public MixtraxSettingStatus mixtraxSettingStatus = new MixtraxSettingStatus();
    /** パーキングセンサー表示状態. */
    public boolean isDisplayParkingSensor;
    /** リバース状態. */
    public ReverseStatus reverseStatus;
    /** パーキング状態. */
    public ParkingStatus parkingStatus;
    /** 車載機ボリュームの最大値. */
    public int maxDeviceVolume;
    /** 現在の車載機ボリューム. */
    public int currentDeviceVolume;
    /** ボリューム表示開始状態. */
    public boolean deviceVolumeDisplayStatus;

    /**
     * コンストラクタ.
     */
    public CarDeviceStatus() {
        reset();
    }

    /**
     * コピーコンストラクタ.
     *
     * @param carDeviceStatus コピー元のオブジェクト
     */
    public CarDeviceStatus(CarDeviceStatus carDeviceStatus) {
        controlLevel = carDeviceStatus.controlLevel;
        sourceType = carDeviceStatus.sourceType;
        sourceStatus = carDeviceStatus.sourceStatus;
        availableSourceTypes = EnumSet.copyOf(carDeviceStatus.availableSourceTypes);
        seekStep = carDeviceStatus.seekStep;
        attMode = carDeviceStatus.attMode;
        muteMode = carDeviceStatus.muteMode;
        listType = carDeviceStatus.listType;
        phoneSettingEnabled = carDeviceStatus.phoneSettingEnabled;
        ac2AudioSettingEnabled = carDeviceStatus.ac2AudioSettingEnabled;
        jasperAudioSettingEnabled = carDeviceStatus.jasperAudioSettingEnabled;
        functionSettingEnabled = carDeviceStatus.functionSettingEnabled;
        mixtraxSettingEnabled = carDeviceStatus.mixtraxSettingEnabled;
        illuminationSettingEnabled = carDeviceStatus.illuminationSettingEnabled;
        audioSettingEnabled = carDeviceStatus.audioSettingEnabled;
        systemSettingEnabled = carDeviceStatus.systemSettingEnabled;
        soundFxSettingEnabled = carDeviceStatus.soundFxSettingEnabled;
        initialSettingEnabled = carDeviceStatus.initialSettingEnabled;
        naviGuideVoiceSettingEnabled = carDeviceStatus.naviGuideVoiceSettingEnabled;
        parkingSensorSettingEnabled = carDeviceStatus.parkingSensorSettingEnabled;
        btAudioFunctionSettingEnabled = carDeviceStatus.btAudioFunctionSettingEnabled;
        dabFunctionSettingEnabled = carDeviceStatus.dabFunctionSettingEnabled;
        hdRadioFunctionSettingEnabled = carDeviceStatus.hdRadioFunctionSettingEnabled;
        tunerFunctionSettingEnabled = carDeviceStatus.tunerFunctionSettingEnabled;
        mixtraxSettingStatus = new MixtraxSettingStatus(carDeviceStatus.mixtraxSettingStatus);
        isDisplayParkingSensor = carDeviceStatus.isDisplayParkingSensor;
        reverseStatus = carDeviceStatus.reverseStatus;
        parkingStatus = carDeviceStatus.parkingStatus;
        maxDeviceVolume = carDeviceStatus.maxDeviceVolume;
        currentDeviceVolume = carDeviceStatus.currentDeviceVolume;
        deviceVolumeDisplayStatus = carDeviceStatus.deviceVolumeDisplayStatus;
    }

    /**
     * リセット.
     */
    public void reset() {
        controlLevel = CarDeviceControlLevel.FULL_CONTROL;
        sourceType = MediaSourceType.IPOD;
        sourceStatus = MediaSourceStatus.CHANGE_COMPLETED;
        availableSourceTypes = EnumSet.of(MediaSourceType.APP_MUSIC);
        seekStep = null;
        attMode = null;
        muteMode = null;
        listType = ListType.NOT_LIST;
        phoneSettingEnabled = false;
        ac2AudioSettingEnabled = false;
        jasperAudioSettingEnabled = false;
        functionSettingEnabled = false;
        mixtraxSettingEnabled = false;
        illuminationSettingEnabled = false;
        audioSettingEnabled = false;
        systemSettingEnabled = false;
        soundFxSettingEnabled = false;
        initialSettingEnabled = false;
        naviGuideVoiceSettingEnabled = false;
        parkingSensorSettingEnabled = false;
        btAudioFunctionSettingEnabled = false;
        dabFunctionSettingEnabled = false;
        hdRadioFunctionSettingEnabled = false;
        tunerFunctionSettingEnabled = false;
        mixtraxSettingStatus.reset();
        isDisplayParkingSensor = false;
        reverseStatus = null;
        parkingStatus = null;
        maxDeviceVolume = 0;
        currentDeviceVolume = 0;
        deviceVolumeDisplayStatus = false;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof CarDeviceStatus)) {
            return false;
        }

        CarDeviceStatus other = (CarDeviceStatus) obj;
        return Objects.equal(controlLevel, other.controlLevel)
                && Objects.equal(sourceType, other.sourceType)
                && Objects.equal(sourceStatus, other.sourceStatus)
                && Objects.equal(availableSourceTypes, other.availableSourceTypes)
                && Objects.equal(seekStep, other.seekStep)
                && Objects.equal(attMode, other.attMode)
                && Objects.equal(muteMode, other.muteMode)
                && Objects.equal(listType, other.listType)
                && Objects.equal(phoneSettingEnabled, other.phoneSettingEnabled)
                && Objects.equal(ac2AudioSettingEnabled, other.ac2AudioSettingEnabled)
                && Objects.equal(jasperAudioSettingEnabled, other.jasperAudioSettingEnabled)
                && Objects.equal(functionSettingEnabled, other.functionSettingEnabled)
                && Objects.equal(mixtraxSettingEnabled, other.mixtraxSettingEnabled)
                && Objects.equal(illuminationSettingEnabled, other.illuminationSettingEnabled)
                && Objects.equal(audioSettingEnabled, other.audioSettingEnabled)
                && Objects.equal(systemSettingEnabled, other.systemSettingEnabled)
                && Objects.equal(soundFxSettingEnabled, other.soundFxSettingEnabled)
                && Objects.equal(initialSettingEnabled, other.initialSettingEnabled)
                && Objects.equal(naviGuideVoiceSettingEnabled, other.naviGuideVoiceSettingEnabled)
                && Objects.equal(parkingSensorSettingEnabled, other.parkingSensorSettingEnabled)
                && Objects.equal(btAudioFunctionSettingEnabled, other.btAudioFunctionSettingEnabled)
                && Objects.equal(dabFunctionSettingEnabled, other.dabFunctionSettingEnabled)
                && Objects.equal(hdRadioFunctionSettingEnabled, other.hdRadioFunctionSettingEnabled)
                && Objects.equal(tunerFunctionSettingEnabled, other.tunerFunctionSettingEnabled)
                && Objects.equal(mixtraxSettingStatus, other.mixtraxSettingStatus)
                && Objects.equal(isDisplayParkingSensor, other.isDisplayParkingSensor)
                && Objects.equal(reverseStatus, other.reverseStatus)
                && Objects.equal(parkingStatus, other.parkingStatus)
                && Objects.equal(maxDeviceVolume, other.maxDeviceVolume)
                && Objects.equal(currentDeviceVolume, other.currentDeviceVolume)
                && Objects.equal(deviceVolumeDisplayStatus, other.deviceVolumeDisplayStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(controlLevel,
                sourceType,
                sourceStatus,
                availableSourceTypes,
                seekStep,
                attMode,
                muteMode,
                listType,
                phoneSettingEnabled,
                ac2AudioSettingEnabled,
                jasperAudioSettingEnabled,
                functionSettingEnabled,
                mixtraxSettingEnabled,
                illuminationSettingEnabled,
                audioSettingEnabled,
                systemSettingEnabled,
                soundFxSettingEnabled,
                initialSettingEnabled,
                naviGuideVoiceSettingEnabled,
                parkingSensorSettingEnabled,
                btAudioFunctionSettingEnabled,
                dabFunctionSettingEnabled,
                hdRadioFunctionSettingEnabled,
                tunerFunctionSettingEnabled,
                mixtraxSettingStatus,
                isDisplayParkingSensor,
                reverseStatus,
                parkingStatus,
                maxDeviceVolume,
                currentDeviceVolume,
                deviceVolumeDisplayStatus);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("controlLevel", controlLevel)
                .add("sourceType", sourceType)
                .add("sourceStatus", sourceStatus)
                .add("availableSourceTypes", availableSourceTypes)
                .add("seekStep", seekStep)
                .add("attMode", attMode)
                .add("muteMode", muteMode)
                .add("listType", listType)
                .add("phoneSettingEnabled", phoneSettingEnabled)
                .add("ac2AudioSettingEnabled", ac2AudioSettingEnabled)
                .add("jasperAudioSettingEnabled", jasperAudioSettingEnabled)
                .add("functionSettingEnabled", functionSettingEnabled)
                .add("mixtraxSettingEnabled", mixtraxSettingEnabled)
                .add("illuminationSettingEnabled", illuminationSettingEnabled)
                .add("audioSettingEnabled", audioSettingEnabled)
                .add("systemSettingEnabled", systemSettingEnabled)
                .add("soundFxSettingEnabled", soundFxSettingEnabled)
                .add("initialSettingEnabled", initialSettingEnabled)
                .add("naviGuideVoiceSettingEnabled", naviGuideVoiceSettingEnabled)
                .add("parkingSensorSettingEnabled", parkingSensorSettingEnabled)
                .add("btAudioFunctionSettingEnabled", btAudioFunctionSettingEnabled)
                .add("dabFunctionSettingEnabled", dabFunctionSettingEnabled)
                .add("hdRadioFunctionSettingEnabled", hdRadioFunctionSettingEnabled)
                .add("tunerFunctionSettingEnabled", tunerFunctionSettingEnabled)
                .add("mixtraxSettingStatus", mixtraxSettingStatus)
                .add("isDisplayParkingSensor", isDisplayParkingSensor)
                .add("reverseStatus", reverseStatus)
                .add("parkingStatus", parkingStatus)
                .add("maxDeviceVolume", maxDeviceVolume)
                .add("currentDeviceVolume", currentDeviceVolume)
                .add("deviceVolumeDisplayStatus", deviceVolumeDisplayStatus)
                .toString();
    }
}
