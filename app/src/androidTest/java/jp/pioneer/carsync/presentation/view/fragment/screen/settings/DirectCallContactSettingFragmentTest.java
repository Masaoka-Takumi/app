package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.app.Instrumentation;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import jp.pioneer.carsync.presentation.presenter.DirectCallContactSettingPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * DirectCallContactSettingFragmentのテスト
 */

public class DirectCallContactSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<DirectCallContactSettingFragment> mFragmentRule = new FragmentTestRule<DirectCallContactSettingFragment>() {
        @Override
        protected DirectCallContactSettingFragment createDialogFragment() {
            return DirectCallContactSettingFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock DirectCallContactSettingPresenter mPresenter;
    /** 仮想的に作成するDBのカラム名 */
    private static final String[] FROM = {ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.PHOTO_URI, ContactsContract.CommonDataKinds.Phone.TYPE};
    private MatrixCursor mCursor = new MatrixCursor(FROM);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        DirectCallContactSettingFragmentTest.TestPresenterComponent presenterComponent(DirectCallContactSettingFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = DirectCallContactSettingFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public DirectCallContactSettingPresenter provideDirectCallContactSettingPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        DirectCallContactSettingFragmentTest.TestAppComponent appComponent = DaggerDirectCallContactSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        DirectCallContactSettingFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new DirectCallContactSettingFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, DirectCallContactSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(DirectCallContactSettingFragmentTest.TestPresenterComponent.class), any(DirectCallContactSettingFragment.class))).thenReturn(fragmentComponent);

        // DBを仮想的に作成して値を追加していく。FROM配列の長さ＝addRowする配列の長さ。
        mCursor.addRow(new String[]{"1", "satou", "satousan", "11122223333", "", "1"});
        mCursor.addRow(new String[]{"2", "suzuki", "suzukisan", "44455556666", "", "2"});
        mCursor.addRow(new String[]{"3", "tanaka", "tanakasan", "77777777777", "", "3"});
        mCursor.moveToFirst();
    }
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final DirectCallContactSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            DirectCallContactSettingFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }
    @Test
    public void testSetTargetContact() throws Exception {
        mFragmentRule.launchActivity(null);

        final DirectCallContactSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setTargetContact(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)));
            fragment.setGroupCursor(mCursor);
        });
        //fragment.setTargetContact(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)));
        //エラー；Wanted but not invoked: Actually, there were zero interactions with this mock.
        //verify(mAdapter).setTargetKey(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)));
        //チェックアイコン表示
        onData(anything())
                .inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.check_icon)).check(matches(isDisplayed()));
        //onView(withId(R.id.check_icon)).check(matches(isDisplayed()));
    }

    @Test
    public void testSetGroupCursor() throws Exception {
        mFragmentRule.launchActivity(null);

        final DirectCallContactSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setTargetContact(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)));
            fragment.setGroupCursor(mCursor);
            //fragment.setChildrenCursor(1,mCursor);
        });
//        onData(anything()).inAdapterView(withId(R.id.list_view))
        //               .atPosition(0).check(matches(withText("satou")));
//        onData(anything()).inAdapterView(withId(R.id.list_view))
//                .atPosition(0).onChildView(withId(android.R.id.text1)).check(matches(withText("11122223333")));
        onView(withText("satou")).check(matches(isDisplayed()));
        onView(withText("suzuki")).check(matches(isDisplayed()));
        onView(withText("tanaka")).check(matches(isDisplayed()));
    }

    @Test
    public void testSetChildrenCursorRow1() throws Exception {
        mFragmentRule.launchActivity(null);

        final DirectCallContactSettingFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setTargetContact(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)));
            fragment.setGroupCursor(mCursor);
            //fragment.setChildrenCursor(0, Cursor);
            //子要素を表示できない⇒expandGroupで強制表示
            //fragment.mListView.expandGroup(0);
        });

        doAnswer(invocationOnMock -> {
            mCursor = new MatrixCursor(FROM);
            mCursor.addRow(new String[]{"1", "satou", "satousan", "11122223333", "", "1"});
            mCursor.moveToFirst();
            fragment.setChildrenCursor(0, mCursor);
            return null;
        }).when(mPresenter).onSelectGroup(mCursor);
        onView(withText("satou")).perform(click());
        verify(mPresenter).onSelectGroup(any(Cursor.class));
        onView(withText("11122223333")).check(matches(isDisplayed()));
        onView(withText("11122223333")).perform(click());
        verify(mPresenter).onNumberAction(any(Cursor.class));
        //onView(withId(R.id.number_text)).check(matches(withText("11122223333")));

        //  onData(anything())
        //          .inAdapterView(withId(R.id.list_view))
        //         .atPosition(0).onChildView(withId(R.id.number_text)).check(matches(withText("11122223333")));
        //Error performing 'load adapter data' on view 'is assignable from class: class android.widget.AdapterView'.
        //onData(withRowString(0, "11122223333")).check(matches(isDisplayed()));
        //onView(withText("satou")).perform(click());
//        doAnswer(invocationOnMock -> {
//            mCursor = new MatrixCursor(FROM);
//            mCursor.addRow(new String[]{"2", "suzuki", "suzukisan", "44455556666", ""});
//            mCursor.moveToFirst();
//            fragment.setChildrenCursor(1, mCursor);
//            return null;
//        }).when(mPresenter).onSelectGroup(mCursor);
//        onView(withText("suzuki")).perform(click());
//        verify(mPresenter).onSelectGroup(any(Cursor.class));
//        onView(withText("44455556666")).check(matches(isDisplayed()));
        //Thread.sleep(1000);

    }

    @Test
    public void testSetChildrenCursorRow2() throws Exception {
        mFragmentRule.launchActivity(null);

        final DirectCallContactSettingFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setTargetContact(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)));
            fragment.setGroupCursor(mCursor);
        });

        doAnswer(invocationOnMock -> {
            mCursor = new MatrixCursor(FROM);
            mCursor.addRow(new String[]{"2", "suzuki", "suzukisan", "44455556666", "", "1"});
            mCursor.moveToFirst();
            fragment.setChildrenCursor(1, mCursor);
            return null;
        }).when(mPresenter).onSelectGroup(mCursor);

        onView(withText("suzuki")).perform(click());
        verify(mPresenter).onSelectGroup(any(Cursor.class));
        onView(withText("44455556666")).check(matches(isDisplayed()));
        onView(withText("44455556666")).perform(click());
        verify(mPresenter).onNumberAction(any(Cursor.class));
    }

    @Test
    public void testSetOnGroupCollapse() throws Exception {
        mFragmentRule.launchActivity(null);

        final DirectCallContactSettingFragment fragment = mFragmentRule.getFragment();

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setTargetContact(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.LOOKUP_KEY)));
            fragment.setGroupCursor(mCursor);
        });

        doAnswer(invocationOnMock -> {
            mCursor = new MatrixCursor(FROM);
            mCursor.addRow(new String[]{"1", "satou", "satousan", "11122223333", "", "1"});
            mCursor.moveToFirst();
            fragment.setChildrenCursor(0, mCursor);
            return null;
        }).when(mPresenter).onSelectGroup(mCursor);
        onView(withText("satou")).perform(click());
        verify(mPresenter).onSelectGroup(any(Cursor.class));
        onView(withText("11122223333")).check(matches(isDisplayed()));

        onView(withText("satou")).perform(click());
        verify(mPresenter).onGroupCollapseAction(0);

    }
}
