package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.VisibleForTesting;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.ImpactDetector;
import jp.pioneer.carsync.domain.event.ImpactEvent;
import timber.log.Timber;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * ImpactDetectorの実装.
 */
public class ImpactDetectorImpl implements ImpactDetector, SensorEventListener {
    private static final int SAMPLING_PERIOD = SENSOR_DELAY_GAME;

    @Inject Context mContext;
    @Inject EventBus mEventBus;
    @Inject SensorManager mSensorManager;
    private boolean mStarted;
    private float mFilterConstant;
    private float mImpactThreshold;
    private SensorHolder mAccelerometer;
    private float[] mGravity;

    /**
     * コンストラクタ.
     */
    @Inject
    public ImpactDetectorImpl() {
    }

    /**
     * 初期化.
     */
    public void initialize() {
        Timber.i("initialize()");

        if (mSensorManager == null) {
            Timber.e("initialize() SensorManager does not exist.");
            return;
        }

        mAccelerometer = getSensor();
        if (mAccelerometer.isSensorNull()) {
            Timber.e("initialize() SensorManager#getDefaultSensor() failed.");
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getMaximumRange() {return (mAccelerometer.isSensorNull()) ? 0.0f : mAccelerometer.getMaximumRange();}

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startDetection(float filterConstant, float impactThreshold) {
        Timber.i("startDetection() filterConstant = %f, impactThreshold = %f", filterConstant, impactThreshold);

        checkState(!mStarted);
        checkArgument(0.0f <= filterConstant && filterConstant < 1.0f);
        checkArgument(0.0f < impactThreshold);

        if (mSensorManager == null || mAccelerometer.isSensorNull()) {
            return false;
        }

        mFilterConstant = filterConstant;
        mImpactThreshold = impactThreshold;
        mGravity = new float[3];
        mSensorManager.registerListener(this, mAccelerometer.get(), SAMPLING_PERIOD);
        mStarted = true;
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopDetection() {
        Timber.i("stopDetection()");

        if (mStarted) {
            mSensorManager.unregisterListener(this);
            mStarted = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mAccelerometer.getSensorType(event) != Sensor.TYPE_ACCELEROMETER) {
            return;
        }

        float linearAcceleration = getLinearAcceleration(mAccelerometer.getSensorValues(event));
        if (linearAcceleration > mImpactThreshold) {
            Timber.d("onSensorChanged() ImpactEvent. linearAcceleration = %f m/s2(%f G)",
                    linearAcceleration, linearAcceleration / 9.8f);
            mEventBus.post(new ImpactEvent());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do
    }

    private float getLinearAcceleration(float[] acceleration) {
        mGravity[0] = applyLowPassFilter(mGravity[0], acceleration[0]);
        mGravity[1] = applyLowPassFilter(mGravity[1], acceleration[1]);
        mGravity[2] = applyLowPassFilter(mGravity[2], acceleration[2]);

        float x = applyHighPassFilter(mGravity[0], acceleration[0]);
        float y = applyHighPassFilter(mGravity[1], acceleration[1]);
        float z = applyHighPassFilter(mGravity[2], acceleration[2]);

        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    private float applyLowPassFilter(float gravity, float acceleration) {
        return mFilterConstant * gravity + (1.0f - mFilterConstant) * acceleration;
    }

    private float applyHighPassFilter(float gravity, float acceleration) {
        return acceleration - gravity;
    }

    /**
     * センサー取得
     * <p>
     * UnitTest用
     *
     * @return センサー保持クラス
     */
    @VisibleForTesting
    SensorHolder getSensor(){
        return new SensorHolder(mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER));
    }

    /**
     * センサー保持クラス
     * <p>
     * UnitTest用
     * SensorがFinalクラスによりmock化できないため、Sensorを保持するクラスを実装.
     */
    static class SensorHolder {
        Sensor mSensor;

        /**
         * コンストラクタ
         *
         * @param sensor センサー
         */
        SensorHolder(Sensor sensor) {
            mSensor = sensor;
        }

        /**
         * センサー取得
         *
         * @return センサー
         */
        Sensor get() {
            return mSensor;
        }

        /**
         * センサーの最大値取得
         *
         * @return 最大値
         */
        float getMaximumRange() {
            return mSensor.getMaximumRange();
        }

        /**
         * センサーnull判定
         *
         * @return {@code true}:センサーがnull状態. {@code false}:センサーのインスタンス生成済.
         */
        @VisibleForTesting
        boolean isSensorNull() {
            return mSensor == null;
        }

        /**
         * 発生したセンサーイベントのセンサー種別取得
         *
         * @param event 発生したセンサーイベント
         * @return センサーの種別
         */
        @VisibleForTesting
        int getSensorType(SensorEvent event) {
            return event.sensor.getType();
        }

        /**
         * 発生したセンサーイベントの値取得
         *
         * @param event 発生したセンサーイベント
         * @return センサーの値
         */
        @VisibleForTesting
        float[] getSensorValues(SensorEvent event){
            return event.values.clone();
        }

    }

}
