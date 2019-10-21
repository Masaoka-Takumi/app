package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.DialogFragmentTestRule;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.di.FragmentLifeCycle;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.controller.SourceSelectFragmentController;
import jp.pioneer.carsync.presentation.presenter.SourceSelectContainerPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.application.di.module.FragmentModule.CHILD_FRAGMENT_MANAGER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/10/24.
 */
public class SourceSelectContainerFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<SourceSelectContainerFragment> mFragmentRule = new DialogFragmentTestRule<SourceSelectContainerFragment>() {
        @Override
        protected SourceSelectContainerFragment createDialogFragment() {
            return SourceSelectContainerFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SourceSelectContainerPresenter mPresenter;
    @Mock AbstractDialogFragment.Callback mCallback;
    @Mock SourceSelectFragmentController mFragmentController;
    @Mock FragmentManager mFragmentManager;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        SourceSelectContainerFragmentTest.TestPresenterComponent presenterComponent(SourceSelectContainerFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = SourceSelectContainerFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
        SourceSelectContainerFragmentTest.TestFragmentComponent testFragmentComponent(SourceSelectContainerFragmentTest.TestFragmentModule module);
    }

    @FragmentLifeCycle
    @Subcomponent(modules = SourceSelectContainerFragmentTest.TestFragmentModule.class)
    public interface TestFragmentComponent extends FragmentComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public SourceSelectContainerPresenter provideSourceSelectContainerPresenter() {
            return mPresenter;
        }

        @Provides
        public SourceSelectFragmentController provideSourceSelectFragmentController() {
            return mFragmentController;
        }
    }

    @Module
    public class TestFragmentModule {
        public TestFragmentModule() {
        }

        @Provides
        public FragmentManager provideFragmentManager() {
            return mFragmentManager;
        }

        @Provides
        @Named(CHILD_FRAGMENT_MANAGER)
        public FragmentManager provideChildFragmentManager() {
            return mFragmentManager;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        SourceSelectContainerFragmentTest.TestAppComponent appComponent = DaggerSourceSelectContainerFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        SourceSelectContainerFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new SourceSelectContainerFragmentTest.TestPresenterModule());
        SourceSelectContainerFragmentTest.TestFragmentComponent fragmentComponent = presenterComponent.testFragmentComponent(new SourceSelectContainerFragmentTest.TestFragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SourceSelectContainerFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(SourceSelectContainerFragmentTest.TestPresenterComponent.class), any(SourceSelectContainerFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testLifeCycle() throws Exception {
        mFragmentRule.launchActivity(null);
        mFragmentRule.getActivity().finish();

        Thread.sleep(200);

        verify(mFragmentController).setContainerViewId(any(Integer.class));
    }

    @Test
    public void testOnGoBack() throws Exception {
        mFragmentRule.launchActivity(null);

        when(mFragmentController.goBack()).thenReturn(true);

        boolean actual = mFragmentRule.getFragment().onGoBack();

        assertThat(actual, is(true));
    }

    @Test
    public void testOnNavigate() throws Exception {
        mFragmentRule.launchActivity(null);
        ScreenId screenId = ScreenId.SOURCE_SELECT;
        Bundle args = Bundle.EMPTY;

        when(mFragmentController.navigate(any(ScreenId.class), any(Bundle.class))).thenReturn(true);

        boolean actual = mFragmentRule.getFragment().onNavigate(screenId, args);

        assertThat(actual, is(true));
    }

}