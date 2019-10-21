package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
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
import jp.pioneer.carsync.domain.model.BtPhoneColor;
import jp.pioneer.carsync.domain.model.SphBtPhoneColorSetting;
import jp.pioneer.carsync.presentation.presenter.PhoneSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/09/22.
 */
public class PhoneSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<PhoneSettingFragment> mFragmentRule = new FragmentTestRule<PhoneSettingFragment>() {
        private PhoneSettingFragment mFragment;

        @Override
        protected PhoneSettingFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new PhoneSettingFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock PhoneSettingPresenter mPresenter;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        PhoneSettingFragmentTest.TestPresenterComponent presenterComponent(PhoneSettingFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = PhoneSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public PhoneSettingPresenter providePhoneSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        PhoneSettingFragmentTest.TestAppComponent appComponent = DaggerPhoneSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        PhoneSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new PhoneSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, PhoneSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(PhoneSettingFragmentTest.TestPresenterComponent.class), any(PhoneSettingFragment.class))).thenReturn(fragmentComponent);

    }

    @Test
    public void display() throws Exception {
        // exercise
        launchActivity();

        // verify
        onView(withText(R.string.setting_phone_device_settings)).check(matches(isEnabled()));
        onView(withText(R.string.setting_phone_auto_pairing)).check(matches(isEnabled()));
        onView(withText(R.string.setting_phone_direct_call)).check(matches(isEnabled()));
        onView(withText(R.string.setting_phone_book_access_setting)).check(matches(isEnabled()));
        onView(withText(R.string.setting_phone_incoming_call_pattern)).check(matches(isEnabled()));
        onView(withText(R.string.setting_phone_incoming_call_color)).check(matches(isEnabled()));
        onView(withText(R.string.setting_phone_incoming_call_auto_answer)).check(matches(isEnabled()));
    }

    @Test
    public void onClickDeviceSettings() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText(R.string.setting_phone_device_settings)).perform(click());

        // verify
        verify(mPresenter).onSelectDeviceSettingsAction();
    }

    @Test
    public void onClickAutoPairingSetting() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText(R.string.setting_phone_auto_pairing)).perform(click());

        // verify
        verify(mPresenter).onSelectAutoPairingAction(false);
    }

    @Test
    public void onClickDirectCallSetting() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText(R.string.setting_phone_direct_call)).perform(click());

        // verify
        verify(mPresenter).onSelectDirectCallAction();
    }

    @Test
    public void onClickPhoneBookAccessibleSetting() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText(R.string.setting_phone_book_access_setting)).perform(click());

        // verify
        verify(mPresenter).onSelectPhoneBookAccessibleAction(false);
    }

    @Test
    public void onClickIncomingCallPatternSetting() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText(R.string.setting_phone_incoming_call_pattern)).perform(click());

        // verify
        verify(mPresenter).onSelectIncomingCallPatternItemAction();
    }

    @Test
    public void onClickIncomingCallColorSetting() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText(R.string.setting_phone_incoming_call_color)).perform(click());

        // verify
        verify(mPresenter).onSelectIncomingCallColorItemAction();
    }

    @Test
    public void onClickAutoAnswerSetting() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText(R.string.setting_phone_incoming_call_auto_answer)).perform(click());

        // verify
        verify(mPresenter).onSelectIncomingCallAutoAnswer(false);
    }

    private void launchActivity() {
        mFragmentRule.launchActivity(null);
        PhoneSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceSettings(true, true);
            fragment.setAutoPairingSetting(true, true,true);
            fragment.setPhoneBookAccessibleSetting(true);
            fragment.setIncomingCallPatternSetting(true, true, BtPhoneColor.FLASHING_PATTERN1);
            fragment.setIncomingCallColorSetting(true, true, SphBtPhoneColorSetting.ORANGE);
            fragment.setAutoAnswerSetting(true, true,true);
        });
    }

}