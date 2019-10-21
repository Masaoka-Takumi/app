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
import jp.pioneer.carsync.domain.model.ListeningPosition;
import jp.pioneer.carsync.domain.model.SmallCarTaSettingType;
import jp.pioneer.carsync.presentation.presenter.SmallCarTaSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.presentation.model.UiColor.AQUA;
import static jp.pioneer.carsync.presentation.view.EspressoTestsMatchers.withDrawable;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/07/26.
 */
public class SmallCarTaSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<SmallCarTaSettingFragment> mFragmentRule = new FragmentTestRule<SmallCarTaSettingFragment>() {
        @Override
        protected SmallCarTaSettingFragment createDialogFragment() {
            return SmallCarTaSettingFragment.newInstance(Bundle.EMPTY);
        }

    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SmallCarTaSettingPresenter mPresenter;

    private ArrayList<SmallCarTaSettingType> mTypeArray = new ArrayList<SmallCarTaSettingType>(){{
        add(SmallCarTaSettingType.OFF);
        add(SmallCarTaSettingType.A);
        add(SmallCarTaSettingType.B);
        add(SmallCarTaSettingType.C);
    }};

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        SmallCarTaSettingFragmentTest.TestPresenterComponent presenterComponent(SmallCarTaSettingFragmentTest.TestPresenterModule module);
    }
    @PresenterLifeCycle
    @Subcomponent(modules = SmallCarTaSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public SmallCarTaSettingPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        SmallCarTaSettingFragmentTest.TestAppComponent appComponent = DaggerSmallCarTaSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        SmallCarTaSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new SmallCarTaSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SmallCarTaSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(SmallCarTaSettingFragmentTest.TestPresenterComponent.class), any(SmallCarTaSettingFragment.class))).thenReturn(fragmentComponent);

    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final SmallCarTaSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            SmallCarTaSettingFragment m_fragment;
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
        final SmallCarTaSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
            fragment.setSeatTypeSettingEnabled(true);
            fragment.setSelectedItem(1);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.setting_ta_off))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.setting_ta_a))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(2).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.setting_ta_b))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(3).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.setting_ta_c))));

    }

    /**
     * 選択中設定のテスト(OFF)
     */
    @Test
    public void testSetSelectedItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final SmallCarTaSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
            fragment.setSeatTypeSettingEnabled(false);
            fragment.setSelectedItem(0);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.check)).check(matches(isDisplayed()));
        onView(withId(R.id.front_l)).check(matches(not(isEnabled())));
        onView(withId(R.id.front_r)).check(matches(not(isEnabled())));
    }

    /**
     * Presetイメージ設定のテスト
     */
    @Test
    public void testSetPresetView() throws Exception {
        mFragmentRule.launchActivity(null);
        final ListeningPosition smallCarTaSeatPosition = ListeningPosition.LEFT;
        final SmallCarTaSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
            fragment.setSelectedItem(1);
            fragment.setSeatTypeSettingEnabled(true);
            fragment.setPresetView(R.drawable.p0681_pta_suv_l);
            fragment.setSeatType(smallCarTaSeatPosition);
        });
        onView(withId(R.id.preset_view)).check(matches(withDrawable(R.drawable.p0681_pta_suv_l)));

    }
}