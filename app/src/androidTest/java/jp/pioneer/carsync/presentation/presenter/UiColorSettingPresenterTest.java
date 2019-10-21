package jp.pioneer.carsync.presentation.presenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.view.UiColorSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * UIカラー設定画面presenterのテスト
 */
public class UiColorSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks UiColorSettingPresenter mPresenter = new UiColorSettingPresenter();
    @Mock UiColorSettingView mView;
    @Mock AppSharedPreference mPreference;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testOnTakeView() throws Exception {
        when(mPreference.getUiColor()).thenReturn(UiColor.RED);

        mPresenter.takeView(mView);
        verify(mView).setColor(anyList());
        verify(mView).setPosition(5, UiColor.RED.getResource());
    }

    @Test
    public void testOnSelectColorItemAction() throws Exception {
        when(mPreference.getUiColor()).thenReturn(UiColor.RED);

        mPresenter.takeView(mView);
        mPresenter.onSelectColorItemAction(0);

        verify(mPreference).setUiColor(UiColor.BLUE);
        verify(mView).setPosition(0, UiColor.BLUE.getResource());
    }
}