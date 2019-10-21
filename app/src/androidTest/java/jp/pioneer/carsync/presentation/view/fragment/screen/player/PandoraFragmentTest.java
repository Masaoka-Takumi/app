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
import jp.pioneer.carsync.domain.model.PandoraMediaInfo;
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.domain.model.ThumbStatus;
import jp.pioneer.carsync.presentation.presenter.PandoraPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeDown;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.presentation.view.EspressoTestsMatchers.withDrawable;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pandora再生の画面のテスト
 */
public class PandoraFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<PandoraFragment> mFragmentRule = new FragmentTestRule<PandoraFragment>() {
        private PandoraFragment mFragment;

        @Override
        protected PandoraFragment createDialogFragment() {
            // 非UIスレッドから呼ばれるが、GestureDetectorがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new PandoraFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock PandoraPresenter mPresenter;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })

    public interface TestAppComponent extends AppComponent {
        PandoraFragmentTest.TestPresenterComponent presenterComponent(PandoraFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = PandoraFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public PandoraPresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        PandoraFragmentTest.TestAppComponent appComponent = DaggerPandoraFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        PandoraFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new PandoraFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, PandoraFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(PandoraFragmentTest.TestPresenterComponent.class), any(PandoraFragment.class))).thenReturn(fragmentComponent);
    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            PandoraFragment m_fragment;
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
     * 中央エリアタッチのテスト
     */
    @Test
    public void testOnTouchJacketView() throws Exception {
        mFragmentRule.launchActivity(null);
        PandoraMediaInfo info = new PandoraMediaInfo();
        info.playbackMode = PlaybackMode.PLAY;
        when(mPresenter.getPandoraMediaInfo()).thenReturn(info);
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
     * 設定ボタン押下のテスト
     */
    @Test
    public void testOnClickHome() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.home_button)).perform(click());
        verify(mPresenter).onHomeAction();
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
                return isEnabled();
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
     * ThumbUpボタン押下のテスト
     */
    @Test
    public void testOnClickThumbUp() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.thumb_up_button)).perform(click());
        verify(mPresenter).onThumbUpAction();
    }

    /**
     * ThumbDownボタン押下のテスト
     */
    @Test
    public void testOnClickThumbDown() throws Exception {
        mFragmentRule.launchActivity(null);

        onView(withId(R.id.thumb_down_button)).perform(click());
        verify(mPresenter).onThumbDownAction();
    }

    /**
     * 楽曲タイトルの設定のテスト
     */
    @Test
    public void testSetMusicTitle() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setMusicTitle("songTitle"));
        onView(withId(R.id.music_title_text)).check(matches(withText("songTitle")));
    }

    /**
     * アーティストの設定のテスト
     */
    @Test
    public void testSetMusicArtist() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setMusicArtist("artistName"));
        onView(withId(R.id.music_artist_text)).check(matches(withText("artistName")));
    }

    /**
     * アルバム名の設定のテスト
     */
    @Test
    public void testSetMusicAlbum() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setMusicAlbum("albumTitle"));
        onView(withId(R.id.music_album_text)).check(matches(withText("albumTitle")));
    }

    /**
     * 再生元の設定のテスト
     */
    @Test
    public void testSetPlayingTrackSource() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setStationName("stationName"));
        onView(withId(R.id.music_info_text)).check(matches(withText("stationName")));
    }

    /**
     * プログレスバーの最大値の設定のテスト
     */
    @Test
    public void testSetMaxProgress() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setMaxProgress(300));
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()));
    }

    /**
     * プログレスバーの現在値の設定のテスト
     */
    @Test
    public void testSetCurrentProgress() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setCurrentProgress(20));
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()));
    }

    /**
     * Thumbアイコンの設定-どちらもOFFの場合のテスト
     */
    @Test
    public void testSetThumbStatusNone() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setThumbStatus(ThumbStatus.NONE));
        onView(withId(R.id.thumb_up_button)).check(matches(withDrawable(R.drawable.p0317_iconbtn_1nrm)));
        onView(withId(R.id.thumb_down_button)).check(matches(withDrawable(R.drawable.p0318_iconbtn_1nrm)));
    }

    /**
     * Thumbアイコンの設定-UPの場合のテスト
     */
    @Test
    public void testSetThumbStatusUp() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setThumbStatus(ThumbStatus.UP));
        onView(withId(R.id.thumb_up_button)).perform(click());
        onView(withId(R.id.thumb_up_button)).check(matches(withDrawable(R.drawable.p0317_iconbtn_2slc)));
        onView(withId(R.id.thumb_down_button)).check(matches(withDrawable(R.drawable.p0318_iconbtn_1nrm)));
    }

    /**
     * Thumbアイコンの設定-DOWNの場合のテスト
     */
    @Test
    public void testSetThumbStatusDown() throws Exception {
        mFragmentRule.launchActivity(null);
        final PandoraFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> fragment.setThumbStatus(ThumbStatus.DOWN));
        onView(withId(R.id.thumb_down_button)).perform(click());
        onView(withId(R.id.thumb_up_button)).check(matches(withDrawable(R.drawable.p0317_iconbtn_1nrm)));
        onView(withId(R.id.thumb_down_button)).check(matches(withDrawable(R.drawable.p0318_iconbtn_2slc)));
    }
}