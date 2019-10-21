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
import jp.pioneer.carsync.presentation.presenter.SxmPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.presentation.view.EspressoTestsMatchers.withDrawable;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008316 on 2017/06/09.
 */
public class SxmFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<SxmFragment> mFragmentRule = new FragmentTestRule<SxmFragment>() {
        private SxmFragment mFragment;

        @Override
        protected SxmFragment createDialogFragment() {
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = SxmFragment.newInstance(Bundle.EMPTY));
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SxmPresenter mPresenter;

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
        public SxmPresenter provideSxmPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerSxmFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SxmFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(SxmFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testLifecycle() throws Exception {
        mFragmentRule.launchActivity(null);
        SxmFragment fragment = mFragmentRule.getFragment();

        verify(mPresenter).setLoaderManager(any(LoaderManager.class));
        assertThat(fragment.getScreenId(), is(ScreenId.SIRIUS_XM));
    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        doAnswer(invocationOnMock -> {
            fragment.setFavorite(false);
            return null;
        }).when(mPresenter).onFavoriteAction();
        doAnswer(invocationOnMock -> {
            fragment.setReplayMode(true);
            return null;
        }).when(mPresenter).onReplayAction();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setTitle("TEST : TITLE");
            fragment.setArtist("TEST : ARTIST");
            fragment.setChannelNumber("CH 003");
            fragment.setPch(1);
            fragment.setFavorite(true);
            fragment.setBand("SXM1");
            fragment.setReplayMode(false);
            fragment.setChannelName("TEST : CHANNEL");
            fragment.setCategory("TEST : CATEGORY");
            fragment.setMaxProgress(50);
            fragment.setCurrentProgress(10);
            fragment.setAntennaLevel(0.8f);
        });
        onView(withText("TEST : TITLE")).check(matches(isDisplayed()));
        onView(withText("TEST : ARTIST")).check(matches(isDisplayed()));
        onView(withText("CH 003")).check(matches(isDisplayed()));
        onView(withId(R.id.favorite_view)).check(matches(withDrawable(R.drawable.p0040_favoselectbtn_1nrm)));
        onView(withText("SXM1")).check(matches(isDisplayed()));
        onView(withId(R.id.replay_button)).check(matches(isDisplayed()));
        onView(withId(R.id.tune_mix_button)).check(matches(isDisplayed()));
        onView(withId(R.id.replay_button)).check(matches(isDisplayed()));
        onView(withId(R.id.favorite_view)).perform(click());
        onView(withId(R.id.channel_name_text)).check(matches(withText("TEST : CHANNEL")));
        onView(withId(R.id.music_info_text)).check(matches(withText("TEST : CATEGORY")));
         /*
         * Viewが重なっている場合、espresso標準の#clickでは、
         * 対象のViewが90%以上表示されていないとクリックできないため、
         * クリックの方法を変更する。
         *
         * 当テストクラスの他メソッドも同様
         */
        onView(withId(R.id.replay_button)).check(matches(allOf(isEnabled(), isClickable()))).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isEnabled();
                    }

                    @Override
                    public String getDescription() {
                        return "click plus button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );
        onView(withId(R.id.favorite_view)).check(matches(withDrawable(R.drawable.p0039_favobtn_1nrm)));
        onView(withId(R.id.channel_button)).check(matches(isDisplayed()));
        onView(withId(R.id.live_button)).check(matches(isDisplayed()));
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()));
    }

    @Test
    public void testShowSubscription() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.showSubscription());

        onView(withText(getTargetContext().getString(R.string.sxm_subscription_update))).check(matches(isDisplayed()));
        onView(withText("OK")).perform(click());

        verify(mPresenter).onReleaseSubscription();
    }

    @Test
    public void testOnClickBandButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setTitle("TEST : TITLE");
            fragment.setArtist("TEST : ARTIST");
            fragment.setFavorite(true);
            fragment.setBand("SXM1");
            fragment.setReplayMode(false);
            fragment.setChannelName("TEST : CHANNEL");
            fragment.setCategory("TEST : CATEGORY");
            fragment.setMaxProgress(50);
            fragment.setCurrentProgress(10);
            fragment.setAntennaLevel(0.8f);
            fragment.setChannelNumber("CH 003");
            fragment.setPch(1);
        });

        onView(withId(R.id.band_text)).perform(click());

        verify(mPresenter).onToggleBandAction();
    }

    @Test
    public void testOnClickFavoriteButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setTitle("TEST : TITLE");
            fragment.setArtist("TEST : ARTIST");
            fragment.setFavorite(true);
            fragment.setBand("SXM1");
            fragment.setReplayMode(false);
            fragment.setChannelName("TEST : CHANNEL");
            fragment.setCategory("TEST : CATEGORY");
            fragment.setMaxProgress(50);
            fragment.setCurrentProgress(10);
            fragment.setAntennaLevel(0.8f);
            fragment.setChannelNumber("CH 003");
            fragment.setPch(1);
        });

        onView(withId(R.id.favorite_view)).perform(click());

        verify(mPresenter).onFavoriteAction();
    }

    @Test
    public void testOnClickSourceButton() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.source_button)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled(); // no constraints, they are checked above
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
/*
    @Test
    public void testOnClickPresetChannelButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setTitle("TEST : TITLE");
            fragment.setArtist("TEST : ARTIST");
            fragment.setFrequency("TEST : XXX.xx");
            fragment.setPsInformation("TEST : PS INFORMATION");
            fragment.setFavorite(true);
            fragment.setBand(SxmBandType.SXM1);
            fragment.setReplayMode(false);
        });

        onView(withId(R.id.preset_channel_button)).check(matches(allOf(isEnabled(), isClickable()))).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isEnabled();
                    }

                    @Override
                    public String getDescription() {
                        return "click plus button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );
        verify(mPresenter).onPresetShowAction();
    }*/

    @Test
    public void testOnClickListButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setTitle("TEST : TITLE");
            fragment.setArtist("TEST : ARTIST");
            fragment.setFavorite(true);
            fragment.setBand("SXM1");
            fragment.setReplayMode(false);
            fragment.setChannelName("TEST : CHANNEL");
            fragment.setCategory("TEST : CATEGORY");
            fragment.setMaxProgress(50);
            fragment.setCurrentProgress(10);
            fragment.setAntennaLevel(0.8f);
            fragment.setChannelNumber("CH 003");
            fragment.setPch(1);
            fragment.setTuneMix(false);
        });

        onView(withId(R.id.list_button)).check(matches(allOf(isEnabled(), isClickable()))).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isEnabled();
                    }

                    @Override
                    public String getDescription() {
                        return "click plus button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );
        verify(mPresenter).onSelectListAction();
    }

    @Test
    public void testOnClickReplayButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setTitle("TEST : TITLE");
            fragment.setArtist("TEST : ARTIST");
            fragment.setFavorite(true);
            fragment.setBand("SXM1");
            fragment.setReplayMode(false);
            fragment.setChannelName("TEST : CHANNEL");
            fragment.setCategory("TEST : CATEGORY");
            fragment.setMaxProgress(50);
            fragment.setCurrentProgress(10);
            fragment.setAntennaLevel(0.8f);
            fragment.setChannelNumber("CH 003");
            fragment.setPch(1);
        });

        onView(withId(R.id.replay_button)).check(matches(allOf(isEnabled(), isClickable()))).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isEnabled();
                    }

                    @Override
                    public String getDescription() {
                        return "click plus button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );
        verify(mPresenter).onReplayAction();
    }

    @Test
    public void testOnClickTuneMixButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setTitle("TEST : TITLE");
            fragment.setArtist("TEST : ARTIST");
            fragment.setFavorite(true);
            fragment.setBand("SXM1");
            fragment.setReplayMode(false);
            fragment.setChannelName("TEST : CHANNEL");
            fragment.setCategory("TEST : CATEGORY");
            fragment.setMaxProgress(50);
            fragment.setCurrentProgress(10);
            fragment.setAntennaLevel(0.8f);
            fragment.setChannelNumber("CH 003");
            fragment.setPch(1);
        });

        onView(withId(R.id.tune_mix_button)).check(matches(allOf(isEnabled(), isClickable()))).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isEnabled();
                    }

                    @Override
                    public String getDescription() {
                        return "click plus button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );
        verify(mPresenter).onTuneMixAction();
        instr.runOnMainSync(() -> {
            fragment.setTuneMix(true);
        });
        Thread.sleep(500);
    }

    @Test
    public void testOnClickLiveButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setTitle("TEST : TITLE");
            fragment.setArtist("TEST : ARTIST");
            fragment.setFavorite(true);
            fragment.setBand("SXM1");
            fragment.setReplayMode(false);
            fragment.setChannelName("TEST : CHANNEL");
            fragment.setCategory("TEST : CATEGORY");
            fragment.setMaxProgress(50);
            fragment.setCurrentProgress(10);
            fragment.setAntennaLevel(0.8f);
            fragment.setChannelNumber("CH 003");
            fragment.setPch(1);
        });

        onView(withId(R.id.live_button)).check(matches(allOf(isEnabled(), isClickable()))).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isEnabled();
                    }

                    @Override
                    public String getDescription() {
                        return "click plus button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );
        verify(mPresenter).onLiveAction();
    }

    @Test
    public void testOnClickChannelButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final SxmFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setTitle("TEST : TITLE");
            fragment.setArtist("TEST : ARTIST");
            fragment.setFavorite(true);
            fragment.setBand("SXM1");
            fragment.setReplayMode(false);
            fragment.setChannelName("TEST : CHANNEL");
            fragment.setCategory("TEST : CATEGORY");
            fragment.setMaxProgress(50);
            fragment.setCurrentProgress(10);
            fragment.setAntennaLevel(0.8f);
            fragment.setChannelNumber("CH 003");
            fragment.setPch(1);
        });

        onView(withId(R.id.channel_button)).check(matches(allOf(isEnabled(), isClickable()))).perform(
                new ViewAction() {
                    @Override
                    public Matcher<View> getConstraints() {
                        return isEnabled();
                    }

                    @Override
                    public String getDescription() {
                        return "click plus button";
                    }

                    @Override
                    public void perform(UiController uiController, View view) {
                        view.performClick();
                    }
                }
        );
        verify(mPresenter).onChannelAction();
    }
}