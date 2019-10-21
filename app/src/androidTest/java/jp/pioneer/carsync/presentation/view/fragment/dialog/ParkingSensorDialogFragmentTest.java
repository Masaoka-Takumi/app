package jp.pioneer.carsync.presentation.view.fragment.dialog;

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
import jp.pioneer.carsync.DialogFragmentTestRule;
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
import jp.pioneer.carsync.domain.model.ParkingSensorStatus;
import jp.pioneer.carsync.domain.model.SensorDistanceUnit;
import jp.pioneer.carsync.domain.model.SensorStatus;
import jp.pioneer.carsync.presentation.presenter.ParkingSensorDialogPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.DialogFragmentTestRule.getTestApp;
import static jp.pioneer.carsync.presentation.view.EspressoTestsMatchers.withDrawable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test
 */
public class ParkingSensorDialogFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<ParkingSensorDialogFragment> mFragmentRule = new DialogFragmentTestRule<ParkingSensorDialogFragment>() {
        @Override
        protected ParkingSensorDialogFragment createDialogFragment() {
            return ParkingSensorDialogFragment.newInstance(null, Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock ParkingSensorDialogPresenter mPresenter;
    @Mock ParkingSensorDialogFragment.Callback mCallback;
    private ParkingSensorStatus mStatus = new ParkingSensorStatus();
    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        ParkingSensorDialogFragmentTest.TestPresenterComponent presenterComponent(ParkingSensorDialogFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = ParkingSensorDialogFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public ParkingSensorDialogPresenter provideParkingSensorDialogPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        ParkingSensorDialogFragmentTest.TestAppComponent appComponent = DaggerParkingSensorDialogFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        ParkingSensorDialogFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new ParkingSensorDialogFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ParkingSensorDialogFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(ParkingSensorDialogFragmentTest.TestPresenterComponent.class), any(ParkingSensorDialogFragment.class))).thenReturn(fragmentComponent);

        mStatus.sensorStatusA = SensorStatus.NORMAL;
        mStatus.sensorStatusB = SensorStatus.NORMAL;
        mStatus.sensorStatusC = SensorStatus.NORMAL;
        mStatus.sensorStatusD = SensorStatus.NORMAL;
        mStatus.sensorDistanceA = 12;
        mStatus.sensorDistanceB = 1;
        mStatus.sensorDistanceC = 13;
        mStatus.sensorDistanceD = 24;
        mStatus.sensorDistanceUnit = SensorDistanceUnit._0_1M;
    }

    @Test
    public void setSensorNormal() throws Exception {
        mFragmentRule.launchActivity(null);
        ParkingSensorDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setSensor(mStatus);
        });
        Thread.sleep(300);
        onView(withId(R.id.distance_text)).check(matches(withText("0.1")));
        onView(withId(R.id.distance_unit_text)).check(matches(withText("m")));
/*        onView(withId(R.id.sensor_a_circle)).check(matches(not(isDisplayed())));
        onView(withId(R.id.sensor_b_circle)).check(matches(not(isDisplayed())));
        onView(withId(R.id.sensor_c_circle)).check(matches(not(isDisplayed())));
        onView(withId(R.id.sensor_d_circle)).check(matches(isDisplayed()));*/
    }

    @Test
    public void setSensorNull() throws Exception {
        mFragmentRule.launchActivity(null);
        mStatus.sensorStatusB = null;
        mStatus.sensorStatusC = null;
        mStatus.sensorStatusD = null;
        ParkingSensorDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setSensor(mStatus);
        });
        Thread.sleep(300);
        onView(withId(R.id.distance_text)).check(matches(withText("1.2")));
        onView(withId(R.id.distance_unit_text)).check(matches(withText("m")));
/*        onView(withId(R.id.sensor_a_circle)).check(matches(isDisplayed()));
        onView(withId(R.id.sensor_b_circle)).check(matches(not(isDisplayed())));
        onView(withId(R.id.sensor_c_circle)).check(matches(not(isDisplayed())));
        onView(withId(R.id.sensor_d_circle)).check(matches(not(isDisplayed())));*/
    }

    @Test
    public void setSensorError() throws Exception {
        mFragmentRule.launchActivity(null);
        mStatus.sensorStatusB = SensorStatus.ERROR;
        mStatus.sensorStatusC = SensorStatus.ERROR;
        mStatus.sensorStatusD = SensorStatus.ERROR;
        ParkingSensorDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setSensor(mStatus);
        });
        Thread.sleep(300);
        onView(withId(R.id.error_layout)).check(matches(isDisplayed()));
        onView(withId(R.id.sensor_a_icon)).check(matches(withDrawable(R.drawable.p1212_ok_pserror)));
        onView(withId(R.id.sensor_b_icon)).check(matches(withDrawable(R.drawable.p1213_error_pserror)));
        onView(withId(R.id.sensor_c_icon)).check(matches(withDrawable(R.drawable.p1213_error_pserror)));
        onView(withId(R.id.sensor_d_icon)).check(matches(withDrawable(R.drawable.p1213_error_pserror)));

    }

    @Test
    public void dismissDialog() throws Exception {
        mFragmentRule.launchActivity(null);
        ParkingSensorDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.dismissDialog();
        });
    }

}