package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.app.Instrumentation;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.view.View;

import org.hamcrest.Matcher;
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
import jp.pioneer.carsync.domain.model.BtAudioInfo;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.presentation.presenter.BtAudioPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BtAudio再生の画面のテスト
 */
public class BtAudioFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<BtAudioFragment> mFragmentRule = new FragmentTestRule<BtAudioFragment>() {
        private BtAudioFragment mFragment;

        @Override
        protected BtAudioFragment createDialogFragment() {
            // 非UIスレッドから呼ばれるが、GestureDetectorがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new BtAudioFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock BtAudioPresenter mPresenter;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })

    public interface TestAppComponent extends AppComponent {
        BtAudioFragmentTest.TestPresenterComponent presenterComponent(BtAudioFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = BtAudioFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public BtAudioPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        BtAudioFragmentTest.TestAppComponent appComponent = DaggerBtAudioFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        BtAudioFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new BtAudioFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, BtAudioFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(BtAudioFragmentTest.TestPresenterComponent.class), any(BtAudioFragment.class))).thenReturn(fragmentComponent);

    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            BtAudioFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), is(args));
        });
    }

    /**
     * 左フリックのテスト
     */
    @Test
    public void testOnFlickLeft() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        onView(withId(R.id.jacket_view)).perform(swipeLeft());
        verify(mPresenter).onSkipNextAction();
    }

    /**
     * 右フリックのテスト
     */
    @Test
    public void testOnFlickRight() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        onView(withId(R.id.jacket_view)).perform(swipeRight());
        verify(mPresenter).onSkipPreviousAction();
    }

    /**
     * 中央エリアタッチのテスト
     */
    @Test
    public void testOnTouchJacketView() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        BtAudioInfo info = new BtAudioInfo();
        info.playbackMode = PlaybackMode.PLAY;
        when(mPresenter.getBtAudioInfo()).thenReturn(info);
//        onView(withId(R.id.jacket_view)).perform(click());
        onView(withId(R.id.jacket_view)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled();
            }

            @Override
            public String getDescription() {
                return "click Jacket button";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        });
        verify(mPresenter).onPlayPauseAction();
    }

    /**
     * 中央エリアスワイプのテスト
     */
    @Test
    public void testOnTouchJacketView2() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.jacket_view)).perform(swipeDown());
        verify(mPresenter, times(0)).onPlayPauseAction();
    }

    /**
     * ソース選択ボタン押下のテスト
     */
    @Test
    public void testOnClickLeft() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        onView(withId(R.id.source_button)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled(); // no constraints, they are checked above
            }

            @Override
            public String getDescription() {
                return "click Left button";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        });
        verify(mPresenter).onSelectSourceAction();
    }

    /**
     * デバイス選択ボタン押下のテスト
     */
/*    @Test
    public void testOnClickRight() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        onView(withId(R.id.list_button_icon)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isEnabled(); // no constraints, they are checked above
            }

            @Override
            public String getDescription() {
                return "click Right button";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.performClick();
            }
        });
        verify(mPresenter).onSelectDeviceAction();
    }*/

    /**
     * 視覚効果ボタン押下のテスト
     */
    @Test
    public void testOnClickLeftCenter() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        onView(withId(R.id.visualizer_button)).perform(click());
        verify(mPresenter).onSelectVisualAction();
    }

    /**
     * 音響効果ボタン押下のテスト
     */
    @Test
    public void testOnClickRightCenter() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        onView(withId(R.id.fx_button)).perform(click());
        verify(mPresenter).onSelectFxAction();
    }

    /**
     * 設定ボタン押下のテスト
     */
    @Test
    public void testOnClickHome() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
        });
        onView(withId(R.id.home_button)).perform(click());
        verify(mPresenter).onHomeAction();
    }

    /**
     * 楽曲タイトルの設定のテスト
     */
    @Test
    public void testSetMusicTitle() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setMusicTitle("songTitle");
        });
        onView(withId(R.id.music_title_text)).check(matches(withText("songTitle")));
    }

    /**
     * アーティストの設定のテスト
     */
    @Test
    public void testSetMusicArtist() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setMusicArtist("artistName");
        });
        onView(withId(R.id.music_artist_text)).check(matches(withText("artistName")));
    }

    /**
     * アルバム名の設定のテスト
     */
    @Test
    public void testSetMusicAlbum() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setMusicAlbum("albumTitle");
        });
        onView(withId(R.id.music_album_text)).check(matches(withText("albumTitle")));
    }

    /**
     * 接続デバイス名の設定のテスト
     */
    @Test
    public void testSetAudioDeviceName() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setAudioDeviceName("deviceName");
        });
        onView(withId(R.id.device_name)).check(matches(withText("deviceName")));
    }

    /**
     * プログレスバーの最大値の設定のテスト
     */
    @Test
    public void testSetMaxProgress() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setMaxProgress(300);
        });
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()));
    }

    /**
     * プログレスバーの現在値の設定のテスト
     */
    @Test
    public void testSetCurrentProgress() throws Exception {
        mFragmentRule.launchActivity(null);
        final BtAudioFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setCurrentProgress(20);
        });
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()));
    }

}