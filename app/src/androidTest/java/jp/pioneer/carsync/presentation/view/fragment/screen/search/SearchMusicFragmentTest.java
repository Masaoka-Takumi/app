package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.app.Instrumentation;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.LoaderManager;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.FragmentTestRule;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.FragmentModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.presenter.SearchMusicPresenter;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.anything;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008316 on 2017/05/10.
 */
public class SearchMusicFragmentTest {
//    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
//    @Rule public FragmentTestRule<SearchMusicFragment> mFragmentRule = new FragmentTestRule<SearchMusicFragment>() {
//        @Override
//        protected SearchMusicFragment createDialogFragment() {
//            String[] keywords = new String[]{"aaa", "bbb", "ccc"};
//            Bundle args = SearchContentParams.toBundle(VoiceSearchContent.BOTH, keywords);
//            return SearchMusicFragment.newInstance(args);
//        }
//    };
//    @Mock ComponentFactory mComponentFactory;
//    @Mock SearchMusicPresenter mPresenter;
//
//    private static final String[] COLUMN_SONG = {
//            MediaStore.Audio.Media._ID,
//            MediaStore.Audio.Media.ARTIST,
//            MediaStore.Audio.Media.TITLE,
//            MediaStore.Audio.Media.ALBUM,
//            MediaStore.Audio.Media.ALBUM_ID,
//            MediaStore.Audio.Media.TRACK,
//            MediaStore.Audio.Media.DATA,
//    };
//    private MatrixCursor mSongCursor = new MatrixCursor(COLUMN_SONG);
//    private static final String[] COLUMN_ALBUM = {
//            MediaStore.Audio.Albums._ID,
//            MediaStore.Audio.Albums.ALBUM,
//            MediaStore.Audio.Albums.ARTIST,
//    };
//    private MatrixCursor mAlbumCursor = new MatrixCursor(COLUMN_ALBUM);
//    private static final String[] COLUMN_ARTIST = {
//            MediaStore.Audio.Artists._ID,
//            MediaStore.Audio.Artists.ARTIST,
//            MediaStore.Audio.Artists.NUMBER_OF_ALBUMS,
//    };
//    private MatrixCursor mArtistCursor = new MatrixCursor(COLUMN_ARTIST);
//
//    @Singleton
//    @Component(modules = {
//            AppModule.class,
//            DomainModule.class,
//            InfrastructureModule.class,
//            InfrastructureBindsModule.class
//    })
//    public interface TestAppComponent extends AppComponent {
//        TestPresenterComponent presenterComponent(TestPresenterModule module);
//    }
//
//    @PresenterLifeCycle
//    @Subcomponent(modules = TestPresenterModule.class)
//    public interface TestPresenterComponent extends PresenterComponent {
//    }
//
//    @Module
//    public class TestPresenterModule {
//        public TestPresenterModule() {
//        }
//
//        @Provides
//        public SearchMusicPresenter provideSearchMusicPresenter() {
//            return mPresenter;
//        }
//    }
//
//    @Before
//    public void setUp() throws Exception {
//        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
//        TestApp testApp = getTestApp();
//        TestAppComponent appComponent = DaggerSearchMusicFragmentTest_TestAppComponent.builder().build();
//        testApp.setAppComponent(appComponent);
//        testApp.setComponentFactory(mComponentFactory);
//        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
//        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
//        when(mComponentFactory.getPresenterComponent(appComponent, SearchMusicFragment.class)).thenReturn(presenterComponent);
//        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(SearchMusicFragment.class))).thenReturn(fragmentComponent);
//
//        mSongCursor.addRow(new String[]{"1", "AAA", "Zero", "AA0", "3", "1", "2017/5/10 14:00:00",});
//        mSongCursor.addRow(new String[]{"2", "BBB", "Zeroge", "BB0", "4", "1", "2017/5/10 14:30:00",});
//        mSongCursor.moveToFirst();
//        mAlbumCursor.addRow(new String[]{"1", "from Zero", "CCC",});
//        mAlbumCursor.addRow(new String[]{"2", "ZeroZeroNine", "DDD",});
//        mAlbumCursor.moveToFirst();
//        mArtistCursor.addRow(new String[]{"1", "Mr.Zero", "1",});
//        mArtistCursor.addRow(new String[]{"2", "Zero 8", "1",});
//        mArtistCursor.moveToFirst();
//    }
//
//    @Test
//    public void testLifeCycle() throws Exception {
//        mFragmentRule.launchActivity(null);
//        mFragmentRule.getActivity().finish();
//
//        Thread.sleep(200);
//
//        verify(mPresenter).setArguments(any(Bundle.class));
//        verify(mPresenter).setLoaderManager(any(LoaderManager.class));
//    }
//
//    @Test
//    public void testNoMatchPattern() throws Exception {
//        mFragmentRule.launchActivity(null);
//        final SearchMusicFragment fragment = mFragmentRule.getFragment();
//
//        MatrixCursor sCursor = new MatrixCursor(COLUMN_SONG);
//        MatrixCursor alCursor = new MatrixCursor(COLUMN_ALBUM);
//        MatrixCursor arCursor = new MatrixCursor(COLUMN_ARTIST);
//
//        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
//        instr.runOnMainSync(() -> {
//            fragment.setArtistCursor(sCursor);
//            fragment.setAlbumCursor(alCursor);
//            fragment.setMusicCursor(arCursor);
//        });
//
//        onView(withText("アーティスト")).check(doesNotExist());
//        onView(withText("アルバム")).check(doesNotExist());
//        onView(withText("トラック")).check(doesNotExist());
//        onView(withText("項目が見つかりませんでした")).check(matches(isDisplayed()));
//    }
//
//    @Test
//    public void testSelectArtist() throws Exception {
//        mFragmentRule.launchActivity(null);
//        final SearchMusicFragment fragment = mFragmentRule.getFragment();
//
//        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
//        instr.runOnMainSync(() -> {
//            fragment.setArtistCursor(mArtistCursor);
//            fragment.setAlbumCursor(mAlbumCursor);
//            fragment.setMusicCursor(mSongCursor);
//        });
//
//        // 表示確認
//        onView(withText("Mr.Zero")).check(matches(isDisplayed()));
//        onView(withText("Zero 8")).check(matches(isDisplayed()));
//        // 動作確認
//        onData(anything())
//                .inAdapterView(withId(R.id.search_list))
//                .atPosition(1)
//                .perform(click());
//
//        verify(mPresenter).onArtistAlbumListShowAction(any(MatrixCursor.class), eq(1L));
//    }
//
//    @Test
//    public void testSelectAlbum() throws Exception {
//        mFragmentRule.launchActivity(null);
//        final SearchMusicFragment fragment = mFragmentRule.getFragment();
//
//        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
//        instr.runOnMainSync(() -> {
//            fragment.setArtistCursor(mArtistCursor);
//            fragment.setAlbumCursor(mAlbumCursor);
//            fragment.setMusicCursor(mSongCursor);
//        });
//
//        // 表示確認
//        onView(withText("from Zero")).check(matches(isDisplayed()));
//        onView(withText("ZeroZeroNine")).check(matches(isDisplayed()));
//        // 動作確認
//        onData(anything())
//                .inAdapterView(withId(R.id.search_list))
//                .atPosition(4)
//                .perform(click());
//
//        verify(mPresenter).onAlbumSongListShowAction(any(MatrixCursor.class), eq(1L));
//    }
//
//    @Test
//    public void testSelectSong() throws Exception {
//        mFragmentRule.launchActivity(null);
//        final SearchMusicFragment fragment = mFragmentRule.getFragment();
//
//        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
//        instr.runOnMainSync(() -> {
//            fragment.setArtistCursor(mArtistCursor);
//            fragment.setAlbumCursor(mAlbumCursor);
//            fragment.setMusicCursor(mSongCursor);
//        });
//
//        // 表示確認
//        onView(withText("Zero")).check(matches(isDisplayed()));
//        onView(withText("Zeroge")).check(matches(isDisplayed()));
//        // 動作確認
//        onData(anything())
//                .inAdapterView(withId(R.id.search_list))
//                .atPosition(7)
//                .perform(click());
//
//        verify(mPresenter).onSongPlayAction(eq(1L));
//    }
//
//    @Test
//    public void testOnClickExtra() throws Exception {
//        mFragmentRule.launchActivity(null);
//        final SearchMusicFragment fragment = mFragmentRule.getFragment();
//
//        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
//        instr.runOnMainSync(() -> {
//            fragment.setArtistCursor(null);
//            fragment.setAlbumCursor(null);
//            fragment.setMusicCursor(null);
//        });
//
//        ListView parent = mock(ListView.class);
//        ListAdapter adapter = mock(ListAdapter.class);
//        View view = mock(View.class);
//
//        when(parent.getAdapter()).thenReturn(adapter);
//
//        fragment.onClickListItem(parent, view, 1, 1L);
//
//        verify(mPresenter, never()).onArtistAlbumListShowAction(any(Cursor.class), eq(1L));
//        verify(mPresenter, never()).onAlbumSongListShowAction(any(Cursor.class), eq(1L));
//        verify(mPresenter, never()).onSongPlayAction(eq(1L));
//    }
}