package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.util.SparseBooleanArray;

import org.junit.After;
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
import jp.pioneer.carsync.application.content.AppSharedPreference;
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
import jp.pioneer.carsync.presentation.presenter.MessagePresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/06/09.
 */
public class MessageFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<MessageFragment> mFragmentRule = new FragmentTestRule<MessageFragment>() {
        @Override
        protected MessageFragment createDialogFragment() {
            return MessageFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock MessagePresenter mPresenter;

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
        MessageFragmentTest.TestPresenterComponent presenterComponent(MessageFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = MessageFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public MessagePresenter provideMessagePresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        MessageFragmentTest.TestAppComponent appComponent = DaggerMessageFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        MessageFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new MessageFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, MessageFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(MessageFragmentTest.TestPresenterComponent.class), any(MessageFragment.class))).thenReturn(fragmentComponent);

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

    @After
    public void tearDown() throws Exception {
    }

    @Test(expected = NullPointerException.class)
    public void test1() throws Exception {
        mFragmentRule.launchActivity(null);
        final MessageFragment fragment = mFragmentRule.getFragment();
        when(mPresenter.getEnable()).thenReturn(false);
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
//            fragment.setReadNotificationEnabled(false);
//            fragment.setInstalledMessagingApps(mInstallApplications);
//            fragment.setCheckedItemPositions(mCheckedItemPosition);
        });
        onView(withText("com.sample.app1")).check(matches(not(isEnabled())));
        onView(withText("com.sample.app2")).check(matches(not(isEnabled())));
        onView(withText("com.sample.app3")).check(matches(not(isEnabled())));

    }

//    @Test
//    public void test2() throws Exception {
//        mFragmentRule.launchActivity(null);
//        final MessageFragment fragment = mFragmentRule.getFragment();
//        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
//        instr.runOnMainSync(() -> {
//            fragment.setReadNotificationEnabled(true);
//            fragment.setInstalledMessagingApps(mInstallApplications);
//            fragment.setCheckedItemPositions(mCheckedItemPosition);
//        });
//        Instrumentation instr2 = InstrumentationRegistry.getInstrumentation();
//        instr2.runOnMainSync(() -> {
//            fragment.setReadNotificationEnabled(true);
//        });
//        onView(withText("com.sample.app1")).perform(click());
//        verify(mPresenter).onMessagingAppDecided();
//        onView(withText("com.sample.app1")).check(matches(isDisplayed()));
//        assertThat(fragment.isReadNotificationEnabled(), is(true));
//        assertThat(fragment.getCheckedItemPositions().size(), is(2));
//    }
//
//    @Test
//    public void test3() throws Exception {
//        mFragmentRule.launchActivity(null);
//        final MessageFragment fragment = mFragmentRule.getFragment();
//        when(mPresenter.getEnable()).thenReturn(true);
//        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
//        instr.runOnMainSync(() -> {
//            fragment.setReadNotificationEnabled(false);
//            fragment.setInstalledMessagingApps(mInstallApplications);
//            fragment.setCheckedItemPositions(mCheckedItemPosition);
//        });
//        Instrumentation instr2 = InstrumentationRegistry.getInstrumentation();
//        instr2.runOnMainSync(() -> {
//            fragment.setReadNotificationEnabled(true);
//        });
//        onView(withId(R.id.switch_read_notification_enabled)).perform(click());
//        verify(mPresenter).onSwitchReadNotificationEnabledChange(true);
//        onView(withText("com.sample.app1")).check(matches(isEnabled()));
//        onView(withText("com.sample.app2")).check(matches(isEnabled()));
//        onView(withText("com.sample.app3")).check(matches(isEnabled()));
//
//    }
}