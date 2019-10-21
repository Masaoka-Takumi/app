package jp.pioneer.carsync.presentation.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.screen.AbstractScreenFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.home.HomeContainerFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchAlbumSongsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchArtistAlbumSongsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchArtistAlbumsFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchContactFragment;
import jp.pioneer.carsync.presentation.view.fragment.screen.search.SearchMusicFragment;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008316 on 2017/05/10.
 */
public class SearchFragmentControllerTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks TestSearchFragmentController mFragmentController = new TestSearchFragmentController();
    @Mock FragmentManager mFragmentManager;
    @Mock SearchContactFragment mSearchContactFragment;
    @Mock SearchMusicFragment mSearchMusicFragment;
    @Mock SearchArtistAlbumsFragment mSearchArtistAlbumsFragment;
    @Mock SearchArtistAlbumSongsFragment mSearchArtistAlbumSongsFragment;
    @Mock SearchAlbumSongsFragment mSearchAlbumSongsFragment;
    @Mock View mView;

    class TestSearchFragmentController extends SearchFragmentController {
        @Override
        Fragment createSearchContactFragment(Bundle args) {
            return mSearchContactFragment;
        }

        @Override
        Fragment createSearchMusicFragment(Bundle args) {
            return mSearchMusicFragment;
        }

        @Override
        Fragment createSearchArtistAlbumsFragment(Bundle args) {
            return mSearchArtistAlbumsFragment;
        }

        @Override
        Fragment createSearchArtistAlbumSongsFragment(Bundle args) {
            return mSearchArtistAlbumSongsFragment;
        }

        @Override
        Fragment createSearchAlbumSongsFragment(Bundle args) {
            return mSearchAlbumSongsFragment;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
    }

    @Test
    public void testGetScreenIdInContainer() throws Exception {
        AbstractScreenFragment mockFragment = mock(AbstractScreenFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.getScreenId()).thenReturn(ScreenId.HOME_CONTAINER);

        mFragmentController.setContainerViewId(mView.getId());

        assertThat(mFragmentController.getScreenIdInContainer(), is(ScreenId.HOME_CONTAINER));
    }

    @Test
    public void testGetScreenIdInContainerIsNull() throws Exception {
        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(null);

        mFragmentController.setContainerViewId(mView.getId());

        assertThat(mFragmentController.getScreenIdInContainer(), is(nullValue()));
    }

    @Test
    public void testNavigate() throws Exception {
        FragmentTransaction mockTransaction = mock(FragmentTransaction.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(null);
        when(mFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.SEARCH_CONTACT_RESULTS, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.SEARCH_MUSIC_RESULTS, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.SEARCH_MUSIC_ARTIST_ALBUM_LIST, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.SEARCH_MUSIC_ARTIST_ALBUM_SONG_LIST, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.SEARCH_MUSIC_ALBUM_SONG_LIST, Bundle.EMPTY), is(true));
        assertThat(mFragmentController.navigate(ScreenId.ANDROID_MUSIC, Bundle.EMPTY), is(false));

        verify(mockTransaction).replace(mView.getId(), mSearchContactFragment);
        verify(mockTransaction).replace(mView.getId(), mSearchMusicFragment);
        verify(mockTransaction).replace(mView.getId(), mSearchArtistAlbumsFragment);
        verify(mockTransaction).replace(mView.getId(), mSearchArtistAlbumSongsFragment);
        verify(mockTransaction).replace(mView.getId(), mSearchAlbumSongsFragment);
    }

    @Test
    public void testNavigate2() throws Exception {
        FragmentTransaction mockTransaction = mock(FragmentTransaction.class);
        Fragment mockFragment = mock(Fragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.SEARCH_CONTACT_RESULTS, Bundle.EMPTY), is(true));
        verify(mockTransaction).replace(mView.getId(), mSearchContactFragment);
    }

    @Test
    public void testNavigate3() throws Exception {
        FragmentTransaction mockTransaction = mock(FragmentTransaction.class);
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onNavigate(ScreenId.HOME, Bundle.EMPTY)).thenReturn(false);
        when(mFragmentManager.beginTransaction()).thenReturn(mockTransaction);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.SEARCH_CONTACT_RESULTS, Bundle.EMPTY), is(true));
        verify(mockTransaction).replace(mView.getId(), mSearchContactFragment);
    }

    @Test
    public void testNavigateInOther() throws Exception {
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onNavigate(ScreenId.HOME, Bundle.EMPTY)).thenReturn(true);

        mFragmentController.setContainerViewId(mView.getId());
        assertThat(mFragmentController.navigate(ScreenId.HOME, Bundle.EMPTY), is(true));
    }

    @Test
    public void testGoBack() throws Exception {
        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(null);

        assertThat(mFragmentController.goBack(), is(false));
    }

    @Test
    public void testGoBack2() throws Exception {
        Fragment mockFragment = mock(Fragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);

        assertThat(mFragmentController.goBack(), is(false));
    }

    @Test
    public void testGoBack3() throws Exception {
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onGoBack()).thenReturn(false);

        assertThat(mFragmentController.goBack(), is(false));
    }

    @Test
    public void testGoBackInOther() throws Exception {
        HomeContainerFragment mockFragment = mock(HomeContainerFragment.class);

        when(mFragmentManager.findFragmentById(mView.getId())).thenReturn(mockFragment);
        when(mockFragment.onGoBack()).thenReturn(true);

        assertThat(mFragmentController.goBack(), is(true));
    }
}