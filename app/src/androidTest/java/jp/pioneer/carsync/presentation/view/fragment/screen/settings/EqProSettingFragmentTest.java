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
import jp.pioneer.carsync.presentation.presenter.EqProSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * EQ Pro Settingの画面のテスト
 */
public class EqProSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<EqProSettingFragment> mFragmentRule = new FragmentTestRule<EqProSettingFragment>() {
        @Override
        protected EqProSettingFragment createDialogFragment() {
            return EqProSettingFragment.newInstance(Bundle.EMPTY);
        }

    };
    @Mock ComponentFactory mComponentFactory;
    @Mock EqProSettingPresenter mPresenter;

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
        EqProSettingFragmentTest.TestPresenterComponent presenterComponent(EqProSettingFragmentTest.TestPresenterModule module);
    }
    @PresenterLifeCycle
    @Subcomponent(modules = EqProSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public EqProSettingPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        EqProSettingFragmentTest.TestAppComponent appComponent = DaggerEqProSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        EqProSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new EqProSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, EqProSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(EqProSettingFragmentTest.TestPresenterComponent.class), any(EqProSettingFragment.class))).thenReturn(fragmentComponent);


    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            EqProSettingFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), Matchers.is(args));
        });
    }

    @Test
    public void testOnClickZoom() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.onClickZoom();
        });
        verify(mPresenter).onZoomAction();
    }

    @Test
    public void testOnClickReset() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.onClickReset();
        });
        int[] bands = new int[BAND_DATA_COUNT];
        Arrays.fill(bands,0);
        //verify(mPresenter).onChangeBandValueAction(any(int[].class));
    }

    @Test
    public void testSetBandData() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setBandData(BANDS);
        });
        //verify(mPresenter).onChangeBandValueAction(any(int[].class));
    }

    @Test
    public void testSetColor() throws Exception {
        mFragmentRule.launchActivity(null);
        final EqProSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        Thread.sleep(1000);
    }

}