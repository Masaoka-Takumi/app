package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

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
import jp.pioneer.carsync.presentation.model.ImpactNotificationMethod;
import jp.pioneer.carsync.presentation.presenter.ImpactDetectionSettingsPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 衝突検知機能設定の画面のテスト
 */
public class ImpactDetectionSettingsFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<ImpactDetectionSettingsFragment> mFragmentRule = new FragmentTestRule<ImpactDetectionSettingsFragment>() {

        private ImpactDetectionSettingsFragment mFragment;

        @Override
        protected ImpactDetectionSettingsFragment createDialogFragment() {
            // 非UIスレッドから呼ばれるが、PreferenceFragmentCompatがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new ImpactDetectionSettingsFragment());
            return mFragment;
        }
    };

    @Mock ImpactDetectionSettingsPresenter mPresenter;
    @Mock ComponentFactory mComponentFactory;
    @Mock AppSharedPreference mPreference;

    private SwitchPreferenceCompat mImpactDetection;
    private Preference mImpactMethod;
    private Preference mImpactContact;

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
        public ImpactDetectionSettingsPresenter provideImpactDetectionSettingsPresenter() {
            return mPresenter;
        }

    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerImpactDetectionSettingsFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ImpactDetectionSettingsFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(ImpactDetectionSettingsFragment.class))).thenReturn(fragmentComponent);
    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final ImpactDetectionSettingsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            ImpactDetectionSettingsFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }

    /**
     * 衝突検知機能無効端末のテスト
     */
    @Test
    public void testInvalidImpactDetection() throws Exception {
        mFragmentRule.launchActivity(null);

        final ImpactDetectionSettingsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.invalidImpactDetection();

        });
        onView(withText("衝突検知")).check(matches(not(isEnabled())));
        //summaryのcheck方法不明
        //onView(withText("衝突検知")).onChildView(withId(android.R.id.summary)).check(matches(withText(getString(R.string.impact_detection_incapable))));
        onView(withText(R.string.impact_detection_incapable)).check(matches(isDisplayed()));
        onView(withText("通知方法")).check(matches(not(isEnabled())));
        onView(withText("通知先")).check(matches(not(isEnabled())));

    }

    /**
     * 衝突検知機能有効/無効押下テスト
     */
    @Test
    public void testClickImpactDetectionEnabled() throws Exception {
        mFragmentRule.launchActivity(null);

        final ImpactDetectionSettingsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setImpactDetectionEnabled(false);
        });
        //onData(PreferenceMatchers.withKey("impact_detection_enabled")).perform(click());
//        onData(withPreferenceKey(PreferenceMatchers.withKey("impact_detection_enabled"))).perform(click());
        //onView(withText("衝突検知")).perform(click());
        onView(allOf(withId(android.support.v7.preference.R.id.switchWidget))).perform(click());
        verify(mPresenter).onImpactDetectionChange(true);
        onView(allOf(withId(android.support.v7.preference.R.id.switchWidget))).perform(click());
        verify(mPresenter).onImpactDetectionChange(false);
    }

    /**
     * 衝突検知機能有効/無効-無効の場合の表示テスト
     */
    @Test
    public void testSetImpactDetectionNotEnabled() throws Exception {
        mFragmentRule.launchActivity(null);

        final ImpactDetectionSettingsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setImpactDetectionEnabled(false);
        });
        //onView(withText("衝突検知")).check(matches(not(isChecked())));
        onView(allOf(withId(android.support.v7.preference.R.id.switchWidget))).check(matches(not(isChecked())));
        //summaryのcheck方法不明
        //onView(withText("衝突検知")).onChildView(withId(android.R.id.summary)).check(matches(withText("")));
        onView(withText("通知方法")).check(matches(not(isEnabled())));
        onView(withText("通知先")).check(matches(not(isEnabled())));
    }

    /**
     * 衝突検知機能有効/無効-有効の場合の表示テスト
     */
    @Test
    public void testSetImpactDetectionEnabled() throws Exception {
        mFragmentRule.launchActivity(null);

        final ImpactDetectionSettingsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setImpactDetectionEnabled(true);
        });
        onView(allOf(withId(android.support.v7.preference.R.id.switchWidget))).check(matches(isChecked()));
        //summaryのcheck方法不明
        //onView(withText("衝突検知")).onChildView(withId(android.R.id.summary)).check(matches(withText("")));
        onView(withText("通知方法")).check(matches(isEnabled()));
        onView(withText("通知先")).check(matches(isEnabled()));
    }

    /**
     * 衝突検知機能通知方法設定-押下テスト
     */
    @Test
    public void testClickImpactNotificationMethodMethod() throws Exception {
        mFragmentRule.launchActivity(null);

        final ImpactDetectionSettingsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setImpactNotificationMethod(ImpactNotificationMethod.PHONE);
        });
        onView(withText("通知方法")).perform(click());
        verify(mPresenter).onImpactNotificationMethodChange();
    }

    /**
     * 衝突検知機能通知方法設定-表示テスト
     */
    @Test
    public void testSetImpactNotificationMethod() throws Exception {
        mFragmentRule.launchActivity(null);

        final ImpactDetectionSettingsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setImpactNotificationMethod(ImpactNotificationMethod.PHONE);
        });
        //summaryのcheck方法不明
        onView(withText(ImpactNotificationMethod.PHONE.name())).check(matches(isDisplayed()));
        //onView(withText("通知方法")).check(matches(withText(ImpactNotificationMethod.PHONE.name())));
        //onData(withPreferenceKey("impact_notification_method")).check(matches(withText("通知方法")));
        //onChildView(withId(android.R.id.summary)).check(matches(withText(ImpactNotificationMethod.PHONE.name())));
        instr.runOnMainSync(() -> {
            fragment.setImpactNotificationMethod(ImpactNotificationMethod.SMS);
        });
        onView(withText(ImpactNotificationMethod.SMS.name())).check(matches(isDisplayed()));
    }

    /**
     * 緊急連絡先設定押下テスト
     */
    @Test
    public void testImpactNotificationContact() throws Exception {
        mFragmentRule.launchActivity(null);

        final ImpactDetectionSettingsFragment fragment = mFragmentRule.getFragment();
        onView(withText("通知先")).perform(click());
        verify(mPresenter).onImpactNotificationContactAction();
    }

    /**
     * 緊急連絡先設定表示テスト
     */
    @Test
    public void testSetImpactNotificationContact() throws Exception {
        mFragmentRule.launchActivity(null);

        final ImpactDetectionSettingsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setImpactNotificationContact("satou");
        });
        onView(withText("satou")).check(matches(isDisplayed()));
    }

}