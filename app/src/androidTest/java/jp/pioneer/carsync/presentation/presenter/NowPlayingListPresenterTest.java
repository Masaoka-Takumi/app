package jp.pioneer.carsync.presentation.presenter;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.event.AppMusicPlaybackModeChangeEvent;
import jp.pioneer.carsync.domain.event.AppMusicTrackChangeEvent;
import jp.pioneer.carsync.domain.interactor.ControlAppMusicSource;
import jp.pioneer.carsync.domain.interactor.CreateNowPlayingList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.interactor.SelectTrack;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.view.NowPlayingListView;

import static android.R.attr.id;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NowPlayingListのPresenterのテスト
 */
public class NowPlayingListPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks NowPlayingListPresenter mPresenter = new NowPlayingListPresenter();
    @Mock NowPlayingListView mView;
    @Mock EventBus mEventBus;
    @Mock GetStatusHolder mGetCase;
    @Mock CreateNowPlayingList mCreateList;
    @Mock ControlAppMusicSource mControlAppMusicSource;
    @Mock SelectTrack mSelectTrack;
    private static final int LOADER_ID_NOW_PLAY = 1;
    private LoaderManager mLoaderManager;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    /**
     * OnResumeのテスト EventBus未登録の場合
     */
    @Test
    public void testOnResume() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(false);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        info.trackNumber = 1;
        info.mediaId = 3;
        status.playbackMode = PlaybackMode.STOP;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onResume();

        verify(mEventBus).register(mPresenter);
        verify(mView).setNowPlaySong(1,3, PlaybackMode.STOP);
    }

    /**
     * OnResumeのテスト EventBus登録済の場合
     */
    @Test
    public void testOnResume2() throws Exception {
        when(mEventBus.isRegistered(mPresenter)).thenReturn(true);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        info.trackNumber = 1;
        info.mediaId = 3;
        status.playbackMode = PlaybackMode.PLAY;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onResume();

        verify(mEventBus,times(0)).register(mPresenter);
        verify(mView).setNowPlaySong(1,3, PlaybackMode.PLAY);
    }

    /**
     * onPauseのテスト
     */
    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();
        verify(mEventBus).unregister(mPresenter);
    }

    @Test
    public void testOnCreateLoader() throws Exception {
        Bundle args = new Bundle();
        AppMusicCursorLoader appMusicCursorLoader = mock(AppMusicCursorLoader.class);
        when(mCreateList.execute()).thenReturn(appMusicCursorLoader);
        Loader<Cursor> loader = mPresenter.onCreateLoader(id, args);
        assertThat(loader, is(appMusicCursorLoader));
    }

    @Test
    public void testSetLoaderManager() throws Exception {
        LoaderManager loaderManager = mock(LoaderManager.class);
        mLoaderManager = loaderManager;
        mPresenter.setLoaderManager(loaderManager);
        verify(mLoaderManager).initLoader(LOADER_ID_NOW_PLAY, Bundle.EMPTY, mPresenter);
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
    public void testOnTrackChangeAction() throws Exception {
        AppMusicTrackChangeEvent event = mock(AppMusicTrackChangeEvent.class);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        info.trackNumber = 1;
        info.mediaId = 3;
        status.playbackMode = PlaybackMode.PLAY;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onTrackChangeAction(event);
        verify(mView).setNowPlaySong(1,3,PlaybackMode.PLAY);
    }

    @Test
    public void testOnAppMusicPlaybackModeChangeEvent() throws Exception {
        AppMusicPlaybackModeChangeEvent event = mock(AppMusicPlaybackModeChangeEvent.class);
        AndroidMusicMediaInfo info = mock(AndroidMusicMediaInfo.class);
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceMediaInfoHolder mediaHolder = mock(CarDeviceMediaInfoHolder.class);
        SmartPhoneStatus status = mock(SmartPhoneStatus.class);
        info.trackNumber = 1;
        info.mediaId = 3;
        status.playbackMode = PlaybackMode.STOP;
        mediaHolder.androidMusicMediaInfo = info;
        when(holder.getCarDeviceMediaInfoHolder()).thenReturn(mediaHolder);
        when(holder.getSmartPhoneStatus()).thenReturn(status);
        when(mGetCase.execute()).thenReturn(holder);
        mPresenter.onAppMusicPlaybackModeChangeEvent(event);
        verify(mView).setNowPlaySong(1,3,PlaybackMode.STOP);
    }
    @Test
    public void testOnSongPlayAction() throws Exception {
        mPresenter.onSongPlayAction(1);
        verify(mSelectTrack).execute(1);
    }

}