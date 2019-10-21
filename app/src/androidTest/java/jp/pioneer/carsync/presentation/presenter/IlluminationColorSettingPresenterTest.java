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

import jp.pioneer.carsync.domain.event.IlluminationSettingChangeEvent;
import jp.pioneer.carsync.domain.event.IlluminationSettingStatusChangeEvent;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferIllumination;
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.domain.model.IlluminationColorMap;
import jp.pioneer.carsync.domain.model.IlluminationColorSpec;
import jp.pioneer.carsync.domain.model.IlluminationSetting;
import jp.pioneer.carsync.domain.model.IlluminationSettingStatus;
import jp.pioneer.carsync.domain.model.IlluminationTarget;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.IlluminationColorModel;
import jp.pioneer.carsync.presentation.view.IlluminationColorSettingView;
import jp.pioneer.carsync.presentation.view.argument.IlluminationColorParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * イルミネーションカラー設定画面presenterのテスト
 */
public class IlluminationColorSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks IlluminationColorSettingPresenter mPresenter = new IlluminationColorSettingPresenter();
    @Mock IlluminationColorSettingView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock PreferIllumination mIllumiCase;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        mPresenter.mModel = new IlluminationColorModel();
    }

    @Test
    public void testLifecycleWhenDisp() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        IlluminationSettingStatus illumiStatus = new IlluminationSettingStatus();
        mockSetting.dispColor = IlluminationColor.WHITE;
        mockSetting.dispColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;

        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(illumiStatus);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.dispColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSpec.isValid()).thenReturn(true);
        when(mEventBus.isRegistered(any(IlluminationColorSettingPresenter.class))).thenReturn(false);

        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

        verify(mView).setColor(anyList());
        verify(mView).setPosition(0);
        verify(mView).setCustomColor(255, 255, 255);
        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testLifecycleWhenKey() throws Exception {
        IlluminationColorParams params = new IlluminationColorParams();
        params.type = IlluminationColorParams.IlluminationType.KEY;

        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        IlluminationSettingStatus illumiStatus = new IlluminationSettingStatus();
        mockSetting.keyColor = IlluminationColor.WHITE;
        mockSetting.keyColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;

        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(illumiStatus);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.keyColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSpec.isValid()).thenReturn(true);
        when(mEventBus.isRegistered(any(IlluminationColorSettingPresenter.class))).thenReturn(false);

        mPresenter.setArgument(params.toBundle());
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

        verify(mView).setColor(anyList());
        verify(mView).setPosition(0);
        verify(mView).setCustomColor(255, 255, 255);
        verify(mEventBus).register(mPresenter);
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnIlluminationSettingChangeEvent() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        mockSetting.dispColor = IlluminationColor.WHITE;
        mockSetting.dispColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;

        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.dispColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSpec.isValid()).thenReturn(true);

        mPresenter.takeView(mView);
        mPresenter.onIlluminationSettingChangeEvent(new IlluminationSettingChangeEvent());

        verify(mView).setColor(anyList());
        verify(mView, times(2)).setPosition(0);
        verify(mView, times(2)).setCustomColor(255, 255, 255);
    }

    @Test
    public void testOnIlluminationSettingStatusChangeEvent() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSettingStatus status = new IlluminationSettingStatus();
        status.commonColorSettingEnabled = false;
        status.commonColorCustomSettingEnabled = false;
        status.dispColorSettingEnabled = false;
        status.colorCustomDispSettingEnabled = false;
        status.keyColorSettingEnabled = false;
        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSettingStatus()).thenReturn(status);
        mPresenter.onIlluminationSettingStatusChangeEvent(new IlluminationSettingStatusChangeEvent());

    }

    @Test
    public void testOnSelectColorItemAction() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        mockSetting.dispColor = IlluminationColor.WHITE;
        mockSetting.dispColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;

        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.dispColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSpec.isValid()).thenReturn(true);

        mPresenter.takeView(mView);
        mPresenter.onSelectColorItemAction(2);

        verify(mView).setPosition(2);
        verify(mIllumiCase).setColor(eq(IlluminationTarget.DISP), any(IlluminationColor.class));
    }

    @Test
    public void testOnSelectCustomItemAction() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        mockSetting.dispColor = IlluminationColor.WHITE;
        mockSetting.dispColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;

        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.dispColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSpec.isValid()).thenReturn(true);

        mPresenter.takeView(mView);
        mPresenter.onSelectCustomItemAction();

        verify(mView).setPosition(-1);
        verify(mIllumiCase).setCustomColor(IlluminationTarget.DISP, 60, 60, 60);
    }

    @Test
    public void testOnCustomColorAction() throws Exception {
        StatusHolder mockHolder = mock(StatusHolder.class);
        IlluminationSetting mockSetting = mock(IlluminationSetting.class);
        mockSetting.dispColor = IlluminationColor.WHITE;
        mockSetting.dispColorSpec = mock(IlluminationColorMap.class);
        IlluminationColorSpec mockSpec = mock(IlluminationColorSpec.class);
        mockSpec.red = 60;
        mockSpec.green = 60;
        mockSpec.blue = 60;

        when(mGetCase.execute()).thenReturn(mockHolder);
        when(mockHolder.getIlluminationSetting()).thenReturn(mockSetting);
        when(mockSetting.dispColorSpec.get(any(IlluminationColor.class))).thenReturn(mockSpec);
        when(mockSpec.isValid()).thenReturn(true);
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mPresenter.takeView(mView);
        mPresenter.onCustomColorAction(255, 255, 255);

        verify(mView, times(2)).setCustomColor(255, 255, 255);
        verify(mIllumiCase).setCustomColor(eq(IlluminationTarget.DISP), anyInt(), anyInt(), anyInt());
    }
}