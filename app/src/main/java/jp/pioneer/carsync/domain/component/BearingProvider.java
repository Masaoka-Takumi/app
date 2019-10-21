package jp.pioneer.carsync.domain.component;

import android.hardware.Sensor;
import android.support.annotation.NonNull;

/**
 * 方角プロバイダー.
 * <p>
 * 方位取得は端末の加速度センサー（{@link Sensor#TYPE_ACCELEROMETER}）と
 * 地磁気センサー（{@link Sensor#TYPE_MAGNETIC_FIELD}）を使用して検出する。
 * 加速度だけでなく、地磁気についてもノイズ除去を実施する。
 * 基本的に、
 * <a href="https://developer.android.com/reference/android/hardware/SensorEvent.html">SensorEvent</a>
 * に記載されている方法を使用しており、{@code ALPHA}が{@code filterConstant}に相当する。<br>
 * <p>
 * 取得した方位は0~360の値が設定される(360°式)
 * 0(360):北,90:東,180:南,270:西
 */
public interface BearingProvider {

    /**
     * 方位取得開始.
     *
     * @throws NullPointerException {@code callback}がnull
     */
    void startGetBearing(@NonNull Callback callback);

    /**
     * 方位取得終了.
     */
    void finishGetBearing();

    /**
     * エラー
     *
     * @see Callback#onError(Error)
     */
    enum Error {
        /** 多重開始. */
        MULTIPLE_ACCESS,
        /** 加速度計非対応. */
        ACCELEROMETER_UNSUPPORTED,
        /** 地磁気計非対応. */
        GEOMAGNETIC_UNSUPPORTED,
        /** SensorManager非対応. */
        SENSOR_MANAGER_UNSUPPORTED
    }

    /**
     * コールバック.
     */
    interface Callback{
        /**
         * 成功.
         *
         * @param deg 方位
         */
        void onSuccess(float deg);

        /**
         * 失敗.
         */
        void onError(Error error);
    }
}
