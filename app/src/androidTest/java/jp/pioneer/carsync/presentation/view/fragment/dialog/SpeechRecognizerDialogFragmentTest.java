package jp.pioneer.carsync.presentation.view.fragment.dialog;

import android.os.Bundle;

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
import jp.pioneer.carsync.presentation.presenter.SpeechRecognizerDialogPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static jp.pioneer.carsync.DialogFragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2017/12/05.
 */
public class SpeechRecognizerDialogFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<SpeechRecognizerDialogFragment> mFragmentRule = new DialogFragmentTestRule<SpeechRecognizerDialogFragment>() {
        @Override
        protected SpeechRecognizerDialogFragment createDialogFragment() {
            return SpeechRecognizerDialogFragment.newInstance(null, Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SpeechRecognizerDialogPresenter mPresenter;
    @Mock SpeechRecognizerDialogFragment.Callback mCallback;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        SpeechRecognizerDialogFragmentTest.TestPresenterComponent presenterComponent(SpeechRecognizerDialogFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = SpeechRecognizerDialogFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public SpeechRecognizerDialogPresenter provideSpeechRecognizerDialogPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        SpeechRecognizerDialogFragmentTest.TestAppComponent appComponent = DaggerSpeechRecognizerDialogFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        SpeechRecognizerDialogFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new SpeechRecognizerDialogFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SpeechRecognizerDialogFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(SpeechRecognizerDialogFragmentTest.TestPresenterComponent.class), any(SpeechRecognizerDialogFragment.class))).thenReturn(fragmentComponent);

    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        Thread.sleep(1000);
        onView(withId(R.id.close_button)).perform(click());
    }

}