package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.GeneralClickAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Tap;
import android.view.View;
import android.widget.SeekBar;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
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
import jp.pioneer.carsync.presentation.presenter.ThemePresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * テーマ設定画面のテスト
 */
public class ThemeFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<ThemeFragment> mFragmentRule = new FragmentTestRule<ThemeFragment>() {
        private ThemeFragment mFragment;

        @Override
        protected ThemeFragment createDialogFragment() {
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = ThemeFragment.newInstance(Bundle.EMPTY));
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock ThemePresenter mPresenter;

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
        public ThemePresenter provideThemePresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerThemeFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ThemeFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(ThemeFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testDisplayWhenSync() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });
        Thread.sleep(200);
        onView(withText(R.string.setting_theme_theme_set)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_illumination_color_common)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumination_color_disp)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_illumination_color_key)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_ui_color)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_illumination_dimmer)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_illumination_brightness_common)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumination_brightness_disp)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_illumination_brightness_key)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_illumi_fx)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_illumi_fx_with_bgv)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_illumi_fx_with_audio_level)).check(matches(isEnabled()));
    }

    @Test
    public void testDisplayWhenNonSync() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        Thread.sleep(200);

        onView(withText(R.string.setting_theme_theme_set)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumination_color_common)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumination_color_disp)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumination_color_key)).check(doesNotExist());
        onView(withText(R.string.setting_theme_ui_color)).check(matches(isEnabled()));
        onView(withText(R.string.setting_theme_illumination_dimmer)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumination_brightness_common)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumination_brightness_disp)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumination_brightness_key)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumi_fx)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumi_fx_with_bgv)).check(doesNotExist());
        onView(withText(R.string.setting_theme_illumi_fx_with_audio_level)).check(doesNotExist());
    }

    @Test
    public void testOnClickThemeSet() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        onView(withText(R.string.setting_theme_theme_set)).perform(click());

        verify(mPresenter).onThemeSetAction();
    }

    @Test
    public void onClickIlluminationColor() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        onView(withText(R.string.setting_theme_illumination_color_common)).perform(click());

        verify(mPresenter).onIlluminationColorAction();
    }

    @Test
    public void onClickIlluminationDispColor() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        onView(withText(R.string.setting_theme_illumination_color_disp)).perform(click());

        verify(mPresenter).onIlluminationDispColorAction();
    }

    @Test
    public void onClickIlluminationKeyColor() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        onView(withText(R.string.setting_theme_illumination_color_key)).perform(click());

        verify(mPresenter).onIlluminationKeyColorAction();
    }

    @Test
    public void testOnClickUiColor() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        onView(withText(R.string.setting_theme_ui_color)).perform(click());

        verify(mPresenter).onUiColorAction();
    }

    @Test
    public void testOnClickDimmer() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        onView(withText(R.string.setting_theme_illumination_dimmer)).perform(click());

        verify(mPresenter).onIlluminationDimmerAction();
    }
    @Test
    public void testOnClickBrightness() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        onView(withIndex(withId(R.id.seekbar), 0)).perform(clickSeekBar(5));

        verify(mPresenter).onBrightnessAction(6);
    }
    @Test
    public void testOnClickDisplayBrightness() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        onView(withIndex(withId(R.id.seekbar), 0)).perform(clickSeekBar(5));

        verify(mPresenter).onDisplayBrightnessAction(6);
    }

    @Test
    public void testOnClickKeyBrightness() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });

        onView(withIndex(withId(R.id.seekbar), 1)).perform(clickSeekBar(5));

        verify(mPresenter).onKeyBrightnessAction(6);
    }

    @Test
    public void testOnClickIllumiFx() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });
        onView(withText(R.string.setting_theme_ui_color)).perform(swipeUp());
        Thread.sleep(300);
        onView(withText(R.string.setting_theme_illumi_fx)).perform(click());

        verify(mPresenter).onIllumiFxChange(true);
    }

    @Test
    public void testOnClickIllumiFxWithBgv() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });
        onView(withText(R.string.setting_theme_ui_color)).perform(swipeUp());
        Thread.sleep(300);
        onView(withText(R.string.setting_theme_illumi_fx_with_bgv)).perform(click());

        verify(mPresenter).onIllumiFxWithBgvChange(true);
    }

    @Test
    public void testOnClickIlummiFxWithAudioLevel() throws Exception {
        mFragmentRule.launchActivity(null);
        ThemeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setThemeSettingEnabled(true);
            fragment.setBrightnessSetting(false,true,1, 10, 5);
            fragment.setKeyBrightnessSetting(false,true,1, 10, 5);
            fragment.setDisplayBrightnessSetting(false,true,1, 10, 5);
            fragment.setIlluminationSetting(false,true);
            fragment.setDisplayIlluminationSetting(false,true);
            fragment.setKeyIlluminationSetting(false,true);
            fragment.setIlluminationEffectSetting(false,false,true);
            fragment.setBgvLinkedSetting(true,true ,true);
            fragment.setAudioLevelLinkedSetting(false,false,true);
        });
        onView(withText(R.string.setting_theme_ui_color)).perform(swipeUp());
        Thread.sleep(300);
        onView(withText(R.string.setting_theme_illumi_fx_with_audio_level)).perform(click());

        verify(mPresenter).onIllumiFxWithAudioLevelChange(true);
    }

    public Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    public ViewAction clickSeekBar(final int pos) {
        return new GeneralClickAction(Tap.SINGLE, view -> {
            SeekBar seekBar = (SeekBar) view;
            final int[] screenPos = new int[2];
            seekBar.getLocationOnScreen(screenPos);

            int trueWidth = seekBar.getWidth()
                    - seekBar.getPaddingLeft() - seekBar.getPaddingRight();

            float relativePos = (0.3f + pos) / (float) seekBar.getMax();
            if (relativePos > 1.0f)
                relativePos = 1.0f;

            final float screenX = trueWidth * relativePos + screenPos[0]
                    + seekBar.getPaddingLeft();
            final float screenY = seekBar.getHeight() / 2f + screenPos[1];
            float[] coordinates = {screenX, screenY};

            return coordinates;
        }, Press.FINGER);
    }
}