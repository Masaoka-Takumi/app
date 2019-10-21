package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

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
import jp.pioneer.carsync.presentation.controller.RadioTabFragmentController;
import jp.pioneer.carsync.presentation.presenter.RadioTabContainerPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.DialogFragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ラジオリストコンテナ画面のテストコード
 */
public class RadioTabContainerFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public DialogFragmentTestRule<RadioTabContainerFragment> mFragmentRule = new DialogFragmentTestRule<RadioTabContainerFragment>() {
        private RadioTabContainerFragment mFragment;

        @Override
        protected RadioTabContainerFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new RadioTabContainerFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock RadioTabContainerPresenter mPresenter;
    @Mock RadioTabFragmentController mFragmentController;
    @Mock AbstractDialogFragment.Callback mCallback;

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
        public RadioTabContainerPresenter provideRadioTabContainerPresenter() {
            return mPresenter;
        }

        @Provides
        public RadioTabFragmentController provideRadioTabFragmentController() {
            return mFragmentController;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerRadioTabContainerFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, RadioTabContainerFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(RadioTabContainerFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testLifecycle() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioTabContainerFragment fragment = mFragmentRule.getFragment();
        mFragmentRule.finishActivityIfNecessary();

        verify(mFragmentController).setContainerViewId(any(Integer.class));
        verify(mCallback, never()).onClose(any(RadioTabContainerFragment.class));
    }

    @Test
    public void testCallback() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioTabContainerFragment fragment = mFragmentRule.getFragment();
        fragment.setCallback(mCallback);
        mFragmentRule.finishActivityIfNecessary();
        Thread.sleep(1000);
        verify(mFragmentController).setContainerViewId(any(Integer.class));
        verify(mCallback).onClose(any(RadioTabContainerFragment.class));
    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioTabContainerFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setTitle("FAVORITE"));

        onView(withText("FAVORITE")).check(matches(isDisplayed()));
    }

    @Test
    public void testOnNavigate() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioTabContainerFragment fragment = mFragmentRule.getFragment();

        fragment.onNavigate(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY);

        verify(mFragmentController).navigate(ScreenId.RADIO_PRESET_LIST, Bundle.EMPTY);
    }

    @Test
    public void testOnGoBack() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioTabContainerFragment fragment = mFragmentRule.getFragment();

        fragment.onGoBack();

        verify(mFragmentController).goBack();
    }

    @Test
    public void testOnClickBackButton() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioTabContainerFragment fragment = mFragmentRule.getFragment();

        onView(withId(R.id.back_button)).perform(click());

        verify(mPresenter).onCloseAction();
    }

    @Test
    public void testOnClickFavoriteButton() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioTabContainerFragment fragment = mFragmentRule.getFragment();

        onView(withId(R.id.favorite_button)).perform(click());

        verify(mPresenter).onFavoriteAction();
    }

    @Test
    public void testOnClickPresetButton() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioTabContainerFragment fragment = mFragmentRule.getFragment();

        onView(withId(R.id.preset_button)).perform(click());

        verify(mPresenter).onPresetAction();
    }

    @Test
    public void testOnClickBsmButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final RadioTabContainerFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setTab(RadioTabContainerPresenter.RadioTabType.PRESET);
            fragment.setBsmButtonVisible(true);
        });
        onView(withId(R.id.bsm_button)).perform(click());
        verify(mPresenter).onBsmAction();
    }
}