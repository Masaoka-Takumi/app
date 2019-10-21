package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Instrumentation;
import android.database.Cursor;
import android.database.MatrixCursor;
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
import jp.pioneer.carsync.domain.content.UsbListContract;
import jp.pioneer.carsync.presentation.presenter.UsbListPresenter;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.anything;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * USBリスト画面のTest
 */
public class UsbListFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<UsbListFragment> mFragmentRule = new FragmentTestRule<UsbListFragment>() {
        private UsbListFragment mFragment;

        @Override
        protected UsbListFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new UsbListFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock UsbListPresenter mPresenter;
    @Mock AbstractDialogFragment.Callback mCallback;

    private static final String[] FROM = {UsbListContract._ID, UsbListContract.LIST_INDEX, UsbListContract.TEXT, UsbListContract.TYPE, UsbListContract.DATA_ENABLED};
    private MatrixCursor mCursor = new MatrixCursor(FROM);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })

    public interface TestAppComponent extends AppComponent {
        UsbListFragmentTest.TestPresenterComponent presenterComponent(UsbListFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = UsbListFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public UsbListPresenter provideUsbListPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        UsbListFragmentTest.TestAppComponent appComponent = DaggerUsbListFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        UsbListFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new UsbListFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, UsbListFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(UsbListFragmentTest.TestPresenterComponent.class), any(UsbListFragment.class))).thenReturn(fragmentComponent);
        mCursor.addRow(new String[]{"1", "1", "Folder1", "1", "1"});
        mCursor.addRow(new String[]{"2", "2", "Folder2", "1", "1"});
        mCursor.addRow(new String[]{"3", "3", "Folder3", "2", "1"});
        mCursor.addRow(new String[]{"4", "4", "Folder4", "2", "1"});
        mCursor.addRow(new String[]{"5", "5", "File1", "0", "1"});
        mCursor.addRow(new String[]{"6", "6", "File2", "0", "1"});
        mCursor.addRow(new String[]{"7", "7", "File3", "0", "1"});
        mCursor.addRow(new String[]{"8", "8", "File4", "0", "1"});
        mCursor.addRow(new String[]{"9", "9", "File5", "0", "0"});
        mCursor.addRow(new String[]{"10", "10", "File6", "0", "0"});
        mCursor.addRow(new String[]{"11", "11", "Folder1", "1", "1"});
        mCursor.addRow(new String[]{"12", "12", "Folder2", "1", "1"});
        mCursor.addRow(new String[]{"13", "13", "Folder3", "2", "1"});
        mCursor.addRow(new String[]{"14", "14", "Folder4", "2", "1"});
        mCursor.addRow(new String[]{"15", "15", "File1", "0", "1"});
        mCursor.addRow(new String[]{"16", "16", "File2", "0", "1"});
        mCursor.addRow(new String[]{"17", "17", "File3", "0", "1"});
        mCursor.addRow(new String[]{"18", "18", "File4", "0", "1"});
        mCursor.addRow(new String[]{"19", "19", "File5", "0", "0"});
        mCursor.addRow(new String[]{"20", "20", "File6", "0", "0"});
        mCursor.moveToFirst();

    }

    @Test
    public void setCursor() throws Exception {
        mFragmentRule.launchActivity(null);
        final UsbListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setCursor(mCursor);
        });
        Thread.sleep(300);
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.title_text)).check(matches(withText("Folder1")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.title_text)).check(matches(withText("Folder2")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(4).onChildView(withId(R.id.title_text)).check(matches(withText("File1")));
        verify(mPresenter, atLeastOnce()).onAddListItem(anyInt(), anyInt());
    }

    @Test
    public void onClickListItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final UsbListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setCursor(mCursor);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).perform(click());
        verify(mPresenter).onSelectList(eq(1), any(Cursor.class));
    }

    @Test
    public void testOnScrollList() throws Exception {
        mFragmentRule.launchActivity(null);
        final UsbListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setCursor(mCursor);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(12).perform(click());
        verify(mPresenter, atLeastOnce()).onAddListItem(anyInt(), anyInt());
        verify(mPresenter, atLeastOnce()).onRemoveListItem(anyInt(), anyInt());
    }

    @Test
    public void onClickBackButton() throws Exception {
        mFragmentRule.launchActivity(null);
        final UsbListFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setCursor(mCursor);
        });
        onView(withId(R.id.back_button)).perform(click());
        verify(mPresenter).onBackAction();
    }

}