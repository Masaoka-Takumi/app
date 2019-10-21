package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.app.Instrumentation;
import android.database.Cursor;
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
import jp.pioneer.carsync.presentation.presenter.SearchArtistAlbumsPresenter;
import jp.pioneer.carsync.presentation.view.argument.MusicParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.anything;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008316 on 2017/05/10.
 */
public class SearchArtistAlbumsFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<SearchArtistAlbumsFragment> mFragmentRule = new FragmentTestRule<SearchArtistAlbumsFragment>() {
        @Override
        protected SearchArtistAlbumsFragment createDialogFragment() {
            MusicParams params = new MusicParams();
            params.pass = "testArtist";
            params.artistId = 1L;
            return SearchArtistAlbumsFragment.newInstance(params.toBundle());
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SearchArtistAlbumsPresenter mPresenter;

    private static final String[] COLUMN_ALBUM = {
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST,
    };

    private MatrixCursor mAlbumCursor = new MatrixCursor(COLUMN_ALBUM);

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
        public SearchArtistAlbumsPresenter provideSearchMusicPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerSearchArtistAlbumsFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SearchArtistAlbumsFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(SearchArtistAlbumsFragment.class))).thenReturn(fragmentComponent);

        mAlbumCursor.addRow(new String[]{"1", "AA0", "AAA",});
        mAlbumCursor.addRow(new String[]{"2", "BB0", "BBB",});
        mAlbumCursor.moveToFirst();
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
    public void testSelectAlbum() throws Exception {
        mFragmentRule.launchActivity(null);
        final SearchArtistAlbumsFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setAlbumCursor(mAlbumCursor, Bundle.EMPTY));

        // 表示確認
        onView(withText("AA0")).check(matches(isDisplayed()));
        onView(withText("BB0")).check(matches(isDisplayed()));
        // 動作確認
        onView(withText("AA0")).perform(click());

        verify(mPresenter).onArtistAlbumSongListShowAction(any(Cursor.class), eq(1L));
    }

    //@Test
    public void testSelectShuffle() throws Exception {
        mFragmentRule.launchActivity(null);
        final SearchArtistAlbumsFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setAlbumCursor(mAlbumCursor, Bundle.EMPTY));

        // 動作確認
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0)
                .perform(click());

        verify(mPresenter).onArtistAlbumShufflePlayAction();
    }
}