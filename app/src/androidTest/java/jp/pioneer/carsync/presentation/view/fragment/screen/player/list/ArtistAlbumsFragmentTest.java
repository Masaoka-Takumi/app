package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Instrumentation;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
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
import jp.pioneer.carsync.presentation.presenter.ArtistAlbumsPresenter;

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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ArtistAlbumsFragmentのテスト
 */
public class ArtistAlbumsFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<ArtistAlbumsFragment> mFragmentRule = new FragmentTestRule<ArtistAlbumsFragment>() {
        @Override
        protected ArtistAlbumsFragment createDialogFragment() {
            return ArtistAlbumsFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock ArtistAlbumsPresenter mPresenter;
    private static final String[] FROM = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST};
    private MatrixCursor mCursor = new MatrixCursor(FROM);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        ArtistAlbumsFragmentTest.TestPresenterComponent presenterComponent(ArtistAlbumsFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = ArtistAlbumsFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public ArtistAlbumsPresenter provideArtistAlbumsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        ArtistAlbumsFragmentTest.TestAppComponent appComponent = DaggerArtistAlbumsFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        ArtistAlbumsFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new ArtistAlbumsFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ArtistAlbumsFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(ArtistAlbumsFragmentTest.TestPresenterComponent.class), any(ArtistAlbumsFragment.class))).thenReturn(fragmentComponent);
        mCursor.addRow(new String[]{"1", "tanaka", "daisuke"});
        mCursor.moveToFirst();

    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSetAlbumCursor() throws Exception {
        mFragmentRule.launchActivity(null);
        final ArtistAlbumsFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAlbumCursor(mCursor, args);
//            fragment.setDirectoryPass("pass");
        });
        onView(withText("tanaka")).check(matches(isDisplayed()));
    }

    //@Test
    public void testOnClickListItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final ArtistAlbumsFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAlbumCursor(mCursor, args);
        });
        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(0)
                .perform(click());
        verify(mPresenter).onArtistAlbumShufflePlayAction();
    }

    @Test
    public void testOnClickListItem2() throws Exception {
        mFragmentRule.launchActivity(null);
        final ArtistAlbumsFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAlbumCursor(mCursor, args);
        });
        onView(withText("tanaka")).perform(click());
        verify(mPresenter).onArtistAlbumSongListShowAction(any(Cursor.class), anyLong());
    }

    @Test
    public void testOnClickListItem3() throws Exception {
        mFragmentRule.launchActivity(null);
        final ArtistAlbumsFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAlbumCursor(mCursor, args);
        });
        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(0)
                .onChildView(withId(R.id.jacket_view))
                .perform(click());
        verify(mPresenter).onArtistAlbumPlayAction(anyLong());
    }
}