package jp.pioneer.carsync.infrastructure.crp;

import android.support.annotation.Nullable;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.pioneer.carsync.domain.model.ProtocolVersion;

import static jp.pioneer.carsync.domain.model.ProtocolVersion.*;
import static jp.pioneer.carsync.infrastructure.crp.IncomingPacketIdType.*;

/**
 * 送信パケットIDタイプ.
 * <p>
 * CarRemote Protocolのパケットは、
 * <ul>
 *     <li>ID（コマンドの大分類）</li>
 *     <li>Type（IDより下位のコマンド小分類）</li>
 *     <li>D0（通信の種別）</li>
 * </ul>
 * でコマンドを特定可能となっているため、個々のコマンドを列挙値に分類している。
 *
 * @see IncomingPacketIdType
 */
@SuppressFBWarnings("SE_BAD_FIELD")
public enum OutgoingPacketIdType {
    //////// ベース:認証 ////////
    /** 認証開始. */
    START_INITIAL_AUTH(0x00, 0x00, 0x00, START_INITIAL_AUTH_RESPONSE, null),
    /** 認証終了. */
    END_INITIAL_AUTH(0x00, 0x00, 0x01, END_INITIAL_AUTH_RESPONSE, null),
    /** Class ID要求. */
    CLASS_ID_REQUEST(0x00, 0x01, 0x80, CLASS_ID_REQUEST_RESPONSE, null),
    /** Protocol Version要求. */
    PROTOCOL_VERSION_REQUEST(0x00, 0x02, 0x80, PROTOCOL_VERSION_RESPONSE, null),
    /** Protocol Version通知. */
    PROTOCOL_VERSION_NOTIFICATION(0x00, 0x02, 0x00, PROTOCOL_VERSION_NOTIFICATION_RESPONSE, null),
    /** 初期通信開始. */
    START_INITIAL_COMM(0x00, 0x10, 0x00, START_INITIAL_COMM_RESPONSE, null),
    /** 初期通信終了. */
    END_INITIAL_COMM(0x00, 0x10, 0x01, END_INITIAL_COMM_RESPONSE, null),
    /** 車載機情報取得開始. */
    START_GET_DEVICE_SPEC(0x00, 0x11, 0x00, START_GET_DEVICE_SPEC_RESPONSE, null),
    /** 車載機情報取得終了. */
    END_GET_DEVICE_SPEC(0x00, 0x11, 0x01, END_GET_DEVICE_SPEC_RESPONSE, null),
    /** SmartPhone情報通知開始. */
    START_SEND_SMART_PHONE_SPEC(0x00, 0x12, 0x00, START_SEND_SMART_PHONE_SPEC_RESPONSE, null),
    /** SmartPhone情報通知終了. */
    END_SEND_SMART_PHONE_SPEC(0x00, 0x12, 0x01, END_SEND_SMART_PHONE_SPEC_RESPONSE, null),
    /** セッション開始. */
    START_SESSION(0x00, 0x20, 0x00, START_SESSION_RESPONSE, null),
    /** 認証エラー通知. */
    AUTH_ERROR(0x00, 0x30, 0x00, null),

    //////// ベース:車載機情報 ////////
    /** 車載機Spec要求. */
    DEVICE_SPEC_REQUEST(0x02, 0x00, 0x80, DEVICE_SPEC_RESPONSE, V1),
    /** 車載機型番要求. */
    DEVICE_MODEL_REQUEST(0x02, 0x01, 0x80, DEVICE_MODEL_RESPONSE, V2),
    /** 車載機BDアドレス要求. */
    DEVICE_BD_ADDRESS_REQUEST(0x02, 0x01, 0x81, DEVICE_BD_ADDRESS_RESPONSE, V3),
    /** 車載機ソフトウェアバージョン要求. */
    DEVICE_FARM_VERSION_REQUEST(0x02, 0x01, 0x83, DEVICE_FARM_VERSION_RESPONSE, V4_1),
    //////// ベース:SmartPhone情報 ////////
    /** SmartPhoneSpec通知. */
    SMART_PHONE_SPEC_NOTIFICATION(0x04, 0x00, 0x00, SMART_PHONE_SPEC_NOTIFICATION_RESPONSE, V1),
    /** 時刻情報通知. */
    TIME_NOTIFICATION(0x04, 0x01, 0x00, TIME_NOTIFICATION_RESPONSE, V2),

    //////// ステータス:車載機ステータス ////////
    /** 車載機ステータス情報要求. */
    DEVICE_STATUS_REQUEST(0x10, 0x00, 0x80, DEVICE_STATUS_RESPONSE, V1),
    /** Tuner系共通ステータス情報通知応答. */
    TUNER_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xC0, V2),
    /** Radioステータス情報通知応答. */
    RADIO_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xC1, V2),
    /** DABステータス情報通知応答. */
    DAB_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xC2, V2),
    /** SiriusXMステータス情報通知応答. */
    SXM_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xC3, V2),
    /** HD Radioステータス情報通知応答. */
    HD_RADIO_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xC4, V2),
    /** メディア系共通ステータス情報応答. */
    MEDIA_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xD0, V2),
    /** CDステータス情報通知応答. */
    CD_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xD1, V2),
    /** USBステータス情報通知応答. */
    USB_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xD2, V2),
    /** BT Audioステータス情報通知応答. */
    BT_AUDIO_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xD3, V2),
    /** Pandoraステータス情報通知応答. */
    PANDORA_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xD4, V2),
    /** Spotifyステータス情報通知応答. */
    SPOTIFY_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xD5, V2),
    /** iPodステータス情報通知応答. */
    IPOD_STATUS_NOTIFICATION_RESPONSE(0x10, 0x01, 0xD6, V2),
    /** システム設定ステータス情報要求. */
    SYSTEM_SETTING_STATUS_REQUEST(0x10, 0x02, 0x80, SYSTEM_SETTING_STATUS_RESPONSE, V4),
    /** システム設定ステータス情報通知応答. */
    SYSTEM_SETTING_STATUS_NOTIFICATION_RESPONSE(0x10, 0x02, 0xC0, V4),
    /** オーディオ設定ステータス情報要求. */
    AUDIO_SETTING_STATUS_REQUEST(0x10, 0x02, 0x81, AUDIO_SETTING_STATUS_RESPONSE, V2),
    /** オーディオ設定ステータス情報通知応答. */
    AUDIO_SETTING_STATUS_NOTIFICATION_RESPONSE(0x10, 0x02, 0xC1, V2),
    /** イルミ設定ステータス情報要求. */
    ILLUMINATION_SETTING_STATUS_REQUEST(0x10, 0x02, 0x82, ILLUMINATION_SETTING_STATUS_RESPONSE, V2),
    /** イルミ設定ステータス情報通知応答. */
    ILLUMINATION_SETTING_STATUS_NOTIFICATION_RESPONSE(0x10, 0x02, 0xC2, V2),
    /** Function設定ステータス情報要求. */
    FUNCTION_SETTING_STATUS_REQUEST(0x10, 0x02, 0x84, FUNCTION_SETTING_STATUS_RESPONSE, V2),
    /** Function設定ステータス情報通知応答. */
    FUNCTION_SETTING_STATUS_NOTIFICATION_RESPONSE(0x10, 0x02, 0xC4, V2),
    /** Phone設定ステータス情報要求. */
    PHONE_SETTING_STATUS_REQUEST(0x10, 0x02, 0x85, PHONE_SETTING_STATUS_RESPONSE, V3),
    /** Phone設定ステータス情報通知応答. */
    PHONE_SETTING_STATUS_NOTIFICATION_RESPONSE(0x10, 0x02, 0xC5, V3),
    /** パーキングセンサー設定ステータス情報要求. */
    PARKING_SENSOR_SETTING_STATUS_REQUEST(0x10, 0x02, 0x86, PARKING_SENSOR_SETTING_STATUS_RESPONSE, V4),
    /** パーキングセンサー設定ステータス情報通知応答. */
    PARKING_SENSOR_SETTING_STATUS_NOTIFICATION_RESPONSE(0x10, 0x02, 0xC6, V4),
    /** 初期設定ステータス情報要求. */
    INITIAL_SETTING_STATUS_REQUEST(0x10, 0x02, 0x87, INITIAL_SETTING_STATUS_RESPONSE, V4),
    /** 初期設定ステータス情報通知応答. */
    INITIAL_SETTING_STATUS_NOTIFICATION_RESPONSE(0x10, 0x02, 0xC7, V4),
    /** Sound FX設定ステータス情報要求. */
    SOUND_FX_SETTING_STATUS_REQUEST(0x10, 0x02, 0x88, SOUND_FX_SETTING_STATUS_RESPONSE, V4),
    /** Sound FX設定ステータス情報通知応答. */
    SOUND_FX_SETTING_STATUS_NOTIFICATION_RESPONSE(0x10, 0x02, 0xC8, V4),
    /** パーキングセンサーステータス情報応答. */
    PARKING_SENSOR_STATUS_NOTIFICATION_RESPONSE(0x10, 0x03, 0xC0, V4),

    //////// ステータス:SmartPhoneステータス ////////
    /** SmartPhoneステータス情報（初回）通知. */
    SMART_PHONE_STATUS_INITIAL_NOTIFICATION(0x12, 0x00, 0x00, SMART_PHONE_STATUS_NOTIFICATION_RESPONSE, V1),
    /** SmartPhoneステータス情報通知. */
    SMART_PHONE_STATUS_NOTIFICATION(0x12, 0x00, 0x00, V1),
    /** SmartPhoneステータス情報応答. */
    SMART_PHONE_STATUS_RESPONSE(0x12, 0x00, 0xE0, V1),

    //////// ステータス:エラー通知 ////////
    /** SmartPhoneエラー通知. */
    SMART_PHONE_ERROR_NOTIFICATION(0x1E, 0x00, 0x00, SMART_PHONE_ERROR_NOTIFICATION_RESPONSE, V1),
    /** 車載機エラー通知応答. */
    DEVICE_ERROR_NOTIFICATION_RESPONSE(0x1E, 0x01, 0xC0, V1),

    //////// コマンド:制御コマンド ////////
    /** 車載機操作コマンド通知. */
    DEVICE_CONTROL_COMMAND(0x20, 0x00, 0x00, V1),
    /** ソース切替通知. */
    SOURCE_SWITCH_COMMAND(0x20, 0x00, 0x01, V1),
    /** 車載機画面切替通知. */
    SCREEN_CHANGE_COMMAND(0x20, 0x00, 0x02, V3),
    /** 読み上げ通知. */
    READING_COMMAND(0x20, 0x00, 0x03, V4),
    /** 新着メッセージ通知. */
    NEW_MESSAGE_COMMAND(0x20, 0x00, 0x04, V4),
    /** CUSTOM発光通知. */
    CUSTOM_FLASH_COMMAND(0x20, 0x00, 0x05, V4),
    /** 衝突検知通知. */
    IMPACT_DETECTION_COMMAND(0x20, 0x00, 0x06, V4),
    /** 電話発信通知. */
    PHONE_CALL_COMMAND(0x20, 0x00, 0x07, V4),
    /** メニュー表示解除通知. */
    EXIT_MENU_COMMAND(0x20, 0x00, 0x08, V4),
    /** 音声認識通知. */
    VOICE_RECOGNITION_COMMAND(0x20, 0x00, 0x09, VOICE_RECOGNITION_NOTIFICATION_RESPONSE, V4),
    /** リバース極性切替通知. */
    REVERSE_POLARITY_CHANGE_COMMAND(0x20, 0x00, 0x0A, V4),
    /** 車載機音声認識実行通知. */
    DEVICE_VOICE_RECOGNITION_COMMAND(0x20, 0x00, 0x0A, V4_1),
    /** 車載機ボリューム指定通知. */
    DEVICE_VOLUME_CHANGE_COMMAND(0x20, 0x00, 0x0B, V4_1),
    /** Favorite情報取通知 : Radio. */
    FAVORITE_RADIO_SET_COMMAND(0x20, 0x00, 0x20, V1),
    /** Favorite情報取通知 : DAB. */
    FAVORITE_DAB_SET_COMMAND(0x20, 0x00, 0x21, V1),
    /** Favorite情報取通知 : SiriusXM. */
    FAVORITE_SIRIUS_XM_SET_COMMAND(0x20, 0x00, 0x22, V1),
    /** Favorite情報取通知 : HD Radio. */
    FAVORITE_HD_RADIO_SET_COMMAND(0x20, 0x00, 0x23, V1),
    /** 音声認識終了通知応答. */
    FINISH_VOICE_RECOGNITION_NOTIFICATION_RESPONSE(0x20, 0x00, 0xC3, V4),
    /** リスト遷移通知. */
    LIST_TRANSITION_NOTIFICATION(0x20, 0x03, 0x00, V2),
    /** リストアイテム選択通知 : DAB. */
    DAB_LIST_ITEM_SELECTED_NOTIFICATION(0x20, 0x03, 0x01, V2),
    /** プリセット登録通知：Tuner系共通. */
    TUNER_LIST_REGISTER_PRESET_NOTIFICATION(0x20, 0x03, 0x02, V4_1),
    /** フォーカス位置変更要求. */
    LIST_FOCUS_POSITION_CHANGE_REQUEST(0x20, 0x03, 0x80, LIST_FOCUS_POSITION_CHANGE_RESPONSE, V2),
    /** DAB ABCサーチ実行要求. */
    DAB_ABC_SEARCH_EXECUTE_REQUEST(0x20, 0x03, 0x81, DAB_ABC_SEARCH_EXECUTE_RESPONSE, V4),
    /** リスト情報更新通知応答. */
    LIST_UPDATED_NOTIFICATION_RESPONSE(0x20, 0x03, 0xC0, V2),
    /** 設定リスト情報更新通知応答. */
    SETTING_LIST_UPDATED_NOTIFICATION_RESPONSE(0x20, 0x03, 0xC1, V3),
    /** 連携切断通知応答. */
    DISCONNECT_NOTIFICATION_RESPONSE(0x20, 0xF0, 0xC0, V2),

    //////// コマンド:設定コマンド ////////
    /** BEEP TONE設定通知. */
    BEEP_TONE_SETTING_NOTIFICATION(0x22, 0x00, 0x00, V4),
    /** ATT/MUTE設定通知. */
    ATT_MUTE_SETTING_NOTIFICATION(0x22, 0x00, 0x01, V4),
    /** DEMO設定通知. */
    DEMO_SETTING_NOTIFICATION(0x22, 0x00, 0x02, V4),
    /** POWER SAVE設定通知. */
    POWER_SAVE_SETTING_NOTIFICATION(0x22, 0x00, 0x03, V4),
    /** BT Audio設定通知. */
    BT_AUDIO_SETTING_NOTIFICATION(0x22, 0x00, 0x04, V4),
    /** Pandora設定通知. */
    PANDORA_SETTING_NOTIFICATION(0x22, 0x00, 0x05, V4),
    /** Spotify設定通知. */
    SPOTIFY_SETTING_NOTIFICATION(0x22, 0x00, 0x06, V4),
    /** AUX設定通知. */
    AUX_SETTING_NOTIFICATION(0x22, 0x00, 0x07, V4),
    /** 99App自動起動設定通知. */
    APP_AUTO_START_SETTING_NOTIFICATION(0x22, 0x00, 0x08, V4),
    /** USB AUTO設定通知. */
    USB_AUTO_SETTING_NOTIFICATION(0x22, 0x00, 0x09, V4),
    /** ステアリングリモコン設定通知. */
    STEERING_REMOTE_CONTROL_SETTING_NOTIFICATION(0x22, 0x00, 0x0A, V4),
    /** AUTO PI設定通知. */
    AUTO_PI_SETTING_NOTIFICATION(0x22, 0x00, 0x0B, V4),
    /** DISP OFF設定通知. */
    DISPLAY_OFF_SETTING_NOTIFICATION(0x22, 0x00, 0x0C, V4),
    /** 距離単位設定通知. */
    DISTANCE_UNIT_SETTING_NOTIFICATION(0x22, 0x00, 0x0D, V4),
    /** EQ設定通知. */
    EQUALIZER_SETTING_NOTIFICATION(0x22, 0x01, 0x00, V4),
    /** EQカスタム調整通知. */
    EQUALIZER_CUSTOM_ADJUST_NOTIFICATION(0x22, 0x01, 0x01, V2),
    /** FADER/BALANCE設定通知. */
    FADER_BALANCE_SETTING_NOTIFICATION(0x22, 0x01, 0x02, V2),
    /** SUBWOOFER設定通知. */
    SUBWOOFER_SETTING_NOTIFICATION(0x22, 0x01, 0x03, V2),
    /** SUBWOOFER位相設定通知. */
    SUBWOOFER_PHASE_SETTING_NOTIFICATION(0x22, 0x01, 0x04, V2),
    /** SPEAKER LEVEL設定通知. */
    SPEAKER_LEVEL_SETTING_NOTIFICATION(0x22, 0x01, 0x05, V2),
    /** CROSSOVER HFP/LPF設定通知. */
    CROSSOVER_HPF_LPF_SETTING_NOTIFICATION(0x22, 0x01, 0x06, V2),
    /** CROSSOVER カットオフ周波数設定通知. */
    CROSSOVER_CUTOFF_SETTING_NOTIFICATION(0x22, 0x01, 0x07, V2),
    /** CROSSOVER スロープ設定通知. */
    CROSSOVER_SLOPE_SETTING_NOTIFICATION(0x22, 0x01, 0x08, V2),
    /** LISTENING POSITION設定通知. */
    LISTENING_POSITION_SETTING_NOTIFICATION(0x22, 0x01, 0x09, V2),
    /** TIME ALIGNMENT プリセット設定通知. */
    TIME_ALIGNMENT_PRESET_SETTING_NOTIFICATION(0x22, 0x01, 0x0A, V2),
    /** TIME ALIGNMENT カスタム調整設定通知. */
    TIME_ALIGNMENT_SETTING_NOTIFICATION(0x22, 0x01, 0x0B, V2),
    /** AUTO EQ補正設定通知. */
    AUTO_EQ_CORRECTION_SETTING_NOTIFICATION(0x22, 0x01, 0x0C, V2),
    /** SAVE SETTING実行通知. */
    SAVE_SETTING_NOTIFICATION(0x22, 0x01, 0x0D, SAVE_SETTING_NOTIFICATION_RESPONSE, V2),
    /** LOAD SETTING実行通知. */
    LOAD_SETTING_NOTIFICATION(0x22, 0x01, 0x0E, LOAD_SETTING_NOTIFICATION_RESPONSE, V2),
    /** BASS BOOSTERレベル設定通知. */
    BASS_BOOSTER_LEVEL_SETTING_NOTIFICATION(0x22, 0x01, 0x0F, V2),
    /** LOUDNESS設定通知. */
    LOUDNESS_SETTING_NOTIFICATION(0x22, 0x01, 0x10, V2),
    /** ALC設定通知. */
    ALC_SETTING_NOTIFICATION(0x22, 0x01, 0x11, V2),
    /** SLA設定通知. */
    SLA_SETTING_NOTIFICATION(0x22, 0x01, 0x12, V2),
    /** [JASPER] EQ設定通知. */
    JASPER_EQUALIZER_SETTING_NOTIFICATION(0x22, 0x01, 0x14, V2),
    /** [JASPER] EQカスタム調整通知. */
    JASPER_EQUALIZER_CUSTOM_ADJUST_NOTIFICATION(0x22, 0x01, 0x15, V2),
    /** [JASPER] CROSSOVER HPF/LPF設定通知. */
    JASPER_CROSSOVER_HPF_LPF_SETTING_NOTIFICATION(0x22, 0x01, 0x16, V2),
    /** [JASPER] CROSSOVER カットオフ周波数設定通知. */
    JASPER_CROSSOVER_CUTOFF_SETTING_NOTIFICATION(0x22, 0x01, 0x17, V2),
    /** [JASPER] CROSSOVER スロープ設定通知. */
    JASPER_CROSSOVER_SLOPE_SETTING_NOTIFICATION(0x22, 0x01, 0x18, V2),
    /** [AC2] EQカスタム調整通知(車種専用セッティング：オーディオが有効時). */
    AC2_EQUALIZER_CUSTOM_ADJUST_NOTIFICATION(0x22, 0x01, 0x19, V3),
    /** [AC2] Beat Blaster設定通知. */
    BEAT_BLASTER_SETTING_NOTIFICATION(0x22, 0x01, 0x20, V3),
    /** [AC2] LEVEL設定通知. */
    LEVEL_SETTING_NOTIFICATION(0x22, 0x01, 0x21, V3),
    /** SPECIAL EQ設定通知. */
    SPECIAL_EQUALIZER_SETTING_NOTIFICATION(0x22, 0x01, 0x22, V4),
    /** EQ PRESET初期化通知. */
    EQUALIZER_PRESET_INITIALIZATION_NOTIFICATION(0x22, 0x01, 0x23, V4),
    /** SOUND RETRIEVER設定通知. */
    SOUND_RETRIEVER_SETTING_NOTIFICATION(0x22, 0x01, 0x24, V4),
    /** COLOR設定通知. */
    COLOR_SETTING_NOTIFICATION(0x22, 0x02, 0x00, V2),
    /** CUSTOM COLOR設定通知. */
    CUSTOM_COLOR_SETTING_NOTIFICATION(0x22, 0x02, 0x01, V2),
    /** DIMMER設定通知. */
    DIMMER_SETTING_NOTIFICATION(0x22, 0x02, 0x02, V2),
    /** DIMMER時刻設定通知. */
    DIMMER_TIME_SETTING_NOTIFICATION(0x22, 0x02, 0x03, V2),
    /** BRIGHTNESS設定通知. */
    BRIGHTNESS_SETTING_NOTIFICATION(0x22, 0x02, 0x04, V2),
    /** BT PHONE COLOR設定通知. */
    BT_PHONE_COLOR_SETTING_NOTIFICATION(0x22, 0x02, 0x05, V2),
    /** 蛍の光風設定通知. */
    ILLUMINATION_SETTING_NOTIFICATION(0x22, 0x02, 0x06, V2),
    /** BRIGHTNESS設定通知（個別設定モデル）. */
    KEY_DISPLAY_BRIGHTNESS_SETTING_NOTIFICATION(0x22, 0x02, 0x07, V3),
    /** CUSTOM発光パターン設定通知. */
    CUSTOM_FLASH_PATTERN_SETTING_NOTIFICATION(0x22, 0x02, 0x08, CUSTOM_FLASH_PATTERN_SETTING_NOTIFICATION_RESPONSE, V4),
    /** オーディオレベルメータ連動設定通知. */
    AUDIO_LEVEL_METER_LINKED_SETTING_NOTIFICATION(0x22, 0x02, 0x09, V4),
    /** [SPH] BT PHONE COLOR設定通知. */
    SPH_BT_PHONE_COLOR_SETTING_NOTIFICATION(0x22, 0x02, 0x0A, V4),
    /** COLOR設定通知(共通設定モデル). */
    COMMON_COLOR_SETTING_NOTIFICATION(0x22, 0x02, 0x0B, V4),
    /** CUSTOM COLOR設定通知(共通設定モデル). */
    COMMON_CUSTOM_COLOR_SETTING_NOTIFICATION(0x22, 0x02, 0x0C, V4),
    /** メッセージ受信COLOR設定通知. */
    INCOMING_MESSAGE_COLOR_SETTING_NOTIFICATION(0x22, 0x02, 0x0D, V4),
    /** Function設定通知. */
    FUNCTION_SETTING_NOTIFICATION(0x22, 0x04, 0x00, V2),
    /** Audio Device 切替コマンド通知.  */
    AUDIO_DEVICE_SWITCH_COMMAND(0x22, 0x04, 0x01, AUDIO_DEVICE_SWITCH_COMMAND_RESPONSE, V3),
    /** Audio Device 切替完了通知応答. */
    AUDIO_DEVICE_SWITCH_COMPLETE_NOTIFICATION_RESPONSE(0x22, 0x04, 0xC1, V3),
    /** サーチコマンド通知. */
    PHONE_SEARCH_COMMAND(0x22, 0x05, 0x00, PHONE_SEARCH_COMMAND_RESPONSE, V3),
    /** ペアリング追加コマンド通知. */
    PAIRING_ADD_COMMAND(0x22, 0x05, 0x01, PAIRING_ADD_COMMAND_RESPONSE, V3),
    /** ペアリング削除コマンド通知. */
    PAIRING_DELETE_COMMAND(0x22, 0x05, 0x02, PAIRING_DELETE_COMMAND_RESPONSE, V3),
    /** サービスコネクトコマンド通知. */
    PHONE_SERVICE_CONNECT_COMMAND(0x22, 0x05, 0x03, PHONE_SERVICE_CONNECT_COMMAND_RESPONSE, V3),
    /** AUTO ANSWER設定通知. */
    AUTO_ANSWER_SETTING_NOTIFICATION(0x22, 0x05, 0x04, V4),
    /** AUTO PAIRING設定通知. */
    AUTO_PAIRING_SETTING_NOTIFICATION(0x22, 0x05, 0x05, V4),
    /** サーチコマンド完了通知応答. */
    PHONE_SEARCH_COMPLETE_NOTIFICATION_RESPONSE(0x22, 0x05, 0xC0, V3),
    /** ペアリング追加コマンド完了通知応答. */
    PAIRING_ADD_COMPLETE_NOTIFICATION_RESPONSE(0x22, 0x05, 0xC1, V3),
    /** ペアリング削除コマンド完了通知応答. */
    PAIRING_DELETE_COMPLETE_NOTIFICATION_RESPONSE(0x22, 0x05, 0xC2, V3),
    /** サービスコネクトコマンド完了通知応答. */
    PHONE_SERVICE_CONNECT_COMPLETE_NOTIFICATION_RESPONSE(0x22, 0x05, 0xC3, V3),
    /** パーキングセンサー設定通知. */
    PARKING_SENSOR_SETTING_NOTIFICATION(0x22, 0x06, 0x00, V4),
    /** 警告音出力先設定通知. */
    ALARM_OUTPUT_DESTINATION_SETTING_NOTIFICATION(0x22, 0x06, 0x01, V4),
    /** 警告音量設定通知. */
    ALARM_VOLUME_SETTING_NOTIFICATION(0x22, 0x06, 0x02, V4),
    /** バック信号極性設定通知. */
    BACK_POLARITY_SETTING_NOTIFICATION(0x22, 0x06, 0x03, V4),
    /** ナビガイド音声設定通知. */
    NAVI_GUIDE_VOICE_SETTING_NOTIFICATION(0x22, 0x07, 0x00, V4),
    /** ナビガイド音声ボリューム設定通知. */
    NAVI_GUIDE_VOICE_VOLUME_SETTING_NOTIFICATION(0x22, 0x07, 0x01, V4),
    /** FM STEP設定通知. */
    FM_STEP_SETTING_NOTIFICATION(0x22, 0x08, 0x00, V4),
    /** AM STEP設定通知. */
    AM_STEP_SETTING_NOTIFICATION(0x22, 0x08, 0x01, V4),
    /** REAR出力設定/PREOUT出力設定通知. */
    REAR_OUTPUT_PREOUT_OUTPUT_SETTING_NOTIFICATION(0x22, 0x08, 0x02, V4),
    /** REAR出力設定通知. */
    REAR_OUTPUT_SETTING_NOTIFICATION(0x22, 0x08, 0x03, V4),
    /** MENU表示言語設定通知. */
    MENU_DISPLAY_LANGUAGE_SETTING_NOTIFICATION(0x22, 0x08, 0x04, V4),
    /** DAB ANT PW設定通知. */
    DAB_ANTENNA_POWER_SETTING_NOTIFICATION(0x22, 0x08, 0x05, V4),
    /** Super轟設定通知. */
    SUPER_TODOROKI_SETTING_NOTIFICATION(0x22, 0x09, 0x00, V4),
    /** Small Car TA設定通知. */
    SMALL_CAR_TA_SETTING_NOTIFICATION(0x22, 0x09, 0x01, V4),
    /** ライブシミュレーション設定通知. */
    LIVE_SIMULATION_SETTING_NOTIFICATION(0x22, 0x09, 0x02, V4),
    /** カラオケ設定通知. */
    KARAOKE_SETTING_NOTIFICATION(0x22, 0x09, 0x03, V4),
    /** マイク音量設定通知. */
    MIC_VOLUME_SETTING_NOTIFICATION(0x22, 0x09, 0x04, V4),
    /** Vocal Cancel設定通知. */
    VOCAL_CANCEL_SETTING_NOTIFICATION(0x22, 0x09, 0x05, V4),

    //////// 表示データ:車載機情報 ////////
    /** リスト初期情報要求. */
    INITIAL_LIST_INFO_REQUEST(0x30, 0x01, 0x80, INITIAL_LIST_INFO_RESPONSE, V2),
    /** リスト情報要求. */
    LIST_INFO_REQUEST(0x30, 0x01, 0x81, LIST_INFO_RESPONSE, V2),
    /** 設定リスト初期情報要求.  */
    INITIAL_SETTING_LIST_INFO_REQUEST(0x30, 0x01, 0x82, INITIAL_SETTING_LIST_INFO_RESPONSE, V3),
    /** 設定リスト情報要求. */
    SETTING_LIST_INFO_REQUEST(0x30, 0x01, 0x83, SETTING_LIST_INFO_RESPONSE, V3),
    /** ペアリングデバイスリスト情報要求.  */
    PAIRING_DEVICE_LIST_INFO_REQUEST(0x30, 0x80, 0x80, PAIRING_DEVICE_LIST_INFO_RESPONSE, V4),
    /** ペアリングデバイス情報要求. */
    PAIRING_DEVICE_INFO_REQUEST(0x30, 0x80, 0x81, PAIRING_DEVICE_INFO_RESPONSE, V4),

    //////// 表示データ:SmartPhone情報 ////////
    /** オーディオ情報通知. */
    SMART_PHONE_AUDIO_INFO_NOTIFICATION(0x32, 0x00, 0x00, V1),
    /** 楽曲再生時間通知. */
    SMART_PHONE_AUDIO_PLAYBACK_TIME_NOTIFICATION(0x32, 0x00, 0x10, V1),
    /** 選択リスト表示情報通知 */
    SELECTED_LIST_DISPLAY_INFO_NOTIFICATION(0x32, 0x01, 0x00, V2),

    //////// 表示データ:設定 ////////
    /** BEEP TONE設定情報要求. */
    BEEP_TONE_SETTING_INFO_REQUEST(0x34, 0x00, 0x80, BEEP_TONE_SETTING_INFO_RESPONSE, V4),
    /** ATT/MUTE設定情報要求. */
    ATT_MUTE_SETTING_INFO_REQUEST(0x34, 0x00, 0x81, ATT_MUTE_SETTING_INFO_RESPONSE, V4),
    /** DEMO設定情報要求. */
    DEMO_SETTING_INFO_REQUEST(0x34, 0x00, 0x82, DEMO_SETTING_INFO_RESPONSE, V4),
    /** POWER SAVE設定情報要求. */
    POWER_SAVE_SETTING_INFO_REQUEST(0x34, 0x00, 0x83, POWER_SAVE_SETTING_INFO_RESPONSE, V4),
    /** BT Audio設定情報要求. */
    BT_AUDIO_SETTING_INFO_REQUEST(0x34, 0x00, 0x84, BT_AUDIO_SETTING_INFO_RESPONSE, V4),
    /** Pandora設定情報要求. */
    PANDORA_SETTING_INFO_REQUEST(0x34, 0x00, 0x85, PANDORA_SETTING_INFO_RESPONSE, V4),
    /** Spotify設定情報要求. */
    SPOTIFY_SETTING_INFO_REQUEST(0x34, 0x00, 0x86, SPOTIFY_SETTING_INFO_RESPONSE, V4),
    /** AUX設定情報要求. */
    AUX_SETTING_INFO_REQUEST(0x34, 0x00, 0x87, AUX_SETTING_INFO_RESPONSE, V4),
    /** 99APP自動起動設定情報要求. */
    APP_AUTO_START_SETTING_INFO_REQUEST(0x34, 0x00, 0x88, APP_AUTO_START_SETTING_INFO_RESPONSE, V4),
    /** USB AUTO設定情報要求. */
    USB_AUTO_SETTING_INFO_REQUEST(0x34, 0x00, 0x89, USB_AUTO_SETTING_INFO_RESPONSE, V4),
    /** ステアリングリモコン設定情報要求. */
    STEERING_REMOTE_CONTROL_SETTING_INFO_REQUEST(0x34, 0x00, 0x8A, STEERING_REMOTE_CONTROL_SETTING_INFO_RESPONSE, V4),
    /** AUTO PI設定情報要求. */
    AUTO_PI_SETTING_INFO_REQUEST(0x34, 0x00, 0x8B, AUTO_PI_SETTING_INFO_RESPONSE, V4),
    /** DISP PFF設定情報要求. */
    DISPLAY_OFF_SETTING_INFO_REQUEST(0x34, 0x00, 0x8C, DISPLAY_OFF_SETTING_INFO_RESPONSE, V4),
    /** 距離単位設定情報要求. */
    DISTANCE_UNIT_SETTING_INFO_REQUEST(0x34, 0x00, 0x8D, DISTANCE_UNIT_SETTING_INFO_RESPONSE, V4),
    /** [OPAL] EQ設定情報要求. */
    EQUALIZER_SETTING_INFO_REQUEST(0x34, 0x01, 0x80, EQUALIZER_SETTING_INFO_RESPONSE, V2),
    /** FADER/BALANCE設定情報要求. */
    FADER_BALANCE_SETTING_INFO_REQUEST(0x34, 0x01, 0x81, FADER_BALANCE_SETTING_INFO_RESPONSE, V2),
    /** SUBWOOFER設定情報要求. */
    SUBWOOFER_SETTING_INFO_REQUEST(0x34, 0x01, 0x82, SUBWOOFER_SETTING_INFO_RESPONSE, V2),
    /** SUBWOOFER位相設定情報要求. */
    SUBWOOFER_PHASE_SETTING_INFO_REQUEST(0x34, 0x01, 0x83, SUBWOOFER_PHASE_SETTING_INFO_RESPONSE, V2),
    /** SPEAKER LEVEL情報要求. */
    SPEAKER_LEVEL_SETTING_INFO_REQUEST(0x34, 0x01, 0x84, SPEAKER_LEVEL_SETTING_INFO_RESPONSE, V2),
    /** CROSSOVER設定情報要求. */
    CROSSOVER_SETTING_INFO_REQUEST(0x34, 0x01, 0x85, CROSSOVER_SETTING_INFO_RESPONSE, V2),
    /** LISTENING POSITION設定情報要求. */
    LISTENING_POSITION_SETTING_INFO_REQUEST(0x34, 0x01, 0x86, LISTENING_POSITION_SETTING_INFO_RESPONSE, V2),
    /** TIME ALIGNMENT設定情報要求. */
    TIME_ALIGNMENT_SETTING_INFO_REQUEST(0x34, 0x01, 0x87, TIME_ALIGNMENT_SETTING_INFO_RESPONSE, V2),
    /** Auto EQ補正設定情報要求 */
    AUTO_EQ_CORRECTION_SETTING_INFO_REQUEST(0x34, 0x01, 0x88, AUTO_EQ_CORRECTION_SETTING_INFO_RESPONSE, V2),
    /** BASS BOOSTERレベル設定情報要求. */
    BASS_BOOSTER_LEVEL_SETTING_INFO_REQUEST(0x34, 0x01, 0x89, BASS_BOOSTER_LEVEL_SETTING_INFO_RESPONSE, V2),
    /** LOUDNESS設定情報要求. */
    LOUDNESS_SETTING_INFO_REQUEST(0x34, 0x01, 0x8A, LOUDNESS_SETTING_INFO_RESPONSE, V2),
    /** ALC設定情報要求. */
    ALC_SETTING_INFO_REQUEST(0x34, 0x01, 0x8B, ALC_SETTING_INFO_RESPONSE, V2),
    /** SLA設定情報要求. */
    SLA_SETTING_INFO_REQUEST(0x34, 0x01, 0x8C, SLA_SETTING_INFO_RESPONSE, V2),
    /** [JASPER] EQ設定情報要求 */
    JASPER_EQUALIZER_SETTING_INFO_REQUEST(0x34, 0x01, 0x8D, JASPER_EQUALIZER_SETTING_INFO_RESPONSE, V2),
    /** [JASPER] CROSSOVER HPF/LPF設定情報要求. */
    JASPER_CROSSOVER_SETTING_INFO_REQUEST(0x34, 0x01, 0x8E, JASPER_CROSSOVER_SETTING_INFO_RESPONSE, V2),
    /** [AC2] EQ設定情報要求 (車種専用チューニング：オーディオが有効時). */
    AC2_EQUALIZER_SETTING_INFO_REQUEST(0x34, 0x01, 0x8F, AC2_EQUALIZER_SETTING_INFO_RESPONSE, V3),
    /** [AC2] Beat Blasterレベル設定情報要求. */
    BEAT_BLASTER_LEVEL_SETTING_INFO_REQUEST(0x34, 0x01, 0x90, BEAT_BLASTER_LEVEL_SETTING_INFO_RESPONSE, V3),
    /** [AC2] LEVEL設定情報要求. */
    LEVEL_SETTING_INFO_REQUEST(0x34, 0x01, 0x91, LEVEL_SETTING_INFO_RESPONSE, V3),
    /** EQカスタム設定情報応答. */
    CUSTOM_EQUALIZER_SETTING_INFO_NOTIFICATION_RESPONSE(0x34, 0x01, 0xC0, V4),
    /** SOUND RETRIEVER設定情報要求. */
    SOUND_RETRIEVER_SETTING_INFO_REQUEST(0x34, 0x01, 0x92, SOUND_RETRIEVER_SETTING_INFO_RESPONSE, V4),
    /** KEY COLOR設定情報要求 */
    KEY_COLOR_SETTING_INFO_REQUEST(0x34, 0x02, 0x80, KEY_COLOR_SETTING_INFO_RESPONSE, V2),
    /** DISP COLOR設定情報要求. */
    DISP_COLOR_SETTING_INFO_REQUEST(0x34, 0x02, 0x81, DISP_COLOR_SETTING_INFO_RESPONSE, V2),
    /** KEY COLOR一括情報要求 */
    KEY_COLOR_BULK_SETTING_INFO_REQUEST(0x34, 0x02, 0x82, KEY_COLOR_BULK_SETTING_INFO_RESPONSE, V2),
    /** DISP COLOR一括情報要求. */
    DISP_COLOR_BULK_SETTING_INFO_REQUEST(0x34, 0x02, 0x83, DISP_COLOR_BULK_SETTING_INFO_RESPONSE, V2),
    /** DIMMER設定情報要求. */
    DIMMER_SETTING_INFO_REQUEST(0x34, 0x02, 0x84, DIMMER_SETTING_INFO_RESPONSE, V2),
    /** BRIGHTNESS設定情報要求（共通設定モデル用）. */
    BRIGHTNESS_SETTING_INFO_REQUEST(0x34, 0x02, 0x85, BRIGHTNESS_SETTING_INFO_RESPONSE, V2),
    /** BT PHONE COLOR情報要求. */
    BT_PHONE_COLOR_SETTING_INFO_REQUEST(0x34, 0x02, 0x86, BT_PHONE_COLOR_SETTING_INFO_RESPONSE, V2),
    /** 蛍の光風設定情報要求. */
    ILLUMINATION_SETTING_INFO_REQUEST(0x34, 0x02, 0x87, ILLUMINATION_SETTING_INFO_RESPONSE, V2),
    /** KEY BRIGHTNESS設定情報要求（個別設定モデル用）. */
    KEY_BRIGHTNESS_SETTING_INFO_REQUEST(0x34, 0x02, 0x88, KEY_BRIGHTNESS_SETTING_INFO_RESPONSE, V3),
    /** DISP BRIGHTNESS設定情報要求（個別設定モデル用）. */
    DISPLAY_BRIGHTNESS_SETTING_INFO_REQUEST(0x34, 0x02, 0x89, DISPLAY_BRIGHTNESS_SETTING_INFO_RESPONSE, V3),
    /** オーディオレベルメータ連動設定情報要求. */
    AUDIO_LEVEL_METER_LINKED_SETTING_INFO_REQUEST(0x34, 0x02, 0x8A, AUDIO_LEVEL_METER_LINKED_SETTING_INFO_RESPONSE, V4),
    /** [SPH] BT PHONE COLOR設定情報要求. */
    SPH_BT_PHONE_COLOR_SETTING_INFO_REQUEST(0x34, 0x02, 0x8B, SPH_BT_PHONE_COLOR_SETTING_INFO_RESPONSE, V4),
    /** COLOR設定情報要求  (共通設定モデル用). */
    COMMON_COLOR_SETTING_INFO_REQUEST(0x34, 0x02, 0x8C, COMMON_COLOR_SETTING_INFO_RESPONSE, V4),
    /** COLOR一括情報要求  (共通設定モデル用). */
    COMMON_COLOR_BULK_SETTING_INFO_REQUEST(0x34, 0x02, 0x8D, COMMON_COLOR_BULK_SETTING_INFO_RESPONSE, V4),
    /** メッセージ受信通知COLOR設定情報要求. */
    INCOMING_MESSAGE_SETTING_INFO_REQUEST(0x34, 0x02, 0x8E, INCOMING_MESSAGE_COLOR_SETTING_INFO_RESPONSE, V4),
    /** Function設定情報要求. */
    FUNCTION_SETTING_INFO_REQUEST(0x34, 0x04, 0x80, FUNCTION_SETTING_INFO_RESPONSE, V2),
    /** パーキングセンサー設定要求. */
    PARKING_SENSOR_SETTING_INFO_REQUEST(0x34, 0x05, 0x80, PARKING_SENSOR_SETTING_INFO_RESPONSE, V4),
    /** 警告音出力先設定要求. */
    ALARM_OUTPUT_DESTINATION_SETTING_INFO_REQUEST(0x34, 0x05, 0x81, ALARM_OUTPUT_DESTINATION_SETTING_INFO_RESPONSE, V4),
    /** 警告音量設定要求. */
    ALARM_VOLUME_SETTING_INFO_REQUEST(0x34, 0x05, 0x82, ALARM_VOLUME_SETTING_INFO_RESPONSE, V4),
    /** バック信号極性設定要求. */
    BACK_POLARITY_SETTING_INFO_REQUEST(0x34, 0x05, 0x83, BACK_POLARITY_SETTING_INFO_RESPONSE, V4),
    /** ナビガイド音声設定要求. */
    NAVI_GUIDE_VOICE_SETTING_INFO_REQUEST(0x34, 0x06, 0x80, NAVI_GUIDE_VOICE_SETTING_INFO_RESPONSE, V4),
    /** ナビガイド音声ボリューム設定要求. */
    NAVI_GUIDE_VOICE_VOLUME_SETTING_INFO_REQUEST(0x34, 0x06, 0x81, NAVI_GUIDE_VOICE_VOLUME_SETTING_INFO_RESPONSE, V4),
    /** FM STEP設定要求. */
    FM_STEP_SETTING_INFO_REQUEST(0x34, 0x07, 0x80, FM_STEP_SETTING_INFO_RESPONSE, V4),
    /** AM STEP設定要求. */
    AM_STEP_SETTING_INFO_REQUEST(0x34, 0x07, 0x81, AM_STEP_SETTING_INFO_RESPONSE, V4),
    /** REAR出力設定/PREOUT出力設定要求. */
    REAR_OUTPUT_PREOUT_OUTPUT_SETTING_INFO_REQUEST(0x34, 0x07, 0x82, REAR_OUTPUT_PREOUT_OUTPUT_SETTING_INFO_RESPONSE, V4),
    /** REAR出力設定要求. */
    REAR_OUTPUT_SETTING_INFO_REQUEST(0x34, 0x07, 0x83, REAR_OUTPUT_SETTING_INFO_RESPONSE, V4),
    /** MENU表示言語設定要求. */
    MENU_DISPLAY_LANGUAGE_SETTING_INFO_REQUEST(0x34, 0x07, 0x84, MENU_DISPLAY_LANGUAGE_SETTING_INFO_RESPONSE, V4),
    /** DAB ANT PW設定要求. */
    DAB_ANTENNA_POWER_SETTING_INFO_REQUEST(0x34, 0x07, 0x85, DAB_ANTENNA_POWER_SETTING_INFO_RESPONSE, V4),
    /** AUTO ANSWER設定要求. */
    AUTO_ANSWER_SETTING_INFO_REQUEST(0x34, 0x08, 0x80, AUTO_ANSWER_SETTING_INFO_RESPONSE, V4),
    /** AUTO PAIRING設定要求. */
    AUTO_PAIRING_SETTING_INFO_REQUEST(0x34, 0x08, 0x81, AUTO_PAIRING_SETTING_INFO_RESPONSE, V4),
    /** Super轟設定要求. */
    SUPER_TODOROKI_SETTING_INFO_REQUEST(0x34, 0x09, 0x80, SUPER_TODOROKI_SETTING_INFO_RESPONSE, V4),
    /** Small Car TA設定要求. */
    SMALL_CAR_TA_SETTING_INFO_REQUEST(0x34, 0x09, 0x81, SMALL_CAR_TA_SETTING_INFO_RESPONSE, V4),
    /** ライブシミュレーション設定要求. */
    LIVE_SIMULATION_SETTING_INFO_REQUEST(0x34, 0x09, 0x82, LIVE_SIMULATION_SETTING_INFO_RESPONSE, V4),
    /** カラオケ設定要求. */
    KARAOKE_SETTING_INFO_REQUEST(0x34, 0x09, 0x83, KARAOKE_SETTING_INFO_RESPONSE, V4),
    /** マイク音量設定要求. */
    MIC_VOLUME_SETTING_INFO_REQUEST(0x34, 0x09, 0x84, MIC_VOLUME_SETTING_INFO_RESPONSE, V4),
    /** Vocal Cancel設定要求. */
    VOCAL_CANCEL_SETTING_INFO_REQUEST(0x34, 0x09, 0x85, VOCAL_CANCEL_SETTING_INFO_RESPONSE, V4),

    //////// 表示データ:割り込み情報 ////////
    /** SmartPhone割り込み情報通知. */
    SMART_PHONE_INTERRUPT_NOTIFICATION(0x36, 0x00, 0x00, SMART_PHONE_INTERRUPT_RESPONSE, V4),
    /** 車載機割り込み情報通知応答. */
    DEVICE_INTERRUPT_NOTIFICATION_RESPONSE(0x36, 0x01, 0xC0, V1),
    ;

    /** ID（コマンドの大分類）. */
    public final int id;
    /** Type（IDより下位のコマンド小分類）. */
    public final int type;
    /** D0（通信の種別）. */
    public final int d0;
    /** 応答パケットIDタイプ（通知の場合はnull）. */
    public final IncomingPacketIdType responsePacketIdType;
    /** サポートする最小のプロトコルバージョン. */
    public final ProtocolVersion supportVersion;

    /**
     * コンストラクタ.
     * <p>
     * 通知用に特化。
     *
     * @param id ID（コマンドの大分類）
     * @param type Type（IDより下位のコマンド小分類）
     * @param d0 D0（通信の種別）
     * @param supportVersion サポートする最小のプロトコルバージョン。全てサポートする場合はnull。
     */
    OutgoingPacketIdType(int id, int type, int d0, @Nullable ProtocolVersion supportVersion) {
        this(id, type, d0, null, supportVersion);
    }

    /**
     * コンストラクタ.
     *
     * @param id ID（コマンドの大分類）
     * @param type Type（IDより下位のコマンド小分類）
     * @param d0 D0（通信の種別）
     * @param responsePacketIdType 受信するレスポンスのIDタイプ。レスポンスを受けない場合はnull。
     * @param supportVersion サポートする最小のプロトコルバージョン。全てサポートする場合はnull。
     */
    OutgoingPacketIdType(int id, int type, int d0, @Nullable IncomingPacketIdType responsePacketIdType,
                         @Nullable ProtocolVersion supportVersion) {
        this.id = id;
        this.type = type;
        this.d0 = d0;
        this.responsePacketIdType = responsePacketIdType;
        this.supportVersion = supportVersion;
    }
}
