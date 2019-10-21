package jp.pioneer.carsync.presentation.view.fragment.screen.contacts;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.v4.app.FragmentManager;

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
import jp.pioneer.carsync.presentation.controller.ContactsFragmentController;
import jp.pioneer.carsync.presentation.presenter.ContactsContainerPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 電話帳 コンテナの画面のテスト
 */
public class ContactsContainerFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<ContactsContainerFragment> mFragmentRule = new FragmentTestRule<ContactsContainerFragment>() {
        @Override
        protected ContactsContainerFragment createDialogFragment() {
            return ContactsContainerFragment.newInstance(Bundle.EMPTY);
        }
    };

    @Mock ComponentFactory mComponentFactory;
    @Mock FragmentManager mFragmentManager;
    @Mock ContactsContainerPresenter mPresenter;
    @Mock ContactsFragmentController mFragmentController;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        ContactsContainerFragmentTest.TestPresenterComponent presenterComponent(ContactsContainerFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = ContactsContainerFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public ContactsContainerPresenter provideContactsContainerPresenter() {
            return mPresenter;
        }
    }

    public class TestFragmentModule extends FragmentModule {
        public TestFragmentModule() {
        }

        @Override
        public FragmentManager provideChildFragmentManager() {
            return mFragmentManager;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        ContactsContainerFragmentTest.TestAppComponent appComponent = DaggerContactsContainerFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        ContactsContainerFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new TestFragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ContactsContainerFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(ContactsContainerFragmentTest.TestPresenterComponent.class), any(ContactsContainerFragment.class))).thenReturn(fragmentComponent);

    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);

        final ContactsContainerFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            ContactsContainerFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }

    /**
     * 現在のタブ設定-Historyのテスト
     */
    @Test
    public void testSetCurrentTabHistory() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsContainerFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setCurrentTab(ContactsContainerPresenter.ContactsTab.HISTORY));
        onView(withId(R.id.directory_pass_text)).check(matches(withText(R.string.contacts_history)));
    }

    /**
     * 現在のタブ設定-Contactsのテスト
     */
    @Test
    public void testSetCurrentTabContacts() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsContainerFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setCurrentTab(ContactsContainerPresenter.ContactsTab.CONTACTS));
        onView(withId(R.id.directory_pass_text)).check(matches(withText(R.string.contacts_list)));
    }

    /**
     * 現在のタブ設定-Favoritesのテスト
     */
    @Test
    public void testSetCurrentTabFavorites() throws Exception {
        mFragmentRule.launchActivity(null);
        final ContactsContainerFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setCurrentTab(ContactsContainerPresenter.ContactsTab.FAVORITE));
        onView(withId(R.id.directory_pass_text)).check(matches(withText(R.string.contacts_favorites)));
    }

    /**
     * タブ押下-Historyのテスト
     */
    @Test
    public void testOnClickHistoryTab() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.tab_history)).perform(click());
        verify(mPresenter).onTabAction(ContactsContainerPresenter.ContactsTab.HISTORY);

    }

    /**
     * タブ押下-Contactsのテスト
     */
    @Test
    public void testOnClickContactsTab() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.tab_contacts)).perform(click());
        verify(mPresenter).onTabAction(ContactsContainerPresenter.ContactsTab.CONTACTS);
    }

    /**
     * タブ押下-Favoritesのテスト
     */
    @Test
    public void testOnClickFavoritesTab() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.tab_favorites)).perform(click());
        verify(mPresenter).onTabAction(ContactsContainerPresenter.ContactsTab.FAVORITE);
    }

    /**
     * 戻るボタン押下のテスト
     */
    @Test
    public void testOnClickBackButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.back_button)).perform(click());
        verify(mPresenter).onBackAction();
    }

}