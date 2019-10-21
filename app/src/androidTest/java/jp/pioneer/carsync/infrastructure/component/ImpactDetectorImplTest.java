package jp.pioneer.carsync.infrastructure.component;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.greenrobot.eventbus.EventBus;
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

import jp.pioneer.carsync.domain.event.ImpactEvent;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Created by NSW00_008320 on 2017/04/21.
 */
@RunWith(Theories.class)
public class ImpactDetectorImplTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ImpactDetectorImpl mImpactDetectorImpl = new ImpactDetectorImpl(){
        SensorHolder mSensorHolder = new SensorHolder(null){
            @Override
            Sensor get() {
                return super.get();
            }

            @Override
            float getMaximumRange() {
                return mMaximumRange;
            }

            @Override
            boolean isSensorNull() {
                return mSensorIsNull;
            }

            @Override
            int getSensorType(SensorEvent event) {
                return mSensorType;
            }

            @Override
            float[] getSensorValues(SensorEvent event) {
                return mSensorValues;
            }
        };

        @Override
        SensorHolder getSensor() {
            return mSensorHolder;
        }
    };

    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock SensorManager mSensorManager;

    float mMaximumRange;
    boolean mSensorIsNull;
    int mSensorType;
    float[] mSensorValues;

    private static final float FILTER_CONSTANT = 0.9f;
    static final float IMPACT_THRESHOLD = 6.0f * 9.8f;

    static class FailureStartDetectionFixture {
        boolean isSensorNull;
        SensorManager sensorManager;

        FailureStartDetectionFixture(boolean isSensorNull,SensorManager sensorManager) {
            this.isSensorNull = isSensorNull;
            this.sensorManager = sensorManager;
        }
    }

    @DataPoints
    public static final FailureStartDetectionFixture[] FAILURE_START_DETECTION_FIXTURES = new FailureStartDetectionFixture[] {
            new FailureStartDetectionFixture(true,null), // Sensor:null Manager:null
            new FailureStartDetectionFixture(true,mock(SensorManager.class)), // Sensor:null Manager:not null
            new FailureStartDetectionFixture(false,null) // Sensor:not null Manager:null
    };

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void getMaximumRange_SensorNotNull() throws Exception {
        // setup
        mMaximumRange = 100f;
        mSensorIsNull = false;
        mImpactDetectorImpl.initialize();

        // exercise
        float actual = mImpactDetectorImpl.getMaximumRange();

        // verify
        assertThat(actual,is(100f));

    }

    @Test
    public void getMaximumRange_SensorNull() throws Exception {
        // setup
        mMaximumRange = 100f;
        mSensorIsNull = true;
        mImpactDetectorImpl.initialize();

        // exercise
        float actual = mImpactDetectorImpl.getMaximumRange();

        // verify
        assertThat(actual,is(0.0f));

    }

    @Test
    public void startDetectionTrue() throws Exception {
        // setup
        mSensorIsNull = false;
        mImpactDetectorImpl.initialize();

        // exercise
        boolean actual = mImpactDetectorImpl.startDetection(FILTER_CONSTANT,IMPACT_THRESHOLD);

        // verify
        assertThat(actual,is(true));
        verify(mSensorManager).registerListener(mImpactDetectorImpl,null,SENSOR_DELAY_GAME);

    }

    @Theory
    public void startDetectionFalse(FailureStartDetectionFixture fixture) throws Exception {
        // setup
        mSensorIsNull = fixture.isSensorNull;
        mImpactDetectorImpl.mSensorManager = fixture.sensorManager;
        mImpactDetectorImpl.initialize();

        // exercise
        boolean actual = mImpactDetectorImpl.startDetection(FILTER_CONSTANT,IMPACT_THRESHOLD);

        // verify
        assertThat(actual,is(false));

    }

    @Test
    public void stopDetection_StartedTrue() throws Exception {
        // setup
        mSensorIsNull = false;
        mImpactDetectorImpl.initialize();

        // exercise
        mImpactDetectorImpl.startDetection(FILTER_CONSTANT,IMPACT_THRESHOLD);
        mImpactDetectorImpl.stopDetection();

        // verify
        verify(mSensorManager).unregisterListener(mImpactDetectorImpl);
    }

    @Test
    public void stopDetection_StartedFalse() throws Exception {
        // setup
        mSensorIsNull = true;
        mImpactDetectorImpl.initialize();

        // exercise
        mImpactDetectorImpl.stopDetection();

        // verify
        verify(mSensorManager,never()).unregisterListener(any(SensorEventListener.class));
    }

    @Test
    public void onSensorChanged_CalculationResultIsOverThreshold() throws Exception {
        // setup
        SensorEvent sensorEvent = mock(SensorEvent.class);
        SensorEventListener sensorEventListener = mImpactDetectorImpl;

        mSensorValues = new float[]{10f,20f,30f};

        mSensorType = Sensor.TYPE_ACCELEROMETER;
        mImpactDetectorImpl.initialize();
        mImpactDetectorImpl.startDetection(FILTER_CONSTANT,IMPACT_THRESHOLD);
        sensorEventListener.onSensorChanged(sensorEvent);
        sensorEventListener.onSensorChanged(sensorEvent);
        sensorEventListener.onSensorChanged(sensorEvent);

        // exercise
        mSensorValues = new float[]{100f,200f,300f};
        sensorEventListener.onSensorChanged(sensorEvent);

        // verify
        verify(mEventBus).post(any(ImpactEvent.class));

    }

    @Test
    public void onSensorChanged_CalculationResultIsUnderOrEqualThreshold() throws Exception {
        // setup
        SensorEvent sensorEvent = mock(SensorEvent.class);
        SensorEventListener sensorEventListener = mImpactDetectorImpl;
        mSensorValues = new float[]{10f,20f,30f};

        mSensorType = Sensor.TYPE_ACCELEROMETER;
        mImpactDetectorImpl.initialize();
        mImpactDetectorImpl.startDetection(FILTER_CONSTANT,IMPACT_THRESHOLD);
        sensorEventListener.onSensorChanged(sensorEvent);
        sensorEventListener.onSensorChanged(sensorEvent);
        sensorEventListener.onSensorChanged(sensorEvent);

        // exercise
        mSensorValues = new float[]{10f,20f,30f};
        sensorEventListener.onSensorChanged(sensorEvent);

        // verify
        verify(mEventBus,never()).post(any(ImpactEvent.class));
    }

    @Test
    public void onSensorChanged_SensorTypeIsNotAccelerometer() throws Exception {
        // setup
        SensorEvent sensorEvent = mock(SensorEvent.class);
        mSensorValues = null;
        mSensorType = Sensor.TYPE_GRAVITY;
        mImpactDetectorImpl.initialize();

        // exercise
        mImpactDetectorImpl.startDetection(FILTER_CONSTANT,IMPACT_THRESHOLD);
        mImpactDetectorImpl.onSensorChanged(sensorEvent);

        // verify
        verify(mEventBus,never()).post(any(ImpactEvent.class));
    }

    @Test
    public void onAccuracyChanged() throws Exception {
        // not test
    }

}