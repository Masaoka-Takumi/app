package jp.pioneer.carsync.presentation.presenter;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.presentation.view.AppSettingView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AppSettingPresenterのテスト.
 */
public class AppSettingPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks AppSettingPresenter mPresenter = new AppSettingPresenter();
    @Mock AppSharedPreference mPreference;
    @Mock AppSettingView mView;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    /**
     * onTakeViewのテスト
     */
    @Test
    public void testOnTakeView() throws Exception {
        when(mPreference.getDistanceUnit()).thenReturn(DistanceUnit.METER_KILOMETER);
        when(mPreference.isShortCutButtonEnabled()).thenReturn(true);
        when(mPreference.isHomeButtonEnabled()).thenReturn(true);
        when(mPreference.isAlbumArtEnabled()).thenReturn(true);
        when(mPreference.isGenreCardEnabled()).thenReturn(true);
        when(mPreference.isPlaylistCardEnabled()).thenReturn(true);

        mPresenter.takeView(mView);

        verify(mView).setDistanceUnit(DistanceUnit.METER_KILOMETER);
        verify(mView).setShortCutEnabled(true);
        verify(mView).setHomeButtonEnabled(true);
        verify(mView).setAlbumArtEnabled(true);
        verify(mView).setGenreCardEnabled(true);
        verify(mView).setPlaylistCardEnabled(true);
    }

    /**
     * onDistanceUnitChangeのテスト
     */
    @Test
    public void testOnDistanceUnitChange() throws Exception {
        when(mPreference.getDistanceUnit()).thenReturn(DistanceUnit.METER_KILOMETER);

        mPresenter.onDistanceUnitChange();

        verify(mPreference).setDistanceUnit(DistanceUnit.FEET_MILE);
        verify(mView).setDistanceUnit(DistanceUnit.FEET_MILE);
    }

    /**
     * onShortCutButtonChangeのテスト
     */
    @Test
    public void testOnShortCutButtonChange() throws Exception {
        mPresenter.onShortCutButtonChange(true);

        verify(mPreference).setShortCutButtonEnabled(true);
    }

    /**
     * onHomeButtonChangeのテスト
     */
    @Test
    public void testOnHomeButtonChange() throws Exception {
        mPresenter.onHomeButtonChange(true);

        verify(mPreference).setHomeButtonEnabled(true);

    }

    /**
     * onAlbumArtChangeのテスト
     */
    @Test
    public void testOnAlbumArtChange() throws Exception {
        when(mPreference.isAlbumArtEnabled()).thenReturn(true).thenReturn(false);

        mPresenter.onAlbumArtChange();

        verify(mPreference).setAlbumArtEnabled(false);
        verify(mView).setAlbumArtEnabled(false);
    }

    /**
     * onGenreCardChangeのテスト
     */
    @Test
    public void testOnGenreCardChange() throws Exception {
        when(mPreference.isGenreCardEnabled()).thenReturn(true).thenReturn(false);

        mPresenter.onGenreCardChange();

        verify(mPreference).setGenreCardEnabled(false);
        verify(mView).setGenreCardEnabled(false);
    }

    /**
     * onPlaylistChangeのテスト
     */
    @Test
    public void onPlaylistChange() throws Exception {
        when(mPreference.isPlaylistCardEnabled()).thenReturn(true).thenReturn(false);

        mPresenter.onPlaylistChange();

        verify(mPreference).setPlayListViewId(false);
        verify(mView).setPlaylistCardEnabled(false);
    }
}