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
import java.util.List;

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
import jp.pioneer.carsync.domain.model.SoundEffectType;
import jp.pioneer.carsync.domain.model.SoundFieldControlSettingType;
import jp.pioneer.carsync.presentation.model.LiveSimulationItem;
import jp.pioneer.carsync.presentation.model.VisualEffectItem;
import jp.pioneer.carsync.presentation.presenter.LiveSimulationSettingPresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.presentation.view.RecyclerViewMatcher.withIdInRecyclerView;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_906320 on 2017/07/25.
 */
public class LiveSimulationSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<LiveSimulationSettingFragment> mFragmentRule = new FragmentTestRule<LiveSimulationSettingFragment>() {
        @Override
        protected LiveSimulationSettingFragment createDialogFragment() {
            return LiveSimulationSettingFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock LiveSimulationSettingPresenter mPresenter;

    static final ArrayList<LiveSimulationItem> LIVE_SIMULATION_ITEMS = new ArrayList<LiveSimulationItem>() {{
        add(new LiveSimulationItem(SoundFieldControlSettingType.OFF, R.drawable.p0656_ls_off,SoundFieldControlSettingType.OFF.code));
        add(new LiveSimulationItem(SoundFieldControlSettingType.LIVE_REC, R.drawable.p0653_ls_liverecording,SoundFieldControlSettingType.LIVE_REC.code));
        add(new LiveSimulationItem(SoundFieldControlSettingType.LIVE, R.drawable.p0652_ls_live,SoundFieldControlSettingType.LIVE.code));
        add(new LiveSimulationItem(SoundFieldControlSettingType.HALL, R.drawable.p0651_ls_hall,SoundFieldControlSettingType.HALL.code));
        add(new LiveSimulationItem(SoundFieldControlSettingType.LIVE_STEMIC, R.drawable.p0654_ls_livestereomic,SoundFieldControlSettingType.LIVE_STEMIC.code));
        add(new LiveSimulationItem(SoundFieldControlSettingType.DOME, R.drawable.p0650_ls_dome,SoundFieldControlSettingType.DOME.code));
        add(new LiveSimulationItem(SoundFieldControlSettingType.STADIUM, R.drawable.p0655_ls_stadium,SoundFieldControlSettingType.STADIUM.code));
    }};
    static final List<VisualEffectItem> EFFECT_SETTING_TYPES = new ArrayList<VisualEffectItem>() {{
        add(new VisualEffectItem(SoundEffectType.OFF, R.drawable.p0143_veiconbtn_1nrm));
        add(new VisualEffectItem(SoundEffectType.FEMALE, R.drawable.p0145_veiconbtn_1nrm));
        add(new VisualEffectItem(SoundEffectType.MALE, R.drawable.p0146_veiconbtn_1nrm));
    }};

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        LiveSimulationSettingFragmentTest.TestPresenterComponent presenterComponent(LiveSimulationSettingFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = LiveSimulationSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public LiveSimulationSettingPresenter provideLiveSimulationSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        LiveSimulationSettingFragmentTest.TestAppComponent appComponent = DaggerLiveSimulationSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        LiveSimulationSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new LiveSimulationSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, LiveSimulationSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(LiveSimulationSettingFragmentTest.TestPresenterComponent.class), any(LiveSimulationSettingFragment.class))).thenReturn(fragmentComponent);
    }

    @Test
    public void testLifecycle() throws Exception {
        mFragmentRule.launchActivity(null);
        LiveSimulationSettingFragment fragment = mFragmentRule.getFragment();

        assertThat(fragment.getScreenId(), is(ScreenId.LIVE_SIMULATION_SETTING));
    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        final LiveSimulationSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setLiveSimulationAdapter(LIVE_SIMULATION_ITEMS, 3);
            fragment.setVisualEffectAdapter(EFFECT_SETTING_TYPES, 3);
        });
    }

    @Test
    public void testClickVisualEffect() throws Exception {
        mFragmentRule.launchActivity(null);
        final LiveSimulationSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setLiveSimulationAdapter(LIVE_SIMULATION_ITEMS, 0);
            fragment.setVisualEffectAdapter(EFFECT_SETTING_TYPES, 0);
        });
        onView(withIdInRecyclerView(R.id.item_image, R.id.effect_list, 2)).perform(click());
        verify(mPresenter).onSelectVisualEffectAction(2);
    }
}