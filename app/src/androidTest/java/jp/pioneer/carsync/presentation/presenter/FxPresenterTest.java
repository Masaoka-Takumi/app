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
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.MicVolumeSetting;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.FxView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/12/20.
 */
public class FxPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks FxPresenter mPresenter = new FxPresenter();
    @Mock FxView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock PreferSoundFx mPreferSoundFx;
    @Mock Context mContext;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testLifecycle() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        SoundFxSettingSpec fxSpec = new SoundFxSettingSpec();
        AudioSettingSpec audioSpec = new AudioSettingSpec();
        SoundFxSettingStatus status = new SoundFxSettingStatus();
        SoundFxSetting setting = new SoundFxSetting();
        status.karaokeSettingEnabled = true;
        setting.karaokeSetting = true;
        setting.micVolumeSetting = new MicVolumeSetting();
        setting.micVolumeSetting.min = 0;
        setting.micVolumeSetting.max = 30;
        setting.micVolumeSetting.current = 10;
        setting.vocalCancelSetting = true;
        spec.soundFxSettingSpec = fxSpec;
        when(mockHolder.getSoundFxSettingStatus()).thenReturn(status);
        when(mockHolder.getSoundFxSetting()).thenReturn(setting);
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

        verify(mView).setKaraokeSetting(true,true);

        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void onEqSettingAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onEqSettingAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.EQ_SETTING));
    }

    @Test
    public void onLiveSettingAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onLiveSettingAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.LIVE_SIMULATION_SETTING));
    }

    @Test
    public void onTodorokiSettingAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onTodorokiSettingAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.TODOROKI_SETTING));
    }

    @Test
    public void onSmallCarSettingAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onSmallCarSettingAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.SMALL_CAR_TA_SETTING));
    }

    @Test
    public void onKaraokeSettingAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onKaraokeSettingAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.KARAOKE_SETTING));
    }


}