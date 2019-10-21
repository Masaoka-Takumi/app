package jp.pioneer.carsync.presentation.view.activity;

import android.app.Activity;
import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.component.TestActivityComponent;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.presenter.TestPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static java.lang.Thread.sleep;
import static org.mockito.Mockito.*;

/**
 * AbstractActivityのテスト.
 */
public class AbstractActivityTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public ActivityTestRule<TestAbstractActivity> mActivityRule =
            new ActivityTestRule<>(TestAbstractActivity.class, true, false);
    @Mock ComponentFactory mComponentFactory;
    @Mock TestPresenter mPresenter;

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
    @Subcomponent(modules = {
            TestPresenterModule.class
    })
    public interface TestPresenterComponent extends PresenterComponent {
        TestActivityComponent testActivityComponent();
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public TestPresenter provideTestPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerAbstractActivityTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        TestActivityComponent activityComponent = presenterComponent.testActivityComponent();
        when(mComponentFactory.getPresenterComponent(appComponent, TestAbstractActivity.class))
                .thenReturn(presenterComponent);
        when(mComponentFactory.createActivityComponent(any(TestPresenterComponent.class), any(TestAbstractActivity.class)))
                .thenReturn(activityComponent);
    }

    @After
    public void tearDown() throws Exception {
        finishActivityIfNecessary();
    }

    private void finishActivityIfNecessary() {
        Activity activity = mActivityRule.getActivity();
        if (activity != null && !activity.isFinishing()) {
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            Instrumentation.ActivityMonitor monitor = instr.addMonitor(TestAbstractActivity.class.getName(), null, false);
            mActivityRule.getActivity().finish();
            monitor.waitForActivity();
        }
    }

    private TestApp getTestApp() {
        return (TestApp) InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
    }

    @Test
    public void lifecycle() throws Exception {
        // setup & exercise
        mActivityRule.launchActivity(null);
        sleep(300);
        finishActivityIfNecessary();

        // verify
        InOrder inOrder = inOrder(mComponentFactory, mPresenter);
        inOrder.verify(mComponentFactory).getPresenterComponent(any(TestAppComponent.class), eq(TestAbstractActivity.class));
        inOrder.verify(mComponentFactory).createActivityComponent(any(TestPresenterComponent.class), any(TestAbstractActivity.class));
        inOrder.verify(mPresenter).takeView(mActivityRule.getActivity());
        inOrder.verify(mPresenter).initialize();
        inOrder.verify(mPresenter).resume();
        inOrder.verify(mPresenter).pause();
        inOrder.verify(mPresenter).dropView();
        inOrder.verify(mComponentFactory).releasePresenterComponent(TestAbstractActivity.class);
    }
}