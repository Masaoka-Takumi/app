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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.QueryAppMusic;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AlbumsView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

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
 * AlbumsPresenterのテスト
 */
public class AlbumsPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks AlbumsPresenter mPresenter = new AlbumsPresenter();
    @Mock AlbumsView mView;
    @Mock EventBus mEventBus;
    @Mock QueryAppMusic mQueryCase;
    @Mock ControlAppMusicSource mControlAppMusicSource;
    @Mock AppSharedPreference mPreference;
    @Mock ControlMediaList mControlMediaList;
    private static final int LOADER_ID_ALBUM = 2;
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
        Bundle args = new Bundle();
        AppMusicCursorLoader appMusicCursorLoader = mock(AppMusicCursorLoader.class);
        QueryParams params = AppMusicContract.QueryParamsBuilder.createAllAlbums();
        when(mQueryCase.execute(params)).thenReturn(appMusicCursorLoader);
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);
        assertThat(loader, is(appMusicCursorLoader));
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);
        mLoaderManager = loaderManager;
        mPresenter.setLoaderManager(loaderManager);
        verify(mLoaderManager).initLoader(LOADER_ID_ALBUM, Bundle.EMPTY, mPresenter);
    }

    @Test
    public void testOnLoadFinished() throws Exception {
        AppMusicCursorLoader appMusicCursorLoader = mock(AppMusicCursorLoader.class);
        Cursor cursor = mock(Cursor.class);
        Bundle args = new Bundle();
        when(appMusicCursorLoader.getExtras()).thenReturn(args);
        mPresenter.onLoadFinished(appMusicCursorLoader, cursor);
        verify(mView).setAlbumCursor(cursor, args);
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        CursorLoader cursorLoader = mock(CursorLoader.class);
        Cursor cursor = mock(Cursor.class);
        when(cursor.isClosed()).thenReturn(false);
        mPresenter.onLoaderReset(cursorLoader);
        verify(mView, times(0)).setAlbumCursor(cursor, Bundle.EMPTY);
    }

    @Test
    public void testIsAlbumArtEnabled() throws Exception {
        when(mPreference.isAlbumArtEnabled()).thenReturn(true);
        mPresenter.isAlbumArtEnabled();
    }

    @Test
    public void testOnAlbumPlayAction() throws Exception {
        long id = 1;
        QueryParams params = AppMusicContract.QueryParamsBuilder.createSongsForAlbum(id);
        AppMusicContract.PlayParams playParams = createPlayParams(params);
        mControlAppMusicSource.play(playParams);
        mPresenter.onAlbumPlayAction(id);
        verify(mControlMediaList).exitList();
    }

    @Test
    public void testOnAlbumSongListShowAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        long id = 1;
        MusicParams params = mock(MusicParams.class);
        Cursor cursor = mock(Cursor.class);
        mPresenter.onAlbumSongListShowAction(cursor, id);
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, is(ScreenId.ALBUM_SONG_LIST));
    }

}