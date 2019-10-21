package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferParkingSensor;
import jp.pioneer.carsync.domain.model.AlarmOutputDestinationSetting;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ParkingSensorSetting;
import jp.pioneer.carsync.domain.model.ParkingSensorSettingStatus;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.CarSafetyView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * CarSafety設定のPresenter
 */
public class CarSafetyPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks CarSafetyPresenter mPresenter = new CarSafetyPresenter();
    @Mock CarSafetyView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock PreferParkingSensor mPreferParkingSensor;
    private String[] mOutputArray;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testLifecycle() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        status.parkingSensorSettingEnabled = true;
        ParkingSensorSetting parkingSensorSetting = new ParkingSensorSetting();
        ParkingSensorSettingStatus parkingSensorSettingStatus = new ParkingSensorSettingStatus();
        parkingSensorSettingStatus.alarmOutputDestinationSettingEnabled = true;
        parkingSensorSettingStatus.alarmVolumeSettingEnabled = true;
        parkingSensorSetting.parkingSensorSetting = true;
        parkingSensorSetting.alarmOutputDestinationSetting = AlarmOutputDestinationSetting.FRONT;
        parkingSensorSetting.alarmVolumeSetting.min = 1;
        parkingSensorSetting.alarmVolumeSetting.max = 30;
        parkingSensorSetting.alarmVolumeSetting.current = 15;

        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(status);
        when(mockHolder.getParkingSensorSettingStatus()).thenReturn(parkingSensorSettingStatus);
        when(mockHolder.getParkingSensorSetting()).thenReturn(parkingSensorSetting);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        mPresenter.onInitialize();
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

//        verify(mView).setParkingSensorEnabled(true);
//        verify(mView).setParkingSensorAlarmOutputDestinationSettingEnabled(true);
//        verify(mView).setParkingSensorAlarmVolumeSettingEnabled(true);
//        verify(mView).setParkingSensorAlarmOutputDestination(mContext.getResources().getStringArray(R.array.parking_sensor_alarm_output_destination)[0]);
//        verify(mView).setParkingSensorAlarmVolume(1,30,15);
//        verify(mView).setDeviceSessionEnabled(true);
        verify(mView).setParkingSensorSetting(true,true);
        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnParkingSensorSettingChangeEvent() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus status = new CarDeviceStatus();
        status.parkingSensorSettingEnabled = true;
        ParkingSensorSetting parkingSensorSetting = new ParkingSensorSetting();
        ParkingSensorSettingStatus parkingSensorSettingStatus = new ParkingSensorSettingStatus();
        parkingSensorSettingStatus.alarmOutputDestinationSettingEnabled = false;
        parkingSensorSettingStatus.alarmVolumeSettingEnabled = false;
        parkingSensorSetting.parkingSensorSetting = true;
        parkingSensorSetting.alarmOutputDestinationSetting = AlarmOutputDestinationSetting.FRONT;
        parkingSensorSetting.alarmVolumeSetting.min = 1;
        parkingSensorSetting.alarmVolumeSetting.max = 30;
        parkingSensorSetting.alarmVolumeSetting.current = 15;

        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceStatus()).thenReturn(status);
        when(mockHolder.getParkingSensorSettingStatus()).thenReturn(parkingSensorSettingStatus);
        when(mockHolder.getParkingSensorSetting()).thenReturn(parkingSensorSetting);
        mPresenter.onInitialize();
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

//        verify(mView).setParkingSensorEnabled(true);
//        verify(mView).setParkingSensorAlarmOutputDestinationSettingEnabled(false);
//        verify(mView).setParkingSensorAlarmVolumeSettingEnabled(false);
//        verify(mView,times(0)).setParkingSensorAlarmOutputDestination(mContext.getResources().getStringArray(R.array.parking_sensor_alarm_output_destination)[0]);
//        verify(mView,times(0)).setParkingSensorAlarmVolume(1,30,15);
        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnSelectImpactDetectionAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onSelectImpactDetectionAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.IMPACT_DETECTION_SETTINGS));
    }

    @Test
    public void testOnParkingSensorAlarmOutputChange() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        ParkingSensorSetting parkingSensorSetting = new ParkingSensorSetting();
        parkingSensorSetting.alarmOutputDestinationSetting = AlarmOutputDestinationSetting.FRONT;
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getParkingSensorSetting()).thenReturn(parkingSensorSetting);
        mPresenter.onInitialize();
//        mPresenter.onParkingSensorAlarmOutputChange();
//        verify(mPreferParkingSensor).toggleAlarmOutputDestination(AlarmOutputDestinationSetting.FRONT_LEFT);
    }


}