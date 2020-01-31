package jp.pioneer.carsync.domain.model;

import com.google.common.base.MoreObjects;

/**
 * 自車走行状態.
 * <p>
 * 現在の速度は{@link #speed}を参照
 * スピードメーターで使用する速度は{@link #speedForSpeedMeter}を参照
 */
public class CarRunningStatus {
    /** 現在位置の緯度. */
    public double latitude;
    /** 現在位置の経度. */
    public double longitude;
    /** 速度[km/h]. */
    public double speed;

    /** スピードメーター表示用速度[km/h]. */
    public double speedForSpeedMeter;

    /**
     * 方位.
     * <p>
     * 0~360の値が設定される(360°式)
     * 0(360):北,90:東,180:南,270:西
     * <p>
     * 取得失敗や取得できない端末の場合は-1を設定
     */
    public float bearing;

    /** 高度[m]. */
    public double altitude;

    /** 平均速度[km/h]. */
    public double averageSpeed;

    /** メッシュコード. */
    public int meshCode;

    /**
     * コンストラクタ.
     */
    public void reset() {
        latitude = 0;
        longitude = 0;
        speed = -1;
        speedForSpeedMeter = -1;
        bearing = -1.0f;
        altitude = Double.MIN_VALUE;
        averageSpeed = -1;
        meshCode = -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return MoreObjects.toStringHelper("")
                .add("latitude", latitude)
                .add("longitude", longitude)
                .add("speed", speed)
                .add("speedForSpeedMeter", speedForSpeedMeter)
                .add("bearing", bearing)
                .add("altitude", altitude)
                .add("averageSpeed", averageSpeed)
                .add("meshCode", meshCode)
                .toString();
    }
}
