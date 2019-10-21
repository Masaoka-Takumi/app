package jp.pioneer.carsync.presentation.presenter;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.presentation.view.CautionDialogView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;

/**
 * Created by NSW00_008316 on 2017/04/25.
 */
public class CautionDialogPresenterTest {

    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks CautionDialogPresenter mPresenter;
    @Mock CautionDialogView mView;
    @Mock EventBus mEventBus;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void onConfirmAction() throws Exception {
        mPresenter.takeView(mView);
        mPresenter.onConfirmAction();

        verify(mView).callbackClose();
    }
}