package jp.pioneer.carsync.presentation.presenter;

import android.app.Instrumentation;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.LoaderManager;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.ControlRadioSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.LiveSimulationSetting;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.util.FrequencyUtil;
import jp.pioneer.carsync.presentation.view.TiView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TiPresenterのTest
 */
public class TiPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    TiPresenter mPresenter;
    @Mock TiView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mStatusHolder;
    @Mock ControlRadioSource mControlCase;
    @Mock AppSharedPreference mPreference;
    @Mock ControlMediaList mMediaCase;
    @Mock PreferSoundFx mFxCase;
    private RadioInfo mTestRadio;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        mTestRadio = new RadioInfo();
        mTestRadio.band = RadioBandType.FM1;
        mTestRadio.psInfo = "TEST PS";
        mTestRadio.ptyInfo = "TEST PTY";
        mTestRadio.currentFrequency = 99999L;
        mTestRadio.frequencyUnit = TunerFrequencyUnit.MHZ2;
        mTestRadio.songTitle = "TEST SONG";
        mTestRadio.artistName = "TEST ARTIST";
        mTestRadio.antennaLevel = 8;
        mTestRadio.maxAntennaLevel = 10;
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.NOT_LIST;
        carDeviceStatus.sourceType = MediaSourceType.TI;
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mStatusHolder.execute()).thenReturn(holder);

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() ->{
            mPresenter = new TiPresenter();
            mPresenter.mContext = mContext;
            mPresenter.mPreference = mPreference;
            mPresenter.mEventBus = mEventBus;
            mPresenter.mStatusHolder = mStatusHolder;
            mPresenter.mControlCase = mControlCase;
            mPresenter.mFxCase = mFxCase;
            mPresenter.setUp(mStatusHolder,mEventBus,mMediaCase);
        });

    }

    @After
    public void tearDown() throws Exception {
        mPresenter.stopHandler();
    }

    @Test
    public void testOnTakeView() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        CarDeviceMediaInfoHolder mockMediaHolder = mock(CarDeviceMediaInfoHolder.class);
        mockMediaHolder.radioInfo = mTestRadio;

        SoundFxSettingStatus settingStatus = mock(SoundFxSettingStatus.class);
        settingStatus.liveSimulationSettingEnabled = true;
        settingStatus.superTodorokiSettingEnabled = true;

        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);

        AudioSettingStatus audioSettingStatus = mock(AudioSettingStatus.class);
        audioSettingStatus.equalizerSettingEnabled = true;
        when(mockHolder.getAudioSettingStatus()).thenReturn(audioSettingStatus);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.SUPER_BASS;
        fxSetting.liveSimulationSetting = new LiveSimulationSetting();
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.OFF;
        fxSetting.liveSimulationSetting.soundFieldControlSettingType = SoundFieldControlSettingType.DOME;
        when(mockHolder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mockHolder.getCarDeviceMediaInfoHolder()).thenReturn(mockMediaHolder);
        when(mockHolder.getSoundFxSettingStatus()).thenReturn(settingStatus);
        when(mStatusHolder.execute()).thenReturn(mockHolder);

        LoaderManager mockLoaderManager = mock(LoaderManager.class);
        mPresenter.takeView(mView);
        mPresenter.onResume();

        verify(mView).setFrequency(FrequencyUtil.toString(getTargetContext(), mTestRadio.currentFrequency, mTestRadio.frequencyUnit));
        verify(mView).setAntennaLevel((float)mTestRadio.antennaLevel/mTestRadio.maxAntennaLevel);
    }

    @Test
    public void testOnNextChannelAction() throws Exception {
        mPresenter.onNextChannelAction();
        verify(mControlCase).channelUp();
    }

    @Test
    public void testOnPreviousChannelAction() throws Exception {
        mPresenter.onPreviousChannelAction();
        verify(mControlCase).channelDown();
    }

    /**
     * onSelectSourceActionのテスト
     */
    @Test
    public void testOnSelectSourceAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onSelectSourceAction();

        verify(mEventBus).post(any(BackgroundChangeEvent.class));
        verify(mEventBus, atLeast(2)).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getAllValues().get(1);
        assertThat(capturedEvent.screenId, is(ScreenId.SOURCE_SELECT));
    }

}