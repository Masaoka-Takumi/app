package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.internal.matchers.Null;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.FragmentTestRule;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.content.AppSharedPreference;
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
import jp.pioneer.carsync.presentation.presenter.NavigationPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/06/02.
 */
public class NavigationFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<NavigationFragment> mFragmentRule = new FragmentTestRule<NavigationFragment>() {
        @Override
        protected NavigationFragment createDialogFragment() {
            return NavigationFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock NavigationPresenter mPresenter;
    @Mock AppSharedPreference mPreference;

    private List<ApplicationInfo> mInstallApplications;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        NavigationFragmentTest.TestPresenterComponent presenterComponent(NavigationFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = NavigationFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public NavigationPresenter provideNavigationPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        NavigationFragmentTest.TestAppComponent appComponent = DaggerNavigationFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        NavigationFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new NavigationFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, NavigationFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(NavigationFragmentTest.TestPresenterComponent.class), any(NavigationFragment.class))).thenReturn(fragmentComponent);

        mInstallApplications = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ApplicationInfo info = new ApplicationInfo();
            info.packageName = "com.sample.app" + String.valueOf(i + 1);
            mInstallApplications.add(info);
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = NullPointerException.class)
    public void test1() throws Exception {
        mFragmentRule.launchActivity(null);
        final NavigationFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
//            fragment.setChoiceItems(mInstallApplications,1);
        });

        onView(withText("com.sample.app1")).perform(click());
//        verify(mPresenter).onNavigationAppSelected(any(ApplicationInfo.class));
        onView(withText("com.sample.app1")).check(matches(isDisplayed()));
    }
//
//    @Test
//    public void test2() throws Exception {
//        mFragmentRule.launchActivity(null);
//        final NavigationFragment fragment = mFragmentRule.getFragment();
//        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
//        instr.runOnMainSync(() -> {
//            fragment.setChoiceItems(mInstallApplications,1);
//        });
//
//        onView(withText("com.sample.app2")).perform(click());
//        verify(mPresenter).onNavigationAppSelected(any(ApplicationInfo.class));
//        onView(withText("com.sample.app2")).check(matches(isDisplayed()));
//    }
}