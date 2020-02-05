package jp.pioneer.carsync.infrastructure.crp;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Size;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;

import javax.inject.Inject;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.model.AdasWarningStatus;
import jp.pioneer.carsync.domain.model.AlarmOutputDestinationSetting;
import jp.pioneer.carsync.domain.model.AlcSetting;
import jp.pioneer.carsync.domain.model.AmStep;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.AttMuteSetting;
import jp.pioneer.carsync.domain.model.AudioMode;
import jp.pioneer.carsync.domain.model.AudioSettingEqualizerType;
import jp.pioneer.carsync.domain.model.AutoEqCorrectionSetting;
import jp.pioneer.carsync.domain.model.BackPolarity;
import jp.pioneer.carsync.domain.model.BeatBlasterSetting;
import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.CarDeviceControlCommand;
import jp.pioneer.carsync.domain.model.CarDeviceScreen;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.ConnectServiceType;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.CustomFlashRequestType;
import jp.pioneer.carsync.domain.model.CutoffSetting;
import jp.pioneer.carsync.domain.model.DabFunctionType;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.domain.model.FlashPatternRegistrationType;
import jp.pioneer.carsync.domain.model.FmStep;
import jp.pioneer.carsync.domain.model.HdRadioFunctionType;
import jp.pioneer.carsync.domain.model.HpfLpfFilterType;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.IncomingMessageColorSetting;
import jp.pioneer.carsync.domain.model.InterruptFlashPatternDirecting;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.LoadSettingsType;
import jp.pioneer.carsync.domain.model.LoudnessSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.NaviGuideVoiceVolumeSetting;
import jp.pioneer.carsync.domain.model.PairingSpecType;
import jp.pioneer.carsync.domain.model.PhoneConnectRequestType;
import jp.pioneer.carsync.domain.model.PhoneSearchRequestType;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ProtocolVersion;
import jp.pioneer.carsync.domain.model.ReadingRequestType;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.RearOutputSetting;
import jp.pioneer.carsync.domain.model.ReversePolarity;
import jp.pioneer.carsync.domain.model.SettingListType;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SlopeSetting;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.domain.model.SmartPhoneErrorCode;
import jp.pioneer.carsync.domain.model.SmartPhoneInterruptType;
import jp.pioneer.carsync.domain.model.SmartPhoneMediaInfoType;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.SoundEffectSettingType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundRetrieverSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType;
import jp.pioneer.carsync.domain.model.SubDisplayInfo;
import jp.pioneer.carsync.domain.model.SubwooferPhaseSetting;
import jp.pioneer.carsync.domain.model.SubwooferSetting;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSettingMode;
import jp.pioneer.carsync.domain.model.TransitionDirection;
import jp.pioneer.carsync.domain.model.TunerFunctionType;
import jp.pioneer.carsync.domain.model.VoiceRecognitionRequestType;
import jp.pioneer.carsync.domain.model.ZoneColorSpec;
import jp.pioneer.carsync.domain.model.ZoneFrameInfo;
import jp.pioneer.carsync.infrastructure.crp.handler.ResponseCode;
import jp.pioneer.carsync.infrastructure.crp.util.PacketUtil;
import jp.pioneer.mbg.alexa.AlexaInterface.AlexaIfDirectiveItem;
import jp.pioneer.mbg.alexa.AlexaInterface.directive.TemplateRuntime.RenderPlayerInfoItem;
import jp.pioneer.mbg.alexa.manager.AlexaAudioManager;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.infrastructure.crp.OutgoingPacketIdType.*;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.uintToByteArray;
import static jp.pioneer.carsync.infrastructure.crp.util.PacketUtil.ushortToByteArray;

/**
 * 送信パケットビルダー.
 */
public class OutgoingPacketBuilder {
    @Inject AppSharedPreference mPreference;
    @Inject @ForInfrastructure StatusHolder mStatusHolder;
    /**
     * コンストラクタ.
     */
    @Inject
    public OutgoingPacketBuilder() {
    }

    /**
     * 認証開始パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createStartInitialAuth() {
        return createWith(START_INITIAL_AUTH);
    }

    /**
     * 認証終了パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createEndInitialAuth() {
        return createWith(END_INITIAL_AUTH);
    }

    /**
     * Class ID要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createClassIdRequest() {
        return createWith(CLASS_ID_REQUEST);
    }

    /**
     * Protocol Version要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createProtocolVersionRequest() {
        return createWith(PROTOCOL_VERSION_REQUEST);
    }

    /**
     * Protocol Version通知パケット生成.
     *
     * @param version プロトコルバージョン
     * @return 送信パケット
     * @throws NullPointerException {@code version}がnull
     */
    @NonNull
    public OutgoingPacket createProtocolVersionNotification(@NonNull ProtocolVersion version) {
        checkNotNull(version);

        byte[] data;
        if (version.isGreaterThanOrEqual(ProtocolVersion.V3)) {
            data = new byte[] { 0x00, (byte) version.major, (byte) version.minor };
        } else {
            data = new byte[] { 0x00, (byte) version.major };
        }

        return createWith(PROTOCOL_VERSION_NOTIFICATION, data);
    }

    /**
     * 初期通信開始パケット生成.
     *
     * @return 送信パケット生成
     */
    @NonNull
    public OutgoingPacket createStartInitialComm() {
        return createWith(START_INITIAL_COMM);
    }

    /**
     * 初期通信終了パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createEndInitialComm() {
        return createWith(END_INITIAL_COMM);
    }

    /**
     * 車載機情報取得開始パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createStartGetDeviceSpec() {
        return createWith(START_GET_DEVICE_SPEC);
    }

    /**
     * 車載機情報取得終了パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createEndGetDeviceSpec() {
        return createWith(END_GET_DEVICE_SPEC);
    }

    /**
     * SmartPhone情報通知開始パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createStartSendSmartPhoneSpec() {
        return createWith(START_SEND_SMART_PHONE_SPEC);
    }

    /**
     * SmartPhone情報通知終了パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createEndSendSmartPhoneSpec() {
        return createWith(END_SEND_SMART_PHONE_SPEC);
    }

    /**
     * セッション開始パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createStartSession() {
        return createWith(START_SESSION);
    }

    /**
     * 認証エラーパケット生成.
     *
     * @param errorIdType エラーになった要因の受信パケットIDタイプ
     * @return 送信パケット
     * @throws NullPointerException {@code errorIdType}がnull
     */
    @NonNull
    public OutgoingPacket createAuthError(@NonNull IncomingPacketIdType errorIdType) {
        checkNotNull(errorIdType);

        byte[] data = { 0x00, (byte) errorIdType.id, (byte) errorIdType.type };
        return createWith(AUTH_ERROR, data);
    }

    /**
     * 車載機Spec要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDeviceSpecRequest() {
        return createWith(DEVICE_SPEC_REQUEST);
    }

    /**
     * 車載機型番要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDeviceModelRequest() {
        return createWith(DEVICE_MODEL_REQUEST);
    }

    /**
     * 車載機BDアドレス要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDeviceBdAddressRequest() {
        return createWith(DEVICE_BD_ADDRESS_REQUEST);
    }

    /**
     * SmartPhoneSpec通知パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSmartPhoneSpecNotification() {
        int supportBitmap = 0x01; // AndroidはApp Music対応を固定で通知する
        byte[] data = { 0x00, (byte) supportBitmap, 0x00, 0x00 };
        return createWith(SMART_PHONE_SPEC_NOTIFICATION, data);
    }

    /**
     * 時刻情報通知パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createTimeNotification() {
        Calendar cal = Calendar.getInstance();
        byte[] year = ushortToByteArray(cal.get(Calendar.YEAR));

        byte[] data = {
                0x00,
                (byte) cal.get(Calendar.HOUR_OF_DAY),
                (byte) cal.get(Calendar.MINUTE),
                (byte) cal.get(Calendar.SECOND),
                (byte) cal.get(Calendar.DAY_OF_MONTH),
                (byte) (cal.get(Calendar.MONTH) + 1),
                year[0],
                year[1]
        };
        return createWith(TIME_NOTIFICATION, data);
    }

    /**
     * 車載機ステータス情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDeviceStatusRequest() {
        return createWith(DEVICE_STATUS_REQUEST);
    }

    /**
     * Tuner系共通ステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createTunerStatusNotificationResponse() {
        return createWith(TUNER_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * Radioステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createRadioStatusNotificationResponse() {
        return createWith(RADIO_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * DABステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDabStatusNotificationResponse() {
        return createWith(DAB_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * SiriusXMステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSxmStatusNotificationResponse() {
        return createWith(SXM_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * HD Radioステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createHdRadioStatusNotificationResponse() {
        return createWith(HD_RADIO_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * メディア系共通ステータス情報応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createMediaStatusNotificationResponse() {
        return createWith(MEDIA_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * CDステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createCdStatusNotificationResponse() {
        return createWith(CD_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * iPodステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createIPodStatusNotificationResponse() {
        return createWith(IPOD_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * USBステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createUsbStatusNotificationResponse() {
        return createWith(USB_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * BT Audioステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBtAudioStatusNotificationResponse() {
        return createWith(BT_AUDIO_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * Pandoraステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPandoraStatusNotificationResponse() {
        return createWith(PANDORA_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * Spotifyステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSpotifyStatusNotificationResponse() {
        return createWith(SPOTIFY_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * システム設定ステータス情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSystemSettingStatusRequest() {
        return createWith(SYSTEM_SETTING_STATUS_REQUEST);
    }

    /**
     * システム設定ステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSystemSettingStatusNotificationResponse() {
        return createWith(SYSTEM_SETTING_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * オーディオ設定ステータス情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAudioSettingStatusRequest() {
        return createWith(AUDIO_SETTING_STATUS_REQUEST);
    }

    /**
     * オーディオ設定ステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAudioSettingStatusNotificationResponse() {
        return createWith(AUDIO_SETTING_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * イルミ設定ステータス情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createIlluminationSettingStatusRequest() {
        return createWith(ILLUMINATION_SETTING_STATUS_REQUEST);
    }

    /**
     * イルミ設定ステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createIlluminationSettingNotificationResponse() {
        return createWith(ILLUMINATION_SETTING_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * Function設定ステータス情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFunctionSettingStatusRequest() {
        return createWith(FUNCTION_SETTING_STATUS_REQUEST);
    }

    /**
     * Function設定ステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFunctionSettingStatusNotificationResponse() {
        return createWith(FUNCTION_SETTING_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * Phone設定ステータス情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPhoneSettingStatusRequest() {
        return createWith(PHONE_SETTING_STATUS_REQUEST);
    }

    /**
     * Phone設定ステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPhoneSettingStatusNotificationResponse() {
        return createWith(PHONE_SETTING_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * パーキングセンサー設定ステータス情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createParkingSensorSettingStatusRequest() {
        return createWith(PARKING_SENSOR_SETTING_STATUS_REQUEST);
    }

    /**
     * パーキングセンサー設定ステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createParkingSensorSettingStatusNotificationResponse() {
        return createWith(PARKING_SENSOR_SETTING_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * 初期設定ステータス情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createInitialSettingStatusRequest() {
        return createWith(INITIAL_SETTING_STATUS_REQUEST);
    }

    /**
     * 初期設定ステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createInitialSettingStatusNotificationResponse() {
        return createWith(INITIAL_SETTING_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * Sound FX設定ステータス情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSoundFxSettingStatusRequest() {
        return createWith(SOUND_FX_SETTING_STATUS_REQUEST);
    }

    /**
     * Sound FX設定ステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSoundFxSettingStatusNotificationResponse() {
        return createWith(SOUND_FX_SETTING_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * パーキングセンサーステータス情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createParkingSensorStatusNotificationResponse() {
        return createWith(PARKING_SENSOR_STATUS_NOTIFICATION_RESPONSE);
    }

    /**
     * SmartPhoneステータス情報（初回）の通知パケット生成.
     *
     * @param version プロトコルバージョン
     * @param smartPhoneStatus SmartPhoneステータス情報
     * @return 送信パケット
     * @throws NullPointerException {@code version}、または、{@code smartPhoneStatus}がnull
     */
    @NonNull
    public OutgoingPacket createSmartPhoneStatusInitialNotification(
            @NonNull ProtocolVersion version,
            @NonNull SmartPhoneStatus smartPhoneStatus) {
        return createWith(
                SMART_PHONE_STATUS_INITIAL_NOTIFICATION,
                createSmartPhoneStatusData(checkNotNull(version), checkNotNull(smartPhoneStatus), false));
    }

    /**
     * SmartPhoneステータス情報の通知パケット生成.
     *
     * @param version プロトコルバージョン
     * @param smartPhoneStatus SmartPhoneステータス情報
     * @return 送信パケット
     * @throws NullPointerException {@code version}、または、{@code smartPhoneStatus}がnull
     */
    @NonNull
    public OutgoingPacket createSmartPhoneStatusNotification(
            @NonNull ProtocolVersion version,
            @NonNull SmartPhoneStatus smartPhoneStatus) {
        boolean isAdasAlarm = mPreference == null ? false : mPreference.isAdasAlarmEnabled();
        return createWith(
                SMART_PHONE_STATUS_NOTIFICATION,
                createSmartPhoneStatusData(checkNotNull(version), checkNotNull(smartPhoneStatus), isAdasAlarm));
    }

    /**
     * SmartPhoneステータス情報応答パケット生成.
     *
     * @param version プロトコルバージョン
     * @param smartPhoneStatus SmartPhoneステータス情報
     * @return 送信パケット
     * @throws NullPointerException {@code version}、または、{@code smartPhoneStatus}がnull
     */
    @NonNull
    public OutgoingPacket createSmartPhoneStatusResponse(
            @NonNull ProtocolVersion version,
            @NonNull SmartPhoneStatus smartPhoneStatus) {
        boolean isAdasAlarm = mPreference == null ? false : mPreference.isAdasAlarmEnabled();
        return createWith(
                SMART_PHONE_STATUS_RESPONSE,
                createSmartPhoneStatusData(checkNotNull(version), checkNotNull(smartPhoneStatus), isAdasAlarm));
    }

    /**
     * SmartPhoneエラー通知パケット生成.
     *
     * @param errorCode エラーコード
     * @return 送信パケット
     * @throws NullPointerException {@code errorCode}がnull
     */
    @NonNull
    public OutgoingPacket createSmartPhoneErrorNotification(@NonNull SmartPhoneErrorCode errorCode) {
        checkNotNull(errorCode);

        int d1 = (int) (errorCode.code >> 24 & 0xFF);
        int d2 = (int) (errorCode.code >> 16 & 0xFF);
        int d3 = (int) (errorCode.code >> 8 & 0xFF);
        int d4 = (int) (errorCode.code & 0xFF);

        byte[] data = { 0x00, (byte) d1, (byte) d2, (byte) d3, (byte) d4 };
        return createWith(SMART_PHONE_ERROR_NOTIFICATION, data);
    }

    /**
     * 車載機エラー通知応答パケット生成.
     *
     * @param responseCode 応答コード
     * @return 送信パケット
     * @throws NullPointerException {@code responseCode}がnull
     */
    @NonNull
    public OutgoingPacket createDeviceErrorNotificationResponse(@NonNull ResponseCode responseCode) {
        return createWith(DEVICE_ERROR_NOTIFICATION_RESPONSE, new byte[] { 0x00, (byte) checkNotNull(responseCode).code });
    }

    /**
     * 車載機操作コマンド通知パケット生成.
     *
     * @param command 車載機操作コマンド
     * @return 送信パケット
     * @throws NullPointerException {@code responseCode}がnull
     */
    public OutgoingPacket createDeviceControlCommand(@NonNull CarDeviceControlCommand command) {
        return createWith(DEVICE_CONTROL_COMMAND, new byte[] { 0x00, (byte) checkNotNull(command).code });
    }

    /**
     * ソース切替通知パケット生成
     *
     * @param sourceType ソース種別
     * @return 送信パケット
     * @throws NullPointerException {@code sourceType}がnull
     */
    @NonNull
    public OutgoingPacket createSourceSwitchCommand(@NonNull MediaSourceType sourceType) {
        return createWith(SOURCE_SWITCH_COMMAND, new byte[] { 0x00, (byte) checkNotNull(sourceType).code } );
    }

    /**
     * 車載機画面切替通知パケット生成.
     *
     * @param screen 車載機画面種別
     * @param direction 遷移方向
     * @return 送信パケット
     * @throws NullPointerException {@code screen}、または、{@code direction}がnull
     */
    @NonNull
    public OutgoingPacket createScreenChangeCommand(@NonNull CarDeviceScreen screen, @NonNull TransitionDirection direction) {
        return createWith(SCREEN_CHANGE_COMMAND,
                new byte[] {0x00, (byte) checkNotNull(screen).code, (byte) checkNotNull(direction).code });
    }

    /**
     * 読み上げ通知パケット生成.
     *
     * @param type 要求種別
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createReadingCommand(@NonNull ReadingRequestType type) {
        return createWith(READING_COMMAND,
                new byte[] {0x00, (byte) checkNotNull(type).code});
    }

    /**
     * 新着メッセージ通知パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createNewMessageCommand() {
        return createWith(NEW_MESSAGE_COMMAND);
    }

    /**
     * CUSTOM発光通知パケット生成.
     *
     * @param type 要求種別
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createCustomFlashCommand(@NonNull CustomFlashRequestType type) {
        return createWith(CUSTOM_FLASH_COMMAND,
                new byte[] {0x00, (byte) checkNotNull(type).code});
    }

    /**
     * 衝突検知通知パケット生成.
     *
     * @param timerSecond 発信までの時間(秒)
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createImpactDetectionCommand(int timerSecond) {
        return createWith(IMPACT_DETECTION_COMMAND,
                new byte[] {0x00, (byte) timerSecond});
    }

    /**
     * 電話発信通知パケット生成.
     *
     * @param phoneNumber 電話番号
     * @return 送信パケット
     * @throws NullPointerException {@code phoneNumber}がnull
     */
    @NonNull
    public OutgoingPacket createPhoneCallCommand(@NonNull String phoneNumber) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        // D0:通知
        stream.write(0x00);
        // D1-NN:電話番号
        int length = 0;
        try {
            checkNotNull(phoneNumber);
            for (int i = 0; i < phoneNumber.length(); i++) {
                byte[] bytes = phoneNumber.substring(i, i + 1).getBytes("UTF-8");
                for (byte b : bytes) {
                    length += PacketUtil.isDLE(b) ? 2 : 1;
                }

                // 終端文字を除くバイトサイズが65バイトより大きくならないように調整
                if (length + 1 > 65) {
                    break; // これ以上writeするとパケットサイズオーバーするので
                }

                stream.write(bytes);
            }
            stream.write(0x00); // 終端文字
        } catch (IOException e) {
            // ignore
        }
        return createWith(PHONE_CALL_COMMAND, stream.toByteArray());
    }

    /**
     * メニュー表示解除通知パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createExitMenuCommand() {
        return createWith(EXIT_MENU_COMMAND);
    }

    /**
     * 音声認識通知パケット生成.
     *
     * @param type 要求種別
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createVoiceRecognitionCommand(@NonNull VoiceRecognitionRequestType type) {
        return createWith(VOICE_RECOGNITION_COMMAND,
                new byte[] {0x00, (byte) checkNotNull(type).code});
    }

    /**
     * リバース極性切替通知パケット生成.
     *
     * @param polarity 極性
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createReversePolarityChangeCommand(@NonNull ReversePolarity polarity) {
        return createWith(REVERSE_POLARITY_CHANGE_COMMAND,
                new byte[] {0x00, (byte) checkNotNull(polarity).code});
    }

    /**
     * Favorite情報取通知 : Radioパケット生成.
     *
     * @param index 周波数インデックス
     * @param band Bandコード
     * @param pi PI。RDSの場合:有効値を設定。RBDSの場合:無効値として"0xFFFF"を設定。
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFavoriteRadioSetCommand(int index, int band, int pi) {
        byte[] frequencyByteArray = ushortToByteArray(index);
        byte[] piByteArray = ushortToByteArray(pi);
        byte[] data = {
                0x00,
                frequencyByteArray[0],
                frequencyByteArray[1],
                (byte) band,
                piByteArray[0],
                piByteArray[1]
        };

        return createWith(FAVORITE_RADIO_SET_COMMAND, data);
    }

    /**
     * Favorite情報取通知 : DABパケット生成.
     *
     * @param index 周波数インデックス
     * @param band Bandコード
     * @param eid EID
     * @param sid SID
     * @param scids SCIdS
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFavoriteDabSetCommand(int index, int band, int eid, long sid, int scids) {
        byte[] eidByteArray = ushortToByteArray(eid);
        byte[] sidByteArray = uintToByteArray(sid);
        byte[] scidsByteArray = ushortToByteArray(scids);
        byte[] data = {
                0x00,
                (byte) index,
                (byte) band,
                eidByteArray[0],
                eidByteArray[1],
                sidByteArray[0],
                sidByteArray[1],
                sidByteArray[2],
                sidByteArray[3],
                scidsByteArray[0],
                scidsByteArray[1]
        };

        return createWith(FAVORITE_DAB_SET_COMMAND, data);
    }

    /**
     * Favorite情報取通知 : Sirius XMパケット生成.
     *
     * @param channelNumber CH番号
     * @param band Bandコード
     * @param sid SID
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFavoriteSiriusXmSetCommand(int channelNumber, int band, int sid) {
        byte[] channelNumberByteArray = ushortToByteArray(channelNumber);
        byte[] sidByteArray = ushortToByteArray(sid);
        byte[] data = {
                0x00,
                channelNumberByteArray[0],
                channelNumberByteArray[1],
                (byte) band,
                sidByteArray[0],
                sidByteArray[1],
        };

        return createWith(FAVORITE_SIRIUS_XM_SET_COMMAND, data);
    }

    /**
     * Favorite情報取通知 : HD Radioパケット生成.
     *
     * @param index 周波数インデックス
     * @param band Bandコード
     * @param multicastChannelNumber マルチキャストCH番号
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFavoriteHdRadioSetCommand(int index, int band, int multicastChannelNumber) {
        byte[] frequencyByteArray = ushortToByteArray(index);
        byte[] data = {
                0x00,
                frequencyByteArray[0],
                frequencyByteArray[1],
                (byte) band,
                (byte) multicastChannelNumber
        };

        return createWith(FAVORITE_HD_RADIO_SET_COMMAND, data);
    }

    /**
     * 音声認識終了通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFinishVoiceRecognitionNotificationResponseCommand() {
        return createWith(FINISH_VOICE_RECOGNITION_NOTIFICATION_RESPONSE);
    }

    /**
     * リスト遷移通知パケット生成.
     *
     * @param direction 遷移方向
     * @param type 現在のソース種別
     * @param listType 遷移するリスト種別
     * @return 送信パケット
     * @throws NullPointerException {@code direction}、{@code type}、{@code listType}のいずれかがnull
     */
    @NonNull
    public OutgoingPacket createListTransitionNotification(@NonNull TransitionDirection direction,
                                                           @NonNull MediaSourceType type,
                                                           @NonNull ListType listType) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(direction).code,
                (byte) checkNotNull(type).code,
                (byte) checkNotNull(listType).code
        };

        return createWith(LIST_TRANSITION_NOTIFICATION, data);
    }

    /**
     * リストアイテム選択通知 : DABパケット生成.
     *
     * @param index 周波数インデックス
     * @param eid EID
     * @param sid SID
     * @param scids SCIdS
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDabListItemSelectedNotification(int index, int eid, long sid, int scids) {
        byte[] eidByteArray = ushortToByteArray(eid);
        byte[] sidByteArray = uintToByteArray(sid);
        byte[] scidsByteArray = ushortToByteArray(scids);
        byte[] data = {
                0x00,
                (byte) index,
                eidByteArray[0],
                eidByteArray[1],
                sidByteArray[0],
                sidByteArray[1],
                sidByteArray[2],
                sidByteArray[3],
                scidsByteArray[0],
                scidsByteArray[1]
        };

        return createWith(DAB_LIST_ITEM_SELECTED_NOTIFICATION, data);
    }

    /**
     * フォーカス位置変更要求パケット生成.
     *
     * @param index フォーカス位置のリストインデックス（1オリジン）
     * @return 送信パケット
     * @throws IllegalArgumentException {@code index}が0以下
     */
    @NonNull
    public OutgoingPacket createListFocusPositionChangeRequest(@IntRange(from = 1) int index) {
        checkArgument(1 <= index);

        byte[] indexByteArray = ushortToByteArray(index);
        byte[] data = {
                0x00,
                indexByteArray[0],
                indexByteArray[1]
        };

        return createWith(LIST_FOCUS_POSITION_CHANGE_REQUEST, data);
    }

    /**
     * リスト情報更新通知パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createListUpdatedNotificationResponse() {
        return createWith(LIST_UPDATED_NOTIFICATION_RESPONSE);
    }

    /**
     * 設定リスト情報更新通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSettingListUpdateNotificationResponse() {
        return createWith(SETTING_LIST_UPDATED_NOTIFICATION_RESPONSE);
    }

    /**
     * 連携切断通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDisconnectNotificationResponse() {
        return createWith(DISCONNECT_NOTIFICATION_RESPONSE);
    }

    /**
     * BEEP TONE設定通知パケット生成.
     *
     * @param isOn BEEP TONE設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBeepToneSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(BEEP_TONE_SETTING_NOTIFICATION, data);
    }

    /**
     * ATT/MUTE設定通知パケット生成.
     *
     * @param setting ATT/MUTE設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createAttMuteSettingNotification(@NonNull AttMuteSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(ATT_MUTE_SETTING_NOTIFICATION, data);
    }
    /**
     * 距離単位設定通知パケット生成.
     *
     * @param setting 距離単位設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createDistanceUnitSettingNotification(@NonNull DistanceUnit setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(DISTANCE_UNIT_SETTING_NOTIFICATION, data);
    }
    /**
     * DEMO設定通知パケット生成.
     *
     * @param isOn DEMO設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDemoSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(DEMO_SETTING_NOTIFICATION, data);
    }

    /**
     * POWER SAVE設定通知パケット生成.
     *
     * @param isOn POWER SAVE設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPowerSaveSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(POWER_SAVE_SETTING_NOTIFICATION, data);
    }

    /**
     * Bt Audio設定通知パケット生成.
     *
     * @param isOn Bt Audio設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBtAudioSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(BT_AUDIO_SETTING_NOTIFICATION, data);
    }

    /**
     * Pandora設定通知パケット生成.
     *
     * @param isOn Pandora設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPandoraSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(PANDORA_SETTING_NOTIFICATION, data);
    }

    /**
     * Spotify設定通知パケット生成.
     *
     * @param isOn Spotify設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSpotifySettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(SPOTIFY_SETTING_NOTIFICATION, data);
    }

    /**
     * AUX設定通知パケット生成.
     *
     * @param isOn AUX設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAuxSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(AUX_SETTING_NOTIFICATION, data);
    }

    /**
     * 99App自動起動設定通知パケット生成.
     *
     * @param isOn 99App自動起動設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAppAutoStartSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(APP_AUTO_START_SETTING_NOTIFICATION, data);
    }

    /**
     * USB AUTO設定通知パケット生成.
     *
     * @param isOn USB AUTO設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createUsbAutoSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(USB_AUTO_SETTING_NOTIFICATION, data);
    }

    /**
     * ステアリングリモコン設定通知パケット生成.
     *
     * @param type ステアリングリモコン設定種別
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createSteeringRemoteControlSettingNotification(@NonNull SteeringRemoteControlSettingType type) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code
        };

        return createWith(STEERING_REMOTE_CONTROL_SETTING_NOTIFICATION, data);
    }

    /**
     * AUTO PI設定通知パケット生成.
     *
     * @param isOn AUTO PI設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAutoPiSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(AUTO_PI_SETTING_NOTIFICATION, data);
    }

    /**
     * DISP OFF設定通知パケット生成.
     *
     * @param isOn AUTO PI設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDisplayOffSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(DISPLAY_OFF_SETTING_NOTIFICATION, data);
    }

    /**
     * EQ設定通知パケット生成.
     *
     * @param type EQ設定種別
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createEqualizerSettingNotification(@NonNull AudioSettingEqualizerType type) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code
        };

        return createWith(EQUALIZER_SETTING_NOTIFICATION, data);
    }

    /**
     * EQカスタム調整通知パケット生成.
     *
     * @param customEqType カスタムEQ種別。{@code 0x00}:CUSTOM1。{@code 0x01}:CUSTOM2。
     * @param bands BAND1からBAND13までの設定値
     * @return 送信パケット
     * @throws NullPointerException {@code customEqType}、または、{@code bands}がnull
     * @throws IllegalArgumentException {@code bands.length}が13ではない。
     */
    @NonNull
    public OutgoingPacket createEqualizerCustomAdjustNotification(@NonNull CustomEqType customEqType, @NonNull @Size(13) int[] bands) {
        checkArgument(checkNotNull(bands).length == 13);

        byte[] data = new byte[15];
        data[0] = 0x00;
        data[1] = (byte) checkNotNull(customEqType).code;
        int index = 2;
        for (int band : bands) {
            data[index] = (byte) band;
            ++index;
        }

        return createWith(EQUALIZER_CUSTOM_ADJUST_NOTIFICATION, data);
    }

    /**
     * FADER/BALANCE設定通知パケット生成.
     *
     * @param fader FADER設定値。2Way Network Mode時、FADER設定は無効なため0を設定する。
     * @param balance BALANCE設定値
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFaderBalanceSettingNotification(int fader, int balance) {
        byte[] data = {
                0x00,
                (byte) fader,
                (byte) balance
        };

        return createWith(FADER_BALANCE_SETTING_NOTIFICATION, data);
    }

    /**
     * SUBWOOFER設定通知パケット生成.
     *
     * @param setting SUBWOOFER設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createSubwooferSettingNotification(@NonNull SubwooferSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(SUBWOOFER_SETTING_NOTIFICATION, data);
    }

    /**
     * SUBWOOFER位相設定通知パケット生成.
     *
     * @param setting SUBWOOFER位相設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createSubwooferPhaseSettingNotification(@NonNull SubwooferPhaseSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(SUBWOOFER_PHASE_SETTING_NOTIFICATION, data);
    }

    /**
     * SPEAKER LEVEL設定通知パケット生成.
     *
     * @param type スピーカー種別
     * @param level レベル値
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     * @throws IllegalArgumentException {@code type}が不正
     */
    @NonNull
    public OutgoingPacket createSpeakerLevelSettingNotification(@NonNull MixedSpeakerType type, int level) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code,
                (byte) level
        };

        return createWith(SPEAKER_LEVEL_SETTING_NOTIFICATION, data);
    }

    /**
     * CROSSOVER HFP/LPF設定通知パケット生成.
     *
     * @param type スピーカー種別
     * @param isOn HPF/LPF設定がONが否か。
     *             スピーカーによってフィルター種別が変わる。
     *             <pre>
     *             -Standard Mode
     *              HPF : Front, Rear
     *              LPF : Subwoofer
     *             -2way Network Mode
     *              HPF : High, Mid-HPF
     *              LPF : Subwoofer, Mid-LPF
     *             </pre>
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createCrossoverHpfLpfSettingNotification(@NonNull SpeakerType type, boolean isOn) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(CROSSOVER_HPF_LPF_SETTING_NOTIFICATION, data);
    }

    /**
     * CROSSOVER カットオフ周波数設定通知パケット生成.
     *
     * @param type スピーカー種別
     * @param setting カットオフ周波数
     * @return 送信パケット
     * @throws NullPointerException {@code type}、または、{@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createCrossoverCutoffSettingNotification(@NonNull SpeakerType type, @NonNull CutoffSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code,
                (byte) checkNotNull(setting).getCode()
        };

        return createWith(CROSSOVER_CUTOFF_SETTING_NOTIFICATION, data);
    }

    /**
     * CROSSOVER スロープ設定通知パケット生成.
     *
     * @param type スピーカー種別
     * @param setting スロープ
     * @return 送信パケット
     * @throws NullPointerException {@code type}、または、{@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createCrossoverSlopeSettingNotification(@NonNull SpeakerType type, @NonNull SlopeSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code,
                (byte) checkNotNull(setting).getCode()
        };

        return createWith(CROSSOVER_SLOPE_SETTING_NOTIFICATION, data);
    }

    /**
     * LISTENING POSITION設定通知パケット生成.
     *
     * @param setting LISTENING POSITION設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createListeningPositionSettingNotification(@NonNull ListeningPositionSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(LISTENING_POSITION_SETTING_NOTIFICATION, data);
    }

    /**
     * TIME ALIGNMENT プリセット設定通知パケット生成.
     *
     * @param setting プリセット設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createTimeAlignmentPresetSettingNotification(@NonNull TimeAlignmentSettingMode setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(TIME_ALIGNMENT_PRESET_SETTING_NOTIFICATION, data);
    }

    /**
     * TIME ALIGNMENT カスタム調整設定通知パケット生成.
     *
     * @param type スピーカー種別
     * @param step ステップ値
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     * @throws IllegalArgumentException {@code type}が不正
     */
    @NonNull
    public OutgoingPacket createTimeAlignmentSettingNotification(@NonNull MixedSpeakerType type, int step) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code,
                (byte) step
        };

        return createWith(TIME_ALIGNMENT_SETTING_NOTIFICATION, data);
    }

    /**
     * AUTO EQ補正設定通知パケット生成.
     *
     * @param setting AUTO EQ補正設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createAutoEqCorrectionSettingNotification(@NonNull AutoEqCorrectionSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(AUTO_EQ_CORRECTION_SETTING_NOTIFICATION, data);
    }

    /**
     * SAVE SETTING実行通知パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSaveSettingNotification() {
        return createWith(SAVE_SETTING_NOTIFICATION);
    }

    /**
     * LOAD SETTING実行通知パケット生成.
     *
     * @param type LOAD SETTING種別
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createLoadSettingNotification(@NonNull LoadSettingsType type) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code
        };

        return createWith(LOAD_SETTING_NOTIFICATION, data);
    }

    /**
     * BASS BOOSTERレベル設定通知パケット生成.
     *
     * @param level BASS BOOSTERレベル
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBassBoosterLevelSettingNotification(int level) {
        byte[] data = {
                0x00,
                (byte) level
        };

        return createWith(BASS_BOOSTER_LEVEL_SETTING_NOTIFICATION, data);
    }

    /**
     * LOUDNESS設定通知パケット生成.
     *
     * @param setting LOUDNESS設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createLoudnessSettingNotification(@NonNull LoudnessSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(LOUDNESS_SETTING_NOTIFICATION, data);
    }

    /**
     * ALC設定通知パケット生成.
     *
     * @param setting ALC設定値
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createAlcSettingNotification(@NonNull AlcSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(ALC_SETTING_NOTIFICATION, data);
    }

    /**
     * SLA設定通知パケット生成.
     *
     * @param step SLA設定値
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSlaSettingNotification(int step) {
        byte[] data = {
                0x00,
                (byte) step
        };

        return createWith(SLA_SETTING_NOTIFICATION, data);
    }

    /**
     * [JASPER] EQ設定通知パケット生成.
     *
     * @param type EQ設定種別
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createJasperEqualizerSettingNotification(@NonNull AudioSettingEqualizerType type) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code
        };

        return createWith(JASPER_EQUALIZER_SETTING_NOTIFICATION, data);
    }

    /**
     * [JASPER] EQカスタム調整通知パケット生成.
     *
     * @param customEqType カスタムEQ種別
     * @param bands BAND1からBAND5までの設定値
     * @return 送信パケット
     * @throws NullPointerException {@code customEqType}、または、{@code bands}がnull
     * @throws IllegalArgumentException {@code bands.length}が5ではない。
     */
    @NonNull
    public OutgoingPacket createJasperEqualizerCustomAdjustNotification(@NonNull CustomEqType customEqType, @NonNull @Size(5) int[] bands) {
        checkArgument(checkNotNull(bands).length == 5);

        byte[] data = new byte[7];
        data[0] = 0x00;
        data[1] = (byte) checkNotNull(customEqType).code;
        int index = 2;
        for (int band : bands) {
            data[index] = (byte) band;
            ++index;
        }

        return createWith(JASPER_EQUALIZER_CUSTOM_ADJUST_NOTIFICATION, data);
    }

    /**
     * [JASPER] CROSSOVER HPF/LPF設定通知パケット生成.
     *
     * @param type FILTER種別
     * @param isOn HPF/LPF設定がONが否か
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createJasperCrossoverHpfLpfSettingNotification(@NonNull HpfLpfFilterType type, boolean isOn) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code,
                (byte) (isOn ? 0x01 : 0x00)
        };

        return createWith(JASPER_CROSSOVER_HPF_LPF_SETTING_NOTIFICATION, data);
    }

    /**
     * [JASPER] CROSSOVER カットオフ周波数設定通知パケット生成.
     *
     * @param type FILTER種別
     * @param setting カットオフ周波数
     * @return 送信パケット
     * @throws NullPointerException {@code type}、または、{@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createJasperCrossoverCutoffSettingNotification(@NonNull HpfLpfFilterType type, @NonNull CutoffSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code,
                (byte) (byte) checkNotNull(setting).getCode()
        };

        return createWith(JASPER_CROSSOVER_CUTOFF_SETTING_NOTIFICATION, data);
    }

    /**
     * [JASPER] CROSSOVER スロープ設定通知パケット生成.
     *
     * @param type FILTER種別
     * @param setting スロープ
     * @return 送信パケット
     * @throws NullPointerException {@code type}、または、{@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createJasperCrossoverSlopeSettingNotification(@NonNull HpfLpfFilterType type, @NonNull SlopeSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code,
                (byte) (byte) checkNotNull(setting).getCode()
        };

        return createWith(JASPER_CROSSOVER_SLOPE_SETTING_NOTIFICATION, data);
    }

    /**
     * [AC2] EQカスタム調整通知(車種専用セッティング：オーディオが有効時)パケット生成.
     *
     * @param customEqType カスタムEQ種別。{@link CustomEqType#CUSTOM1}を指定すること。
     * @param bands BAND1からBAND5までの設定値
     * @return 送信パケット
     * @throws NullPointerException {@code customEqType}、または、{@code bands}がnull
     * @throws IllegalArgumentException {@code bands.length}が5ではない。または、{@link CustomEqType#CUSTOM1}以外を指定。
     */
    @NonNull
    public OutgoingPacket createAc2EqualizerCustomAdjustNotification(@NonNull CustomEqType customEqType, @NonNull @Size(5) int[] bands) {
        checkArgument(checkNotNull(customEqType) == CustomEqType.CUSTOM1);
        checkArgument(checkNotNull(bands).length == 5);

        byte[] data = new byte[7];
        data[0] = 0x00;
        data[1] = (byte) checkNotNull(customEqType).code;
        int index = 2;
        for (int band : bands) {
            data[index] = (byte) band;
            ++index;
        }

        return createWith(AC2_EQUALIZER_CUSTOM_ADJUST_NOTIFICATION, data);
    }

    /**
     * [AC2] Beat Blaster設定通知パケット生成.
     *
     * @param setting Beat Blaster設定値
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createBeatBlasterSettingNotification(@NonNull BeatBlasterSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(BEAT_BLASTER_SETTING_NOTIFICATION, data);
    }

    /**
     * [AC2] LEVEL設定通知パケット生成.
     *
     * @param level LEVEL設定値
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createLevelSettingNotification(int level) {
        byte[] data = {
                0x00,
                (byte) level
        };

        return createWith(LEVEL_SETTING_NOTIFICATION, data);
    }

    /**
     * SPECIAL EQカ設定通知パケット生成.
     *
     * @param typeCode SPECIAL EQ種別。
     * @param bands BAND1からBAND13までの設定値
     * @return 送信パケット
     * @throws NullPointerException {@code customEqType}、または、{@code bands}がnull
     * @throws IllegalArgumentException {@code bands.length}が13ではない。
     */
    @NonNull
    public OutgoingPacket createSpecialEqualizerSettingNotification(int typeCode, @NonNull @Size(13) int[] bands) {
        checkArgument(checkNotNull(bands).length == 13);

        byte[] data = new byte[15];
        data[0] = 0x00;
        data[1] = (byte) typeCode;
        int index = 2;
        for (int band : bands) {
            data[index] = (byte) band;
            ++index;
        }

        return createWith(SPECIAL_EQUALIZER_SETTING_NOTIFICATION, data);
    }

    /**
     * EQ PRESET初期化通知パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createEqualizerPresetInitializationNotification() {
        return createWith(EQUALIZER_PRESET_INITIALIZATION_NOTIFICATION);
    }

    /**
     * SOUND RETRIEVER設定通知パケット生成.
     *
     * @param setting 設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createSoundRetrieverSettingNotification(@NonNull SoundRetrieverSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(SOUND_RETRIEVER_SETTING_NOTIFICATION, data);
    }

    /**
     * COLOR設定通知パケット生成.
     *
     * @param target 設定対象
     * @param color 設定色。無効な設定色（R,G,B = 255, 255, 255）は指定しないこと。本メソッドではチェックしない。
     * @return 送信パケット
     * @throws NullPointerException {@code target}、または、{@code color}がnull
     */
    @NonNull
    public OutgoingPacket createColorSettingNotification(@NonNull IlluminationTarget target, @NonNull IlluminationColor color) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(target).code,
                (byte) checkNotNull(color).code
        };

        return createWith(COLOR_SETTING_NOTIFICATION, data);
    }

    /**
     * CUSTOM COLOR設定通知パケット生成.
     * <p>
     * {@code red}、{@code green}、{@code blue}のどれか1つが10以上であること。
     *
     * @param target 設定対象
     * @param red RED
     * @param green GREEN
     * @param blue BLUE
     * @return 送信パケット
     * @throws NullPointerException {@code target}がnull
     * @throws IllegalArgumentException {@code red}、{@code green}、{@code blue}が不正
     */
    @NonNull
    public OutgoingPacket createCustomColorSettingNotification(@NonNull IlluminationTarget target,
                                                               @IntRange(from = 0, to = 60) int red,
                                                               @IntRange(from = 0, to = 60) int green,
                                                               @IntRange(from = 0, to = 60) int blue) {
        checkArgument(Math.max(red, Math.max(green, blue)) >= 10);

        byte[] data = {
                0x00,
                (byte) checkNotNull(target).code,
                (byte) red,
                (byte) green,
                (byte) blue
        };

        return createWith(CUSTOM_COLOR_SETTING_NOTIFICATION, data);
    }

    /**
     * DIMMER設定通知パケット生成.
     *
     * @param setting DIMMER設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createDimmerSettingNotification(@NonNull DimmerSetting.Dimmer setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(DIMMER_SETTING_NOTIFICATION, data);
    }

    /**
     * DIMMER時刻設定通知パケット生成.
     *
     * @param type 設定時刻
     * @param hour Hour
     * @param minute Min
     * @return 送信パケット
     * @throws NullPointerException {@code type}がnull
     */
    @NonNull
    public OutgoingPacket createDimmerTimeSettingNotification(@NonNull DimmerTimeType type, int hour, int minute) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code,
                (byte) hour,
                (byte) minute
        };

        return createWith(DIMMER_TIME_SETTING_NOTIFICATION, data);
    }

    /**
     * BRIGHTNESS設定通知パケット生成.
     *
     * @param brightness BRIGHTNESS設定
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBrightnessSettingNotification(int brightness) {
        byte[] data = {
                0x00,
                (byte) brightness
        };

        return createWith(BRIGHTNESS_SETTING_NOTIFICATION, data);
    }

    /**
     * BT PHONE COLOR設定通知パケット生成.
     *
     * @param color BT PHONE COLOR設定
     * @return 送信パケット
     * @throws NullPointerException {@code color}がnull
     */
    @NonNull
    public OutgoingPacket createBtPhoneColorSettingNotification(@NonNull BtPhoneColor color) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(color).code
        };

        return createWith(BT_PHONE_COLOR_SETTING_NOTIFICATION, data);
    }

    /**
     * 蛍の光風設定通知パケット生成.
     *
     * @param enabled 有効か否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createIlluminationEffectSettingNotification(boolean enabled) {
        byte[] data = {
                0x00,
                enabled ? (byte) 0x01 : (byte) 0x00
        };

        return createWith(ILLUMINATION_SETTING_NOTIFICATION, data);
    }

    /**
     * BRIGHTNESS設定通知（個別設定モデル）パケット生成.
     *
     * @param target 設定対象
     * @param brightness BRIGHTNESS設定
     * @return 送信パケット
     * @throws NullPointerException {@code target}がnull
     */
    @NonNull
    public OutgoingPacket createKeyDisplayBrightnessSettingNotification(@NonNull IlluminationTarget target, int brightness) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(target).code,
                (byte) brightness
        };

        return createWith(KEY_DISPLAY_BRIGHTNESS_SETTING_NOTIFICATION, data);
    }

    /**
     * CUSTOM発光パターン設定通知パケット生成.
     * <p>
     * 各設定値に不正がないかについては本メソッドではチェックしない。
     *
     * @param type 登録種別
     * @param transactionId トランザクションID
     * @param totalFrame フレーム総数
     * @param index フレームインデックス
     * @param zones ゾーン情報
     * @return 送信パケット
     * @throws NullPointerException {@code type}、{@code zone}がnull
     */
    @NonNull
    public OutgoingPacket createCustomFlashPatternSettingNotification(@NonNull FlashPatternRegistrationType type,
                                                                      int transactionId,
                                                                      int totalFrame,
                                                                      int index,
                                                                      @NonNull @Size(max = 110) ZoneFrameInfo[] zones) {
        checkNotNull(type);
        checkNotNull(zones);

        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        stream.write(0x00);
        // D1:登録種別
        stream.write((byte) type.code);
        // D2:トランザクションID
        stream.write((byte) transactionId);
        // D3:総フレーム数
        stream.write((byte) totalFrame);
        // D4:フレームインデックス
        stream.write((byte) index);
        // D5:送信フレーム数
        stream.write((byte) zones.length);
        // D6-N:フレーム情報
        for(ZoneFrameInfo zone : zones) {
            ZoneColorSpec[] specs = new ZoneColorSpec[]{zone.zone2, zone.zone3};
            for (ZoneColorSpec spec : specs) {
                stream.write((byte) spec.red);
                stream.write((byte) spec.green);
                stream.write((byte) spec.blue);
            }

            byte[] timerIdByteArray = PacketUtil.ushortToByteArray(zone.duration);
            stream.write(timerIdByteArray[0]);
            stream.write(timerIdByteArray[1]);
        }

        return createWith(CUSTOM_FLASH_PATTERN_SETTING_NOTIFICATION, stream.toByteArray());
    }

    /**
     * オーディオレベルメータ連動設定通知パケット生成.
     *
     * @param isOn オーディオレベルメータ連動設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAudioLevelMeterLinkedSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                isOn ? (byte) 0x01 : (byte) 0x00
        };

        return createWith(AUDIO_LEVEL_METER_LINKED_SETTING_NOTIFICATION, data);
    }

    /**
     * [SPH] BT PHONE COLOR設定通知パケット生成.
     *
     * @param color [SPH] BT PHONE COLOR設定
     * @return 送信パケット
     * @throws NullPointerException {@code color}がnull
     */
    @NonNull
    public OutgoingPacket createSphBtPhoneColorSettingNotification(@NonNull SphBtPhoneColorSetting color) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(color).code
        };

        return createWith(SPH_BT_PHONE_COLOR_SETTING_NOTIFICATION, data);
    }

    /**
     * COLOR設定通知(共通設定モデル)パケット生成.
     *
     * @param color 設定色。無効な設定色（R,G,B = 255, 255, 255）は指定しないこと。本メソッドではチェックしない。
     * @return 送信パケット
     * @throws NullPointerException {@code color}がnull
     */
    @NonNull
    public OutgoingPacket createCommonColorSettingNotification( @NonNull IlluminationColor color) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(color).code
        };

        return createWith(COMMON_COLOR_SETTING_NOTIFICATION, data);
    }

    /**
     * CUSTOM COLOR設定通知(共通設定モデル)パケット生成.
     * <p>
     * {@code red}、{@code green}、{@code blue}のどれか1つが10以上であること。
     *
     * @param red RED
     * @param green GREEN
     * @param blue BLUE
     * @return 送信パケット
     * @throws IllegalArgumentException {@code red}、{@code green}、{@code blue}が不正
     */
    @NonNull
    public OutgoingPacket createCommonCustomColorSettingNotification(@IntRange(from = 0, to = 60) int red,
                                                               @IntRange(from = 0, to = 60) int green,
                                                               @IntRange(from = 0, to = 60) int blue) {
        checkArgument(Math.max(red, Math.max(green, blue)) >= 10);

        byte[] data = {
                0x00,
                (byte) red,
                (byte) green,
                (byte) blue
        };

        return createWith(COMMON_CUSTOM_COLOR_SETTING_NOTIFICATION, data);
    }

    /**
     * メッセージ受信通知COLOR設定通知パケット生成.
     *
     * @param color メッセージ受信通知COLOR設定
     * @return 送信パケット
     * @throws NullPointerException {@code color}がnull
     */
    @NonNull
    public OutgoingPacket createIncomingMessageColorSettingNotification(@NonNull IncomingMessageColorSetting color) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(color).code
        };

        return createWith(INCOMING_MESSAGE_COLOR_SETTING_NOTIFICATION, data);
    }

    /**
     * Function設定通知.
     *
     * @param sourceType 現在のソース種別
     * @param typeCode 設定するFunction種別。{@code ***FunctionType#code}。
     * @param value 設定値。設定値は各Function種別を参照。
     * @return 送信パケット
     * @throws NullPointerException {@code sourceType}がnull
     * @see TunerFunctionType
     * @see HdRadioFunctionType
     * @see DabFunctionType
     */
    @NonNull
    public OutgoingPacket createFunctionSettingNotification(@NonNull MediaSourceType sourceType, int typeCode, int value) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(sourceType).code,
                (byte) typeCode,
                (byte) value
        };

        return createWith(FUNCTION_SETTING_NOTIFICATION, data);
    }

    /**
     * Audio Device 切替コマンド通知パケット生成.
     *
     * @param bdAddress BDアドレス。オクテット毎に:(半角コロン)で区切った16進数表現 ex)12:34:56:78:9A:BC
     * @return 送信パケット
     * @throws NullPointerException {@code bdAddress}がnull
     * @throws IllegalArgumentException {@code bdAddress}が不正
     */
    @NonNull
    public OutgoingPacket createAudioDeviceSwitchCommand(@NonNull String bdAddress) {
        byte[] data = new byte[19];
        copyBdAddressToData(data, 1, bdAddress);
        return createWith(AUDIO_DEVICE_SWITCH_COMMAND, data);
    }

    /**
     * Audio Device 切替完了通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAudioDeviceSwitchCompleteNotificationResponse() {
        return createWith(AUDIO_DEVICE_SWITCH_COMPLETE_NOTIFICATION_RESPONSE);
    }

    /**
     * サーチコマンド通知パケット生成.
     *
     * @param requestType 要求種別
     * @return 送信パケット
     * @throws NullPointerException {@code requestType}がnull
     */
    @NonNull
    public OutgoingPacket createPhoneSearchCommand(@NonNull PhoneSearchRequestType requestType) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(requestType).code,
        };

        return createWith(PHONE_SEARCH_COMMAND, data);
    }

    /**
     * ペアリング追加コマンド通知パケット生成.
     *
     * @param bdAddress BDアドレス
     * @param serviceTypes 接続要求サービス情報
     * @return 送信パケット
     * @throws NullPointerException {@code bdAddress}、または、{@code serviceTypes}がnull
     * @throws IllegalArgumentException {@code bdAddress}、または、{@code serviceTypes}が不正
     */
    @NonNull
    public OutgoingPacket createPairingAddCommand(@NonNull String bdAddress,
                                                  @NonNull @Size(min = 1) EnumSet<ConnectServiceType> serviceTypes) {
        checkArgument(1 <= checkNotNull(serviceTypes).size());

        byte[] data = new byte[20];
        copyBdAddressToData(data, 1, bdAddress);
        byte services = 0;
        if (serviceTypes.contains(ConnectServiceType.PHONE)) {
            services |= 1;
        }
        if (serviceTypes.contains(ConnectServiceType.AUDIO)) {
            services |= (1 << 1);
        }
        data[19] = services;

        return createWith(PAIRING_ADD_COMMAND, data);
    }

    /**
     * ペアリング削除コマンド通知パケット生成.
     *
     * @param bdAddress BDアドレス
     * @return 送信パケット
     * @throws NullPointerException {@code bdAddress}がnull
     * @throws IllegalArgumentException {@code bdAddress}が不正
     */
    @NonNull
    public OutgoingPacket createPairingDeleteCommand(@NonNull String bdAddress) {
        byte[] data = new byte[19];
        copyBdAddressToData(data, 1, bdAddress);
        return createWith(PAIRING_DELETE_COMMAND, data);
    }

    /**
     * サービスコネクトコマンド通知パケット生成.
     *
     * @param bdAddress BDアドレス
     * @param requestType 要求接続種別
     * @param serviceTypes サービス種別
     * @return 送信パケット
     * @throws NullPointerException {@code bdAddress}、{@code requestType}、{@code serviceTypes}のいずれかがnull
     * @throws IllegalArgumentException {@code bdAddress}、または、{@code serviceTypes}が不正
     */
    @NonNull
    public OutgoingPacket createPhoneServiceConnectCommand(
            @NonNull String bdAddress,
            @NonNull PhoneConnectRequestType requestType,
            @NonNull @Size(min = 1) EnumSet<ConnectServiceType> serviceTypes) {
        checkArgument(1 <= checkNotNull(serviceTypes).size());

        byte[] data = new byte[21];
        copyBdAddressToData(data, 1, bdAddress);
        data[19] = (byte) checkNotNull(requestType).code;
        if (serviceTypes.size() == ConnectServiceType.values().length) {
            data[20] = (byte) ConnectServiceType.ALL_CODE;
        } else if (serviceTypes.contains(ConnectServiceType.PHONE)) {
            data[20] = (byte) ConnectServiceType.PHONE.code;
        } else if (serviceTypes.contains(ConnectServiceType.AUDIO)) {
            data[20] = (byte) ConnectServiceType.AUDIO.code;
        } else {
            throw new AssertionError("can't happen.");
        }

        return createWith(PHONE_SERVICE_CONNECT_COMMAND, data);
    }

    /**
     * AUTO ANSWER設定通知パケット生成.
     *
     * @param isOn AUTO ANSWER設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAutoAnswerSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                isOn ? (byte) 0x01 : (byte) 0x00
        };

        return createWith(AUTO_ANSWER_SETTING_NOTIFICATION, data);
    }

    /**
     * AUTO PAIRING設定通知パケット生成.
     *
     * @param isOn AUTO PAIRING設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAutoPairingSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                isOn ? (byte) 0x01 : (byte) 0x00
        };

        return createWith(AUTO_PAIRING_SETTING_NOTIFICATION, data);
    }

    /**
     * サーチコマンド完了通知パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPhoneSearchCompleteNotificationResponse() {
        return createWith(PHONE_SEARCH_COMPLETE_NOTIFICATION_RESPONSE);
    }

    /**
     * ペアリング追加コマンド完了通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPairingAddCompleteNotificationResponse() {
        return createWith(PAIRING_ADD_COMPLETE_NOTIFICATION_RESPONSE);
    }

    /**
     * ペアリング削除コマンド完了通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPairingDeleteCompleteNotificationResponse() {
        return createWith(PAIRING_DELETE_COMPLETE_NOTIFICATION_RESPONSE);
    }

    /**
     * サービスコネクトコマンド完了通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPhoneServiceConnectCompleteNotificationResponse() {
        return createWith(PHONE_SERVICE_CONNECT_COMPLETE_NOTIFICATION_RESPONSE);
    }

    /**
     * パーキングセンサー設定通知パケット生成.
     *
     * @param isOn パーキングセンサー設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createParkingSensorSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                isOn ? (byte) 0x01 : (byte) 0x00
        };

        return createWith(PARKING_SENSOR_SETTING_NOTIFICATION, data);
    }

    /**
     * 警告音出力先設定通知パケット生成.
     *
     * @param setting 警告音出力先設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createAlarmOutputDestinationSettingNotification(@NonNull AlarmOutputDestinationSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(ALARM_OUTPUT_DESTINATION_SETTING_NOTIFICATION, data);
    }

    /**
     * 警告音量設定通知パケット生成.
     *
     * @param volume 警告音量設定
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAlarmVolumeSettingNotification(int volume) {
        byte[] data = {
                0x00,
                (byte) volume
        };

        return createWith(ALARM_VOLUME_SETTING_NOTIFICATION, data);
    }

    /**
     * バック信号極性設定通知パケット生成.
     *
     * @param setting バック信号極性設定通知
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createBackPolaritySettingNotification(@NonNull BackPolarity setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(BACK_POLARITY_SETTING_NOTIFICATION, data);
    }

    /**
     * ナビガイド音声設定通知パケット生成.
     *
     * @param isOn ナビガイド音声設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createNaviGuideVoiceSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                isOn ? (byte) 0x01 : (byte) 0x00
        };

        return createWith(NAVI_GUIDE_VOICE_SETTING_NOTIFICATION, data);
    }

    /**
     * ナビガイド音声ボリューム設定通知パケット生成.
     *
     * @param setting 設定
     * @return 送信パケット
     * @throws NullPointerException {@code setting}がnull
     */
    @NonNull
    public OutgoingPacket createNaviGuideVoiceVolumeSettingNotification(@NonNull NaviGuideVoiceVolumeSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(NAVI_GUIDE_VOICE_VOLUME_SETTING_NOTIFICATION, data);
    }

    /**
     * FM STEP設定通知パケット生成.
     *
     * @param step FM STEP設定
     * @return 送信パケット
     * @throws NullPointerException {@code step}がnull
     */
    @NonNull
    public OutgoingPacket createFmStepSettingNotification(@NonNull FmStep step) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(step).code
        };

        return createWith(FM_STEP_SETTING_NOTIFICATION, data);
    }

    /**
     * AM STEP設定通知パケット生成.
     *
     * @param step AM STEP設定
     * @return 送信パケット
     * @throws NullPointerException {@code step}がnull
     */
    @NonNull
    public OutgoingPacket createAmStepSettingNotification(@NonNull AmStep step) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(step).code
        };

        return createWith(AM_STEP_SETTING_NOTIFICATION, data);
    }

    /**
     * REAR出力設定/PREOUT出力設定通知パケット生成.
     *
     * @param setting REAR出力設定/PREOUT出力設定
     * @return 送信パケット
     * @throws NullPointerException {@code step}がnull
     */
    @NonNull
    public OutgoingPacket createRearOutputPreoutOutputSettingNotification(@NonNull RearOutputPreoutOutputSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(REAR_OUTPUT_PREOUT_OUTPUT_SETTING_NOTIFICATION, data);
    }

    /**
     * REAR出力設定通知パケット生成.
     *
     * @param setting REAR出力設定
     * @return 送信パケット
     * @throws NullPointerException {@code step}がnull
     */
    @NonNull
    public OutgoingPacket createRearOutputSettingNotification(@NonNull RearOutputSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(REAR_OUTPUT_SETTING_NOTIFICATION, data);
    }

    /**
     * MENU表示言語設定通知パケット生成.
     *
     * @param type MENU表示言語設定
     * @return 送信パケット
     * @throws NullPointerException {@code step}がnull
     */
    @NonNull
    public OutgoingPacket createMenuDisplayLanguageSettingNotification(@NonNull MenuDisplayLanguageType type) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(type).code
        };

        return createWith(MENU_DISPLAY_LANGUAGE_SETTING_NOTIFICATION, data);
    }

    /**
     * Super轟設定通知パケット生成.
     *
     * @param setting Super轟設定
     * @return 送信パケット
     * @throws NullPointerException {@code step}がnull
     */
    @NonNull
    public OutgoingPacket createSuperTodorokiSettingNotification(@NonNull SuperTodorokiSetting setting) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(setting).code
        };

        return createWith(SUPER_TODOROKI_SETTING_NOTIFICATION, data);
    }

    /**
     * Small Car TA設定通知パケット生成.
     *
     * @param type Small Car TA設定
     * @param position Listening Position設定
     * @return 送信パケット
     * @throws NullPointerException {@code type}、または、{@code position}がnull
     */
    @NonNull
    public OutgoingPacket createSmallCarTaSettingNotification(@NonNull SmallCarTaSettingType type, @NonNull ListeningPosition position) {
        int[] stepValue =
                checkNotNull(position) == ListeningPosition.LEFT ? checkNotNull(type).leftStepValue : checkNotNull(type).rightStepValue;
        byte[] data = {
                0x00,
                (byte) type.code,
                (byte) position.code,
                (byte) stepValue[0],
                (byte) stepValue[1],
                (byte) stepValue[2],
                (byte) stepValue[3]
        };

        return createWith(SMALL_CAR_TA_SETTING_NOTIFICATION, data);
    }

    /**
     * ライブシミュレーション設定通知パケット生成.
     *
     * @param soundFieldControlSettingType Sound Field Control設定
     * @param effectSettingType Sound Effect設定
     * @return 送信パケット
     * @throws NullPointerException {@code soundFieldControlSettingType}、または、{@code effectSettingType}がnull
     */
    @NonNull
    public OutgoingPacket createLiveSimulationSettingNotification(@NonNull SoundFieldControlSettingType soundFieldControlSettingType, @NonNull SoundEffectSettingType effectSettingType) {

        byte[] data = {
                0x00,
                (byte) checkNotNull(soundFieldControlSettingType).code,
                (byte) checkNotNull(effectSettingType).code
        };

        return createWith(LIVE_SIMULATION_SETTING_NOTIFICATION, data);
    }

    /**
     * カラオケ設定通知パケット生成.
     *
     * @param isOn カラオケ設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createKaraokeSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                isOn ? (byte) 0x01 : (byte) 0x00
        };

        return createWith(KARAOKE_SETTING_NOTIFICATION, data);
    }

    /**
     * マイク音量設定通知パケット生成.
     *
     * @param volume マイク音量設定
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createMicVolumeSettingNotification(int volume) {
        byte[] data = {
                0x00,
                (byte) volume
        };

        return createWith(MIC_VOLUME_SETTING_NOTIFICATION, data);
    }

    /**
     * Vocal Cancel設定通知パケット生成.
     *
     * @param isOn Vocal Cancel設定がONか否か
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createVocalCancelSettingNotification(boolean isOn) {
        byte[] data = {
                0x00,
                isOn ? (byte) 0x01 : (byte) 0x00
        };

        return createWith(VOCAL_CANCEL_SETTING_NOTIFICATION, data);
    }

    /**
     * リスト初期情報要求パケット生成.
     *
     * @param sourceType ソース種別
     * @param listType リスト種別
     * @return 送信パケット
     * @throws NullPointerException {@code sourceType}、または、{@code listType}がnull
     */
    @NonNull
    public OutgoingPacket createInitialListInfoRequest(@NonNull MediaSourceType sourceType, @NonNull ListType listType) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(sourceType).code,
                (byte) checkNotNull(listType).code
        };

        return createWith(INITIAL_LIST_INFO_REQUEST, data);
    }

    /**
     * リスト情報要求パケットハンドラ.
     *
     * @param transactionId トランザクションID
     * @param sourceType ソース種別
     * @param listType リスト種別
     * @param listIndex リストインデックス
     * @param limit リスト取得件数
     * @return 送信パケット
     * @throws NullPointerException {@code sourceType}、または、{@code listType}がnull
     */
    @NonNull
    public OutgoingPacket createListInfoRequest(int transactionId,
                                                @NonNull MediaSourceType sourceType,
                                                @NonNull ListType listType,
                                                int listIndex,
                                                int limit) {
        byte[] transactionIdByteArray = PacketUtil.ushortToByteArray(transactionId);
        byte[] listIndexByteArray = PacketUtil.ushortToByteArray(listIndex);
        byte[] limitByteArray = PacketUtil.ushortToByteArray(limit);
        byte[] data = {
                0x00,
                transactionIdByteArray[0],
                transactionIdByteArray[1],
                (byte) checkNotNull(sourceType).code,
                (byte) checkNotNull(listType).code,
                listIndexByteArray[0],
                listIndexByteArray[1],
                limitByteArray[0],
                limitByteArray[1]
        };

        return createWith(LIST_INFO_REQUEST, data);
    }

    /**
     * 設定リスト初期情報要求パケット生成.
     *
     * @param listType 設定種別
     * @return 送信パケット
     * @throws NullPointerException {@code listType}がnull
     */
    @NonNull
    public OutgoingPacket createInitialSettingListInfoRequest(@NonNull SettingListType listType) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(listType).settingTypeCode,
                (byte) checkNotNull(listType).listTypeCode
        };

        return createWith(INITIAL_SETTING_LIST_INFO_REQUEST, data);
    }

    /**
     * 設定リスト情報要求パケット生成.
     *
     * @param transactionId トランザクションID
     * @param listType 設定リスト種別
     * @param listIndex リストインデックス
     * @return 送信パケット
     * @throws NullPointerException {@code listType}がnull
     */
    @NonNull
    public OutgoingPacket createSettingListInfoRequest(int transactionId, @NonNull SettingListType listType, int listIndex) {
        byte[] transactionIdByteArray = PacketUtil.ushortToByteArray(transactionId);
        byte[] listIndexByteArray = PacketUtil.ushortToByteArray(listIndex);
        byte[] data = {
                0x00,
                transactionIdByteArray[0],
                transactionIdByteArray[1],
                (byte) checkNotNull(listType).settingTypeCode,
                (byte) checkNotNull(listType).listTypeCode,
                listIndexByteArray[0],
                listIndexByteArray[1]
        };

        return createWith(SETTING_LIST_INFO_REQUEST, data);
    }

    /**
     * ペアリングデバイスリスト情報要求パケット生成.
     *
     * @param standardType ペアリング規格種別
     * @return 送信パケット
     * @throws NullPointerException {@code standardType}がnull
     */
    @NonNull
    public OutgoingPacket createPairingDeviceListInfoRequest(@NonNull PairingSpecType standardType) {
        byte[] data = {
                0x00,
                (byte) checkNotNull(standardType).code
        };

        return createWith(PAIRING_DEVICE_LIST_INFO_REQUEST, data);
    }

    /**
     * ペアリングデバイス情報要求パケット生成.
     *
     * @param bdAddress BDアドレス
     * @param standardType ペアリング規格種別
     * @return 送信パケット
     * @throws NullPointerException {@code bdAddress}、{@code standardType}がnull
     */
    @NonNull
    public OutgoingPacket createPairingDeviceInfoRequest(@NonNull String bdAddress, @NonNull PairingSpecType standardType) {
        byte[] data = new byte[20];
        data[1] = (byte) checkNotNull(standardType).code;
        copyBdAddressToData(data, 2, bdAddress);


        return createWith(PAIRING_DEVICE_INFO_REQUEST, data);
    }

    /**
     * オーディオ情報通知パケット生成.
     *
     * @param info App Music（Android Music）の情報
     * @param type 生成する情報種別
     * @param carDeviceSpec 車載機のスペック情報
     * @return 送信パケット
     * @throws NullPointerException {@code info}、{@code type}、{@code carDeviceSpec}のいずれかがnull
     * @throws IllegalArgumentException {@code type}が不正（情報種別追加時の対応が漏れている）
     */
    @NonNull
    public OutgoingPacket createSmartPhoneAudioInfoNotification(
            @NonNull AndroidMusicMediaInfo info,
            @NonNull SmartPhoneMediaInfoType type,
            @NonNull CarDeviceSpec carDeviceSpec) {
        checkNotNull(info);
        checkNotNull(type);
        checkNotNull(carDeviceSpec);

        String text;
        switch (type) {
            case SONG_NAME:
                text = left(info.songTitle, carDeviceSpec.maxCharLength);
                break;
            case ARTIST_NAME:
                text = left(info.artistName, carDeviceSpec.maxCharLength);
                break;
            case ALBUM_NAME:
                text = left(info.albumTitle, carDeviceSpec.maxCharLength);
                break;
            case GENRE:
                text = left(info.genre, carDeviceSpec.maxCharLength);
                break;
            default:
                throw new IllegalArgumentException("invalid type: " + type);
        }
        if(mStatusHolder.getAppStatus().isLaunchedThirdPartyAudioApp){
            text = "APP";
        }

        if(mStatusHolder.getAppStatus().appMusicAudioMode == AudioMode.ALEXA){
            RenderPlayerInfoItem renderPlayerInfoItem = mStatusHolder.getAppStatus().playerInfoItem;
            if(renderPlayerInfoItem!=null) {
                AlexaIfDirectiveItem.Content content = renderPlayerInfoItem.content;
                ArrayList<String> textInfoList = new ArrayList<>();
                if (content.getTitle() != null) {
                    textInfoList.add(content.getTitle());
                }
                if (content.getTitleSubtext1() != null) {
                    textInfoList.add(content.getTitleSubtext1());
                }
                if (content.getTitleSubtext2() != null) {
                    textInfoList.add(content.getTitleSubtext2());
                }
                if (content.getHeader() != null) {
                    textInfoList.add(content.getHeader());
                }
                if (content.getHeaderSubtext1() != null) {
                    textInfoList.add(content.getHeaderSubtext1());
                }
                switch (type) {
                    case SONG_NAME:
                        if(textInfoList.size()>0) {
                            text = left(textInfoList.get(0), carDeviceSpec.maxCharLength);
                        }else{
                            text = "";
                        }
                        break;
                    case ARTIST_NAME:
                        if(textInfoList.size()>1) {
                            text = left(textInfoList.get(1), carDeviceSpec.maxCharLength);
                        }else{
                            text = "";
                        }
                        break;
                    case ALBUM_NAME:
                        if(textInfoList.size()>2) {
                            text = left(textInfoList.get(2), carDeviceSpec.maxCharLength);
                        }else{
                            text = "";
                        }
                        break;
                    case GENRE:
                        text = "";
                        break;
                    default:
                        throw new IllegalArgumentException("invalid type: " + type);
                }
            }
        }
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        // D0:通知
        stream.write(0x00);
        // D1:ソース情報
        stream.write(MediaSourceType.APP_MUSIC.code);
        // D2:情報種別
        stream.write(type.code);
        // D3-N:文字列
        int length = 0;
        // ID,TYPE,D0,D1,D2の実際のパケットサイズを計算
        length += PacketUtil.isDLE((byte) SMART_PHONE_AUDIO_INFO_NOTIFICATION.id) ? 2 : 1;
        length += PacketUtil.isDLE((byte) SMART_PHONE_AUDIO_INFO_NOTIFICATION.type) ? 2 : 1;
        length += PacketUtil.isDLE((byte) SMART_PHONE_AUDIO_INFO_NOTIFICATION.d0) ? 2 : 1;
        length += PacketUtil.isDLE((byte) MediaSourceType.APP_MUSIC.code) ? 2 : 1;
        length += PacketUtil.isDLE((byte) type.code) ? 2 : 1;

        try {
            for (int i = 0; i < text.length(); i++) {
                byte[] bytes = text.substring(i, i + 1).getBytes("UTF-8");
                for (byte b : bytes) {
                    length += PacketUtil.isDLE(b) ? 2 : 1;
                }
                // DLE,STX,NULL,CS(念のために2バイト分),DLE,ETXの合計が7バイト
                if (length > (1024 - 7)) {
                    break; // これ以上writeするとパケットサイズオーバーするので
                }

                stream.write(bytes);
            }
            stream.write(0x00); // 終端文字
        } catch (IOException e) {
            // ignore
        }

        return createWith(SMART_PHONE_AUDIO_INFO_NOTIFICATION, stream.toByteArray());
    }

    /**
     * 楽曲再生時間通知パケット生成.
     *
     * @param sourceType ソース種別
     * @param length 総再生時間（秒）
     * @param position 現在の再生時間（秒）
     * @return 送信パケット
     * @throws NullPointerException {@code sourceType}がnull
     */
    @NonNull
    public OutgoingPacket createSmartPhoneAudioPlaybackTimeNotification(@NonNull MediaSourceType sourceType,
                                                                        int length,
                                                                        int position) {
        if(mStatusHolder.getAppStatus().isLaunchedThirdPartyAudioApp){
            length = 0;
            position = 0;
        }
        byte[] lengthByteArray = ushortToByteArray(length);
        byte[] positionByteArray = ushortToByteArray(position);

        byte[] data = {
                0x00,
                (byte) checkNotNull(sourceType).code,
                lengthByteArray[0],
                lengthByteArray[1],
                positionByteArray[0],
                positionByteArray[1]
        };

        return createWith(SMART_PHONE_AUDIO_PLAYBACK_TIME_NOTIFICATION, data);
    }

    /**
     * 選択リスト表示情報通知パケット生成.
     *
     * @param version プロトコルバージョン
     * @param sourceType ソース種別
     * @param hasParent 上方向の階層があるか否か
     * @param hasChildren 下方向の階層があるか否か
     * @param currentPosition リスト階層数
     * @param info サブディスプレイに表示する現在のカテゴリ情報
     * @param text 車載機側の画面に表示する文字列
     * @return 送信パケット
     * @throws NullPointerException {@code version}、{@code sourceType}、{@code info}、{@code text}のいずれかがnull
     */
    @NonNull
    public OutgoingPacket createSelectedListDisplayInfoNotification(@NonNull ProtocolVersion version,
                                                                    @NonNull MediaSourceType sourceType,
                                                                    boolean hasParent,
                                                                    boolean hasChildren,
                                                                    int currentPosition,
                                                                    @NonNull SubDisplayInfo info,
                                                                    @NonNull String text) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        // D0:通知
        stream.write(0x00);
        // D1:ソース情報
        stream.write(checkNotNull(sourceType).code);
        // D2:階層情報
        //  bit[4-7]:リスト階層数
        int d2 = 0;
        if (checkNotNull(version).isGreaterThanOrEqual(ProtocolVersion.V4)) {
            d2 |= (currentPosition << 4);
        }
        //  bit[1]:上階層
        d2 |= (hasParent ? 1 << 1 : 0);
        //  bit[0]:下階層
        d2 |= (hasChildren ? 1 : 0);

        stream.write(d2);
        // D3:サブディスプレイ情報
        stream.write(checkNotNull(info).code);
        // D4-D68:文字列
        int length = 0;
        try {
            checkNotNull(text);
            for (int i = 0; i < text.length(); i++) {
                byte[] bytes = text.substring(i, i + 1).getBytes("UTF-8");
                for (byte b : bytes) {
                    length += PacketUtil.isDLE(b) ? 2 : 1;
                }

                // 終端文字を除くバイトサイズが65バイトより大きくならないように調整
                if (length + 1 > 65) {
                    break; // これ以上writeするとパケットサイズオーバーするので
                }

                stream.write(bytes);
            }
            stream.write(0x00); // 終端文字
        } catch (IOException e) {
            // ignore
        }

        return createWith(SELECTED_LIST_DISPLAY_INFO_NOTIFICATION, stream.toByteArray());
    }

    /**
     * BEEP TONE設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBeepToneSettingInfoRequest() {
        return createWith(BEEP_TONE_SETTING_INFO_REQUEST);
    }

    /**
     * ATT/MUTE設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAttMuteSettingInfoRequest() {
        return createWith(ATT_MUTE_SETTING_INFO_REQUEST);
    }

    /**
     * DEMO設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDemoSettingInfoRequest() {
        return createWith(DEMO_SETTING_INFO_REQUEST);
    }

    /**
     * POWER SAVE設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPowerSaveSettingInfoRequest() {
        return createWith(POWER_SAVE_SETTING_INFO_REQUEST);
    }

    /**
     * BT Audio設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBtAudioSettingInfoRequest() {
        return createWith(BT_AUDIO_SETTING_INFO_REQUEST);
    }

    /**
     * Pandora設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createPandoraSettingInfoRequest() {
        return createWith(PANDORA_SETTING_INFO_REQUEST);
    }

    /**
     * Spotify設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSpotifySettingInfoRequest() {
        return createWith(SPOTIFY_SETTING_INFO_REQUEST);
    }

    /**
     * AUX設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAuxSettingInfoRequest() {
        return createWith(AUX_SETTING_INFO_REQUEST);
    }

    /**
     * 99APP自動起動設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAppAutoStartSettingInfoRequest() {
        return createWith(APP_AUTO_START_SETTING_INFO_REQUEST);
    }

    /**
     * USB AUTO設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createUsbAutoSettingInfoRequest() {
        return createWith(USB_AUTO_SETTING_INFO_REQUEST);
    }

    /**
     * ステアリングリモコン設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSteeringRemoteControlSettingInfoRequest() {
        return createWith(STEERING_REMOTE_CONTROL_SETTING_INFO_REQUEST);
    }

    /**
     * AUTO PI設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAutoPiSettingInfoRequest() {
        return createWith(AUTO_PI_SETTING_INFO_REQUEST);
    }

    /**
     * DISP OFF設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDisplayOffSettingInfoRequest() {
        return createWith(DISPLAY_OFF_SETTING_INFO_REQUEST);
    }

    /**
     * 距離単位設定情報要求パケット作成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDistanceUnitSettingInfoRequest() {
        return createWith(DISTANCE_UNIT_SETTING_INFO_REQUEST);
    }

    /**
     * [OPAL] EQ設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createEqualizerSettingInfoRequest() {
        return createWith(EQUALIZER_SETTING_INFO_REQUEST);
    }

    /**
     * FADER/BALANCE設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFaderBalanceSettingInfoRequest() {
        return createWith(FADER_BALANCE_SETTING_INFO_REQUEST);
    }

    /**
     * SUBWOOFER設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSubwooferSettingInfoRequest() {
        return createWith(SUBWOOFER_SETTING_INFO_REQUEST);
    }

    /**
     * SUBWOOFER位相設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSubwooferPhaseSettingInfoRequest() {
        return createWith(SUBWOOFER_PHASE_SETTING_INFO_REQUEST);
    }

    /**
     * SPEAKER LEVEL情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSpeakerLevelSettingInfoRequest() {
        return createWith(SPEAKER_LEVEL_SETTING_INFO_REQUEST);
    }

    /**
     * CROSSOVER設定情報要求パケット生成.
     *
     * @param speakerType スピーカー種別
     * @return 送信パケット
     * @throws NullPointerException {@code speakerType}がnull
     */
    @NonNull
    public OutgoingPacket createCrossoverSettingRequest(@NonNull SpeakerType speakerType) {
        byte[] data = { 0x00, (byte) checkNotNull(speakerType).code };
        return createWith(CROSSOVER_SETTING_INFO_REQUEST, data);
    }

    /**
     * LISTENING POSITION設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createListeningPositionSettingInfoRequest() {
        return createWith(LISTENING_POSITION_SETTING_INFO_REQUEST);
    }

    /**
     * TIME ALIGNMENT設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createTimeAlignmentSettingInfoRequest() {
        return createWith(TIME_ALIGNMENT_SETTING_INFO_REQUEST);
    }

    /**
     * Auto EQ補正設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAutoEqCorrectionSettingInfoRequest() {
        return createWith(AUTO_EQ_CORRECTION_SETTING_INFO_REQUEST);
    }

    /**
     * BASS BOOSTERレベル設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBassBoosterLevelSettingInfoRequest() {
        return createWith(BASS_BOOSTER_LEVEL_SETTING_INFO_REQUEST);
    }

    /**
     * LOUDNESS設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createLoudnessSettingInfoRequest() {
        return createWith(LOUDNESS_SETTING_INFO_REQUEST);
    }

    /**
     * ALC設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAlcSettingInfoRequest() {
        return createWith(ALC_SETTING_INFO_REQUEST);
    }

    /**
     * SLA設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSlaSettingInfoRequest() {
        return createWith(SLA_SETTING_INFO_REQUEST);
    }

    /**
     * [JASPER] EQ設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createJasperEqualizerSettingInfoRequest() {
        return createWith(JASPER_EQUALIZER_SETTING_INFO_REQUEST);
    }

    /**
     * [JASPER] CROSSOVER HPF/LPF設定情報要求パケット生成.
     *
     * @param filterType FILTER種別
     * @return 送信パケット
     * @throws NullPointerException {@code filterType}がnull
     */
    @NonNull
    public OutgoingPacket createJasperCrossoverSettingRequest(@NonNull HpfLpfFilterType filterType) {
        byte[] data = { 0x00, (byte) checkNotNull(filterType).code };
        return createWith(JASPER_CROSSOVER_SETTING_INFO_REQUEST, data);
    }

    /**
     * [AC2] EQ設定情報要求 (車種専用チューニング：オーディオが有効時)パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAc2EqualizerSettingInfoRequest() {
        return createWith(AC2_EQUALIZER_SETTING_INFO_REQUEST);
    }

    /**
     * [AC2] Beat Blasterレベル設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBeatBlasterLevelSettingInfoRequest() {
        return createWith(BEAT_BLASTER_LEVEL_SETTING_INFO_REQUEST);
    }

    /**
     * [AC2] LEVEL設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createLevelSettingInfoRequest() {
        return createWith(LEVEL_SETTING_INFO_REQUEST);
    }

    /**
     * EQカスタム設定情報応答パケット生成.
     *
     * @param customEqType カスタムEQ種別
     * @return 送信パケット
     * @throws NullPointerException {@code customEqType}がnull
     */
    @NonNull
    public OutgoingPacket createCustomEqualizerSettingRequest(@NonNull CustomEqType customEqType) {
        byte[] data = { 0x00, (byte) checkNotNull(customEqType).code };
        return createWith(CUSTOM_EQUALIZER_SETTING_INFO_NOTIFICATION_RESPONSE, data);
    }

    /**
     * SOUND RETRIEVER設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSoundRetrieverSettingInfoRequest() {
        return createWith(SOUND_RETRIEVER_SETTING_INFO_REQUEST);
    }

    /**
     * KEY COLOR設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createKeyColorSettingInfoRequest() {
        return createWith(KEY_COLOR_SETTING_INFO_REQUEST);
    }

    /**
     * DISP COLOR設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDispColorSettingInfoRequest() {
        return createWith(DISP_COLOR_SETTING_INFO_REQUEST);
    }

    /**
     * KEY COLOR一括情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createKeyColorBulkSettingInfoRequest() {
        return createWith(KEY_COLOR_BULK_SETTING_INFO_REQUEST);
    }

    /**
     * DISP COLOR一括情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDispColorBulkSettingInfoRequest() {
        return createWith(DISP_COLOR_BULK_SETTING_INFO_REQUEST);
    }

    /**
     * DIMMER設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDimmerSettingInfoRequest() {
        return createWith(DIMMER_SETTING_INFO_REQUEST);
    }

    /**
     * BRIGHTNESS設定情報要求（共通設定モデル用）パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBrightnessSettingInfoRequest() {
        return createWith(BRIGHTNESS_SETTING_INFO_REQUEST);
    }

    /**
     * BT PHONE COLOR情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBtPhoneColorSettingInfoRequest() {
        return createWith(BT_PHONE_COLOR_SETTING_INFO_REQUEST);
    }

    /**
     * 蛍の光風設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createIlluminationSettingInfoRequest() {
        return createWith(ILLUMINATION_SETTING_INFO_REQUEST);
    }

    /**
     * KEY BRIGHTNESS設定情報要求（個別設定モデル用）パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createKeyBrightnessSettingInfoRequest() {
        return createWith(KEY_BRIGHTNESS_SETTING_INFO_REQUEST);
    }

    /**
     * DISP BRIGHTNESS設定情報要求（個別設定モデル用）パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDisplayBrightnessSettingInfoRequest() {
        return createWith(DISPLAY_BRIGHTNESS_SETTING_INFO_REQUEST);
    }

    /**
     * オーディオレベルメータ連動設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAudioLevelMeterLinkedSettingInfoRequest() {
        return createWith(AUDIO_LEVEL_METER_LINKED_SETTING_INFO_REQUEST);
    }

    /**
     * [SPH] BT PHONE COLOR設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSphBtPhoneColorSettingInfoRequest() {
        return createWith(SPH_BT_PHONE_COLOR_SETTING_INFO_REQUEST);
    }

    /**
     * COLOR設定情報要求  (共通設定モデル用)パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createCommonColorSettingInfoRequest() {
        return createWith(COMMON_COLOR_SETTING_INFO_REQUEST);
    }

    /**
     * COLOR一括情報要求  (共通設定モデル用)パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createCommonColorBulkSettingInfoRequest() {
        return createWith(COMMON_COLOR_BULK_SETTING_INFO_REQUEST);
    }

    /**
     * メッセージ受信通知COLOR設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createIncomingMessageColorSettingInfoRequest() {
        return createWith(INCOMING_MESSAGE_SETTING_INFO_REQUEST);
    }

    /**
     * Function設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFunctionSettingInfoRequest() {
        return createWith(FUNCTION_SETTING_INFO_REQUEST);
    }

    /**
     * パーキングセンサー設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createParkingSensorSettingInfoRequest() {
        return createWith(PARKING_SENSOR_SETTING_INFO_REQUEST);
    }

    /**
     * 警告音出力先設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAlarmOutputDestinationSettingInfoRequest() {
        return createWith(ALARM_OUTPUT_DESTINATION_SETTING_INFO_REQUEST);
    }

    /**
     * 警告音量設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAlarmVolumeSettingInfoRequest() {
        return createWith(ALARM_VOLUME_SETTING_INFO_REQUEST);
    }

    /**
     * バック信号極性設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createBackPolaritySettingInfoRequest() {
        return createWith(BACK_POLARITY_SETTING_INFO_REQUEST);
    }

    /**
     * ナビガイド音声設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createNaviGuideVoiceSettingInfoRequest() {
        return createWith(NAVI_GUIDE_VOICE_SETTING_INFO_REQUEST);
    }

    /**
     * ナビガイド音声ボリューム設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createNaviGuideVoiceVolumeSettingInfoRequest() {
        return createWith(NAVI_GUIDE_VOICE_VOLUME_SETTING_INFO_REQUEST);
    }

    /**
     * FM STEP設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createFmStepSettingInfoRequest() {
        return createWith(FM_STEP_SETTING_INFO_REQUEST);
    }

    /**
     * AM STEP設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAmStepSettingInfoRequest() {
        return createWith(AM_STEP_SETTING_INFO_REQUEST);
    }

    /**
     * REAR出力設定/PREOUT出力設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createRearOutputPreoutOutputSettingInfoRequest() {
        return createWith(REAR_OUTPUT_PREOUT_OUTPUT_SETTING_INFO_REQUEST);
    }

    /**
     * REAR出力設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createRearOutputSettingInfoRequest() {
        return createWith(REAR_OUTPUT_SETTING_INFO_REQUEST);
    }

    /**
     * MENU表示言語設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createMenuDisplayLanguageSettingInfoRequest() {
        return createWith(MENU_DISPLAY_LANGUAGE_SETTING_INFO_REQUEST);
    }

    /**
     * AUTO ANSWER設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAutoAnswerSettingInfoRequest() {
        return createWith(AUTO_ANSWER_SETTING_INFO_REQUEST);
    }

    /**
     * AUTO PAIRING設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createAutoPairingSettingInfoRequest() {
        return createWith(AUTO_PAIRING_SETTING_INFO_REQUEST);
    }

    /**
     * Super轟設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSuperTodorokiSettingInfoRequest() {
        return createWith(SUPER_TODOROKI_SETTING_INFO_REQUEST);
    }

    /**
     * Small Car TA設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createSmallCarTaSettingInfoRequest() {
        return createWith(SMALL_CAR_TA_SETTING_INFO_REQUEST);
    }

    /**
     * ライブシミュレーション設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createLiveSimulationSettingInfoRequest() {
        return createWith(LIVE_SIMULATION_SETTING_INFO_REQUEST);
    }

    /**
     * カラオケ設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createKaraokeSettingInfoRequest() {
        return createWith(KARAOKE_SETTING_INFO_REQUEST);
    }

    /**
     * マイク音量設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createMicVolumeSettingInfoRequest() {
        return createWith(MIC_VOLUME_SETTING_INFO_REQUEST);
    }

    /**
     * Vocal Cancel設定情報要求パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createVocalCancelSettingInfoRequest() {
        return createWith(VOCAL_CANCEL_SETTING_INFO_REQUEST);
    }

    /**
     * SmartPhone割り込み情報通知応答パケット生成.
     *
     * @return 送信パケット
     * @throws NullPointerException {@code type}、{@code text}がnull
     */
    @NonNull
    public OutgoingPacket createSmartPhoneInterruptNotification(@NonNull SmartPhoneInterruptType interruptType,
                                                                @NonNull String text,
                                                                @NonNull InterruptFlashPatternDirecting requestType) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        // D0:通知
        stream.write(0x00);
        // D1:ソース情報
        stream.write(checkNotNull(interruptType).code);
        // D2:発光パターン演出
        stream.write(checkNotNull(requestType).code);
        // D3-N:文字列
        if(interruptType == SmartPhoneInterruptType.RELEASE){
            stream.write(0x00);
        } else {
            int length = 0;
            try {
                checkNotNull(text);
                for (int i = 0; i < text.length(); i++) {
                    byte[] bytes = text.substring(i, i + 1).getBytes("UTF-8");
                    for (byte b : bytes) {
                        length += PacketUtil.isDLE(b) ? 2 : 1;
                    }

                    // 終端文字を除くバイトサイズが65バイトより大きくならないように調整
                    if (length + 1 > 65) {
                        break; // これ以上writeするとパケットサイズオーバーするので
                    }

                    stream.write(bytes);
                }
                stream.write(0x00); // 終端文字
            } catch (IOException e) {
                // ignore
            }
        }
        return createWith(SMART_PHONE_INTERRUPT_NOTIFICATION, stream.toByteArray());
    }

    /**
     * 車載機割り込み情報通知応答パケット生成.
     *
     * @return 送信パケット
     */
    @NonNull
    public OutgoingPacket createDeviceInterruptNotificationResponse() {
        return createWith(DEVICE_INTERRUPT_NOTIFICATION_RESPONSE);
    }

    /**
     * 送信パケットIDタイプで生成.
     * <p>
     * データ部がD0しかないもの特化。
     *
     * @param idType 送信パケットIDタイプ
     * @return 送信パケット
     * @throws NullPointerException {@code idType}がnull
     */
    @NonNull
    private OutgoingPacket createWith(@NonNull OutgoingPacketIdType idType) {
        return createWith(idType, new byte[1]);
    }

    /**
     * 送信パケットIDタイプとデータ部で生成.
     *
     * @param idType 送信パケットIDタイプ
     * @param data データ部。{@code data[0]}は、{@code idType.d0}で置き換えられる。（未設定で良い）
     * @return 送信パケット
     * @throws NullPointerException {@code idType}がnull
     */
    @NonNull
    private OutgoingPacket createWith(@NonNull OutgoingPacketIdType idType, @NonNull @Size(min = 1) byte[] data) {
        checkNotNull(idType);
        checkNotNull(data);
        checkArgument(data.length >= 1);

        data[0] = (byte) idType.d0;
        return new OutgoingPacket(idType, data);
    }

    private byte[] createSmartPhoneStatusData(@NonNull ProtocolVersion version, @NonNull SmartPhoneStatus status, boolean isAdasAlarm) {

        // D1:楽曲再生情報1
        //  bit[5]   シャッフル状態
        int d1 = (status.shuffleMode.code << 5 & 0x01 << 5);
        //  bit[3-4] リピート状態
        d1 |= (status.repeatMode.code << 3 & 0x03 << 3);
        //  bit[0-2] 再生状態
        d1 |= (status.playbackMode.code & 0x07);
        AppStatus appStatus = mStatusHolder.getAppStatus();
        if(appStatus.isLaunchedThirdPartyAudioApp){
            d1 = (ShuffleMode.OFF.code << 5 & 0x01 << 5)|(SmartPhoneRepeatMode.OFF.code << 3 & 0x03 << 3)|(PlaybackMode.PLAY.code & 0x07);
        }
        if(appStatus.appMusicAudioMode==AudioMode.ALEXA){
            int code = 1;
            if(isPlaying()){
                code = 2;
            }else{
                code = 1;
            }
            d1 = (ShuffleMode.OFF.code << 5 & 0x01 << 5)|(SmartPhoneRepeatMode.OFF.code << 3 & 0x03 << 3)|(code & 0x07);
        }
        if (version.isGreaterThanOrEqual(ProtocolVersion.V3)) {
            // D5:設定リスト表示状態
            //  bit[1]:サーチリスト
            int d5 = (status.showingSearchList ? 1 << 1 : 0);
            //  bit[0]:デバイスリスト
            d5 |= (status.showingDeviceList ? 1 : 0);

            if (version.isGreaterThanOrEqual(ProtocolVersion.V4)) {
                // D3:ADAS状態
                //  bit[0-1] 警告状態
                int d3 = isAdasAlarm ? status.getAdasWarningStatus().code : AdasWarningStatus.NONE.code;
                // D4:その他
                //  bit[0]:99App Service(BLE)
                int d4 = status.bleAppServicePublicationStatus.code;
                if(BuildConfig.DEBUG&&version.isGreaterThanOrEqual(ProtocolVersion.V4_1)){
                    //  bit[1] Smartphone内EQ使用状態
                    d4 |= (status.smartPhoneEqUseStatus ? 1 << 1 : 0);
                }
                return new byte[]{0x00, (byte) d1, 0x00, (byte) d3, (byte) d4, (byte) d5};
            } else {
                return new byte[]{0x00, (byte) d1, 0x00, 0x00, 0x00, (byte) d5};
            }
        } else {
            return new byte[] { 0x00, (byte) d1, 0x00, 0x00, 0x00 } ;
        }
    }
    /**
     * 音楽再生状態を取得するメソッド.
     *
     * @return
     */
    private boolean isPlaying() {
        boolean isPlaying = false;
        if (AlexaAudioManager.getInstance().getAlexaPlayer() != null) {
            isPlaying = AlexaAudioManager.getInstance().getAlexaPlayer().isPlaying();
        } else {
            isPlaying = false;
        }
        return isPlaying;
    }

    private static String left(String str, int len) {
        if (str == null) {
            return "";
        }

        if (str.length() <= len) {
            return str;
        }

        return str.substring(0, len);
    }

    private static void copyBdAddressToData(@NonNull byte[] data, int start, @NonNull String bdAddress) {
        checkNotNull(bdAddress);

        try {
            byte[] bdAddressBytes = bdAddress.getBytes("UTF-8");
            checkArgument(bdAddressBytes.length == 17);

            System.arraycopy(bdAddressBytes, 0, data, start, bdAddressBytes.length);
            data[start + bdAddressBytes.length] = 0x00; // NUL終端
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
