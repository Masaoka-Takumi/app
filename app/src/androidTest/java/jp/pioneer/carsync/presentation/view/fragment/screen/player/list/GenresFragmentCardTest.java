package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Instrumentation;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;

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
import jp.pioneer.carsync.presentation.presenter.GenresPresenter;

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
 * GenresFragmentのCardのテスト
 */
public class GenresFragmentCardTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<GenresFragment> mFragmentRule = new FragmentTestRule<GenresFragment>() {
        @Override
        protected GenresFragment createDialogFragment() {
            return GenresFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock GenresPresenter mPresenter;
    private static final String[] FROM = {MediaStore.Audio.Media._ID,MediaStore.Audio.GenresColumns.NAME };
    private MatrixCursor mCursor = new MatrixCursor(FROM);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        GenresFragmentCardTest.TestPresenterComponent presenterComponent(GenresFragmentCardTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = GenresFragmentCardTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public GenresPresenter provideGenresPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        GenresFragmentCardTest.TestAppComponent appComponent = DaggerGenresFragmentCardTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        GenresFragmentCardTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new GenresFragmentCardTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, GenresFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(GenresFragmentCardTest.TestPresenterComponent.class), any(GenresFragment.class))).thenReturn(fragmentComponent);
        mCursor.addRow(new String[] {"1", "tanaka"});
        mCursor.moveToFirst();

    }

    @After
    public void tearDown() throws Exception {
    }

//    @Test
    public void testSetAlbumCursor() throws Exception {
        mFragmentRule.launchActivity(null);
        final GenresFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setGenreCursor(mCursor,args);
        });
        onView(withText("tanaka")).check(matches(isDisplayed()));

    }

//    @Test
    public void testJacketClick() throws Exception {
        mFragmentRule.launchActivity(null);
        final GenresFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setGenreCursor(mCursor,args);
        });
        onView(ViewMatchers.withId(R.id.play_button)).perform(click());
        verify(mPresenter).onGenrePlayAction(anyLong());
    }

    @Test
    public void testListItemCLick() throws Exception {
        mFragmentRule.launchActivity(null);
        final GenresFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setGenreCursor(mCursor,args);
        });
        onView(withText("tanaka")).perform(click());
        verify(mPresenter).onGenreSongListShowAction(any(Cursor.class),anyLong());
    }
}