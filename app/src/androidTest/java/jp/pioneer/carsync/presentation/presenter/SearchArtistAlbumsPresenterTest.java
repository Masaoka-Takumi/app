package jp.pioneer.carsync.presentation.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v7.view.menu.MenuView;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.QueryAppMusic;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.AlbumsView;
import jp.pioneer.carsync.presentation.view.SongsView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * アーティスト指定アルバムリスト画面（検索用）のpresenterテスト
 */
public class SearchArtistAlbumsPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SearchArtistAlbumsPresenter mPresenter = new SearchArtistAlbumsPresenter();
    @Mock AlbumsView mView;
    @Mock EventBus mEventBus;
    @Mock QueryAppMusic mQueryCase;
    @Mock ControlAppMusicSource mControlAppMusicSource;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testOnCreateLoader() throws Exception {
        MusicParams params = new MusicParams();
        params.pass = "testArtist";
        params.artistId = 1;

        mPresenter.setArguments(params.toBundle());
        mPresenter.onCreateLoader(1, Bundle.EMPTY);

        verify(mQueryCase).execute(any(QueryParams.class));
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager mockLoader = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockLoader);

        verify(mockLoader).initLoader(eq(2), any(Bundle.class), any(SearchArtistAlbumsPresenter.class));
    }

    @Test
    public void testOnLoadFinished() throws Exception {
        MusicParams params = new MusicParams();
        params.pass = "testArtist";
        params.artistId = 1;
        AppMusicCursorLoader mockLoader = mock(AppMusicCursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        Bundle args = new Bundle();

        when(mockLoader.getExtras()).thenReturn(args);

        mPresenter.setArguments(params.toBundle());
        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setAlbumCursor(eq(mockCursor), eq(args));
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        MusicParams params = new MusicParams();
        params.pass = "testArtist";
        params.artistId = 1;
        AppMusicCursorLoader mockLoader = mock(AppMusicCursorLoader.class);

        mPresenter.setArguments(params.toBundle());
        mPresenter.takeView(mView);
        mPresenter.onLoaderReset(mockLoader);

        verify(mView).setAlbumCursor(isNull(), eq(Bundle.EMPTY));
    }

    @Test
    public void testOnArtistAlbumShufflePlayAction() throws Exception {
        MusicParams params = new MusicParams();
        params.pass = "testArtist";
        params.artistId = 1;

        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.setArguments(params.toBundle());
        mPresenter.takeView(mView);
        mPresenter.onArtistAlbumShufflePlayAction();

        verify(mControlAppMusicSource).play(any(AppMusicContract.PlayParams.class));
        verify(mEventBus).post(argument.capture());
        assertThat(argument.getValue().screenId, is(ScreenId.PLAYER_CONTAINER));
    }

    @Test
    public void testOnArtistAlbumPlayAction() throws Exception {
        MusicParams params = new MusicParams();
        params.pass = "testArtist";
        params.artistId = 1;

        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.setArguments(params.toBundle());
        mPresenter.takeView(mView);
        mPresenter.onArtistAlbumPlayAction(2);

        verify(mControlAppMusicSource).play(any(AppMusicContract.PlayParams.class));
        verify(mEventBus).post(argument.capture());
        assertThat(argument.getValue().screenId, is(ScreenId.PLAYER_CONTAINER));
    }

    @Test
    public void testOnArtistAlbumSongListShowAction() throws Exception {
        MusicParams params = new MusicParams();
        params.pass = "testArtist";
        params.artistId = 1L;
        Cursor mockCursor = mock(Cursor.class);

        when(mockCursor.getColumnIndexOrThrow(any(String.class))).thenReturn(2);
        when(mockCursor.getString(any(Integer.class))).thenReturn("testAlbum");

        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.setArguments(params.toBundle());
        mPresenter.takeView(mView);
        mPresenter.onArtistAlbumSongListShowAction(mockCursor, 2L);

        verify(mEventBus).post(argument.capture());
        assertThat(argument.getValue().screenId, is(ScreenId.SEARCH_MUSIC_ARTIST_ALBUM_SONG_LIST));
        MusicParams params2 = MusicParams.from(argument.getValue().args);
        assertThat(params2.artistId, is(1L));
        assertThat(params2.albumId, is(2L));
    }
}