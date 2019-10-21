package jp.pioneer.carsync.presentation.presenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.presentation.view.VoiceSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/07/20.
 */
public class VoiceSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks VoiceSettingPresenter mPresenter;
    @Mock AppSharedPreference mPreference;
    @Mock VoiceSettingView mView;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mPreference.isVoiceRecognitionEnabled()).thenReturn(true);
    }

    @Test
    public void lifecycle() throws Exception {
        // exercise
        mPresenter.takeView(mView);
        mPresenter.resume();
        mPresenter.pause();
        mPresenter.destroy();

        // verify
        verify(mView).setVoiceRecognitionEnabled(true);
    }

    @Test
    public void onVoiceRecognitionChange() throws Exception {
        // exercise
        mPresenter.onVoiceRecognitionChange(true);

        // verify
        verify(mPreference).setVoiceRecognitionEnabled(true);
    }

}