package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.app.Instrumentation;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import jp.pioneer.carsync.domain.model.IlluminationColor;
import jp.pioneer.carsync.presentation.presenter.IlluminationColorSettingPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * イルミネーションカラー設定画面のテスト
 */
public class IlluminationColorSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<IlluminationColorSettingFragment> mFragmentRule = new FragmentTestRule<IlluminationColorSettingFragment>() {
        @Override
        protected IlluminationColorSettingFragment createDialogFragment() {
            return IlluminationColorSettingFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock IlluminationColorSettingPresenter mPresenter;

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
        public IlluminationColorSettingPresenter provideIlluminationColorSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerIlluminationColorSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, IlluminationColorSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(IlluminationColorSettingFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testLifecycle() throws Exception {
        mFragmentRule.launchActivity(null);
        IlluminationColorSettingFragment fragment = mFragmentRule.getFragment();

        assertThat(fragment.getScreenId(), is(ScreenId.ILLUMINATION_COLOR_SETTING));
    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        List<Integer> list = new ArrayList<Integer>() {{
            add(Color.rgb(255, 0, 0));
            add(Color.rgb(0, 255, 0));
            add(Color.rgb(0, 0, 255));
        }};

        IlluminationColorSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(list);
            fragment.setPosition(1);
            fragment.setCustomColor(255, 255, 255);
        });

        onView(withId(R.id.color_list))
                .perform(RecyclerViewActions.scrollToPosition(1))
                .check(matches(atPositionOnView(1, withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE), R.id.illumi_select)));
    }

    @Test
    public void testOnClickPresetItem() throws Exception {
        mFragmentRule.launchActivity(null);
        List<Integer> list = new ArrayList<Integer>() {{
            add(Color.rgb(255, 0, 0));
            add(Color.rgb(0, 255, 0));
            add(Color.rgb(0, 0, 255));
        }};

        IlluminationColorSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(list);
            fragment.setPosition(1);
            fragment.setCustomColor(255, 255, 255);
        });

        onView(withId(R.id.color_list)).perform(RecyclerViewActions.actionOnItemAtPosition(2, click()));

        verify(mPresenter).onSelectColorItemAction(2);
    }

    @Test
    public void testOnClickCustomItem() throws Exception {
        mFragmentRule.launchActivity(null);
        List<Integer> list = new ArrayList<Integer>() {{
            add(Color.rgb(255, 0, 0));
            add(Color.rgb(0, 255, 0));
            add(Color.rgb(0, 0, 255));
        }};

        IlluminationColorSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(list);
            fragment.setPosition(1);
            fragment.setCustomColor(255, 255, 255);
        });

        onView(withId(R.id.custom_item)).perform(click());

        verify(mPresenter).onSelectCustomItemAction();
    }

    @Test
    public void testOnTouchColorPalette() throws Exception {
        mFragmentRule.launchActivity(null);
        List<Integer> list = new ArrayList<Integer>() {{
            add(Color.rgb(255, 0, 0));
            add(Color.rgb(0, 255, 0));
            add(Color.rgb(0, 0, 255));
        }};

        IlluminationColorSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(list);
            fragment.setPosition(-1);
            fragment.setCustomColor(255, 255, 255);
        });

        doAnswer(invocationOnMock -> {
            fragment.setCustomColor((int) invocationOnMock.getArguments()[0], (int) invocationOnMock.getArguments()[1], (int) invocationOnMock.getArguments()[2]);
            return null;
        }).when(mPresenter).onCustomColorAction(anyInt(), anyInt(), anyInt());

        onView(withId(R.id.color_palette)).perform(click());
        onView(withId(R.id.target_point)).check(matches(not(isDisplayed())));
    }

    private Matcher<View> atPositionOnView(final int position, final Matcher<View> itemMatcher, @NonNull final int targetViewId) {
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has view id " + itemMatcher + " at position " + position);
            }

            @Override
            public boolean matchesSafely(final RecyclerView recyclerView) {
                RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
                View targetView = viewHolder.itemView.findViewById(targetViewId);
                return itemMatcher.matches(targetView);
            }
        };
    }
}