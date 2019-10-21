package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.v4.app.LoaderManager;
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
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.presentation.presenter.RadioPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.presentation.view.EspressoTestsMatchers.withDrawable;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ラジオ再生画面のテストコード
 */
public class RadioFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<RadioFragment> mFragmentRule = new FragmentTestRule<RadioFragment>() {
        private RadioFragment mFragment;

        @Override
        protected RadioFragment createDialogFragment() {
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = RadioFragment.newInstance(Bundle.EMPTY));
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock RadioPresenter mPresenter;

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
        public RadioPresenter provideRadioPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerRadioFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, RadioFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(RadioFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testLifecycle() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioFragment fragment = mFragmentRule.getFragment();

        verify(mPresenter).setLoaderManager(any(LoaderManager.class));
        assertThat(fragment.getScreenId(), is(ScreenId.RADIO));
    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        final RadioFragment fragment = mFragmentRule.getFragment();

        doAnswer(invocationOnMock -> {
            fragment.setFavorite(false);
            return null;
        }).when(mPresenter).onFavoriteAction();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            //fragment.setTitle("TEST : TITLE");
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setArtist("TEST : ARTIST");
            fragment.setFrequency("100.00MHz");
            fragment.setPch(1);
            fragment.setPsInformation("TEST : PS INFORMATION");
            fragment.setPtyName("TEST : PTY NAME");
            fragment.setFavorite(true);
            fragment.setBand(RadioBandType.FM1);
            fragment.setAntennaLevel(0.8f);
        });
        Thread.sleep(500);
        onView(withText("TEST : ARTIST")).check(matches(isDisplayed()));
        onView(withText("100")).check(matches(isDisplayed()));
        onView(withText(".00")).check(matches(isDisplayed()));
        onView(withText("MHz")).check(matches(isDisplayed()));
        onView(withId(R.id.pch_text)).check(matches(withText(("1"))));
        onView(withText("TEST : PS INFORMATION")).check(matches(isDisplayed()));
        onView(withId(R.id.favorite_view)).check(matches(withDrawable(R.drawable.p0040_favoselectbtn_1nrm)));
        onView(withText("FM1")).check(matches(isDisplayed()));
        instr.runOnMainSync(() -> fragment.setFrequency("100kHz"));
        onView(withId(R.id.favorite_view)).perform(click());
        onView(withId(R.id.favorite_view)).check(matches(withDrawable(R.drawable.p0039_favobtn_1nrm)));
    }

    @Test
    public void testOnClickFrequency() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.frequency_text)).perform(click());

        verify(mPresenter).onSeekUpAction();
    }

    @Test
    public void testOnClickFavoriteButton() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.favorite_view)).perform(click());

        verify(mPresenter).onFavoriteAction();
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

    @Test
    public void testOnClickListButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.list_button)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled();
            }

            @Override
            public String getDescription() {
                return "click Right button";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        });
        verify(mPresenter).onSelectListAction();
    }
}