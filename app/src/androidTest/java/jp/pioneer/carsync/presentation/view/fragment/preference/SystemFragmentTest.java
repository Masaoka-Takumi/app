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
import jp.pioneer.carsync.domain.model.AttMuteSetting;
import jp.pioneer.carsync.domain.model.SteeringRemoteControlSettingType;
import jp.pioneer.carsync.presentation.presenter.SystemPresenter;
import jp.pioneer.carsync.presentation.view.SystemView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withTagKey;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/09/25.
 */
public class SystemFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<SystemFragment> mFragmentRule = new FragmentTestRule<SystemFragment>() {
        private SystemFragment mFragment;

        @Override
        protected SystemFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new SystemFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SystemPresenter mPresenter;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        SystemFragmentTest.TestPresenterComponent presenterComponent(SystemFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = SystemFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public SystemPresenter provideSystemPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        SystemFragmentTest.TestAppComponent appComponent = DaggerSystemFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        SystemFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new SystemFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SystemFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(SystemFragmentTest.TestPresenterComponent.class), any(SystemFragment.class))).thenReturn(fragmentComponent);

    }

    @Test
    public void display() throws Exception {
        // exercise
        launchActivity();

        // verify
        onView(withText("Beep Tone")).check(matches(isEnabled()));
        onView(withText("Auto PI")).check(matches(isEnabled()));
        onView(withText("Demo Mode")).check(matches(isEnabled()));
        onView(withText("AUX")).check(matches(isEnabled()));
        onView(withText("BT Audio")).check(matches(isEnabled()));
        onView(withId(R.id.list)).perform(swipeUp());
        onView(withText("Pandora")).check(matches(isEnabled()));
        onView(withText("Spotify")).check(matches(isEnabled()));
        onView(withText("Power Save")).check(matches(isEnabled()));
        onView(withText("App Auto Launch")).check(matches(isEnabled()));
        onView(withText("Steering Remote Control")).check(matches(isEnabled()));
        onView(withId(R.id.list)).perform(swipeUp());
        onView(withText("USB Auto")).check(matches(isEnabled()));
        onView(withText("Display Off")).check(matches(isEnabled()));
        onView(withText("ATT/Mute")).check(matches(isEnabled()));
        onView(withText("Initial Settings")).check(matches(isEnabled()));
    }

    @Test
    public void onClickBeepTone() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("Beep Tone")).perform(click());

        // verify
        verify(mPresenter).onSelectBeepToneSettingAction(false);
    }

    @Test
    public void onClickAutoPi() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("Auto PI")).perform(click());

        // verify
        verify(mPresenter).onSelectAutoPiSettingAction(false);
    }

    @Test
    public void onClickDemoMode() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("Demo Mode")).perform(click());

        // verify
        verify(mPresenter).onSelectDemoModeSettingAction(false);
    }

    @Test
    public void onClickAux() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("AUX")).perform(click());

        // verify
        verify(mPresenter).onSelectAuxSettingAction(false);
    }

    @Test
    public void onClickBtAudio() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("BT Audio")).perform(click());

        // verify
        verify(mPresenter).onSelectBtAudioSettingAction(false);
    }

    @Test
    public void onClickPandora() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("Pandora")).perform(click());

        // verify
        verify(mPresenter).onSelectPandoraSettingAction(false);
    }

    @Test
    public void onClickSpotify() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withId(R.id.list)).perform(swipeUp());
        Thread.sleep(50);
        onView(withText("Spotify")).perform(click());

        // verify
        verify(mPresenter).onSelectSpotifySettingAction(false);
    }

    @Test
    public void onClickPowerSave() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withId(R.id.list)).perform(swipeUp());
        Thread.sleep(50);
        onView(withText("Power Save")).perform(click());

        // verify
        verify(mPresenter).onSelectPowerSaveModeSettingAction(false);
    }

    @Test
    public void onClickAppAutoLaunch() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withId(R.id.list)).perform(swipeUp());
        Thread.sleep(50);
        onView(withText("App Auto Launch")).perform(click());

        // verify
        verify(mPresenter).onSelectAppAutoLaunchSettingAction(false);
    }

    @Test
    public void onClickUsbAuto() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withId(R.id.list)).perform(swipeUp());
        onView(withId(R.id.list)).perform(swipeUp());
        Thread.sleep(50);
        onView(withText("USB Auto")).perform(click());

        // verify
        verify(mPresenter).onSelectUsbAutoSettingAction(false);
    }

    @Test
    public void onClickDisplayOff() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withId(R.id.list)).perform(swipeUp());
        onView(withId(R.id.list)).perform(swipeUp());
        Thread.sleep(50);
        onView(withText("Display Off")).perform(click());

        // verify
        verify(mPresenter).onSelectDisplayOffSettingAction(false);
    }

    @Test
    public void onClickAttMute() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withId(R.id.list)).perform(swipeUp());
        onView(withId(R.id.list)).perform(swipeUp());
        Thread.sleep(50);
        onView(withText("ATT/Mute")).perform(click());

        // verify
        verify(mPresenter).onSelectAttMuteSettingAction();
    }

    @Test
    public void onClickInitialSettings() throws Exception {
        // setup
        launchActivity();

        // exercise
        onView(withId(R.id.list)).perform(swipeUp());
        onView(withId(R.id.list)).perform(swipeUp());
        Thread.sleep(50);
        onView(withText("Initial Settings")).perform(click());

        // verify
        verify(mPresenter).onInitialSettingAction();
    }

    private void launchActivity() {
        mFragmentRule.launchActivity(null);
        SystemFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setBeepToneSetting(true, true,true);
            fragment.setAutoPiSetting(true, true,true);
            fragment.setDemoModeSetting(true, true,true);
            fragment.setAuxSetting(true, true,true);
            fragment.setBtAudioSetting(true, true,true);
            fragment.setPandoraSetting(true, true,true);
            fragment.setSpotifySetting(true, true,true);
            fragment.setPowerSaveModeSetting(true, true,true);
            fragment.setAppAutoLaunchSetting(true, true,true);
            fragment.setUsbAutoSetting(true, true,true);
            fragment.setDisplayOffSetting(true,true,true);
            fragment.setAttMuteSetting(true,true, AttMuteSetting.ATT);
            fragment.setInitialSettings(true,true);
        });
    }

}