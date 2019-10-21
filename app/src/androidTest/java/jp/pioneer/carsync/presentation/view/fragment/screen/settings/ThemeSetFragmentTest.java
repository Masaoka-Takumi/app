package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.FragmentTestRule;
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
import jp.pioneer.carsync.domain.model.ThemeType;
import jp.pioneer.carsync.presentation.model.IlluminationColorModel;
import jp.pioneer.carsync.presentation.model.ThemeSelectItem;
import jp.pioneer.carsync.presentation.presenter.ThemeSetPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * テーマセット設定画面のテスト
 */
public class ThemeSetFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<ThemeSetFragment> mFragmentRule = new FragmentTestRule<ThemeSetFragment>() {
        @Override
        protected ThemeSetFragment createDialogFragment() {
            return ThemeSetFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock ThemeSetPresenter mPresenter;

    private static final ArrayList<ThemeSelectItem> mSampleItems = new ArrayList<ThemeSelectItem>(){{
        for (ThemeType type : ThemeType.values()) {
            add(new ThemeSelectItem(type, type.getResourceId(), type.getThumbnail(),type.isVideo(), type.code));
        }
    }};
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
        public ThemeSetPresenter provideThemeSetPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerThemeSetFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ThemeSetFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(ThemeSetFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testLifecycle() throws Exception {
        IlluminationColorModel commonColorModel = new IlluminationColorModel();
        commonColorModel.red.setValue(255);
        commonColorModel.green.setValue(0);
        commonColorModel.blue.setValue(0);

        mFragmentRule.launchActivity(null);

        ThemeSetFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mSampleItems);
            fragment.setCurrentItem(2);
            fragment.setDispColorSettingEnabled(true);
            fragment.setKeyColorSettingEnabled(true);
            fragment.setDispColor(commonColorModel);
            fragment.setKeyColor(commonColorModel);
            fragment.setUIColor(R.color.ui_color_red);
        });

        onView(withId(R.id.carousel_pager)).perform(swipeLeft());
        Thread.sleep(300);
        verify(mPresenter).onSelectThemeAction(4);
    }

    @Test
    public void testDisplayDisable() throws Exception {
        IlluminationColorModel commonColorModel = new IlluminationColorModel();
        commonColorModel.red.setValue(255);
        commonColorModel.green.setValue(0);
        commonColorModel.blue.setValue(0);

        mFragmentRule.launchActivity(null);

        ThemeSetFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mSampleItems);
            fragment.setCurrentItem(2);
            fragment.setDispColorSettingEnabled(false);
            fragment.setKeyColorSettingEnabled(false);
            fragment.setDispColor(commonColorModel);
            fragment.setKeyColor(commonColorModel);
            fragment.setUIColor(R.color.ui_color_red);
        });

        onView(withId(R.id.carousel_pager)).perform(swipeLeft());
        Thread.sleep(300);
        verify(mPresenter).onSelectThemeAction(4);
    }

}