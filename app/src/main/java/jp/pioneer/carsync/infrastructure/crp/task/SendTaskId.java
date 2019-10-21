package jp.pioneer.carsync.infrastructure.crp.task;

/**
 * 送信タスクID.
 */
public enum SendTaskId {
    /** 初期認証タスク. */
    INITIAL_AUTH,
    /** セッション開始タスク. */
    SESSION_START,
    /** オーディオ設定情報要求タスク. */
    AUDIO_SETTINGS_REQUEST,
    /** システム設定情報要求タスク */
    SYSTEM_SETTING_REQUEST,
    /** イルミ設定情報要求タスク. */
    ILLUMINATION_SETTINGS_REQUEST,
    /** Function設定情報要求タスク. */
    FUNCTION_SETTINGS_REQUEST,
    /** パーキングセンサー設定情報要求タスク. */
    PARKING_SENSOR_SETTINGS_REQUEST,
    /** ナビガイド音声設定情報要求タスク. */
    NAVI_GUIDE_VOICE_SETTINGS_REQUEST,
    /** 初期設定情報要求タスク. */
    INITIAL_SETTINGS_REQUEST,
    /** Phone設定情報要求タスク. */
    PHONE_SETTINGS_REQUEST,
    /** SoundFX設定情報要求タスク. */
    SOUND_FX_SETTINGS_REQUEST,
    /** メディアリスト選択タスク. */
    MEDIA_LIST_SELECT,
    /** 通知タスク. */
    NOTIFY_TASK,
    /** 要求タスク. */
    REQUEST_TASK
}
