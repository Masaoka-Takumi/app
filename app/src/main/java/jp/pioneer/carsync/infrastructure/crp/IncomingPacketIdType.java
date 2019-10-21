package jp.pioneer.carsync.infrastructure.crp;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.infrastructure.crp.handler.PacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.SimpleResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.auth.ClassIdRequestResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.auth.ProtocolVersionResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.DisconnectNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.ExitMenuNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.FinishVoiceRecognitionNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.ListFocusPositionChangeResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.ListFocusPositionNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.ListUpdateNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.RotaryKeyNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.SettingListUpdatedNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.SmartPhoneAppStartCommandPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.SmartPhoneControlCommandNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.SmartPhoneMediaCommandPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.controlcommand.VoiceRecognitionResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.AbcSearchDisplayNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.DeviceAudioInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.DeviceAudioPlaybackPositionNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.DeviceDabInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.DeviceHdRadioInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.DeviceRadioInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.DeviceSxmChannelNumberNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.DeviceTunerBufferNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.InitialListInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.InitialSettingListInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.ListInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.PairingDeviceInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.PairingDeviceListInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.deviceinfo.SettingListInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicespec.DeviceBdAddressResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicespec.DeviceModelResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicespec.DeviceSpecResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.AudioSettingStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.AudioSettingStatusResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.BtAudioStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.CdStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.DabStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.DeviceStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.DeviceStatusResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.FunctionSettingStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.FunctionSettingStatusResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.HdRadioStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.IPodStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.IlluminationSettingStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.IlluminationSettingStatusResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.InitialSettingStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.InitialSettingStatusResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.MediaStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.PandoraStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.ParkingSensorSettingStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.ParkingSensorSettingStatusResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.ParkingSensorStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.PhoneSettingStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.PhoneSettingStatusResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.RadioStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.SoundFxSettingStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.SoundFxSettingStatusResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.SpotifyStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.SxmStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.SystemSettingStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.SystemSettingStatusResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.TunerStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.devicestatus.UsbStatusNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.errornotification.DeviceErrorNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.errornotification.SmartPhoneErrorNotificationResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.interruptinfo.DeviceInterruptNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.Ac2EqualizerSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.Ac2EqualizerSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.AlcSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.AlcSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.AutoEqCorrectionSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.AutoEqCorrectionSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.BassBoosterLevelSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.BassBoosterLevelSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.BeatBlasterLevelSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.BeatBlasterLevelSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.CrossoverSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.CrossoverSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.CustomEqualizerSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.EqualizerSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.EqualizerSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.FaderBalanceSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.FaderBalanceSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.JasperCrossoverSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.JasperCrossoverSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.JasperEqualizerSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.JasperEqualizerSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.LevelSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.LevelSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.ListeningPositionSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.ListeningPositionSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.LoudnessSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.LoudnessSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SlaSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SlaSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SoundRetrieverSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SoundRetrieverSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SpeakerLevelSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SpeakerLevelSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SubwooferPhaseSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SubwooferPhaseSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SubwooferSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.SubwooferSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.TimeAlignmentSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.audio.TimeAlignmentSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.function.FunctionSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.function.FunctionSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.AudioLevelMeterLinkedSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.AudioLevelMeterLinkedSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.BrightnessSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.BrightnessSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.BtPhoneColorSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.BtPhoneColorSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.CommonColorBulkSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.CommonColorSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.CommonColorSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.CommonCustomColorSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.CustomColorSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.DimmerSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.DimmerSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.DispColorBulkSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.DispColorSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.DispColorSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.DisplayBrightnessSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.DisplayBrightnessSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.IlluminationSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.IlluminationSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.IncomingMessageColorSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.IncomingMessageColorSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.KeyBrightnessSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.KeyBrightnessSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.KeyColorBulkSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.KeyColorSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.KeyColorSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.SphBtPhoneColorSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.illumi.SphBtPhoneColorSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.AmStepSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.AmStepSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.FmStepSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.FmStepSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.MenuDisplayLanguageSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.MenuDisplayLanguageSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.RearOutputPreoutOutputSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.RearOutputPreoutOutputSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.RearOutputSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.initial.RearOutputSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.naviguidevoice.NaviGuideVoiceSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.naviguidevoice.NaviGuideVoiceSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.naviguidevoice.NaviGuideVoiceVolumeSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.naviguidevoice.NaviGuideVoiceVolumeSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor.AlarmOutputDestinationSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor.AlarmOutputDestinationSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor.AlarmVolumeSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor.AlarmVolumeSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor.BackPolaritySettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor.BackPolaritySettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor.ParkingSensorSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.parkingsensor.ParkingSensorSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.phone.AutoAnswerSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.phone.AutoAnswerSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.phone.AutoPairingSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.phone.AutoPairingSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.KaraokeSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.KaraokeSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.LiveSimulationSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.LiveSimulationSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.MicVolumeSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.MiceVolumeSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.SmallCarTaSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.SmallCarTaSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.SuperTodorokiSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.SuperTodorokiSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.VocalCancelSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.soundfx.VocalCancelSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.AppAutoStartSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.AppAutoStartSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.AttMuteSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.AttMuteSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.AutoPiSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.AutoPiSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.AuxSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.AuxSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.BeepToneSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.BeepToneSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.BtAudioSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.BtAudioSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.DemoSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.DemoSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.DisplayOffSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.DisplayOffSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.DistanceUnitSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.DistanceUnitSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.PandoraSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.PandoraSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.PowerSaveSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.PowerSaveSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.SpotifySettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.SpotifySettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.SteeringRemoteControlSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.SteeringRemoteControlSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.UsbAutoSettingInfoNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.setting.system.UsbAutoSettingInfoResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.AudioDeviceSwitchCommandResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.AudioDeviceSwitchCompleteNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.CustomFlashPatternNotificationResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.LoadSettingNotificationResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.PairingAddCommandCompleteNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.PairingAddCommandResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.PairingDeleteCommandCompleteNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.PairingDeleteCommandResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.PhoneSearchCommandCompleteNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.PhoneSearchCommandResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.PhoneServiceConnectCommandResponsePacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.settingcommand.PhoneServiceConnectCompleteNotificationPacketHandler;
import jp.pioneer.carsync.infrastructure.crp.handler.smartphonestatus.SmartPhoneStatusRequestPacketHandler;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.model.ProtocolVersion.V1;
import static jp.pioneer.carsync.domain.model.ProtocolVersion.V2;
import static jp.pioneer.carsync.domain.model.ProtocolVersion.V3;
import static jp.pioneer.carsync.domain.model.ProtocolVersion.V4;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.toHex;

/**
 * 受信パケットIDタイプ.
 * <p>
 * CarRemote Protocolのパケットは、
 * <ul>
 *     <li>ID（コマンドの大分類）</li>
 *     <li>Type（IDより下位のコマンド小分類）</li>
 *     <li>D0（通信の種別）</li>
 * </ul>
 * でコマンドを特定可能となっているため、個々のコマンドを列挙値に分類している。
 *
 * @see OutgoingPacketIdType
 */
public enum IncomingPacketIdType {
    //////// ベース:認証 ////////
    /** 認証開始応答. */
    START_INITIAL_AUTH_RESPONSE(0x01, 0x00, 0xC0, SimpleResponsePacketHandler.class, null),
    /** 認証終了応答. */
    END_INITIAL_AUTH_RESPONSE(0x01, 0x00, 0xC1, SimpleResponsePacketHandler.class, null),
    /** Class ID応答. */
    CLASS_ID_REQUEST_RESPONSE(0x01, 0x01, 0xE0, ClassIdRequestResponsePacketHandler.class, null),
    /** Protocol Version応答. */
    PROTOCOL_VERSION_RESPONSE(0x01, 0x02, 0xE0, ProtocolVersionResponsePacketHandler.class, null),
    /** Protocol Version通知応答. */
    PROTOCOL_VERSION_NOTIFICATION_RESPONSE(0x01, 0x02, 0xC0, SimpleResponsePacketHandler.class, null),
    /** 初期通信開始応答. */
    START_INITIAL_COMM_RESPONSE(0x01, 0x10, 0xC0, SimpleResponsePacketHandler.class, null),
    /** 初期通信終了応答. */
    END_INITIAL_COMM_RESPONSE(0x01, 0x10, 0xC1, SimpleResponsePacketHandler.class, null),
    /** 車載機情報取得開始応答. */
    START_GET_DEVICE_SPEC_RESPONSE(0x01, 0x11, 0xC0, SimpleResponsePacketHandler.class, null),
    /** 車載機情報取得終了応答. */
    END_GET_DEVICE_SPEC_RESPONSE(0x01, 0x11, 0xC1, SimpleResponsePacketHandler.class, null),
    /** SmartPhone情報通知開始応答. */
    START_SEND_SMART_PHONE_SPEC_RESPONSE(0x01, 0x12, 0xC0, SimpleResponsePacketHandler.class, null),
    /** SmartPhone情報通知終了応答. */
    END_SEND_SMART_PHONE_SPEC_RESPONSE(0x01, 0x12, 0xC1, SimpleResponsePacketHandler.class, null),
    /** セッション開始応答. */
    START_SESSION_RESPONSE(0x01, 0x20, 0xC0, SimpleResponsePacketHandler.class, null),

    //////// ベース:車載機情報 ////////
    /** 車載機Spec応答. */
    DEVICE_SPEC_RESPONSE(0x03, 0x00, 0xE0, DeviceSpecResponsePacketHandler.class, V1),
    /** 車載機型番応答. */
    DEVICE_MODEL_RESPONSE(0x03, 0x01, 0xE0, DeviceModelResponsePacketHandler.class, V2),
    /** 車載機BDアドレス応答. */
    DEVICE_BD_ADDRESS_RESPONSE(0x03, 0x01, 0xE1, DeviceBdAddressResponsePacketHandler.class, V3),

    //////// ベース:SmartPhone情報 ////////
    /** SmartPhoneSpec通知応答. */
    SMART_PHONE_SPEC_NOTIFICATION_RESPONSE(0x05, 0x00, 0xC0, SimpleResponsePacketHandler.class, V1),
    /** 時刻情報通知応答. */
    TIME_NOTIFICATION_RESPONSE(0x05, 0x01, 0xC0, SimpleResponsePacketHandler.class, V2),

    //////// ステータス:車載機ステータス ////////
    /** 車載機ステータス情報通知. */
    DEVICE_STATUS_NOTIFICATION(0x11, 0x00, 0x00, DeviceStatusNotificationPacketHandler.class, V1),
    /** 車載機ステータス情報通知応答. */
    DEVICE_STATUS_RESPONSE(0x11, 0x00, 0xE0, DeviceStatusResponsePacketHandler.class, V1),
    /** Tuner系共通ステータス情報通知. */
    TUNER_STATUS_NOTIFICATION(0x11, 0x01, 0x00, TunerStatusNotificationPacketHandler.class, V2),
    /** Radioステータス情報通知. */
    RADIO_STATUS_NOTIFICATION(0x11, 0x01, 0x01, RadioStatusNotificationPacketHandler.class, V2),
    /** DABステータス情報通知. */
    DAB_STATUS_NOTIFICATION(0x11, 0x01, 0x02, DabStatusNotificationPacketHandler.class, V2),
    /** SiriusXMステータス情報通知. */
    SXM_STATUS_NOTIFICATION(0x11, 0x01, 0x03, SxmStatusNotificationPacketHandler.class, V2),
    /** HD Radioステータス情報通知. */
    HD_RADIO_STATUS_NOTIFICATION(0x11, 0x01, 0x04, HdRadioStatusNotificationPacketHandler.class, V2),
    /** メディア系共通ステータス情報通知. */
    MEDIA_STATUS_NOTIFICATION(0x11, 0x01, 0x10, MediaStatusNotificationPacketHandler.class, V2),
    /** CDステータス情報通知. */
    CD_STATUS_NOTIFICATION(0x11, 0x01, 0x11, CdStatusNotificationPacketHandler.class, V2),
    /** USBステータス情報通知. */
    USB_STATUS_NOTIFICATION(0x11, 0x01, 0x12, UsbStatusNotificationPacketHandler.class, V2),
    /** BT Audioステータス情報通知. */
    BT_AUDIO_STATUS_NOTIFICATION(0x11, 0x01, 0x13, BtAudioStatusNotificationPacketHandler.class, V2),
    /** Pandoraステータス情報通知. */
    PANDORA_STATUS_NOTIFICATION(0x11, 0x01, 0x14, PandoraStatusNotificationPacketHandler.class, V2),
    /** Spotifyステータス情報通知. */
    SPOTIFY_STATUS_NOTIFICATION(0x11, 0x01, 0x15, SpotifyStatusNotificationPacketHandler.class, V2),
    /** iPodステータス情報通知. */
    IPOD_STATUS_NOTIFICATION(0x11, 0x01, 0x16, IPodStatusNotificationPacketHandler.class, V2),
    /** システム設定ステータス情報通知. */
    SYSTEM_SETTING_STATUS_NOTIFICATION(0x11, 0x02, 0x00, SystemSettingStatusNotificationPacketHandler.class, V4),
    /** システム設定ステータス情報応答. */
    SYSTEM_SETTING_STATUS_RESPONSE(0x11, 0x02, 0xE0, SystemSettingStatusResponsePacketHandler.class, V4),
    /** オーディオ設定ステータス情報通知. */
    AUDIO_SETTING_STATUS_NOTIFICATION(0x11, 0x02, 0x01, AudioSettingStatusNotificationPacketHandler.class, V2),
    /** オーディオ設定ステータス情報応答. */
    AUDIO_SETTING_STATUS_RESPONSE(0x11, 0x02, 0xE1, AudioSettingStatusResponsePacketHandler.class, V2),
    /** イルミ設定ステータス情報通知. */
    ILLUMINATION_SETTING_STATUS_NOTIFICATION(0x11, 0x02, 0x02, IlluminationSettingStatusNotificationPacketHandler.class, V2),
    /** イルミ設定ステータス情報応答. */
    ILLUMINATION_SETTING_STATUS_RESPONSE(0x11, 0x02, 0xE2, IlluminationSettingStatusResponsePacketHandler.class, V2),
    /** Function設定ステータス情報通知. */
    FUNCTION_SETTING_STATUS_NOTIFICATION(0x11, 0x02, 0x04, FunctionSettingStatusNotificationPacketHandler.class, V2),
    /** Function設定ステータス情報応答. */
    FUNCTION_SETTING_STATUS_RESPONSE(0x11, 0x02, 0xE4, FunctionSettingStatusResponsePacketHandler.class, V2),
    /** Phone設定ステータス情報通知. */
    PHONE_SETTING_STATUS_NOTIFICATION(0x11, 0x02, 0x05, PhoneSettingStatusNotificationPacketHandler.class, V3),
    /** Phone設定ステータス情報応答. */
    PHONE_SETTING_STATUS_RESPONSE(0x11, 0x02, 0xE5, PhoneSettingStatusResponsePacketHandler.class, V3),
    /** パーキングセンサー設定ステータス情報通知. */
    PARKING_SENSOR_SETTING_STATUS_NOTIFICATION(0x11, 0x02, 0x06, ParkingSensorSettingStatusNotificationPacketHandler.class, V4),
    /** パーキングセンサー設定ステータス情報応答. */
    PARKING_SENSOR_SETTING_STATUS_RESPONSE(0x11, 0x02, 0xE6, ParkingSensorSettingStatusResponsePacketHandler.class, V4),
    /** 初期設定ステータス情報通知. */
    INITIAL_SETTING_STATUS_NOTIFICATION(0x11, 0x02, 0x07, InitialSettingStatusNotificationPacketHandler.class, V4),
    /** 初期設定ステータス情報応答. */
    INITIAL_SETTING_STATUS_RESPONSE(0x11, 0x02, 0xE7, InitialSettingStatusResponsePacketHandler.class, V4),
    /** Sound FX設定ステータス情報通知. */
    SOUND_FX_SETTING_STATUS_NOTIFICATION(0x11, 0x02, 0x08, SoundFxSettingStatusNotificationPacketHandler.class, V4),
    /** Sound FX設定ステータス情報応答. */
    SOUND_FX_SETTING_STATUS_RESPONSE(0x11, 0x02, 0xE8, SoundFxSettingStatusResponsePacketHandler.class, V4),
    /** パーキングセンサーステータス情報通知. */
    PARKING_SENSOR_STATUS_NOTIFICATION(0x11, 0x03, 0x00, ParkingSensorStatusNotificationPacketHandler.class, V4),

    //////// ステータス:SmartPhoneステータス ////////
    /** SmartPhoneステータス情報（初回）通知応答. */
    SMART_PHONE_STATUS_NOTIFICATION_RESPONSE(0x13, 0x00, 0xC0, SimpleResponsePacketHandler.class, V1),
    /** SmartPhoneステータス情報要求. */
    SMART_PHONE_STATUS_REQUEST(0x13, 0x00, 0x80, SmartPhoneStatusRequestPacketHandler.class, V1),

    //////// ステータス:エラー通知 ////////
    /** SmartPhoneエラー通知応答. */
    SMART_PHONE_ERROR_NOTIFICATION_RESPONSE(0x1F, 0x00, 0xC0, SmartPhoneErrorNotificationResponsePacketHandler.class, V1),
    /** 車載機エラー通知. */
    DEVICE_ERROR_NOTIFICATION(0x1F, 0x01, 0x00, DeviceErrorNotificationPacketHandler.class, V1),

    //////// コマンド:制御コマンド ////////
    /** ロータリーキー通知. */
    ROTARY_KEY_NOTIFICATION(0x21, 0x00, 0x00, RotaryKeyNotificationPacketHandler.class, V2),
    /** SmartPhone操作コマンド通知. */
    SMART_PHONE_CONTROL_COMMAND_NOTIFICATION(0x21, 0x00, 0x01, SmartPhoneControlCommandNotificationPacketHandler.class, V2),
    /** メニュー表示解除通知. */
    EXIT_MENU_NOTIFICATION(0x21, 0x00, 0x02, ExitMenuNotificationPacketHandler.class, V4),
    /** 音声認識終了通知. */
    FINISH_VOICE_RECOGNITION_NOTIFICATION(0x21, 0x00, 0x03, FinishVoiceRecognitionNotificationPacketHandler.class, V4),
    /** 音声認識通知応答. */
    VOICE_RECOGNITION_NOTIFICATION_RESPONSE(0x21, 0x00, 0xC9, VoiceRecognitionResponsePacketHandler.class, V4),
    /** メディアコマンド通知. */
    SMART_PHONE_MEDIA_COMMAND(0x21, 0x01, 0x00, SmartPhoneMediaCommandPacketHandler.class, V1),
    /**
     * App起動コマンド通知.
     * <p>
     * 認証を行う前（プロトコルバージョン確定前）に通知が来るので、supportVersionをnullにする。
     */
    SMART_PHONE_APP_START_COMMAND(0x21, 0x02, 0x00, SmartPhoneAppStartCommandPacketHandler.class, null),
    /** フォーカス位置通知. */
    LIST_FOCUS_POSITION_NOTIFICATION(0x21, 0x03, 0x00, ListFocusPositionNotificationPacketHandler.class, V2),
    /** フォーカス位置変更応答. */
    LIST_FOCUS_POSITION_CHANGE_RESPONSE(0x21, 0x03, 0xE0, ListFocusPositionChangeResponsePacketHandler.class, V2),
    /** リスト情報更新通知. */
    LIST_UPDATE_NOTIFICATION(0x21, 0x03, 0x01, ListUpdateNotificationPacketHandler.class, V2),
    /** 設定リスト情報更新通知. */
    SETTING_LIST_UPDATED_NOTIFICATION(0x21, 0x03, 0x02, SettingListUpdatedNotificationPacketHandler.class, V3),
    /** 連携切断通知. */
    DISCONNECT_NOTIFICATION(0x21, 0xF0, 0x00, DisconnectNotificationPacketHandler.class, V2),

    //////// コマンド:設定コマンド ////////
    /** SAVE SETTING実行応答. */
    SAVE_SETTING_NOTIFICATION_RESPONSE(0x23, 0x01, 0xC0, SimpleResponsePacketHandler.class, V2),
    /** LOAD SETTING実行応答. */
    LOAD_SETTING_NOTIFICATION_RESPONSE(0x23, 0x01, 0xC1, LoadSettingNotificationResponsePacketHandler.class, V2),
    /** オーディオ一括設定通知応答. */
    AUDIO_BULK_SETTING_NOTIFICATION_RESPONSE(0x23, 0x01, 0x01, SimpleResponsePacketHandler.class, V2),
    /** CUSTOM発光パターン設定通知応答. */
    CUSTOM_FLASH_PATTERN_SETTING_NOTIFICATION_RESPONSE(0x23, 0x02, 0xC0, CustomFlashPatternNotificationResponsePacketHandler.class, V4),
    /** Audio Device 切替コマンド通知応答. */
    AUDIO_DEVICE_SWITCH_COMMAND_RESPONSE(0x23, 0x04, 0xC1, AudioDeviceSwitchCommandResponsePacketHandler.class, V3),
    /** Audio Device 切替完了通知. */
    AUDIO_DEVICE_SWITCH_COMPLETE_NOTIFICATION(0x23, 0x04, 0x01, AudioDeviceSwitchCompleteNotificationPacketHandler.class, V3),
    /** サーチコマンド通知応答. */
    PHONE_SEARCH_COMMAND_RESPONSE(0x23, 0x05, 0xC0, PhoneSearchCommandResponsePacketHandler.class, V3),
    /** ペアリング追加コマンド通知応答. */
    PAIRING_ADD_COMMAND_RESPONSE(0x23, 0x05, 0xC1, PairingAddCommandResponsePacketHandler.class, V3),
    /** ペアリング削除コマンド通知応答. */
    PAIRING_DELETE_COMMAND_RESPONSE(0x23, 0x05, 0xC2, PairingDeleteCommandResponsePacketHandler.class, V3),
    /** サービスコネクトコマンド通知応答. */
    PHONE_SERVICE_CONNECT_COMMAND_RESPONSE(0x23, 0x05, 0xC3, PhoneServiceConnectCommandResponsePacketHandler.class, V3),
    /** サーチコマンド完了通知. */
    PHONE_SEARCH_COMMAND_COMPLETE_NOTIFICATION(0x23, 0x05, 0x00, PhoneSearchCommandCompleteNotificationPacketHandler.class, V3),
    /** ペアリング追加コマンド完了通知. */
    PAIRING_ADD_COMMAND_COMPLETE_NOTIFICATION(0x23, 0x05, 0x01, PairingAddCommandCompleteNotificationPacketHandler.class, V3),
    /** ペアリング削除コマンド完了通知. */
    PAIRING_DELETE_COMMAND_COMPLETE_NOTIFICATION(0x23, 0x05, 0x02, PairingDeleteCommandCompleteNotificationPacketHandler.class, V3),
    /** サービスコネクトコマンド完了通知. */
    PHONE_SERVICE_CONNECT_COMPLETE_NOTIFICATION(0x23, 0x05, 0x03, PhoneServiceConnectCompleteNotificationPacketHandler.class, V3),

    //////// 表示データ:車載機情報 ////////
    /** オーディオ情報通知. */
    DEVICE_AUDIO_INFO_NOTIFICATION(0x31, 0x00, 0x00, DeviceAudioInfoNotificationPacketHandler.class, V1),
    /** 楽曲再生時間通知. */
    DEVICE_AUDIO_PLAYBACK_POSITION_NOTIFICATION(0x31, 0x00, 0x10, DeviceAudioPlaybackPositionNotificationPacketHandler.class, V1),
    /** 周波数通知 : Radio/TI. */
    DEVICE_RADIO_INFO_NOTIFICATION(0x31, 0x00, 0x20, DeviceRadioInfoNotificationPacketHandler.class, V1),
    /** 周波数通知 : DAB. */
    DEVICE_DAB_INFO_NOTIFICATION(0x31, 0x00, 0x21, DeviceDabInfoNotificationPacketHandler.class, V1),
    /** CHANNEL NUMBER : SiriusXM. */
    DEVICE_SXM_CHANNEL_NUMBER_NOTIFICATION(0x31, 0x00, 0x22, DeviceSxmChannelNumberNotificationPacketHandler.class, V1),
    /** 周波数通知 : HD Radio. */
    DEVICE_HD_RADIO_INFO_NOTIFICATION(0x31, 0x00, 0x23, DeviceHdRadioInfoNotificationPacketHandler.class, V1),
    /** Tunerソースバッファ再生時間通知. */
    DEVICE_TUNER_BUFFER_NOTIFICATION(0x31, 0x00, 0x24, DeviceTunerBufferNotificationPacketHandler.class, V2),
    /** リスト初期情報応答. */
    INITIAL_LIST_INFO_RESPONSE(0x31, 0x01, 0xE0, InitialListInfoResponsePacketHandler.class, V2),
    /** リスト情報応答. */
    LIST_INFO_RESPONSE(0x31, 0x01, 0xE1, ListInfoResponsePacketHandler.class, V2),
    /** 設定リスト初期情報応答. */
    INITIAL_SETTING_LIST_INFO_RESPONSE(0x31, 0x01, 0xE2, InitialSettingListInfoResponsePacketHandler.class, V3),
    /** 設定リスト情報応答. */
    SETTING_LIST_INFO_RESPONSE(0x31, 0x01, 0xE3, SettingListInfoResponsePacketHandler.class, V3),
    /** ABCサーチ表示通知. */
    AbcSearchDisplayNotification(0x31, 0x01, 0x00, AbcSearchDisplayNotificationPacketHandler.class, V2),
    /** ペアリングデバイスリスト情報要求に対する応答. */
    PAIRING_DEVICE_LIST_INFO_RESPONSE(0x31, 0x80, 0xE0, PairingDeviceListInfoResponsePacketHandler.class, V4),
    /** ペアリングデバイス情報要求に対する応答. */
    PAIRING_DEVICE_INFO_RESPONSE(0x31, 0x80, 0xE1, PairingDeviceInfoResponsePacketHandler.class, V4),

    //////// 表示データ:設定 ////////
    /** BEEP TONE設定情報通知. */
    BEEP_TONE_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x00, BeepToneSettingInfoNotificationPacketHandler.class, V4),
    /** BEEP TONE設定情報応答. */
    BEEP_TONE_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE0, BeepToneSettingInfoResponsePacketHandler.class, V4),
    /** ATT/MUTE設定情報通知. */
    ATT_MUTE_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x01, AttMuteSettingInfoNotificationPacketHandler.class, V4),
    /** ATT/MUTE設定情報応答. */
    ATT_MUTE_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE1, AttMuteSettingInfoResponsePacketHandler.class, V4),
    /** DEMO設定情報通知. */
    DEMO_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x02, DemoSettingInfoNotificationPacketHandler.class, V4),
    /** DEMO設定情報応答. */
    DEMO_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE2, DemoSettingInfoResponsePacketHandler.class, V4),
    /** POWER SAVE設定情報通知. */
    POWER_SAVE_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x03, PowerSaveSettingInfoNotificationPacketHandler.class, V4),
    /** POWER SAVE設定情報応答. */
    POWER_SAVE_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE3, PowerSaveSettingInfoResponsePacketHandler.class, V4),
    /** BT Audio設定情報通知. */
    BT_AUDIO_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x04, BtAudioSettingInfoNotificationPacketHandler.class, V4),
    /** BT Audio設定情報応答. */
    BT_AUDIO_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE4, BtAudioSettingInfoResponsePacketHandler.class, V4),
    /** Pandora設定情報通知. */
    PANDORA_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x05, PandoraSettingInfoNotificationPacketHandler.class, V4),
    /** Pandora設定情報応答. */
    PANDORA_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE5, PandoraSettingInfoResponsePacketHandler.class, V4),
    /** Spotify設定情報通知. */
    SPOTIFY_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x06, SpotifySettingInfoNotificationPacketHandler.class, V4),
    /** Spotify設定情報応答. */
    SPOTIFY_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE6, SpotifySettingInfoResponsePacketHandler.class, V4),
    /** AUX設定情報通知. */
    AUX_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x07, AuxSettingInfoNotificationPacketHandler.class, V4),
    /** AUX設定情報応答. */
    AUX_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE7, AuxSettingInfoResponsePacketHandler.class, V4),
    /** 99APP自動起動設定情報通知. */
    APP_AUTO_START_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x08, AppAutoStartSettingInfoNotificationPacketHandler.class, V4),
    /** 99APP自動起動設定情報応答. */
    APP_AUTO_START_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE8, AppAutoStartSettingInfoResponsePacketHandler.class, V4),
    /** USB AUTO設定情報通知. */
    USB_AUTO_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x09, UsbAutoSettingInfoNotificationPacketHandler.class, V4),
    /** USB AUTO設定情報応答. */
    USB_AUTO_SETTING_INFO_RESPONSE(0x35, 0x00, 0xE9, UsbAutoSettingInfoResponsePacketHandler.class, V4),
    /** ステアリングリモコン設定情報通知. */
    STEERING_REMOTE_CONTROL_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x0A, SteeringRemoteControlSettingInfoNotificationPacketHandler.class, V4),
    /** ステアリングリモコン設定情報応答. */
    STEERING_REMOTE_CONTROL_SETTING_INFO_RESPONSE(0x35, 0x00, 0xEA, SteeringRemoteControlSettingInfoResponsePacketHandler.class, V4),
    /** AUTO PI設定情報通知. */
    AUTO_PI_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x0B, AutoPiSettingInfoNotificationPacketHandler.class, V4),
    /** AUTO PI設定情報応答. */
    AUTO_PI_SETTING_INFO_RESPONSE(0x35, 0x00, 0xEB, AutoPiSettingInfoResponsePacketHandler.class, V4),
    /** DISP OFF設定情報通知. */
    DISPLAY_OFF_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x0C, DisplayOffSettingInfoNotificationPacketHandler.class, V4),
    /** DISP OFF設定情報応答. */
    DISPLAY_OFF_SETTING_INFO_RESPONSE(0x35, 0x00, 0xEC, DisplayOffSettingInfoResponsePacketHandler.class, V4),
    /** Distance Unit設定情報通知. */
    DISTANCE_UNIT_SETTING_INFO_NOTIFICATION(0x35, 0x00, 0x0D, DistanceUnitSettingInfoNotificationPacketHandler.class, V4),
    /** Distance Unit設定情報応答. */
    DISTANCE_UNIT_SETTING_INFO_RESPONSE(0x35, 0x00, 0xED, DistanceUnitSettingInfoResponsePacketHandler.class, V4),
    /** [OPAL] EQ設定情報通知. */
    EQUALIZER_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x00, EqualizerSettingInfoNotificationPacketHandler.class, V2),
    /** [OPAL] EQ設定情報応答.*/
    EQUALIZER_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE0, EqualizerSettingInfoResponsePacketHandler.class, V2),
    /** FADER/BALANCE情報通知. */
    FADER_BALANCE_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x01, FaderBalanceSettingInfoNotificationPacketHandler.class, V2),
    /** FADER/BALANCE情報応答. */
    FADER_BALANCE_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE1, FaderBalanceSettingInfoResponsePacketHandler.class, V2),
    /** SUBWOOFER設定情報通知. */
    SUBWOOFER_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x02, SubwooferSettingInfoNotificationPacketHandler.class, V2),
    /** SUBWOOFER設定情報応答. */
    SUBWOOFER_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE2, SubwooferSettingInfoResponsePacketHandler.class, V2),
    /** SUBWOOFER位相設定情報通知. */
    SUBWOOFER_PHASE_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x03, SubwooferPhaseSettingInfoNotificationPacketHandler.class, V2),
    /** SUBWOOFER位相設定情報応答. */
    SUBWOOFER_PHASE_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE3, SubwooferPhaseSettingInfoResponsePacketHandler.class, V2),
    /** SPEAKER LEVEL情報通知. */
    SPEAKER_LEVEL_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x04, SpeakerLevelSettingInfoNotificationPacketHandler.class, V2),
    /** SPEAKER LEVEL情報応答. */
    SPEAKER_LEVEL_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE4, SpeakerLevelSettingInfoResponsePacketHandler.class, V2),
    /** CROSSOVER設定情報通知 */
    CROSSOVER_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x05, CrossoverSettingInfoNotificationPacketHandler.class, V2),
    /** CROSSOVER設定情報応答 */
    CROSSOVER_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE5, CrossoverSettingInfoResponsePacketHandler.class, V2),
    /** LISTENING POSITION設定情報通知. */
    LISTENING_POSITION_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x06, ListeningPositionSettingInfoNotificationPacketHandler.class, V2),
    /** LISTENING POSITION設定情報応答. */
    LISTENING_POSITION_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE6, ListeningPositionSettingInfoResponsePacketHandler.class, V2),
    /** TIME ALIGNMENT情報通知. */
    TIME_ALIGNMENT_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x07, TimeAlignmentSettingInfoNotificationPacketHandler.class, V2),
    /** TIME ALIGNMENT情報応答. */
    TIME_ALIGNMENT_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE7, TimeAlignmentSettingInfoResponsePacketHandler.class, V2),
    /** Auto EQ補正設定情報通知. */
    AUTO_EQ_CORRECTION_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x08, AutoEqCorrectionSettingInfoNotificationPacketHandler.class, V2),
    /** Auto EQ補正設定情報応答. */
    AUTO_EQ_CORRECTION_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE8, AutoEqCorrectionSettingInfoResponsePacketHandler.class, V2),
    /** BASS BOOSTERレベル設定情報通知. */
    BASS_BOOSTER_LEVEL_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x09, BassBoosterLevelSettingInfoNotificationPacketHandler.class, V2),
    /** BASS BOOSTERレベル設定情報応答. */
    BASS_BOOSTER_LEVEL_SETTING_INFO_RESPONSE(0x35, 0x01, 0xE9, BassBoosterLevelSettingInfoResponsePacketHandler.class, V2),
    /** LOUDNESS設定情報通知. */
    LOUDNESS_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x0A, LoudnessSettingInfoNotificationPacketHandler.class, V2),
    /** LOUDNESS設定情報応答. */
    LOUDNESS_SETTING_INFO_RESPONSE(0x35, 0x01, 0xEA, LoudnessSettingInfoResponsePacketHandler.class, V2),
    /** ALC設定情報通知. */
    ALC_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x0B, AlcSettingInfoNotificationPacketHandler.class, V2),
    /** ALC設定情報応答. */
    ALC_SETTING_INFO_RESPONSE(0x35, 0x01, 0xEB, AlcSettingInfoResponsePacketHandler.class, V2),
    /** SLA設定情報通知. */
    SLA_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x0C, SlaSettingInfoNotificationPacketHandler.class, V2),
    /** SLA設定情報応答. */
    SLA_SETTING_INFO_RESPONSE(0x35, 0x01, 0xEC, SlaSettingInfoResponsePacketHandler.class, V2),
    /** [JASPER] EQ設定情報通知. */
    JASPER_EQUALIZER_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x0D, JasperEqualizerSettingInfoNotificationPacketHandler.class, V2),
    /** [JASPER] EQ設定情報応答. */
    JASPER_EQUALIZER_SETTING_INFO_RESPONSE(0x35, 0x01, 0xED, JasperEqualizerSettingInfoResponsePacketHandler.class, V2),
    /** [JASPER] CROSSOVER HPF/LPF設定情報通知. */
    JASPER_CROSSOVER_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x0E, JasperCrossoverSettingInfoNotificationPacketHandler.class, V2),
    /** [JASPER] CROSSOVER HPF/LPF設定情報応答. */
    JASPER_CROSSOVER_SETTING_INFO_RESPONSE(0x35, 0x01, 0xEE, JasperCrossoverSettingInfoResponsePacketHandler.class, V2),
    /** [AC2] EQ設定情報通知 (車種専用チューニング：オーディオが有効時). */
    AC2_EQUALIZER_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x0F, Ac2EqualizerSettingInfoNotificationPacketHandler.class, V3),
    /** [AC2] EQ設定情報応答 (車種専用チューニング：オーディオが有効時). */
    AC2_EQUALIZER_SETTING_INFO_RESPONSE(0x35, 0x01, 0xEF, Ac2EqualizerSettingInfoResponsePacketHandler.class, V3),
    /** [AC2] Beat Blasterレベル設定情報通知. */
    BEAT_BLASTER_LEVEL_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x10, BeatBlasterLevelSettingInfoNotificationPacketHandler.class, V3),
    /** [AC2] Beat Blasterレベル設定情報応答. */
    BEAT_BLASTER_LEVEL_SETTING_INFO_RESPONSE(0x35, 0x01, 0xF0, BeatBlasterLevelSettingInfoResponsePacketHandler.class, V3),
    /** [AC2] LEVEL設定情報通知. */
    LEVEL_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x11, LevelSettingInfoNotificationPacketHandler.class, V3),
    /** [AC2] LEVEL設定情報応答. */
    LEVEL_SETTING_INFO_RESPONSE(0x35, 0x01, 0xF1, LevelSettingInfoResponsePacketHandler.class, V3),
    /** EQカスタム設定情報a. */
    CUSTOM_EQUALIZER_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x20, CustomEqualizerSettingInfoNotificationPacketHandler.class, V4),
    /** SOUND RETRIEVER設定情報通知. */
    SOUND_RETRIEVER_SETTING_INFO_NOTIFICATION(0x35, 0x01, 0x12, SoundRetrieverSettingInfoNotificationPacketHandler.class, V4),
    /** SOUND RETRIEVER設定情報応答. */
    SOUND_RETRIEVER_SETTING_INFO_RESPONSE(0x35, 0x01, 0xF2, SoundRetrieverSettingInfoResponsePacketHandler.class, V4),
    /** KEY COLOR設定情報通知. */
    KEY_COLOR_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x00, KeyColorSettingInfoNotificationPacketHandler.class, V2),
    /** KEY COLOR設定情報応答. */
    KEY_COLOR_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE0, KeyColorSettingInfoResponsePacketHandler.class, V2),
    /** DISP COLOR設定情報通知. */
    DISP_COLOR_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x01, DispColorSettingInfoNotificationPacketHandler.class, V2),
    /** DISP COLOR設定情報応答. */
    DISP_COLOR_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE1, DispColorSettingInfoResponsePacketHandler.class, V2),
    /** KEY COLOR一括情報応答. */
    KEY_COLOR_BULK_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE2, KeyColorBulkSettingInfoResponsePacketHandler.class, V2),
    /** DISP COLOR一括情報応答. */
    DISP_COLOR_BULK_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE3, DispColorBulkSettingInfoResponsePacketHandler.class, V2),
    /** COLOR CUSTOM設定情報通知. */
    CustomColorSettingInfoNotification(0x35, 0x02, 0x02, CustomColorSettingInfoNotificationPacketHandler.class, V2),
    /** DIMMER設定情報通知. */
    DIMMER_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x03, DimmerSettingInfoNotificationPacketHandler.class, V2),
    /** DIMMER設定情報応答. */
    DIMMER_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE4, DimmerSettingInfoResponsePacketHandler.class, V2),
    /** BRIGHTNESS設定情報通知（共通設定モデル用）. */
    BRIGHTNESS_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x04, BrightnessSettingInfoNotificationPacketHandler.class, V2),
    /** BRIGHTNESS設定情報応答（共通設定モデル用）. */
    BRIGHTNESS_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE5, BrightnessSettingInfoResponsePacketHandler.class, V2),
    /** BT PHONE COLOR設定情報通知. */
    BT_PHONE_COLOR_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x05, BtPhoneColorSettingInfoNotificationPacketHandler.class, V2),
    /** BT PHONE COLOR設定情報応答. */
    BT_PHONE_COLOR_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE6, BtPhoneColorSettingInfoResponsePacketHandler.class, V2),
    /** 蛍の光風設定情報通知. */
    ILLUMINATION_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x06, IlluminationSettingInfoNotificationPacketHandler.class, V2),
    /** 蛍の光風設定情報応答. */
    ILLUMINATION_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE7, IlluminationSettingInfoResponsePacketHandler.class, V2),
    /** KEY BRIGHTNESS設定情報通知（個別設定モデル用）. */
    KEY_BRIGHTNESS_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x07, KeyBrightnessSettingInfoNotificationPacketHandler.class, V3),
    /** KEY BRIGHTNESS設定情報応答（個別設定モデル用）. */
    KEY_BRIGHTNESS_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE8, KeyBrightnessSettingInfoResponsePacketHandler.class, V3),
    /** DISP BRIGHTNESS設定情報通知（個別設定モデル用）. */
    DISPLAY_BRIGHTNESS_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x08, DisplayBrightnessSettingInfoNotificationPacketHandler.class, V3),
    /** DISP BRIGHTNESS設定情報応答 （個別設定モデル用）. */
    DISPLAY_BRIGHTNESS_SETTING_INFO_RESPONSE(0x35, 0x02, 0xE9, DisplayBrightnessSettingInfoResponsePacketHandler.class, V3),
    /** オーディオレベルメータ連動設定情報通知. */
    AUDIO_LEVEL_METER_LINKED_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x09, AudioLevelMeterLinkedSettingInfoNotificationPacketHandler.class, V4),
    /** オーディオレベルメータ連動設定情報応答. */
    AUDIO_LEVEL_METER_LINKED_SETTING_INFO_RESPONSE(0x35, 0x02, 0xEA, AudioLevelMeterLinkedSettingInfoResponsePacketHandler.class, V4),
    /** [SPH] BT PHONE COLOR設定情報通知. */
    SPH_BT_PHONE_COLOR_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x0A, SphBtPhoneColorSettingInfoNotificationPacketHandler.class, V4),
    /** [SPH] BT PHONE COLOR設定情報応答. */
    SPH_BT_PHONE_COLOR_SETTING_INFO_RESPONSE(0x35, 0x02, 0xEB, SphBtPhoneColorSettingInfoResponsePacketHandler.class, V4),
    /** COLOR設定情報通知  (共通設定モデル用). */
    COMMON_COLOR_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x0B, CommonColorSettingInfoNotificationPacketHandler.class, V4),
    /** COLOR設定情報応答  (共通設定モデル用). */
    COMMON_COLOR_SETTING_INFO_RESPONSE(0x35, 0x02, 0xEC, CommonColorSettingInfoResponsePacketHandler.class, V4),
    /** COLOR一括情報応答  (共通設定モデル用). */
    COMMON_COLOR_BULK_SETTING_INFO_RESPONSE(0x35, 0x02, 0xED, CommonColorBulkSettingInfoResponsePacketHandler.class, V4),
    /** COLOR CUSTOM設定情報通知  (共通設定モデル用). */
    COMMON_CUSTOM_COLOR_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x0C, CommonCustomColorSettingInfoNotificationPacketHandler.class, V4),
    /** メッセージ受信通知COLOR設定情報通知. */
    INCOMING_MESSAGE_COLOR_SETTING_INFO_NOTIFICATION(0x35, 0x02, 0x0D, IncomingMessageColorSettingInfoNotificationPacketHandler.class, V4),
    /** メッセージ受信通知COLOR設定情報応答. */
    INCOMING_MESSAGE_COLOR_SETTING_INFO_RESPONSE(0x35, 0x02, 0xEE, IncomingMessageColorSettingInfoResponsePacketHandler.class, V4),
    /** Function設定情報通知. */
    FUNCTION_SETTING_INFO_NOTIFICATION(0x35, 0x04, 0x00, FunctionSettingInfoNotificationPacketHandler.class, V2),
    /** Function設定情報応答. */
    FUNCTION_SETTING_INFO_RESPONSE(0x35, 0x04, 0xE0, FunctionSettingInfoResponsePacketHandler.class, V2),
    /** パーキングセンサー設定情報通知. */
    PARKING_SENSOR_SETTING_INFO_NOTIFICATION(0x35, 0x05, 0x00, ParkingSensorSettingInfoNotificationPacketHandler.class, V4),
    /** パーキングセンサー設定応答. */
    PARKING_SENSOR_SETTING_INFO_RESPONSE(0x35, 0x05, 0xE0, ParkingSensorSettingInfoResponsePacketHandler.class, V4),
    /** 警告音出力先設定情報通知. */
    ALARM_OUTPUT_DESTINATION_SETTING_INFO_NOTIFICATION(0x35, 0x05, 0x01, AlarmOutputDestinationSettingInfoNotificationPacketHandler.class, V4),
    /** 警告音出力先設定情報応答. */
    ALARM_OUTPUT_DESTINATION_SETTING_INFO_RESPONSE(0x35, 0x05, 0xE1, AlarmOutputDestinationSettingInfoResponsePacketHandler.class, V4),
    /** 警告音量設定情報通知. */
    ALARM_VOLUME_SETTING_INFO_NOTIFICATION(0x35, 0x05, 0x02, AlarmVolumeSettingInfoNotificationPacketHandler.class, V4),
    /** 警告音量設定応答. */
    ALARM_VOLUME_SETTING_INFO_RESPONSE(0x35, 0x05, 0xE2, AlarmVolumeSettingInfoResponsePacketHandler.class, V4),
    /** バック信号極性設定情報通知. */
    BACK_POLARITY_SETTING_INFO_NOTIFICATION(0x35, 0x05, 0x03, BackPolaritySettingInfoNotificationPacketHandler.class, V4),
    /** バック信号極性設定応答. */
    BACK_POLARITY_SETTING_INFO_RESPONSE(0x35, 0x05, 0xE3, BackPolaritySettingInfoResponsePacketHandler.class, V4),
    /** ナビガイド音声設定情報通知. */
    NAVI_GUIDE_VOICE_SETTING_INFO_NOTIFICATION(0x35, 0x06, 0x00, NaviGuideVoiceSettingInfoNotificationPacketHandler.class, V4),
    /** ナビガイド音声設定応答. */
    NAVI_GUIDE_VOICE_SETTING_INFO_RESPONSE(0x35, 0x06, 0xE0, NaviGuideVoiceSettingInfoResponsePacketHandler.class, V4),
    /** ナビガイド音声ボリューム設定情報通知. */
    NAVI_GUIDE_VOICE_VOLUME_SETTING_INFO_NOTIFICATION(0x35, 0x06, 0x01, NaviGuideVoiceVolumeSettingInfoNotificationPacketHandler.class, V4),
    /** ナビガイド音声ボリューム設定応答. */
    NAVI_GUIDE_VOICE_VOLUME_SETTING_INFO_RESPONSE(0x35, 0x06, 0xE1, NaviGuideVoiceVolumeSettingInfoResponsePacketHandler.class, V4),
    /** FM STEP設定情報通知. */
    FM_STEP_SETTING_INFO_NOTIFICATION(0x35, 0x07, 0x00, FmStepSettingInfoNotificationPacketHandler.class, V4),
    /** FM STEP設定応答. */
    FM_STEP_SETTING_INFO_RESPONSE(0x35, 0x07, 0xE0, FmStepSettingInfoResponsePacketHandler.class, V4),
    /** AM STEP設定情報通知. */
    AM_STEP_SETTING_INFO_NOTIFICATION(0x35, 0x07, 0x01, AmStepSettingInfoNotificationPacketHandler.class, V4),
    /** AM STEP設定応答. */
    AM_STEP_SETTING_INFO_RESPONSE(0x35, 0x07, 0xE1, AmStepSettingInfoResponsePacketHandler.class, V4),
    /** REAR出力設定/PREOUT出力設定情報通知. */
    REAR_OUTPUT_PREOUT_OUTPUT_SETTING_INFO_NOTIFICATION(0x35, 0x07, 0x02, RearOutputPreoutOutputSettingInfoNotificationPacketHandler.class, V4),
    /** REAR出力設定/PREOUT出力設定応答. */
    REAR_OUTPUT_PREOUT_OUTPUT_SETTING_INFO_RESPONSE(0x35, 0x07, 0xE2, RearOutputPreoutOutputSettingInfoResponsePacketHandler.class, V4),
    /** REAR出力設定情報通知. */
    REAR_OUTPUT_SETTING_INFO_NOTIFICATION(0x35, 0x07, 0x03, RearOutputSettingInfoNotificationPacketHandler.class, V4),
    /** REAR出力設定応答. */
    REAR_OUTPUT_SETTING_INFO_RESPONSE(0x35, 0x07, 0xE3, RearOutputSettingInfoResponsePacketHandler.class, V4),
    /** MENU表示言語設定情報通知. */
    MENU_DISPLAY_LANGUAGE_SETTING_INFO_NOTIFICATION(0x35, 0x07, 0x04, MenuDisplayLanguageSettingInfoNotificationPacketHandler.class, V4),
    /** MENU表示言語設定応答. */
    MENU_DISPLAY_LANGUAGE_SETTING_INFO_RESPONSE(0x35, 0x07, 0xE4, MenuDisplayLanguageSettingInfoResponsePacketHandler.class, V4),
    /** AUTO ANSWER設定情報通知. */
    AUTO_ANSWER_SETTING_INFO_NOTIFICATION(0x35, 0x08, 0x00, AutoAnswerSettingInfoNotificationPacketHandler.class, V4),
    /** AUTO ANSWER設定応答. */
    AUTO_ANSWER_SETTING_INFO_RESPONSE(0x35, 0x08, 0xE0, AutoAnswerSettingInfoResponsePacketHandler.class, V4),
    /** AUTO PAIRING設定情報通知. */
    AUTO_PAIRING_SETTING_INFO_NOTIFICATION(0x35, 0x08, 0x01, AutoPairingSettingInfoNotificationPacketHandler.class, V4),
    /** AUTO PAIRING設定応答. */
    AUTO_PAIRING_SETTING_INFO_RESPONSE(0x35, 0x08, 0xE1, AutoPairingSettingInfoResponsePacketHandler.class, V4),
    /** Super轟設定情報通知. */
    SUPER_TODOROKI_SETTING_INFO_NOTIFICATION(0x35, 0x09, 0x00, SuperTodorokiSettingInfoNotificationPacketHandler.class, V4),
    /** Super轟設定応答. */
    SUPER_TODOROKI_SETTING_INFO_RESPONSE(0x35, 0x09, 0xE0, SuperTodorokiSettingInfoResponsePacketHandler.class, V4),
    /** Small Car TA設定情報通知. */
    SMALL_CAR_TA_SETTING_INFO_NOTIFICATION(0x35, 0x09, 0x01, SmallCarTaSettingInfoNotificationPacketHandler.class, V4),
    /** Small Car TA設定応答. */
    SMALL_CAR_TA_SETTING_INFO_RESPONSE(0x35, 0x09, 0xE1, SmallCarTaSettingInfoResponsePacketHandler.class, V4),
    /** ライブシミュレーション設定情報通知. */
    LIVE_SIMULATION_SETTING_INFO_NOTIFICATION(0x35, 0x09, 0x02, LiveSimulationSettingInfoNotificationPacketHandler.class, V4),
    /** ライブシミュレーション設定応答. */
    LIVE_SIMULATION_SETTING_INFO_RESPONSE(0x35, 0x09, 0xE2, LiveSimulationSettingInfoResponsePacketHandler.class, V4),
    /** カラオケ設定情報通知. */
    KARAOKE_SETTING_INFO_NOTIFICATION(0x35, 0x09, 0x03, KaraokeSettingInfoNotificationPacketHandler.class, V4),
    /** カラオケ設定応答. */
    KARAOKE_SETTING_INFO_RESPONSE(0x35, 0x09, 0xE3, KaraokeSettingInfoResponsePacketHandler.class, V4),
    /** マイク音量設定情報通知. */
    MIC_VOLUME_SETTING_INFO_NOTIFICATION(0x35, 0x09, 0x04, MicVolumeSettingInfoNotificationPacketHandler.class, V4),
    /** マイク音量設定応答. */
    MIC_VOLUME_SETTING_INFO_RESPONSE(0x35, 0x09, 0xE4, MiceVolumeSettingInfoResponsePacketHandler.class, V4),
    /** Vocal Cancel設定情報通知. */
    VOCAL_CANCEL_SETTING_INFO_NOTIFICATION(0x35, 0x09, 0x05, VocalCancelSettingInfoNotificationPacketHandler.class, V4),
    /** Vocal Cancel設定応答. */
    VOCAL_CANCEL_SETTING_INFO_RESPONSE(0x35, 0x09, 0xE5, VocalCancelSettingInfoResponsePacketHandler.class, V4),

    //////// 表示データ:割り込み情報 ////////
    /** SmartPhone割り込み情報応答. */
    SMART_PHONE_INTERRUPT_RESPONSE(0x37, 0x00, 0xC0, SimpleResponsePacketHandler.class, V4),
    /** 車載機割り込み情報通知. */
    DEVICE_INTERRUPT_NOTIFICATION(0x37, 0x01, 0x00, DeviceInterruptNotificationPacketHandler.class, V1)
    ;

    /** ID（コマンドの大分類）. */
    public final int id;
    /** Type（IDより下位のコマンド小分類）. */
    public final int type;
    /** D0（通信の種別）. */
    public final int d0;
    /** 受信パケットハンドラクラス. */
    public final Class<? extends PacketHandler> packetHandlerClass;
    /** サポートする最小のプロトコルバージョン. */
    public final ProtocolVersion supportVersion;

    /**
     * コンストラクタ.
     *
     * @param id ID（コマンドの大分類）
     * @param type Type（IDより下位のコマンド小分類）
     * @param d0 D0（通信の種別）
     * @param packetHandlerClass 受信パケットハンドラ
     * @param supportVersion サポートする最小のプロトコルバージョン。全てサポートする場合はnull。
     * @throws NullPointerException {@code packetHandlerClass}がnull
     */
    IncomingPacketIdType(int id, int type, int d0, @NonNull Class<? extends PacketHandler> packetHandlerClass,
                         @Nullable ProtocolVersion supportVersion) {
        this.id = id;
        this.type = type;
        this.d0 = d0;
        this.packetHandlerClass = checkNotNull(packetHandlerClass);
        this.supportVersion = supportVersion;
    }

    /**
     * ID、Type、D0から{@link IncomingPacketIdType}取得.
     *
     * @param id ID（コマンドの大分類）
     * @param type Type（IDより下位のコマンド小分類）
     * @param d0 D0（通信の種別）
     * @return {@code id}、{@code type}、{@code d0}に該当する受信パケットIDタイプ
     * @throws IllegalArgumentException 該当する受信パケットIDタイプがない
     */
    public static IncomingPacketIdType valueOf(int id, int type, int d0) {
        for (IncomingPacketIdType idType : values()) {
            if (idType.id == id && idType.type == type && idType.d0 == d0) {
                return idType;
            }
        }

        throw new IllegalArgumentException(String.format("invalid arguments. (ID, Type, D0) = %s, %s, %s",
                toHex((byte) id), toHex((byte) type), toHex((byte) d0)));
    }

    /**
     * 応答パケットか否か取得.
     *
     * @return {@code true}:応答パケットである。{@code false}:それ以外。
     */
    public boolean isResponsePacket() {
        return ResponsePacketHandler.class.isAssignableFrom(packetHandlerClass);
    }
}
