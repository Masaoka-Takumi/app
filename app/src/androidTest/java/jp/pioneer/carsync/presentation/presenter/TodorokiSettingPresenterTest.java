package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.event.SuperTodorokiSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.presentation.view.TodorokiSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/07/24.
 */
public class TodorokiSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks TodorokiSettingPresenter mPresenter = new TodorokiSettingPresenter();
    @Mock TodorokiSettingView mView;
    @Mock PreferSoundFx mFxCase;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock AppSharedPreference mPreference;
    @Mock Context mContext;


    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    /**
     * onTakeViewのテスト
     */
    @Test
    public void testOnTakeView() throws Exception {
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mPreference.getUiColor()).thenReturn(AQUA);

        mPresenter.onTakeView();

        verify(mView).setAdapter(any());
        verify(mView).setColor(anyInt());
    }

    /**
     * OnResumeのテスト EventBus未登録の場合
     */
    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        SoundFxSettingSpec fxSpec = new SoundFxSettingSpec();
        SoundFxSettingStatus status = new SoundFxSettingStatus();
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.OFF;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getSoundFxSettingStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);

        verify(mView).setSelectedItem(SuperTodorokiSetting.OFF.ordinal());
        verify(mView).setPresetView(anyInt());
    }

    /**
     * OnResumeのテスト EventBus登録済の場合
     */
    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        SoundFxSettingSpec fxSpec = new SoundFxSettingSpec();
        SoundFxSettingStatus status = new SoundFxSettingStatus();
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.OFF;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getSoundFxSettingStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onResume();

        verify(mEventBus,times(0)).register(mPresenter);
        verify(mView).setSelectedItem(SuperTodorokiSetting.OFF.ordinal());
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
     * onSelectTodorokiTypeActionのテスト
     */
    @Test
    public void testnSelectTodorokiTypeAction() throws Exception {
        mPresenter.onSelectTodorokiTypeAction(0);
        verify(mFxCase).setSuperTodoroki(any(SuperTodorokiSetting.class));
    }

    /**
     * onSoundFxChangeEventのテスト
     */
    @Test
    public void testOnSuperTodorokiSettingChangeEvent() throws Exception {
        SuperTodorokiSettingChangeEvent event = mock(SuperTodorokiSettingChangeEvent.class);
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.superTodorokiSetting = SuperTodorokiSetting.LOW;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onSuperTodorokiSettingChangeEvent(event);

        verify(mView).setSelectedItem(SuperTodorokiSetting.LOW.ordinal());
    }
}