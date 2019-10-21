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

import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.view.SourceSelectContainerView;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/10/24.
 */
public class SourceSelectContainerPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SourceSelectContainerPresenter mPresenter = new SourceSelectContainerPresenter();
    @Mock SourceSelectContainerView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testOnInitialize() throws Exception {
        mPresenter.initialize();
        verify(mView).onNavigate(eq(ScreenId.SOURCE_SELECT), eq(null));
    }

    @Test
    public void testOnBackAction() throws Exception {
        mPresenter.onBackAction();

        verify(mEventBus).post(any(GoBackEvent.class));
    }

}