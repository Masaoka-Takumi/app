package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.app.Instrumentation;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.LoaderManager;

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
import jp.pioneer.carsync.presentation.presenter.SearchArtistAlbumSongsPresenter;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008316 on 2017/05/10.
 */

public class SearchArtistAlbumSongsFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<SearchArtistAlbumSongsFragment> mFragmentRule = new FragmentTestRule<SearchArtistAlbumSongsFragment>() {
        @Override
        protected SearchArtistAlbumSongsFragment createDialogFragment() {
            MusicParams params = new MusicParams();
            params.pass = "testArtist>testAlbum";
            params.albumId = 1L;
            return SearchArtistAlbumSongsFragment.newInstance(params.toBundle());
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SearchArtistAlbumSongsPresenter mPresenter;

    private static final String[] COLUMN_SONG = {
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.TRACK,
            MediaStore.Audio.Media.DATA,
    };
    private MatrixCursor mSongCursor = new MatrixCursor(COLUMN_SONG);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        TestPresenterComponent presenterComponent(TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public SearchArtistAlbumSongsPresenter provideSearchMusicPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerSearchArtistAlbumSongsFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SearchArtistAlbumSongsFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(SearchArtistAlbumSongsFragment.class))).thenReturn(fragmentComponent);

        mSongCursor.addRow(new String[]{"1", "AAA", "Zero", "AA0", "1", "1", "2017/5/10 14:00:00",});
        mSongCursor.addRow(new String[]{"2", "BBB", "One", "BB0", "2", "1", "2017/5/10 14:30:00",});
        mSongCursor.moveToFirst();
    }

    @Test
    public void testLifeCycle() throws Exception {
        mFragmentRule.launchActivity(null);
        mFragmentRule.getActivity().finish();

        Thread.sleep(200);

        verify(mPresenter).setArguments(any(Bundle.class));
        verify(mPresenter).setLoaderManager(any(LoaderManager.class));
    }

    @Test
    public void testSelectSong() throws Exception {
        mFragmentRule.launchActivity(null);
        final SearchArtistAlbumSongsFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setSongCursor(mSongCursor, Bundle.EMPTY));

        // 表示確認
        onView(withText("Zero")).check(matches(isDisplayed()));
        onView(withText("One")).check(matches(isDisplayed()));
        // 動作確認
        onView(withText("Zero")).perform(click());

        verify(mPresenter).onArtistAlbumSongPlayAction(eq(1L));
    }
}