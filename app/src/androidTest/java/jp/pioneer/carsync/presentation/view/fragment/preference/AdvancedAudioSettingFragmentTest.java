package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;
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
import jp.pioneer.carsync.domain.model.CrossoverSetting;
import jp.pioneer.carsync.domain.model.HpfLpfSetting;
import jp.pioneer.carsync.domain.model.ListeningPositionSetting;
import jp.pioneer.carsync.domain.model.MixedSpeakerType;
import jp.pioneer.carsync.domain.model.SpeakerLevelSetting;
import jp.pioneer.carsync.domain.model.SpeakerType;
import jp.pioneer.carsync.domain.model.StandardCutoffSetting;
import jp.pioneer.carsync.domain.model.StandardSlopeSetting;
import jp.pioneer.carsync.domain.model.StatusHolder;
import jp.pioneer.carsync.domain.model.SubwooferPhaseSetting;
import jp.pioneer.carsync.domain.model.SubwooferSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSetting;
import jp.pioneer.carsync.domain.model.TimeAlignmentSettingMode;
import jp.pioneer.carsync.domain.model.TimeAlignmentStepUnit;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.presenter.AdvancedAudioSettingPresenter;
import jp.pioneer.carsync.presentation.view.adapter.GridSpeakerSettingMenuAdapter;
import jp.pioneer.carsync.presentation.view.adapter.ListeningPositionAdapter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/09/21.
 */
public class AdvancedAudioSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<AdvancedAudioSettingFragment> mFragmentRule = new FragmentTestRule<AdvancedAudioSettingFragment>() {
        @Override
        protected AdvancedAudioSettingFragment createDialogFragment() {
            return AdvancedAudioSettingFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock AdvancedAudioSettingPresenter mPresenter;
    private StatusHolder mStatusHolder;
    private static final boolean CORE_AUDIO_ADV_SPK_MENU_NOTIFY = false;
    private static final String LEVEL_FORMAT = "%+ddB";
    private static final String DISTANCE_FORMAT = "%.1f%s";
    private AudioOutputMode mMode;
    private ListeningPositionAdapter mListeningPositionAdapter;
    private GridSpeakerSettingMenuAdapter mFlAdapter;
    private GridSpeakerSettingMenuAdapter mFrAdapter;
    private GridSpeakerSettingMenuAdapter mRlAdapter;
    private GridSpeakerSettingMenuAdapter mRrAdapter;
    private GridSpeakerSettingMenuAdapter mSwAdapter;

    private AudioSetting audioSetting = new AudioSetting();
    private CarDeviceSpec spec = new CarDeviceSpec();
    private boolean isAudioSettingEnabled = true;
    private SpeakerLevelSetting speakerLevelSetting = new SpeakerLevelSetting();

    private ViewAction clickAction = new ViewAction() {
        @Override
        public Matcher<View> getConstraints() {
            return isEnabled(); // no constraints, they are checked above
        }

        @Override
        public String getDescription() {
            return "click button";
        }

        @Override
        public void perform(UiController uiController, View view) {
            view.performClick();
        }
    };
    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        AdvancedAudioSettingFragmentTest.TestPresenterComponent presenterComponent(AdvancedAudioSettingFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = AdvancedAudioSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public AdvancedAudioSettingPresenter provideAdvancedAudioSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        AdvancedAudioSettingFragmentTest.TestAppComponent appComponent = DaggerAdvancedAudioSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        AdvancedAudioSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new AdvancedAudioSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, AdvancedAudioSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(AdvancedAudioSettingFragmentTest.TestPresenterComponent.class), any(AdvancedAudioSettingFragment.class))).thenReturn(fragmentComponent);

        when(mPresenter.getUiColor()).thenReturn(UiColor.AQUA.getResource());

        StatusHolder mockHolder = mock(StatusHolder.class);
        CarDeviceStatus status = new CarDeviceStatus();
        spec.audioSettingSpec.audioOutputMode = AudioOutputMode.STANDARD;

        TimeAlignmentSetting timeAlignmentSetting = new TimeAlignmentSetting();
        CrossoverSetting crossoverSetting = new CrossoverSetting();
        timeAlignmentSetting.mode = TimeAlignmentSettingMode.OFF;
        timeAlignmentSetting.stepUnit = TimeAlignmentStepUnit._1INCH;
        timeAlignmentSetting.minimumStep=0;
        timeAlignmentSetting.maximumStep=10;
        timeAlignmentSetting.frontLeftHighLeftStep = 1;
        timeAlignmentSetting.frontRightHighRightStep = 1;
        timeAlignmentSetting.rearLeftMidLeftStep = 1;
        timeAlignmentSetting.rearRightMidRightStep = 1;
        timeAlignmentSetting.subwooferStep = 1;
        crossoverSetting.front.cutoffSetting = StandardCutoffSetting._50HZ;
        crossoverSetting.front.hpfLpfSetting = HpfLpfSetting.ON;
        crossoverSetting.front.slopeSetting = StandardSlopeSetting._6DB;
        crossoverSetting.rear.cutoffSetting = StandardCutoffSetting._50HZ;
        crossoverSetting.rear.hpfLpfSetting = HpfLpfSetting.ON;
        crossoverSetting.rear.slopeSetting = StandardSlopeSetting._6DB;
        crossoverSetting.subwooferStandardMode.cutoffSetting = StandardCutoffSetting._50HZ;
        crossoverSetting.subwooferStandardMode.hpfLpfSetting = HpfLpfSetting.ON;
        crossoverSetting.subwooferStandardMode.slopeSetting = StandardSlopeSetting._6DB;
        crossoverSetting.high.cutoffSetting = StandardCutoffSetting._50HZ;
        crossoverSetting.high.hpfLpfSetting = HpfLpfSetting.ON;
        crossoverSetting.high.slopeSetting = StandardSlopeSetting._6DB;
        crossoverSetting.midHPF.cutoffSetting = StandardCutoffSetting._50HZ;
        crossoverSetting.midHPF.hpfLpfSetting = HpfLpfSetting.ON;
        crossoverSetting.midHPF.slopeSetting = StandardSlopeSetting._6DB;
        crossoverSetting.midLPF.cutoffSetting = StandardCutoffSetting._50HZ;
        crossoverSetting.midLPF.hpfLpfSetting = HpfLpfSetting.ON;
        crossoverSetting.midLPF.slopeSetting = StandardSlopeSetting._6DB;
        crossoverSetting.subwoofer2WayNetworkMode.cutoffSetting = StandardCutoffSetting._50HZ;
        crossoverSetting.subwoofer2WayNetworkMode.hpfLpfSetting = HpfLpfSetting.ON;
        crossoverSetting.subwoofer2WayNetworkMode.slopeSetting = StandardSlopeSetting._6DB;
        speakerLevelSetting.frontLeftHighLeftLevel=2;
        speakerLevelSetting.frontRightHighRightLevel=4;
        speakerLevelSetting.rearLeftMidLeftLevel=6;
        speakerLevelSetting.rearRightMidRightLevel=8;
        speakerLevelSetting.subwooferLevel=10;
        speakerLevelSetting.minimumLevel = 0;
        speakerLevelSetting.maximumLevel = 10;
        audioSetting.subwooferSetting = SubwooferSetting.ON;
        audioSetting.subwooferPhaseSetting = SubwooferPhaseSetting.NORMAL;
        audioSetting.speakerLevelSetting =  speakerLevelSetting;
        audioSetting.timeAlignmentSetting = timeAlignmentSetting;
        audioSetting.listeningPositionSetting = ListeningPositionSetting.OFF;
        audioSetting.crossoverSetting = crossoverSetting;
        AudioSettingStatus audioSettingStatus = new AudioSettingStatus();
        audioSettingStatus.subwooferSettingEnabled = true;
        audioSettingStatus.subwooferPhaseSettingEnabled = true;
        audioSettingStatus.speakerLevelSettingEnabled = true;
        audioSettingStatus.crossoverSettingEnabled = true;
        audioSettingStatus.timeAlignmentSettingEnabled = true;
        audioSettingStatus.listeningPositionSettingEnabled = true;
        when(mockHolder.getCarDeviceSpec()).thenReturn(spec);
        when(mockHolder.getCarDeviceStatus()).thenReturn(status);
        when(mockHolder.getAudioSetting()).thenReturn(audioSetting);
        when(mockHolder.getAudioSettingStatus()).thenReturn(audioSettingStatus);
        when(mockHolder.isAudioSettingEnabled()).thenReturn(isAudioSettingEnabled);
        when(mockHolder.isTimeAlignmentSettingEnabled()).thenReturn(true);
        when(mPresenter.getStatusHolder()).thenReturn(mockHolder);
    }

    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            AdvancedAudioSettingFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }

    @Test
    public void testDisplayListeningPosition() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        onView(withId(R.id.listeningPositionText)).check(matches(withText(R.string.listening_position_off)));
        Thread.sleep(300);
        instr.runOnMainSync(() -> {
            audioSetting.listeningPositionSetting = ListeningPositionSetting.FRONT_LEFT;
            fragment.applyStatus();
        });
        onView(withId(R.id.listeningPositionText)).check(matches(withText(R.string.listening_position_front_left)));
        Thread.sleep(300);
        instr.runOnMainSync(() -> {
            audioSetting.listeningPositionSetting = ListeningPositionSetting.FRONT_RIGHT;
            fragment.applyStatus();
        });
        onView(withId(R.id.listeningPositionText)).check(matches(withText(R.string.listening_position_front_right)));
        Thread.sleep(300);
        instr.runOnMainSync(() -> {
            audioSetting.listeningPositionSetting = ListeningPositionSetting.FRONT;
            fragment.applyStatus();
        });
        onView(withId(R.id.listeningPositionText)).check(matches(withText(R.string.listening_position_front)));
        Thread.sleep(300);
        instr.runOnMainSync(() -> {
            audioSetting.listeningPositionSetting = ListeningPositionSetting.ALL;
            fragment.applyStatus();
        });
        onView(withId(R.id.listeningPositionText)).check(matches(withText(R.string.listening_position_all)));
        Thread.sleep(300);
    }

    @Test
    public void testDisplaySubwoofer() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        instr.runOnMainSync(() -> {
            audioSetting.subwooferSetting = SubwooferSetting.ON;
            fragment.applyStatus();
        });
        Thread.sleep(300);
    }
    @Test
    public void testDisplaySubwooferPhase() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        instr.runOnMainSync(() -> {
            audioSetting.subwooferPhaseSetting = SubwooferPhaseSetting.REVERSE;
            fragment.applyStatus();
        });
        Thread.sleep(300);
    }
    @Test
    public void testSpeakerLevelSetting() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        onView(withId(R.id.flSpeakerInfo)).perform(click());
        Thread.sleep(300);
//        onData(anything()).inAdapterView(allOf(withId(R.id.list),isDisplayed()))
//                .atPosition(1).onChildView(allOf(withId(R.id.increaseButton),isDisplayed())).perform(click());
//        onView(allOf(withId(R.id.increaseButton),hasSibling(withText(R.string.a159_speaker_level)),isDisplayed())).perform(click());
        onView(allOf(withId(R.id.increaseButton),hasSibling(withText(R.string.a159_speaker_level)),isDisplayed())).perform(clickAction);

        verify(mPresenter).setSpeakerLevel(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT,true);
    }

    @Test
    public void testHiPassFilterSetting() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        onView(withId(R.id.flSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.increaseButton),hasSibling(withText(R.string.a147_high_pass_filter)),isDisplayed())).perform(clickAction);

        verify(mPresenter).onToggleCrossoverHpfLpf(SpeakerType.FRONT);
    }

    @Test
    public void testHiPassFilterCutOffSetting() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        onView(withId(R.id.flSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.increaseButton),hasSibling(withText(R.string.a161_cutoff)),isDisplayed())).perform(clickAction);

        verify(mPresenter).setCrossoverCutOff(SpeakerType.FRONT,true);
    }

    @Test
    public void testHiPassFilterSlopeSetting() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        onView(withId(R.id.flSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.increaseButton),hasSibling(withText(R.string.a150_slope)),isDisplayed())).perform(clickAction);

        verify(mPresenter).setCrossoverSlope(SpeakerType.FRONT,true);
    }


    @Test
    public void testTimeAlignmentSetting() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        onView(withId(R.id.flSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.increaseButton),hasSibling(withText(R.string.a145_time_alignment)),isDisplayed())).perform(clickAction);

        verify(mPresenter).setTimeAlignment(MixedSpeakerType.FRONT_LEFT_HIGH_LEFT,true);
    }

    @Test
    public void testSubwooferSetting() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        onView(withId(R.id.swSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.increaseButton),hasSibling(withText(R.string.a091_subwoofer)),isDisplayed())).perform(clickAction);

        verify(mPresenter).onToggleSubWoofer();
    }

    @Test
    public void testSubwooferPhaseSetting() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        onView(withId(R.id.swSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.increaseButton),hasSibling(withText(R.string.a092_subwoofer_phase)),isDisplayed())).perform(clickAction);

        verify(mPresenter).onToggleSubWooferPhase();
    }

    @Test
    public void testLowPassFilterSetting() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Thread.sleep(300);
        onView(withId(R.id.swSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.increaseButton),hasSibling(withText(R.string.a146_low_pass_filter)),isDisplayed())).perform(clickAction);

        verify(mPresenter).onToggleCrossoverHpfLpf(SpeakerType.SUBWOOFER_STANDARD_MODE);
    }

    @Test
    public void testOnTaButtonClicked() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.taButton)).perform(click());
        verify(mPresenter).onToggleTimeAlignmentMode();
    }

    @Test
    public void testOnSpeakerInfoClicked() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.flSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.downButton),isDisplayed())).perform(click());
        onView(withId(R.id.frSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.downButton),isDisplayed())).perform(click());
        onView(withId(R.id.rlSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.downButton),isDisplayed())).perform(click());
        onView(withId(R.id.rrSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.downButton),isDisplayed())).perform(click());
        onView(withId(R.id.swSpeakerInfo)).perform(click());
        Thread.sleep(300);
        onView(allOf(withId(R.id.downButton),isDisplayed())).perform(click());
    }

    @Test
    public void testOnSeatClicked() throws Exception {
        mFragmentRule.launchActivity(null);
        if(spec.audioSettingSpec.audioOutputMode==AudioOutputMode.STANDARD) {
            onView(allOf(withId(R.id.stdSeatIconContainer), isDisplayed())).perform(click());
        }else {
            onView(allOf(withId(R.id.nwSeatIconContainer), isDisplayed())).perform(click());
        }
        Thread.sleep(100);
        onData(anything()).inAdapterView(allOf(withId(R.id.list),isDisplayed()))
                .atPosition(0).perform(click());
        onView(withId(R.id.listeningPositionSettingMenu)).check(matches(not(isDisplayed())));
        verify(mPresenter).setListeningPosition(ListeningPositionSetting.OFF);

    }

    @Test
    public void testOnGraphClicked() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.flSpeakerInfo)).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.graphViewContainer)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.speakerSettingMenu)).check(matches(not(isDisplayed())));
        onView(anyOf(allOf(withId(R.id.stdSeatIconContainer),isDisplayed()),allOf(withId(R.id.nwSeatIconContainer),isDisplayed()))).perform(click());
        Thread.sleep(100);
        onView(withId(R.id.graphViewContainer)).perform(click());
        Thread.sleep(200);
        onView(withId(R.id.listeningPositionSettingMenu)).check(matches(not(isDisplayed())));
    }

    @Test
    public void testCheckMenuIsAvailable() throws Exception {
        mFragmentRule.launchActivity(null);
        final AdvancedAudioSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        onView(withId(R.id.flSpeakerInfo)).perform(click());
        instr.runOnMainSync(() -> {
            isAudioSettingEnabled=false;
            fragment.checkMenuIsAvailable();
        });
        onView(withId(R.id.speakerSettingMenu)).check(matches(not(isDisplayed())));
    }

}