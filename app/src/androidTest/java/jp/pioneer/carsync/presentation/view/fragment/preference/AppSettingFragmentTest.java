package jp.pioneer.carsync.presentation.view.fragment.preference;

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
import jp.pioneer.carsync.domain.model.DistanceUnit;
import jp.pioneer.carsync.presentation.presenter.AppSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * App設定画面のテスト
 */
public class AppSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<AppSettingFragment> mFragmentRule = new FragmentTestRule<AppSettingFragment>() {
        private AppSettingFragment mFragment;

        @Override
        protected AppSettingFragment createDialogFragment() {
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = AppSettingFragment.newInstance(Bundle.EMPTY));
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock AppSettingPresenter mPresenter;

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
        public AppSettingPresenter provideAppSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerAppSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, AppSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(AppSettingFragment.class))).thenReturn(fragmentComponent);
    }

    /**
     * Viewの表示状態テスト
     */
    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        AppSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDistanceUnit(DistanceUnit.METER_KILOMETER);
            fragment.setShortCutEnabled(true);
            fragment.setHomeButtonEnabled(true);
            fragment.setAlbumArtEnabled(true);
            fragment.setGenreCardEnabled(true);
            fragment.setPlaylistCardEnabled(false);
        });
        onView(withText(R.string.setting_app_distance_unit_meter)).check(matches(isEnabled()));
        onView(withText(R.string.setting_app_short_cut_button)).check(matches(isEnabled()));
        onView(withText(R.string.setting_app_home_button)).check(matches(isEnabled()));
        onView(withText(R.string.setting_app_list_view_album_art)).check(matches(isDisplayed()));
        onView(withText(R.string.setting_app_list_view_card)).check(matches(isDisplayed()));
        onView(withText(R.string.setting_app_list_view_list)).check(matches(isDisplayed()));
    }

    /**
     * ボタンClickテスト
     */
    @Test
    public void testClick() throws Exception {
        mFragmentRule.launchActivity(null);
        AppSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDistanceUnit(DistanceUnit.METER_KILOMETER);
            fragment.setShortCutEnabled(false);
            fragment.setHomeButtonEnabled(false);
            fragment.setAlbumArtEnabled(false);
            fragment.setGenreCardEnabled(false);
            fragment.setPlaylistCardEnabled(true);
        });

        onView(withText(R.string.setting_app_distance_unit)).perform(click());
        onView(withText(R.string.setting_app_short_cut_button)).perform(click());
        onView(withText(R.string.setting_app_home_button)).perform(click());
        onView(withText(R.string.setting_app_album_list_view)).perform(click());
        onView(withText(R.string.setting_app_genre_list_view)).perform(click());
        onView(withText(R.string.setting_app_playlist_view)).perform(click());

        verify(mPresenter).onDistanceUnitChange();
        verify(mPresenter).onShortCutButtonChange(true);
        verify(mPresenter).onHomeButtonChange(true);
        verify(mPresenter).onAlbumArtChange();
        verify(mPresenter).onGenreCardChange();
        verify(mPresenter).onPlaylistChange();
    }
}