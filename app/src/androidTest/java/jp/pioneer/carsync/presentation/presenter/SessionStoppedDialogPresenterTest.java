package jp.pioneer.carsync.presentation.presenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.presentation.view.SessionStoppedDialogView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;

/**
 * Created by NSW00_008320 on 2017/08/02.
 */
public class SessionStoppedDialogPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SessionStoppedDialogPresenter mPresenter;
    @Mock SessionStoppedDialogView mView;

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