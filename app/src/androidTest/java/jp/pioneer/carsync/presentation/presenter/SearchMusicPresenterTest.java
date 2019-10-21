package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.greenrobot.eventbus.EventBus;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.R;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.content.QueryParams;
import jp.pioneer.carsync.domain.interactor.CheckAvailableTextToSpeech;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.QueryAppMusic;
import jp.pioneer.carsync.domain.interactor.ReadText;
import jp.pioneer.carsync.domain.model.VoiceCommand;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.SearchMusicView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 音楽検索画面のpresenterテスト
 */
public class SearchMusicPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SearchMusicPresenter mPresenter = new SearchMusicPresenter();
    @Mock SearchMusicView mView;
    @Mock EventBus mEventBus;
    @Mock QueryAppMusic mMusicCase;
    @Mock ControlAppMusicSource mControlAppMusicSource;
    @Mock Context mContext;
    @Mock CheckAvailableTextToSpeech mCheckTtsCase;
    @Mock ReadText mReadText;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testOnCreateLoader() throws Exception {
        AppMusicCursorLoader mockLoader = mock(AppMusicCursorLoader.class);
        Bundle args = new Bundle();
        String[] words = new String[]{"mr.",};
        Bundle params = SearchContentParams.toBundle(VoiceCommand.ARTIST, words);
        LoaderManager mockManager = mock(LoaderManager.class);

        when(mMusicCase.execute(any(QueryParams.class))).thenReturn(mockLoader);

        mPresenter.setArguments(params);
        mPresenter.setLoaderManager(mockManager);
        verify(mockManager).initLoader(eq(1), any(Bundle.class), any(SearchMusicPresenter.class));
        //verify(mockManager).initLoader(eq(2), any(Bundle.class), any(SearchMusicPresenter.class));
        //verify(mockManager).initLoader(eq(3), any(Bundle.class), any(SearchMusicPresenter.class));
        Loader<Cursor> loader1 = mPresenter.onCreateLoader(1, args);
        assertThat(loader1, is(mockLoader));
        Loader<Cursor> loader2 = mPresenter.onCreateLoader(2, args);
        assertThat(loader2, is(mockLoader));
        Loader<Cursor> loader3 = mPresenter.onCreateLoader(3, args);
        assertThat(loader3, is(mockLoader));
        Loader<Cursor> loader0 = mPresenter.onCreateLoader(0, args);
        assertThat(loader0, nullValue());
    }

    @Test
    public void testOnLoadFinishedForArtist() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockLoader.getId()).thenReturn(1);
        when(mockCursor.getCount()).thenReturn(2);

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setArtistCursor(any(Cursor.class));
    }

    @Test
    public void testOnLoadFinishedForArtistOne() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockLoader.getId()).thenReturn(1);
        when(mockCursor.getCount()).thenReturn(1);

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        mockCursor.moveToFirst();
        verify(mView).setArtistCursor(any(Cursor.class));
        verify(mControlAppMusicSource).play(any());
        verify(mEventBus).post(argument.capture());
        final NavigateEvent capturedEvent = argument.getValue();
        assertThat(capturedEvent.screenId, Matchers.is(ScreenId.PLAYER_CONTAINER));
    }

    @Test
    public void testOnLoadFinishedForArtistZero() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockLoader.getId()).thenReturn(1);
        when(mockCursor.getCount()).thenReturn(0);
        doAnswer(invocationOnMock -> {
            if (invocationOnMock.getArgument(0) instanceof CheckAvailableTextToSpeech.Callback) {
                ((CheckAvailableTextToSpeech.Callback) invocationOnMock.getArgument(0))
                        .onResult(CheckAvailableTextToSpeech.Result.AVAILABLE);
            }
            return null;
        }).when(mCheckTtsCase).execute(any(CheckAvailableTextToSpeech.Callback.class));
        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);
        mPresenter.onSpeakDone();
        verify(mView).setArtistCursor(any(Cursor.class));
        verify(mReadText).startReading(mContext.getString(R.string.vr_search_not_found));
        verify(mView).closeDialog();
    }

    @Test
    public void testOnLoadFinishedForAlbum() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockLoader.getId()).thenReturn(2);
        when(mockCursor.getCount()).thenReturn(2);

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setAlbumCursor(any(Cursor.class));
    }

    @Test
    public void testOnLoadFinishedForSong() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockLoader.getId()).thenReturn(3);
        when(mockCursor.getCount()).thenReturn(2);

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setMusicCursor(any(Cursor.class));
    }

    @Test
    public void testOnLoadFinishedForOther() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);

        when(mockLoader.getId()).thenReturn(0);

        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView, never()).setArtistCursor(any(Cursor.class));
        verify(mView, never()).setAlbumCursor(any(Cursor.class));
        verify(mView, never()).setMusicCursor(any(Cursor.class));
    }

    @Test
    public void testOnLoaderResetForArtist() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);

        when(mockLoader.getId()).thenReturn(1);

        mPresenter.takeView(mView);
        mPresenter.onLoaderReset(mockLoader);

        verify(mView).setArtistCursor(isNull());
    }

    @Test
    public void testOnLoaderResetForAlbum() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);

        when(mockLoader.getId()).thenReturn(2);

        mPresenter.takeView(mView);
        mPresenter.onLoaderReset(mockLoader);

        verify(mView).setAlbumCursor(isNull());
    }

    @Test
    public void testOnLoaderResetForSong() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);

        when(mockLoader.getId()).thenReturn(3);

        mPresenter.takeView(mView);
        mPresenter.onLoaderReset(mockLoader);

        verify(mView).setMusicCursor(isNull());
    }

    @Test
    public void testOnLoaderResetForOther() throws Exception {
        CursorLoader mockLoader = mock(CursorLoader.class);

        when(mockLoader.getId()).thenReturn(0);

        mPresenter.takeView(mView);
        mPresenter.onLoaderReset(mockLoader);

        verify(mView, never()).setArtistCursor(isNull());
        verify(mView, never()).setAlbumCursor(isNull());
        verify(mView, never()).setMusicCursor(isNull());
    }

    @Test
    public void testOnArtistAlbumListShowAction() throws Exception {
        Cursor mockCursor = mock(Cursor.class);

        when(mockCursor.getColumnIndexOrThrow(any(String.class))).thenReturn(1);
        when(mockCursor.getString(any(Integer.class))).thenReturn("test");

        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onArtistAlbumListShowAction(mockCursor, 1L);

        verify(mEventBus).post(argument.capture());
        assertThat(argument.getValue().screenId, is(ScreenId.SEARCH_MUSIC_ARTIST_ALBUM_LIST));
        MusicParams params = MusicParams.from(argument.getValue().args);
        assertThat(params.artistId, is(1L));
    }

    @Test
    public void testOnAlbumSongListShowAction() throws Exception {
        Cursor mockCursor = mock(Cursor.class);

        when(mockCursor.getColumnIndexOrThrow(any(String.class))).thenReturn(1);
        when(mockCursor.getString(any(Integer.class))).thenReturn("test");

        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onAlbumSongListShowAction(mockCursor, 1L);

        verify(mEventBus).post(argument.capture());
        assertThat(argument.getValue().screenId, is(ScreenId.SEARCH_MUSIC_ALBUM_SONG_LIST));
        MusicParams params = MusicParams.from(argument.getValue().args);
        assertThat(params.albumId, is(1L));
    }

    @Test
    public void testOnSongPlayAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onSongPlayAction(1L);

        verify(mControlAppMusicSource).play(any(AppMusicContract.PlayParams.class));
        verify(mEventBus).post(argument.capture());
        assertThat(argument.getValue().screenId, is(ScreenId.PLAYER_CONTAINER));
    }
}