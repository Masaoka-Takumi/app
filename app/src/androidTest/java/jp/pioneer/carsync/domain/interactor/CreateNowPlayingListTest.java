package jp.pioneer.carsync.domain.interactor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.domain.component.AppMusicSourceController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.content.AppMusicCursorLoader;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.repository.NowPlayingListRepository;
import jp.pioneer.carsync.infrastructure.database.AppMusicPlaylistCursor;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/07/10.
 */
public class CreateNowPlayingListTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    CreateNowPlayingList mCreateNowPlayingList;
    @Mock CarDevice mCarDevice;
    @Mock AppMusicSourceController mAppMusicSourceController;
    @Mock AppMusicCursorLoader mAppMusicCursorLoader;
    @Mock NowPlayingListRepository mRepository;
    @Mock AppMusicPlaylistCursor mPlaylistCursor;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mAppMusicSourceController.isActive()).thenReturn(true);
        when(mAppMusicSourceController.getPlaylistCursor()).thenReturn(mPlaylistCursor);
        when(mRepository.get(mPlaylistCursor)).thenReturn(mAppMusicCursorLoader);
        when(mCarDevice.getSourceController(eq(MediaSourceType.APP_MUSIC))).thenReturn(mAppMusicSourceController);

        mCreateNowPlayingList = new CreateNowPlayingList(mCarDevice);
        mCreateNowPlayingList.mRepository = mRepository;
    }

    @Test
    public void execute() throws Exception {
        // exercise
        AppMusicCursorLoader actual = mCreateNowPlayingList.execute();

        // verify
        assertThat(actual,is(mAppMusicCursorLoader));
    }

    @Test
    public void execute_isInactive() throws Exception {
        // setup
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        AppMusicCursorLoader actual = mCreateNowPlayingList.execute();

        // verify
        assertThat(actual, is(nullValue()));
    }
}