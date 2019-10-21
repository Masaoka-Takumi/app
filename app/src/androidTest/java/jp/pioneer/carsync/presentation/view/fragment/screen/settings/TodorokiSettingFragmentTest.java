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
import jp.pioneer.carsync.domain.model.SuperTodorokiSetting;
import jp.pioneer.carsync.presentation.presenter.TodorokiSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
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
 * Created by NSW00_906320 on 2017/07/24.
 */
public class TodorokiSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<TodorokiSettingFragment> mFragmentRule = new FragmentTestRule<TodorokiSettingFragment>() {
        @Override
        protected TodorokiSettingFragment createDialogFragment() {
            return TodorokiSettingFragment.newInstance(Bundle.EMPTY);
        }

    };
    @Mock ComponentFactory mComponentFactory;
    @Mock TodorokiSettingPresenter mPresenter;


    private ArrayList<SuperTodorokiSetting> mTypeArray = new ArrayList<>();
    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        TodorokiSettingFragmentTest.TestPresenterComponent presenterComponent(TodorokiSettingFragmentTest.TestPresenterModule module);
    }
    @PresenterLifeCycle
    @Subcomponent(modules = TodorokiSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public TodorokiSettingPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TodorokiSettingFragmentTest.TestAppComponent appComponent = DaggerTodorokiSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TodorokiSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TodorokiSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, TodorokiSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TodorokiSettingFragmentTest.TestPresenterComponent.class), any(TodorokiSettingFragment.class))).thenReturn(fragmentComponent);

        mTypeArray.clear();
        mTypeArray.add(SuperTodorokiSetting.OFF);
        mTypeArray.add(SuperTodorokiSetting.LOW);
        mTypeArray.add(SuperTodorokiSetting.HIGH);
        mTypeArray.add(SuperTodorokiSetting.SUPER_HIGH);
    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final TodorokiSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            TodorokiSettingFragment m_fragment;
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
        final TodorokiSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.todoroki_off))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.todoroki_low))));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(2).onChildView(withId(R.id.textView)).check(matches(withText(getTargetContext().getString(R.string.todoroki_high))));
    }

    /**
     * 選択中設定のテスト
     */
    @Test
    public void testSetSelectedItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final TodorokiSettingFragment fragment = mFragmentRule.getFragment();
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
        final TodorokiSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mTypeArray);
            fragment.setColor(AQUA.getResource());
            fragment.setSelectedItem(0);
            fragment.setPresetView(R.drawable.p0070_noimage);
        });
        onView(withId(R.id.preset_view)).check(matches(withDrawable(R.drawable.p0070_noimage)));

    }
}