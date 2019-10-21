package jp.pioneer.carsync.presentation.view.fragment.screen.home;

import android.os.Bundle;
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
import jp.pioneer.carsync.presentation.presenter.OpeningEulaPresenter;


import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TermsAndConditionsFragmentのテスト
 */
public class TermsAndConditionsFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<OpeningEulaFragment> mFragmentRule = new FragmentTestRule<OpeningEulaFragment>() {
        private OpeningEulaFragment mFragment;
        @Override
        protected OpeningEulaFragment createDialogFragment() {
            return OpeningEulaFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock OpeningEulaPresenter mPresenter;
    @Mock AppSharedPreference mPreference;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        TermsAndConditionsFragmentTest.TestPresenterComponent presenterComponent(TermsAndConditionsFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = TermsAndConditionsFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }
    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public OpeningEulaPresenter provideTermsAndConditionsPresenter() {
            return mPresenter;
        }

    }
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerTermsAndConditionsFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TermsAndConditionsFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, OpeningEulaFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(OpeningEulaFragment.class))).thenReturn(fragmentComponent);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnClickAcceptButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final OpeningEulaFragment fragment = mFragmentRule.getFragment();
        onView(ViewMatchers.withId(R.id.agree)).perform(click());
        verify(mPresenter).onAcceptAction();
    }

}