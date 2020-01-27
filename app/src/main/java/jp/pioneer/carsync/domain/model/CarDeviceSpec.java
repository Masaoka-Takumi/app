package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

import java.util.EnumSet;
import java.util.Set;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 車載機スペック情報.
 */
@SuppressFBWarnings("UWF_NULL_FIELD")
public class CarDeviceSpec {
    /** Accessory ID. */
    public int accessoryId;
    /** 受信可能な最大文字列長. */
    public int maxCharLength;
    /** サポートソース群. */
    public Set<MediaSourceType> supportedSources;
    /** ADAS警告音対応. */
    public boolean adasAlarmSupported;
    /** リバースセンス対応. */
    public boolean reverseSenseSupported;
    /** パーキングセンス対応. */
    public boolean parkingSenseSupported;
    /** プリセットキー対応. */
    public boolean presetKeyEnabled;
    /** SiriusXMのTune Mix機能対応. */
    public boolean tuneMixSupported;
    /** DABのTimeShift機能対応. */
    public boolean timeShiftSupported;
    /** 車載機音声認識(Siri/GoogleVR)機能の対応. */
    public boolean androidVrSupported;
    /** Phone設定対応. */
    public boolean phoneSettingSupported;
    /** オーディオ設定（AC2）対応. */
    public boolean ac2AudioSettingSupported;
    /** オーディオ設定（JASPER）対応. */
    public boolean jasperAudioSettingSupported;
    /** Function設定対応. */
    public boolean functionSettingSupported;
    /** MIXTRAX設定対応. */
    public boolean mixtraxSettingSupported;
    /** イルミ設定対応. */
    public boolean illuminationSettingSupported;
    /** オーディオ設定（OPAL）対応. */
    public boolean audioSettingSupported;
    /** システム設定対応. */
    public boolean systemSettingSupported;
    /** Sound FX設定対応. */
    public boolean soundFxSettingSupported;
    /** 初期設定対応. */
    public boolean initialSettingSupported;
    /** ナビガイド音声設定対応. */
    public boolean naviGuideVoiceSettingSupported;
    /** パーキングセンサー設定対応. */
    public boolean parkingSensorSettingSupported;
    /** オーディオ設定スペック. */
    public final AudioSettingSpec audioSettingSpec = new AudioSettingSpec();
    /** BT Audio Function設定対応. */
    public boolean btAudioFunctionSettingSupported;
    /** DAB Function設定対応.*/
    public boolean dabFunctionSettingSupported;
    /** HD Radio Function設定対応. */
    public boolean hdRadioFunctionSettingSupported;
    /** Radio Function設定対応. */
    public boolean tunerFunctionSettingSupported;
    /** Tuner(Radio)Function設定スペック. */
    public TunerFunctionSettingSpec tunerFunctionSettingSpec = new TunerFunctionSettingSpec();
    /** HD Radio Function設定スペック. */
    public HdRadioFunctionSettingSpec hdRadioFunctionSettingSpec = new HdRadioFunctionSettingSpec();
    /** DAB Function設定スペック. */
    public DabFunctionSettingSpec dabFunctionSettingSpec = new DabFunctionSettingSpec();
    /** イルミ設定スペック. */
    public IlluminationSettingSpec illuminationSettingSpec = new IlluminationSettingSpec();
    /** BT Audio設定スペック. */
    public BtAudioSettingSpec btAudioSettingSpec = new BtAudioSettingSpec();
    /** 車種専用セッティング. */
    public CarModelSpecializedSetting carModelSpecializedSetting = new CarModelSpecializedSetting();
    /** パーキングセンサー設定スペック. */
    public ParkingSensorSettingSpec parkingSensorSettingSpec = new ParkingSensorSettingSpec();
    /** システム設定スペック. */
    public SystemSettingSpec systemSettingSpec = new SystemSettingSpec();
    /** 初期設定スペック. */
    public InitialSettingSpec initialSettingSpec = new InitialSettingSpec();
    /** Sound FX設定スペック. */
    public SoundFxSettingSpec soundFxSettingSpec = new SoundFxSettingSpec();
    /** 仕向け情報. */
    public CarDeviceDestinationInfo carDeviceDestinationInfo;
    /** 車載機型番. */
    public String modelName;
    /** BDアドレス. */
    public String bdAddress;

    /**
     * コンストラクタ.
     */
    public CarDeviceSpec() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        accessoryId = 0;
        maxCharLength = 0;
        supportedSources = EnumSet.of(MediaSourceType.APP_MUSIC);
        adasAlarmSupported = false;
        reverseSenseSupported = false;
        parkingSenseSupported = false;
        presetKeyEnabled = false;
        tuneMixSupported = false;
        timeShiftSupported = false;
        androidVrSupported = false;
        phoneSettingSupported = false;
        ac2AudioSettingSupported = false;
        jasperAudioSettingSupported = false;
        functionSettingSupported = false;
        mixtraxSettingSupported = false;
        illuminationSettingSupported = false;
        audioSettingSupported = false;
        systemSettingSupported = false;
        soundFxSettingSupported = false;
        initialSettingSupported = false;
        naviGuideVoiceSettingSupported = false;
        parkingSensorSettingSupported = false;
        audioSettingSpec.reset();
        btAudioFunctionSettingSupported = false;
        dabFunctionSettingSupported = false;
        hdRadioFunctionSettingSupported = false;
        tunerFunctionSettingSupported = false;
        tunerFunctionSettingSpec.reset();
        hdRadioFunctionSettingSpec.reset();
        dabFunctionSettingSpec.reset();
        illuminationSettingSpec.reset();
        btAudioSettingSpec.reset();
        carModelSpecializedSetting.reset();
        parkingSensorSettingSpec.reset();
        systemSettingSpec.reset();
        initialSettingSpec.reset();
        soundFxSettingSpec.reset();
        carDeviceDestinationInfo = null;
        modelName = null;
        bdAddress = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("accessoryId", String.format("0x%04X", accessoryId))
                .add("maxCharLength", maxCharLength)
                .add("supportedSources", supportedSources)
                .add("adasAlarmSupported", adasAlarmSupported)
                .add("reverseSenseSupported", reverseSenseSupported)
                .add("parkingSenseSupported", parkingSenseSupported)
                .add("presetKeyEnabled", presetKeyEnabled)
                .add("tuneMixSupported", tuneMixSupported)
                .add("timeShiftSupported", timeShiftSupported)
                .add("androidVrSupported", androidVrSupported)
                .add("phoneSettingSupported", phoneSettingSupported)
                .add("ac2AudioSettingSupported", ac2AudioSettingSupported)
                .add("jasperAudioSettingSupported", jasperAudioSettingSupported)
                .add("functionSettingSupported", functionSettingSupported)
                .add("mixtraxSettingSupported", mixtraxSettingSupported)
                .add("illuminationSettingSupported", illuminationSettingSupported)
                .add("audioSettingSupported", audioSettingSupported)
                .add("systemSettingSupported", systemSettingSupported)
                .add("soundFxSettingSupported", soundFxSettingSupported)
                .add("initialSettingSupported", initialSettingSupported)
                .add("naviGuideVoiceSettingSupported", naviGuideVoiceSettingSupported)
                .add("parkingSensorSettingSupported", parkingSensorSettingSupported)
                .add("audioSettingSpec", audioSettingSpec)
                .add("btAudioFunctionSettingSupported", btAudioFunctionSettingSupported)
                .add("dabFunctionSettingSupported", dabFunctionSettingSupported)
                .add("hdRadioFunctionSettingSupported", hdRadioFunctionSettingSupported)
                .add("tunerFunctionSettingSupported", tunerFunctionSettingSupported)
                .add("tunerFunctionSettingSpec", tunerFunctionSettingSpec)
                .add("hdRadioFunctionSettingSpec", hdRadioFunctionSettingSpec)
                .add("dabFunctionSettingSpec", dabFunctionSettingSpec)
                .add("illuminationSettingSpec", illuminationSettingSpec)
                .add("carModelSpecializedSetting", carModelSpecializedSetting)
                .add("parkingSensorSettingSpec", parkingSensorSettingSpec)
                .add("systemSettingSpec", systemSettingSpec)
                .add("initialSettingSpec", initialSettingSpec)
                .add("soundFxSettingSpec", soundFxSettingSpec)
                .add("carDeviceDestinationInfo", carDeviceDestinationInfo)
                .add("modelName", modelName)
                .add("bdAddress", bdAddress)
                .toString();
    }
}
