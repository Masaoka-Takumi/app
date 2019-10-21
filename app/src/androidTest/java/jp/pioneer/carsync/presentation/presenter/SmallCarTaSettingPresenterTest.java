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

import java.util.ArrayList;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.SmallCarTaSetting;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.domain.model.SoundFxSetting;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.SmallCarTaSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/07/26.
 */
public class SmallCarTaSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SmallCarTaSettingPresenter mPresenter = new SmallCarTaSettingPresenter();
    @Mock SmallCarTaSettingView mView;
    @Mock PreferSoundFx mFxCase;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock AppSharedPreference mPreference;
    @Mock Context mContext;
    private ArrayList<SmallCarTaSettingType> mTypeArray = new ArrayList<SmallCarTaSettingType>(){{
        add(SmallCarTaSettingType.OFF);
        add(SmallCarTaSettingType.A);
        add(SmallCarTaSettingType.B);
        add(SmallCarTaSettingType.C);
    }};

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

        verify(mView).setAdapter(mTypeArray);
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
        fxSetting.smallCarTaSetting = new SmallCarTaSetting();
        fxSetting.smallCarTaSetting.smallCarTaSettingType = SmallCarTaSettingType.OFF;
        fxSetting.smallCarTaSetting.listeningPosition = ListeningPosition.LEFT;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getSoundFxSettingStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);
        verify(mView).setSeatTypeSettingEnabled(false);
        verify(mView).setSelectedItem(SmallCarTaSettingType.OFF.ordinal());
        verify(mView).setPresetView(R.drawable.p0683_pta_none_l);
        verify(mView).setSeatType(fxSetting.smallCarTaSetting.listeningPosition);
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
        fxSetting.smallCarTaSetting = new SmallCarTaSetting();
        fxSetting.smallCarTaSetting.smallCarTaSettingType = SmallCarTaSettingType.OFF;
        fxSetting.smallCarTaSetting.listeningPosition = ListeningPosition.LEFT;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(holder.getSoundFxSettingStatus()).thenReturn(status);
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onResume();

        verify(mEventBus,times(0)).register(mPresenter);
        verify(mView).setSeatTypeSettingEnabled(false);
        verify(mView).setSelectedItem(SmallCarTaSettingType.OFF.ordinal());
        verify(mView).setPresetView(R.drawable.p0683_pta_none_l);
        verify(mView).setSeatType(fxSetting.smallCarTaSetting.listeningPosition);
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
     * onSelectSmallCarTaSettingActionのテスト
     */
    @Test
    public void testOnSelectSmallCarTaSettingAction() throws Exception {
        SmallCarTaSetting smallCarTaSetting = mock(SmallCarTaSetting.class);
        smallCarTaSetting.smallCarTaSettingType = SmallCarTaSettingType.OFF;
        smallCarTaSetting.listeningPosition = ListeningPosition.LEFT;
        mPresenter.onSelectSmallCarTaSettingAction(smallCarTaSetting);
        verify(mFxCase).setSmallCarTa(smallCarTaSetting.smallCarTaSettingType,smallCarTaSetting.listeningPosition);
    }

    /**
     * onSoundFxChangeEventのテスト
     */
    @Test
    public void testOnSoundFxChangeEvent() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        SoundFxSetting fxSetting = mock(SoundFxSetting.class);
        fxSetting.smallCarTaSetting = new SmallCarTaSetting();
        fxSetting.smallCarTaSetting.smallCarTaSettingType= SmallCarTaSettingType.A;
        fxSetting.smallCarTaSetting.listeningPosition = ListeningPosition.LEFT;
        when(holder.getSoundFxSetting()).thenReturn(fxSetting);
        when(mGetStatusHolder.execute()).thenReturn(holder);
        mPresenter.onSoundFxChangeEvent(null);

        verify(mView).setSeatTypeSettingEnabled(true);
        verify(mView).setSelectedItem(SmallCarTaSettingType.A.ordinal());
        verify(mView).setPresetView(R.drawable.p0681_pta_suv_l);
        verify(mView).setSeatType(ListeningPosition.LEFT);
    }

}