package jp.pioneer.carsync.presentation.presenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.PreferAudio;
import jp.pioneer.carsync.presentation.view.AudioView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/06/26.
 */
public class AudioPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks AudioPresenter mPresenter = new AudioPresenter();
    @Mock AppSharedPreference mPreference;
    @Mock AudioView mView;
    @Mock PreferAudio mPreferAudio;
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }
    @Test
    public void onTakeView() throws Exception {
        when(mPreference.isSoundRetrieverEnabled()).thenReturn(true);
        when(mPreference.getSourceLevelAdjusterPosition()).thenReturn(1);
        mPresenter.takeView(mView);
        verify(mView).setSoundRetrieverSetting(true);
    }

    @Test
    public void onFaderBalanceAction() throws Exception {
    }

    @Test
    public void onSourceLevelAdjusterAction() throws Exception {
        mPresenter.onSourceLevelAdjusterAction(1);
        verify(mPreference).setSourceLevelAdjusterPosition(1);

    }

    @Test
    public void onAdvancedSettingsAction() throws Exception {

    }

    @Test
    public void onSoundRetrieverChange() throws Exception {
        mPresenter.onSoundRetrieverChange(true);
        verify(mPreference).setSoundRetrieverEnabled(true);
    }

}