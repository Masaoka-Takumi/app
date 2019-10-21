package jp.pioneer.carsync.presentation.presenter;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.event.RadioFunctionSettingChangeEvent;
import jp.pioneer.carsync.domain.event.RadioFunctionSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferRadioFunction;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.FMTunerSetting;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.TunerFunctionSetting;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingSpec;
import jp.pioneer.carsync.domain.model.TunerFunctionSettingStatus;
import jp.pioneer.carsync.presentation.view.RadioFunctionSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/07/19.
 */
public class RadioFunctionSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks RadioFunctionSettingPresenter mPresenter;
    @Mock GetStatusHolder mGetCase;
    @Mock EventBus mEventBus;
    @Mock PreferRadioFunction mPreferCase;
    @Mock RadioFunctionSettingView mView;
    @Mock StatusHolder mStatusHolder;
    CarDeviceSpec mCarDeviceSpec;
    TunerFunctionSettingSpec mSpec;
    TunerFunctionSettingStatus mStatus;
    TunerFunctionSetting mSetting;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mCarDeviceSpec = new CarDeviceSpec();
        mSpec = new TunerFunctionSettingSpec();
        mStatus = new TunerFunctionSettingStatus();
        mSetting = new TunerFunctionSetting();
        mCarDeviceSpec.tunerFunctionSettingSpec = mSpec;
        mSpec.fmSettingSupported = true;
        mSpec.regSettingSupported = true;
        mSpec.localSettingSupported = true;
        mSpec.afSettingSupported = true;
        mSpec.newsSettingSupported = true;
        mSpec.alarmSettingSupported = true;
        mSpec.pchManualSupported = true;
        mStatus.fmSettingEnabled = true;
        mStatus.regSettingEnabled = true;
        mStatus.localSettingEnabled = true;
        mStatus.afSettingEnabled = true;
        mStatus.newsSettingEnabled = true;
        mStatus.alarmSettingEnabled = true;
        mStatus.pchManualEnabled = true;
        mSetting.fmTunerSetting = FMTunerSetting.STANDARD;
        mSetting.regSetting = true;
        mSetting.localSetting = LocalSetting.OFF;
        mSetting.afSetting = true;
        mSetting.newsSetting = true;
        mSetting.alarmSetting = true;
        mSetting.pchManualSetting = PCHManualSetting.MANUAL;

        when(mGetCase.execute()).thenReturn(mStatusHolder);
        when(mStatusHolder.getTunerFunctionSetting()).thenReturn(mSetting);
        when(mStatusHolder.getTunerFunctionSettingStatus()).thenReturn(mStatus);
    }

    @Test
    public void lifecycle() throws Exception {
        // exercise
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

        // verify
        verify(mView).setFmTunerSetting(true,true, FMTunerSetting.STANDARD);
        verify(mView).setRegionSetting(true,true,true);
        verify(mView).setLocalSetting(true,true,LocalSetting.OFF);
        verify(mView).setAfSetting(true,true,true);
        verify(mView).setNewsSetting(true,true,true);
        verify(mView).setAlarmSetting(true,true,true);
        verify(mView).setPchManual(true,true,PCHManualSetting.MANUAL);

        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void onRadioFunctionSettingChangeEvent() throws Exception {
        // exercise
        mPresenter.takeView(mView);
        mPresenter.onRadioFunctionSettingChangeEvent(new RadioFunctionSettingChangeEvent());

        // verify
        verify(mView).setFmTunerSetting(true,true, FMTunerSetting.STANDARD);
        verify(mView).setRegionSetting(true,true,true);
        verify(mView).setLocalSetting(true,true,LocalSetting.OFF);
        verify(mView).setAfSetting(true,true,true);
        verify(mView).setNewsSetting(true,true,true);
        verify(mView).setAlarmSetting(true,true,true);
        verify(mView).setPchManual(true,true,PCHManualSetting.MANUAL);
    }

    @Test
    public void onRadioFunctionSettingStatusChangeEvent() throws Exception {
        // exercise
        mPresenter.takeView(mView);
        mPresenter.onRadioFunctionSettingStatusChangeEvent(new RadioFunctionSettingStatusChangeEvent());

        // verify
        verify(mView).setFmTunerSetting(true,true, FMTunerSetting.STANDARD);
        verify(mView).setRegionSetting(true,true,true);
        verify(mView).setLocalSetting(true,true,LocalSetting.OFF);
        verify(mView).setAfSetting(true,true,true);
        verify(mView).setNewsSetting(true,true,true);
        verify(mView).setAlarmSetting(true,true,true);
        verify(mView).setPchManual(true,true,PCHManualSetting.MANUAL);
    }

    @Test
    public void onSelectFmTunerSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectFmTunerSettingAction(FMTunerSetting.MUSIC);

        // verify
        verify(mPreferCase).setFmTuner(FMTunerSetting.MUSIC);
    }

    @Test
    public void onSelectRegionSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectRegionSettingAction(true);

        // verify
        verify(mPreferCase).setReg(true);
    }

    @Test
    public void onSelectLocalSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectLocalSettingAction(LocalSetting.LEVEL2);

        // verify
        verify(mPreferCase).setLocal(LocalSetting.LEVEL2);
    }

    @Test
    public void onSelectAfSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectAfSettingAction(true);

        // verify
        verify(mPreferCase).setAf(true);
    }

    @Test
    public void onSelectNewsSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectNewsSettingAction(true);

        // verify
        verify(mPreferCase).setNews(true);
    }

    @Test
    public void onSelectAlarmSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectAlarmSettingAction(true);

        // verify
        verify(mPreferCase).setAlarm(true);
    }

    @Test
    public void onSelectPchManualSettingAction() throws Exception {
        // exercise
        mPresenter.onSelectPchManualSettingAction();

        // verify
        verify(mPreferCase).togglePchManual();
    }

}