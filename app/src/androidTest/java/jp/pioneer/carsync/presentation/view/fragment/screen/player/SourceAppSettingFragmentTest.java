package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.app.Instrumentation;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.util.SparseBooleanArray;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
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
import jp.pioneer.carsync.presentation.presenter.SourceAppSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/10/24.
 */
public class SourceAppSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<SourceAppSettingFragment> mFragmentRule = new FragmentTestRule<SourceAppSettingFragment>() {
        @Override
        protected SourceAppSettingFragment createDialogFragment() {
            return SourceAppSettingFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SourceAppSettingPresenter mPresenter;

    @Mock Context mContext;
    private List<ApplicationInfo> mInstallApplications;
    private SparseBooleanArray mCheckedItemPosition;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        SourceAppSettingFragmentTest.TestPresenterComponent presenterComponent(SourceAppSettingFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = SourceAppSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public SourceAppSettingPresenter provideSourceAppSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        SourceAppSettingFragmentTest.TestAppComponent appComponent = DaggerSourceAppSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        SourceAppSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new SourceAppSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SourceAppSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(SourceAppSettingFragmentTest.TestPresenterComponent.class), any(SourceAppSettingFragment.class))).thenReturn(fragmentComponent);

        when(mContext.getResources()).thenReturn(getTargetContext().getResources());

        mInstallApplications = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            ApplicationInfo info = new ApplicationInfo();
            info.packageName = "com.sample.app" + String.valueOf(i + 1);
            mInstallApplications.add(info);
        }
        mCheckedItemPosition = new SparseBooleanArray();
        mCheckedItemPosition.put(0, false);
        mCheckedItemPosition.put(1, true);
        mCheckedItemPosition.put(2, false);
    }

    @Test
    public void test() throws Exception {
        mFragmentRule.launchActivity(null);
        final SourceAppSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setInstalledMusicApps(mInstallApplications);
            fragment.setCheckedItemPositions(mCheckedItemPosition);
        });
        onView(withText("com.sample.app1")).check(matches(isDisplayed()));
        onView(withText("com.sample.app2")).check(matches(isDisplayed()));
        onView(withText("com.sample.app3")).check(matches(isDisplayed()));
        onView(withText("com.sample.app1")).perform(click());
        verify(mPresenter).onMusicAppDecided();
        assertThat(fragment.getCheckedItemPositions().size(), is(2));
    }

    @Test
    public void setPass() throws Exception {
        mFragmentRule.launchActivity(null);
        final SourceAppSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setInstalledMusicApps(mInstallApplications);
            fragment.setCheckedItemPositions(mCheckedItemPosition);
            fragment.setPass(mContext.getString(R.string.source_app_settings));
        });
        onView(withText(mContext.getString(R.string.source_app_settings))).check(matches(isDisplayed()));
        onView(withId(R.id.directory_pass_text)).check(matches(withText(mContext.getString(R.string.source_app_settings))));
    }

    @Test
    public void onClickBackButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final SourceAppSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setInstalledMusicApps(mInstallApplications);
            fragment.setCheckedItemPositions(mCheckedItemPosition);
        });
        onView(withId(R.id.back_button)).perform(click());
        verify(mPresenter).onBackAction();
    }

}