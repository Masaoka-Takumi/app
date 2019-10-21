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
import jp.pioneer.carsync.presentation.presenter.BtDeviceListPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BtDeviceList画面のTest
 */
public class BtDeviceListFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<BtDeviceListFragment> mFragmentRule = new FragmentTestRule<BtDeviceListFragment>() {
        @Override
        protected BtDeviceListFragment createDialogFragment() {
            return BtDeviceListFragment.newInstance(Bundle.EMPTY);
        }

    };
    @Mock ComponentFactory mComponentFactory;
    @Mock BtDeviceListPresenter mPresenter;
    private static final String[] FROM = {SettingListContract.SettingListBaseColumn._ID,SettingListContract.SettingListBaseColumn.BD_ADDRESS,
            SettingListContract.SettingListBaseColumn.DEVICE_NAME,SettingListContract.SettingListBaseColumn.AUDIO_SUPPORTED,
            SettingListContract.SettingListBaseColumn.PHONE_SUPPORTED, SettingListContract.DeviceList.AUDIO_CONNECTED,
            SettingListContract.DeviceList.PHONE_1_CONNECTED, SettingListContract.DeviceList.PHONE_2_CONNECTED,
            SettingListContract.DeviceList.LAST_AUDIO_DEVICE, SettingListContract.DeviceList.SESSION_CONNECTED,
            SettingListContract.DeviceList.AUDIO_CONNECT_STATUS, SettingListContract.DeviceList.PHONE_CONNECT_STATUS,
            SettingListContract.DeviceList.DELETE_STATUS};
    private MatrixCursor mCursor = new MatrixCursor(FROM);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })

    public interface TestAppComponent extends AppComponent {
        BtDeviceListFragmentTest.TestPresenterComponent presenterComponent(BtDeviceListFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = BtDeviceListFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public BtDeviceListPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        BtDeviceListFragmentTest.TestAppComponent appComponent = DaggerBtDeviceListFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        BtDeviceListFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new BtDeviceListFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, BtDeviceListFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(BtDeviceListFragmentTest.TestPresenterComponent.class), any(BtDeviceListFragment.class))).thenReturn(fragmentComponent);

        mCursor.addRow(new String[] {"1", "2C:4D:54:BC:33:EF" ,"TestPhone1","1","1","1","1","0", "1","1", "2","2","0"});
        mCursor.addRow(new String[] {"2", "2C:4D:54:BC:33:EF" ,"TestPhone2","1","1","1","0","1", "0","0", "0","0","0"});
        mCursor.addRow(new String[] {"3", "2C:4D:54:BC:33:EF" ,"TestPhone3","1","1","0","0","0", "0","0", "0","0","0"});

        mCursor.moveToFirst();

    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtDeviceListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            BtDeviceListFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtDeviceListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceCursor(mCursor, Bundle.EMPTY);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.name_text)).check(matches(withText("TestPhone1")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.name_text)).check(matches(withText("TestPhone2")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(2).onChildView(withId(R.id.name_text)).check(matches(withText("TestPhone3")));

    }

    @Test
    public void onClickListItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtDeviceListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceCursor(mCursor, Bundle.EMPTY);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).perform(click());
    }

    @Test
    public void onClickAddButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.add_button)).perform(click());
        verify(mPresenter).onClickSearchButton();
    }

    @Test
    public void onClickDeleteButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtDeviceListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceCursor(mCursor, Bundle.EMPTY);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).perform(click());
        onView(withId(R.id.delete_button)).perform(click());
        verify(mPresenter).onDeleteDevice(mCursor);
    }
    @Test
    public void onClickMusicButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtDeviceListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceCursor(mCursor, Bundle.EMPTY);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.music_view)).perform(click());

        verify(mPresenter).onConnectA2dpDevice(mCursor);
    }

    @Test
    public void onClickPhoneButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtDeviceListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDeviceCursor(mCursor, Bundle.EMPTY);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.phone_view)).perform(click());

        verify(mPresenter).onConnectHfpDevice(mCursor);
    }
}