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
import jp.pioneer.carsync.domain.model.AudioOutputMode;
import jp.pioneer.carsync.domain.model.AudioSetting;
import jp.pioneer.carsync.domain.model.AudioSettingStatus;
import jp.pioneer.carsync.domain.model.CarDeviceSpec;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.FaderBalanceSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.presenter.FaderBalanceSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/09/22.
 */
public class FaderBalanceSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<FaderBalanceSettingFragment> mFragmentRule = new FragmentTestRule<FaderBalanceSettingFragment>() {
        private FaderBalanceSettingFragment mFragment;

        @Override
        protected FaderBalanceSettingFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new FaderBalanceSettingFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock FaderBalanceSettingPresenter mPresenter;

    private AudioSetting audioSetting = new AudioSetting();
    private CarDeviceSpec spec = new CarDeviceSpec();

    private FaderBalanceSetting setting = new FaderBalanceSetting();


    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        FaderBalanceSettingFragmentTest.TestPresenterComponent presenterComponent(FaderBalanceSettingFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = FaderBalanceSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public FaderBalanceSettingPresenter provideFaderBalanceSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        FaderBalanceSettingFragmentTest.TestAppComponent appComponent = DaggerFaderBalanceSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        FaderBalanceSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new FaderBalanceSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, FaderBalanceSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(FaderBalanceSettingFragmentTest.TestPresenterComponent.class), any(FaderBalanceSettingFragment.class))).thenReturn(fragmentComponent);

        when(mPresenter.getUiColor()).thenReturn(UiColor.AQUA.getResource());

        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus status = new CarDeviceStatus();
        spec.audioSettingSpec.audioOutputMode = AudioOutputMode.STANDARD;
        setting.currentFader = 1;
        setting.currentBalance = -1;
        setting.minimumBalance = -12;
        setting.maximumBalance = 12;
        setting.minimumFader = -12;
        setting.maximumFader = 12;

        audioSetting.faderBalanceSetting = setting;

        AudioSettingStatus audioSettingStatus = new AudioSettingStatus();
        audioSettingStatus.faderSettingEnabled = true;
        audioSettingStatus.balanceSettingEnabled = true;

        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);
        when(mockHolder.getCarDeviceStatus()).thenReturn(status);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);
        when(mockHolder.getAudioSettingStatus()).thenReturn(audioSettingStatus);
        when(mPresenter.getStatusHolder()).thenReturn(mockHolder);
    }

    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final FaderBalanceSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            FaderBalanceSettingFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }

    @Test
    public void onButtonClicked() throws Exception {

        mFragmentRule.launchActivity(null);
        final FaderBalanceSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        onView(withId(R.id.centerPositionButton)).perform(click());
        verify(mPresenter).setFaderBalance(0,0);
        onView(withId(R.id.faderUpButton)).perform(click());
        verify(mPresenter).setFaderBalance(2,-1);
        onView(withId(R.id.faderDownButton)).perform(click());
        verify(mPresenter).setFaderBalance(0,-1);
        onView(withId(R.id.balanceLeftButton)).perform(click());
        verify(mPresenter).setFaderBalance(1,-2);
        onView(withId(R.id.balanceRightButton)).perform(click());
        verify(mPresenter).setFaderBalance(1,0);

    }

    @Test
    public void DisplayTwoWayNetWork() throws Exception {
        spec.audioSettingSpec.audioOutputMode = AudioOutputMode.TWO_WAY_NETWORK;
        mFragmentRule.launchActivity(null);
        final FaderBalanceSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        Thread.sleep(1000);
        onView(withId(R.id.faderText)).check(matches(not(isDisplayed())));
    }

}