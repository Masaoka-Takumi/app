package jp.pioneer.carsync.presentation.view.fragment.screen;

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
 * AbstractScreenFragmentのテスト.
 */
public class AbstractScreenFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<TestAbstractScreenFragment> mFragmentRule = new FragmentTestRule<TestAbstractScreenFragment>() {
        @Override
        protected TestAbstractScreenFragment createDialogFragment() {
            return new TestAbstractScreenFragment();
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
        public TestPresenter provideMainPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerAbstractScreenFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        TestFragmentComponent fragmentComponent = presenterComponent.testFragmentComponent();
        when(mComponentFactory.getPresenterComponent(appComponent, TestAbstractScreenFragment.class))
                .thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(TestAbstractScreenFragment.class)))
                .thenReturn(fragmentComponent);
    }

    @Test
    public void lifecycle() throws Exception {
        mFragmentRule.launchActivity(null);

        sleep(300);
        mFragmentRule.finishActivityIfNecessary();

        InOrder inOrder = inOrder(mComponentFactory, mPresenter);
        inOrder.verify(mComponentFactory).getPresenterComponent(any(TestAppComponent.class), eq(TestAbstractScreenFragment.class));
        inOrder.verify(mComponentFactory).createFragmentComponent(any(TestPresenterComponent.class), any(TestAbstractScreenFragment.class));
        inOrder.verify(mPresenter).takeView(mFragmentRule.getFragment());
        inOrder.verify(mPresenter).initialize();
        inOrder.verify(mPresenter).resume();
        inOrder.verify(mPresenter).pause();
        inOrder.verify(mPresenter).dropView();
        inOrder.verify(mComponentFactory).releasePresenterComponent(TestAbstractScreenFragment.class);
    }
}
