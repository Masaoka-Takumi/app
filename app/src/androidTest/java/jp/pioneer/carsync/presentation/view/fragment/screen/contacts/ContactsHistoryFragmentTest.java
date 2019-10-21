package jp.pioneer.carsync.presentation.view.fragment.screen.contacts;

import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Date;

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
import jp.pioneer.carsync.presentation.model.ContactsHistoryItem;
import jp.pioneer.carsync.presentation.presenter.ContactsHistoryPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.presentation.view.EspressoTestsMatchers.withDrawable;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 電話帳 発着信履歴リストの画面のテスト
 */
public class ContactsHistoryFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<ContactsHistoryFragment> mFragmentRule = new FragmentTestRule<ContactsHistoryFragment>() {
        @Override
        protected ContactsHistoryFragment createDialogFragment() {
            return ContactsHistoryFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock ContactsHistoryPresenter mPresenter;

/*    *//** 仮想的に作成するDBのカラム名 *//*
    private static final String[] FROM = {
            CallLog.Calls._ID, CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
            CallLog.Calls.CACHED_NUMBER_TYPE,
            CallLog.Calls.DATE,
            CallLog.Calls.TYPE,
    };
    private MatrixCursor mCursor = new MatrixCursor(FROM);*/
    private ArrayList<ContactsHistoryItem> mHistoryList = new ArrayList<>();;
    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        ContactsHistoryFragmentTest.TestPresenterComponent presenterComponent(ContactsHistoryFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = ContactsHistoryFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public ContactsHistoryPresenter provideContactsHistoryPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        ContactsHistoryFragmentTest.TestAppComponent appComponent = DaggerContactsHistoryFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        ContactsHistoryFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new ContactsHistoryFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ContactsHistoryFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(ContactsHistoryFragmentTest.TestPresenterComponent.class), any(ContactsHistoryFragment.class))).thenReturn(fragmentComponent);
/*        // DBを仮想的に作成して値を追加していく。FROM配列の長さ＝addRowする配列の長さ。
        mCursor.addRow(new String[]{"1", "11122223333", "satou", "1", "1494390000000", "1"});
        mCursor.addRow(new String[]{"2", "44455556666", "", "1", "1494390509000", "2"});
        mCursor.addRow(new String[]{"3", "77777777777", "tanaka", "1", "1494300000000", "3"});
        mCursor.moveToFirst();*/

        mHistoryList.add(new ContactsHistoryItem(1,"satou", 1, new Date(), "11122223333"));
        mHistoryList.add(new ContactsHistoryItem(1,"suzuki", 1, new Date(), "44455556666"));
        mHistoryList.add(new ContactsHistoryItem(1,"tanaka", 1, new Date(), "77777777777"));
    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsHistoryFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            ContactsHistoryFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }

    /**
     * 発着信履歴表示のテスト
     */
    @Test
    public void testSetHistoryCursor() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsHistoryFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setHistoryList(mHistoryList));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.name_text)).check(matches(withText("satou")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.name_text)).check(matches(withText("suzuki")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(2).onChildView(withId(R.id.name_text)).check(matches(withText("tanaka")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.date_text)).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.history_type)).check(matches(withDrawable(R.drawable.p0055_callreceived)));
    }

    /**
     * 発着信履歴押下のテスト
     */
    @Test
    public void testOnClickListItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsHistoryFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setHistoryList(mHistoryList));
        doAnswer(invocationOnMock -> {
            Uri uri = Uri.parse("tel:11122223333");
            fragment.dial(new Intent(Intent.ACTION_DIAL, uri));
            return null;
        }).when(mPresenter).onContactsHistoryAction(mHistoryList.get(0));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.name_text)).perform(click());
        verify(mPresenter).onContactsHistoryAction(mHistoryList.get(0));
    }
}