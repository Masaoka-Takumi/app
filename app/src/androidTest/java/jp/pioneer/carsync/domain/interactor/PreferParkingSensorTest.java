package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.ParkingSensorSettingUpdater;
import jp.pioneer.carsync.domain.model.AlarmOutputDestinationSetting;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.ParkingSensorSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/11/13.
 */
public class PreferParkingSensorTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public ExpectedException mException = ExpectedException.none();
    @InjectMocks PreferParkingSensor mPreferParkingSensor;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;
    @Mock ParkingSensorSettingUpdater mUpdater;

    ParkingSensorSettingStatus mParkingSensorSettingStatus = new ParkingSensorSettingStatus();
    ParkingSensorSetting mParkingSensorSetting = new ParkingSensorSetting();
    CarDeviceStatus mCarDeviceStatus = new CarDeviceStatus();
    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

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

        when(mStatusHolder.getParkingSensorSettingStatus()).thenReturn(mParkingSensorSettingStatus);
        when(mStatusHolder.getParkingSensorSetting()).thenReturn(mParkingSensorSetting);
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);

        mSignal = new CountDownLatch(1);
    }

    @Test
    public void setParkingSensorSetting_StatusEnabled() throws Exception {
        // setup
        mCarDeviceStatus.parkingSensorSettingEnabled = true;

        // exercise
        mPreferParkingSensor.setParkingSensorSetting(true);
        mSignal.await();

        // verify
        verify(mUpdater).setParkingSensorSetting(true);
    }

    @Test
    public void setParkingSensorSetting_StatusDisabled() throws Exception {
        // setup
        mCarDeviceStatus.parkingSensorSettingEnabled = false;

        // exercise
        mPreferParkingSensor.setParkingSensorSetting(true);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setParkingSensorSetting(anyBoolean());
    }

    @Test
    public void toggleAlarmOutputDestination() throws Exception {
        // setup
        mParkingSensorSettingStatus.alarmOutputDestinationSettingEnabled = true;
        mParkingSensorSetting.alarmOutputDestinationSetting = AlarmOutputDestinationSetting.FRONT_LEFT;

        // exercise
        mPreferParkingSensor.toggleAlarmOutputDestination();
        mSignal.await();

        // verify
        verify(mUpdater).setAlarmOutputDestination(AlarmOutputDestinationSetting.FRONT_RIGHT);
    }

    @Test
    public void toggleAlarmOutputDestination_SettingDisabled() throws Exception {
        // setup
        mParkingSensorSettingStatus.alarmOutputDestinationSettingEnabled = false;

        // exercise
        mPreferParkingSensor.toggleAlarmOutputDestination();
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAlarmOutputDestination(any(AlarmOutputDestinationSetting.class));
    }

    @Test
    public void setAlarmVolume_StatusEnabled() throws Exception {
        // setup
        mParkingSensorSettingStatus.alarmVolumeSettingEnabled = true;

        // exercise
        mPreferParkingSensor.setAlarmVolume(5);
        mSignal.await();

        // verify
        verify(mUpdater).setAlarmVolume(5);
    }

    @Test
    public void setAlarmVolume_StatusDisabled() throws Exception {
        // setup
        mParkingSensorSettingStatus.alarmVolumeSettingEnabled = false;

        // exercise
        mPreferParkingSensor.setAlarmVolume(5);
        mSignal.await();

        // verify
        verify(mUpdater, never()).setAlarmVolume(anyInt());
    }

}