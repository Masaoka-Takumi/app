package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.app.Instrumentation;
import android.os.Bundle;
import android.provider.Settings;
import android.support.test.InstrumentationRegistry;

import org.junit.After;
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
import jp.pioneer.carsync.presentation.model.SettingEntrance;
import jp.pioneer.carsync.presentation.presenter.SettingsEntrancePresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/05/31.
 */
public class SettingsEntranceFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<SettingsEntranceFragment> mFragmentRule = new FragmentTestRule<SettingsEntranceFragment>() {
        private SettingsEntranceFragment mFragment;
        @Override
        protected SettingsEntranceFragment createDialogFragment() {
            return SettingsEntranceFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SettingsEntrancePresenter mPresenter;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        SettingsEntranceFragmentTest.TestPresenterComponent presenterComponent(SettingsEntranceFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = SettingsEntranceFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }
    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public SettingsEntrancePresenter provideSettingsEntrancePresenter() {
            return mPresenter;
        }

    }
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        SettingsEntranceFragmentTest.TestAppComponent appComponent = DaggerSettingsEntranceFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        SettingsEntranceFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new SettingsEntranceFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SettingsEntranceFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(SettingsEntranceFragmentTest.TestPresenterComponent.class), any(SettingsEntranceFragment.class))).thenReturn(fragmentComponent);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnShowMessage() throws Exception {
        mFragmentRule.launchActivity(null);
        final SettingsEntranceFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.onShowMessage("test");
        });
    }

    //
    public void testOnShowAndroidSettings() throws Exception {
        mFragmentRule.launchActivity(null);
        final SettingsEntranceFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.onShowAndroidSettings(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
        });
    }

    @Test
    public void testSetAdapter() throws Exception {
        ArrayList<Integer> mIconArray = new ArrayList<>();
        ArrayList<SettingEntrance> mTitleArray = new ArrayList<>();
        mIconArray.add(R.drawable.p0091_icon);
        mTitleArray.add(SettingEntrance.SYSTEM);
        mFragmentRule.launchActivity(null);
        final SettingsEntranceFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mIconArray,mTitleArray);
        });
        onView(withId(R.id.setting_view)).perform(click());
    }
}