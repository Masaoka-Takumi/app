package jp.pioneer.carsync.presentation.presenter;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.event.InitialSettingChangeEvent;
import jp.pioneer.carsync.domain.event.InitialSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferInitial;
import jp.pioneer.carsync.domain.model.AmStep;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.FmStep;
import jp.pioneer.carsync.domain.model.InitialSetting;
import jp.pioneer.carsync.domain.model.InitialSettingSpec;
import jp.pioneer.carsync.domain.model.InitialSettingStatus;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.RearOutputSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.InitialSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/13.
 */
public class InitialSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks InitialSettingPresenter mPresenter;
    @Mock InitialSettingView mView;
    @Mock GetStatusHolder mGetCase;
    @Mock EventBus mEventBus;
    @Mock PreferInitial mPreferCase;

    StatusHolder mStatusHolder = mock(StatusHolder.class);
    CarDeviceSpec mCarDeviceSpec = new CarDeviceSpec();
    InitialSettingSpec mInitialSettingSpec = new InitialSettingSpec();
    InitialSetting mInitialSetting = new InitialSetting();
    InitialSettingStatus mInitialSettingStatus = new InitialSettingStatus();

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mCarDeviceSpec.initialSettingSpec = mInitialSettingSpec;
        mInitialSettingSpec.menuDisplayLanguageSettingSupported = true;
        mInitialSettingSpec.fmStepSettingSupported = true;
        mInitialSettingSpec.amStepSettingSupported = true;
        mInitialSettingSpec.rearOutputSettingSupported = true;
        mInitialSettingSpec.rearOutputPreoutOutputSettingSupported = true;
        mInitialSettingStatus.menuDisplayLanguageSettingEnabled = true;
        mInitialSettingStatus.fmStepSettingEnabled = true;
        mInitialSettingStatus.amStepSettingEnabled = true;
        mInitialSettingStatus.rearOutputPreoutOutputSettingEnabled = true;
        mInitialSettingStatus.rearOutputSettingEnabled = true;
        mInitialSetting.menuDisplayLanguageType = MenuDisplayLanguageType.CANADIAN_FRENCH;
        mInitialSetting.fmStep = FmStep._50KHZ;
        mInitialSetting.amStep = AmStep._10KHZ;
        mInitialSetting.rearOutputPreoutOutputSetting = RearOutputPreoutOutputSetting.REAR_SUBWOOFER;
        mInitialSetting.rearOutputSetting = RearOutputSetting.SUBWOOFER;

        when(mGetCase.execute()).thenReturn(mStatusHolder);
        when(mStatusHolder.getInitialSetting()).thenReturn(mInitialSetting);
        when(mStatusHolder.getInitialSettingStatus()).thenReturn(mInitialSettingStatus);
        when(mStatusHolder.getCarDeviceSpec()).thenReturn(mCarDeviceSpec);
    }

    @Test
    public void testLifecycle() throws Exception {
        // setup
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);

        // exercise
        mPresenter.onInitialize();
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

        // verify
        verify(mView).setMenuDisplayLanguageSetting(mInitialSettingSpec.menuDisplayLanguageSettingSupported,mInitialSettingStatus.menuDisplayLanguageSettingEnabled, null, mInitialSetting.menuDisplayLanguageType);
        verify(mView).setFmStepSetting(mInitialSettingSpec.fmStepSettingSupported,mInitialSettingStatus.fmStepSettingEnabled,mInitialSetting.fmStep);
        verify(mView).setAmStepSetting(mInitialSettingSpec.amStepSettingSupported,mInitialSettingStatus.amStepSettingEnabled, mInitialSetting.amStep);
        verify(mView).setRearOutputPreoutOutputSetting(mInitialSettingSpec.rearOutputPreoutOutputSettingSupported,mInitialSettingStatus.rearOutputPreoutOutputSettingEnabled, mInitialSetting.rearOutputPreoutOutputSetting);
        verify(mView).setRearOutputSetting(mInitialSettingSpec.rearOutputSettingSupported,mInitialSettingStatus.rearOutputSettingEnabled, mInitialSetting.rearOutputSetting);

        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void onInitialSettingChangeEvent() throws Exception {
        // exercise
        mPresenter.onInitialSettingChangeEvent(new InitialSettingChangeEvent());

        // verify
        verify(mView).setMenuDisplayLanguageSetting(mInitialSettingSpec.menuDisplayLanguageSettingSupported,mInitialSettingStatus.menuDisplayLanguageSettingEnabled, null, mInitialSetting.menuDisplayLanguageType);
        verify(mView).setFmStepSetting(mInitialSettingSpec.fmStepSettingSupported,mInitialSettingStatus.fmStepSettingEnabled,mInitialSetting.fmStep);
        verify(mView).setAmStepSetting(mInitialSettingSpec.amStepSettingSupported,mInitialSettingStatus.amStepSettingEnabled, mInitialSetting.amStep);
        verify(mView).setRearOutputPreoutOutputSetting(mInitialSettingSpec.rearOutputPreoutOutputSettingSupported,mInitialSettingStatus.rearOutputPreoutOutputSettingEnabled, mInitialSetting.rearOutputPreoutOutputSetting);
        verify(mView).setRearOutputSetting(mInitialSettingSpec.rearOutputSettingSupported,mInitialSettingStatus.rearOutputSettingEnabled, mInitialSetting.rearOutputSetting);
    }

    @Test
    public void onSystemSettingStatusChangeEvent() throws Exception {
        // exercise
        mPresenter.onInitialSettingStatusChangeEvent(new InitialSettingStatusChangeEvent());

        // verify
        verify(mView).setMenuDisplayLanguageSetting(mInitialSettingSpec.menuDisplayLanguageSettingSupported,mInitialSettingStatus.menuDisplayLanguageSettingEnabled, null, mInitialSetting.menuDisplayLanguageType);
        verify(mView).setFmStepSetting(mInitialSettingSpec.fmStepSettingSupported,mInitialSettingStatus.fmStepSettingEnabled,mInitialSetting.fmStep);
        verify(mView).setAmStepSetting(mInitialSettingSpec.amStepSettingSupported,mInitialSettingStatus.amStepSettingEnabled, mInitialSetting.amStep);
        verify(mView).setRearOutputPreoutOutputSetting(mInitialSettingSpec.rearOutputPreoutOutputSettingSupported,mInitialSettingStatus.rearOutputPreoutOutputSettingEnabled, mInitialSetting.rearOutputPreoutOutputSetting);
        verify(mView).setRearOutputSetting(mInitialSettingSpec.rearOutputSettingSupported,mInitialSettingStatus.rearOutputSettingEnabled, mInitialSetting.rearOutputSetting);

    }

    @Test
    public void onSelectMenuDisplayLanguage() throws Exception {
        // exercise
        mPresenter.onSelectMenuDisplayLanguage(MenuDisplayLanguageType.RUSSIAN);

        // verify
        verify(mPreferCase).setMenuDisplayLanguage(MenuDisplayLanguageType.RUSSIAN);
    }

    @Test
    public void onSelectFmStepSetting() throws Exception {
        // exercise
        mPresenter.onSelectFmStepSetting();

        // verify
        verify(mPreferCase).toggleFmStep();
    }

    @Test
    public void onSelectAmStepSetting() throws Exception {
        // exercise
        mPresenter.onSelectAmStepSetting();

        // verify
        verify(mPreferCase).toggleAmStep();
    }

    @Test
    public void onSelectRearOutputPreoutOutput() throws Exception {
        // exercise
        mPresenter.onSelectRearOutputPreoutOutput();

        // verify
        verify(mPreferCase).toggleRearOutputPreoutOutput();
    }

    @Test
    public void onSelectRearOutput() throws Exception {
        // exercise
        mPresenter.onSelectRearOutput();

        // verify
        verify(mPreferCase).toggleRearOutput();
    }

}