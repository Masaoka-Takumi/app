package jp.pioneer.carsync.presentation.view.fragment.screen.settings;

import android.app.Instrumentation;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.assertion.ViewAssertions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v7.preference.Preference;
import android.widget.Button;


import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
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
import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.application.di.component.FragmentComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.FragmentModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.presentation.presenter.DirectCallSettingPresenter;
import jp.pioneer.carsync.presentation.view.fragment.preference.ImpactDetectionSettingsFragment;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static android.support.test.InstrumentationRegistry.getTargetContext;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.junit.Assert.*;

/**
 * DirectCallSettingFragmentのテスト
 */
public class DirectCallSettingFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<DirectCallSettingFragment> mFragmentRule = new FragmentTestRule<DirectCallSettingFragment>() {
        private DirectCallSettingFragment mFragment;

        @Override
        protected DirectCallSettingFragment createDialogFragment() {
            return DirectCallSettingFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock DirectCallSettingPresenter mPresenter;
    @Mock AppSharedPreference mPreference;

    private static final String[] FROM = { ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY,ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.Contacts.PHOTO_URI, ContactsContract.CommonDataKinds.Phone.TYPE};
    private MatrixCursor mCursor = new MatrixCursor(FROM);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        TestPresenterComponent presenterComponent(TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }
    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public DirectCallSettingPresenter provideDirectCallSettingPresenter() {
            return mPresenter;
        }
    }
    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerDirectCallSettingFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, DirectCallSettingFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(DirectCallSettingFragment.class))).thenReturn(fragmentComponent);
        mCursor.addRow(new String[] { "1", "satou", "1","11122223333" ,"","1"});
        mCursor.moveToFirst();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testSetContactItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final DirectCallSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setContactItem(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)), Uri.parse(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))));
        });
        onView(withText("satou")).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.contact_icon)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

    }

    @Test
    public void testSetPhoneItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final DirectCallSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPhoneItem(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)), mCursor.getInt(mCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)));
        });
        onView(withText("11122223333")).check(matches(isDisplayed()));
        onView(ViewMatchers.withId(R.id.number_type)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testSetDisable() throws Exception {
        mFragmentRule.launchActivity(null);
        final DirectCallSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setDisable();
        });
        onView(ViewMatchers.withId(R.id.name_text)).check(ViewAssertions.matches(ViewMatchers.withText("")));
        onView(ViewMatchers.withId(R.id.contact_icon)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(ViewMatchers.withId(R.id.number_type)).check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
        onView(ViewMatchers.withId(R.id.number_text)).check(ViewAssertions.matches(ViewMatchers.withText("")));
        onView(ViewMatchers.withId(R.id.delete_button)).check(ViewAssertions.matches(Matchers.not(ViewMatchers.isEnabled())));

    }

    @Test
    public void testOnClickDelete() throws Exception {
        mFragmentRule.launchActivity(null);
        final DirectCallSettingFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setContactItem(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)), Uri.parse(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))));
            fragment.setPhoneItem(mCursor.getString(mCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)), mCursor.getInt(mCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)));
        });
        onView(ViewMatchers.withId(R.id.delete_button)).perform(click());
        verify(mPresenter).onDeleteAction();
    }

    @Test
    public void testOnClickRegister() throws Exception {
        mFragmentRule.launchActivity(null);
        final DirectCallSettingFragment fragment = mFragmentRule.getFragment();
        onView(ViewMatchers.withId(R.id.register_button)).perform(click());
        verify(mPresenter).onRegisterAction();
    }
}