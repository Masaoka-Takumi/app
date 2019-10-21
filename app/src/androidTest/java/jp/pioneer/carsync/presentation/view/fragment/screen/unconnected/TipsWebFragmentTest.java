package jp.pioneer.carsync.presentation.view.fragment.screen.unconnected;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

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
import jp.pioneer.carsync.presentation.presenter.TipsWebPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2018/01/24.
 */
public class TipsWebFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<TipsWebFragment> mFragmentRule = new FragmentTestRule<TipsWebFragment>() {
        @Override
        protected TipsWebFragment createDialogFragment() {
            return TipsWebFragment.newInstance(Bundle.EMPTY);
        }

    };
    @Mock ComponentFactory mComponentFactory;
    @Mock TipsWebPresenter mPresenter;
    
    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        TipsWebFragmentTest.TestPresenterComponent presenterComponent(TipsWebFragmentTest.TestPresenterModule module);
    }
    @PresenterLifeCycle
    @Subcomponent(modules = TipsWebFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public TipsWebPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TipsWebFragmentTest.TestAppComponent appComponent = DaggerTipsWebFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TipsWebFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TipsWebFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, TipsWebFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TipsWebFragmentTest.TestPresenterComponent.class), any(TipsWebFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void loadUrl() throws Exception {
        mFragmentRule.launchActivity(null);
        final TipsWebFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.loadUrl("https://www.google.co.jp/");
        });
    }

    @Test
    public void onBackClickButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.back_btn)).perform(click());
        verify(mPresenter).onBackAction();
    }

}