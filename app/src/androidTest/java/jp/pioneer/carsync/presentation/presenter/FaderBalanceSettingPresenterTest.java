package jp.pioneer.carsync.presentation.presenter;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.AudioSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferAudio;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.FaderBalanceSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/09/22.
 */
public class FaderBalanceSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks FaderBalanceSettingPresenter mPresenter = new FaderBalanceSettingPresenter();
    @Mock FaderBalanceSettingView mView;
    @Mock EventBus mEventBus;
    @Mock PreferAudio mPreferAudio;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock AppSharedPreference mAppSharedPreference;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void onTakeView() throws Exception {
        when(mAppSharedPreference.getUiColor()).thenReturn(AQUA);
    }

    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        AudioSettingSpec audioSpec = new AudioSettingSpec();
        AudioSettingStatus audioStatus =  new AudioSettingStatus();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getAudioSettingStatus()).thenReturn(audioStatus);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onResume();
        verify(mEventBus).register(mPresenter);
    }

    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        AudioSettingSpec audioSpec = new AudioSettingSpec();
        AudioSettingStatus audioStatus =  new AudioSettingStatus();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getAudioSettingStatus()).thenReturn(audioStatus);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onResume();
        verify(mEventBus, times(0)).register(mPresenter);
    }

    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void setFaderBalance() throws Exception {
        mPresenter.setFaderBalance(1, 2);
        verify(mPreferAudio).setFaderBalance(1, 2);
    }

    @Test
    public void onAudioSettingChangeAction() throws Exception {
        AudioSettingChangeEvent event = mock(AudioSettingChangeEvent.class);
        mPresenter.onAudioSettingChangeAction(event);
        verify(mView).onStatusUpdated();
    }

}