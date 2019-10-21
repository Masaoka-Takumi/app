package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Instrumentation;
import android.database.MatrixCursor;
import android.os.Bundle;
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
import jp.pioneer.carsync.application.content.ProviderContract;
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
import jp.pioneer.carsync.presentation.presenter.RadioFavoritePresenter;
import jp.pioneer.carsync.presentation.view.fragment.ScreenId;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ラジオお気に入り画面のテストコード
 */
public class RadioFavoriteFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<RadioFavoriteFragment> mFragmentRule = new FragmentTestRule<RadioFavoriteFragment>() {
        @Override
        protected RadioFavoriteFragment createDialogFragment() {
            return RadioFavoriteFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock RadioFavoritePresenter mPresenter;

    private static final String[] FROM = {
            ProviderContract.Favorite._ID,
            ProviderContract.Favorite.TUNER_BAND,
            ProviderContract.Favorite.DESCRIPTION,
            ProviderContract.Favorite.NAME,
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
        public RadioFavoritePresenter provideRadioFavoritePresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TestAppComponent appComponent = DaggerRadioFavoriteFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, RadioFavoriteFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TestPresenterComponent.class), any(RadioFavoriteFragment.class))).thenReturn(fragmentComponent);

        mCursor.addRow(new String[]{"1", "1", "FM1 80.0kHz", "Station1"});
        mCursor.addRow(new String[]{"2", "1", "FM1 79.5kHz", "Station2"});
        mCursor.moveToFirst();
    }

    @Test
    public void testLifecycle() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioFavoriteFragment fragment = mFragmentRule.getFragment();

        verify(mPresenter).setLoaderManager(any(LoaderManager.class));
        assertThat(fragment.getScreenId(), is(ScreenId.RADIO_FAVORITE_LIST));
    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioFavoriteFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setCursor(mCursor, Bundle.EMPTY));

        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
                .onChildView(withText("FM1")).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
                .onChildView(withText("80.0kHz")).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(0)
                .onChildView(withText("Station1")).check(matches(isDisplayed()));

        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(1)
                .onChildView(withText("FM1")).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(1)
                .onChildView(withText("79.5kHz")).check(matches(isDisplayed()));
        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(1)
                .onChildView(withText("Station2")).check(matches(isDisplayed()));
    }

    @Test
    public void testOnClickListItem() throws Exception {
        mFragmentRule.launchActivity(null);
        RadioFavoriteFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setCursor(mCursor, Bundle.EMPTY));

        onData(anything()).inAdapterView(withId(R.id.list_view)).atPosition(1).perform(click());

        mCursor.moveToPosition(1);
        verify(mPresenter).onSelectFavoriteAction(mCursor);
    }
}