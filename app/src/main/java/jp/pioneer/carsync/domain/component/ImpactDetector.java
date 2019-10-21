package jp.pioneer.carsync.domain.component;

import android.hardware.Sensor;

/**
 * 衝突検知者.
 * <p>
 * 衝突検知は端末の加速度センサー（{@link Sensor#TYPE_ACCELEROMETER}）を使用して検出する。
 * 加速度には重力が含まれているため、重力分を除いた加速度を使用する。
 * 基本的に、
 * <a href="https://developer.android.com/reference/android/hardware/SensorEvent.html">SensorEvent</a>
 * に記載されている方法を使用しており、{@code alpha}が{@link #startDetection(float, float)}の
 * {@code filterConstant}に相当する。<br>
 * 線形加速度（{@link Sensor#TYPE_LINEAR_ACCELERATION}）のセンサーを使用していないのは、
 * 一部の端末でおかしな値を示すものがあったためである。
 */
public interface ImpactDetector {
    /**
     * センサーの最大値域取得.
     *
     * @return 最大値域（m/s2）。センサーが無効の場合は0を返す。
     */
    float getMaximumRange();

    /**
     * 衝突検知開始.
     * <p>
     * 値のチェックは最低限のレベルであり、現実的かどうかは判断しない。
     *
     * @param filterConstant 平滑化係数。[0.0, 1.0)。
     * @param impactThreshold 衝突とみなす閾値（m/s2）。本値を超えた場合に衝突が発生したと判断する。(0, +∞)。
     * @return {@code true}:成功。{@code false}:失敗。
     * @throws IllegalStateException 既に開始している
     * @throws IllegalArgumentException {@code filterConstant}か{@code impactThreshold}の値が不正
     */
    boolean startDetection(float filterConstant, float impactThreshold);

    /**
     * 衝突検知停止.
     * <p>
     * 既に停止している（開始していない）場合は無視する。
     */
    void stopDetection();
}
