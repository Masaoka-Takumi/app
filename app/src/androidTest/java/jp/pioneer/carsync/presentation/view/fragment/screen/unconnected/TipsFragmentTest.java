package jp.pioneer.carsync.presentation.view.fragment.screen.unconnected;

import android.app.Instrumentation;
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
import jp.pioneer.carsync.presentation.model.TipsItem;
import jp.pioneer.carsync.presentation.model.TipsTag;
import jp.pioneer.carsync.presentation.presenter.TipsPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * TipsFragmentTest
 */
public class TipsFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<TipsFragment> mFragmentRule = new FragmentTestRule<TipsFragment>() {
        @Override
        protected TipsFragment createDialogFragment() {
            return TipsFragment.newInstance(Bundle.EMPTY);
        }

    };
    @Mock ComponentFactory mComponentFactory;
    @Mock TipsPresenter mPresenter;

    private TipsItem[] mItems;
    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        TipsFragmentTest.TestPresenterComponent presenterComponent(TipsFragmentTest.TestPresenterModule module);
    }
    @PresenterLifeCycle
    @Subcomponent(modules = TipsFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public TipsPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        TipsFragmentTest.TestAppComponent appComponent = DaggerTipsFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TipsFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TipsFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, TipsFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(TipsFragmentTest.TestPresenterComponent.class), any(TipsFragment.class))).thenReturn(fragmentComponent);
        mItems= new TipsItem[3];
        TipsTag[] tag1 = new TipsTag[1];
        tag1[0] = new TipsTag(1,"manual","manual","");
        mItems[0] = new TipsItem(1,"https://www.google.co.jp/","Title Manual","Sample Text",null, tag1,"Sample Text");
        TipsTag[] tag2 = new TipsTag[1];
        tag2[0] =new TipsTag(1,"tips","tips","");
        mItems[1] = new TipsItem(2,"https://www.google.co.jp/","Title Tips","Sample Text",null, tag2,"Sample Text");
        TipsTag[] tag3 = new TipsTag[1];
        tag3[0] = new TipsTag(1,"information","information","");
        mItems[2] = new TipsItem(3,"https://www.google.co.jp/","Title Information","Sample Text",null, tag3,"Sample Text");

    }

    @Test
    public void testDisplay() throws Exception {
        mFragmentRule.launchActivity(null);
        final TipsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mItems);
        });

        onView(withText("Title Manual")).check(matches(isDisplayed()));
        onView(withText("Title Tips")).check(matches(isDisplayed()));
        onView(withId(R.id.list_view)).perform(swipeUp());
        onView(withText("Title Information")).check(matches(isDisplayed()));
    }

    @Test
    public void onClickListItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final TipsFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setAdapter(mItems);
        });

        onView(withText("Title Manual")).perform(click());
        verify(mPresenter).showTips(0);

    }

    @Test
    public void onClickSettingButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.set_button)).perform(click());
        verify(mPresenter).onSettingAction();
    }

    @Test
    public void onClickBtButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.bt_button)).perform(click());
        verify(mPresenter).onBtAction();
    }

}