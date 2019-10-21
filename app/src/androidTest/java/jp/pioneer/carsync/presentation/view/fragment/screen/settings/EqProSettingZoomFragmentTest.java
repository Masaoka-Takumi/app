package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;

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
import jp.pioneer.carsync.presentation.presenter.EqProSettingZoomPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EQ Pro Setting Zoomの画面のテスト
 */
public class EqProSettingZoomFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<EqProSettingZoomFragment> mFragmentRule = new FragmentTestRule<EqProSettingZoomFragment>() {
        @Override
        protected EqProSettingZoomFragment createDialogFragment() {
            return EqProSettingZoomFragment.newInstance(Bundle.EMPTY);
        }

    };
    @Mock ComponentFactory mComponentFactory;
    @Mock EqProSettingZoomPresenter mPresenter;

    private static final int BAND_DATA_COUNT = 31; //全Band数
    float[] BANDS = new float[]{
            0, 2, 4, 8, 12, 10, 8, 6, 3, 0, -1, -2, -5, -9, -10 ,-12, -10, -9,-5 ,-3, -1, 0, 3, 6, 8, 10, 12, 8, 4, 2, 0
    };

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        EqProSettingZoomFragmentTest.TestPresenterComponent presenterComponent(EqProSettingZoomFragmentTest.TestPresenterModule module);
    }
    @PresenterLifeCycle
    @Subcomponent(modules = EqProSettingZoomFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public EqProSettingZoomPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        EqProSettingZoomFragmentTest.TestAppComponent appComponent = DaggerEqProSettingZoomFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        EqProSettingZoomFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new EqProSettingZoomFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, EqProSettingZoomFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(EqProSettingZoomFragmentTest.TestPresenterComponent.class), any(EqProSettingZoomFragment.class))).thenReturn(fragmentComponent);


    }
    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void newInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingZoomFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            EqProSettingZoomFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), Matchers.is(args));
        });
    }

    @Test
    public void onClickZoom() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingZoomFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.onClickZoom();
        });
        verify(mPresenter).onZoomAction();
    }

    @Test
    public void onClickReset() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingZoomFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.onClickReset();
        });
        int[] bands = new int[BAND_DATA_COUNT];
        Arrays.fill(bands,0);
        //verify(mPresenter).onChangeBandValueAction(bands);
    }

    @Test
    public void setBandData() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingZoomFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setBandData(BANDS);
        });
        //verify(mPresenter).onChangeBandValueAction(BANDS);
    }

    @Test
    public void setColor() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingZoomFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        Thread.sleep(1000);
    }

}