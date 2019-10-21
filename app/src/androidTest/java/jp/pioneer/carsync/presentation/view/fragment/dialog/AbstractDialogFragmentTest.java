package jp.pioneer.carsync.presentation.view.fragment.dialog;

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
import jp.pioneer.carsync.DialogFragmentTestRule;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.component.TestDialogFragmentComponent;
import jp.pioneer.carsync.application.di.component.TestFragmentComponent;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.presenter.TestDialogPresenter;
import jp.pioneer.carsync.presentation.view.TestDialogView;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static java.lang.Thread.sleep;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * AbstractDialogFragmentのテスト.
 */
public class AbstractDialogFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<TestAbstractDialogFragment> mFragmentRule = new DialogFragmentTestRule<TestAbstractDialogFragment>() {
        @Override
        protected TestAbstractDialogFragment createDialogFragment() {
            return new TestAbstractDialogFragment();
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock TestDialogPresenter mPresenter;

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
        TestDialogFragmentComponent testFragmentComponent();
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public TestDialogPresenter provideTestDialogPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerAbstractDialogFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        TestDialogFragmentComponent fragmentComponent = presenterComponent.testFragmentComponent();
        when(mComponentFactory.getPresenterComponent(appComponent, TestAbstractDialogFragment.class))
                .thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(TestAbstractDialogFragment.class)))
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
        inOrder.verify(mComponentFactory).getPresenterComponent(any(TestAppComponent.class), eq(TestAbstractDialogFragment.class));
        inOrder.verify(mComponentFactory).createFragmentComponent(any(TestPresenterComponent.class), any(TestAbstractDialogFragment.class));
        inOrder.verify(mPresenter).takeView(mFragmentRule.getFragment());
        inOrder.verify(mPresenter).initialize();
        inOrder.verify(mPresenter).resume();
        inOrder.verify(mPresenter).pause();
        inOrder.verify(mPresenter).dropView();
        inOrder.verify(mComponentFactory).releasePresenterComponent(TestAbstractDialogFragment.class);
    }

    @Test
    public void callback() throws Exception {
        // setup
        mFragmentRule.launchActivity(null);
        sleep(300);
        TestDialogView.Callback callback = mock(TestDialogView.Callback.class);
        mFragmentRule.getFragment().setCallback(callback);

        // exercise
        mFragmentRule.getFragment().doCallback();

        // verify
        verify(callback).onCallback();
    }
}