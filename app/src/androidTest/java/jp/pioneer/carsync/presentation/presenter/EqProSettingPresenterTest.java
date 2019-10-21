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

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.AudioSettingSpec;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CustomBandSetting;
import jp.pioneer.carsync.domain.model.CustomEqType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.EqProSettingView;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EQ Pro SettingのPresenterのテスト
 */
public class EqProSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks EqProSettingPresenter mPresenter = new EqProSettingPresenter();
    @Mock EqProSettingView mView;
    @Mock PreferSoundFx mFxCase;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock AppSharedPreference mPreference;
    @Mock Context mContext;
    private static final int BAND_DATA_COUNT = 31; //全Band数
    private CustomEqType mCustomType = CustomEqType.CUSTOM1;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    /**
     * OnResumeのテスト EventBus未登録の場合
     */
    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        AudioSettingSpec audioSpec = new AudioSettingSpec();
        AudioSettingStatus audioStatus =  new AudioSettingStatus();
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.customBandSettingA = new CustomBandSetting(CUSTOM1);
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getAudioSettingStatus()).thenReturn(audioStatus);
        when(mGetStatusHolder.execute()).thenReturn(holder);

        Bundle args = new Bundle();
        createEqSettingsParams(args,CUSTOM1);
        mPresenter.setCustomType(args);
        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);
        verify(mView).setColor(anyInt());
        verify(mView).setBandData(any(float[].class));
    }

    /**
     * OnResumeのテスト EventBus登録済の場合
     */
    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        AudioSettingSpec audioSpec = new AudioSettingSpec();
        AudioSettingStatus audioStatus =  new AudioSettingStatus();
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.customBandSettingB = new CustomBandSetting(CUSTOM2);
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(holder.getAudioSettingStatus()).thenReturn(audioStatus);
        when(mGetStatusHolder.execute()).thenReturn(holder);

        Bundle args = new Bundle();
        createEqSettingsParams(args,CUSTOM2);
        mPresenter.setCustomType(args);
        mPresenter.onResume();
        verify(mEventBus,times(0)).register(mPresenter);
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
     * setCustomTypeのテスト
     */
    @Test
    public void testSetCustomType() throws Exception {
        Bundle args = new Bundle();
        createEqSettingsParams(args,CUSTOM2);
        mPresenter.setCustomType(args);
        assertThat(mPresenter.getCustomType(),is(CUSTOM2));
    }

    /**
     * onChangeBandValueActionのテスト
     */
    @Test
    public void testOnChangeBandValueAction() throws Exception {
        float[] bands = new float[]{
                0, 2, 4, 8, 12, 10, 8, 6, 3, 0, -1, -2, -5, -9, -10 ,-12, -10, -9,-5 ,-3, -1, 0, 3, 6, 8, 10, 12, 8, 4, 2, 0
        };
        mPresenter.onChangeBandValueAction(bands);
        verify(mFxCase).setCustomBand(any(CustomEqType.class), eq(bands));
    }

    /**
     * onSoundFxChangeEventのテスト
     */
    @Test
    public void testOnSoundFxChangeEvent() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.customBandSettingA = new CustomBandSetting(CUSTOM1);
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetStatusHolder.execute()).thenReturn(holder);

        Bundle args = new Bundle();
        createEqSettingsParams(args,CUSTOM1);
        mPresenter.setCustomType(args);
        mPresenter.onSoundFxChangeEvent(null);

        verify(mView).setBandData(any(float[].class));
    }

    /**
     * onZoomActionのテスト
     */
    @Test
    public void testOnZoomAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Bundle args = createSettingsParams("Custom1 Pro Setting");
        args = createEqSettingsParams(args,CUSTOM1);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        mPresenter.setCustomType(args);
        mPresenter.onZoomAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.EQ_PRO_SETTING_ZOOM));
        assertThat(EqSettingParams.from(capturedEvent.args).customType, is(CUSTOM1));
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