package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.v7.preference.Preference;

import org.junit.After;
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
import jp.pioneer.carsync.presentation.presenter.InformationPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/06/01.
 */
public class InformationFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<InformationFragment> mFragmentRule = new FragmentTestRule<InformationFragment>() {

        private InformationFragment mFragment;

        @Override
        protected InformationFragment createDialogFragment() {
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new InformationFragment());

            return mFragment;
        }
    };

    @Mock InformationPresenter mPresenter;
    @Mock ComponentFactory mComponentFactory;

    private Preference mManual;
    private Preference mDeviceInfo;
    private Preference mLicense;
    private Preference mEula;
    private Preference mPrivacyPolicy;
    private Preference mAppVersion;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        InformationFragmentTest.TestPresenterComponent presenterComponent(InformationFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = InformationFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public InformationPresenter provideInformationPresenter() {
            return mPresenter;
        }

    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        InformationFragmentTest.TestAppComponent appComponent = DaggerInformationFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        InformationFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new InformationFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, InformationFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(InformationFragmentTest.TestPresenterComponent.class), any(InformationFragment.class))).thenReturn(fragmentComponent);

    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final InformationFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            InformationFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }

    /**
     * 表示確認のテスト
     */
    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);

        final InformationFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceInformation("未接続");
            fragment.setAppVersion("1.0");
        });
    }

    /**
     * マニュアルタップ時のテスト
     */
    @Test
    public void testManual() throws Exception {
        mFragmentRule.launchActivity(null);

        final InformationFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceInformation("未接続");
            fragment.setAppVersion("1.0");
        });
//        doAnswer(invocationOnMock -> {
//            Uri uri = Uri.parse("http://google.com/");
//            fragment.startBrowser(new Intent(Intent.ACTION_VIEW, uri));
//            return null;
//        }).when(mPresenter).onManualAction();
        onView(withText(R.string.setting_information_manual)).perform(click());
        verify(mPresenter).onManualAction();
    }

    /**
     * Licenseタップ時のテスト
     */
    @Test
    public void testLicenseAction() throws Exception {
        mFragmentRule.launchActivity(null);

        final InformationFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceInformation("Test");
            fragment.setAppVersion("1.0");
        });
        onView(withText(R.string.setting_information_licence)).perform(click());
        verify(mPresenter).onLicenseAction();
    }

    /**
     * Eulaタップ時のテスト
     */
    @Test
    public void testEulaAction() throws Exception {
        mFragmentRule.launchActivity(null);

        final InformationFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceInformation("");
            fragment.setAppVersion("1.0");
        });
        onView(withText(R.string.setting_information_eula)).perform(click());
        verify(mPresenter).onEulaAction();
    }

    /**
     * PrivacyPolicyタップ時のテスト
     */
    @Test
    public void testPrivacyPolicyAction() throws Exception {
        mFragmentRule.launchActivity(null);

        final InformationFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceInformation("test");
            fragment.setAppVersion("1.0");
        });
        onView(withText(R.string.setting_information_privacy_policy)).perform(click());
        verify(mPresenter).onPrivacyPolicyAction();
    }
}