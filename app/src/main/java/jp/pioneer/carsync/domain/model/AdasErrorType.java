package jp.pioneer.carsync.domain.model;

/**
 * Created by NSW00_007906 on 2018/07/11.
 */

public enum AdasErrorType {
    /** 端末の位置情報のアクセス許可がOFF */
    PERMISSION_DENIED_ACCESS_LOCATION,
    /** カメラのアクセス許可がOFF */
    PERMISSION_DENIED_CAMERA,
    /** 認識率低下 */
    DECLINE_IN_RECOGNITION_RATE,
    /** 低照度・あるいは視界不良 */
    LOW_ILLUMINANCE_OR_POOR_VISIBILITY,
    /** ネットワークモード中(DEHのみ) */
    ALARM_ERROR_DURING_NETWORK_MODE,
    /** ソースOFF */
    ALARM_ERROR_SOURCE_OFF,
    /** 端末の向きが縦画面表示 */
    ORIENTATION_PORTRAIT,
}
