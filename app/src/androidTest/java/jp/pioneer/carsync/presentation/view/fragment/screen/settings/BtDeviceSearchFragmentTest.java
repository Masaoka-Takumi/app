package jp.pioneer.carsync.presentation.view.fragment.screen.settings;


import android.app.Instrumentation;
import android.database.MatrixCursor;
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
import jp.pioneer.carsync.domain.content.SettingListContract;
import jp.pioneer.carsync.presentation.presenter.BtDeviceSearchPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.anything;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_007906 on 2018/01/19.
 */
public class BtDeviceSearchFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<BtDeviceSearchFragment> mFragmentRule = new FragmentTestRule<BtDeviceSearchFragment>() {
        @Override
        protected BtDeviceSearchFragment createDialogFragment() {
            return BtDeviceSearchFragment.newInstance(Bundle.EMPTY);
        }

    };
    @Mock ComponentFactory mComponentFactory;
    @Mock BtDeviceSearchPresenter mPresenter;
    private static final String[] FROM = {SettingListContract.SettingListBaseColumn._ID,SettingListContract.SettingListBaseColumn.BD_ADDRESS,
            SettingListContract.SettingListBaseColumn.DEVICE_NAME,SettingListContract.SettingListBaseColumn.AUDIO_SUPPORTED,
            SettingListContract.SettingListBaseColumn.PHONE_SUPPORTED};
    private MatrixCursor mCursor = new MatrixCursor(FROM);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })

    public interface TestAppComponent extends AppComponent {
        BtDeviceSearchFragmentTest.TestPresenterComponent presenterComponent(BtDeviceSearchFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = BtDeviceSearchFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public BtDeviceSearchPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        BtDeviceSearchFragmentTest.TestAppComponent appComponent = DaggerBtDeviceSearchFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        BtDeviceSearchFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new BtDeviceSearchFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, BtDeviceSearchFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(BtDeviceSearchFragmentTest.TestPresenterComponent.class), any(BtDeviceSearchFragment.class))).thenReturn(fragmentComponent);

        mCursor.addRow(new String[] {"1", "2C:4D:54:BC:33:EF" ,"TestPhone1","1","1"});
        mCursor.addRow(new String[] {"2", "2C:4D:54:BC:33:EF" ,"TestPhone2","1","1"});
        mCursor.addRow(new String[] {"3", "2C:4D:54:BC:33:EF" ,"TestPhone3","1","1"});

        mCursor.moveToFirst();

    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtDeviceSearchFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceCursor(mCursor, Bundle.EMPTY);
            fragment.setSearchStatus(true);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.name_text)).check(matches(withText("TestPhone1")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.name_text)).check(matches(withText("TestPhone2")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(2).onChildView(withId(R.id.name_text)).check(matches(withText("TestPhone3")));
        onView(withId(R.id.search_status_icon)).check(matches(isDisplayed()));
    }

    @Test
    public void testOnClickListItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtDeviceSearchFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceCursor(mCursor, Bundle.EMPTY);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).perform(click());
        verify(mPresenter).onSelectDevice(mCursor);
    }

    @Test
    public void testOnClickSearchButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.search_button)).perform(click());
        verify(mPresenter).onStartSearch();
    }

    @Test
    public void testOnClickStopButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.stop_button)).perform(click());
        verify(mPresenter).onStopSearch();
    }

}