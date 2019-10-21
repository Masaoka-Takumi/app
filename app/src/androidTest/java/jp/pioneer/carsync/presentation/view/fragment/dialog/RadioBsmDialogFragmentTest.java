package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;

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
import jp.pioneer.carsync.DialogFragmentTestRule;
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
import jp.pioneer.carsync.presentation.presenter.RadioBsmDialogPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.DialogFragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Radio BSM Dialogの画面のテスト
 */
public class RadioBsmDialogFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<RadioBsmDialogFragment> mFragmentRule = new DialogFragmentTestRule<RadioBsmDialogFragment>() {
        private RadioBsmDialogFragment mFragment;

        @Override
        protected RadioBsmDialogFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new RadioBsmDialogFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock RadioBsmDialogPresenter mPresenter;
    @Mock RadioBsmDialogFragment.Callback mCallback;
    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        RadioBsmDialogFragmentTest.TestPresenterComponent presenterComponent(RadioBsmDialogFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = RadioBsmDialogFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public RadioBsmDialogPresenter provideRadioBsmDialogPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        RadioBsmDialogFragmentTest.TestAppComponent appComponent = DaggerRadioBsmDialogFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        RadioBsmDialogFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new RadioBsmDialogFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, RadioBsmDialogFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(RadioBsmDialogFragmentTest.TestPresenterComponent.class), any(RadioBsmDialogFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testBsmDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        final RadioBsmDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setTitleText(R.string.radio_bsm_dialog);
        });
        Thread.sleep(300);
        instr.runOnMainSync(() -> {
            fragment.callbackClose();
        });
    }

    @Test
    public void testPtyDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        final RadioBsmDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setTitleText(R.string.radio_pty_dialog);
        });
        Thread.sleep(300);
        instr.runOnMainSync(() -> {
            fragment.callbackClose();
        });
    }

}