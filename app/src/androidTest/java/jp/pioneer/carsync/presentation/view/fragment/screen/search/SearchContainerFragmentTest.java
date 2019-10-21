package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.FragmentManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.FragmentTestRule;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.di.FragmentLifeCycle;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.domain.model.VoiceCommand;
import jp.pioneer.carsync.presentation.controller.SearchFragmentController;
import jp.pioneer.carsync.presentation.presenter.SearchContainerPresenter;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 検索画面コンテナのテスト
 */
public class SearchContainerFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<SearchContainerFragment> mFragmentRule = new FragmentTestRule<SearchContainerFragment>() {
        @Override
        protected SearchContainerFragment createDialogFragment() {
            String[] keywords = new String[]{"aaa", "bbb", "ccc"};
            Bundle args = SearchContentParams.toBundle(VoiceCommand.ARTIST, keywords);
            return SearchContainerFragment.newInstance(args);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SearchContainerPresenter mPresenter;
    @Mock SearchFragmentController mFragmentController;
    @Mock FragmentManager mFragmentManager;

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
        TestFragmentComponent testFragmentComponent(TestFragmentModule module);
    }

    @FragmentLifeCycle
    @Subcomponent(modules = TestFragmentModule.class)
    public interface TestFragmentComponent extends FragmentComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public SearchContainerPresenter provideSearchContainerPresenter() {
            return mPresenter;
        }

        @Provides
        public SearchFragmentController provideSearchFragmentController() {
            return mFragmentController;
        }
    }

    @Module
    public class TestFragmentModule {
        public TestFragmentModule() {
        }

        @Provides
        public FragmentManager provideFragmentManager() {
            return mFragmentManager;
        }

        @Provides
        @Named(CHILD_FRAGMENT_MANAGER)
        public FragmentManager provideChildFragmentManager() {
            return mFragmentManager;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerSearchContainerFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        TestFragmentComponent fragmentComponent = presenterComponent.testFragmentComponent(new TestFragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SearchContainerFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(SearchContainerFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testLifeCycle() throws Exception {
        mFragmentRule.launchActivity(null);
        mFragmentRule.getActivity().finish();

        Thread.sleep(200);

        verify(mFragmentController).setContainerViewId(any(Integer.class));
        verify(mPresenter).setArgument(any(Bundle.class));
    }

//    @Test
//    public void testGetScreenId() throws Exception {
//        mFragmentRule.launchActivity(null);
//
//        when(mFragmentController.getScreenIdInContainer()).thenReturn(ScreenId.HOME);
//
//        assertThat(mFragmentRule.getFragment().getScreenId(), is(ScreenId.HOME));
//    }

    @Test
    public void testOnGoBack() throws Exception {
        mFragmentRule.launchActivity(null);

        when(mFragmentController.goBack()).thenReturn(true);

        boolean actual = mFragmentRule.getFragment().onGoBack();

        verify(mPresenter).removeTitle();
        assertThat(actual, is(true));
    }

    @Test
    public void testOnGoBackInOther() throws Exception {
        mFragmentRule.launchActivity(null);

        when(mFragmentController.goBack()).thenReturn(false);

        boolean actual = mFragmentRule.getFragment().onGoBack();

        verify(mPresenter, never()).removeTitle();
        assertThat(actual, is(false));
    }

    @Test
    public void testOnNavigate() throws Exception {
        mFragmentRule.launchActivity(null);
        ScreenId screenId = ScreenId.SEARCH_MUSIC_RESULTS;
        Bundle args = Bundle.EMPTY;

        when(mFragmentController.navigate(any(ScreenId.class), any(Bundle.class))).thenReturn(true);

        boolean actual = mFragmentRule.getFragment().onNavigate(screenId, args);

        verify(mPresenter).setTitle(any(Bundle.class));
        assertThat(actual, is(true));
    }

    @Test
    public void testOnNavigateInOther() throws Exception {
        mFragmentRule.launchActivity(null);
        ScreenId screenId = ScreenId.SEARCH_MUSIC_RESULTS;
        Bundle args = Bundle.EMPTY;

        when(mFragmentController.navigate(any(ScreenId.class), any(Bundle.class))).thenReturn(false);

        boolean actual = mFragmentRule.getFragment().onNavigate(screenId, args);

        verify(mPresenter, never()).setTitle(any(Bundle.class));
        assertThat(actual, is(false));
    }

    @Test
    public void testBack() throws Exception {
        mFragmentRule.launchActivity(null);

        final SearchContainerFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setTitle("TEST"));

        onView(withText("TEST")).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).perform(click());

        verify(mPresenter).onBackAction();
    }
}