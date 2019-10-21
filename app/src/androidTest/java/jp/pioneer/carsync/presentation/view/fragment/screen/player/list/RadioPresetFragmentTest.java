package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.LoaderManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

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
import jp.pioneer.carsync.presentation.model.AbstractPresetItem;
import jp.pioneer.carsync.presentation.model.RadioPresetItem;
import jp.pioneer.carsync.presentation.presenter.RadioPresetPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ラジオプリセットリスト画面のテストコード
 */
public class RadioPresetFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<RadioPresetFragment> mFragmentRule = new FragmentTestRule<RadioPresetFragment>() {
        @Override
        protected RadioPresetFragment createDialogFragment() {
            return RadioPresetFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock RadioPresetPresenter mPresenter;
    private ArrayList<AbstractPresetItem> mPresetList = new ArrayList<>();

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
        public RadioPresetPresenter provideRadioPresetPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerRadioPresetFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, RadioPresetFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(RadioPresetFragment.class))).thenReturn(fragmentComponent);

        mPresetList.add(new RadioPresetItem(RadioBandType.FM1,1, "Station1", "99.99mHz", true));
        mPresetList.add(new RadioPresetItem(RadioBandType.FM1,2, "Station2", "99.99mHz", false));
        mPresetList.add(new RadioPresetItem(RadioBandType.FM1,3, "Station3", "99.99mHz", false));
        mPresetList.add(new RadioPresetItem(RadioBandType.FM1,4, "Station4", "99.99mHz", false));
        mPresetList.add(new RadioPresetItem(RadioBandType.FM1,5, "Station5", "99.99mHz", false));
        mPresetList.add(new RadioPresetItem(RadioBandType.FM1,6, "Station6", "99.99mHz", false));
    }

    @Test
    public void testLifecycle() throws Exception {

        mFragmentRule.launchActivity(null);
        RadioPresetFragment fragment = mFragmentRule.getFragment();

        verify(mPresenter).setLoaderManager(any(LoaderManager.class));
        assertThat(fragment.getScreenId(), is(ScreenId.RADIO_PRESET_LIST));
    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioPresetFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setPresetList(mPresetList);
        });
        onView(withText("Station1")).check(matches(isDisplayed()));
        onView(withText("Station2")).check(matches(isDisplayed()));
        onView(withText("Station3")).check(matches(isDisplayed()));
        onView(withText("Station4")).check(matches(isDisplayed()));
        onView(withText("Station5")).check(matches(isDisplayed()));
        onView(withText("Station6")).check(matches(isDisplayed()));
    }

    @Test
    public void testOnClickPreset() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioPresetFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setPresetList(mPresetList);
        });
        onView(withId(R.id.preset_button2)).perform(click());

        verify(mPresenter).onSelectPresetNumber(2);
    }
}