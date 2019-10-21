package jp.pioneer.carsync.presentation.view.fragment.screen.search;

import android.app.Instrumentation;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.LoaderManager;

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
import jp.pioneer.carsync.presentation.presenter.SearchContactPresenter;
import jp.pioneer.carsync.presentation.view.argument.SearchContentParams;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.anything;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008316 on 2017/05/10.
 */
public class SearchContactFragmentTest {
//    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
//    @Rule public FragmentTestRule<SearchContactFragment> mFragmentRule = new FragmentTestRule<SearchContactFragment>() {
//        @Override
//        protected SearchContactFragment createDialogFragment() {
//            String[] keywords = new String[]{"aaa", "bbb", "ccc"};
//            Bundle args = SearchContentParams.toBundle(VoiceSearchContent.BOTH, keywords);
//            return SearchContactFragment.newInstance(args);
//        }
//    };
//    @Mock ComponentFactory mComponentFactory;
//    @Mock SearchContactPresenter mPresenter;
//
//    private static final String[] FROM = {
//            ContactsContract.Contacts._ID,
//            ContactsContract.Contacts.DISPLAY_NAME,
//            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,
//            ContactsContract.Contacts.PHOTO_URI,
//            ContactsContract.CommonDataKinds.Phone.NUMBER,
//            ContactsContract.CommonDataKinds.Phone.TYPE,
//    };
//    private MatrixCursor mCursor = new MatrixCursor(FROM);
//
//    @Singleton
//    @Component(modules = {
//            AppModule.class,
//            DomainModule.class,
//            InfrastructureModule.class,
//            InfrastructureBindsModule.class
//    })
//    public interface TestAppComponent extends AppComponent {
//        TestPresenterComponent presenterComponent(TestPresenterModule module);
//    }
//
//    @PresenterLifeCycle
//    @Subcomponent(modules = TestPresenterModule.class)
//    public interface TestPresenterComponent extends PresenterComponent {
//    }
//
//    @Module
//    public class TestPresenterModule {
//        public TestPresenterModule() {
//        }
//
//        @Provides
//        public SearchContactPresenter provideSearchContactPresenter() {
//            return mPresenter;
//        }
//    }
//
//    @Before
//    public void setUp() throws Exception {
//        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
//        TestApp testApp = getTestApp();
//        TestAppComponent appComponent = DaggerSearchContactFragmentTest_TestAppComponent.builder().build();
//        testApp.setAppComponent(appComponent);
//        testApp.setComponentFactory(mComponentFactory);
//        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
//        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
//        when(mComponentFactory.getPresenterComponent(appComponent, SearchContactFragment.class)).thenReturn(presenterComponent);
//        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(SearchContactFragment.class))).thenReturn(fragmentComponent);
//
//        mCursor.addRow(new String[]{"1", "satou", "satousan", "", "11122223333", "1",});
//        mCursor.addRow(new String[]{"2", "suzuki", "suzukisan", "", "44455556666", "2",});
//        mCursor.addRow(new String[]{"3", "tanaka", "tanakasan", "", "77777777777", "0",});
//        mCursor.moveToFirst();
//    }
//
//    @Test
//    public void testLifeCycle() throws Exception {
//        mFragmentRule.launchActivity(null);
//        mFragmentRule.getActivity().finish();
//
//        Thread.sleep(200);
//
//        verify(mPresenter).setArguments(any(Bundle.class));
//        verify(mPresenter).setLoaderManager(any(LoaderManager.class));
//    }
//
//    @Test
//    public void testDial() throws Exception {
//        mFragmentRule.launchActivity(null);
//        final SearchContactFragment fragment = mFragmentRule.getFragment();
//        MatrixCursor mockCursor = new MatrixCursor(FROM);
//        mockCursor.addRow(new String[]{"1", "satou", "satousan", "", "11122223333", "1",});
//        mockCursor.moveToFirst();
//
//        doAnswer(invocationOnMock -> {
//            fragment.setChildrenCursor(0, mockCursor);
//            return null;
//        }).when(mPresenter).onSelectGroup(mCursor);
//        doAnswer(invocationOnMock -> {
//            fragment.dial("11122223333");
//            return null;
//        }).when(mPresenter).onNumberAction(mockCursor);
//
//        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
//        instr.runOnMainSync(() -> fragment.setGroupCursor(mCursor));
//
//        // 親項目表示確認
//        onView(withText("satou")).check(matches(isDisplayed()));
//        onView(withText("suzuki")).check(matches(isDisplayed()));
//        onView(withText("tanaka")).check(matches(isDisplayed()));
//        // 子項目表示確認
//        onData(anything())
//                .inAdapterView(withId(R.id.list_view))
//                .atPosition(0)
//                .perform(click());
//        onView(withText("11122223333")).check(matches(isDisplayed()));
//        // 発話準備確認
////        onView(withText("11122223333")).perform(click());
//        // 子項目非表示
//        onData(anything())
//                .inAdapterView(withId(R.id.list_view))
//                .atPosition(0)
//                .perform(click());
//        onView(withText("11122223333")).check(doesNotExist());
//    }
}