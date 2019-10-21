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
import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingSpec;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.view.DimmerSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ディマー設定presenterのテスト
 */
public class DimmerSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks DimmerSettingPresenter mPresenter = new DimmerSettingPresenter();
    @Mock DimmerSettingView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock AppSharedPreference mPreference;
    @Mock GetStatusHolder mGetStatusHolder;
    @Mock PreferIllumination mPreferIllumination;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testLifecycle() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        IlluminationSettingSpec illumiSpec = new IlluminationSettingSpec();
        IlluminationSettingStatus status = new IlluminationSettingStatus();
        IlluminationSetting illumi = new IlluminationSetting();
        illumi.dimmerSetting.setValue(DimmerSetting.Dimmer.OFF, 18, 0, 6, 0);
        spec.illuminationSettingSpec = illumiSpec;
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mPreference.getUiColor()).thenReturn(UiColor.RED);
        when(mockHolder.getIlluminationSetting()).thenReturn(illumi);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(status);
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
        when(mEventBus.isRegistered(any(DimmerSettingPresenter.class))).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

        verify(mView).setSelectedItem(0);
        verify(mView).setDimmerSchedule(18,0, 6,0);
        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnIlluminationSettingChangeEvent() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting illumi = new IlluminationSetting();
        illumi.dimmerSetting.setValue(DimmerSetting.Dimmer.OFF, 18, 0, 6, 0);

        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mPreference.getUiColor()).thenReturn(UiColor.RED);
        when(mockHolder.getIlluminationSetting()).thenReturn(illumi);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onIlluminationSettingChangeEvent(new IlluminationSettingChangeEvent());

        verify(mView, times(1)).setSelectedItem(0);
        verify(mView, times(1)).setDimmerSchedule(18,0, 6,0);
    }

    @Test
    public void testOnIlluminationSettingStatusChangeEvent() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting illumi = new IlluminationSetting();
        illumi.dimmerSetting.setValue(DimmerSetting.Dimmer.OFF, 18, 0, 6, 0);
        CarDeviceSpec spec = mock(CarDeviceSpec.class);
        IlluminationSettingSpec illumiSpec = new IlluminationSettingSpec();
        IlluminationSettingStatus status = new IlluminationSettingStatus();
        spec.illuminationSettingSpec = illumiSpec;
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(status);
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mPreference.getUiColor()).thenReturn(UiColor.RED);
        when(mockHolder.getIlluminationSetting()).thenReturn(illumi);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onIlluminationSettingChangeEvent(new IlluminationSettingChangeEvent());

        verify(mView, times(1)).setSelectedItem(0);
        verify(mView, times(1)).setDimmerSchedule(18,0, 6,0);
    }

    @Test
    public void testOnSelectDimmerAction() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting illumi = mock(IlluminationSetting.class);
        DimmerSetting setting = mock(DimmerSetting.class);
        setting.dimmer = DimmerSetting.Dimmer.OFF;
        illumi.dimmerSetting = setting;

        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSetting()).thenReturn(illumi);

        mPresenter.onSelectDimmerAction(0);
        verify(mPreferIllumination).setDimmer(DimmerSetting.Dimmer.OFF);
    }

    @Test
    public void testOnDimmerStartTimeAction() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting illumi = new IlluminationSetting();
        illumi.dimmerSetting.setValue(DimmerSetting.Dimmer.OFF, 18, 0, 6, 0);

        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mPreference.getUiColor()).thenReturn(UiColor.RED);
        when(mockHolder.getIlluminationSetting()).thenReturn(illumi);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onSelectDimmerAction(4);

        verify(mView).showTimePickerDialog(DimmerTimeType.START_TIME, 18, 0);
    }

    @Test
    public void testOnDimmerEndTimeAction() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting illumi = new IlluminationSetting();
        illumi.dimmerSetting.setValue(DimmerSetting.Dimmer.OFF, 18, 0, 6, 0);

        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        when(mPreference.getUiColor()).thenReturn(UiColor.RED);
        when(mockHolder.getIlluminationSetting()).thenReturn(illumi);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onSelectDimmerAction(5);

        verify(mView).showTimePickerDialog(DimmerTimeType.END_TIME, 6, 0);
    }

    @Test
    public void testOnSelectDimmerTimeAction() throws Exception {
        mPresenter.onSelectDimmerTimeAction(DimmerTimeType.START_TIME, 16, 0);
        verify(mPreferIllumination).setDimmerTime(DimmerTimeType.START_TIME, 16, 0);
    }
}