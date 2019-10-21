package jp.pioneer.carsync.presentation.presenter;

import android.content.Context;
import android.os.Bundle;

import org.greenrobot.eventbus.EventBus;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.interactor.ControlMediaList;
import jp.pioneer.carsync.domain.interactor.GetStatusHolder;
import jp.pioneer.carsync.domain.model.AppStatus;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.ListType;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubDisplayInfo;
import jp.pioneer.carsync.presentation.event.GoBackEvent;
import jp.pioneer.carsync.presentation.event.NavigateEvent;
import jp.pioneer.carsync.presentation.view.PlayerTabContainerView;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008316 on 2017/05/17.
 */
public class PlayerTabContainerPresenterTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PlayerTabContainerPresenter mPresenter = new PlayerTabContainerPresenter();
    @Mock PlayerTabContainerView mView;
    @Mock Context mContext;
    @Mock EventBus mEventBus;
    @Mock AppSharedPreference mPreference;
    @Mock GetStatusHolder mUseCase;
    @Mock ControlMediaList mControlMediaList;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        when(mContext.getResources()).thenReturn(getTargetContext().getResources());
    }

    @Test
    public void testOnInitialize() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        StatusHolder mockHolder = mock(StatusHolder.class);
        AppStatus status = new AppStatus();
        when(mockHolder.getAppStatus()).thenReturn(status);
        when(mUseCase.execute()).thenReturn(mockHolder);
        mPresenter.takeView(mView);
        mPresenter.initialize();

        verify(mView).onNavigate(eq(ScreenId.ARTIST_LIST), any(Bundle.class));
        verify(mControlMediaList).notifySelectedListInfo(true,true, SubDisplayInfo.TOP_LIST, mContext.getString(SubDisplayInfo.ARTISTS.label));
    }

    @Test
    public void testSetTitle() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        MusicParams params = new MusicParams();
        params.pass = "TEST";

        mPresenter.takeView(mView);
        mPresenter.setTitle(params.toBundle());

        verify(mView).setTitle(eq("TEST"));
    }

    @Test
    public void testNotSetTitle() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        MusicParams params1 = new MusicParams();
        params1.pass = "TEST1";

        mPresenter.takeView(mView);
        mPresenter.setTitle(params1.toBundle());
        mPresenter.setTitle(Bundle.EMPTY);

        verify(mView, times(2)).setTitle(eq("TEST1"));
    }

    @Test
    public void testRemoveTitle() throws Exception {
        when(mPreference.getUiColor()).thenReturn(AQUA);
        MusicParams params1 = new MusicParams();
        params1.pass = "TEST1";
        MusicParams params2 = new MusicParams();
        params2.pass = "TEST2";

        mPresenter.takeView(mView);
        mPresenter.setTitle(params1.toBundle());
        mPresenter.setTitle(params2.toBundle());
        mPresenter.removeTitle();

        verify(mView, times(2)).setTitle(eq("TEST1"));
    }

    @Test
    public void testOnArtistAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onArtistAction();

        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.ARTIST_LIST));
        MusicParams params = MusicParams.from(event.args);
        assertThat(params.pass, is("Artist"));
    }

    @Test
    public void testOnAlbumAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onAlbumAction();

        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.ALBUM_LIST));
        MusicParams params = MusicParams.from(event.args);
        assertThat(params.pass, is("Album"));
    }

    @Test
    public void testOnSongAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onSongAction();

        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.SONG_LIST));
        MusicParams params = MusicParams.from(event.args);
        assertThat(params.pass, is("Song"));
    }

    @Test
    public void testOnPlaylistAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onPlaylistAction();

        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.PLAYLIST_LIST));
        MusicParams params = MusicParams.from(event.args);
        assertThat(params.pass, is("Playlist"));
    }

    @Test
    public void testOnGenreAction() throws Exception {
        ArgumentCaptor<NavigateEvent> argument = ArgumentCaptor.forClass(NavigateEvent.class);

        mPresenter.onGenreAction();

        verify(mEventBus).post(argument.capture());
        NavigateEvent event = argument.getValue();
        assertThat(event.screenId, is(ScreenId.GENRE_LIST));
        MusicParams params = MusicParams.from(event.args);
        assertThat(params.pass, is("Genre"));
    }

    @Test
    public void testOnBackAction() throws Exception {
        StatusHolder holder = mock(StatusHolder.class);
        CarDeviceStatus carDeviceStatus = mock(CarDeviceStatus.class);
        carDeviceStatus.listType = ListType.NOT_LIST;
        carDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;
        when(holder.getCarDeviceStatus()).thenReturn(carDeviceStatus);
        when(mUseCase.execute()).thenReturn(holder);
        mPresenter.onBackAction();

        verify(mEventBus).post(any(GoBackEvent.class));
    }
}