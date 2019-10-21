package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

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
import jp.pioneer.carsync.domain.model.DimmerSetting;
import jp.pioneer.carsync.domain.model.DimmerTimeType;
import jp.pioneer.carsync.presentation.presenter.DimmerSettingPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ディマー設定画面のテスト
 */
public class DimmerSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<DimmerSettingFragment> mFragmentRule = new FragmentTestRule<DimmerSettingFragment>() {
        @Override
        protected DimmerSettingFragment createDialogFragment() {
            return DimmerSettingFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock DimmerSettingPresenter mPresenter;
    private ArrayList<String> mTypeArray = new ArrayList<>();

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
        public DimmerSettingPresenter provideDimmerSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerDimmerSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, DimmerSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(DimmerSettingFragment.class))).thenReturn(fragmentComponent);

        mTypeArray.clear();
        mTypeArray.add("OFF");
        mTypeArray.add("Always ON");
        mTypeArray.add("AUTO");
        mTypeArray.add("Sync Clock");
        mTypeArray.add("Start");
        mTypeArray.add("Stop");
    }

    @Test
    public void testLifecycle() throws Exception {
        mFragmentRule.launchActivity(null);
        DimmerSettingFragment fragment = mFragmentRule.getFragment();

        assertThat(fragment.getScreenId(), is(ScreenId.ILLUMINATION_DIMMER_SETTING));
    }

    @Test
    public void testDisplayOFF() throws Exception {
        mFragmentRule.launchActivity(null);
        DimmerSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setSelectedItem(0);
        });

        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(0)
                .onChildView(withId(R.id.check))
                .check(matches(isDisplayed()));
        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(0)
                .perform(click());

        verify(mPresenter).onSelectDimmerAction(0);
    }

    @Test
    public void testDisplayIllumiLine() throws Exception {
        mFragmentRule.launchActivity(null);
        DimmerSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setSelectedItem(2);
        });

        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(2)
                .onChildView(withId(R.id.check))
                .check(matches(isDisplayed()));
        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(2)
                .perform(click());

        verify(mPresenter).onSelectDimmerAction(2);
    }

    @Test
    public void testDisplaySyncClock() throws Exception {
        mFragmentRule.launchActivity(null);
        DimmerSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setSelectedItem(3);
            fragment.setDimmerSchedule(18,0,6,0);
        });

        onView(withText("06:00 AM")).check(matches(isDisplayed()));
        onView(withText("06:00 PM")).check(matches(isDisplayed()));
        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(3)
                .onChildView(withId(R.id.check))
                .check(matches(isDisplayed()));
        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(3)
                .perform(click());

        verify(mPresenter).onSelectDimmerAction(3);
    }

    @Test
    public void testShowStartTimePickerDialog() throws Exception {
        mFragmentRule.launchActivity(null);
        DimmerSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setDimmerSchedule(18,0,6,0);
        });

        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(4)
                .perform(click());

        onView(withText("午後")).check(matches(isDisplayed()));
        onView(withText("6")).check(matches(isDisplayed()));
        onView(withText(":")).check(matches(isDisplayed()));
        onView(withText("00")).check(matches(isDisplayed()));
    }

    @Test
    public void testShowEndTimePickerDialog() throws Exception {
        mFragmentRule.launchActivity(null);
        DimmerSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setDimmerSchedule(18,0,6,0);
        });

        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(5)
                .perform(click());

        onView(withText("午前")).check(matches(isDisplayed()));
        onView(withText("6")).check(matches(isDisplayed()));
        onView(withText(":")).check(matches(isDisplayed()));
        onView(withText("00")).check(matches(isDisplayed()));
    }
}