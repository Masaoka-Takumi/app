package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.content.pm.ApplicationInfo;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import jp.pioneer.carsync.domain.event.AudioSettingChangeEvent;
import jp.pioneer.carsync.domain.event.CarDeviceStatusChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.SessionStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.CheckAvailableTextToSpeech;
import jp.pioneer.carsync.domain.interactor.ExitMenu;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.IsGrantReadNotification;
import jp.pioneer.carsync.domain.interactor.PreferNaviApp;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.SettingsEntranceView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/05/31.
 */
public class SettingsEntrancePresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SettingsEntrancePresenter mPresenter = new SettingsEntrancePresenter();
    @Mock SettingsEntranceView mView;
    @Mock EventBus mEventBus;
    @Mock PreferNaviApp mNaviCase;
    @Mock IsGrantReadNotification mIsGrantCase;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock CheckAvailableTextToSpeech mCheckTtsCase;
    @Mock Context mContext;
    @Mock ExitMenu mExitMenu;
    private List<ApplicationInfo> mInstallApplications;
    private List<ApplicationInfo> mInstallApplications2;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        mInstallApplications = new ArrayList<>();
        mInstallApplications2 = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ApplicationInfo info = new ApplicationInfo();
            info.packageName = "com.sample.app" + String.valueOf(i + 1);
            mInstallApplications.add(info);
        }
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        mPresenter.onResume();
    }

    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        mPresenter.onResume();
        verify(mEventBus).register(mPresenter);
    }

    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnClickActionSystem() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        status.systemSettingEnabled = true;
        spec.systemSettingSupported = true;
        mPresenter.onTakeView();
        mPresenter.onClickAction(0);
    }

    @Test
    public void testOnClickActionVoice() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        status.systemSettingEnabled = false;
        spec.systemSettingSupported = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        mPresenter.onTakeView();
        mPresenter.onClickAction(0);
    }

    @Test
    public void testOnClickActionNavigation() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        status.systemSettingEnabled = true;
        spec.systemSettingSupported = false;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        mPresenter.onTakeView();
        mPresenter.onClickAction(1);
    }

    @Test
    public void testOnClickActionMessage() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STOPPED);
        status.systemSettingEnabled = true;
        spec.systemSettingSupported = false;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        mPresenter.onTakeView();
        mPresenter.onClickAction(2);
    }

    @Test
    public void testOnClickActionPhone() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        status.phoneSettingEnabled = true;
        spec.phoneSettingSupported = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        mPresenter.onTakeView();
        mPresenter.onClickAction(3);
    }

    @Test
    public void testOnClickActionCarSafety() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        status.phoneSettingEnabled = false;
        spec.phoneSettingSupported = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        mPresenter.onTakeView();
        mPresenter.onClickAction(3);
    }

    @Test
    public void testOnClickActionAppearance() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        status.phoneSettingEnabled = true;
        spec.phoneSettingSupported = false;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        mPresenter.onTakeView();
        mPresenter.onClickAction(4);
    }

    @Test
    public void testOnClickActionSoundFX() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        status.systemSettingEnabled = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        mPresenter.onTakeView();
        mPresenter.onClickAction(5);
    }

    @Test
    public void testOnClickActionAudio() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        status.systemSettingEnabled = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(holder.isAudioSettingEnabled()).thenReturn(true);
        when(holder.isAudioSettingSupported()).thenReturn(true);
        mPresenter.onTakeView();
        mPresenter.onClickAction(6);
    }

    @Test
    public void testOnClickActionRadio() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        status.tunerFunctionSettingEnabled = true;
        spec.tunerFunctionSettingSupported = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        mPresenter.onTakeView();
        mPresenter.onClickAction(6);
    }

    @Test
    public void testOnClickActionAppFunction() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STOPPED);
        status.tunerFunctionSettingEnabled = true;
        spec.tunerFunctionSettingSupported = false;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        mPresenter.onTakeView();
        mPresenter.onClickAction(5);
    }

    @Test
    public void testOnClickActionAppInformation() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STOPPED);
        status.tunerFunctionSettingEnabled = false;
        spec.tunerFunctionSettingSupported = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        mPresenter.onTakeView();
        mPresenter.onClickAction(6);
    }

    @Test
    public void testOnAudioSettingChangeEvent() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        status.systemSettingEnabled = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        AudioSettingChangeEvent event = mock(AudioSettingChangeEvent.class);
        mPresenter.onAudioSettingChangeEvent(event);

    }
    @Test
    public void testOnRadioFunctionSettingChangeEvent() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        status.systemSettingEnabled = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        RadioFunctionSettingChangeEvent event = mock(RadioFunctionSettingChangeEvent.class);
        mPresenter.onRadioFunctionSettingChangeEvent(event);
    }
    @Test
    public void testOnCarDeviceStatusChangeEvent() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        status.systemSettingEnabled = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        CarDeviceStatusChangeEvent event = mock(CarDeviceStatusChangeEvent.class);
        mPresenter.onCarDeviceStatusChangeEvent(event);
    }
    @Test
    public void testOnSessionStatusChangeEvent() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus status = mock(CarDeviceStatus.class);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        status.systemSettingEnabled = true;
        when(holder.getCarDeviceStatus()).thenReturn(status);
        when(holder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        SessionStatusChangeEvent event = mock(SessionStatusChangeEvent.class);
        mPresenter.onSessionStatusChangeEvent(event);
    }

}