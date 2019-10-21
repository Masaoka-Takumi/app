package jp.pioneer.carsync.presentation.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;

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
 * アルバム内リスト画面（検索用）のpresenterテスト
 */
public class SearchAlbumSongsPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks SearchAlbumSongsPresenter mPresenter = new SearchAlbumSongsPresenter();
    @Mock SongsView mView;
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
        params.pass = "testAlbum";
        params.albumId = 1;

        mPresenter.setArguments(params.toBundle());
        mPresenter.onCreateLoader(1, Bundle.EMPTY);

        verify(mQueryCase).execute(any(QueryParams.class));
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager mockLoader = mock(LoaderManager.class);

        mPresenter.setLoaderManager(mockLoader);

        verify(mockLoader).initLoader(eq(3), any(Bundle.class), any(SearchAlbumSongsPresenter.class));
    }

    @Test
    public void testOnLoadFinished() throws Exception {
        MusicParams params = new MusicParams();
        params.pass = "testAlbum";
        params.albumId = 1;
        AppMusicCursorLoader mockLoader = mock(AppMusicCursorLoader.class);
        Cursor mockCursor = mock(Cursor.class);
        Bundle args = new Bundle();

        when(mockLoader.getExtras()).thenReturn(args);

        mPresenter.setArguments(params.toBundle());
        mPresenter.takeView(mView);
        mPresenter.onLoadFinished(mockLoader, mockCursor);

        verify(mView).setSongCursor(eq(mockCursor), eq(args));
    }

    @Test
    public void testOnLoaderReset() throws Exception {
        MusicParams params = new MusicParams();
        params.pass = "testAlbum";
        params.albumId = 1;
        AppMusicCursorLoader mockLoader = mock(AppMusicCursorLoader.class);

        mPresenter.setArguments(params.toBundle());
        mPresenter.takeView(mView);
        mPresenter.onLoaderReset(mockLoader);

        verify(mView).setSongCursor(isNull(), eq(Bundle.EMPTY));
    }

    @Test
    public void testOnAlbumSongPlayAction() throws Exception {
        MusicParams params = new MusicParams();
        params.pass = "testAlbum";
        params.albumId = 1;

        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.setArguments(params.toBundle());
        mPresenter.takeView(mView);
        mPresenter.onAlbumSongPlayAction(1);

        verify(mControlAppMusicSource).play(any(AppMusicContract.PlayParams.class));
        verify(mEventBus).post(argument.capture());
        assertThat(argument.getValue().screenId, is(ScreenId.PLAYER_CONTAINER));
    }
}