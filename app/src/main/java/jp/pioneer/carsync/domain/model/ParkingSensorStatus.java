package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * パーキングセンサーステータス.
 * <p>
 * センサー値取得前は各センサーはnullの状態
 * 各センサーの状態が異常の場合、そのセンサーに対応したセンサー距離は参照しない
 */
public class ParkingSensorStatus extends SerialVersion {
    /** エラー状態.*/
    public ParkingSensorErrorStatus errorStatus;
    /** センサーD状態.*/
    public SensorStatus sensorStatusD;
    /** センサーC状態.*/
    public SensorStatus sensorStatusC;
    /** センサーB状態.*/
    public SensorStatus sensorStatusB;
    /** センサーA状態.*/
    public SensorStatus sensorStatusA;
    /** 距離単位.*/
    public SensorDistanceUnit sensorDistanceUnit;
    /** センサーD距離.*/
    public int sensorDistanceD;
    /** センサーC距離.*/
    public int sensorDistanceC;
    /** センサーB距離.*/
    public int sensorDistanceB;
    /** センサーA距離.*/
    public int sensorDistanceA;

    /**
     * コンストラクタ.
     */
    public ParkingSensorStatus() {
        reset();
    }

    /**
     * リセット.
     */
    public void reset() {
        errorStatus = null;
        sensorStatusD = null;
        sensorStatusC = null;
        sensorStatusB = null;
        sensorStatusA = null;
        sensorDistanceUnit = null;
        sensorDistanceD = 0;
        sensorDistanceC = 0;
        sensorDistanceB = 0;
        sensorDistanceA = 0;
        updateVersion();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("errorStatus", errorStatus)
                .add("sensorStatusD", sensorStatusD)
                .add("sensorStatusC", sensorStatusC)
                .add("sensorStatusB", sensorStatusB)
                .add("sensorStatusA", sensorStatusA)
                .add("sensorDistanceUnit", sensorDistanceUnit)
                .add("sensorDistanceD", sensorDistanceD)
                .add("sensorDistanceC", sensorDistanceC)
                .add("sensorDistanceB", sensorDistanceB)
                .add("sensorDistanceA", sensorDistanceA)
                .toString();
    }
}
