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
import jp.pioneer.carsync.presentation.presenter.CautionDialogPresenter;

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
 * Created by NSW00_008316 on 2017/04/25.
 */
public class CautionDialogFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<CautionDialogFragment> mFragmentRule = new DialogFragmentTestRule<CautionDialogFragment>() {
        @Override
        protected CautionDialogFragment createDialogFragment() {
            return CautionDialogFragment.newInstance(null, Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock CautionDialogPresenter mPresenter;
    @Mock CautionDialogFragment.Callback mCallback;

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
        public CautionDialogPresenter provideCautionDialogPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerCautionDialogFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, CautionDialogFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(CautionDialogFragment.class))).thenReturn(fragmentComponent);
    }

    private TestApp getTestApp() {
        return (TestApp) InstrumentationRegistry
                .getInstrumentation()
                .getTargetContext()
                .getApplicationContext();
    }

    @Test
    public void testConfirm() throws Exception {
        mFragmentRule.launchActivity(null);

        final CautionDialogFragment fragment = mFragmentRule.getFragment();
        fragment.setCallback(mCallback);

        doAnswer(invocationOnMock -> {
            fragment.callbackClose();
            return null;
        }).when(mPresenter).onConfirmAction();
        onView(withText(R.string.caution_text)).check(matches(isDisplayed()));
        onView(withText(R.string.common_ok)).perform(click());
        verify(mCallback).onClose(any(CautionDialogFragment.class));
    }

    @Test
    public void testConfirmNonCallback() throws Exception {
        mFragmentRule.launchActivity(null);

        final CautionDialogFragment fragment = mFragmentRule.getFragment();

        doAnswer(invocationOnMock -> {
            fragment.callbackClose();
            return null;
        }).when(mPresenter).onConfirmAction();

        onView(withText(R.string.caution_text)).check(matches(isDisplayed()));
        onView(withText(R.string.common_ok)).perform(click());

        verify(mCallback, never()).onClose(any(CautionDialogFragment.class));
    }
}