package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.VisibleForTesting;
import android.view.Surface;
import android.view.WindowManager;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import jp.pioneer.carsync.domain.component.BearingProvider;
import timber.log.Timber;

import static android.hardware.SensorManager.SENSOR_DELAY_NORMAL;

/**
 * BearingProviderの実装.
 */
public class BearingProviderImpl implements BearingProvider, SensorEventListener {
    /** センサー取得間隔. */
    private static final int SAMPLING_PERIOD = SENSOR_DELAY_NORMAL;
    /** フィルター値. */
    private static final float ALPHA = 0.8f;

    @Inject Context mContext;
    @Inject Handler mHandler;
    @Inject SensorManager mSensorManager;
    private WeakReference<Callback> mCallback;
    private SensorHolder mAccelerometer;
    private SensorHolder mGeomagnetic;

    private float[] mGravity;
    private float[] mMagnetic;

    /**
     * コンストラクタ.
     */
    @Inject
    public BearingProviderImpl() {
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

        mAccelerometer = getSensor(Sensor.TYPE_ACCELEROMETER);
        if (mAccelerometer.isSensorNull()) {
            Timber.e("initialize() SensorManager#getDefaultSensor() failed.");
            return;
        }

        mGeomagnetic = getSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mGeomagnetic.isSensorNull()) {
            Timber.e("initialize() SensorManager#getDefaultSensor() failed.");
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void startGetBearing(@NonNull Callback callback) {
        Timber.i("startGetBearing()");

        if (mCallback != null && mCallback.get() != null) {
            Timber.e("startGetBearing() multiple access.");
            callbackError(Error.MULTIPLE_ACCESS);
            return;
        }

        mCallback = new WeakReference<>(callback);

        if (mSensorManager == null) {
            Timber.e("startGetBearing() sensor manager unsupported.");
            callbackError(Error.SENSOR_MANAGER_UNSUPPORTED);
            mCallback = null;
            return;
        }

        if (mAccelerometer.isSensorNull()) {
            Timber.e("startGetBearing() accelerometer unsupported.");
            callbackError(Error.ACCELEROMETER_UNSUPPORTED);
            mCallback = null;
            return;
        }

        if (mGeomagnetic.isSensorNull()) {
            Timber.e("startGetBearing() geomagnetic unsupported.");
            callbackError(Error.GEOMAGNETIC_UNSUPPORTED);
            mCallback = null;
            return;
        }

        mSensorManager.registerListener(this, mAccelerometer.get(), SAMPLING_PERIOD);
        mSensorManager.registerListener(this, mGeomagnetic.get(), SAMPLING_PERIOD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void finishGetBearing() {
        Timber.i("finishGetBearing()");

        mCallback = null;
        mSensorManager.unregisterListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void onSensorChanged(SensorEvent event) {
        SensorEventHolder sensorEvent = createSensorEventHolder(event);
        switch (sensorEvent.getSensorType()) {
            case Sensor.TYPE_ACCELEROMETER:
                if(mGravity == null){
                    mGravity = new float[3];
                }
                lowPassFilter(mGravity, sensorEvent.getSensorValues());
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                if(mMagnetic == null){
                    mMagnetic = new float[3];
                }
                lowPassFilter(mMagnetic, sensorEvent.getSensorValues());
                break;
            default:
                return;
        }

        if (mGravity == null || mMagnetic == null) {
            return;
        }

        // 回転行列
        float[] inR = getRotationMatrix(mGravity, mMagnetic);
        // ワールド座標とデバイス座標のマッピング変換
        float[] outR = remapCoordinateSystem(inR);
        // 姿勢取得
        float[] attitude = getOrientation(outR);
        callbackSuccess(convertRadToDeg(attitude[0]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do
    }

    private float convertRadToDeg(float rad) {
        float deg = rad * (float) 180.0 / (float) Math.PI;

        if (deg >= 0)
            return deg;
        else
            return 360 + deg;
    }

    private synchronized void callbackSuccess(float deg) {
        Callback callback = getCallback();
        if (callback == null) {
            Timber.w("callbackSuccess() callback has been cleared.");
            return;
        }

        mHandler.post(() -> callback.onSuccess(deg));
    }

    private synchronized void callbackError(Error error) {
        Callback callback = getCallback();
        if (callback == null) {
            Timber.w("callbackError() callback has been cleared.");
            return;
        }

        mHandler.post(() -> callback.onError(error));
    }

    @Nullable
    private synchronized Callback getCallback() {
        if (mCallback != null) {
            return mCallback.get();
        }
        return null;
    }

    /**
     * ローパスフィルター.
     * <p>
     * UnitTest用
     *
     * @param previousValue 前回取得値
     * @param newValue 新規取得値
     */
    @VisibleForTesting
    void lowPassFilter(float[] previousValue, float[] newValue) {
        for (int i = 0; i < previousValue.length; i++) {
            previousValue[i] = ALPHA * previousValue[i] + (1 - ALPHA) * newValue[i];
        }
    }

    /**
     * 回転行列生成
     * <p>
     * UnitTest用
     *
     * @return 回転行列
     */
    @Size(9)
    @VisibleForTesting
    float[] getRotationMatrix(float[] gravity, float[] magnetic) {
        float[] inR = new float[9];
        SensorManager.getRotationMatrix(
                inR,
                null,
                gravity,
                magnetic
        );

        return inR;
    }

    /**
     * 回転行列を画面の座標系にマッピング.
     * <p>
     * UnitTest用
     *
     * @param inR 回転行列
     * @return マッピング後の値
     */
    @Size(9)
    @VisibleForTesting
    float[] remapCoordinateSystem(@Size(9) float[] inR) {
        float[] outR = new float[9];
        WindowManager winMan = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int dr = winMan.getDefaultDisplay().getRotation();
        int axisX,axisY;
        switch (dr) {
            case Surface.ROTATION_0:
                axisX = SensorManager.AXIS_X;
                axisY = SensorManager.AXIS_Y;
                break;

            case Surface.ROTATION_90:
                axisX = SensorManager.AXIS_Y;
                axisY = SensorManager.AXIS_MINUS_X;
                break;

            case Surface.ROTATION_180:
                axisX = SensorManager.AXIS_MINUS_X;
                axisY = SensorManager.AXIS_MINUS_Y;
                break;

            case Surface.ROTATION_270:
                axisX = SensorManager.AXIS_MINUS_Y;
                axisY = SensorManager.AXIS_X;
                break;

            default:
                axisX=SensorManager.AXIS_X;
                axisY=SensorManager.AXIS_Y;
                break;
        }
        SensorManager.remapCoordinateSystem(
                inR,
                axisX,
                axisY,
                outR
        );
        return outR;
    }

    /**
     * 回転角取得.
     * <p>
     * UnitTest用
     *
     * @param outR マッピング後の値
     * @return 回転角
     */
    @Size(3)
    @VisibleForTesting
    float[] getOrientation(@Size(9) float[] outR) {
        float[] attitude = new float[3];
        SensorManager.getOrientation(
                outR,
                attitude
        );
        return attitude;
    }

    /**
     * センサー取得
     * <p>
     * UnitTest用
     *
     * @return センサー保持クラス
     */
    @VisibleForTesting
    SensorHolder getSensor(int sensorType) {
        return new SensorHolder(mSensorManager.getDefaultSensor(sensorType));
    }

    /**
     * センサーイベントホルダー生成
     * <p>
     * UnitTest用
     *
     * @return センサー保持クラス
     */
    @VisibleForTesting
    SensorEventHolder createSensorEventHolder(SensorEvent event) {
        return new SensorEventHolder(event);
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
    }

    /**
     * センサーイベント保持クラス.
     * <p>
     * UnitTest用
     */
    static class SensorEventHolder {
        SensorEvent mEvent;

        /**
         * コンストラクタ
         *
         * @param event センサーイベント
         */
        SensorEventHolder(SensorEvent event) {
            mEvent = event;
        }

        /**
         * 発生したセンサーイベントのセンサー種別取得
         *
         * @return センサーの種別
         */
        @VisibleForTesting
        int getSensorType() {
            return mEvent.sensor.getType();
        }

        /**
         * 発生したセンサーイベントの値取得
         *
         * @return センサーの値
         */
        @VisibleForTesting
        float[] getSensorValues() {
            return mEvent.values.clone();
        }
    }
}
