package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.hamcrest.Matchers;
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
import jp.pioneer.carsync.domain.model.SoundFxSettingEqualizerType;
import jp.pioneer.carsync.presentation.presenter.EqSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static jp.pioneer.carsync.presentation.view.EspressoTestsMatchers.withDrawable;
import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/07/20.
 */
public class EqSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<EqSettingFragment> mFragmentRule = new FragmentTestRule<EqSettingFragment>() {
        @Override
        protected EqSettingFragment createDialogFragment() {
            return EqSettingFragment.newInstance(Bundle.EMPTY);
        }

    };
    @Mock ComponentFactory mComponentFactory;
    @Mock EqSettingPresenter mPresenter;

    private static final int BAND_DATA_COUNT = 31; //全Band数
    float[] BANDS = new float[]{
            0, 2, 4, 8, 12, 10, 8, 6, 3, 0, -1, -2, -5, -9, -10 ,-12, -10, -9,-5 ,-3, -1, 0, 3, 6, 8, 10, 12, 8, 4, 2, 0
    };
    private ArrayList<SoundFxSettingEqualizerType> mTypeArray = new ArrayList<>();
    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        EqSettingFragmentTest.TestPresenterComponent presenterComponent(EqSettingFragmentTest.TestPresenterModule module);
    }
    @PresenterLifeCycle
    @Subcomponent(modules = EqSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public EqSettingPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        EqSettingFragmentTest.TestAppComponent appComponent = DaggerEqSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        EqSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new EqSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, EqSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(EqSettingFragmentTest.TestPresenterComponent.class), any(EqSettingFragment.class))).thenReturn(fragmentComponent);

        mTypeArray.clear();
        mTypeArray.add(SoundFxSettingEqualizerType.FLAT);
        mTypeArray.add(SoundFxSettingEqualizerType.SUPER_BASS);
        mTypeArray.add(SoundFxSettingEqualizerType.POWERFUL);
        mTypeArray.add(SoundFxSettingEqualizerType.NATURAL);
        mTypeArray.add(SoundFxSettingEqualizerType.VOCAL);
        mTypeArray.add(SoundFxSettingEqualizerType.CLEAR);
        mTypeArray.add(SoundFxSettingEqualizerType.COMMON_CUSTOM);
        mTypeArray.add(SoundFxSettingEqualizerType.COMMON_CUSTOM_2ND);
    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            EqSettingFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), Matchers.is(args));
        });
    }

    /**
     * アダプター設定のテスト
     */
    @Test
    public void testSetAdapter() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.eq_type_flat))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.eq_type_super_bass))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(2).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.eq_type_powerful))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(3).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.eq_type_natural))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(4).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.eq_type_vocal))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(5).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.eq_type_clear))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(6).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.eq_type_custom_a))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(7).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.eq_type_custom_b))));
    }

    /**
     * 選択中EQ設定のテスト
     */
    @Test
    public void testSetSelectedItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
            fragment.setSelectedItem(0);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
               .atPosition(0).onChildView(withId(R.id.check)).check(matches(isDisplayed()));
    }

    /**
     * Presetイメージ設定のテスト
     */
    @Test
    public void testSetPresetView() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
            fragment.setSelectedItem(0);
            fragment.setPresetView(R.drawable.p0070_noimage);
        });
        onView(withId(R.id.preset_view)).check(matches(withDrawable(R.drawable.p0070_noimage)));

    }

    /**
     * CustomView設定のテスト
     */
    @Test
    public void testSetCustomView() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
            fragment.setSelectedItem(6);
            fragment.setCustomView(BANDS);
        });
        onView(withId(R.id.custom_view)).check(matches(isDisplayed()));
    }

    /**
     * QuickSet押下のテスト
     */
    @Test
    public void testOnClickQuickSet() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
            fragment.setSelectedItem(6);
            fragment.setCustomView(BANDS);
        });
        onView(withId(R.id.quick_set)).perform(click());
    }

    /**
     * ProSet押下のテスト
     */
    @Test
    public void testOnClickProSet() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
            fragment.setSelectedItem(6);
            fragment.setCustomView(BANDS);
        });
        onView(withId(R.id.pro_set)).perform(click());
    }

}