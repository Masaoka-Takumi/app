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
import jp.pioneer.carsync.domain.model.FMTunerSetting;
import jp.pioneer.carsync.domain.model.LocalSetting;
import jp.pioneer.carsync.domain.model.PCHManualSetting;
import jp.pioneer.carsync.presentation.presenter.RadioFunctionSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/07/19.
 */
public class RadioFunctionSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<RadioFunctionSettingFragment> mFragmentRule = new FragmentTestRule<RadioFunctionSettingFragment>() {
        private RadioFunctionSettingFragment mFragment;

        @Override
        protected RadioFunctionSettingFragment createDialogFragment() {
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = RadioFunctionSettingFragment.newInstance(Bundle.EMPTY));
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock RadioFunctionSettingPresenter mPresenter;

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
        public RadioFunctionSettingPresenter provideRadioFunctionSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerRadioFunctionSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, RadioFunctionSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(RadioFunctionSettingFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void display() throws Exception {
        // exercise
        launchActivity();

        // verify
        onView(withText("FM Setting")).check(matches(isEnabled()));
        onView(withText("Region")).check(matches(isEnabled()));
        onView(withText("Local")).check(matches(isEnabled()));
        onView(withText("AF")).check(matches(isEnabled()));
        onView(withId(R.id.list)).perform(swipeUp());
        onView(withText("News")).check(matches(isEnabled()));
        onView(withText("Alarm")).check(matches(isEnabled()));
        onView(withText("Seek")).check(matches(isEnabled()));
    }

    @Test
    public void onClickFmTuner() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withText("FM Setting")).perform(click());
        onView(withText("Talk")).perform(click());

        // verify
        verify(mPresenter).onSelectFmTunerSettingAction(FMTunerSetting.TALK);
    }

    @Test
    public void onClickRegion() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withText("Region")).perform(click());

        // verify
        verify(mPresenter).onSelectRegionSettingAction(false);
    }

    @Test
    public void onClickLocal() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withText("Local")).perform(click());
        onView(withText("Level2")).perform(click());

        // verify
        verify(mPresenter).onSelectLocalSettingAction(LocalSetting.LEVEL2);
    }

    @Test
    public void onClickAf() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withText("AF")).perform(click());

        // verify
        verify(mPresenter).onSelectAfSettingAction(false);
    }

    @Test
    public void onClickNews() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withText("News")).perform(click());

        // verify
        verify(mPresenter).onSelectNewsSettingAction(false);
    }

    @Test
    public void onClickAlarm() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withId(R.id.list)).perform(swipeUp());
        onView(withText("Alarm")).perform(click());

        // verify
        verify(mPresenter).onSelectAlarmSettingAction(false);
    }

    @Test
    public void onClickSeek() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withId(R.id.list)).perform(swipeUp());
        onView(withText("Seek")).perform(click());

        // verify
        verify(mPresenter).onSelectPchManualSettingAction();
    }

    private void launchActivity() {
        mFragmentRule.launchActivity(null);
        RadioFunctionSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setFmTunerSetting(true,true,FMTunerSetting.STANDARD);
            fragment.setRegionSetting(true,true,true);
            fragment.setLocalSetting(true,true,LocalSetting.LEVEL3);
            fragment.setAfSetting(true,true,true);
            fragment.setNewsSetting(true,true,true);
            fragment.setAlarmSetting(true,true,true);
            fragment.setPchManual(true,true,PCHManualSetting.MANUAL);
        });
    }

}