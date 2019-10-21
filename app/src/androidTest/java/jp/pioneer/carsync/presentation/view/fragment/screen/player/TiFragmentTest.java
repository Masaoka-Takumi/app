package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;
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
import jp.pioneer.carsync.presentation.presenter.TiPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TI再生画面の
 */
public class TiFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<TiFragment> mFragmentRule = new FragmentTestRule<TiFragment>() {
        private TiFragment mFragment;

        @Override
        protected TiFragment createDialogFragment() {
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = TiFragment.newInstance(Bundle.EMPTY));
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock TiPresenter mPresenter;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        TiFragmentTest.TestPresenterComponent presenterComponent(TiFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = TiFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public TiPresenter provideTiPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TiFragmentTest.TestAppComponent appComponent = DaggerTiFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TiFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TiFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, TiFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TiFragmentTest.TestPresenterComponent.class), any(TiFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testLifecycle() throws Exception {
        mFragmentRule.launchActivity(null);
        TiFragment fragment = mFragmentRule.getFragment();

        assertThat(fragment.getScreenId(), is(ScreenId.TI));
    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        final TiFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setFrequency("100.00MHz");
            fragment.setAntennaLevel(0.8f);
        });
        Thread.sleep(500);
        onView(withId(R.id.frequency_text)).check(matches(withText("100")));
        onView(withId(R.id.frequency_decimal_text)).check(matches(withText(".00")));
        onView(withId(R.id.frequency_unit_text)).check(matches(withText("MHz")));
    }

    /**
     * 左フリックのテスト
     */
    @Test
    public void testOnFlickLeft() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.jacket_view)).perform(swipeLeft());
        verify(mPresenter).onNextChannelAction();
    }

    /**
     * 右フリックのテスト
     */
    @Test
    public void testOnFlickRight() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.jacket_view)).perform(swipeRight());
        verify(mPresenter).onPreviousChannelAction();
    }

    @Test
    public void testOnClickSourceButton() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.source_button)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled();
            }

            @Override
            public String getDescription() {
                return "click Left button";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        });
        verify(mPresenter).onSelectSourceAction();
    }

    @Test
    public void testOnClickVisualizerButton() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.visualizer_button)).perform(click());
        verify(mPresenter).onSelectVisualAction();
    }

    @Test
    public void testOnClickFxButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.fx_button)).perform(click());
        verify(mPresenter).onSelectFxAction();
    }

}