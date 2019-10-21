package jp.pioneer.carsync.application.content;

import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.annimon.stream.Stream;
import com.google.common.base.Objects;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Set;
import java.util.WeakHashMap;

import jp.pioneer.carsync.BuildConfig;
import jp.pioneer.carsync.domain.content.ContactsContract;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.model.AdasCameraSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionSetting;
import jp.pioneer.carsync.domain.model.AdasFunctionType;
import jp.pioneer.carsync.domain.model.AlexaLanguageType;
import jp.pioneer.carsync.domain.model.CarDeviceClassId;
import jp.pioneer.carsync.domain.model.CarDeviceDestinationInfo;
import jp.pioneer.carsync.domain.model.CustomBandSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MusicApp;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.domain.model.ThemeType;
import jp.pioneer.carsync.domain.model.TimeFormatSetting;
import jp.pioneer.carsync.domain.model.TunerSeekStep;
import jp.pioneer.carsync.domain.model.VoiceRecognizeMicType;
import jp.pioneer.carsync.domain.model.VoiceRecognizeType;
import jp.pioneer.carsync.presentation.model.AdasTrialState;
import jp.pioneer.carsync.presentation.model.CustomKey;
import jp.pioneer.carsync.presentation.model.ImpactNotificationMethod;
import jp.pioneer.carsync.presentation.model.TipsContentsEndpoint;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.util.YouTubeLinkActionHandler;

import static com.google.common.base.Preconditions.checkNotNull;
import static jp.pioneer.carsync.domain.content.AppMusicContract.QueryParamsBuilder.createAllSongs;

/**
 * アプリケーションの設定.
 * <p>
 * SharedPreferencesを直接使用せず、本クラスを使用すること。
 */
public class AppSharedPreference {
    /**
     * Preferenceキー:ログ出力有効.
     * <p>
     * 既定値:{@link #DEFAULT_LOG_ENABLED}
     *
     * @see #isLogEnabled()
     * @see #setLogEnabled(boolean) (boolean)
     */
    public static final String KEY_LOG_ENABLED = "log_enabled";
    /**
     * Preferenceキー:バージョンコード
     * <p>
     * 既定値:{@link #DEFAULT_APP_VERSION_CODE}
     *
     * @see #getVersionCode() ()
     * @see #setVersionCode(int)
     */
    public static final String KEY_APP_VERSION_CODE = "app_version_code";
    /**
     * Preferenceキー:利用規約バージョンコード
     * <p>
     * 既定値:{@link #DEFAULT_EULA_PRIVACY_VERSION_CODE}
     *
     * @see #getEulaPrivacyVersionCode() ()
     * @see #setEulaPrivacyVersionCode(int)
     */
    public static final String KEY_EULA_PRIVACY_VERSION_CODE = "eula_privacy_version_code";

    /**
     * Preferenceキー:利用規約の同意.
     * <p>
     * 既定値:{@link #DEFAULT_AGREED_EULA_PRIVACY_POLICY}
     *
     * @see #isAgreedEulaPrivacyPolicy()
     * @see #setAgreedEulaPrivacyPolicy(boolean)
     */
    public static final String KEY_AGREED_EULA_PRIVACY_POLICY = "agreed_eula_privacy_policy";

    /**
     * Preferenceキー:機能APIバージョンコード
     * <p>
     * 既定値:{@link #DEFAULT_ALEXA_CAPABILITIES_VERSION_CODE}
     *
     * @see #getAlexaCapabilitiesVersionCode()
     * @see #setAlexaCapabilitiesVersionCode(int)
     */
    public static final String KEY_ALEXA_CAPABILITIES_VERSION_CODE = "alexa_capabilities_version_code";

    /**
     * Preferenceキー:機能API設定済.
     * <p>
     * 既定値:{@link #DEFAULT_ALEXA_CAPABILITIES_SEND}
     *
     * @see #isAlexaCapabilitiesSend()
     * @see #setAlexaCapabilitiesSend(boolean)
     */
    public static final String KEY_ALEXA_CAPABILITIES_SEND = "alexa_capabilities_send";

    /**
     * Preferenceキー:初回初期設定済み.
     * <p>
     * 既定値:{@link #DEFAULT_FIRST_INITIAL_SETTING_COMPLETION}
     *
     * @see #isFirstInitialSettingCompletion()
     * @see #setFirstInitialSettingCompletion(boolean)
     */
    public static final String KEY_FIRST_INITIAL_SETTING_COMPLETION = "first_initial_setting_completion";

    /**
     * Preferenceキー:初回ADAS購入情報設定済み.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_PSEUDO_COOPERATION}
     *
     * @see #isAdasPseudoCooperation()
     * @see #setAdasPseudoCooperation(boolean)
     */
    public static final String KEY_ADAS_PSEUDO_COOPERATION = "adas_pseudo_cooperation";

    /**
     * Preferenceキー:TIPS記事URLのエンドポイント.
     * <p>
     * 既定値:{@link #DEFAULT_TIPS_LIST_ENDPOINT}
     *
     * @see #getTipsListEndpoint()
     * @see #setTipsListEndpoint(TipsContentsEndpoint) )
     */
    public static final String KEY_TIPS_LIST_ENDPOINT = "tips_list_endpoint";
    /**
     * Preferenceキー:バージョン1.1機能有効.
     * <p>
     * 既定値:{@link #DEFAULT_VERSION_1_1_FUNCTION_ENABLED}
     *
     * @see #isVersion_1_1_FunctionEnabled()
     * @see #setVersion_1_1_FunctionEnabled(boolean) )
     */
    public static final String KEY_VERSION_1_1_FUNCTION_ENABLED = "version_1_1_function_enabled";

    /**
     * Preferenceキー:最後に接続した車載機のモデル名.
     * <p>
     * 既定値:{@link #DEFAULT_LAST_CONNECTED_CAR_DEVICE_MODEL}
     *
     * @see #getLastConnectedCarDeviceModel()
     * @see #setLastConnectedCarDeviceModel(String)
     */
    public static final String KEY_LAST_CONNECTED_CAR_DEVICE_MODEL = "last_connected_car_device_model";

    /**
     * Preferenceキー:最後に接続した車載機の仕向け情報.
     * <p>
     * 既定値:{@link #DEFAULT_LAST_CONNECTED_CAR_DEVICE_DESTINATION}
     *
     * @see #getLastConnectedCarDeviceDestination()
     * @see #setLastConnectedCarDeviceDestination(int)
     */
    public static final String KEY_LAST_CONNECTED_CAR_DEVICE_DESTINATION = "last_connected_car_device_destination";

    /**
     * Preferenceキー:最後に接続した車載機のClassID.
     * <p>
     * 既定値:{@link #DEFAULT_LAST_CONNECTED_CAR_DEVICE_CLASS_ID}
     *
     * @see #getLastConnectedCarDeviceClassId()
     * @see #setLastConnectedCarDeviceClassId(CarDeviceClassId)
     */
    public static final String KEY_LAST_CONNECTED_CAR_DEVICE_CLASS_ID = "last_connected_car_device_class_id";

    /**
     * Preferenceキー:最後に接続した車載機がADAS対応/非対応.
     * <p>
     * 既定値:{@link #DEFAULT_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE}
     *
     * @see #getLastConnectedCarDeviceAdasAvailable()
     * @see #setLastConnectedCarDeviceAdasAvailable(boolean)
     */
    public static final String KEY_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE = "last_connected_car_device_adas_available";
    /**
     * Preferenceキー:最後に接続した車載機のAM Step.
     * <p>
     * 既定値:{@link #DEFAULT_LAST_CONNECTED_CAR_DEVICE_AM_STEP}
     *
     * @see #getLastConnectedCarDeviceAmStep()
     * @see #setLastConnectedCarDeviceAmStep(TunerSeekStep)
     */
    public static final String KEY_LAST_CONNECTED_CAR_DEVICE_AM_STEP = "last_connected_car_device_am_seek_step";
    /**
     * Preferenceキー:AppMusicのリピートモード.
     * <p>
     * 既定値:{@link #DEFAULT_APP_MUSIC_REPEAT_MODE}
     *
     * @see #getAppMusicRepeatMode()
     * @see #setAppMusicRepeatMode(SmartPhoneRepeatMode)
     */
    public static final String KEY_APP_MUSIC_REPEAT_MODE = "app_music_repeat_mode";
    /**
     * Preferenceキー:AppMusicのシャッフルモード.
     * <p>
     * 既定値:{@link #DEFAULT_APP_MUSIC_SHUFFLE_MODE}
     *
     * @see #getAppMusicShuffleMode()
     * @see #setAppMusicShuffleMode(ShuffleMode)
     */
    public static final String KEY_APP_MUSIC_SHUFFLE_MODE = "app_music_shuffle_mode";
    /**
     * Preferenceキー:AppMusicのクエリーパラメータ.
     * <p>
     * 既定値:{@link #DEFAULT_APP_MUSIC_QUERY_PARAMS}
     *
     * @see #getAppMusicQueryParams()
     * @see #setAppMusicQueryParams(QueryParams)
     */
    public static final String KEY_APP_MUSIC_QUERY_PARAMS = "app_music_query_params";
    /**
     * Preferenceキー:AppMusicのAudio ID.
     * <p>
     * Audio IDとは、MediaStoreから取得したAudioの{@code _id}の値である。
     * 未設定時は{@value -1}となる。
     * <p>
     * 既定値:{@link #DEFAULT_APP_MUSIC_AUDIO_ID}
     *
     * @see #getAppMusicAudioId()
     * @see #setAppMusicAudioId(long)
     */
    public static final String KEY_APP_MUSIC_AUDIO_ID = "app_music_audio_id";
    /**
     * Preferenceキー:AppMusicの再生位置.
     * <p>
     * 未設定時は{@value -1}となる。
     * <p>
     * 既定値:{@link #DEFAULT_APP_MUSIC_PLAY_POSITION}
     *
     * @see #getAppMusicAudioPlayPosition()
     * @see #setAppMusicAudioPlayPosition(int)
     */
    public static final String KEY_APP_MUSIC_PLAY_POSITION = "app_music_audio_play_position";

    /**
     * Preferenceキー:テーマ種別.
     * <p>
     * 既定値:{@link #DEFAULT_THEME_TYPE}
     *
     * @see #getThemeType()
     * @see #setThemeType(ThemeType)
     */
    public static final String KEY_THEME_TYPE = "theme_type_key";

    /**
     * Preferenceキー:マイフォト設定済.
     * <p>
     * 既定値:{@link #DEFAULT_THEME_MY_PHOTO_ENABLED}
     *
     * @see #getThemeMyPhotoEnabled()
     * @see #setThemeMyPhotoEnabled(boolean)
     */
    public static final String KEY_THEME_MY_PHOTO_ENABLED = "theme_my_photo_enabled";

    /**
     * Preferenceキー:UIカラー.
     * <p>
     * 既定値:{@link #DEFAULT_UI_COLOR}
     *
     * @see #getUiColor()
     * @see #setUiColor(UiColor)
     */
    public static final String KEY_UI_COLOR = "ui_color";

    /**
     * Preferenceキー:時計種別
     * <p>
     * 既定値:{@link #DEFAULT_CLOCK_TYPE}
     *
     * @see #getClockType()
     * @see #setClockType(int)
     */
    public static final String KEY_CLOCK_TYPE = "clock_type";
    /**
     * Preferenceキー:視覚効果.
     * <p>
     * 既定値:{@link #DEFAULT_VISUAL_EFFECT}
     *
     * @see #getVisualEffect()
     * @see #setVisualEffect(int)
     */
    public static final String KEY_VISUAL_EFFECT = "visual_effect";
    /**
     * Preferenceキー:衝突検知有効.
     * <p>
     * 既定値:{@link #DEFAULT_IMPACT_DETECTION_ENABLED}
     *
     * @see #isImpactDetectionEnabled()
     * @see #setImpactDetectionEnabled(boolean)
     */
    public static final String KEY_IMPACT_DETECTION_ENABLED = "impact_detection_enabled";
    /**
     * Preferenceキー:衝突検知通知方法.
     * <p>
     * 既定値:{@link #DEFAULT_IMPACT_NOTIFICATION_METHOD}
     *
     * @see #getImpactNotificationMethod()
     * @see #setImpactNotificationMethod(ImpactNotificationMethod)
     */
    public static final String KEY_IMPACT_NOTIFICATION_METHOD = "impact_notification_method";
    /**
     * Preferenceキー:衝突検知通知先（連絡者）.
     * <p>
     * 連絡先は統合するとIDが変化する場合があるため、{@code _id}の代わりに{@code lookup}を保存する。
     * 未設定時は{@value ""}となる。
     * <p>
     * 既定値:{@link #DEFAULT_IMPACT_NOTIFICATION_CONTACT_LOOKUP}
     *
     * @see #getImpactNotificationContactLookupKey()
     * @see #setImpactNotificationContactLookupKey(String)
     * @see ContactsContract.Phone#getLookupKey(android.database.Cursor)
     */
    public static final String KEY_IMPACT_NOTIFICATION_CONTACT_LOOKUP = "impact_notification_contact_lookup";
    /**
     * Preferenceキー:衝突検知通知先（電話番号）.
     * <p>
     * 電話番号とは、Contactsから取得した電話番号の{@code data1}の値である。
     * 未設定時は{@value ""}となる。
     * <p>
     * 既定値:{@link #DEFAULT_IMPACT_NOTIFICATION_CONTACT_NUMBER}
     *
     * @see #getImpactNotificationContactNumber()
     * @see #setImpactNotificationContactNumber(String)
     */
    public static final String KEY_IMPACT_NOTIFICATION_CONTACT_NUMBER = "impact_notification_contact_number";
    /**
     * Preferenceキー:衝突検知デバッグモード有効.
     * <p>
     * 既定値:{@link #DEFAULT_IMPACT_DETECTION_DEBUG_MODE_ENABLED}
     *
     * @see #isImpactDetectionDebugModeEnabled()
     * @see #setImpactDetectionDebugModeEnabled(boolean)
     */
    public static final String KEY_IMPACT_DETECTION_DEBUG_MODE_ENABLED = "impact_detection_debug_mode_enabled";
    /**
     * Preferenceキー:通知読み上げ有効.
     * <p>
     * 既定値:{@link #DEFAULT_READ_NOTIFICATION_ENABLED}
     *
     * @see #isReadNotificationEnabled()
     * @see #setReadNotificationEnabled(boolean)
     */
    public static final String KEY_READ_NOTIFICATION_ENABLED = "read_notification_enabled";
    /**
     * Preferenceキー:通知読み上げ対象アプリ.
     * <p>
     * 既定値:{@link #DEFAULT_READ_NOTIFICATION_APPS}
     *
     * @see #getReadNotificationApps()
     * @see #setReadNotificationApps(Application[])
     */
    public static final String KEY_READ_NOTIFICATION_APPS = "read_notification_apps";
    /**
     * Preferenceキー:ナビアプリ.
     * <p>
     * 未設定時は{@link Application#packageName}と{@link Application#label}が{@value ""}（空文字列）となる。
     * <p>
     * 既定値:{@link #DEFAULT_NAVIGATION_APP}
     *
     * @see #getNavigationApp()
     * @see #setNavigationApp(Application)
     */
    public static final String KEY_NAVIGATION_APP = "navigation_app";
    /**
     * Preferenceキー:Marin用ナビアプリ.
     * <p>
     * 未設定時は{@link Application#packageName}と{@link Application#label}が{@value ""}（空文字列）となる。
     * <p>
     * 既定値:{@link #DEFAULT_NAVIGATION_APP}
     *
     * @see #getNavigationMarinApp()
     * @see #setNavigationMarinApp(Application)
     */
    public static final String KEY_NAVIGATION_MARIN_APP = "navigation_marin_app";
    /**
     * Preferenceキー:Phone設定連絡先（連絡者）.
     * <p>
     * 連絡先は統合するとIDが変化する場合があるため、{@code _id}の代わりに{@code lookup}を保存する。
     * 未設定時は{@value ""}となる。
     * <p>
     * 既定値:{@link #DEFAULT_DIRECT_CALL_CONTACT_LOOKUP}
     *
     * @see #getDirectCallContactLookupKey()
     * @see #setDirectCallContactLookupKey(String)
     * @see ContactsContract.Phone#getLookupKey(android.database.Cursor)
     */
    public static final String KEY_DIRECT_CALL_CONTACT_LOOKUP = "direct_call_contact_lookup";
    /**
     * Preferenceキー:Phone設定連絡先（電話番号ID）.
     * <p>
     * 電話番号IDとは、Contactsから取得した電話番号の{@code _id}の値である。
     * 未設定時は{@value -1}となる。
     * <p>
     * 既定値:{@link #DEFAULT_DIRECT_CALL_CONTACT_NUMBER_ID}
     *
     * @see #getDirectCallContactNumberId()
     * @see #setDirectCallContactNumberId(long)
     */
    public static final String KEY_DIRECT_CALL_CONTACT_NUMBER_ID = "direct_call_contact_number_id";
    /**
     * Preferenceキー:アルバムアート表記有効.
     * <p>
     * アルバムリストのアルバムアートで表記するか
     * <p>
     * 既定値:{@link #DEFAULT_ALBUM_ART_ENABLED}
     *
     * @see #isAlbumArtEnabled()
     * @see #setAlbumArtEnabled(boolean)
     */
    public static final String KEY_ALBUM_ART_ENABLED = "album_art_enabled";
    /**
     * Preferenceキー:プレイリストのカード表記有効.
     * <p>
     * プレイリストをカード表記するか
     * <p>
     * 既定値:{@link #DEFAULT_PLAYLIST_CARD_ENABLED}
     *
     * @see #isPlaylistCardEnabled()
     * @see #setPlayListViewId(boolean)
     */
    public static final String KEY_PLAYLIST_CARD_ENABLED = "playlist_card_enabled";
    /**
     * Preferenceキー:ジャンルリストのカード表記有効.
     * <p>
     * ジャンルリストをカード表記するか
     * <p>
     * 既定値:{@link #DEFAULT_GENRE_CARD_ENABLED}
     *
     * @see #isGenreCardEnabled()
     * @see #setGenreCardEnabled(boolean)
     */
    public static final String KEY_GENRE_CARD_ENABLED = "genre_card_enabled";
    /**
     * Preferenceキー:の再生SourceLevelAdjusterの設定位置.
     * <p>
     * 未設定時は0となる。
     * <p>
     * 既定値:{@link #DEFAULT_SOURCE_LEVEL_ADJUSTER_POSITION}
     *
     * @see #getSourceLevelAdjusterPosition()
     * @see #setSourceLevelAdjusterPosition(int)
     */
    public static final String KEY_SOURCE_LEVEL_ADJUSTER_POSITION = "source_level_adjuster_position";
    /**
     * Preferenceキー:ショートカットボタンの表示有効.
     * <p>
     * ショートカットボタンを表示するか
     * <p>
     * 既定値:{@link #DEFAULT_SHORT_CUT_BUTTON_ENABLED}
     *
     * @see #isShortCutButtonEnabled()
     * @see #setShortCutButtonEnabled(boolean)
     */
    public static final String KEY_SHORT_CUT_BUTTON_ENABLED = "short_cut_button_enabled";
    /**
     * Preferenceキー:ショートカットボタンの表示有効設定を変更したか否か.
     * <p>
     * 既定値:{@link #DEFAULT_CONFIGURED_SHORT_CUT_BUTTON_ENABLED}
     *
     * @see #isConfiguredShortCutButtonEnabled()
     * @see #setConfiguredShortCutButtonEnabled(boolean)
     */
    public static final String KEY_CONFIGURED_SHORT_CUT_BUTTON_ENABLED = "configured_short_cut_button_enabled";
    /**
     * Preferenceキー:SoundFXのカスタムバンド設定A.
     * <p>
     * 既定値:{@link #DEFAULT_SOUND_FX_CUSTOM_BAND_SETTING_A}
     *
     * @see #setCustomBandSettingA(CustomBandSetting)
     * @see #getCustomBandSettingA()
     */
    public static final String KEY_SOUND_FX_CUSTOM_BAND_SETTING_A = "sound_fx_custom_band_setting_a";
    /**
     * Preferenceキー:SoundFXのカスタムバンド設定B.
     * <p>
     * 既定値:{@link #DEFAULT_SOUND_FX_CUSTOM_BAND_SETTING_B}
     *
     * @see #setCustomBandSettingB(CustomBandSetting)
     * @see #getCustomBandSettingB()
     */
    public static final String KEY_SOUND_FX_CUSTOM_BAND_SETTING_B = "sound_fx_custom_band_setting_b";
    /**
     * Preferenceキー:デバッグ用 SpecialEQの有効.
     * <p>
     * 既定値:{@link #DEFAULT_DEBUG_SPECIAL_EQ_ENABLED}
     *
     * @see #setCustomBandSettingB(CustomBandSetting)
     * @see #getCustomBandSettingB()
     */
    public static final String KEY_DEBUG_SPECIAL_EQ_ENABLED = "debug_special_eq_enabled";
    /**
     * Preferenceキー:音声認識の有効.
     * <p>
     * 音声認識機能を有効にするか
     * <p>
     * 既定値:{@link #DEFAULT_VOICE_RECOGNITION_ENABLED}
     *
     * @see #isVoiceRecognitionEnabled()
     * @see #setVoiceRecognitionEnabled(boolean)
     */
    public static final String KEY_VOICE_RECOGNITION_ENABLED = "voice_recognition_enabled";
    /**
     * Preferenceキー:音声認識の種別.
     * <p>
     * Alexa/Pioneer Smart Sync
     * <p>
     * 既定値:{@link #DEFAULT_VOICE_RECOGNITION_TYPE}
     *
     * @see #getVoiceRecognitionType()
     * @see #setVoiceRecognitionType(VoiceRecognizeType)
     */
    public static final String KEY_VOICE_RECOGNITION_TYPE = "voice_recognition_type";
    /**
     * Preferenceキー:音声認識マイクの種別.
     * <p>
     * 車載器/端末
     * <p>
     * 既定値:{@link #DEFAULT_VOICE_RECOGNITION_MIC_TYPE}
     *
     * @see #getVoiceRecognitionMicType()
     * @see #setVoiceRecognitionMicType(VoiceRecognizeMicType)
     */
    public static final String KEY_VOICE_RECOGNITION_MIC_TYPE = "voice_recognition_mic_type";
    /**
     * Preferenceキー:連絡帳アクセスの有効.
     * <p>
     * 連絡帳へのアクセスを有効にするか
     * <p>
     * 既定値:{@link #DEFAULT_PHONE_BOOK_ACCESSIBLE}
     *
     * @see #isVoiceRecognitionEnabled()
     * @see #setVoiceRecognitionEnabled(boolean)
     */
    public static final String KEY_PHONE_BOOK_ACCESSIBLE = "phone_book_accessible";
    /**
     * Preferenceキー:選択音楽アプリ.
     * <p>
     * 既定値:{@link #DEFAULT_MUSIC_APPS}
     *
     * @see #getMusicApps()
     * @see #setMusicApps(Application[])
     */
    public static final String KEY_MUSIC_APPS = "music_apps";
    /**
     * Preferenceキー:ADAS課金済か否か.
     * <p>
     * ADAS課金済みかどうか。
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_BILLING_RECORD
     *
     * @see #setAdasBillingRecord(boolean)
     * @see #isAdasBillingRecord()
     */
    public static final String KEY_ADAS_BILLING_RECORD = "adas_billing_record";
    /**
     * Preferenceキー:ADASのお試し期間終了日時.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_TRIAL_PERIOD_END}
     *
     * @see #setAdasTrialPeriodEndDate(long)
     * @see #getAdasTrialPeriodEndDate()
     */
    public static final String KEY_ADAS_TRIAL_PERIOD_END_DATE = "adas_trial_period_end_date";

    /**
     * Preferenceキー:ADASのお試し実施状態.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_TRIAL_STATE}
     *
     * @see #setAdasTrialState(AdasTrialState)
     * @see #getAdasTrialState()
     */
    public static final String KEY_ADAS_TRIAL_STATE = "adas_trial_state";
    /**
     * Preferenceキー:ADAS 有効設定.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_ENABLED}
     *
     * @see #setAdasEnabled(boolean)
     * @see #isAdasEnabled()
     */
    public static final String KEY_ADAS_ENABLED = "adas_enabled";
    /**
     * Preferenceキー:ADAS Alarm 有効設定.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_ALARM_ENABLED}
     *
     * @see #setAdasAlarmEnabled(boolean)
     * @see #isAdasAlarmEnabled()
     */
    public static final String KEY_ADAS_ALARM_ENABLED = "adas_alarm_enabled";
    /**
     * Preferenceキー:ADAS設定済か否か.
     * <p>
     * ADASの設定値が設定済みかどうか。
     * 全ての設定を終えているかを判断するために使用する
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_SETTING_CONFIGURED}
     *
     * @see #setAdasSettingConfigured(boolean)
     * @see #isAdasSettingConfigured()
     */
    public static final String KEY_ADAS_SETTING_CONFIGURED = "adas_setting_configured";
    /**
     * Preferenceキー:ADAS キャリブレーション設定.
     * <p>
     * 画面内に見える車体の先端の高さ[px]
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_CALIBRATION_SETTING}
     *
     * @see #setAdasCalibrationSetting(int)
     * @see #getAdasCalibrationSetting()
     */
    public static final String KEY_ADAS_CALIBRATION_SETTING = "adas_calibration_px_setting";
    /**
     * Preferenceキー:ADAS カメラ設定.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_CAMERA_SETTING}
     *
     * @see #setAdasCameraSetting(jp.pioneer.carsync.domain.model.AdasCameraSetting)
     * @see #getAdasCameraSetting()
     */
    public static final String KEY_ADAS_CAMERA_SETTING = "adas_camera_position_setting";
    /**
     * Preferenceキー:ADAS LDW設定.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_LDW_SETTING}
     *
     * @see #setAdasLdwSetting(AdasFunctionSetting)
     * @see #getAdasLdwSetting()
     */
    public static final String KEY_ADAS_LDW_SETTING = "adas_ldw_setting";
    /**
     * Preferenceキー:ADAS PCW設定.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_PCW_SETTING}
     *
     * @see #setAdasPcwSetting(AdasFunctionSetting)
     * @see #getAdasPcwSetting()
     */
    public static final String KEY_ADAS_PCW_SETTING = "adas_pcw_setting";
    /**
     * Preferenceキー:ADAS FCW設定.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_FCW_SETTING}
     *
     * @see #setAdasFcwSetting(AdasFunctionSetting)
     * @see #getAdasFcwSetting()
     */
    public static final String KEY_ADAS_FCW_SETTING = "adas_fcw_setting";
    /**
     * Preferenceキー:ADAS LKW設定.
     * <p>
     * 既定値:{@link #DEFAULT_ADAS_LKW_SETTING}
     *
     * @see #setAdasLkwSetting(AdasFunctionSetting)
     * @see #getAdasLkwSetting()
     */
    public static final String KEY_ADAS_LKW_SETTING = "adas_lkw_setting";
    /**
     * Preferenceキー:ライティングエフェクト有効.
     * <p>
     * 既定値:{@link #DEFAULT_LIGHTING_EFFECT_ENABLED}
     *
     * @see #setLightingEffectEnabled(boolean)
     * @see #isLightingEffectEnabled()
     */
    public static final String KEY_LIGHTING_EFFECT_ENABLED = "lighting_effect_enabled";
    /**
     * Preferenceキー:距離単位.
     * <p>
     * 既定値:{@link #DEFAULT_DISTANCE_UNIT}
     *
     * @see #setDistanceUnit(DistanceUnit)
     * @see #getDistanceUnit()
     */
    public static final String KEY_DISTANCE_UNIT = "distance_unit";
    /**
     * Preferenceキー:時刻表示.
     * <p>
     * 既定値:{@link #DEFAULT_TIME_FORMAT_SETTING}
     *
     * @see #setTimeFormatSetting(TimeFormatSetting)
     * @see #getTimeFormatSetting()
     */
    public static final String KEY_TIME_FORMAT_SETTING = "time_format_setting";
    /**
     * Preferenceキー:初回SLA設定を実施済.
     * <p>
     * 既定値:{@link #DEFAULT_CONFIGURED_INITIAL_SLA_SETTING}
     *
     * @see #setConfiguredSlaSetting(boolean)
     * @see #isConfiguredSlaSetting()
     */
    public static final String KEY_CONFIGURED_INITIAL_SLA_SETTING = "configured_initial_sla_setting";

    /**
     * Preferenceキー:Alexa言語設定.
     * <p>
     * 既定値:{@link #DEFAULT_ALEXA_LANGUAGE_SETTING}
     *
     * @see #setAlexaLanguage(AlexaLanguageType)
     * @see #getAlexaLanguage()
     */
    public static final String KEY_ALEXA_LANGUAGE_SETTING = "alexa_language_setting";
    /**
     * Preferenceキー:フォアグラウンドサービス常駐.
     * <p>
     * 既定値:{@link #DEFAULT_APP_SERVICE_RESIDENT}
     *
     * @see #setAppServiceResident(boolean)
     * @see #isAppServiceResident()
     */
    public static final String KEY_APP_SERVICE_RESIDENT = "app_service_resident";
    /**
     * Preferenceキー:カスタムキー種別.
     * <p>
     * 既定値:{@link #DEFAULT_CUSTOM_KEY_TYPE}
     *
     * @see #setCustomKeyType(CustomKey)
     * @see #getCustomKeyType()
     */
    public static final String KEY_CUSTOM_KEY_TYPE = "custom_key_type";
    /**
     * Preferenceキー:カスタムキー　ダイレクトソース切替の対象ソース.
     * <p>
     * 既定値:{@link #DEFAULT_CUSTOM_KEY_DIRECT_SOURCE}
     *
     * @see #setCustomKeyDirectSource(MediaSourceType)
     * @see #getCustomKeyDirectSource()
     */
    public static final String KEY_CUSTOM_KEY_DIRECT_SOURCE = "custom_key_direct_source";
    /**
     * Preferenceキー:カスタムキー　3rdApp切替の該当アプリ.
     * <p>
     * 既定値:{@link #DEFAULT_CUSTOM_KEY_MUSIC_APP}
     *
     * @see #setCustomKeyMusicApp(Application)
     * @see #getCustomKeyMusicApp()
     */
    public static final String KEY_CUSTOM_KEY_MUSIC_APP = "custom_key_music_app";
    /**
     * Preferenceキー:YouTubeLink設定 有効/無効状態
     * <p>
     * 既定値:{@link #DEFAULT_YOUTUBE_LINK_SETTING_ENABLED}
     *
     * @see #isYouTubeLinkSettingEnabled()
     * @see #setYouTubeLinkSettingEnabled(boolean)
     */
    public static final String KEY_YOUTUBE_LINK_SETTING_ENABLED = "youtube_link_enabled";
    /**
     * Preferenceキー:YouTubeLinkCaution画面の次回以降非表示設定
     * <p>
     * 既定値:{@link #DEFAULT_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN}
     *
     * @see #isYouTubeLinkCautionNoDisplayAgain()
     * @see #setYouTubeLinkCautionNoDisplayAgain(boolean)
     */
    public static final String KEY_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN = "youtube_link_caution_no_display_again";

    private static final boolean DEFAULT_LOG_ENABLED = false;
    private static final int DEFAULT_APP_VERSION_CODE = 1;
    private static final int DEFAULT_EULA_PRIVACY_VERSION_CODE = 1;
    private static final boolean DEFAULT_AGREED_EULA_PRIVACY_POLICY = false;
    private static final int DEFAULT_ALEXA_CAPABILITIES_VERSION_CODE = 1;
    private static final boolean DEFAULT_ALEXA_CAPABILITIES_SEND = false;
    private static final boolean DEFAULT_FIRST_INITIAL_SETTING_COMPLETION = false;
    private static final boolean DEFAULT_ADAS_PSEUDO_COOPERATION = false;
    private static final String DEFAULT_TIPS_LIST_ENDPOINT = TipsContentsEndpoint.PRODUCTION.name();
    private static final boolean DEFAULT_VERSION_1_1_FUNCTION_ENABLED = true;
    private static final String DEFAULT_LAST_CONNECTED_CAR_DEVICE_MODEL = "";
    private static final int DEFAULT_LAST_CONNECTED_CAR_DEVICE_DESTINATION = CarDeviceDestinationInfo.UNKNOWN.code;
    private static final String DEFAULT_LAST_CONNECTED_CAR_DEVICE_CLASS_ID = CarDeviceClassId.DEH.name();
    private static final boolean DEFAULT_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE = false;
    private static final String DEFAULT_LAST_CONNECTED_CAR_DEVICE_AM_STEP = TunerSeekStep._9KHZ.name();
    private static final String DEFAULT_APP_MUSIC_REPEAT_MODE = SmartPhoneRepeatMode.ALL.name();
    private static final String DEFAULT_APP_MUSIC_SHUFFLE_MODE = ShuffleMode.OFF.name();
    private static final String DEFAULT_APP_MUSIC_QUERY_PARAMS = getGson().toJson(createAllSongs());
    private static final long DEFAULT_APP_MUSIC_AUDIO_ID = -1;
    private static final int DEFAULT_APP_MUSIC_PLAY_POSITION = -1;
    private static final String DEFAULT_THEME_TYPE = ThemeType.PICTURE_PATTERN1.name();
    private static final boolean DEFAULT_THEME_MY_PHOTO_ENABLED = false;
    private static final String DEFAULT_UI_COLOR = UiColor.AQUA.name();
    private static final int DEFAULT_CLOCK_TYPE = 0;
    private static final int DEFAULT_VISUAL_EFFECT = 0;
    private static final boolean DEFAULT_IMPACT_DETECTION_ENABLED = false;
    private static final String DEFAULT_IMPACT_NOTIFICATION_METHOD = ImpactNotificationMethod.SMS.name();
    private static final String DEFAULT_IMPACT_NOTIFICATION_CONTACT_LOOKUP = "";
    private static final String DEFAULT_IMPACT_NOTIFICATION_CONTACT_NUMBER = "";
    private static final boolean DEFAULT_IMPACT_DETECTION_DEBUG_MODE_ENABLED = false;
    private static final boolean DEFAULT_READ_NOTIFICATION_ENABLED = false;
    private static final String DEFAULT_READ_NOTIFICATION_APPS = new Gson().toJson(new Application[0]);
    private static final String DEFAULT_NAVIGATION_APP = new Gson().toJson(new Application(NaviApp.GOOGLE_MAP.getPackageName(), ""));
    private static final String DEFAULT_NAVIGATION_MARIN_APP = new Gson().toJson(new Application(NaviApp.GOOGLE_MAP.getPackageName(), ""));
    private static final String DEFAULT_DIRECT_CALL_CONTACT_LOOKUP = "";
    private static final long DEFAULT_DIRECT_CALL_CONTACT_NUMBER_ID = -1;
    private static final boolean DEFAULT_ALBUM_ART_ENABLED = true;
    private static final boolean DEFAULT_PLAYLIST_CARD_ENABLED = true;
    private static final boolean DEFAULT_GENRE_CARD_ENABLED = true;
    private static final int DEFAULT_SOURCE_LEVEL_ADJUSTER_POSITION = 0;
    private static final boolean DEFAULT_SHORT_CUT_BUTTON_ENABLED = true;
    private static final boolean DEFAULT_CONFIGURED_SHORT_CUT_BUTTON_ENABLED = false;
    private static final String DEFAULT_SOUND_FX_CUSTOM_BAND_SETTING_A = getGson().toJson(new CustomBandSetting(CustomEqType.CUSTOM1));
    private static final String DEFAULT_SOUND_FX_CUSTOM_BAND_SETTING_B = getGson().toJson(new CustomBandSetting(CustomEqType.CUSTOM2));
    private static final boolean DEFAULT_DEBUG_SPECIAL_EQ_ENABLED = false;
    private static final boolean DEFAULT_VOICE_RECOGNITION_ENABLED = true;
    private static final String DEFAULT_VOICE_RECOGNITION_TYPE = VoiceRecognizeType.PIONEER_SMART_SYNC.name();
    private static final String DEFAULT_VOICE_RECOGNITION_MIC_TYPE = VoiceRecognizeMicType.HEADSET.name();
    private static final boolean DEFAULT_PHONE_BOOK_ACCESSIBLE = false;
    private static final String DEFAULT_MUSIC_APPS = new Gson().toJson(new Application[0]);
    private static final boolean DEFAULT_ADAS_BILLING_RECORD = false;
    private static final String DEFAULT_ADAS_TRIAL_STATE = AdasTrialState.TRIAL_BEFORE.name();
    private static final long DEFAULT_ADAS_TRIAL_PERIOD_END = 0;
    private static final boolean DEFAULT_ADAS_ENABLED = false;
    private static final boolean DEFAULT_ADAS_ALARM_ENABLED = true;
    private static final boolean DEFAULT_ADAS_SETTING_CONFIGURED = false;
    private static final int DEFAULT_ADAS_CALIBRATION_SETTING = 0;
    private static final String DEFAULT_ADAS_CAMERA_SETTING = new Gson().toJson(new AdasCameraSetting());
    private static final String DEFAULT_ADAS_LDW_SETTING = new Gson().toJson(new AdasFunctionSetting(AdasFunctionType.LDW));
    private static final String DEFAULT_ADAS_PCW_SETTING = new Gson().toJson(new AdasFunctionSetting(AdasFunctionType.PCW));
    private static final String DEFAULT_ADAS_FCW_SETTING = new Gson().toJson(new AdasFunctionSetting(AdasFunctionType.FCW));
    private static final String DEFAULT_ADAS_LKW_SETTING = new Gson().toJson(new AdasFunctionSetting(AdasFunctionType.LKW));
    private static final boolean DEFAULT_LIGHTING_EFFECT_ENABLED = true;
    private static final String DEFAULT_DISTANCE_UNIT = DistanceUnit.METER_KILOMETER.name();
    private static final String DEFAULT_TIME_FORMAT_SETTING = TimeFormatSetting.TIME_FORMAT_24.name();
    private static final boolean DEFAULT_CONFIGURED_INITIAL_SLA_SETTING = false;
    private static final String DEFAULT_ALEXA_LANGUAGE_SETTING = AlexaLanguageType.ENGLISH_US.name();
    private static final boolean DEFAULT_APP_SERVICE_RESIDENT = true;
    private static final String DEFAULT_CUSTOM_KEY_TYPE = CustomKey.SOURCE_CHANGE.name();
    private static final String DEFAULT_CUSTOM_KEY_DIRECT_SOURCE = MediaSourceType.OFF.name();
    private static final String DEFAULT_CUSTOM_KEY_MUSIC_APP = new Gson().toJson(new Application(MusicApp.GOOGLE_PLAY_MUSIC.getPackageName(), ""));;
    private static final boolean DEFAULT_YOUTUBE_LINK_SETTING_ENABLED = false;
    private static final boolean DEFAULT_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN = false;
    private final SharedPreferences mPreferences;
    private final Object mContent = new Object();
    private final WeakHashMap<OnAppSharedPreferenceChangeListener, Object> mListeners = new WeakHashMap<>();
    private final SharedPreferences.OnSharedPreferenceChangeListener mListener = (sharedPreferences, key) -> notifyListeners(key);
    private static volatile Gson sGson = getGson();

    /**
     * アプリケーション.
     * <p>
     * 通知読み上げアプリやナビアプリの設定値保存の際に使用。
     * {@link ApplicationInfo}を元に作成する。
     */
    public static class Application {
        /** パッケージ名. */
        @NonNull public String packageName;
        /** ラベル名. */
        @NonNull public String label;

        /**
         * コンストラクタ.
         *
         * @param packageName パッケージ名
         * @param label       ラベル名
         * @throws NullPointerException {@code packageName}か{@code label}がnull
         */
        public Application(@NonNull String packageName, @NonNull String label) {
            this.packageName = checkNotNull(packageName);
            this.label = checkNotNull(label);
        }

        /**
         * {@link ApplicationInfo}から{@link Application}生成.
         *
         * @param pm              PackageManager
         * @param applicationInfo ApplicationInfo
         * @return Application
         * @throws NullPointerException {@code pm}か{@code applicationInfo}がnull
         */
        public static Application from(@NonNull PackageManager pm, @NonNull ApplicationInfo applicationInfo) {
            checkNotNull(pm);
            checkNotNull(applicationInfo);
            return new Application(applicationInfo.packageName, applicationInfo.loadLabel(pm).toString());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }

            if (getClass() != obj.getClass()) {
                return false;
            }

            Application other = (Application) obj;
            return Objects.equal(packageName, other.packageName)
                    && Objects.equal(label, other.label);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int hashCode() {
            return Objects.hashCode(packageName, label);
        }
    }

    /**
     * コンストラクタ.
     *
     * @param preferences SharedPreferences
     * @throws NullPointerException {@code preferences}がnull
     */
    public AppSharedPreference(@NonNull SharedPreferences preferences) {
        mPreferences = checkNotNull(preferences);
        mPreferences.registerOnSharedPreferenceChangeListener(mListener);
    }

    /**
     * 設定変更時に呼ばれるリスナー登録.
     * <p>
     * リスナーは弱参照で保持するので、利用者はリスナーを強参照で保持すること。
     * 登録済のリスナーを指定した場合多重登録する。
     *
     * @param listener OnAppSharedPreferenceChangeListener
     * @throws NullPointerException {@code listener}がnull
     * @see #unregisterOnAppSharedPreferenceChangeListener(OnAppSharedPreferenceChangeListener)
     */
    public void registerOnAppSharedPreferenceChangeListener(@NonNull OnAppSharedPreferenceChangeListener listener) {
        checkNotNull(listener);

        synchronized (this) {
            mListeners.put(listener, mContent);
        }
    }

    /**
     * 設定変更時に呼ばれるリスナーの登録解除.
     * <p>
     * 未登録のリスナーを指定した場合何もしない。
     *
     * @param listener OnAppSharedPreferenceChangeListener
     * @throws NullPointerException {@code listener}がnull
     * @see #registerOnAppSharedPreferenceChangeListener(OnAppSharedPreferenceChangeListener)
     */
    public void unregisterOnAppSharedPreferenceChangeListener(@NonNull OnAppSharedPreferenceChangeListener listener) {
        checkNotNull(listener);

        synchronized (this) {
            mListeners.remove(listener);
        }
    }

    public synchronized boolean isRegistered(@NonNull OnAppSharedPreferenceChangeListener listener) {
        checkNotNull(listener);
        return mListeners.containsKey(listener);
    }
    /**
     * 常時待ち受けするか否か取得.
     *
     * @return {@code true}:常駐　{@code false}:常駐しない。
     * @see #setAppServiceResident(boolean)
     * @see #KEY_APP_SERVICE_RESIDENT
     */
    public boolean isAppServiceResident() {
        if (mPreferences.contains(KEY_APP_SERVICE_RESIDENT)) {
            return mPreferences.getBoolean(KEY_APP_SERVICE_RESIDENT, DEFAULT_APP_SERVICE_RESIDENT);
        } else {
            setAppServiceResident(DEFAULT_APP_SERVICE_RESIDENT);
            return DEFAULT_APP_SERVICE_RESIDENT;
        }
    }

    /**
     * 常時待ち受け設定.
     *
     * @param enabled {@code true}:常駐。{@code false}:常駐しない。
     * @return 本オブジェクト
     * @see #isAppServiceResident()
     * @see #KEY_APP_SERVICE_RESIDENT
     */
    @NonNull
    public AppSharedPreference setAppServiceResident(boolean enabled) {
        mPreferences.edit()
                .putBoolean(KEY_APP_SERVICE_RESIDENT, enabled)
                .apply();
        return this;
    }
    /**
     * ログ出力するか否か取得.
     *
     * @return {@code true}:同意した。{@code false}:同意していない。
     * @see #setLogEnabled(boolean)
     * @see #KEY_LOG_ENABLED
     */
    public boolean isLogEnabled() {
        if (mPreferences.contains(KEY_LOG_ENABLED)) {
            return mPreferences.getBoolean(KEY_LOG_ENABLED, DEFAULT_LOG_ENABLED);
        } else {
            setLogEnabled(DEFAULT_LOG_ENABLED);
            return DEFAULT_LOG_ENABLED;
        }
    }

    /**
     * ログ出力設定.
     *
     * @param enabled {@code true}:出力する。{@code false}:出力しない。
     * @return 本オブジェクト
     * @see #isLogEnabled()
     * @see #KEY_LOG_ENABLED
     */
    @NonNull
    public AppSharedPreference setLogEnabled(boolean enabled) {
        mPreferences.edit()
                .putBoolean(KEY_LOG_ENABLED, enabled)
                .apply();
        return this;
    }
    /**
     * バージョンコード取得.
     *
     * @return バージョンコード
     * @see #setVersionCode(int)
     * @see #KEY_APP_VERSION_CODE
     */
    public int getVersionCode() {
        if (mPreferences.contains(KEY_APP_VERSION_CODE)) {
            return mPreferences.getInt(KEY_APP_VERSION_CODE, DEFAULT_APP_VERSION_CODE);
        } else {
            setVersionCode(DEFAULT_APP_VERSION_CODE);
            return DEFAULT_APP_VERSION_CODE;
        }
    }

    /**
     * バージョンコード設定.
     *
     * @param versionCode バージョンコード
     * @return 本オブジェクト
     * @see #getVersionCode()
     * @see #KEY_APP_VERSION_CODE
     */
    @NonNull
    public AppSharedPreference setVersionCode(int versionCode) {
        mPreferences.edit()
                .putInt(KEY_APP_VERSION_CODE, versionCode)
                .apply();
        return this;
    }

    /**
     * 利用規約バージョン取得.
     *
     * @return バージョンコード
     * @see #setEulaPrivacyVersionCode(int)
     * @see #KEY_EULA_PRIVACY_VERSION_CODE
     */
    public int getEulaPrivacyVersionCode() {
        if (mPreferences.contains(KEY_EULA_PRIVACY_VERSION_CODE)) {
            return mPreferences.getInt(KEY_EULA_PRIVACY_VERSION_CODE, DEFAULT_EULA_PRIVACY_VERSION_CODE);
        } else {
            setEulaPrivacyVersionCode(DEFAULT_EULA_PRIVACY_VERSION_CODE);
            return DEFAULT_EULA_PRIVACY_VERSION_CODE;
        }
    }

    /**
     * 利用規約バージョン設定.
     *
     * @param versionCode バージョンコード
     * @return 本オブジェクト
     * @see #getEulaPrivacyVersionCode()
     * @see #KEY_EULA_PRIVACY_VERSION_CODE
     */
    @NonNull
    public AppSharedPreference setEulaPrivacyVersionCode(int versionCode) {
        mPreferences.edit()
                .putInt(KEY_EULA_PRIVACY_VERSION_CODE, versionCode)
                .apply();
        return this;
    }

    /**
     * 利用規約を同意したか否か取得.
     *
     * @return {@code true}:同意した。{@code false}:同意していない。
     * @see #setAgreedEulaPrivacyPolicy(boolean)
     * @see #KEY_AGREED_EULA_PRIVACY_POLICY
     */
    public boolean isAgreedEulaPrivacyPolicy() {
        if (mPreferences.contains(KEY_AGREED_EULA_PRIVACY_POLICY)) {
            return mPreferences.getBoolean(KEY_AGREED_EULA_PRIVACY_POLICY, DEFAULT_AGREED_EULA_PRIVACY_POLICY);
        } else {
            setAgreedEulaPrivacyPolicy(DEFAULT_AGREED_EULA_PRIVACY_POLICY);
            return DEFAULT_AGREED_EULA_PRIVACY_POLICY;
        }
    }

    /**
     * 利用規約同意設定.
     *
     * @param agree {@code true}:同意する。{@code false}:同意しない。
     * @return 本オブジェクト
     * @see #isAgreedEulaPrivacyPolicy()
     * @see #KEY_AGREED_EULA_PRIVACY_POLICY
     */
    @NonNull
    public AppSharedPreference setAgreedEulaPrivacyPolicy(boolean agree) {
        mPreferences.edit()
                .putBoolean(KEY_AGREED_EULA_PRIVACY_POLICY, agree)
                .apply();
        return this;
    }
    /**
     * Alexa機能APIバージョン取得.
     *
     * @return バージョンコード
     * @see #setAlexaCapabilitiesVersionCode(int)
     * @see #KEY_ALEXA_CAPABILITIES_VERSION_CODE
     */
    public int getAlexaCapabilitiesVersionCode() {
        if (mPreferences.contains(KEY_ALEXA_CAPABILITIES_VERSION_CODE)) {
            return mPreferences.getInt(KEY_ALEXA_CAPABILITIES_VERSION_CODE, DEFAULT_ALEXA_CAPABILITIES_VERSION_CODE);
        } else {
            setEulaPrivacyVersionCode(DEFAULT_ALEXA_CAPABILITIES_VERSION_CODE);
            return DEFAULT_ALEXA_CAPABILITIES_VERSION_CODE;
        }
    }

    /**
     * Alexa機能APIバージョン設定.
     *
     * @param versionCode バージョンコード
     * @return 本オブジェクト
     * @see #getAlexaCapabilitiesVersionCode()
     * @see #KEY_ALEXA_CAPABILITIES_VERSION_CODE
     */
    @NonNull
    public AppSharedPreference setAlexaCapabilitiesVersionCode(int versionCode) {
        mPreferences.edit()
                .putInt(KEY_ALEXA_CAPABILITIES_VERSION_CODE, versionCode)
                .apply();
        return this;
    }

    /**
     * Alexa機能API設定したか否か取得.
     *
     * @return {@code true}:同意した。{@code false}:同意していない。
     * @see #setAlexaCapabilitiesSend(boolean)
     * @see #KEY_ALEXA_CAPABILITIES_SEND
     */
    public boolean isAlexaCapabilitiesSend() {
        if (mPreferences.contains(KEY_ALEXA_CAPABILITIES_SEND)) {
            return mPreferences.getBoolean(KEY_ALEXA_CAPABILITIES_SEND, DEFAULT_ALEXA_CAPABILITIES_SEND);
        } else {
            setAlexaCapabilitiesSend(DEFAULT_ALEXA_CAPABILITIES_SEND);
            return DEFAULT_ALEXA_CAPABILITIES_SEND;
        }
    }

    /**
     * Alexa機能API設定.
     *
     * @param agree {@code true}:送信済。{@code false}:送信してない。
     * @return 本オブジェクト
     * @see #isAlexaCapabilitiesSend()
     * @see #KEY_ALEXA_CAPABILITIES_SEND
     */
    @NonNull
    public AppSharedPreference setAlexaCapabilitiesSend(boolean agree) {
        mPreferences.edit()
                .putBoolean(KEY_ALEXA_CAPABILITIES_SEND, agree)
                .apply();
        return this;
    }
    /**
     * 初回の初期設定をしたか否か取得.
     *
     * @return {@code true}:設定した。{@code false}:設定していない。
     * @see #setFirstInitialSettingCompletion(boolean)
     * @see #KEY_FIRST_INITIAL_SETTING_COMPLETION
     */
    public boolean isFirstInitialSettingCompletion() {
        if (mPreferences.contains(KEY_FIRST_INITIAL_SETTING_COMPLETION)) {
            return mPreferences.getBoolean(KEY_FIRST_INITIAL_SETTING_COMPLETION, DEFAULT_FIRST_INITIAL_SETTING_COMPLETION);
        } else {
            setFirstInitialSettingCompletion(DEFAULT_FIRST_INITIAL_SETTING_COMPLETION);
            return DEFAULT_FIRST_INITIAL_SETTING_COMPLETION;
        }
    }

    /**
     * 初回初期設定.
     *
     * @param completion {@code true}:設定した。{@code false}:設定していない。
     * @return 本オブジェクト
     * @see #isFirstInitialSettingCompletion()
     * @see #KEY_FIRST_INITIAL_SETTING_COMPLETION
     */
    @NonNull
    public AppSharedPreference setFirstInitialSettingCompletion(boolean completion) {
        mPreferences.edit()
                .putBoolean(KEY_FIRST_INITIAL_SETTING_COMPLETION, completion)
                .apply();
        return this;
    }

    /**
     * ADAS実行用擬似連携してるか取得
     *
     * @return {@code true}:有効。{@code false}:無効。
     * @see #setAdasPseudoCooperation(boolean)
     * @see #KEY_ADAS_PSEUDO_COOPERATION
     */
    public boolean isAdasPseudoCooperation() {
        if (mPreferences.contains(KEY_ADAS_PSEUDO_COOPERATION)) {
            return mPreferences.getBoolean(KEY_ADAS_PSEUDO_COOPERATION, DEFAULT_ADAS_PSEUDO_COOPERATION);
        } else {
            setFirstInitialSettingCompletion(DEFAULT_ADAS_PSEUDO_COOPERATION);
            return DEFAULT_ADAS_PSEUDO_COOPERATION;
        }
    }

    /**
     * ADAS実行用擬似連携設定.
     *
     * @param enabled {@code true}:有効。{@code false}:無効。
     * @return 本オブジェクト
     * @see #isAdasPseudoCooperation()
     * @see #KEY_ADAS_PSEUDO_COOPERATION
     */
    @NonNull
    public AppSharedPreference setAdasPseudoCooperation(boolean enabled) {
        mPreferences.edit()
                .putBoolean(KEY_ADAS_PSEUDO_COOPERATION, enabled)
                .apply();
        return this;
    }

    /**
     * TIPS記事URLのエンドポイント取得.
     *
     * @return エンドポイント
     * @see #setTipsListEndpoint(TipsContentsEndpoint)
     * @see #KEY_TIPS_LIST_ENDPOINT
     */
    public TipsContentsEndpoint getTipsListEndpoint() {
        if (mPreferences.contains(KEY_TIPS_LIST_ENDPOINT)) {
            String value = mPreferences.getString(KEY_TIPS_LIST_ENDPOINT, DEFAULT_TIPS_LIST_ENDPOINT);
            return TipsContentsEndpoint.valueOf(value);
        } else {
            TipsContentsEndpoint endpoint = TipsContentsEndpoint.valueOf(DEFAULT_TIPS_LIST_ENDPOINT);
            setTipsListEndpoint(endpoint);
            return endpoint;
        }
    }

    /**
     * TIPS記事URLのエンドポイント設定.
     *
     * @param endpoint エンドポイント
     * @return 本オブジェクト
     * @see #getTipsListEndpoint()
     * @see #KEY_TIPS_LIST_ENDPOINT
     */
    @NonNull
    public AppSharedPreference setTipsListEndpoint(TipsContentsEndpoint endpoint) {
        mPreferences.edit()
                .putString(KEY_TIPS_LIST_ENDPOINT, endpoint.name())
                .apply();
        return this;
    }

    /**
     * バージョン1.1機能が有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setVersion_1_1_FunctionEnabled(boolean)
     * @see #KEY_VERSION_1_1_FUNCTION_ENABLED
     */
    public boolean isVersion_1_1_FunctionEnabled() {
        return DEFAULT_VERSION_1_1_FUNCTION_ENABLED;
    }

    /**
     * バージョン1.1機能有効設定.
     *
     * @param enabled {@code true}:有効。{@code false}:無効。
     * @return 本オブジェクト
     * @see #isVersion_1_1_FunctionEnabled()
     * @see #KEY_VERSION_1_1_FUNCTION_ENABLED
     */
    @NonNull
    public AppSharedPreference setVersion_1_1_FunctionEnabled(boolean enabled) {
        mPreferences.edit()
                .putBoolean(KEY_VERSION_1_1_FUNCTION_ENABLED, enabled)
                .apply();
        return this;
    }

    /**
     * 最後に接続した車載機のモデル名取得.
     *
     * @return 車載機名
     * @see #setLastConnectedCarDeviceModel(String)
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_MODEL
     */
    public String getLastConnectedCarDeviceModel() {
        if (mPreferences.contains(KEY_LAST_CONNECTED_CAR_DEVICE_MODEL)) {
            return mPreferences.getString(KEY_LAST_CONNECTED_CAR_DEVICE_MODEL, DEFAULT_LAST_CONNECTED_CAR_DEVICE_MODEL);
        } else {
            setLastConnectedCarDeviceModel(DEFAULT_LAST_CONNECTED_CAR_DEVICE_MODEL);
            return DEFAULT_LAST_CONNECTED_CAR_DEVICE_MODEL;
        }
    }

    /**
     * 最後に接続した車載機のモデル名設定.
     *
     * @param name 車載機名
     * @return 本オブジェクト
     * @see #getLastConnectedCarDeviceModel()
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_MODEL
     */
    @NonNull
    public AppSharedPreference setLastConnectedCarDeviceModel(String name) {
        mPreferences.edit()
                .putString(KEY_LAST_CONNECTED_CAR_DEVICE_MODEL, name)
                .apply();
        return this;
    }

    /**
     * 最後に接続した車載機の仕向け情報取得.
     *
     * @return 仕向け情報
     * @see #setLastConnectedCarDeviceDestination(int)
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_DESTINATION
     */
    public int getLastConnectedCarDeviceDestination() {
        if (mPreferences.contains(KEY_LAST_CONNECTED_CAR_DEVICE_DESTINATION)) {
            return mPreferences.getInt(KEY_LAST_CONNECTED_CAR_DEVICE_DESTINATION, DEFAULT_LAST_CONNECTED_CAR_DEVICE_DESTINATION);
        } else {
            setLastConnectedCarDeviceDestination(DEFAULT_LAST_CONNECTED_CAR_DEVICE_DESTINATION);
            return DEFAULT_LAST_CONNECTED_CAR_DEVICE_DESTINATION;
        }
    }

    /**
     * 最後に接続した車載機の仕向け情報設定.
     *
     * @param destination 仕向け情報
     * @return 本オブジェクト
     * @see #getLastConnectedCarDeviceDestination()
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_DESTINATION
     */
    @NonNull
    public AppSharedPreference setLastConnectedCarDeviceDestination(int destination) {
        mPreferences.edit()
                .putInt(KEY_LAST_CONNECTED_CAR_DEVICE_DESTINATION, destination)
                .apply();
        return this;
    }

    /**
     * 最後に接続した車載機のClassId情報取得.
     *
     * @return ClassId
     * @see #setLastConnectedCarDeviceClassId(CarDeviceClassId)
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_CLASS_ID
     */
    public CarDeviceClassId getLastConnectedCarDeviceClassId() {
        if (mPreferences.contains(KEY_LAST_CONNECTED_CAR_DEVICE_CLASS_ID)) {
            String value =  mPreferences.getString(KEY_LAST_CONNECTED_CAR_DEVICE_CLASS_ID, DEFAULT_LAST_CONNECTED_CAR_DEVICE_CLASS_ID);
            return CarDeviceClassId.valueOf(value);
        } else {
            CarDeviceClassId classId = CarDeviceClassId.valueOf(DEFAULT_LAST_CONNECTED_CAR_DEVICE_CLASS_ID);
            setLastConnectedCarDeviceClassId(classId);
            return classId;
        }
    }

    /**
     * 最後に接続した車載機のClassId情報設定.
     *
     * @param classId ClassId
     * @return 本オブジェクト
     * @see #getLastConnectedCarDeviceClassId()
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_CLASS_ID
     */
    @NonNull
    public AppSharedPreference setLastConnectedCarDeviceClassId(CarDeviceClassId classId) {
        mPreferences.edit()
                .putString(KEY_LAST_CONNECTED_CAR_DEVICE_CLASS_ID, classId.name())
                .apply();
        return this;
    }

    /**
     * 最後に接続した車載機のADAS対応モデル/非対応モデル取得.
     *
     * @return ADAS対応モデル/非対応モデル
     * @see #setLastConnectedCarDeviceAdasAvailable(boolean)
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE
     */
    public boolean getLastConnectedCarDeviceAdasAvailable() {
        if (mPreferences.contains(KEY_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE)) {
            return mPreferences.getBoolean(KEY_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE, DEFAULT_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE);
        } else {
            setLastConnectedCarDeviceAdasAvailable(DEFAULT_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE);
            return DEFAULT_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE;
        }
    }

    /**
     * 最後に接続した車載機のADAS対応モデル/非対応モデル設定.
     *
     * @param isAvailable ADAS対応モデル/非対応モデル
     * @return 本オブジェクト
     * @see #getLastConnectedCarDeviceAdasAvailable()
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE
     */
    @NonNull
    public AppSharedPreference setLastConnectedCarDeviceAdasAvailable(boolean isAvailable) {
        mPreferences.edit()
                .putBoolean(KEY_LAST_CONNECTED_CAR_DEVICE_ADAS_AVAILABLE, isAvailable)
                .apply();
        return this;
    }

    /**
     * 最後に接続した車載機のAM Step取得.
     *
     * @return TunerSeekStep
     * @see #setLastConnectedCarDeviceAmStep(TunerSeekStep)
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_AM_STEP
     */
    public TunerSeekStep getLastConnectedCarDeviceAmStep() {
        if (mPreferences.contains(KEY_LAST_CONNECTED_CAR_DEVICE_AM_STEP)) {
            String value =  mPreferences.getString(KEY_LAST_CONNECTED_CAR_DEVICE_AM_STEP, DEFAULT_LAST_CONNECTED_CAR_DEVICE_AM_STEP);
            return TunerSeekStep.valueOf(value);
        } else {
            TunerSeekStep amStep = TunerSeekStep.valueOf(DEFAULT_LAST_CONNECTED_CAR_DEVICE_AM_STEP);
            setLastConnectedCarDeviceAmStep(amStep);
            return amStep;
        }
    }

    /**
     * 最後に接続した車載機のAM Step設定.
     *
     * @param seekStep TunerSeekStep
     * @return 本オブジェクト
     * @see #getLastConnectedCarDeviceAmStep()
     * @see #KEY_LAST_CONNECTED_CAR_DEVICE_AM_STEP
     */
    @NonNull
    public AppSharedPreference setLastConnectedCarDeviceAmStep(TunerSeekStep seekStep) {
        mPreferences.edit()
                .putString(KEY_LAST_CONNECTED_CAR_DEVICE_AM_STEP, seekStep.name())
                .apply();
        return this;
    }
    /**
     * AppMusicのリピートモード取得.
     *
     * @return SmartPhoneRepeatMode
     * @see #setAppMusicRepeatMode(SmartPhoneRepeatMode)
     * @see #KEY_APP_MUSIC_REPEAT_MODE
     */
    @NonNull
    public SmartPhoneRepeatMode getAppMusicRepeatMode() {
        if (mPreferences.contains(KEY_APP_MUSIC_REPEAT_MODE)) {
            String value = mPreferences.getString(KEY_APP_MUSIC_REPEAT_MODE, DEFAULT_APP_MUSIC_REPEAT_MODE);
            return SmartPhoneRepeatMode.valueOf(value);
        } else {
            SmartPhoneRepeatMode mode = SmartPhoneRepeatMode.valueOf(DEFAULT_APP_MUSIC_REPEAT_MODE);
            setAppMusicRepeatMode(mode);
            return mode;
        }
    }

    /**
     * AppMusicのリピートモード設定.
     *
     * @param mode SmartPhoneRepeatMode
     * @return 本オブジェクト
     * @throws NullPointerException {@code mode}がnull
     * @see #getAppMusicQueryParams()
     * @see #KEY_APP_MUSIC_REPEAT_MODE
     */
    @NonNull
    public AppSharedPreference setAppMusicRepeatMode(@NonNull SmartPhoneRepeatMode mode) {
        checkNotNull(mode);

        mPreferences.edit()
                .putString(KEY_APP_MUSIC_REPEAT_MODE, mode.name())
                .apply();
        return this;
    }

    /**
     * AppMusicのシャッフルモード取得.
     *
     * @return ShuffleMode
     * @see #setAppMusicShuffleMode(ShuffleMode)
     * @see #KEY_APP_MUSIC_SHUFFLE_MODE
     */
    @NonNull
    public ShuffleMode getAppMusicShuffleMode() {
        if (mPreferences.contains(KEY_APP_MUSIC_SHUFFLE_MODE)) {
            String value = mPreferences.getString(KEY_APP_MUSIC_SHUFFLE_MODE, DEFAULT_APP_MUSIC_SHUFFLE_MODE);
            return ShuffleMode.valueOf(value);
        } else {
            ShuffleMode mode = ShuffleMode.valueOf(DEFAULT_APP_MUSIC_SHUFFLE_MODE);
            setAppMusicShuffleMode(mode);
            return mode;
        }
    }

    /**
     * AppMusicのシャッフルモード設定.
     *
     * @param mode ShuffleMode
     * @return 本オブジェクト
     * @throws NullPointerException {@code mode}がnull
     * @see #getAppMusicQueryParams()
     * @see #KEY_APP_MUSIC_SHUFFLE_MODE
     */
    @NonNull
    public AppSharedPreference setAppMusicShuffleMode(@NonNull ShuffleMode mode) {
        checkNotNull(mode);

        mPreferences.edit()
                .putString(KEY_APP_MUSIC_SHUFFLE_MODE, mode.name())
                .apply();
        return this;
    }

    /**
     * AppMusicのクエリーパラメータ取得.
     *
     * @return QueryParams
     * @see #setAppMusicQueryParams(QueryParams)
     * @see #KEY_APP_MUSIC_QUERY_PARAMS
     */
    @NonNull
    public QueryParams getAppMusicQueryParams() {
        if (mPreferences.contains(KEY_APP_MUSIC_QUERY_PARAMS)) {
            String value = mPreferences.getString(KEY_APP_MUSIC_QUERY_PARAMS, DEFAULT_APP_MUSIC_QUERY_PARAMS);
            return sGson.fromJson(value, QueryParams.class);
        } else {
            QueryParams params = sGson.fromJson(DEFAULT_APP_MUSIC_QUERY_PARAMS, QueryParams.class);
            setAppMusicQueryParams(params);
            return params;
        }
    }

    /**
     * AppMusicのクエリーパラメータ設定.
     *
     * @param params QueryParams
     * @return 本オブジェクト
     * @throws NullPointerException {@code params}がnull
     * @see #getAppMusicQueryParams()
     * @see #KEY_APP_MUSIC_QUERY_PARAMS
     */
    @NonNull
    public AppSharedPreference setAppMusicQueryParams(@NonNull QueryParams params) {
        checkNotNull(params);

        mPreferences.edit()
                .putString(KEY_APP_MUSIC_QUERY_PARAMS, sGson.toJson(params))
                .apply();
        return this;
    }

    /**
     * AppMusicのAudio ID取得.
     *
     * @return Audio ID
     * @see #setAppMusicAudioId(long)
     * @see #KEY_APP_MUSIC_AUDIO_ID
     */
    public long getAppMusicAudioId() {
        if (mPreferences.contains(KEY_APP_MUSIC_AUDIO_ID)) {
            return mPreferences.getLong(KEY_APP_MUSIC_AUDIO_ID, DEFAULT_APP_MUSIC_AUDIO_ID);
        } else {
            setAppMusicAudioId(DEFAULT_APP_MUSIC_AUDIO_ID);
            return DEFAULT_APP_MUSIC_AUDIO_ID;
        }
    }

    /**
     * AppMusicのAudio ID設定.
     *
     * @param audioId Audio ID
     * @return 本オブジェクト
     * @see #getAppMusicAudioId()
     * @see #KEY_APP_MUSIC_AUDIO_ID
     */
    @NonNull
    public AppSharedPreference setAppMusicAudioId(long audioId) {
        mPreferences.edit()
                .putLong(KEY_APP_MUSIC_AUDIO_ID, audioId)
                .apply();
        return this;
    }

    /**
     * AppMusicの再生位置取得.
     *
     * @return 再生位置（msec）
     * @see #setAppMusicAudioPlayPosition(int)
     * @see #KEY_APP_MUSIC_PLAY_POSITION
     */
    public int getAppMusicAudioPlayPosition() {
        if (mPreferences.contains(KEY_APP_MUSIC_PLAY_POSITION)) {
            return mPreferences.getInt(KEY_APP_MUSIC_PLAY_POSITION, DEFAULT_APP_MUSIC_PLAY_POSITION);
        } else {
            setAppMusicAudioPlayPosition(DEFAULT_APP_MUSIC_PLAY_POSITION);
            return DEFAULT_APP_MUSIC_PLAY_POSITION;
        }
    }

    /**
     * AppMusicの再生位置設定.
     *
     * @param playPosition 再生位置（msec）
     * @return 本オブジェクト
     * @see #getAppMusicAudioPlayPosition()
     * @see #KEY_APP_MUSIC_PLAY_POSITION
     */
    @NonNull
    public AppSharedPreference setAppMusicAudioPlayPosition(int playPosition) {
        mPreferences.edit()
                .putInt(KEY_APP_MUSIC_PLAY_POSITION, playPosition)
                .apply();
        return this;
    }

    /**
     * 時計種別の取得
     * (0 : digital / 1 : analog)
     *
     * @return type
     * @see #setClockType(int)
     * @see #KEY_CLOCK_TYPE
     */
    public int getClockType() {
        if (mPreferences.contains(KEY_CLOCK_TYPE)) {
            return mPreferences.getInt(KEY_CLOCK_TYPE, DEFAULT_CLOCK_TYPE);
        } else {
            setClockType(DEFAULT_CLOCK_TYPE);
            return DEFAULT_CLOCK_TYPE;
        }
    }

    /**
     * 時計種別の設定
     *
     * @param type 種別
     * @return 本オブジェクト
     * @see #getClockType()
     * @see #KEY_CLOCK_TYPE
     */
    public AppSharedPreference setClockType(int type) {
        mPreferences.edit()
                .putInt(KEY_CLOCK_TYPE, type)
                .apply();
        return this;
    }

    /**
     * 視覚効果取得
     *
     * @return 視覚効果 TODO int or enum ?
     * @see #setVisualEffect(int)
     * @see #KEY_VISUAL_EFFECT
     */
    public int getVisualEffect() {
        if (mPreferences.contains(KEY_VISUAL_EFFECT)) {
            return mPreferences.getInt(KEY_VISUAL_EFFECT, DEFAULT_VISUAL_EFFECT);
        } else {
            setVisualEffect(DEFAULT_VISUAL_EFFECT);
            return DEFAULT_VISUAL_EFFECT;
        }
    }

    /**
     * 視覚効果設定
     *
     * @param effect 視覚効果 TODO int or enum ?
     * @return 本オブジェクト
     * @see #getVisualEffect()
     * @see #KEY_VISUAL_EFFECT
     */
    public AppSharedPreference setVisualEffect(int effect) {
        mPreferences.edit()
                .putInt(KEY_VISUAL_EFFECT, effect)
                .apply();
        return this;
    }

    /**
     * 設定Theme取得
     *
     * @return ThemeType
     * @see #setThemeType(ThemeType)
     * @see #KEY_THEME_TYPE
     */
    public ThemeType getThemeType() {
        if (mPreferences.contains(KEY_THEME_TYPE)) {
            String type = mPreferences.getString(KEY_THEME_TYPE, DEFAULT_THEME_TYPE);
            return ThemeType.valueOf(type);
        } else {
            ThemeType type = ThemeType.valueOf(DEFAULT_THEME_TYPE);
            setThemeType(type);
            return type;
        }
    }

    /**
     * Themeの設定
     *
     * @param type Theme
     * @return 本オブジェクト
     * @see #getThemeType()
     * @see #KEY_THEME_TYPE
     */
    public AppSharedPreference setThemeType(ThemeType type) {
        mPreferences.edit()
                .putString(KEY_THEME_TYPE, type.name())
                .apply();
        return this;
    }

    /**
     * マイフォト有効/無効取得
     *
     * @return ThemeType
     * @see #setThemeMyPhotoEnabled(boolean)
     * @see #KEY_THEME_MY_PHOTO_ENABLED
     */
    public boolean getThemeMyPhotoEnabled() {
        if (mPreferences.contains(KEY_THEME_MY_PHOTO_ENABLED)) {
            return mPreferences.getBoolean(KEY_THEME_MY_PHOTO_ENABLED, DEFAULT_THEME_MY_PHOTO_ENABLED);
        } else {
            setThemeMyPhotoEnabled(DEFAULT_THEME_MY_PHOTO_ENABLED);
            return DEFAULT_THEME_MY_PHOTO_ENABLED;
        }
    }

    /**
     * マイフォト有効/無効の設定
     *
     * @param  enabled
     * @return 本オブジェクト
     * @see #getThemeMyPhotoEnabled()
     * @see #KEY_THEME_MY_PHOTO_ENABLED
     */
    public AppSharedPreference setThemeMyPhotoEnabled(boolean enabled) {
        mPreferences.edit()
                .putBoolean(KEY_THEME_MY_PHOTO_ENABLED, enabled)
                .apply();
        return this;
    }

    /**
     * UIカラー取得
     *
     * @return UiColor UIカラー
     * @see #setUiColor(UiColor)
     * @see #KEY_UI_COLOR
     */
    public UiColor getUiColor() {
        if (mPreferences.contains(KEY_UI_COLOR)) {
            return UiColor.valueOf(mPreferences.getString(KEY_UI_COLOR, DEFAULT_UI_COLOR));
        } else {
            UiColor color = UiColor.valueOf(DEFAULT_UI_COLOR);
            setUiColor(color);
            return color;
        }
    }

    /**
     * UIカラーの設定
     *
     * @param color UIカラー
     * @return 本オブジェクト
     * @see #getUiColor()
     * @see #KEY_UI_COLOR
     */
    public AppSharedPreference setUiColor(UiColor color) {
        mPreferences.edit()
                .putString(KEY_UI_COLOR, color.name())
                .apply();
        return this;
    }

    /**
     * 衝突検知が有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setImpactDetectionEnabled(boolean)
     * @see #KEY_IMPACT_DETECTION_ENABLED
     */
    public boolean isImpactDetectionEnabled() {
        if (mPreferences.contains(KEY_IMPACT_DETECTION_ENABLED)) {
            return mPreferences.getBoolean(KEY_IMPACT_DETECTION_ENABLED, DEFAULT_IMPACT_DETECTION_ENABLED);
        } else {
            setImpactDetectionEnabled(DEFAULT_IMPACT_DETECTION_ENABLED);
            return DEFAULT_IMPACT_DETECTION_ENABLED;
        }
    }

    /**
     * 衝突検知有効設定.
     *
     * @param enabled {@code true}:有効。{@code false}:無効。
     * @return 本オブジェクト
     * @see #isImpactDetectionEnabled()
     * @see #KEY_IMPACT_DETECTION_ENABLED
     */
    @NonNull
    public AppSharedPreference setImpactDetectionEnabled(boolean enabled) {
        mPreferences.edit()
                .putBoolean(KEY_IMPACT_DETECTION_ENABLED, enabled)
                .apply();
        return this;
    }

    /**
     * 衝突検知通知方法取得.
     *
     * @return ImpactNotificationMethod
     * @see #setImpactNotificationMethod(ImpactNotificationMethod)
     * @see #KEY_IMPACT_NOTIFICATION_METHOD
     */
    @NonNull
    public ImpactNotificationMethod getImpactNotificationMethod() {
        if (mPreferences.contains(KEY_IMPACT_NOTIFICATION_METHOD)) {
            String value = mPreferences.getString(KEY_IMPACT_NOTIFICATION_METHOD, DEFAULT_IMPACT_NOTIFICATION_METHOD);
            return ImpactNotificationMethod.valueOf(value);
        } else {
            ImpactNotificationMethod mode = ImpactNotificationMethod.valueOf(DEFAULT_IMPACT_NOTIFICATION_METHOD);
            setImpactNotificationMethod(mode);
            return mode;
        }
    }

    /**
     * 衝突検知通知方法設定.
     *
     * @param mode ImpactNotificationMethod
     * @return 本オブジェクト
     * @throws NullPointerException {@code mode}がnull
     * @see #getImpactNotificationMethod()
     * @see #KEY_IMPACT_NOTIFICATION_METHOD
     */
    public AppSharedPreference setImpactNotificationMethod(@NonNull ImpactNotificationMethod mode) {
        checkNotNull(mode);

        mPreferences.edit()
                .putString(KEY_IMPACT_NOTIFICATION_METHOD, mode.name())
                .apply();
        return this;
    }

    /**
     * 衝突検知通知先（連絡者）取得.
     *
     * @return 衝突検知通知先（連絡者）
     * @see #setImpactNotificationContactLookupKey(String)
     * @see #KEY_IMPACT_NOTIFICATION_CONTACT_LOOKUP
     */
    @NonNull
    public String getImpactNotificationContactLookupKey() {
        if (mPreferences.contains(KEY_IMPACT_NOTIFICATION_CONTACT_LOOKUP)) {
            return mPreferences.getString(KEY_IMPACT_NOTIFICATION_CONTACT_LOOKUP, DEFAULT_IMPACT_NOTIFICATION_CONTACT_LOOKUP);
        } else {
            setImpactNotificationContactLookupKey(DEFAULT_IMPACT_NOTIFICATION_CONTACT_LOOKUP);
            return DEFAULT_IMPACT_NOTIFICATION_CONTACT_LOOKUP;
        }
    }

    /**
     * 衝突検知通知先（連絡者）設定.
     *
     * @param lookupKey 衝突検知通知先（連絡者）
     * @return 本オブジェクト
     * @throws NullPointerException {@code lookupKey}がnull
     * @see #getImpactNotificationContactLookupKey()
     * @see #KEY_IMPACT_NOTIFICATION_CONTACT_LOOKUP
     */
    public AppSharedPreference setImpactNotificationContactLookupKey(@NonNull String lookupKey) {
        checkNotNull(lookupKey);

        mPreferences.edit()
                .putString(KEY_IMPACT_NOTIFICATION_CONTACT_LOOKUP, lookupKey)
                .apply();
        return this;
    }

    /**
     * 衝突検知通知先設定（連絡者）削除.
     *
     * @return 本オブジェクト
     * @see #KEY_IMPACT_NOTIFICATION_CONTACT_LOOKUP
     */
    public AppSharedPreference removeImpactNotificationContactLookupKey() {
        mPreferences.edit()
                .remove(KEY_IMPACT_NOTIFICATION_CONTACT_LOOKUP)
                .apply();
        return this;
    }

    /**
     * 衝突検知通知先（電話番号）取得.
     *
     * @return 衝突検知通知先（電話番号）
     * @see #setImpactNotificationContactNumber(String)
     * @see #DEFAULT_IMPACT_NOTIFICATION_CONTACT_NUMBER
     */
    public String getImpactNotificationContactNumber() {
        if (mPreferences.contains(KEY_IMPACT_NOTIFICATION_CONTACT_NUMBER)) {
            return mPreferences.getString(KEY_IMPACT_NOTIFICATION_CONTACT_NUMBER, DEFAULT_IMPACT_NOTIFICATION_CONTACT_NUMBER);
        } else {
            setImpactNotificationContactNumber(DEFAULT_IMPACT_NOTIFICATION_CONTACT_NUMBER);
            return DEFAULT_IMPACT_NOTIFICATION_CONTACT_NUMBER;
        }
    }

    /**
     * 衝突検知通知先（電話番号）設定.
     *
     * @param number 衝突検知通知先（電話番号）
     * @return 本オブジェクト
     * @see #getImpactNotificationContactNumber()
     * @see #KEY_IMPACT_NOTIFICATION_CONTACT_NUMBER
     */
    public AppSharedPreference setImpactNotificationContactNumber(String number) {
        mPreferences.edit()
                .putString(KEY_IMPACT_NOTIFICATION_CONTACT_NUMBER, number)
                .apply();
        return this;
    }

    /**
     * 衝突検知通知先（電話番号）削除.
     *
     * @return 本オブジェクト
     * @see #KEY_IMPACT_NOTIFICATION_CONTACT_NUMBER
     */
    public AppSharedPreference removeImpactNotificationContactNumber() {
        mPreferences.edit()
                .remove(KEY_IMPACT_NOTIFICATION_CONTACT_NUMBER)
                .apply();
        return this;
    }

    /**
     * 衝突検知デバッグモードが有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setImpactDetectionDebugModeEnabled(boolean)
     * @see #KEY_IMPACT_DETECTION_DEBUG_MODE_ENABLED
     */
    public boolean isImpactDetectionDebugModeEnabled() {
        if (mPreferences.contains(KEY_IMPACT_DETECTION_DEBUG_MODE_ENABLED)) {
            return mPreferences.getBoolean(KEY_IMPACT_DETECTION_DEBUG_MODE_ENABLED, DEFAULT_IMPACT_DETECTION_DEBUG_MODE_ENABLED);
        } else {
            setImpactDetectionDebugModeEnabled(DEFAULT_IMPACT_DETECTION_DEBUG_MODE_ENABLED);
            return DEFAULT_IMPACT_DETECTION_DEBUG_MODE_ENABLED;
        }
    }

    /**
     * 衝突検知デバッグモード有効設定.
     *
     * @param enabled {@code true}:有効。{@code false}:無効。
     * @return 本オブジェクト
     * @see #isImpactDetectionDebugModeEnabled()
     * @see #KEY_IMPACT_DETECTION_DEBUG_MODE_ENABLED
     */
    @NonNull
    public AppSharedPreference setImpactDetectionDebugModeEnabled(boolean enabled) {
        mPreferences.edit()
                .putBoolean(KEY_IMPACT_DETECTION_DEBUG_MODE_ENABLED, enabled)
                .apply();
        return this;
    }

    /**
     * 通知読み上げが有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setReadNotificationEnabled(boolean)
     * @see #KEY_READ_NOTIFICATION_ENABLED
     */
    public boolean isReadNotificationEnabled() {
        if (mPreferences.contains(KEY_READ_NOTIFICATION_ENABLED)) {
            return mPreferences.getBoolean(KEY_READ_NOTIFICATION_ENABLED, DEFAULT_READ_NOTIFICATION_ENABLED);
        } else {
            setReadNotificationEnabled(DEFAULT_READ_NOTIFICATION_ENABLED);
            return DEFAULT_READ_NOTIFICATION_ENABLED;
        }
    }

    /**
     * 通知読み上げ有効設定.
     *
     * @param enabled {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isReadNotificationEnabled()
     * @see #KEY_READ_NOTIFICATION_ENABLED
     */
    public AppSharedPreference setReadNotificationEnabled(boolean enabled) {
        mPreferences.edit()
                .putBoolean(KEY_READ_NOTIFICATION_ENABLED, enabled)
                .apply();
        return this;
    }

    /**
     * 通知読み上げアプリ取得.
     *
     * @return Application[]
     * @see #setReadNotificationApps(Application[])
     * @see #KEY_READ_NOTIFICATION_APPS
     */
    @NonNull
    public Application[] getReadNotificationApps() {
        if (mPreferences.contains(KEY_READ_NOTIFICATION_APPS)) {
            String value = mPreferences.getString(KEY_READ_NOTIFICATION_APPS, DEFAULT_READ_NOTIFICATION_APPS);
            return new Gson().fromJson(value, Application[].class);
        } else {
            Application[] messagingApps = new Gson().fromJson(DEFAULT_READ_NOTIFICATION_APPS, Application[].class);
            setReadNotificationApps(messagingApps);
            return messagingApps;
        }
    }

    /**
     * 通知読み上げアプリ設定.
     *
     * @param messagingApps Application[]
     * @return 本オブジェクト
     * @throws NullPointerException {@code messagingApps}がnull
     * @see #getReadNotificationApps()
     * @see #KEY_READ_NOTIFICATION_APPS
     */
    public AppSharedPreference setReadNotificationApps(@NonNull Application[] messagingApps) {
        checkNotNull(messagingApps);

        String value = new Gson().toJson(messagingApps);
        mPreferences.edit()
                .putString(KEY_READ_NOTIFICATION_APPS, value)
                .apply();
        return this;
    }

    /**
     * ナビアプリ取得.
     *
     * @return Application
     * @see #setNavigationApp(Application)
     * @see #KEY_NAVIGATION_APP
     */
    @NonNull
    public Application getNavigationApp() {
        if (mPreferences.contains(KEY_NAVIGATION_APP)) {
            String value = mPreferences.getString(KEY_NAVIGATION_APP, DEFAULT_NAVIGATION_APP);
            return new Gson().fromJson(value, Application.class);
        } else {
            Application naviApp = new Gson().fromJson(DEFAULT_NAVIGATION_APP, Application.class);
            setNavigationApp(naviApp);
            return naviApp;
        }
    }

    /**
     * ナビアプリ設定.
     *
     * @param app Application
     * @return 本オブジェクト
     * @throws NullPointerException {@code app}がnull
     * @see #getNavigationApp()
     * @see #KEY_NAVIGATION_APP
     */
    public AppSharedPreference setNavigationApp(@NonNull Application app) {
        checkNotNull(app);

        String value = new Gson().toJson(app);
        mPreferences.edit()
                .putString(KEY_NAVIGATION_APP, value)
                .apply();
        return this;
    }
    /**
     * Marin用ナビアプリ取得.
     *
     * @return Application
     * @see #setNavigationMarinApp(Application)
     * @see #KEY_NAVIGATION_APP
     */
    @NonNull
    public Application getNavigationMarinApp() {
        if (mPreferences.contains(KEY_NAVIGATION_MARIN_APP)) {
            String value = mPreferences.getString(KEY_NAVIGATION_MARIN_APP, DEFAULT_NAVIGATION_MARIN_APP);
            return new Gson().fromJson(value, Application.class);
        } else {
            Application naviApp = new Gson().fromJson(DEFAULT_NAVIGATION_MARIN_APP, Application.class);
            setNavigationMarinApp(naviApp);
            return naviApp;
        }
    }

    /**
     * Marin用ナビアプリ設定.
     *
     * @param app Application
     * @return 本オブジェクト
     * @throws NullPointerException {@code app}がnull
     * @see #getNavigationMarinApp()
     * @see #KEY_NAVIGATION_MARIN_APP
     */
    public AppSharedPreference setNavigationMarinApp(@NonNull Application app) {
        checkNotNull(app);

        String value = new Gson().toJson(app);
        mPreferences.edit()
                .putString(KEY_NAVIGATION_MARIN_APP, value)
                .apply();
        return this;
    }
    /**
     * Phone設定連絡先（連絡者）取得.
     *
     * @return Phone設定連絡先（連絡者）
     * @see #setDirectCallContactLookupKey(String)
     * @see #KEY_DIRECT_CALL_CONTACT_LOOKUP
     */
    @NonNull
    public String getDirectCallContactLookupKey() {
        if (mPreferences.contains(KEY_DIRECT_CALL_CONTACT_LOOKUP)) {
            return mPreferences.getString(KEY_DIRECT_CALL_CONTACT_LOOKUP, DEFAULT_DIRECT_CALL_CONTACT_LOOKUP);
        } else {
            setDirectCallContactLookupKey(DEFAULT_DIRECT_CALL_CONTACT_LOOKUP);
            return DEFAULT_DIRECT_CALL_CONTACT_LOOKUP;
        }
    }

    /**
     * Phone設定連絡先（連絡者）設定.
     *
     * @param lookupKey Phone設定連絡先（連絡者）
     * @return 本オブジェクト
     * @throws NullPointerException {@code lookupKey}がnull
     * @see #getDirectCallContactLookupKey()
     * @see #KEY_DIRECT_CALL_CONTACT_LOOKUP
     */
    public AppSharedPreference setDirectCallContactLookupKey(@NonNull String lookupKey) {
        checkNotNull(lookupKey);

        mPreferences.edit()
                .putString(KEY_DIRECT_CALL_CONTACT_LOOKUP, lookupKey)
                .apply();
        return this;
    }

    /**
     * Phone連絡先設定（連絡者）削除.
     *
     * @return 本オブジェクト
     * @see #KEY_DIRECT_CALL_CONTACT_LOOKUP
     */
    public AppSharedPreference removeDirectCallContactLookupKey() {
        mPreferences.edit()
                .remove(KEY_DIRECT_CALL_CONTACT_LOOKUP)
                .apply();
        return this;
    }

    /**
     * Phone設定連絡先（電話番号ID）取得.
     *
     * @return Phone設定連絡先（電話番号ID）
     * @see #setDirectCallContactNumberId(long)
     * @see #DEFAULT_DIRECT_CALL_CONTACT_NUMBER_ID
     */
    public long getDirectCallContactNumberId() {
        if (mPreferences.contains(KEY_DIRECT_CALL_CONTACT_NUMBER_ID)) {
            return mPreferences.getLong(KEY_DIRECT_CALL_CONTACT_NUMBER_ID, DEFAULT_DIRECT_CALL_CONTACT_NUMBER_ID);
        } else {
            setDirectCallContactNumberId(DEFAULT_DIRECT_CALL_CONTACT_NUMBER_ID);
            return DEFAULT_DIRECT_CALL_CONTACT_NUMBER_ID;
        }
    }

    /**
     * Phone設定連絡先（電話番号ID）設定.
     *
     * @param id Phone設定連絡先（電話番号ID）
     * @return 本オブジェクト
     * @see #getDirectCallContactNumberId()
     * @see #KEY_DIRECT_CALL_CONTACT_NUMBER_ID
     */
    public AppSharedPreference setDirectCallContactNumberId(long id) {
        mPreferences.edit()
                .putLong(KEY_DIRECT_CALL_CONTACT_NUMBER_ID, id)
                .apply();
        return this;
    }

    /**
     * Phone設定連絡先（電話番号ID）削除.
     *
     * @return 本オブジェクト
     * @see #KEY_DIRECT_CALL_CONTACT_NUMBER_ID
     */
    public AppSharedPreference removeDirectCallContactNumberId() {
        mPreferences.edit()
                .remove(KEY_DIRECT_CALL_CONTACT_NUMBER_ID)
                .apply();
        return this;
    }

    /**
     * アルバムアート表記が有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setAlbumArtEnabled(boolean)
     * @see #KEY_ALBUM_ART_ENABLED
     */
    public boolean isAlbumArtEnabled() {
        if (mPreferences.contains(KEY_ALBUM_ART_ENABLED)) {
            return mPreferences.getBoolean(KEY_ALBUM_ART_ENABLED, DEFAULT_ALBUM_ART_ENABLED);
        } else {
            setAlbumArtEnabled(DEFAULT_ALBUM_ART_ENABLED);
            return DEFAULT_ALBUM_ART_ENABLED;
        }
    }

    /**
     * アルバムアート表記有効設定.
     *
     * @param isAlbumArt {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isAlbumArtEnabled()
     * @see #KEY_ALBUM_ART_ENABLED
     */
    public AppSharedPreference setAlbumArtEnabled(boolean isAlbumArt) {
        mPreferences.edit()
                .putBoolean(KEY_ALBUM_ART_ENABLED, isAlbumArt)
                .apply();
        return this;
    }

    /**
     * プレイリストがカード表記が有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setGenreCardEnabled(boolean)
     * @see #KEY_PLAYLIST_CARD_ENABLED
     */
    public boolean isPlaylistCardEnabled() {
        if (mPreferences.contains(KEY_PLAYLIST_CARD_ENABLED)) {
            return mPreferences.getBoolean(KEY_PLAYLIST_CARD_ENABLED, DEFAULT_PLAYLIST_CARD_ENABLED);
        } else {
            setPlayListViewId(DEFAULT_PLAYLIST_CARD_ENABLED);
            return DEFAULT_PLAYLIST_CARD_ENABLED;
        }
    }

    /**
     * プレイリストのカード表記有効設定.
     *
     * @param isPlaylistCard {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isPlaylistCardEnabled()
     * @see #KEY_PLAYLIST_CARD_ENABLED
     */
    public AppSharedPreference setPlayListViewId(boolean isPlaylistCard) {
        mPreferences.edit()
                .putBoolean(KEY_PLAYLIST_CARD_ENABLED, isPlaylistCard)
                .apply();
        return this;
    }

    /**
     * ジャンルリストがカード表記が有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setGenreCardEnabled(boolean)
     * @see #KEY_GENRE_CARD_ENABLED
     */
    public boolean isGenreCardEnabled() {
        if (mPreferences.contains(KEY_GENRE_CARD_ENABLED)) {
            return mPreferences.getBoolean(KEY_GENRE_CARD_ENABLED, DEFAULT_GENRE_CARD_ENABLED);
        } else {
            setGenreCardEnabled(DEFAULT_GENRE_CARD_ENABLED);
            return DEFAULT_GENRE_CARD_ENABLED;
        }
    }

    /**
     * ジャンルリストのカード表記有効設定.
     *
     * @param isGenreCard {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isGenreCardEnabled()
     * @see #KEY_GENRE_CARD_ENABLED
     */
    public AppSharedPreference setGenreCardEnabled(boolean isGenreCard) {
        mPreferences.edit()
                .putBoolean(KEY_GENRE_CARD_ENABLED, isGenreCard)
                .apply();
        return this;
    }

    /**
     * SourceLevelAdjusterの位置取得.
     *
     * @return 設定位置
     * @see #setSourceLevelAdjusterPosition(int)
     * @see #KEY_SOURCE_LEVEL_ADJUSTER_POSITION
     */
    public int getSourceLevelAdjusterPosition() {
        if (mPreferences.contains(KEY_SOURCE_LEVEL_ADJUSTER_POSITION)) {
            return mPreferences.getInt(KEY_SOURCE_LEVEL_ADJUSTER_POSITION, DEFAULT_SOURCE_LEVEL_ADJUSTER_POSITION);
        } else {
            setAppMusicAudioPlayPosition(DEFAULT_SOURCE_LEVEL_ADJUSTER_POSITION);
            return DEFAULT_SOURCE_LEVEL_ADJUSTER_POSITION;
        }
    }

    /**
     * SourceLevelAdjusterの位置設定.
     *
     * @param position 設定位置
     * @return 本オブジェクト
     * @see #getSourceLevelAdjusterPosition()
     * @see #KEY_SOURCE_LEVEL_ADJUSTER_POSITION
     */
    @NonNull
    public AppSharedPreference setSourceLevelAdjusterPosition(int position) {
        mPreferences.edit()
                .putInt(KEY_SOURCE_LEVEL_ADJUSTER_POSITION, position)
                .apply();
        return this;
    }

    /**
     * ショートカットボタン表示が有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setShortCutButtonEnabled(boolean)
     * @see #KEY_SHORT_CUT_BUTTON_ENABLED
     */
    public boolean isShortCutButtonEnabled() {
        if (mPreferences.contains(KEY_SHORT_CUT_BUTTON_ENABLED)) {
            return mPreferences.getBoolean(KEY_SHORT_CUT_BUTTON_ENABLED, DEFAULT_SHORT_CUT_BUTTON_ENABLED);
        } else {
            setShortCutButtonEnabled(DEFAULT_SHORT_CUT_BUTTON_ENABLED);
            return DEFAULT_SHORT_CUT_BUTTON_ENABLED;
        }
    }

    /**
     * ショートカットボタンの表示有効設定.
     *
     * @param isShortCut {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isShortCutButtonEnabled()
     * @see #KEY_SHORT_CUT_BUTTON_ENABLED
     */
    public AppSharedPreference setShortCutButtonEnabled(boolean isShortCut) {
        mPreferences.edit()
                .putBoolean(KEY_SHORT_CUT_BUTTON_ENABLED, isShortCut)
                .apply();
        return this;
    }

    /**
     * ショートカットボタン表示が設定済か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setConfiguredShortCutButtonEnabled(boolean)
     * @see #KEY_CONFIGURED_SHORT_CUT_BUTTON_ENABLED
     */
    public boolean isConfiguredShortCutButtonEnabled() {
        if (mPreferences.contains(KEY_CONFIGURED_SHORT_CUT_BUTTON_ENABLED)) {
            return mPreferences.getBoolean(KEY_CONFIGURED_SHORT_CUT_BUTTON_ENABLED, DEFAULT_CONFIGURED_SHORT_CUT_BUTTON_ENABLED);
        } else {
            setShortCutButtonEnabled(DEFAULT_CONFIGURED_SHORT_CUT_BUTTON_ENABLED);
            return DEFAULT_CONFIGURED_SHORT_CUT_BUTTON_ENABLED;
        }
    }

    /**
     * ショートカットボタン表示設定済の設定.
     *
     * @param isConfigured {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isConfiguredShortCutButtonEnabled()
     * @see #KEY_CONFIGURED_SHORT_CUT_BUTTON_ENABLED
     */
    public AppSharedPreference setConfiguredShortCutButtonEnabled(boolean isConfigured) {
        mPreferences.edit()
                .putBoolean(KEY_CONFIGURED_SHORT_CUT_BUTTON_ENABLED, isConfigured)
                .apply();
        return this;
    }

    /**
     * カスタムバンド設定A取得.
     *
     * @return CustomBandSetting
     * @see #setCustomBandSettingA(CustomBandSetting)
     * @see #KEY_SOUND_FX_CUSTOM_BAND_SETTING_A
     */
    public CustomBandSetting getCustomBandSettingA() {
        if (mPreferences.contains(KEY_SOUND_FX_CUSTOM_BAND_SETTING_A)) {
            String value = mPreferences.getString(KEY_SOUND_FX_CUSTOM_BAND_SETTING_A, DEFAULT_SOUND_FX_CUSTOM_BAND_SETTING_A);
            return new Gson().fromJson(value, CustomBandSetting.class);
        } else {
            CustomBandSetting setting = new Gson().fromJson(DEFAULT_SOUND_FX_CUSTOM_BAND_SETTING_A, CustomBandSetting.class);
            setCustomBandSettingA(setting);
            return setting;
        }
    }

    /**
     * カスタムバンド設定A設定.
     *
     * @param setting CustomBandSetting
     * @return 本オブジェクト
     * @see #getCustomBandSettingA()
     * @see #KEY_SOUND_FX_CUSTOM_BAND_SETTING_A
     */
    public AppSharedPreference setCustomBandSettingA(@NonNull CustomBandSetting setting) {
        checkNotNull(setting);

        String value = new Gson().toJson(setting);
        mPreferences.edit()
                .putString(KEY_SOUND_FX_CUSTOM_BAND_SETTING_A, value)
                .apply();
        return this;
    }

    /**
     * カスタムバンド設定B取得.
     *
     * @return CustomBandSetting
     * @see #setCustomBandSettingA(CustomBandSetting)
     * @see #KEY_SOUND_FX_CUSTOM_BAND_SETTING_B
     */
    public CustomBandSetting getCustomBandSettingB() {
        if (mPreferences.contains(KEY_SOUND_FX_CUSTOM_BAND_SETTING_B)) {
            String value = mPreferences.getString(KEY_SOUND_FX_CUSTOM_BAND_SETTING_B, DEFAULT_SOUND_FX_CUSTOM_BAND_SETTING_B);
            return new Gson().fromJson(value, CustomBandSetting.class);
        } else {
            CustomBandSetting setting = new Gson().fromJson(DEFAULT_SOUND_FX_CUSTOM_BAND_SETTING_B, CustomBandSetting.class);
            setCustomBandSettingB(setting);
            return setting;
        }
    }

    /**
     * カスタムバンド設定A設定.
     *
     * @param setting CustomBandSetting
     * @return 本オブジェクト
     * @see #getCustomBandSettingB()
     * @see #KEY_SOUND_FX_CUSTOM_BAND_SETTING_B
     */
    public AppSharedPreference setCustomBandSettingB(@NonNull CustomBandSetting setting) {
        checkNotNull(setting);

        String value = new Gson().toJson(setting);
        mPreferences.edit()
                .putString(KEY_SOUND_FX_CUSTOM_BAND_SETTING_B, value)
                .apply();
        return this;
    }

    /**
     * デバッグ用SpecialEQが有効か否か.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setDebugSpecialEqEnabled(boolean)
     * @see #KEY_DEBUG_SPECIAL_EQ_ENABLED
     */
    public boolean isDebugSpecialEqEnabled() {
        if (mPreferences.contains(KEY_DEBUG_SPECIAL_EQ_ENABLED)) {
            return mPreferences.getBoolean(KEY_DEBUG_SPECIAL_EQ_ENABLED, DEFAULT_DEBUG_SPECIAL_EQ_ENABLED);
        } else {
            setDebugSpecialEqEnabled(DEFAULT_DEBUG_SPECIAL_EQ_ENABLED);
            return DEFAULT_DEBUG_SPECIAL_EQ_ENABLED;
        }
    }

    /**
     * デバッグ用SpecialEQの有効設定.
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isDebugSpecialEqEnabled()
     * @see #KEY_DEBUG_SPECIAL_EQ_ENABLED
     */
    public AppSharedPreference setDebugSpecialEqEnabled(boolean isEnabled) {
        mPreferences.edit()
                .putBoolean(KEY_DEBUG_SPECIAL_EQ_ENABLED, isEnabled)
                .apply();
        return this;
    }

    /**
     * 音声認識機能が有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setVoiceRecognitionEnabled(boolean)
     * @see #KEY_VOICE_RECOGNITION_ENABLED
     */
    public boolean isVoiceRecognitionEnabled() {
        if (mPreferences.contains(KEY_VOICE_RECOGNITION_ENABLED)) {
            return mPreferences.getBoolean(KEY_VOICE_RECOGNITION_ENABLED, DEFAULT_VOICE_RECOGNITION_ENABLED);
        } else {
            setVoiceRecognitionEnabled(DEFAULT_VOICE_RECOGNITION_ENABLED);
            return DEFAULT_VOICE_RECOGNITION_ENABLED;
        }
    }

    /**
     * 音声認識機能の有効設定.
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isVoiceRecognitionEnabled()
     * @see #KEY_VOICE_RECOGNITION_ENABLED
     */
    public AppSharedPreference setVoiceRecognitionEnabled(boolean isEnabled) {
        mPreferences.edit()
                .putBoolean(KEY_VOICE_RECOGNITION_ENABLED, isEnabled)
                .apply();
        return this;
    }

    /**
     * 音声認識機能種別取得.
     *
     * @return VoiceRecognizeType
     * @see #setVoiceRecognitionType(VoiceRecognizeType)
     * @see #KEY_VOICE_RECOGNITION_TYPE
     */
    @NonNull
    public VoiceRecognizeType getVoiceRecognitionType() {
        if(!BuildConfig.DEBUG)return VoiceRecognizeType.PIONEER_SMART_SYNC;
        if (mPreferences.contains(KEY_VOICE_RECOGNITION_TYPE)) {
            String value = mPreferences.getString(KEY_VOICE_RECOGNITION_TYPE, DEFAULT_VOICE_RECOGNITION_TYPE);
            return VoiceRecognizeType.valueOf(value);
        } else {
            VoiceRecognizeType type = VoiceRecognizeType.valueOf(DEFAULT_VOICE_RECOGNITION_TYPE);
            setVoiceRecognitionType(type);
            return type;
        }
    }

    /**
     * 音声認識機能の種別設定.
     *
     * @param type VoiceRecognizeType
     * @return 本オブジェクト
     * @see #getVoiceRecognitionType()
     * @see #KEY_VOICE_RECOGNITION_TYPE
     */
    @NonNull
    public AppSharedPreference setVoiceRecognitionType(@NonNull VoiceRecognizeType type) {
        checkNotNull(type);
        mPreferences.edit()
                .putString(KEY_VOICE_RECOGNITION_TYPE, type.name())
                .apply();
        return this;
    }
    /**
     * 音声認識機能マイク種別取得.
     *
     * @return VoiceRecognizeMicType
     * @see #setVoiceRecognitionMicType(VoiceRecognizeMicType)
     * @see #KEY_VOICE_RECOGNITION_MIC_TYPE
     */
    @NonNull
    public VoiceRecognizeMicType getVoiceRecognitionMicType() {
        if (mPreferences.contains(KEY_VOICE_RECOGNITION_MIC_TYPE)) {
            String value = mPreferences.getString(KEY_VOICE_RECOGNITION_MIC_TYPE, DEFAULT_VOICE_RECOGNITION_MIC_TYPE);
            return VoiceRecognizeMicType.valueOf(value);
        } else {
            VoiceRecognizeMicType type = VoiceRecognizeMicType.valueOf(DEFAULT_VOICE_RECOGNITION_MIC_TYPE);
            setVoiceRecognitionMicType(type);
            return type;
        }
    }

    /**
     * 音声認識機能のマイク種別設定.
     *
     * @param type VoiceRecognizeMicType
     * @return 本オブジェクト
     * @see #getVoiceRecognitionMicType()
     * @see #KEY_VOICE_RECOGNITION_MIC_TYPE
     */
    @NonNull
    public AppSharedPreference setVoiceRecognitionMicType(@NonNull VoiceRecognizeMicType type) {
        checkNotNull(type);
        mPreferences.edit()
                .putString(KEY_VOICE_RECOGNITION_MIC_TYPE, type.name())
                .apply();
        return this;
    }
    /**
     * 連絡帳アクセスが有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setPhoneBookAccessible(boolean)
     * @see #KEY_PHONE_BOOK_ACCESSIBLE
     */
    public boolean isPhoneBookAccessible() {
        if (mPreferences.contains(KEY_PHONE_BOOK_ACCESSIBLE)) {
            return mPreferences.getBoolean(KEY_PHONE_BOOK_ACCESSIBLE, DEFAULT_PHONE_BOOK_ACCESSIBLE);
        } else {
            setPhoneBookAccessible(DEFAULT_PHONE_BOOK_ACCESSIBLE);
            return DEFAULT_PHONE_BOOK_ACCESSIBLE;
        }
    }

    /**
     * 連絡帳アクセスの有効設定.
     *
     * @param isAccessible {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isPhoneBookAccessible()
     * @see #KEY_PHONE_BOOK_ACCESSIBLE
     */
    public AppSharedPreference setPhoneBookAccessible(boolean isAccessible) {
        mPreferences.edit()
                .putBoolean(KEY_PHONE_BOOK_ACCESSIBLE, isAccessible)
                .apply();
        return this;
    }

    /**
     * 音楽アプリ取得.
     *
     * @return Application[]
     * @see #setMusicApps(Application[])
     * @see #KEY_MUSIC_APPS
     */
    @NonNull
    public Application[] getMusicApps() {
        if (mPreferences.contains(KEY_MUSIC_APPS)) {
            String value = mPreferences.getString(KEY_MUSIC_APPS, DEFAULT_MUSIC_APPS);
            return new Gson().fromJson(value, Application[].class);
        } else {
            Application[] musicApps = new Gson().fromJson(DEFAULT_MUSIC_APPS, Application[].class);
            setMusicApps(musicApps);
            return musicApps;
        }
    }

    /**
     * 音楽アプリ設定.
     *
     * @param musicApps Application[]
     * @return 本オブジェクト
     * @throws NullPointerException {@code musicApps}がnull
     * @see #getMusicApps()
     * @see #KEY_MUSIC_APPS
     */
    public AppSharedPreference setMusicApps(@NonNull Application[] musicApps) {
        checkNotNull(musicApps);

        String value = new Gson().toJson(musicApps);
        mPreferences.edit()
                .putString(KEY_MUSIC_APPS, value)
                .apply();
        return this;
    }

    /**
     * ADASが課金済か否か取得.
     *
     * @return {@code true}:課金済　{@code false}:未課金
     * @see #setAdasBillingRecord(boolean)
     * @see #KEY_ADAS_BILLING_RECORD
     */
    public boolean isAdasBillingRecord() {
        if (mPreferences.contains(KEY_ADAS_BILLING_RECORD)) {
            return mPreferences.getBoolean(KEY_ADAS_BILLING_RECORD, DEFAULT_ADAS_BILLING_RECORD);
        } else {
            setAdasBillingRecord(DEFAULT_ADAS_BILLING_RECORD);
            return DEFAULT_ADAS_BILLING_RECORD;
        }
    }

    /**
     * ADASの課金記録設定.
     *
     * @param isRecord {@code true}:課金済　{@code false}:未課金
     * @return 本オブジェクト
     * @see #isAdasBillingRecord()
     * @see #KEY_ADAS_BILLING_RECORD
     */
    public AppSharedPreference setAdasBillingRecord(boolean isRecord) {
        mPreferences.edit()
                .putBoolean(KEY_ADAS_BILLING_RECORD, isRecord)
                .apply();
        return this;
    }

    /**
     * ADASのお試し実施状態取得.
     *
     * @return AdasTrialState
     * @see #setAdasTrialState(AdasTrialState)
     * @see #KEY_ADAS_TRIAL_STATE
     */
    public AdasTrialState getAdasTrialState() {
    if (mPreferences.contains(KEY_ADAS_TRIAL_STATE)) {
            return AdasTrialState.valueOf(mPreferences.getString(KEY_ADAS_TRIAL_STATE, DEFAULT_ADAS_TRIAL_STATE));
        } else {
            AdasTrialState state = AdasTrialState.valueOf(DEFAULT_ADAS_TRIAL_STATE);
            setAdasTrialState(state);
            return state;
        }
    }

    /**
     * ADASのお試し実施状態設定.
     *
     * @param state AdasTrialState
     * @return 本オブジェクト
     * @see #getAdasTrialState()
     * @see #KEY_ADAS_TRIAL_STATE
     */
    public AppSharedPreference setAdasTrialState(AdasTrialState state) {
        mPreferences.edit()
                .putString(KEY_ADAS_TRIAL_STATE, state.name())
                .apply();
        return this;
    }

    /**
     * ADASのお試し期間終了日時取得.
     *
     * @return long 日時
     * @see #setAdasTrialPeriodEndDate(long)
     * @see #KEY_ADAS_TRIAL_PERIOD_END_DATE
     */
    public long getAdasTrialPeriodEndDate() {
        if (mPreferences.contains(KEY_ADAS_TRIAL_PERIOD_END_DATE)) {
            return mPreferences.getLong(KEY_ADAS_TRIAL_PERIOD_END_DATE, DEFAULT_ADAS_TRIAL_PERIOD_END);
        } else {
            setAdasTrialPeriodEndDate(DEFAULT_ADAS_TRIAL_PERIOD_END);
            return DEFAULT_ADAS_TRIAL_PERIOD_END;
        }
    }

    /**
     * ADASのお試し実施状態設定.
     *
     * @param date 日時
     * @return 本オブジェクト
     * @see #getAdasTrialPeriodEndDate()
     * @see #KEY_ADAS_TRIAL_PERIOD_END_DATE
     */
    public AppSharedPreference setAdasTrialPeriodEndDate(long date) {
        mPreferences.edit()
                .putLong(KEY_ADAS_TRIAL_PERIOD_END_DATE, date)
                .apply();
        return this;
    }

    /**
     * ADASが有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setAdasEnabled(boolean)
     * @see #KEY_ADAS_ENABLED
     */
    public boolean isAdasEnabled() {
        if (mPreferences.contains(KEY_ADAS_ENABLED)) {
            return mPreferences.getBoolean(KEY_ADAS_ENABLED, DEFAULT_ADAS_ENABLED);
        } else {
            setAdasEnabled(DEFAULT_ADAS_ENABLED);
            return DEFAULT_ADAS_ENABLED;
        }
    }

    /**
     * ADASの有効設定.
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isAdasEnabled()
     * @see #KEY_ADAS_ENABLED
     */
    public AppSharedPreference setAdasEnabled(boolean isEnabled) {
        mPreferences.edit()
                .putBoolean(KEY_ADAS_ENABLED, isEnabled)
                .apply();
        return this;
    }

    /**
     * ADAS Alarm設定が有効か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setAdasAlarmEnabled(boolean)
     * @see #KEY_ADAS_ENABLED
     */
    public boolean isAdasAlarmEnabled() {
        if (mPreferences.contains(KEY_ADAS_ALARM_ENABLED)) {
            return mPreferences.getBoolean(KEY_ADAS_ALARM_ENABLED, DEFAULT_ADAS_ALARM_ENABLED);
        } else {
            setAdasAlarmEnabled(DEFAULT_ADAS_ALARM_ENABLED);
            return DEFAULT_ADAS_ALARM_ENABLED;
        }
    }

    /**
     * ADAS Alarm設定の有効設定.
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isAdasAlarmEnabled()
     * @see #KEY_ADAS_ENABLED
     */
    public AppSharedPreference setAdasAlarmEnabled(boolean isEnabled) {
        mPreferences.edit()
                .putBoolean(KEY_ADAS_ALARM_ENABLED, isEnabled)
                .apply();
        return this;
    }

    /**
     * ADAS設定済か否か取得.
     *
     * @return {@code true}:設定済。｛@code false}:未設定。
     * @see #setAdasSettingConfigured(boolean)
     * @see #KEY_ADAS_SETTING_CONFIGURED
     */
    public boolean isAdasSettingConfigured() {
        if (mPreferences.contains(KEY_ADAS_SETTING_CONFIGURED)) {
            return mPreferences.getBoolean(KEY_ADAS_SETTING_CONFIGURED, DEFAULT_ADAS_SETTING_CONFIGURED);
        } else {
            setAdasSettingConfigured(DEFAULT_ADAS_SETTING_CONFIGURED);
            return DEFAULT_ADAS_SETTING_CONFIGURED;
        }
    }

    /**
     * ADAS設定済か否か.
     *
     * @param isConfigured {@code true}:設定済。｛@code false}:未設定。
     * @return 本オブジェクト
     * @see #isAdasSettingConfigured()
     * @see #KEY_ADAS_SETTING_CONFIGURED
     */
    public AppSharedPreference setAdasSettingConfigured(boolean isConfigured) {
        mPreferences.edit()
                .putBoolean(KEY_ADAS_SETTING_CONFIGURED, isConfigured)
                .apply();
        return this;
    }

    /**
     * キャリブレーション設定取得.
     *
     * @return 画面内に見える車体の先端の高さ[px]
     * @see #setAdasCalibrationSetting(int)
     * @see #KEY_ADAS_CALIBRATION_SETTING
     */
    public int getAdasCalibrationSetting() {
        if (mPreferences.contains(KEY_ADAS_CALIBRATION_SETTING)) {
            return mPreferences.getInt(KEY_ADAS_CALIBRATION_SETTING, DEFAULT_ADAS_CALIBRATION_SETTING);
        } else {
            setAdasCalibrationSetting(DEFAULT_ADAS_CALIBRATION_SETTING);
            return DEFAULT_ADAS_CALIBRATION_SETTING;
        }
    }

    /**
     * キャリブレーション設定.
     *
     * @param setting 画面内に見える車体の先端の高さ[px]
     * @return 本オブジェクト
     * @see #getAdasCalibrationSetting()
     * @see #KEY_ADAS_CALIBRATION_SETTING
     */
    public AppSharedPreference setAdasCalibrationSetting(int setting) {
        mPreferences.edit()
                .putInt(KEY_ADAS_CALIBRATION_SETTING, setting)
                .apply();
        return this;
    }

    /**
     * カメラ設定取得.
     *
     * @return AdasCameraSetting
     * @see #setAdasCameraSetting(AdasCameraSetting)
     * @see #KEY_ADAS_CAMERA_SETTING
     */
    @NonNull
    public AdasCameraSetting getAdasCameraSetting() {
        if (mPreferences.contains(KEY_ADAS_CAMERA_SETTING)) {
            String value = mPreferences.getString(KEY_ADAS_CAMERA_SETTING, DEFAULT_ADAS_CAMERA_SETTING);
            return new Gson().fromJson(value, AdasCameraSetting.class);
        } else {
            AdasCameraSetting setting = new Gson().fromJson(DEFAULT_ADAS_CAMERA_SETTING, AdasCameraSetting.class);
            setAdasCameraSetting(setting);
            return setting;
        }
    }

    /**
     * カメラ設定.
     *
     * @param setting AdasCameraSetting
     * @return 本オブジェクト
     * @throws NullPointerException {@code setting}がnull
     * @see #getAdasCameraSetting()
     * @see #KEY_ADAS_CAMERA_SETTING
     */
    public AppSharedPreference setAdasCameraSetting(@NonNull AdasCameraSetting setting) {
        checkNotNull(setting);

        String value = new Gson().toJson(setting);
        mPreferences.edit()
                .putString(KEY_ADAS_CAMERA_SETTING, value)
                .apply();
        return this;
    }

    /**
     * LDW設定.
     *
     * @return AdasFunctionSetting
     * @see #setAdasLdwSetting(AdasFunctionSetting)
     * @see #KEY_ADAS_LDW_SETTING
     */
    @NonNull
    public AdasFunctionSetting getAdasLdwSetting() {
        if (mPreferences.contains(KEY_ADAS_LDW_SETTING)) {
            String value = mPreferences.getString(KEY_ADAS_LDW_SETTING, DEFAULT_ADAS_LDW_SETTING);
            return new Gson().fromJson(value, AdasFunctionSetting.class);
        } else {
            AdasFunctionSetting setting = new Gson().fromJson(DEFAULT_ADAS_LDW_SETTING, AdasFunctionSetting.class);
            setAdasLdwSetting(setting);
            return setting;
        }
    }

    /**
     * LDW設定取得.
     *
     * @param setting AdasFunctionSetting
     * @return 本オブジェクト
     * @throws NullPointerException {@code setting}がnull
     * @see #getAdasLdwSetting()
     * @see #KEY_ADAS_LDW_SETTING
     */
    public AppSharedPreference setAdasLdwSetting(@NonNull AdasFunctionSetting setting) {
        checkNotNull(setting);

        String value = new Gson().toJson(setting);
        mPreferences.edit()
                .putString(KEY_ADAS_LDW_SETTING, value)
                .apply();
        return this;
    }

    /**
     * PCW設定.
     *
     * @return AdasFunctionSetting
     * @see #setAdasPcwSetting(AdasFunctionSetting)
     * @see #KEY_ADAS_PCW_SETTING
     */
    @NonNull
    public AdasFunctionSetting getAdasPcwSetting() {
        if (mPreferences.contains(KEY_ADAS_PCW_SETTING)) {
            String value = mPreferences.getString(KEY_ADAS_PCW_SETTING, DEFAULT_ADAS_PCW_SETTING);
            return new Gson().fromJson(value, AdasFunctionSetting.class);
        } else {
            AdasFunctionSetting setting = new Gson().fromJson(DEFAULT_ADAS_PCW_SETTING, AdasFunctionSetting.class);
            setAdasPcwSetting(setting);
            return setting;
        }
    }

    /**
     * PCW設定取得.
     *
     * @param setting AdasFunctionSetting
     * @return 本オブジェクト
     * @throws NullPointerException {@code setting}がnull
     * @see #getAdasPcwSetting()
     * @see #KEY_ADAS_PCW_SETTING
     */
    public AppSharedPreference setAdasPcwSetting(@NonNull AdasFunctionSetting setting) {
        checkNotNull(setting);

        String value = new Gson().toJson(setting);
        mPreferences.edit()
                .putString(KEY_ADAS_PCW_SETTING, value)
                .apply();
        return this;
    }

    /**
     * FCW設定.
     *
     * @return AdasFunctionSetting
     * @see #setAdasFcwSetting(AdasFunctionSetting)
     * @see #KEY_ADAS_FCW_SETTING
     */
    @NonNull
    public AdasFunctionSetting getAdasFcwSetting() {
        if (mPreferences.contains(KEY_ADAS_FCW_SETTING)) {
            String value = mPreferences.getString(KEY_ADAS_FCW_SETTING, DEFAULT_ADAS_FCW_SETTING);
            return new Gson().fromJson(value, AdasFunctionSetting.class);
        } else {
            AdasFunctionSetting setting = new Gson().fromJson(DEFAULT_ADAS_FCW_SETTING, AdasFunctionSetting.class);
            setAdasFcwSetting(setting);
            return setting;
        }
    }

    /**
     * FCW設定取得.
     *
     * @param setting AdasFunctionSetting
     * @return 本オブジェクト
     * @throws NullPointerException {@code setting}がnull
     * @see #getAdasFcwSetting()
     * @see #KEY_ADAS_FCW_SETTING
     */
    public AppSharedPreference setAdasFcwSetting(@NonNull AdasFunctionSetting setting) {
        checkNotNull(setting);

        String value = new Gson().toJson(setting);
        mPreferences.edit()
                .putString(KEY_ADAS_FCW_SETTING, value)
                .apply();
        return this;
    }

    /**
     * LKW設定.
     *
     * @return AdasFunctionSetting
     * @see #setAdasLkwSetting(AdasFunctionSetting)
     * @see #KEY_ADAS_LKW_SETTING
     */
    @NonNull
    public AdasFunctionSetting getAdasLkwSetting() {
        if (mPreferences.contains(KEY_ADAS_LKW_SETTING)) {
            String value = mPreferences.getString(KEY_ADAS_LKW_SETTING, DEFAULT_ADAS_LKW_SETTING);
            return new Gson().fromJson(value, AdasFunctionSetting.class);
        } else {
            AdasFunctionSetting setting = new Gson().fromJson(DEFAULT_ADAS_LKW_SETTING, AdasFunctionSetting.class);
            setAdasLkwSetting(setting);
            return setting;
        }
    }

    /**
     * LKW設定取得.
     *
     * @param setting AdasFunctionSetting
     * @return 本オブジェクト
     * @throws NullPointerException {@code setting}がnull
     * @see #getAdasLkwSetting()
     * @see #KEY_ADAS_LKW_SETTING
     */
    public AppSharedPreference setAdasLkwSetting(@NonNull AdasFunctionSetting setting) {
        checkNotNull(setting);

        String value = new Gson().toJson(setting);
        mPreferences.edit()
                .putString(KEY_ADAS_LKW_SETTING, value)
                .apply();
        return this;
    }

    /**
     * ライティングエフェクト設定がか否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setLightingEffectEnabled(boolean)
     * @see #KEY_LIGHTING_EFFECT_ENABLED
     */
    public boolean isLightingEffectEnabled() {
        if (mPreferences.contains(KEY_LIGHTING_EFFECT_ENABLED)) {
            return mPreferences.getBoolean(KEY_LIGHTING_EFFECT_ENABLED, DEFAULT_LIGHTING_EFFECT_ENABLED);
        } else {
            setLightingEffectEnabled(DEFAULT_LIGHTING_EFFECT_ENABLED);
            return DEFAULT_LIGHTING_EFFECT_ENABLED;
        }
    }

    /**
     * ライティングエフェクトの有効設定.
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isLightingEffectEnabled()
     * @see #KEY_LIGHTING_EFFECT_ENABLED
     */
    public AppSharedPreference setLightingEffectEnabled(boolean isEnabled) {
        mPreferences.edit()
                .putBoolean(KEY_LIGHTING_EFFECT_ENABLED, isEnabled)
                .apply();
        return this;
    }

    /**
     * 距離単位取得.
     *
     * @return SmartPhoneRepeatMode
     * @see #setDistanceUnit(DistanceUnit)
     * @see #KEY_DISTANCE_UNIT
     */
    @NonNull
    public DistanceUnit getDistanceUnit() {
        if (mPreferences.contains(KEY_DISTANCE_UNIT)) {
            String value = mPreferences.getString(KEY_DISTANCE_UNIT, DEFAULT_DISTANCE_UNIT);
            return DistanceUnit.valueOf(value);
        } else {
            DistanceUnit unit = DistanceUnit.valueOf(DEFAULT_DISTANCE_UNIT);
            setDistanceUnit(unit);
            return unit;
        }
    }

    /**
     * 時刻表示取得.
     *
     * @return SmartPhoneRepeatMode
     * @see #setTimeFormatSetting(TimeFormatSetting)
     * @see #KEY_TIME_FORMAT_SETTING
     */
    @NonNull
    public TimeFormatSetting getTimeFormatSetting() {
        if (mPreferences.contains(KEY_TIME_FORMAT_SETTING)) {
            String value = mPreferences.getString(KEY_TIME_FORMAT_SETTING, DEFAULT_TIME_FORMAT_SETTING);
            return TimeFormatSetting.valueOf(value);
        } else {
            TimeFormatSetting setting = TimeFormatSetting.valueOf(DEFAULT_TIME_FORMAT_SETTING);
            setTimeFormatSetting(setting);
            return setting;
        }
    }

    /**
     * 時刻表示設定.
     *
     * @param setting TimeFormatSetting
     * @return 本オブジェクト
     * @throws NullPointerException {@code setting}がnull
     * @see #getTimeFormatSetting()
     * @see #KEY_TIME_FORMAT_SETTING
     */
    @NonNull
    public AppSharedPreference setTimeFormatSetting(@NonNull TimeFormatSetting setting) {
        checkNotNull(setting);

        mPreferences.edit()
                .putString(KEY_TIME_FORMAT_SETTING, setting.name())
                .apply();
        return this;
    }

    /**
     * 距離単位設定.
     *
     * @param unit DistanceUnit
     * @return 本オブジェクト
     * @throws NullPointerException {@code unit}がnull
     * @see #getDistanceUnit()
     * @see #KEY_DISTANCE_UNIT
     */
    @NonNull
    public AppSharedPreference setDistanceUnit(@NonNull DistanceUnit unit) {
        checkNotNull(unit);

        mPreferences.edit()
                .putString(KEY_DISTANCE_UNIT, unit.name())
                .apply();
        return this;
    }

    /**
     * 初回SLA設定を実施済か否か取得.
     *
     * @return {@code true}:有効。｛@code false}:無効。
     * @see #setConfiguredSlaSetting(boolean)
     * @see #KEY_CONFIGURED_INITIAL_SLA_SETTING
     */
    public boolean isConfiguredSlaSetting() {
        if (mPreferences.contains(KEY_CONFIGURED_INITIAL_SLA_SETTING)) {
            return mPreferences.getBoolean(KEY_CONFIGURED_INITIAL_SLA_SETTING, DEFAULT_CONFIGURED_INITIAL_SLA_SETTING);
        } else {
            setLightingEffectEnabled(DEFAULT_CONFIGURED_INITIAL_SLA_SETTING);
            return DEFAULT_CONFIGURED_INITIAL_SLA_SETTING;
        }
    }

    /**
     * 初回SLA設定を実施済設定.
     *
     * @param isEnabled {@code true}:有効。｛@code false}:無効。
     * @return 本オブジェクト
     * @see #isConfiguredSlaSetting()
     * @see #KEY_CONFIGURED_INITIAL_SLA_SETTING
     */
    public AppSharedPreference setConfiguredSlaSetting(boolean isEnabled) {
        mPreferences.edit()
                .putBoolean(KEY_CONFIGURED_INITIAL_SLA_SETTING, isEnabled)
                .apply();
        return this;
    }
    /**
     * 音声認識機能種別取得.
     *
     * @return VoiceRecognizeType
     * @see #setVoiceRecognitionType(VoiceRecognizeType)
     * @see #KEY_ALEXA_LANGUAGE_SETTING
     */
    @NonNull
    public AlexaLanguageType getAlexaLanguage() {
        if (mPreferences.contains(KEY_ALEXA_LANGUAGE_SETTING)) {
            String value = mPreferences.getString(KEY_ALEXA_LANGUAGE_SETTING, DEFAULT_ALEXA_LANGUAGE_SETTING);
            return AlexaLanguageType.valueOf(value);
        } else {
            AlexaLanguageType type = AlexaLanguageType.valueOf(DEFAULT_ALEXA_LANGUAGE_SETTING);
            setAlexaLanguage(type);
            return type;
        }
    }

    /**
     * 音声認識機能の種別設定.
     *
     * @param type AlexaLanguageType
     * @return 本オブジェクト
     * @see #getAlexaLanguage()
     * @see #KEY_ALEXA_LANGUAGE_SETTING
     */
    @NonNull
    public AppSharedPreference setAlexaLanguage(@NonNull AlexaLanguageType type) {
        checkNotNull(type);

        mPreferences.edit()
                .putString(KEY_ALEXA_LANGUAGE_SETTING, type.name())
                .apply();
        return this;
    }

    /**
     * カスタムキー種別取得.
     *
     * @return CustomKey
     * @see #setCustomKeyType(CustomKey)
     * @see #KEY_CUSTOM_KEY_TYPE
     */
    @NonNull
    public CustomKey getCustomKeyType() {
        if (mPreferences.contains(KEY_CUSTOM_KEY_TYPE)) {
            String value = mPreferences.getString(KEY_CUSTOM_KEY_TYPE, DEFAULT_CUSTOM_KEY_TYPE);
            return CustomKey.valueOf(value);
        } else {
            CustomKey type = CustomKey.valueOf(DEFAULT_CUSTOM_KEY_TYPE);
            setCustomKeyType(type);
            return type;
        }
    }

    /**
     * カスタムキー種別設定.
     *
     * @param type CustomKey
     * @return 本オブジェクト
     * @see #getCustomKeyType()
     * @see #KEY_CUSTOM_KEY_TYPE
     */
    @NonNull
    public AppSharedPreference setCustomKeyType(@NonNull CustomKey type) {
        checkNotNull(type);

        mPreferences.edit()
                .putString(KEY_CUSTOM_KEY_TYPE, type.name())
                .apply();
        return this;
    }

    /**
     * カスタムキー ダイレクトソース切替の対象ソース取得.
     *
     * @return MediaSourceType
     * @see #setCustomKeyDirectSource(MediaSourceType)
     * @see #KEY_CUSTOM_KEY_DIRECT_SOURCE
     */
    @NonNull
    public MediaSourceType getCustomKeyDirectSource() {
        if (mPreferences.contains(KEY_CUSTOM_KEY_DIRECT_SOURCE)) {
            String value = mPreferences.getString(KEY_CUSTOM_KEY_DIRECT_SOURCE, DEFAULT_CUSTOM_KEY_DIRECT_SOURCE);
            return MediaSourceType.valueOf(value);
        } else {
            MediaSourceType type = MediaSourceType.valueOf(DEFAULT_CUSTOM_KEY_DIRECT_SOURCE);
            setCustomKeyDirectSource(type);
            return type;
        }
    }

    /**
     * カスタムキー ダイレクトソース切替の対象ソース設定.
     *
     * @param type MediaSourceType
     * @return 本オブジェクト
     * @see #getCustomKeyDirectSource()
     * @see #KEY_CUSTOM_KEY_DIRECT_SOURCE
     */
    @NonNull
    public AppSharedPreference setCustomKeyDirectSource(@NonNull MediaSourceType type) {
        checkNotNull(type);

        mPreferences.edit()
                .putString(KEY_CUSTOM_KEY_DIRECT_SOURCE, type.name())
                .apply();
        return this;
    }
    /**
     * カスタムキー 3rdApp切替の該当アプリ取得.
     *
     * @return Application
     * @see #setCustomKeyMusicApp(Application)
     * @see #KEY_CUSTOM_KEY_MUSIC_APP
     */
    @NonNull
    public Application getCustomKeyMusicApp() {
        if (mPreferences.contains(KEY_CUSTOM_KEY_MUSIC_APP)) {
            String value = mPreferences.getString(KEY_CUSTOM_KEY_MUSIC_APP, DEFAULT_CUSTOM_KEY_MUSIC_APP);
            return new Gson().fromJson(value, Application.class);
        } else {
            Application app = new Gson().fromJson(DEFAULT_CUSTOM_KEY_MUSIC_APP, Application.class);
            setCustomKeyMusicApp(app);
            return app;
        }
    }

    /**
     * カスタムキー 3rdApp切替の該当アプリ設定.
     *
     * @param application Application
     * @return 本オブジェクト
     * @see #getCustomKeyMusicApp()
     * @see #KEY_CUSTOM_KEY_MUSIC_APP
     */
    @NonNull
    public AppSharedPreference setCustomKeyMusicApp(@NonNull Application application) {
        checkNotNull(application);

        String value = new Gson().toJson(application);
        mPreferences.edit()
                .putString(KEY_CUSTOM_KEY_MUSIC_APP, value)
                .apply();
        return this;
    }

    /**
     * YouTubeLink設定画面における有効/無効状態の取得
     *
     * @return {@code true}:有効　{@code false}:無効
     * @see #setYouTubeLinkSettingEnabled(boolean)
     * @see #KEY_YOUTUBE_LINK_SETTING_ENABLED
     */
    public boolean isYouTubeLinkSettingEnabled(){
        if(mPreferences.contains(KEY_YOUTUBE_LINK_SETTING_ENABLED)){
            return mPreferences.getBoolean(KEY_YOUTUBE_LINK_SETTING_ENABLED, DEFAULT_YOUTUBE_LINK_SETTING_ENABLED);
        }
        else {
            setYouTubeLinkSettingEnabled(DEFAULT_YOUTUBE_LINK_SETTING_ENABLED);
            return DEFAULT_YOUTUBE_LINK_SETTING_ENABLED;
        }
    }

    /**
     * YouTubeLink設定画面における有効/無効状態の設定
     *
     * @param isEnabled {@code true}:有効　{@code false}:無効
     * @return 本オブジェクト
     * @see #isYouTubeLinkSettingEnabled() YouTubeLink設定画面における有効/無効状態の取得
     * @see #KEY_YOUTUBE_LINK_SETTING_ENABLED
     */
    public AppSharedPreference setYouTubeLinkSettingEnabled(boolean isEnabled){
        mPreferences.edit()
                .putBoolean(KEY_YOUTUBE_LINK_SETTING_ENABLED, isEnabled)
                .apply();
        return this;
    }

    /**
     * YouTubeLinkCaution画面の次回以降非表示設定の取得
     *
     * @return {@code true}:非表示　{@code false}:表示
     * @see #setYouTubeLinkCautionNoDisplayAgain(boolean)
     * @see #KEY_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN
     */
    public boolean isYouTubeLinkCautionNoDisplayAgain(){
        if(mPreferences.contains(KEY_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN)){
            return mPreferences.getBoolean(KEY_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN, DEFAULT_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN);
        }
        else {
            setYouTubeLinkCautionNoDisplayAgain(DEFAULT_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN);
            return DEFAULT_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN;
        }
    }

    /**
     * YouTubeLinkCaution画面の次回以降非表示の設定
     * @param isNoDisplayAgain {@code true}:非表示　{@code false}:表示
     * @return 本オブジェクト
     * @see #isYouTubeLinkCautionNoDisplayAgain()
     * @see #KEY_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN
     */
    public AppSharedPreference setYouTubeLinkCautionNoDisplayAgain(boolean isNoDisplayAgain){
        mPreferences.edit()
                .putBoolean(KEY_YOUTUBE_LINK_CAUTION_NO_DISPLAY_AGAIN, isNoDisplayAgain)
                .apply();
        return this;
    }

    private static Gson getGson() {
        if (sGson == null) {
            sGson = new GsonBuilder().registerTypeHierarchyAdapter(Uri.class, new UriAdapter()).create();
        }

        return sGson;
    }

    private void notifyListeners(String key) {
        Set<OnAppSharedPreferenceChangeListener> listeners = mListeners.keySet();
        Stream.of(listeners).filter(listener -> !(listener == null)).forEach(listener -> listener.onAppSharedPreferenceChanged(this, key));
    }

    /**
     * 設定変更時に呼ばれるリスナー.
     *
     * @see #registerOnAppSharedPreferenceChangeListener(OnAppSharedPreferenceChangeListener)
     * @see #unregisterOnAppSharedPreferenceChangeListener(OnAppSharedPreferenceChangeListener)
     */
    public interface OnAppSharedPreferenceChangeListener {
        /**
         * 設定変更.
         *
         * @param preferences AppSharedPreference
         * @param key         変更されたPreferenceキー（{@code AppSharedPreference.KEY_*}）
         */
        void onAppSharedPreferenceChanged(@NonNull AppSharedPreference preferences, @NonNull String key);
    }
}
