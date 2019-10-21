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
import jp.pioneer.carsync.presentation.presenter.AccidentDetectDialogPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.DialogFragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 事故通知ダイアログのテスト
 */
public class AccidentDetectDialogFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<AccidentDetectDialogFragment> mFragmentRule = new DialogFragmentTestRule<AccidentDetectDialogFragment>() {
        @Override
        protected AccidentDetectDialogFragment createDialogFragment() {
            return AccidentDetectDialogFragment.newInstance(null, Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock AccidentDetectDialogPresenter mPresenter;
    @Mock AccidentDetectDialogFragment.Callback mCallback;

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
        public AccidentDetectDialogPresenter provideAccidentDetectDialogPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerAccidentDetectDialogFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, AccidentDetectDialogFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(AccidentDetectDialogFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testInitProgress() throws Exception {
        mFragmentRule.launchActivity(null);

        final AccidentDetectDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.initProgress(240000);
            fragment.updateProgress(240000);
        });

        onView(withText(R.string.tap_to_cancel)).check(matches(isDisplayed()));
        onView(withText("04:00")).check(matches(isDisplayed()));
    }

    @Test
    public void testUpdateText() throws Exception {
        mFragmentRule.launchActivity(null);

        final AccidentDetectDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.updateText("TEST", false));

        onView(withId(R.id.after_layout)).check(matches(isDisplayed()));
        onView(withText("TEST")).check(matches(isDisplayed()));
    }

    @Test
    public void testCancelBeforeTimeOut() throws Exception {
        mFragmentRule.launchActivity(null);

        final AccidentDetectDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.initProgress(240000);
            fragment.updateProgress(240000);
        });
        fragment.setCallback(mCallback);

        doAnswer(invocationOnMock -> {
            fragment.callbackClose();
            return null;
        }).when(mPresenter).onCancelAction();

        onView(withId(R.id.before_layout)).perform(click());

        verify(mCallback).onClose(any(AccidentDetectDialogFragment.class));
    }

    @Test
    public void testCancelAfterTimeOut() throws Exception {
        mFragmentRule.launchActivity(null);

        final AccidentDetectDialogFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.updateText("TEST", false));

        onView(withId(R.id.after_layout)).perform(click());

        verify(mPresenter).onCancelAction();
    }
}