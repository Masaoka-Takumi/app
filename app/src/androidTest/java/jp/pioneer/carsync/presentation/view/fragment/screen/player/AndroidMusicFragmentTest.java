package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.app.Instrumentation;
import android.net.Uri;
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
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ShuffleMode;
import jp.pioneer.carsync.domain.model.SmartPhoneRepeatMode;
import jp.pioneer.carsync.domain.model.SmartPhoneStatus;
import jp.pioneer.carsync.presentation.presenter.AndroidMusicPresenter;

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
import static jp.pioneer.carsync.presentation.view.EspressoTestsMatchers.withDrawable;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Androidローカルコンテンツ再生の画面のテスト
 */
public class AndroidMusicFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<AndroidMusicFragment> mFragmentRule = new FragmentTestRule<AndroidMusicFragment>() {
        private AndroidMusicFragment mFragment;

        @Override
        protected AndroidMusicFragment createDialogFragment() {
            // 非UIスレッドから呼ばれるが、GestureDetectorがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new AndroidMusicFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock AndroidMusicPresenter mPresenter;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })

    public interface TestAppComponent extends AppComponent {
        AndroidMusicFragmentTest.TestPresenterComponent presenterComponent(AndroidMusicFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = AndroidMusicFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public AndroidMusicPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        AndroidMusicFragmentTest.TestAppComponent appComponent = DaggerAndroidMusicFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        AndroidMusicFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new AndroidMusicFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, AndroidMusicFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(AndroidMusicFragmentTest.TestPresenterComponent.class), any(AndroidMusicFragment.class))).thenReturn(fragmentComponent);
    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            AndroidMusicFragment m_fragment;
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

        onView(withId(R.id.jacket_view)).perform(swipeLeft());
        verify(mPresenter).onSkipNextAction();
    }

    /**
     * 右フリックのテスト
     */
    @Test
    public void testOnFlickRight() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.jacket_view)).perform(swipeRight());
        verify(mPresenter).onSkipPreviousAction();
    }

    /**
     * ジャケット画像タッチのテスト
     */
    @Test
    public void testOnTouchJacketView() throws Exception {
        mFragmentRule.launchActivity(null);
        SmartPhoneStatus status = new SmartPhoneStatus();
        status.playbackMode = PlaybackMode.PLAY;
        when(mPresenter.getSmartPhoneStatus()).thenReturn(status);
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
     * ジャケット画像スワイプのテスト
     */
    @Test
    public void testOnTouchJacketView2() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.jacket_view)).perform(swipeDown());
        verify(mPresenter, times(0)).onPlayPauseAction();
    }

    /**
     * NowPlayingListボタン押下のテスト
     */
    @Test
    public void testnClickNowPlaying() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.now_playing_button)).perform(click());
        verify(mPresenter).onNowPlayingAction();
    }

    /**
     * リピートボタン押下のテスト
     */
    @Test
    public void testOnClickRepeat() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.repeat_button)).perform(click());
        verify(mPresenter).onRepeatAction();
    }

    /**
     * シャッフルボタン押下のテスト
     */
    @Test
    public void testOnClickShuffle() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.shuffle_button)).perform(click());
        verify(mPresenter).onShuffleAction();
    }

    /**
     * リストボタン押下のテスト
     */
    @Test
    public void testOnClickRight() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.list_button)).perform(new ViewAction() {
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
        verify(mPresenter).onSelectListAction();
    }

    /**
     * ソース選択ボタン押下のテスト
     */
    @Test
    public void testOnClickLeft() throws Exception {
        mFragmentRule.launchActivity(null);

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
     * 視覚効果ボタン押下のテスト
     */
    @Test
    public void testOnClickLeftCenter() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.visualizer_button)).perform(click());
        verify(mPresenter).onSelectVisualAction();
    }

    /**
     * 音響効果ボタン押下のテスト
     */
    @Test
    public void testOnClickRightCenter() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.fx_button)).perform(click());
        verify(mPresenter).onSelectFxAction();
    }

    /**
     * 楽曲タイトルの設定のテスト
     */
    @Test
    public void testSetMusicTitle() throws Exception {
        mFragmentRule.launchActivity(null);
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
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
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
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
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setMusicAlbum("albumTitle");
        });
        onView(withId(R.id.music_album_text)).check(matches(withText("albumTitle")));
    }

    /**
     * ジャンルの設定のテスト
     */
    @Test
    public void testSetMusicGenre() throws Exception {
        mFragmentRule.launchActivity(null);
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setMusicGenre("genre");
        });
        onView(withId(R.id.music_info_text)).check(matches(withText("genre")));
    }

    /**
     * アルバムアートの設定のテスト
     */
    @Test
    public void testSetMusicAlbumArt() throws Exception {
        mFragmentRule.launchActivity(null);
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setMusicAlbumArt(Uri.parse("content://jp.pioneer.carsync.provider/artwork/songs/111"));
        });
        Thread.sleep(100);
        onView(withId(R.id.jacket_view)).check(matches(withDrawable(R.drawable.p0070_noimage)));
    }

    /**
     * プログレスバーの最大値の設定のテスト
     */
    @Test
    public void testSetMaxProgress() throws Exception {
        mFragmentRule.launchActivity(null);
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
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
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setCurrentProgress(20);
        });
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()));
    }

    /**
     * リピートアイコンの設定のテスト
     */
    @Test
    public void testSetRepeatImage() throws Exception {
        mFragmentRule.launchActivity(null);
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setRepeatImage(SmartPhoneRepeatMode.OFF));
        onView(withId(R.id.repeat_button)).check(matches(withDrawable(R.drawable.p0043_trickplaybtn_1nrm)));
    }

    /**
     * シャッフルアイコンの設定のテスト
     */
    @Test
    public void testSetShuffleImage() throws Exception {
        mFragmentRule.launchActivity(null);
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setShuffleImage(ShuffleMode.OFF));
        onView(withId(R.id.shuffle_button)).check(matches(withDrawable(R.drawable.p0042_trickplaybtn_1nrm)));
    }

    /**
     * Eq、Fxボタンの設定のテスト
     */
    @Test
    public void testEqFxButtonText() throws Exception {
        mFragmentRule.launchActivity(null);
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setEqFxButtonEnabled(true, true);
            fragment.setEqButton(getTargetContext().getResources().getString(R.string.eq_type_super_bass));
            fragment.setFxButton("Todoroki LOW");
        });
        Thread.sleep(300);
        onView(withId(R.id.visualizer_button)).check(matches(isEnabled()));
        onView(withId(R.id.fx_button)).check(matches(isEnabled()));
        onView(withId(R.id.vis_text)).check(matches(withText(R.string.eq_type_super_bass)));
        onView(withId(R.id.fx_text)).check(matches(withText(getTargetContext().getResources().getString(R.string.setting_fx_super_todoroki_sound_todoroki) + " " + getTargetContext().getResources().getString(R.string.todoroki_low))));
    }

    /**
     * Eq、Fxボタンの設定のテスト
     */
    @Test
    public void testEqFxButtonTextDisable() throws Exception {
        mFragmentRule.launchActivity(null);
        final AndroidMusicFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setEqFxButtonEnabled(false, false);
            fragment.setEqButton("");
            fragment.setFxButton("");
        });
        Thread.sleep(300);
        onView(withId(R.id.visualizer_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.fx_button)).check(matches(not(isEnabled())));
        onView(withId(R.id.vis_text)).check(matches(withText("")));
        onView(withId(R.id.fx_text)).check(matches(withText("")));
    }
}