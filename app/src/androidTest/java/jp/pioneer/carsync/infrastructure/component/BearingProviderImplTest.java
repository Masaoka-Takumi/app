package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Size;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.BearingProvider;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/20.
 */
@RunWith(Theories.class)
public class BearingProviderImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks BearingProviderImpl mBearingProvider = new BearingProviderImpl(){
        @Override
        SensorHolder getSensor(int sensorType) {
            return sensorType == Sensor.TYPE_ACCELEROMETER ? mAccelerometerSensorHolder : mGeomagneticSensorHolder;
        }

        SensorHolder mAccelerometerSensorHolder = new SensorHolder(null){
            @Override
            float getMaximumRange() {
                return mAccelerometerMaximumRange;
            }

            @Override
            boolean isSensorNull() {
                return mAccelerometerSensorIsNull;
            }
        };

        SensorHolder mGeomagneticSensorHolder = new SensorHolder(null){
            @Override
            float getMaximumRange() {
                return mGeomagneticMaximumRange;
            }

            @Override
            boolean isSensorNull() {
                return mGeomagneticSensorIsNull;
            }
        };

        @Override
        SensorEventHolder createSensorEventHolder(SensorEvent event) {
            return mSensorEventHolder;
        }

        SensorEventHolder mSensorEventHolder = new SensorEventHolder(null){
            @Override
            int getSensorType() {
                return mSensorType;
            }

            @Override
            float[] getSensorValues() {
                return mSensorValues;
            }
        };

        @Override
        void lowPassFilter(float[] previousValue, float[] newValue) {
            System.arraycopy(newValue, 0, previousValue, 0, previousValue.length);
        }

        @Override
        float[] getRotationMatrix(float[] gravity, float[] magnetic) {
            assertThat(gravity, is(mAccelerometerResult));
            assertThat(magnetic, is(mMagneticResult));
            return mMatrixResult;
        }

        @Override
        float[] remapCoordinateSystem(@Size(9) float[] inR) {
            assertThat(inR, is(mMatrixResult));
            return mRemapResult;
        }

        @Override
        float[] getOrientation(@Size(9) float[] outR) {
            assertThat(outR, is(mRemapResult));
            return mOrientationResult;
        }
    };
    @Mock Context mContext;
    @Mock Handler mHandler;
    @Mock SensorManager mSensorManager;

    // SensorHolderItem
    private float mAccelerometerMaximumRange;
    private boolean mAccelerometerSensorIsNull;
    private float mGeomagneticMaximumRange;
    private boolean mGeomagneticSensorIsNull;
    // SensorEventHolderItem
    private int mSensorType;
    private float[] mSensorValues;
    // Bearing
    private float[] mAccelerometerResult = new float[]{1,2,3};
    private float[] mMagneticResult = new float[]{11,12,13};
    private float[] mMatrixResult = new float[]{21,22,23,24,25,26,27,28,29};
    private float[] mRemapResult = new float[]{31,32,33,34,35,36,37,38,39};
    private float[] mOrientationResult = new float[]{41,42,43};

    private SensorEventListener mSensorEventListener;
    private CountDownLatch mSignal;
    Handler mMainHandler = new Handler(Looper.getMainLooper());

    static class FailureStartFixture {
        boolean isAccelerometerSensorNull;
        boolean isGeoMagneticSensorNull;
        SensorManager sensorManager;

        FailureStartFixture(boolean isAccelerometerSensorNull, boolean isGeoMagneticSensorNull, SensorManager sensorManager) {
            this.isAccelerometerSensorNull = isAccelerometerSensorNull;
            this.isGeoMagneticSensorNull = isGeoMagneticSensorNull;
            this.sensorManager = sensorManager;
        }
    }

    @DataPoints
    public static final FailureStartFixture[] FAILURE_START_FIXTURES = new FailureStartFixture[] {
            new FailureStartFixture(true, true, null),                     // Accelerometer:null    , GeoMagnetic:null    , Manager:null
            new FailureStartFixture(false, true, null),                    // Accelerometer:not null, GeoMagnetic:null    , Manager:null
            new FailureStartFixture(true, false, null),                    // Accelerometer:null    , GeoMagnetic:not null, Manager:null
            new FailureStartFixture(true, true, mock(SensorManager.class)) // Accelerometer:null    , GeoMagnetic:null    , Manager:not null
    };


    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });
    }

    @Test
    public void startGetBearing() throws Exception {
        // setup
        mSignal = new CountDownLatch(2);
        BearingProvider.Callback callback = mock(BearingProvider.Callback.class);
        when(mSensorManager.registerListener(any(SensorEventListener.class), nullable(Sensor.class), anyInt())).then(invocationOnMock -> {
            mSensorEventListener = (SensorEventListener) invocationOnMock.getArguments()[0];
            mSignal.countDown();
            return true;
        });

        float expected = mOrientationResult[0] * (float) 180.0 / (float) Math.PI;

        mBearingProvider.initialize();

        // exercise
        mBearingProvider.startGetBearing(callback);
        mSignal.await();

        mSignal = new CountDownLatch(1);
        mSensorType = Sensor.TYPE_ACCELEROMETER;
        mSensorValues = mAccelerometerResult;
        mSensorEventListener.onSensorChanged(null);
        mSensorType = Sensor.TYPE_MAGNETIC_FIELD;
        mSensorValues = mMagneticResult;
        mSensorEventListener.onSensorChanged(null);
        mSignal.await();

        // verify
        verify(callback).onSuccess(expected);
    }

    @Theory
    public void startGetBearing_Failure(FailureStartFixture fixture) throws Exception {
        // setup
        mSignal = new CountDownLatch(1);
        BearingProvider.Callback callback = mock(BearingProvider.Callback.class);
        mAccelerometerSensorIsNull = fixture.isAccelerometerSensorNull;
        mGeomagneticSensorIsNull = fixture.isGeoMagneticSensorNull;
        mBearingProvider.mSensorManager = fixture.sensorManager;
        mBearingProvider.initialize();

        // exercise
        mBearingProvider.startGetBearing(callback);
        mSignal.await();

        // verify
        verify(mSensorManager, never()).registerListener(any(SensorEventListener.class), any(Sensor.class), anyInt());
        verify(callback).onError(any(BearingProvider.Error.class));

    }

    @Test
    public void finishGetBearing() throws Exception {
        // exercise
        mBearingProvider.finishGetBearing();

        // verify
        verify(mSensorManager).unregisterListener(mBearingProvider);
    }

}