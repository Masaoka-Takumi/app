package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.preference.SwitchPreferenceCompat;

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
import jp.pioneer.carsync.presentation.presenter.CarSafetyPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * CarSafety設定画面のTest
 */
public class CarSafetyFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<CarSafetyFragment> mFragmentRule = new FragmentTestRule<CarSafetyFragment>() {

        private CarSafetyFragment mFragment;

        @Override
        protected CarSafetyFragment createDialogFragment() {
            // 非UIスレッドから呼ばれるが、PreferenceFragmentCompatがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new CarSafetyFragment());
            return mFragment;
        }
    };

    @Mock CarSafetyPresenter mPresenter;
    @Mock ComponentFactory mComponentFactory;

    private Preference mImpactDetection;
    private SwitchPreferenceCompat mParkingSensorEnabled;
    private Preference mParkingSensorAlarmOutput;
    private SeekBarPreference mParkingSensorAlarmVolume;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        CarSafetyFragmentTest.TestPresenterComponent presenterComponent(CarSafetyFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = CarSafetyFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public CarSafetyPresenter provideCarSafetyPresenter() {
            return mPresenter;
        }

    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        CarSafetyFragmentTest.TestAppComponent appComponent = DaggerCarSafetyFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        CarSafetyFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new CarSafetyFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, CarSafetyFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(CarSafetyFragmentTest.TestPresenterComponent.class), any(CarSafetyFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testDisplayWhenSync() throws Exception {
        mFragmentRule.launchActivity(null);
        CarSafetyFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setParkingSensorSetting(true,true);
            fragment.setAdasSetting(true);
        });

        onView(withText(R.string.setting_car_safety_parking_sensor)).check(matches(isDisplayed()));
        onView(withText(R.string.setting_car_safety_impact_detection)).check(matches(isDisplayed()));
        onView(withText(R.string.setting_car_safety_adas)).check(matches(isDisplayed()));
    }

    @Test
    public void testDisplayWhenNonSync() throws Exception {
        mFragmentRule.launchActivity(null);
        CarSafetyFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setParkingSensorSetting(true,true);
            fragment.setAdasSetting(false);
        });

        Thread.sleep(200);

        onView(withText(R.string.setting_car_safety_impact_detection)).check(matches(isDisplayed()));
    }

}