package jp.pioneer.carsync.infrastructure.crp.handler.devicespec;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.pioneer.carsync.domain.model.AudioOutputMode;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.DabFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.HdRadioFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.InitialSettingSpec;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.ParkingSensorSettingSpec;
import jp.pioneer.carsync.domain.model.PresetEqualizerVariation;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType;
import jp.pioneer.carsync.domain.model.SystemSettingSpec;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingSpec;
import jp.pioneer.carsync.infrastructure.crp.BadPacketException;
import jp.pioneer.carsync.infrastructure.crp.CarRemoteSession;
import jp.pioneer.carsync.infrastructure.crp.IncomingPacket;
import jp.pioneer.carsync.infrastructure.crp.handler.DataResponsePacketHandler;
import timber.log.Timber;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.model.MediaSourceType.*;
import static jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType.*;
import static jp.pioneer.carsync.domain.model.MenuDisplayLanguageType.*;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.checkPacketDataLength;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.getBitsValue;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.isBitOn;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ubyteToInt;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ushortToInt;

/**
 * 車載機Spec要求応答パケットハンドラ.
 * <p>
 * {@link StatusHolder}の値を更新するが、タスクで更新イベントを発行することを想定。
 */
public class DeviceSpecResponsePacketHandler extends DataResponsePacketHandler {
    private StatusHolder mStatusHolder;
    private static final Map<PresetEqualizerVariation, List<SoundFxSettingEqualizerType>> ENABLE_EQUALIZERS = new HashMap<PresetEqualizerVariation, List<SoundFxSettingEqualizerType>>() {{
        put(PresetEqualizerVariation.BRAZIL,
                Arrays.asList(SoundFxSettingEqualizerType.POP_ROCK, SoundFxSettingEqualizerType.ELECTRONICA, SoundFxSettingEqualizerType.EQ_SAMBA, SoundFxSettingEqualizerType.FORRO, SoundFxSettingEqualizerType.SERTANEJO,
                        SoundFxSettingEqualizerType.JAZZ, SoundFxSettingEqualizerType.PRO, SoundFxSettingEqualizerType.SPECIAL_DEBUG_1, SoundFxSettingEqualizerType.SPECIAL_DEBUG_2, SoundFxSettingEqualizerType.COMMON_CUSTOM, SoundFxSettingEqualizerType.FLAT));
        put(PresetEqualizerVariation.INDIA,
                Arrays.asList(SoundFxSettingEqualizerType.SUPER_BASS, SoundFxSettingEqualizerType.POWERFUL, SoundFxSettingEqualizerType.DYNAMIC, SoundFxSettingEqualizerType.TODOROKI, SoundFxSettingEqualizerType.VOCAL,
                        SoundFxSettingEqualizerType.VIVID, SoundFxSettingEqualizerType.SPECIAL_DEBUG_1, SoundFxSettingEqualizerType.SPECIAL_DEBUG_2, SoundFxSettingEqualizerType.COMMON_CUSTOM, SoundFxSettingEqualizerType.COMMON_CUSTOM_2ND, SoundFxSettingEqualizerType.FLAT));
        put(PresetEqualizerVariation.OTHER,
                Arrays.asList(SoundFxSettingEqualizerType.SUPER_BASS, SoundFxSettingEqualizerType.POWERFUL, SoundFxSettingEqualizerType.DYNAMIC, SoundFxSettingEqualizerType.NATURAL, SoundFxSettingEqualizerType.VOCAL,
                        SoundFxSettingEqualizerType.VIVID, SoundFxSettingEqualizerType.SPECIAL_DEBUG_1, SoundFxSettingEqualizerType.SPECIAL_DEBUG_2, SoundFxSettingEqualizerType.COMMON_CUSTOM, SoundFxSettingEqualizerType.COMMON_CUSTOM_2ND, SoundFxSettingEqualizerType.FLAT));
    }};

    /**
     * コンストラクタ.
     *
     * @param session CarRemoteSession
     * @throws NullPointerException {@code session}がnull
     */
    public DeviceSpecResponsePacketHandler(@NonNull CarRemoteSession session) {
        mStatusHolder = checkNotNull(session).getStatusHolder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(@NonNull IncomingPacket packet) throws Exception {
        try {
            byte[] data = packet.getData();

            int majorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().major;
            int minorVer = mStatusHolder.getProtocolSpec().getConnectingProtocolVersion().minor;
            checkPacketDataLength(data, getDataLength(majorVer));
            CarDeviceSpec carDeviceSpec = mStatusHolder.getCarDeviceSpec();
            byte b;

            // D1-D2:Accessory ID
            carDeviceSpec.accessoryId = ushortToInt(data, 1);
            // D3:受信可能な最大文字列長
            carDeviceSpec.maxCharLength = ubyteToInt(data[3]);
            // D4:サポートソース1
            b = data[4];
            Set<MediaSourceType> supportedSources = EnumSet.noneOf(MediaSourceType.class);
            addIfSupported(supportedSources, b, 7, BT_AUDIO);
            addIfSupported(supportedSources, b, 6, AUX);
            addIfSupported(supportedSources, b, 5, USB);
            addIfSupported(supportedSources, b, 4, CD);
            addIfSupported(supportedSources, b, 3, HD_RADIO);
            addIfSupported(supportedSources, b, 2, SIRIUS_XM);
            addIfSupported(supportedSources, b, 1, DAB);
            addIfSupported(supportedSources, b, 0, RADIO);
            // D5:サポートソース2
            b = data[5];
            addIfSupported(supportedSources, b, 6, TI);
            addIfSupported(supportedSources, b, 5, IPOD);
            addIfSupported(supportedSources, b, 4, APP_MUSIC);
            addIfSupported(supportedSources, b, 3, SPOTIFY);
            addIfSupported(supportedSources, b, 2, PANDORA);
            addIfSupported(supportedSources, b, 1, MediaSourceType.OFF);
            addIfSupported(supportedSources, b, 0, BT_PHONE);
            carDeviceSpec.supportedSources = supportedSources;
            // D6:サポートソース3
            //  (RESERVED)
            // D7:サポートソース4
            //  (RESERVED)
            // D8:サポート機能1
            b = data[8];
            carDeviceSpec.presetKeyEnabled = isBitOn(b, 0);
            // D9:サポート機能2
            //  (RESERVED)

            if (majorVer >= 2) {
                v2(data, carDeviceSpec);
            }

            if (majorVer >= 3) {
                v3(data, carDeviceSpec);
            }

            if (majorVer >= 4) {
                v4(data, carDeviceSpec);
                if (majorVer > 4 || minorVer >= 1) {
                    v4_1(data, carDeviceSpec);
                }
            }

            Timber.d("handle() CarDeviceSpec = " + carDeviceSpec);
            setResult(Boolean.TRUE);
        } catch (BadPacketException | IllegalArgumentException e) {
            Timber.e(e, "handle()");
            setResult(Boolean.FALSE);
        }
    }

    private void v2(byte[] data, CarDeviceSpec carDeviceSpec) {
        byte b;
        AudioSettingSpec audioSettingSpec = carDeviceSpec.audioSettingSpec;
        TunerFunctionSettingSpec tunerFunctionSettingSpec = carDeviceSpec.tunerFunctionSettingSpec;
        HdRadioFunctionSettingSpec hdRadioFunctionSettingSpec = carDeviceSpec.hdRadioFunctionSettingSpec;
        DabFunctionSettingSpec dabFunctionSettingSpec = carDeviceSpec.dabFunctionSettingSpec;
        IlluminationSettingSpec illuminationSettingSpec = carDeviceSpec.illuminationSettingSpec;

        // D10:ソース拡張機能1
        b = data[10];
        carDeviceSpec.tuneMixSupported = isBitOn(b, 1);
        carDeviceSpec.timeShiftSupported = isBitOn(b, 0);
        // D11:ソース拡張機能2
        //  (RESERVED)
        // D12:サポート設定
        b = data[12];
        carDeviceSpec.jasperAudioSettingSupported = isBitOn(b, 5);
        carDeviceSpec.functionSettingSupported = isBitOn(b, 4);
        carDeviceSpec.mixtraxSettingSupported = isBitOn(b, 3);
        carDeviceSpec.illuminationSettingSupported = isBitOn(b, 2);
        carDeviceSpec.audioSettingSupported = isBitOn(b, 1);
        carDeviceSpec.systemSettingSupported = isBitOn(b, 0);
        // D13:サポート設定2
        //  (RESERVED)
        // D14:サポートオーディオ設定1
        b = data[14];
        audioSettingSpec.listeningPositionSettingSupported = isBitOn(b, 7);
        audioSettingSpec.crossoverSettingSupported = isBitOn(b, 6);
        audioSettingSpec.speakerLevelSettingSupported = isBitOn(b, 5);
        audioSettingSpec.subwooferPhaseSettingSupported = isBitOn(b, 4);
        audioSettingSpec.subwooferSettingSupported = isBitOn(b, 3);
        audioSettingSpec.balanceSettingSupported = isBitOn(b, 2);
        audioSettingSpec.faderSettingSupported = isBitOn(b, 1);
        audioSettingSpec.equalizerSettingSupported = isBitOn(b, 0);
        // D15:サポートオーディオ設定2
        b = data[15];
        audioSettingSpec.slaSettingSupported = isBitOn(b, 7);
        audioSettingSpec.alcSettingSupported = isBitOn(b, 6);
        audioSettingSpec.loudnessSettingSupported = isBitOn(b, 5);
        audioSettingSpec.bassBoosterSettingSupported = isBitOn(b, 4);
        audioSettingSpec.loadSettingSupported = isBitOn(b, 3);
        audioSettingSpec.saveSettingSupported = isBitOn(b, 2);
        audioSettingSpec.aeqSettingSupported = isBitOn(b, 1);
        audioSettingSpec.timeAlignmentSettingSupported = isBitOn(b, 0);
        // D16:サポートオーディオ設定3
        b = data[16];
        audioSettingSpec.audioDataBulkUpdateSupported = isBitOn(b, 0);
        // D17:サポートオーディオ設定4
        //  (RESERVED)
        // D18;オーディオ設定その他1
        b = data[18];
        audioSettingSpec.loadSoundSettingSupported = isBitOn(b, 5);
        audioSettingSpec.loadAeqSettingSupported = isBitOn(b, 4);
        audioSettingSpec.timeAlignmentPresetAtaSupported = isBitOn(b, 3);
        audioSettingSpec.presetEqualizerVariation = PresetEqualizerVariation.valueOf(getBitsValue(b, 1, 2));
        audioSettingSpec.audioOutputMode = AudioOutputMode.valueOf(getBitsValue(b, 0, 1));
        // D19:オーディオ設定その他2
        //  (RESERVED)
        // D20:サポートFunction設定1
        b = data[20];
        carDeviceSpec.dabFunctionSettingSupported = isBitOn(b, 2);
        carDeviceSpec.hdRadioFunctionSettingSupported = isBitOn(b, 1);
        carDeviceSpec.tunerFunctionSettingSupported = isBitOn(b, 0);
        // D21:サポートFunction設定2
        //  (RESERVED)
        // D22:サポートFunction個別設定1 - Tuner
        b = data[22];
        tunerFunctionSettingSpec.alarmSettingSupported = isBitOn(b, 7);
        tunerFunctionSettingSpec.newsSettingSupported = isBitOn(b, 6);
        tunerFunctionSettingSpec.afSettingSupported = isBitOn(b, 5);
        tunerFunctionSettingSpec.taSettingSupported = isBitOn(b, 4);
        tunerFunctionSettingSpec.localSettingSupported = isBitOn(b, 3);
        tunerFunctionSettingSpec.regSettingSupported = isBitOn(b, 2);
        tunerFunctionSettingSpec.bsmSettingSupported = isBitOn(b, 1);
        tunerFunctionSettingSpec.fmSettingSupported = isBitOn(b, 0);
        // D23:サポートFunction個別設定2 - Tuner
        b = data[23];
        tunerFunctionSettingSpec.pchManualSupported = isBitOn(b, 0);
        // D24:サポートFunction個別設定3 - HD Radio
        b = data[24];
        hdRadioFunctionSettingSpec.activeRadioSettingSupported = isBitOn(b, 4);
        hdRadioFunctionSettingSpec.blendingSettingSupported = isBitOn(b, 3);
        hdRadioFunctionSettingSpec.hdSeekSettingSupported = isBitOn(b, 2);
        hdRadioFunctionSettingSpec.localSettingSupported = isBitOn(b, 1);
        hdRadioFunctionSettingSpec.bsmSettingSupported = isBitOn(b, 0);
        // D25:サポートFunction個別設定4 - DAB
        b = data[25];
        dabFunctionSettingSpec.taSettingSupported = isBitOn(b, 2);
        dabFunctionSettingSpec.serviceFollowSettingSupported = isBitOn(b, 1);
        dabFunctionSettingSpec.softlinkSettingSupported = isBitOn(b, 0);
        // D26:サポートイルミ設定1
        b = data[26];
        illuminationSettingSpec.hotaruNoHikariLikeSettingSupported = isBitOn(b, 7);
        illuminationSettingSpec.btPhoneColorSettingSupported = isBitOn(b, 6);
        illuminationSettingSpec.brightnessSettingSupported = isBitOn(b, 5);
        illuminationSettingSpec.dimmerSettingSupported = isBitOn(b, 4);
        illuminationSettingSpec.colorCustomDispSettingSupported = isBitOn(b, 3);
        illuminationSettingSpec.colorCustomKeySettingSupported = isBitOn(b, 2);
        illuminationSettingSpec.dispColorSettingSupported = isBitOn(b, 1);
        illuminationSettingSpec.keyColorSettingSupported = isBitOn(b, 0);
        // D27:サポートイルミ設定2
        //  (RESERVED)
    }

    private void v3(byte[] data, CarDeviceSpec carDeviceSpec) {
        byte b;
        AudioSettingSpec audioSettingSpec = carDeviceSpec.audioSettingSpec;
        IlluminationSettingSpec illuminationSettingSpec = carDeviceSpec.illuminationSettingSpec;

        // D12:サポート設定
        b = data[12];
        carDeviceSpec.phoneSettingSupported = isBitOn(b, 7);
        carDeviceSpec.ac2AudioSettingSupported = isBitOn(b, 6);
        // D16
        b = data[16];
        audioSettingSpec.levelSettingSupported = isBitOn(b, 2);
        audioSettingSpec.beatBlasterSettingSupported = isBitOn(b, 1);
        // D20
        b = data[20];
        carDeviceSpec.btAudioFunctionSettingSupported = isBitOn(b, 3);
        // D27
        b = data[27];
        illuminationSettingSpec.dispBrightnessSettingSupported = isBitOn(b, 1);
        illuminationSettingSpec.keyBrightnessSettingSupported = isBitOn(b, 0);
        // D28
        b = data[28];
        carDeviceSpec.btAudioSettingSpec.audioDeviceSelectSupported = isBitOn(b, 0);
        // D29
        b = data[29];
        carDeviceSpec.carModelSpecializedSetting.illumiColorSettingSupported = isBitOn(b, 1);
        carDeviceSpec.carModelSpecializedSetting.audioSettingSupported = isBitOn(b, 0);
    }

    private void v4(byte[] data, CarDeviceSpec carDeviceSpec) {
        byte b;
        TunerFunctionSettingSpec tunerFunctionSettingSpec = carDeviceSpec.tunerFunctionSettingSpec;
        IlluminationSettingSpec illuminationSettingSpec = carDeviceSpec.illuminationSettingSpec;
        ParkingSensorSettingSpec parkingSensorSettingSpec = carDeviceSpec.parkingSensorSettingSpec;
        SystemSettingSpec systemSettingSpec = carDeviceSpec.systemSettingSpec;
        InitialSettingSpec initialSettingSpec = carDeviceSpec.initialSettingSpec;
        SoundFxSettingSpec soundFxSettingSpec = carDeviceSpec.soundFxSettingSpec;
        AudioSettingSpec audioSettingSpec = carDeviceSpec.audioSettingSpec;

        // D8:サポート機能1
        b = data[8];
        carDeviceSpec.adasAlarmSupported = isBitOn(b, 3);
        carDeviceSpec.reverseSenseSupported = isBitOn(b, 2);
        carDeviceSpec.parkingSenseSupported = isBitOn(b, 1);
        // D13:サポート設定2
        b = data[13];
        carDeviceSpec.soundFxSettingSupported = isBitOn(b, 3);
        carDeviceSpec.initialSettingSupported = isBitOn(b, 2);
        carDeviceSpec.naviGuideVoiceSettingSupported = isBitOn(b, 1);
        carDeviceSpec.parkingSensorSettingSupported = isBitOn(b, 0);
        // D16:サポートオーディオ設定3
        b = data[16];
        audioSettingSpec.soundRetrieverSettingSupported = isBitOn(b, 3);
        // D23:サポートFunction個別設定2 - Tuner
        b = data[23];
        tunerFunctionSettingSpec.ptySearchSettingSupported = isBitOn(b, 1);
        // D27:サポートイルミ設定2
        b = data[27];
        illuminationSettingSpec.incomingMessageColorSettingSupported = isBitOn(b, 7);
        illuminationSettingSpec.commonColorCustomSettingSupported = isBitOn(b, 6);
        illuminationSettingSpec.commonColorSettingSupported = isBitOn(b, 5);
        illuminationSettingSpec.sphBtPhoneColorSettingSupported = isBitOn(b, 4);
        illuminationSettingSpec.audioLevelMeterLinkedSettingSupported = isBitOn(b, 3);
        illuminationSettingSpec.customFlashPatternSettingSupported = isBitOn(b, 2);
        // D30:サポートパーキングセンサー設定
        b = data[30];
        parkingSensorSettingSpec.backPolaritySettingSupported = isBitOn(b, 2);
        parkingSensorSettingSpec.alarmOutputDestinationSettingSupported = isBitOn(b, 1);
        parkingSensorSettingSpec.alarmVolumeSettingSupported = isBitOn(b, 0);
        // D31:サポートシステム設定1
        b = data[31];
        systemSettingSpec.auxSettingSupported = isBitOn(b, 7);
        systemSettingSpec.spotifySettingSupported = isBitOn(b, 6);
        systemSettingSpec.pandoraSettingSupported = isBitOn(b, 5);
        systemSettingSpec.btAudioSettingSupported = isBitOn(b, 4);
        systemSettingSpec.powerSaveSettingSupported = isBitOn(b, 3);
        systemSettingSpec.demoSettingSupported = isBitOn(b, 2);
        systemSettingSpec.attMuteSettingSupported = isBitOn(b, 1);
        systemSettingSpec.beepToneSettingSupported = isBitOn(b, 0);
        // D32:サポートシステム設定2
        b = data[32];
        systemSettingSpec.distanceUnitSettingSupported = isBitOn(b, 5);
        systemSettingSpec.displayOffSettingSupported = isBitOn(b, 4);
        systemSettingSpec.autoPiSettingSupported = isBitOn(b, 3);
        systemSettingSpec.steeringRemoteControlSettingSupported = isBitOn(b, 2);
        systemSettingSpec.usbAutoSettingSupported = isBitOn(b, 1);
        systemSettingSpec.appAutoStartSettingSupported = isBitOn(b, 0);
        // D33:サポートステアリングリモコン設定1
        b = data[33];
        EnumSet<SteeringRemoteControlSettingType> supportedSteeringRemoteControlSettings = EnumSet.noneOf(SteeringRemoteControlSettingType.class);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 7, SUBARU4);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 6, SUBARU3);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 5, SUBARU2);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 4, SUBARU1);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 3, SUZUKI);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 2, HONDA);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 1, TOYOTA);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 0, PIONEER);
        // D34:サポートステアリングリモコン設定2
        b = data[34];
        addIfSupported(supportedSteeringRemoteControlSettings, b, 7, DAIHATSU);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 6, HYUNDAI);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 5, MITSUBISHI);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 4, NISSAN4);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 3, NISSAN3);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 2, NISSAN2);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 1, NISSAN1);
        addIfSupported(supportedSteeringRemoteControlSettings, b, 0, MAZDA);
        systemSettingSpec.supportedSteeringRemoteControlSettings = supportedSteeringRemoteControlSettings;
        // D35:サポートステアリングリモコン設定3
        //  (RESERVED)
        // D36:サポート初期設定1
        b = data[36];
        initialSettingSpec.menuDisplayLanguageSettingSupported = isBitOn(b, 4);
        initialSettingSpec.rearOutputSettingSupported = isBitOn(b, 3);
        initialSettingSpec.rearOutputPreoutOutputSettingSupported = isBitOn(b, 2);
        initialSettingSpec.amStepSettingSupported = isBitOn(b, 1);
        initialSettingSpec.fmStepSettingSupported = isBitOn(b, 0);
        // D37:サポート初期設定2
        //  (RESERVED)
        // D38:サポートMENU表示言語設定1
        b = data[38];
        EnumSet<MenuDisplayLanguageType> supportedMenuDisplayLanguages = EnumSet.noneOf(MenuDisplayLanguageType.class);
        addIfSupported(supportedMenuDisplayLanguages, b, 7, CANADIAN_FRENCH);
        addIfSupported(supportedMenuDisplayLanguages, b, 6, SOUTH_AMERICAN_SPANISH);
        addIfSupported(supportedMenuDisplayLanguages, b, 5, GERMAN);
        addIfSupported(supportedMenuDisplayLanguages, b, 4, FRENCH);
        addIfSupported(supportedMenuDisplayLanguages, b, 3, TURKISH);
        addIfSupported(supportedMenuDisplayLanguages, b, 2, RUSSIAN);
        addIfSupported(supportedMenuDisplayLanguages, b, 1, BRAZIL_PORTUGUESE);
        addIfSupported(supportedMenuDisplayLanguages, b, 0, ENGLISH);
        initialSettingSpec.supportedMenuDisplayLanguages = supportedMenuDisplayLanguages;
        // D39:サポートMENU表示言語設定2
        //  (RESERVED)
        // D40:サポートSound FX設定1
        b = data[40];
        soundFxSettingSpec.karaokeSettingSupported = isBitOn(b, 3);
        soundFxSettingSpec.liveSimulationSettingSupported = isBitOn(b, 2);
        soundFxSettingSpec.smallCarTaSettingSupported = isBitOn(b, 1);
        soundFxSettingSpec.superTodorokiSettingSupported = isBitOn(b, 0);
        // D41:サポートSound FX設定2
        //  (RESERVED)
        // D42:サポートDIMMER設定
        b = data[42];
        EnumSet<DimmerSetting.Dimmer> supportedDimmers = EnumSet.noneOf(DimmerSetting.Dimmer.class);
        addIfSupported(supportedDimmers, b, 4, DimmerSetting.Dimmer.MANUAL);
        addIfSupported(supportedDimmers, b, 3, DimmerSetting.Dimmer.OFF);
        addIfSupported(supportedDimmers, b, 2, DimmerSetting.Dimmer.ON);
        addIfSupported(supportedDimmers, b, 1, DimmerSetting.Dimmer.ILLUMI_LINE);
        addIfSupported(supportedDimmers, b, 0, DimmerSetting.Dimmer.SYNC_CLOCK);
        illuminationSettingSpec.supportedDimmers = supportedDimmers;

        // 有効イコライザー
        soundFxSettingSpec.supportedEqualizers = ENABLE_EQUALIZERS.get(audioSettingSpec.presetEqualizerVariation);
    }

    private void v4_1(byte[] data, CarDeviceSpec carDeviceSpec) {
        byte b;
        // D10:ソース拡張機能1
        b = data[10];
        carDeviceSpec.androidVrSupported = isBitOn(b, 2);
    }

    private void addIfSupported(Set<MediaSourceType> sources, byte b, int bit, MediaSourceType type) {
        if (isBitOn(b, bit)) {
            sources.add(type);
        }
    }

    private void addIfSupported(Set<SteeringRemoteControlSettingType> controllers, byte b, int bit, SteeringRemoteControlSettingType type) {
        if (isBitOn(b, bit)) {
            controllers.add(type);
        }
    }

    private void addIfSupported(Set<MenuDisplayLanguageType> languages, byte b, int bit, MenuDisplayLanguageType type) {
        if (isBitOn(b, bit)) {
            languages.add(type);
        }
    }

    private void addIfSupported(Set<DimmerSetting.Dimmer> dimmers, byte b, int bit, DimmerSetting.Dimmer dimmer) {
        if (isBitOn(b, bit)) {
            dimmers.add(dimmer);
        }
    }

    /**
     * データ長取得.
     * <p>
     * メジャーバージョンからそれに対応したデータ長を取得する
     * アップデートによりデータ長が変更された場合は本メソッドに追加する
     * <p>
     * 対応したバージョンが存在しない場合は、
     * アップデートされたがデータ長は変更されていないと判断し、
     * 最大のデータ長を返す
     *
     * @param version メジャーバージョン
     * @return データ長
     */
    private int getDataLength(int version) {
        final int V2_DATA_LENGTH = 28;
        final int V3_DATA_LENGTH = 30;
        final int V4_DATA_LENGTH = 43;
        final int MAX_DATA_LENGTH = Math.max(Math.max(V2_DATA_LENGTH, V3_DATA_LENGTH), V4_DATA_LENGTH);

        switch(version){
            case 1:
            case 2:
                return V2_DATA_LENGTH;
            case 3:
                return V3_DATA_LENGTH;
            case 4:
                return V4_DATA_LENGTH;
            default:
                return MAX_DATA_LENGTH;
        }
    }
}
