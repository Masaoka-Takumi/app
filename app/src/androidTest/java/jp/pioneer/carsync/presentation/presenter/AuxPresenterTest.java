package jp.pioneer.carsync.presentation.presenter;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.presentation.view.AuxView;

import static android.support.test.InstrumentationRegistry.getTargetContext;

/**
 * AuxPresenterのテスト
 */
public class AuxPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks AuxPresenter mPresenter = new AuxPresenter();
    @Mock EventBus mEventBus;
    @Mock AuxView mView;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnSelectSourceAction() throws Exception {
        mPresenter.onSelectSourceAction();
    }

}