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

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.ProtocolSpec;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.ThemeView;
import jp.pioneer.carsync.presentation.view.argument.IlluminationColorParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * テーマ設定画面presenterのテスト
 */
public class ThemePresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ThemePresenter mPresenter = new ThemePresenter();
    @Mock ThemeView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock PreferIllumination mPreferIllumination;
    @Mock AppSharedPreference mPreference;
    @Mock Context mContext;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testLifecycle() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.illuminationSettingEnabled = true;
        CarDeviceSpec carDeviceSpec = new CarDeviceSpec();
        IlluminationSettingStatus status = new IlluminationSettingStatus();
        IlluminationSetting setting = new IlluminationSetting();
        IlluminationSettingSpec spec = new IlluminationSettingSpec();
        carDeviceSpec.illuminationSettingSpec = spec;
        spec.brightnessSettingSupported = true;
        spec.dispBrightnessSettingSupported = true;
        spec.keyBrightnessSettingSupported = true;
        spec.commonColorSettingSupported = true;
        spec.commonColorCustomSettingSupported = true;
        spec.dispColorSettingSupported = true;
        spec.keyColorSettingSupported = true;
        spec.colorCustomDispSettingSupported = true;
        spec.colorCustomKeySettingSupported = true;
        spec.hotaruNoHikariLikeSettingSupported = true;
        spec.audioLevelMeterLinkedSettingSupported = true;
        spec.customFlashPatternSettingSupported = true;
        status.brightnessSettingEnabled = true;
        status.dispBrightnessSettingEnabled = true;
        status.keyBrightnessSettingEnabled = true;
        status.commonColorSettingEnabled = true;
        status.commonColorCustomSettingEnabled = true;
        status.dispColorSettingEnabled = true;
        status.colorCustomDispSettingEnabled = true;
        status.keyColorSettingEnabled = true;
        status.colorCustomKeySettingEnabled = true;
        status.hotaruNoHikariLikeSettingEnabled = true;
        status.audioLevelMeterLinkedSettingEnabled = true;
        status.customFlashPatternSettingEnabled = true;
        setting.brightnessSetting.min = 1;
        setting.brightnessSetting.max = 10;
        setting.brightnessSetting.current = 5;
        setting.dispBrightnessSetting.min = 1;
        setting.dispBrightnessSetting.max = 10;
        setting.dispBrightnessSetting.current = 5;
        setting.keyBrightnessSetting.min = 1;
        setting.keyBrightnessSetting.max = 10;
        setting.keyBrightnessSetting.current = 5;
        setting.illuminationEffect = true;
        setting.audioLevelMeterLinkedSetting = true;
        ProtocolSpec protocolSpec = mock(ProtocolSpec.class);
        when(protocolSpec.isSphCarDevice()).thenReturn(true);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(status);
        when(mockHolder.getIlluminationSetting()).thenReturn(setting);
        when(mockHolder.getProtocolSpec()).thenReturn(protocolSpec);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mPreference.isLightingEffectEnabled()).thenReturn(true);
        when(mockHolder.getCarDeviceSpec()).thenReturn(carDeviceSpec);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

        verify(mView).setBrightnessSetting(true, true, 1, 10, 5);
        verify(mView).setDisplayBrightnessSetting(true,true,1, 10, 5);
        verify(mView).setKeyBrightnessSetting(true,true,1, 10, 5);
        verify(mView).setThemeSettingEnabled(true);
        verify(mView).setIlluminationSetting(true,true);
        verify(mView).setKeyIlluminationSetting(true,true);
        verify(mView).setDisplayIlluminationSetting(true,true);
        verify(mView).setIlluminationEffectSetting(true,true,true);
        verify(mView).setBgvLinkedSetting(true,true,true);
        verify(mView).setAudioLevelLinkedSetting(true,true,true);
        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnIlluminationSettingChangeEvent() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.illuminationSettingEnabled = true;
        CarDeviceSpec carSpec = new CarDeviceSpec();
        IlluminationSettingSpec illumiSpec = mock(IlluminationSettingSpec.class);
        IlluminationSettingStatus status = new IlluminationSettingStatus();
        illumiSpec.commonColorSettingSupported = true;
        illumiSpec.dispColorSettingSupported = true;
        illumiSpec.keyColorSettingSupported = true;
        illumiSpec.dimmerSettingSupported = true;
        illumiSpec.brightnessSettingSupported = true;
        illumiSpec.dispBrightnessSettingSupported = true;
        illumiSpec.keyBrightnessSettingSupported = true;
        illumiSpec.hotaruNoHikariLikeSettingSupported = true;
        illumiSpec.audioLevelMeterLinkedSettingSupported = true;
        carSpec.illuminationSettingSpec = illumiSpec;

        status.brightnessSettingEnabled = true;
        status.dispBrightnessSettingEnabled = true;
        status.keyBrightnessSettingEnabled = true;
        status.commonColorSettingEnabled = false;
        status.commonColorCustomSettingEnabled = false;
        status.dispColorSettingEnabled = false;
        status.colorCustomDispSettingEnabled = false;
        status.keyColorSettingEnabled = false;
        status.colorCustomKeySettingEnabled = false;
        status.hotaruNoHikariLikeSettingEnabled = false;
        status.audioLevelMeterLinkedSettingEnabled = false;
        status.customFlashPatternSettingEnabled = true;
        IlluminationSetting setting = new IlluminationSetting();
        setting.brightnessSetting.min = 1;
        setting.brightnessSetting.max = 10;
        setting.brightnessSetting.current = 5;
        setting.dispBrightnessSetting.min = 1;
        setting.dispBrightnessSetting.max = 10;
        setting.dispBrightnessSetting.current = 5;
        setting.keyBrightnessSetting.min = 1;
        setting.keyBrightnessSetting.max = 10;
        setting.keyBrightnessSetting.current = 5;
        setting.illuminationEffect = false;
        setting.audioLevelMeterLinkedSetting = false;
        ProtocolSpec spec = mock(ProtocolSpec.class);
        when(spec.isSphCarDevice()).thenReturn(false);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceSpec()).thenReturn(carSpec);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(status);
        when(mockHolder.getIlluminationSetting()).thenReturn(setting);
        when(mockHolder.getProtocolSpec()).thenReturn(spec);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        when(mPreference.isLightingEffectEnabled()).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.onIlluminationSettingChangeEvent(new IlluminationSettingChangeEvent());
        verify(mView, times(2)).setBrightnessSetting(true,true,1,10,5);
        verify(mView, times(2)).setDisplayBrightnessSetting(true,true,1, 10, 5);
        verify(mView, times(2)).setKeyBrightnessSetting(true,true,1, 10, 5);
        verify(mView, times(2)).setThemeSettingEnabled(true);
        verify(mView, times(2)).setIlluminationSetting(true,false);
        verify(mView, times(2)).setDisplayIlluminationSetting(true,false);
        verify(mView, times(2)).setKeyIlluminationSetting(true,false);

        verify(mView, times(2)).setIlluminationEffectSetting(true,false,false);
        verify(mView, times(2)).setBgvLinkedSetting(false,true,false);
        verify(mView, times(2)).setAudioLevelLinkedSetting(true,false,false);
    }

    @Test
    public void testOnIlluminationSettingStatusChangeEvent() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus mockStatus = mock(CarDeviceStatus.class);
        mockStatus.illuminationSettingEnabled = true;
        CarDeviceSpec carSpec = new CarDeviceSpec();
        IlluminationSettingSpec illumiSpec = mock(IlluminationSettingSpec.class);
        IlluminationSettingStatus status = new IlluminationSettingStatus();
        illumiSpec.commonColorSettingSupported = true;
        illumiSpec.dispColorSettingSupported = true;
        illumiSpec.keyColorSettingSupported = true;
        illumiSpec.dimmerSettingSupported = true;
        illumiSpec.brightnessSettingSupported = true;
        illumiSpec.dispBrightnessSettingSupported = true;
        illumiSpec.keyBrightnessSettingSupported = true;
        illumiSpec.hotaruNoHikariLikeSettingSupported = true;
        illumiSpec.audioLevelMeterLinkedSettingSupported = true;
        carSpec.illuminationSettingSpec = illumiSpec;
        status.brightnessSettingEnabled = true;
        status.dispBrightnessSettingEnabled = true;
        status.keyBrightnessSettingEnabled = true;
        status.commonColorSettingEnabled = true;
        status.commonColorCustomSettingEnabled = false;
        status.dispColorSettingEnabled = true;
        status.colorCustomDispSettingEnabled = false;
        status.keyColorSettingEnabled = true;
        status.colorCustomKeySettingEnabled = false;
        status.hotaruNoHikariLikeSettingEnabled = true;
        status.audioLevelMeterLinkedSettingEnabled = true;
        status.customFlashPatternSettingEnabled = true;
        IlluminationSetting setting = new IlluminationSetting();
        setting.brightnessSetting.min = 1;
        setting.brightnessSetting.max = 10;
        setting.brightnessSetting.current = 5;
        setting.dispBrightnessSetting.min = 1;
        setting.dispBrightnessSetting.max = 10;
        setting.dispBrightnessSetting.current = 5;
        setting.keyBrightnessSetting.min = 1;
        setting.keyBrightnessSetting.max = 10;
        setting.keyBrightnessSetting.current = 5;
        setting.illuminationEffect = true;
        setting.audioLevelMeterLinkedSetting = true;
        ProtocolSpec spec = mock(ProtocolSpec.class);
        when(spec.isSphCarDevice()).thenReturn(false);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getCarDeviceSpec()).thenReturn(carSpec);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(status);
        when(mockHolder.getIlluminationSetting()).thenReturn(setting);
        when(mockHolder.getProtocolSpec()).thenReturn(spec);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mPreference.isLightingEffectEnabled()).thenReturn(true);
        when(mockHolder.getCarDeviceStatus()).thenReturn(mockStatus);
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.onIlluminationSettingStatusChangeEvent(new IlluminationSettingStatusChangeEvent());

        verify(mView, times(2)).setBrightnessSetting(true,true,1,10,5);
        verify(mView, times(2)).setDisplayBrightnessSetting(true,true,1, 10, 5);
        verify(mView, times(2)).setKeyBrightnessSetting(true,true,1, 10, 5);
        verify(mView, times(2)).setThemeSettingEnabled(true);
        verify(mView, times(2)).setIlluminationSetting(true,true);
        verify(mView, times(2)).setDisplayIlluminationSetting(true,true);
        verify(mView, times(2)).setKeyIlluminationSetting(true,true);

        verify(mView, times(2)).setIlluminationEffectSetting(true,true,true);
        verify(mView, times(2)).setBgvLinkedSetting(false,true,true);
        verify(mView, times(2)).setAudioLevelLinkedSetting(true,true,true);
    }

    @Test
    public void testOnThemeSetAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onThemeSetAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.THEME_SET_SETTING));
    }

    @Test
    public void testOnIlluminationColorAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onIlluminationColorAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.ILLUMINATION_COLOR_SETTING));

        IlluminationColorParams params = IlluminationColorParams.from(event.args);
        assertThat(params.type, is(IlluminationColorParams.IlluminationType.COMMON));
    }

    @Test
    public void testOnIlluminationDispColorAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onIlluminationDispColorAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.ILLUMINATION_COLOR_SETTING));

        IlluminationColorParams params = IlluminationColorParams.from(event.args);
        assertThat(params.type, is(IlluminationColorParams.IlluminationType.DISP));
    }

    @Test
    public void testOnIlluminationKeyColorAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onIlluminationKeyColorAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.ILLUMINATION_COLOR_SETTING));

        IlluminationColorParams params = IlluminationColorParams.from(event.args);
        assertThat(params.type, is(IlluminationColorParams.IlluminationType.KEY));
    }

    @Test
    public void testOnUiColorAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onUiColorAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.UI_COLOR_SETTING));
    }

    @Test
    public void testOnIlluminationDimmerAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onIlluminationDimmerAction();
        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.ILLUMINATION_DIMMER_SETTING));
    }

    @Test
    public void testOnDisplayBrightnessAction() throws Exception {
        mPresenter.onDisplayBrightnessAction(5);
        verify(mPreferIllumination).setBrightness(IlluminationTarget.DISP, 5);
    }

    @Test
    public void testOnKeyBrightnessAction() throws Exception {
        mPresenter.onKeyBrightnessAction(5);
        verify(mPreferIllumination).setBrightness(IlluminationTarget.KEY, 5);
    }

    @Test
    public void testOnBrightnessAction() throws Exception {
        mPresenter.onBrightnessAction(5);
        verify(mPreferIllumination).setCommonBrightness(5);
    }


    @Test
    public void testOnIlummiFxChange() throws Exception {
        mPresenter.onIllumiFxChange(true);
        verify(mPreferIllumination).setIlluminationEffect(true);
    }

    @Test
    public void testOnIlummiFxWithBgvChange() throws Exception {
        mPresenter.onIllumiFxWithBgvChange(true);
        verify(mPreference).setLightingEffectEnabled(true);
    }

    @Test
    public void testOnIlummiFxWithAudioLevelChange() throws Exception {
        mPresenter.onIllumiFxWithAudioLevelChange(true);
        verify(mPreferIllumination).setAudioLevelMeterLinked(true);
    }
}