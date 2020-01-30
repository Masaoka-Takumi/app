package jp.pioneer.carsync.domain.interactor;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;

import org.greenrobot.eventbus.EventBus;
import org.matthiaszimmermann.location.egm96.Geoid;

import java.util.ArrayList;

import javax.inject.Inject;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.ForDomain;
import jp.pioneer.carsync.application.di.ForInfrastructure;
import jp.pioneer.carsync.domain.component.BearingProvider;
import jp.pioneer.carsync.domain.component.LocationProvider;
import jp.pioneer.carsync.domain.component.Resolver;
import jp.pioneer.carsync.domain.event.LocationMeshCodeChangeEvent;
import jp.pioneer.carsync.domain.model.CarDeviceDestinationInfo;
import jp.pioneer.carsync.domain.model.CarRunningStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.util.RadioStationNameUtil;
import timber.log.Timber;

/**
 * 走行状態取得.
 * <p>
 * 速度、平均速度、高さ、方位を取得する。
 * 速度、平均速度、高さは{@link Location}から取得
 * 方位は加速度センサーと地磁気センサーから取得
 * <p>
 * スピードメーターの速度を滑らかに更新するため、
 * 速度を管理する1秒周期タイマー（{@link SpeedObservationTask}）と
 * 速度を更新する100ms周期タイマー（{@link SpeedMeterUpdateTask}）を使用する。
 * <p>
 * 走行状態:
 * <pre>{@code
 *      CarRunningStatus status = statusHolder.getCarRunningStatus();
 *  }</pre>
 */
public class GetRunningStatus {
    private static final LocationProvider.Priority PRIORITY = LocationProvider.Priority.HIGH_ACCURACY;
    private static final int ERROR = -1;
    private static final double MAX_SPEED = 240.0d;

    @Inject Context mContext;
    @Inject @ForInfrastructure Handler mHandler;
    @Inject @ForDomain StatusHolder mStatusHolder;
    @Inject LocationProvider mLocationProvider;
    @Inject BearingProvider mBearingProvider;
    @Inject EventBus mEventBus;
    @Inject AppSharedPreference mPreference;
    private SpeedObservationTask mSpeedObservationTask = new SpeedObservationTask();
    private SpeedMeterUpdateTask mSpeedMeterUpdateTask = new SpeedMeterUpdateTask();

    private double mGeoidOffset = 0;
    private boolean mGetMeshCode = false;
    /**
     * コンストラクタ.
     */
    @Inject
    public GetRunningStatus() {
    }

    /**
     * 開始.
     * <p>
     * 走行状態の取得を開始する
     */
    public void start() {
        mGetMeshCode = mPreference.getLastConnectedCarDeviceDestination() == CarDeviceDestinationInfo.JP.code;
        mLocationProvider.startGetCurrentLocation(PRIORITY, mLocationCallback, LocationProvider.GetType.CONTINUOUS);
        mBearingProvider.startGetBearing(mBearingCallback);
        mSpeedObservationTask.start();
        mSpeedMeterUpdateTask.start();
    }

    /**
     * 終了.
     * <p>
     * 走行状態の取得を停止する
     */
    public void stop() {
        mLocationProvider.finishGetCurrentLocation();
        mBearingProvider.finishGetBearing();
        mSpeedObservationTask.stop();
        mSpeedMeterUpdateTask.stop();
    }

    // MARK - callback

    /**
     * 位置取得用コールバック.
     */
    private LocationProvider.Callback mLocationCallback = new LocationProvider.Callback() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onSuccess(@NonNull Location location) {
            // nothing to do
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onError(@NonNull LocationProvider.Error error, @Nullable Resolver resolver) {
            // nothing to do
        }
    };

    /**
     * 方位取得用コールバック.
     */
    private BearingProvider.Callback mBearingCallback = new BearingProvider.Callback() {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onSuccess(float deg) {
            CarRunningStatus status = mStatusHolder.getCarRunningStatus();
            // 許可しないの場合、方位を -1 にする。
            boolean accessCoarseLocation = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            boolean accessFineLocation = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
            if (!(accessCoarseLocation && accessFineLocation)) {
                status.bearing = -1;
            } else {
                status.bearing = deg;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onError(BearingProvider.Error error) {
            CarRunningStatus status = mStatusHolder.getCarRunningStatus();
            status.bearing = ERROR;
        }
    };

    // MARK - task

    /**
     * 速度監視タスク.
     * <p>
     * 取得したロケーションから速度を取得して値を{@link CarRunningStatus}へ反映するタスク
     * 1秒ごとにステータスを更新する
     * 平均速度は1分間単位で1時間の平均を使用する
     */
    class SpeedObservationTask implements Runnable {
        private static final long DELAY_TIME = 1000;
        private static final int SAVE_COUNT = 60;
        private static final float KM_PER_HOUR = 3.6f;

        private boolean mIsStop;
        private Location mPreviousLocation;
        private double mPreviousSpeed = -1;
        private double mCurrentSpeed = -1;
        private double mAverageSpeed = -1;
        private ArrayList<Double> mMinuteSpeeds = new ArrayList<>();
        private ArrayList<Double> mMinuteAverageSpeeds = new ArrayList<>();

        /** 開始. */
        void start() {
            mHandler.postDelayed(this, DELAY_TIME);
            mIsStop = false;
        }

        /** 停止. */
        void stop() {
            removeCallback(this);
            mIsStop = true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            double speed = -1;
            Location location = mLocationProvider.getLastLocation();
            if (location != null) {
                if (mPreviousLocation != null &&
                        (!location.hasSpeed() || mPreviousLocation.getTime() == location.getTime())) {
                    location = mPreviousLocation;
                    speed = convertMsToKmh(mPreviousLocation.getSpeed());
                } else if (location.hasSpeed()) {
                    speed = convertMsToKmh(location.getSpeed());
                    mPreviousLocation = location;
                }
                int debugSpeed = mStatusHolder.getAppStatus().adasCarSpeed;
                if(debugSpeed > 0) {
                    speed = debugSpeed;
                }
                CarRunningStatus status = mStatusHolder.getCarRunningStatus();
                status.latitude = location.getLatitude();
                status.longitude = location.getLongitude();

                if(mGetMeshCode) {
                    //メッシュコード変更監視
                    int meshCode = RadioStationNameUtil.getMeshCode(status);
                    if (status.meshCode != meshCode) {
                        status.meshCode = meshCode;
                        mEventBus.post(new LocationMeshCodeChangeEvent());
                    }
                }

                //ジオイド高取得所要時間は1ms以下
                mGeoidOffset = Geoid.getOffset(new org.matthiaszimmermann.location.Location(status.latitude, status.longitude));
                if (mGeoidOffset == Geoid.OFFSET_INVALID || mGeoidOffset == Geoid.OFFSET_MISSING) {
                    Timber.d("mGeoidOffset = " + mGeoidOffset);
                    mGeoidOffset = 0;
                }

                if (speed >= 0) {
                    mCurrentSpeed = speed;
                    calculationAverageSpeed(speed);
                    status.speed = speed;
                    double wgs84altitude = location.getAltitude();
                    double egm96Altitude = wgs84altitude-mGeoidOffset;
                    //Timber.d("wgs84altitude = " + wgs84altitude + ", egm96Altitude = " + egm96Altitude + ", mGeoidOffset = " + mGeoidOffset);
                    status.altitude = egm96Altitude;
                    status.averageSpeed = mAverageSpeed;
                }

                // 許可しないの場合、速度、平均速度、高度 -1 にし、積算値をクリアする。
                boolean accessCoarseLocation = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean accessFineLocation = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                if (!(accessCoarseLocation && accessFineLocation)) {
                    // 速度
                    mCurrentSpeed = -1;
                    status.speedForSpeedMeter = -1;
                    status.speed = -1;
                    // 平均速度
                    mAverageSpeed = -1;
                    status.averageSpeed = -1;
                    mMinuteSpeeds.clear();
                    mMinuteAverageSpeeds.clear();
                    // 高度
                    status.altitude = Double.MIN_VALUE;
                }

                // アプリの使用中のみ許可の場合、平均速度を -1 にし、積算値をクリアする。
                if (Build.VERSION.SDK_INT >= 29) {
                    //Timber.d("Build.VERSION 29以上");
                    if (accessCoarseLocation && accessFineLocation) {
                        boolean accessBackgroundLocation =
                                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED;
                        // アプリ使用中のみ許可
                        if (!accessBackgroundLocation) {
                            //Timber.d("アプリ使用中のみ許可");
                            mAverageSpeed = -1;
                            status.averageSpeed = -1;
                            mMinuteSpeeds.clear();
                            mMinuteAverageSpeeds.clear();
                        }
                    }
                }

                if (mPreviousSpeed != mCurrentSpeed) {
                    mSpeedMeterUpdateTask.set(splitSpeed());
                    mPreviousSpeed = mCurrentSpeed;
                }
            }

            if (!mIsStop) {
                start();
            }
        }

        /**
         * 速度分割.
         * <p>
         * スピードメーターを滑らかに更新するために
         * 変化した速度を10分割する
         *
         * @return 10分割された速度の配列
         */
        @Size(10)
        private double[] splitSpeed() {
            double[] speeds = new double[10];
            double dif = mCurrentSpeed - mPreviousSpeed;
            for (int i = 0; i < 10; i++) {
                speeds[i] = mPreviousSpeed + ((dif / 10) * (i + 1));
            }

            return speeds;
        }

        private void calculationAverageSpeed(double currentSpeed) {
            mMinuteSpeeds.add(currentSpeed);

            if (mMinuteSpeeds.size() >= SAVE_COUNT) {
                double sum = 0;
                for (Double speed : mMinuteSpeeds) {
                    sum += speed;
                }

                mAverageSpeed = calculationHourAverageSpeed(sum / mMinuteSpeeds.size());
                mMinuteSpeeds.clear();
            }
        }

        private double calculationHourAverageSpeed(double minuteAverage) {
            if (mMinuteAverageSpeeds.size() >= SAVE_COUNT) {
                mMinuteAverageSpeeds.remove(0);
            }

            mMinuteAverageSpeeds.add(minuteAverage);

            double sum = 0;
            for (Double speed : mMinuteAverageSpeeds) {
                sum += speed;
            }

            return sum / mMinuteAverageSpeeds.size();
        }

        private double convertMsToKmh(float meterPerSec) {
            double speed = meterPerSec * KM_PER_HOUR;
            return speed > MAX_SPEED ? MAX_SPEED : speed;
        }
    }

    /**
     * スピードメーター速度更新タスク.
     * <p>
     * 100ミリ秒毎にスピードメーター用の速度を更新するタスク
     * 10分割された速度が設定された場合に速度を更新する。
     * 速度更新中に再設定された場合は再設定された速度で更新する。
     */
    class SpeedMeterUpdateTask implements Runnable {
        private static final long DELAY_TIME = 100;
        private int mCount;
        private double[] mSpeeds;
        private boolean mIsStop;

        /** 開始. */
        void start() {
            mHandler.postDelayed(this, DELAY_TIME);
            mIsStop = false;
        }

        /** 停止. */
        void stop() {
            removeCallback(this);
            mSpeeds = null;
            mIsStop = true;
        }

        /** 速度設定. */
        void set(@Size(10) double[] speeds) {
            mSpeeds = speeds;
            mCount = 0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            if (mSpeeds != null) {
                if (++mCount <= mSpeeds.length) {
                    CarRunningStatus status = mStatusHolder.getCarRunningStatus();
                    status.speedForSpeedMeter = mSpeeds[mCount - 1];
                } else {
                    mSpeeds = null;
                }
            }

            if (!mIsStop) {
                start();
            }
        }

        /**
         * 速度が設定されているか否か.
         */
        @VisibleForTesting
        boolean isSetSpeed() {
            return mSpeeds != null;
        }

        /**
         * 速度の更新が終了したか否か.
         */
        @VisibleForTesting
        boolean isFinished() {
            return mCount == 10;
        }
    }

    @VisibleForTesting
    void removeCallback(Runnable runnable) {
        mHandler.removeCallbacks(runnable);
    }
}
