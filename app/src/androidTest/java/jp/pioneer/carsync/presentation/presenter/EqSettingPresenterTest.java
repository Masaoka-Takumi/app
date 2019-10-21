package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.EqualizerSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CustomBandSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.EqSettingView;
import jp.pioneer.carsync.presentation.view.argument.EqSettingParams;
import jp.pioneer.carsync.presentation.view.argument.SettingsParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.model.CustomEqType.CUSTOM1;
import static jp.pioneer.carsync.domain.model.CustomEqType.CUSTOM2;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/07/20.
 */
public class EqSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks EqSettingPresenter mPresenter = new EqSettingPresenter();
    @Mock EqSettingView mView;
    @Mock PreferSoundFx mFxCase;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock AppSharedPreference mPreference;
    @Mock Context mContext;
    private static final int BAND_DATA_COUNT = 31; //全Band数
    private CustomEqType mCustomType = CustomEqType.CUSTOM1;
    private ArrayList<SoundFxSettingEqualizerType> mTestArray = new ArrayList<>();
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        mTestArray.clear();
        mTestArray.add(SoundFxSettingEqualizerType.SUPER_BASS);
        mTestArray.add(SoundFxSettingEqualizerType.POWERFUL);
        mTestArray.add(SoundFxSettingEqualizerType.NATURAL);
        mTestArray.add(SoundFxSettingEqualizerType.VOCAL);
        mTestArray.add(SoundFxSettingEqualizerType.TODOROKI);
        mTestArray.add(SoundFxSettingEqualizerType.POP_ROCK);
        mTestArray.add(SoundFxSettingEqualizerType.COMMON_CUSTOM);
        mTestArray.add(SoundFxSettingEqualizerType.COMMON_CUSTOM_2ND);
    }

    /**
     * onTakeViewのテスト
     */
    @Test
    public void testOnTakeView() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onTakeView();

        verify(mView).setAdapter(mTestArray);
        verify(mView).setColor(anyInt());

    }

    /**
     * OnResumeのテスト EventBus未登録の場合
     */
    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        AudioSettingSpec audioSpec = new AudioSettingSpec();
        AudioSettingStatus audioStatus =  new AudioSettingStatus();
        fxSetting.customBandSettingA = new CustomBandSetting(CUSTOM1);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.FLAT;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getAudioSettingStatus()).thenReturn(audioStatus);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);

        verify(mView).setSelectedItem(mTestArray.indexOf(SoundFxSettingEqualizerType.FLAT));
        verify(mView).setPresetView(anyInt());
    }

    /**
     * OnResumeのテスト EventBus登録済の場合
     */
    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        AudioSettingSpec audioSpec = new AudioSettingSpec();
        AudioSettingStatus audioStatus =  new AudioSettingStatus();
        fxSetting.customBandSettingA = new CustomBandSetting(CUSTOM1);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.FLAT;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getAudioSettingStatus()).thenReturn(audioStatus);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onResume();

        verify(mEventBus,times(0)).register(mPresenter);
        verify(mView).setSelectedItem(mTestArray.indexOf(SoundFxSettingEqualizerType.FLAT));
        verify(mView).setPresetView(anyInt());
    }

    /**
     * onPauseのテスト
     */
    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    /**
     * onSelectEqTypeActionのテスト
     */
    @Test
    public void testOnSelectEqTypeAction() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.COMMON_CUSTOM;
        fxSetting.customBandSettingA = new CustomBandSetting(CUSTOM1);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onTakeView();
        mPresenter.onSelectEqTypeAction(0);
        verify(mFxCase).setEqualizer(mTestArray.get(0));
    }

    /**
     * onEqualizerSettingChangeEventのテスト
     */
    @Test
    public void testOnEqualizerSettingChangeEvent() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        EqualizerSettingChangeEvent event = mock(EqualizerSettingChangeEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.COMMON_CUSTOM;
        fxSetting.customBandSettingA = new CustomBandSetting(CUSTOM1);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onTakeView();
        mPresenter.onEqualizerSettingChangeEvent(event);

        verify(mView,times(1)).setSelectedItem(mTestArray.indexOf(SoundFxSettingEqualizerType.COMMON_CUSTOM));
        verify(mView,times(1)).setCustomView(any(float[].class));
    }

    /**
     * onQuickSetActionのテスト
     */
    @Test
    public void testOnQuickSetAction() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        AudioSettingStatus audioStatus =  new AudioSettingStatus();
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.COMMON_CUSTOM;
        fxSetting.customBandSettingA = new CustomBandSetting(CUSTOM1);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getAudioSettingStatus()).thenReturn(audioStatus);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onTakeView();
        mPresenter.onResume();
        mPresenter.onQuickSetAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.EQ_QUICK_SETTING));
        assertThat(EqSettingParams.from(capturedEvent.args).customType, is(CUSTOM1));
    }

    /**
     * onProSetActionのテスト
     */
    @Test
    public void testOnProSetAction() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        AudioSettingStatus audioStatus =  new AudioSettingStatus();
        fxSetting.soundFxSettingEqualizerType = SoundFxSettingEqualizerType.COMMON_CUSTOM_2ND;
        fxSetting.customBandSettingB = new CustomBandSetting(CUSTOM2);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = mTestArray;
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getAudioSettingStatus()).thenReturn(audioStatus);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onTakeView();
        mPresenter.onResume();
        mPresenter.onProSetAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.EQ_PRO_SETTING));
        assertThat(EqSettingParams.from(capturedEvent.args).customType, is(CUSTOM2));
    }

    private Bundle createSettingsParams(String pass) {
        SettingsParams params = new SettingsParams();
        params.pass = pass;
        return params.toBundle();
    }

    private Bundle createEqSettingsParams(Bundle args, CustomEqType type) {
        EqSettingParams params = new EqSettingParams();
        params.customType = type;
        return params.toBundle(args);
    }
}