package jp.pioneer.carsync.presentation.view.fragment.screen.contacts;

import android.app.Instrumentation;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
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
import jp.pioneer.carsync.presentation.presenter.ContactsFavoritePresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
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
 * 電話帳 お気に入りリストの画面のテスト
 */
public class ContactsFavoriteFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<ContactsFavoriteFragment> mFragmentRule = new FragmentTestRule<ContactsFavoriteFragment>() {
        @Override
        protected ContactsFavoriteFragment createDialogFragment() {
            return ContactsFavoriteFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock ContactsFavoritePresenter mPresenter;
    /** 仮想的に作成するDBのカラム名 */
    private static final String[] FROM = {
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
            ContactsContract.Contacts.PHOTO_URI,
            ContactsContract.Contacts.STARRED,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
    };
    private MatrixCursor mCursor = new MatrixCursor(FROM);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        ContactsFavoriteFragmentTest.TestPresenterComponent presenterComponent(ContactsFavoriteFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = ContactsFavoriteFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public ContactsFavoritePresenter provideContactsFavoritePresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        ContactsFavoriteFragmentTest.TestAppComponent appComponent = DaggerContactsFavoriteFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        ContactsFavoriteFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new ContactsFavoriteFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ContactsFavoriteFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(ContactsFavoriteFragmentTest.TestPresenterComponent.class), any(ContactsFavoriteFragment.class))).thenReturn(fragmentComponent);
        // DBを仮想的に作成して値を追加していく。FROM配列の長さ＝addRowする配列の長さ。
        mCursor.addRow(new String[]{"1", "satou", "satousan", "", "1", "11122223333", "1",});
        mCursor.addRow(new String[]{"2", "suzuki", "suzukisan", "", "1", "44455556666", "2",});
        mCursor.addRow(new String[]{"3", "tanaka", "tanakasan", "", "1", "77777777777", "0",});
        mCursor.moveToFirst();
    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void newInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsFavoriteFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            ContactsFavoriteFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }

    /**
     * 親項目表示テスト
     */
    @Test
    public void testSetGroupCursor() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsFavoriteFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setGroupCursor(mCursor);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.name_text)).check(matches(withText("satou")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.name_text)).check(matches(withText("suzuki")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(2).onChildView(withId(R.id.name_text)).check(matches(withText("tanaka")));
        //Thread.sleep(5000);
    }

    /**
     * 子項目表示テスト 1行目
     */
    @Test
    public void testSetChildrenCursor() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsFavoriteFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setGroupCursor(mCursor);
        });
        doAnswer(invocationOnMock -> {
            mCursor = new MatrixCursor(FROM);
            mCursor.addRow(new String[]{"1", "satou", "satousan", "", "1", "11122223333", "1",});
            mCursor.moveToFirst();
            fragment.setChildrenCursor(0, mCursor);
            return null;
        }).when(mPresenter).setGroupCursor(mCursor);
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.name_text)).perform(click());
        verify(mPresenter).setGroupCursor(any(Cursor.class));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.number_text)).check(matches(withText("11122223333")));
    }

    /**
     * 電話番号押下テスト
     */
    @Test
    public void testOnClickNumber() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsFavoriteFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setGroupCursor(mCursor);
        });
        doAnswer(invocationOnMock -> {
            mCursor = new MatrixCursor(FROM);
            mCursor.addRow(new String[]{"1", "satou", "satousan", "", "1", "11122223333", "1",});
            mCursor.moveToFirst();
            fragment.setChildrenCursor(0, mCursor);
            return null;
        }).when(mPresenter).setGroupCursor(mCursor);
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.name_text)).perform(click());
        doAnswer(invocationOnMock -> {
            Uri uri = Uri.parse("tel:11122223333");
            fragment.dial(new Intent(Intent.ACTION_DIAL, uri));
            return null;
        }).when(mPresenter).onNumberAction(mCursor);

        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.number_text)).perform(click());
        verify(mPresenter).onNumberAction(any(Cursor.class));
    }

    /**
     * 子項目閉じテスト
     */
    @Test
    public void testSetOnGroupCollapse() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsFavoriteFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setGroupCursor(mCursor);
        });
        doAnswer(invocationOnMock -> {
            mCursor = new MatrixCursor(FROM);
            mCursor.addRow(new String[]{"1", "satou", "satousan", "", "1", "11122223333", "1",});
            mCursor.moveToFirst();
            fragment.setChildrenCursor(0, mCursor);
            return null;
        }).when(mPresenter).setGroupCursor(mCursor);
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.name_text)).perform(click());

        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.name_text)).perform(click());
        verify(mPresenter).onGroupCollapseAction(0);
    }
}