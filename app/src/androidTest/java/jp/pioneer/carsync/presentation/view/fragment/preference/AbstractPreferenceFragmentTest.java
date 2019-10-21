package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;

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
import jp.pioneer.carsync.FragmentTestRule;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.component.TestFragmentComponent;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.presenter.TestPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static java.lang.Thread.sleep;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AbstractPreferenceFragmentのテスト.
 */
public class AbstractPreferenceFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<TestAbstractPreferenceFragment> mFragmentRule = new FragmentTestRule<TestAbstractPreferenceFragment>() {
        private TestAbstractPreferenceFragment mFragment;
        @Override
        protected TestAbstractPreferenceFragment createDialogFragment() {
            // 非UIスレッドから呼ばれるが、PreferenceFragmentCompatがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new TestAbstractPreferenceFragment());
            return mFragment;
        }
    };
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
        TestFragmentComponent testFragmentComponent();
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
        TestAppComponent appComponent = DaggerAbstractPreferenceFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        TestFragmentComponent fragmentComponent = presenterComponent.testFragmentComponent();
        when(mComponentFactory.getPresenterComponent(appComponent, TestAbstractPreferenceFragment.class))
                .thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(TestAbstractPreferenceFragment.class)))
                .thenReturn(fragmentComponent);
    }

    @Test
    public void lifecycle() throws Exception {
        // setup & exercise
        mFragmentRule.launchActivity(null);
        sleep(300);
        mFragmentRule.finishActivityIfNecessary();

        // verify
        InOrder inOrder = inOrder(mComponentFactory, mPresenter);
        inOrder.verify(mComponentFactory).getPresenterComponent(any(TestAppComponent.class), eq(TestAbstractPreferenceFragment.class));
        inOrder.verify(mComponentFactory).createFragmentComponent(any(TestPresenterComponent.class), any(TestAbstractPreferenceFragment.class));
        inOrder.verify(mPresenter).takeView(mFragmentRule.getFragment());
        inOrder.verify(mPresenter).initialize();
        inOrder.verify(mPresenter).resume();
        inOrder.verify(mPresenter).pause();
        inOrder.verify(mPresenter).dropView();
        inOrder.verify(mComponentFactory).releasePresenterComponent(TestAbstractPreferenceFragment.class);
    }
}
