package jp.pioneer.carsync.presentation.presenter;

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

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.ControlSource;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.PreferSoundFx;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.SoundFxSettingSpec;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.SourceOffView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/09/25.
 */
public class SourceOffPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SourceOffPresenter mPresenter = new SourceOffPresenter();
    @Mock SourceOffView mView;
    @Mock EventBus mEventBus;
    @Mock AppSharedPreference mPreference;
    @Mock ControlSource mControlSource;
    @Mock GetStatusHolder mGetCase;
    @Mock PreferSoundFx mFxCase;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void onTakeView() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);

        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceSpec spec = new CarDeviceSpec();
        spec.soundFxSettingSpec =  new SoundFxSettingSpec();
        spec.soundFxSettingSpec.supportedEqualizers = new ArrayList<>();
        when(holder.getCarDeviceSpec()).thenReturn(spec);
        when(mGetCase.execute()).thenReturn(holder);

        mPresenter.onTakeView();
        verify(mView).setColor(R.color.ui_color_aqua);
    }

    @Test
    public void onSelectSourceAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onSelectSourceAction();
        verify(mEventBus,times(2)).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.SOURCE_SELECT));
    }

    @Test
    public void onHomeAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        mPresenter.onHomeAction();
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.HOME_CONTAINER));
    }

}