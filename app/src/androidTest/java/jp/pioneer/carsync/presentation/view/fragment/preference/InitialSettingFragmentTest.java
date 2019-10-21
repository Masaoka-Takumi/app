package jp.pioneer.carsync.presentation.view.fragment.preference;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.util.ArraySet;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Set;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.FragmentTestRule;
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
import jp.pioneer.carsync.domain.model.AmStep;
import jp.pioneer.carsync.domain.model.FmStep;
import jp.pioneer.carsync.domain.model.MenuDisplayLanguageType;
import jp.pioneer.carsync.domain.model.RearOutputPreoutOutputSetting;
import jp.pioneer.carsync.domain.model.RearOutputSetting;
import jp.pioneer.carsync.presentation.presenter.InitialSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/12/13.
 */
public class InitialSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<InitialSettingFragment> mFragmentRule = new FragmentTestRule<InitialSettingFragment>() {
        private InitialSettingFragment mFragment;

        @Override
        protected InitialSettingFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new InitialSettingFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock InitialSettingPresenter mPresenter;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        InitialSettingFragmentTest.TestPresenterComponent presenterComponent(InitialSettingFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = InitialSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public InitialSettingPresenter provideInitialSettingPresenter() {
            return mPresenter;
        }
    }
    
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        InitialSettingFragmentTest.TestAppComponent appComponent = DaggerInitialSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        InitialSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new InitialSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, InitialSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(InitialSettingFragmentTest.TestPresenterComponent.class), any(InitialSettingFragment.class))).thenReturn(fragmentComponent);

    }

    @Test
    public void display() throws Exception {
        // exercise
        launchActivity();

        // verify
        onView(withText("Language")).check(matches(isEnabled()));
        onView(withText("FM Step")).check(matches(isEnabled()));
        onView(withText("AM Step")).check(matches(isEnabled()));
        onView(withText("Speaker Pre-out Mode")).check(matches(isEnabled()));
        onView(withText("Rear Speaker")).check(matches(isEnabled()));
    }

    @Test
    public void onClickMenuDisplayLanguage() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("Language")).perform(click());
        Thread.sleep(50);
        onView(withText("German")).perform(click());

        // verify
        verify(mPresenter).onSelectMenuDisplayLanguage(MenuDisplayLanguageType.GERMAN);
    }

    @Test
    public void onClickFmStep() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("FM Step")).perform(click());

        // verify
        verify(mPresenter).onSelectFmStepSetting();
    }

    @Test
    public void onClickAmStep() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("AM Step")).perform(click());

        // verify
        verify(mPresenter).onSelectAmStepSetting();
    }

    @Test
    public void onClickRearOutputPreoutOutput() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("Speaker Pre-out Mode")).perform(click());
        Thread.sleep(50);
        onView(withText("SUB.W/SUB.W")).perform(click());

        // verify
        verify(mPresenter).onSelectRearOutputPreoutOutput();
    }

    @Test
    public void onClickRearOutput() throws Exception {
        // setup
        launchActivity();

        // exercise
        Thread.sleep(50);
        onView(withText("Rear Speaker")).perform(click());

        // verify
        verify(mPresenter).onSelectRearOutput();
    }

    private void launchActivity() {
        mFragmentRule.launchActivity(null);
        InitialSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        Set<MenuDisplayLanguageType> supportedLanguage = new ArraySet<>();
        instr.runOnMainSync(() -> {
            fragment.setMenuDisplayLanguageSetting(true,true, supportedLanguage, MenuDisplayLanguageType.RUSSIAN);
            fragment.setFmStepSetting(true,true, FmStep._50KHZ);
            fragment.setAmStepSetting(true,true, AmStep._9KHZ);
            fragment.setRearOutputPreoutOutputSetting(true,true, RearOutputPreoutOutputSetting.REAR_SUBWOOFER);
            fragment.setRearOutputSetting(true,true, RearOutputSetting.REAR);
        });
    }
}