package jp.pioneer.carsync.presentation.view.fragment.dialog;

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
import jp.pioneer.carsync.presentation.presenter.SessionStoppedDialogPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/08/02.
 */
public class SessionStoppedDialogFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<SessionStoppedDialogFragment> mFragmentRule = new DialogFragmentTestRule<SessionStoppedDialogFragment>() {
        @Override
        protected SessionStoppedDialogFragment createDialogFragment() {
            return SessionStoppedDialogFragment.newInstance(null, Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SessionStoppedDialogPresenter mPresenter;
    @Mock SessionStoppedDialogFragment.Callback mCallback;

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
        public SessionStoppedDialogPresenter provideSessionStoppedDialogPresenter() {
            return mPresenter;
        }
    }

    private TestApp getTestApp() {
        return (TestApp) InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerSessionStoppedDialogFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SessionStoppedDialogFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(SessionStoppedDialogFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testConfirm() throws Exception {
        mFragmentRule.launchActivity(null);

        final SessionStoppedDialogFragment fragment = mFragmentRule.getFragment();
        fragment.setCallback(mCallback);

        doAnswer(invocationOnMock -> {
            fragment.callbackClose();
            return null;
        }).when(mPresenter).onConfirmAction();

        onView(withText("Connection Closed")).check(matches(isDisplayed()));
        onView(withText("確認")).perform(click());

        verify(mCallback).onClose(any(SessionStoppedDialogFragment.class));
    }

    @Test
    public void testConfirmNonCallback() throws Exception {
        mFragmentRule.launchActivity(null);

        final SessionStoppedDialogFragment fragment = mFragmentRule.getFragment();

        doAnswer(invocationOnMock -> {
            fragment.callbackClose();
            return null;
        }).when(mPresenter).onConfirmAction();

        onView(withText("Connection Closed")).check(matches(isDisplayed()));
        onView(withText("確認")).perform(click());

        verify(mCallback, never()).onClose(any(SessionStoppedDialogFragment.class));
    }

}