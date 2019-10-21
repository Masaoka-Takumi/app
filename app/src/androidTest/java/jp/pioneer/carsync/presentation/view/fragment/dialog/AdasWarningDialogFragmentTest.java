package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.app.Instrumentation;
import android.os.Bundle;
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
import jp.pioneer.carsync.presentation.presenter.AdasWarningDialogPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.DialogFragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2018/01/11.
 */
public class AdasWarningDialogFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<AdasWarningDialogFragment> mFragmentRule = new DialogFragmentTestRule<AdasWarningDialogFragment>() {
        @Override
        protected AdasWarningDialogFragment createDialogFragment() {
            return AdasWarningDialogFragment.newInstance(null, Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock AdasWarningDialogPresenter mPresenter;
    @Mock AdasWarningDialogFragment.Callback mCallback;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        AdasWarningDialogFragmentTest.TestPresenterComponent presenterComponent(AdasWarningDialogFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = AdasWarningDialogFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public AdasWarningDialogPresenter provideAdasWarningDialogPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        AdasWarningDialogFragmentTest.TestAppComponent appComponent = DaggerAdasWarningDialogFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        AdasWarningDialogFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new AdasWarningDialogFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, AdasWarningDialogFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(AdasWarningDialogFragmentTest.TestPresenterComponent.class), any(AdasWarningDialogFragment.class))).thenReturn(fragmentComponent);
    }


    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);

        final AdasWarningDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdasImage(R.drawable.p1203_ldw_l);
            fragment.setAdasText(getTargetContext().getResources().getString(R.string.adas_warning_lane_departure_left));
        });

    }

    @Test
    public void testCloseDialog() throws Exception {
        mFragmentRule.launchActivity(null);

        final AdasWarningDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setCallback(mCallback);
            fragment.callbackClose();
        });
        verify(mCallback).onClose(any(AdasWarningDialogFragment.class));
    }
}