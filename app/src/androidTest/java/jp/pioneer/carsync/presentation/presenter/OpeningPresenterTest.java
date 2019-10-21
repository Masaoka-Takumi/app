package jp.pioneer.carsync.presentation.presenter;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.SessionStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OpeningPresenterのテスト
 */
public class OpeningPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    OpeningPresenter mPresenter;
    @Mock EventBus mEventBus;
    @Mock AppSharedPreference mPreference;
    @Mock GetStatusHolder mGetStatusHolder;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnInitializeHome() throws Exception {
        when(mPreference.isAgreedEulaPrivacyPolicy()).thenReturn(true);

        StatusHolder mockHolder = mock(StatusHolder.class);
        when(mockHolder.getSessionStatus()).thenReturn(SessionStatus.STARTED);
        when(mGetStatusHolder.execute()).thenReturn(mockHolder);
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        doAnswer(
                invocationOnMock -> {
                    NavigateEvent event = (NavigateEvent) invocationOnMock.getArguments()[0];
                    assertThat(event.screenId, is(ScreenId.HOME_CONTAINER));
                    return null;
                }).when(mEventBus).post(ArgumentMatchers.any(NavigateEvent.class));

        instr.runOnMainSync(() -> {
            mPresenter = new OpeningPresenter();
            mPresenter.mEventBus = mEventBus;
            mPresenter.mPreference = mPreference;
            mPresenter.mGetStatusHolder = mGetStatusHolder;
            mPresenter.initialize();
            mPresenter.resume();
        });

        Thread.sleep(2000);
        verify(mEventBus).post(ArgumentMatchers.any(NavigateEvent.class));
    }

    @Test
    public void testOnInitializeTerm() throws Exception {
        when(mPreference.isAgreedEulaPrivacyPolicy()).thenReturn(false);
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            mPresenter = new OpeningPresenter();
            mPresenter.mEventBus = mEventBus;
            mPresenter.mPreference = mPreference;
            mPresenter.mGetStatusHolder = mGetStatusHolder;
            mPresenter.initialize();
            mPresenter.resume();
        });
        Thread.sleep(2000);
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.OPENING_EULA));
    }

}