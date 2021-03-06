package jp.pioneer.carsync.presentation.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.greenrobot.eventbus.EventBus;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.QueryAppMusic;
import jp.pioneer.carsync.presentation.view.SongsView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;

import static android.R.attr.id;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.domain.content.AppMusicContract.PlayParams.createPlayParams;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * SongsPresenterのテスト
 */
public class SongsPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SongsPresenter mPresenter = new SongsPresenter();
    @Mock SongsView mView;
    @Mock EventBus mEventBus;
    @Mock QueryAppMusic mQueryCase;
    @Mock ControlAppMusicSource mControlAppMusicSource;
    @Mock ControlMediaList mControlMediaList;
    private static final int LOADER_ID_MUSIC = 3;
    private LoaderManager mLoaderManager;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnCreateLoader() throws Exception {
        MusicParams musicParams = new MusicParams();
        Bundle args = new Bundle();
        AppMusicCursorLoader appMusicCursorLoader = mock(AppMusicCursorLoader.class);
        QueryParams params = AppMusicContract.QueryParamsBuilder.createAllSongs();
        when(mQueryCase.execute(params)).thenReturn(appMusicCursorLoader);
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);
        assertThat(loader, is(appMusicCursorLoader));
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);
        mLoaderManager = loaderManager;
        mPresenter.setLoaderManager(loaderManager);
        verify(mLoaderManager).initLoader(LOADER_ID_MUSIC, Bundle.EMPTY, mPresenter);
    }

    @Test
    public void testOnLoadFinished() throws Exception {
        AppMusicCursorLoader appMusicCursorLoader = mock(AppMusicCursorLoader.class);
        Cursor cursor = mock(Cursor.class);
        Bundle args = new Bundle();
        when(appMusicCursorLoader.getExtras()).thenReturn(args);
        mPresenter.onLoadFinished(appMusicCursorLoader, cursor);
        verify(mView).setSongCursor(cursor, args);
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor cursor = mock(Cursor.class);
        when(cursor.isClosed()).thenReturn(false);
        mPresenter.onLoaderReset(cursorLoader);
        verify(mView, times(0)).setSongCursor(cursor, Bundle.EMPTY);
    }

    @Test
    public void testOnSongPlayAction() throws Exception {

        MusicParams musicParams = new MusicParams();
        long id = 1;
        QueryParams params = AppMusicContract.QueryParamsBuilder.createAllSongs();
        AppMusicContract.PlayParams playParams = createPlayParams(params, id);
        mControlAppMusicSource.play(playParams);
        mPresenter.onSongPlayAction(id);
        verify(mControlMediaList).exitList();
    }

}