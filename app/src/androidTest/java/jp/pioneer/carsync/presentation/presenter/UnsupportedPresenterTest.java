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

import java.util.List;

import jp.pioneer.carsync.presentation.event.BackgroundChangeEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AuxView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * 非対応ソース画面presenterのテスト
 */
public class UnsupportedPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks UnsupportedPresenter mPresenter = new UnsupportedPresenter();
    @Mock EventBus mEventBus;
    @Mock AuxView mView;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testOnSelectSourceAction() throws Exception {
        ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);

        mPresenter.onSelectSourceAction();
        verify(mEventBus, times(2)).post(argument.capture());

        List<Object> captorData = argument.getAllValues();
        // 1回目
        if (captorData.get(0) instanceof BackgroundChangeEvent) {
            final BackgroundChangeEvent event = (BackgroundChangeEvent) captorData.get(0);
            assertTrue(event.isBlur);
        } else {
            fail();
        }
        // 2回目
        if (captorData.get(1) instanceof NavigateEvent) {
            final NavigateEvent event = (NavigateEvent) captorData.get(1);
            assertThat(event.screenId, is(ScreenId.SOURCE_SELECT));
        } else {
            fail();
        }
    }
}