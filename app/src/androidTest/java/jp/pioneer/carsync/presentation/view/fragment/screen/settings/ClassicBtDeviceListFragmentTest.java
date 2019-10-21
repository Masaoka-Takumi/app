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
import jp.pioneer.carsync.domain.model.PairingDeviceInfo;
import jp.pioneer.carsync.presentation.presenter.ClassicBtDeviceListPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ペアリングデバイスリスト画面のテスト
 */
public class ClassicBtDeviceListFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<ClassicBtDeviceListFragment> mFragmentRule = new FragmentTestRule<ClassicBtDeviceListFragment>() {
        @Override
        protected ClassicBtDeviceListFragment createDialogFragment() {
            return ClassicBtDeviceListFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock ClassicBtDeviceListPresenter mPresenter;
    private ArrayList<PairingDeviceInfo> mArray = new ArrayList<>();
    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        ClassicBtDeviceListFragmentTest.TestPresenterComponent presenterComponent(ClassicBtDeviceListFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = ClassicBtDeviceListFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public ClassicBtDeviceListPresenter provideClassicBtDeviceListPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        ClassicBtDeviceListFragmentTest.TestAppComponent appComponent = DaggerClassicBtDeviceListFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        ClassicBtDeviceListFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new ClassicBtDeviceListFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ClassicBtDeviceListFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(ClassicBtDeviceListFragmentTest.TestPresenterComponent.class), any(ClassicBtDeviceListFragment.class))).thenReturn(fragmentComponent);
        mArray.add(new PairingDeviceInfo("00:1B:DC:XX:XX:XX","123456789"));
        mArray.add(new PairingDeviceInfo("00:1B:DC:XX:XX:XX","123456789"));
        mArray.add(new PairingDeviceInfo("00:1B:DC:XX:XX:XX","123456789"));
        mArray.add(new PairingDeviceInfo("00:1B:DC:XX:XX:XX","123456789"));
        mArray.add(new PairingDeviceInfo("00:1B:DC:XX:XX:XX","123456789"));
        mArray.add(new PairingDeviceInfo("00:1B:DC:XX:XX:XX","123456789"));
        mArray.add(new PairingDeviceInfo("00:1B:DC:XX:XX:XX","123456789"));
        mArray.add(new PairingDeviceInfo("00:1B:DC:XX:XX:XX","123456789"));
        mArray.add(new PairingDeviceInfo("00:1B:DC:XX:XX:XX","123456789"));
    }

    @Test
    public void setAdapter() throws Exception {
        mFragmentRule.launchActivity(null);
        ClassicBtDeviceListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mArray);
        });
        Thread.sleep(300);
    }

    @Test
    public void onClickGetButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.get_button)).perform(click());

        verify(mPresenter).onGetListAction();
    }

}