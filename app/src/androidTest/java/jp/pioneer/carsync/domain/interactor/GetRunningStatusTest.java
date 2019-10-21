package jp.pioneer.carsync.domain.interactor;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.BearingProvider;
import jp.pioneer.carsync.domain.component.LocationProvider;
import jp.pioneer.carsync.domain.model.CarRunningStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/20.
 */
public class GetRunningStatusTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks GetRunningStatus mGetRunningStatus = new GetRunningStatus(){
        @Override
        void removeCallback(Runnable runnable) {
            mRemoveCount++;
        }
    };
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock LocationProvider mLocationProvider;
    @Mock BearingProvider mBearingProvider;
    @Mock EventBus mEventBus;

    CarRunningStatus mCarRunningStatus = new CarRunningStatus();
    Location mLocation = mock(Location.class);
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal;
    LocationProvider.Callback mLocationCallback;

    // startのテストで使用
    boolean isSpeedUpdated;
    int mCount;

    // stopのテストで使用
    int mRemoveCount;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        isSpeedUpdated = false;
        mCount = 0;
        mRemoveCount = 0;

        when(mStatusHolder.getCarRunningStatus()).thenReturn(mCarRunningStatus);
    }

    @Test
    public void start() throws Exception {
        // setup
        mSignal = new CountDownLatch(12);
        when(mLocation.hasSpeed()).thenReturn(true);
        when(mLocation.getAltitude()).thenReturn(10d);
        when(mLocation.getSpeed()).thenReturn(27.7f); // 27.7m/s = 100km/h
        when(mLocationProvider.getLastLocation()).thenReturn(mLocation);
        long[] expected = new long[]{0,10,20,30,40,50,60,70,80,90};

        doAnswer(
                invocationOnMock -> {
                    BearingProvider.Callback callback = (BearingProvider.Callback)invocationOnMock.getArguments()[0];
                    callback.onSuccess(200f);
                    return null;
                }).when(mBearingProvider).startGetBearing(any(BearingProvider.Callback.class));

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                Runnable runnable = message.getCallback();
                if(runnable instanceof GetRunningStatus.SpeedObservationTask){
                    if(isSpeedUpdated){
                        return;
                    } else {
                        isSpeedUpdated = true;
                        mSignal.countDown();
                    }
                } else if (runnable instanceof GetRunningStatus.SpeedMeterUpdateTask){
                    if(((GetRunningStatus.SpeedMeterUpdateTask) runnable).isFinished()){
                        mSignal.countDown();
                        return;
                    } else if (((GetRunningStatus.SpeedMeterUpdateTask) runnable).isSetSpeed()){
                        assertThat(Math.round(mCarRunningStatus.speedForSpeedMeter), is(expected[mCount]));
                        mCount++;

                        mSignal.countDown();
                    }
                }
                runnable.run();
            });
            return true;
        });

        // exercise
        mGetRunningStatus.start();
        mSignal.await();

        // verify
        assertThat(Math.round(mCarRunningStatus.speed), is(100L));
        assertThat(mCarRunningStatus.altitude, is(10d));
        assertThat(mCarRunningStatus.averageSpeed, is(0d));
        assertThat(mCarRunningStatus.bearing, is(200f));
        assertThat(Math.round(mCarRunningStatus.speedForSpeedMeter), is(100L));
        verify(mLocationProvider).startGetCurrentLocation(eq(LocationProvider.Priority.HIGH_ACCURACY), any(LocationProvider.Callback.class), eq(LocationProvider.GetType.CONTINUOUS));
    }

    @Test
    public void stop() throws Exception {
        // exercise
        mGetRunningStatus.stop();

        // verify
        verify(mLocationProvider).finishGetCurrentLocation();
        verify(mBearingProvider).finishGetBearing();
        assertThat(mRemoveCount, is(2));
    }

}