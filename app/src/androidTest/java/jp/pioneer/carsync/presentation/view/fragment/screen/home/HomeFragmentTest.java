package jp.pioneer.carsync.presentation.view.fragment.screen.home;

import android.Manifest;
import android.app.Instrumentation;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;

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
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.CarRunningStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.RadioBandType;
import jp.pioneer.carsync.domain.model.RadioInfo;
import jp.pioneer.carsync.domain.model.ShortcutKey;
import jp.pioneer.carsync.domain.model.SxmBandType;
import jp.pioneer.carsync.domain.model.SxmMediaInfo;
import jp.pioneer.carsync.domain.model.TunerFrequencyUnit;
import jp.pioneer.carsync.presentation.model.ShortcutKeyItem;
import jp.pioneer.carsync.presentation.presenter.HomePresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static jp.pioneer.carsync.presentation.view.EspressoTestsMatchers.withDrawable;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * HOMEの画面のテスト
 */
public class HomeFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<HomeFragment> mFragmentRule = new FragmentTestRule<HomeFragment>() {
        private HomeFragment mFragment;

        @Override
        protected HomeFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new HomeFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock HomePresenter mPresenter;

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        HomeFragmentTest.TestPresenterComponent presenterComponent(HomeFragmentTest.TestPresenterModule module);
    }

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CALL_LOG,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.SEND_SMS,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE,
    };

    private static final int[] GRANTS = new int[]{
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_DENIED,
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_GRANTED,
            PackageManager.PERMISSION_GRANTED,
    };
    private static final int REQUEST_CODE_ASK_FOR_PERMISSION = 100;
    private static final ShortcutKey[] KEY_INDEX = new ShortcutKey[]{
            ShortcutKey.SOURCE,
            ShortcutKey.VOICE,
            ShortcutKey.NAVI,
            ShortcutKey.MESSAGE,
            ShortcutKey.PHONE,
    };
    private static final int[][] KEY_IMAGES = new int[][]{
            {R.drawable.p0161_srcbtn_1nrm, 0},//Source
            {R.drawable.p0162_vrbtn_1nrm, 0},//Voice
            {R.drawable.p0163_navibtn_1nrm, 0},//Navi
            {R.drawable.p0164_messagebtn_1nrm, R.drawable.p0171_notification},//Message
            {R.drawable.p0165_phonebtn_1nrm, 0},//Phone
    };
    private ArrayList<ShortcutKeyItem> mShortCutKeyList = new ArrayList<>();
    private RadioInfo mTestRadio;
    private SxmMediaInfo mTestSxm;
    private CarRunningStatus mTestCarStatus;
    @PresenterLifeCycle
    @Subcomponent(modules = HomeFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public HomePresenter provideContactsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        HomeFragmentTest.TestAppComponent appComponent = DaggerHomeFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        HomeFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new HomeFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, HomeFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(HomeFragmentTest.TestPresenterComponent.class), any(HomeFragment.class))).thenReturn(fragmentComponent);

        for (int i = 0; i < KEY_INDEX.length; i++) {
            ShortcutKeyItem key = new ShortcutKeyItem(KEY_INDEX[i], KEY_IMAGES[i][0], KEY_IMAGES[i][1]);
            mShortCutKeyList.add(key);
        }
        mTestRadio = new RadioInfo();
        mTestRadio.band = RadioBandType.FM1;
        mTestRadio.psInfo = "TEST PS";
        mTestRadio.ptyInfo = "TEST PTY";
        mTestRadio.currentFrequency = 88888L;
        mTestRadio.frequencyUnit = TunerFrequencyUnit.MHZ2;
        mTestRadio.songTitle = "TEST SONG";
        mTestRadio.artistName = "TEST ARTIST";
        mTestRadio.antennaLevel = 8;
        mTestRadio.maxAntennaLevel = 10;

        mTestSxm = new SxmMediaInfo();
        mTestSxm.band = SxmBandType.SXM1;
        mTestSxm.currentFrequency = 10;
        mTestSxm.currentChannelNumber = 3;
        mTestSxm.categoryName = "TEST CATEGORY";
        mTestSxm.artistNameOrContentInfo = "TEST ARTIST";
        mTestSxm.channelAndChannelNameOrAdvisoryMessage = "TEST CHANNEL";
        mTestSxm.inReplayMode = false;
        mTestSxm.subscriptionUpdatingShowing = false;
        mTestSxm.totalBufferTime = 100;
        mTestSxm.currentPosition = 50;
        mTestSxm.antennaLevel = 8;
        mTestSxm.maxAntennaLevel = 10;
        mTestSxm.inTuneMix = false;

        mTestCarStatus = new CarRunningStatus();
        mTestCarStatus.speedForSpeedMeter = 100;
        mTestCarStatus.altitude = 50;
        mTestCarStatus.averageSpeed = 50;
        mTestCarStatus.bearing = 60;

        when(mPresenter.getCarRunningStatus()).thenReturn(mTestCarStatus);
    }

    /**
     * 新規インスタンス取得のテスト
     */
    @Test
    public void testNewInstance() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            Bundle args = new Bundle();
            HomeFragment m_fragment;
            m_fragment = fragment.newInstance(args);
            assertThat(m_fragment.getArguments(), Matchers.is(args));
        });
    }

    /**
     * パーミッションリクエストのテスト
     */
    //@Test
    public void testRequestPermissions() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.requestPermissions(PERMISSIONS);
        });
    }

    /**
     * パーミッションリクエスト応答のテスト
     */
    @Test
    public void testOnRequestPermissionsResult() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.onRequestPermissionsResult(REQUEST_CODE_ASK_FOR_PERMISSION, PERMISSIONS, GRANTS);
        });
    }

    /**
     * Sourceボタン押下のテスト
     */
    @Test
    public void testOnClickKeyButtonSource() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setClockView(0);
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setShortcutKeyItems(mShortCutKeyList);
        });

        onView(allOf(withId(R.id.shortcut_key0), isDisplayed())).perform(click());
        verify(mPresenter).onKeyAction(ShortcutKey.SOURCE);
    }

    /**
     * Voiceボタン押下のテスト
     */
    @Test
    public void testOnClickKeyButtonVoice() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setClockView(0);
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setShortcutKeyItems(mShortCutKeyList);
        });
        onView(allOf(withId(R.id.shortcut_key1), isDisplayed())).perform(click());
        verify(mPresenter).onKeyAction(ShortcutKey.VOICE);
    }

    /**
     * Naviボタン押下のテスト
     */
    @Test
    public void testOnClickKeyButtonNavi() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setClockView(0);
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setShortcutKeyItems(mShortCutKeyList);
        });
        onView(allOf(withId(R.id.shortcut_key2), isDisplayed())).perform(click());
        verify(mPresenter).onKeyAction(ShortcutKey.NAVI);
    }

    /**
     * Message ボタン押下のテスト
     */
    @Test
    public void testOnClickKeyButtonMessage() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setClockView(0);
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setShortcutKeyItems(mShortCutKeyList);
        });
        onView(allOf(withId(R.id.shortcut_key3), isDisplayed())).perform(click());
        verify(mPresenter).onKeyAction(ShortcutKey.MESSAGE);
    }

    /**
     * Phoneボタン押下のテスト
     */
    @Test
    public void testOnClickKeyButtonPhone() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setClockView(0);
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setShortcutKeyItems(mShortCutKeyList);
        });
        onView(allOf(withId(R.id.shortcut_key4), isDisplayed())).perform(click());
        verify(mPresenter).onKeyAction(ShortcutKey.PHONE);
    }

    /**
     * 設定ボタン押下のテスト
     */
    @Test
    public void testOnClickSettingButton() throws Exception {
        mFragmentRule.launchActivity(null);
        onView(withId(R.id.setting_button)).perform(click());
        verify(mPresenter).onSettingsAction();
    }

    /**
     * 楽曲タイトルの設定のテスト
     */
    @Test
    public void testSetMusicTitle() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setMusicTitle("songTitle");
        });
        onView(withId(R.id.music_title_text)).check(matches(withText("songTitle")));
    }

    /**
     * アルバムアートの設定のテスト
     */
    @Test
    public void testSetMusicAlbumArt() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setMusicAlbumArt(Uri.parse("content://jp.pioneer.carsync.provider/artwork/songs/111"));
        });
        Thread.sleep(1000);
        onView(withId(R.id.jacket_view)).check(matches(withDrawable(R.drawable.p0070_noimage)));
    }

    /**
     * プログレスバーの最大値の設定のテスト
     */
    @Test
    public void testSetMaxProgress() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setMaxProgress(300);
        });
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()));
    }

    /**
     * プログレスバー非表示のテスト
     */
    @Test
    public void testSetMaxProgressNotDisplayed() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setMaxProgress(0);
        });
        onView(withId(R.id.progressbar)).check(matches(not(isDisplayed())));
    }

    /**
     * プログレスバーの現在値の設定のテスト
     */
    @Test
    public void testSetCurrentProgress() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setCurrentProgress(20);
        });
        onView(withId(R.id.progressbar)).check(matches(isDisplayed()));
    }

    /**
     * ラジオ再生情報表示のテスト
     */
    @Test
    public void testDisplayRadioView() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.RADIO);
            fragment.setMusicTitle("FM1-PS INFO");
            fragment.setRadioInfo(new CarDeviceStatus(),mTestRadio);
            fragment.setPch(2);
        });
        Thread.sleep(500);
        onView(withId(R.id.music_title_text)).check(matches(withText("FM1-PS INFO")));
        onView(withId(R.id.band_text)).check(matches(withText("FM1")));
        onView(withId(R.id.frequency_text)).check(matches(withText("88")));
        onView(withId(R.id.frequency_decimal_text)).check(matches(withText(".89")));
        onView(withId(R.id.frequency_unit_text)).check(matches(withText("MHz")));
        onView(withId(R.id.pch_text)).check(matches(withText(("2"))));
    }

    /**
     * TI再生情報表示のテスト
     */
    @Test
    public void testDisplayTiView() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.TI);
            fragment.setMusicTitle(getTargetContext().getString(R.string.traffic_information));
            fragment.setRadioInfo(new CarDeviceStatus(),mTestRadio);
            fragment.setTiInfo();
        });
        Thread.sleep(500);
        onView(withId(R.id.music_title_text)).check(matches(withText(R.string.traffic_information)));
        onView(withId(R.id.band_text)).check(matches(not(isDisplayed())));
        onView(withId(R.id.frequency_text)).check(matches(withText("88")));
        onView(withId(R.id.frequency_decimal_text)).check(matches(withText(".89")));
        onView(withId(R.id.frequency_unit_text)).check(matches(withText("MHz")));
        onView(withId(R.id.pch_text)).check(matches(not(isDisplayed())));
    }

    /**
     * SXM再生情報表示のテスト
     */
    @Test
    public void testDisplaySxmView() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.SIRIUS_XM);
            fragment.setMusicTitle("SXM1-TEST CHANNEL");
            fragment.setSxmInfo(new CarDeviceStatus(),mTestSxm);
            fragment.setPch(2);
        });
        Thread.sleep(500);
        onView(withId(R.id.music_title_text)).check(matches(withText("SXM1-TEST CHANNEL")));
        onView(withId(R.id.band_text)).check(matches(withText("SXM1")));
        onView(withId(R.id.channel_number_text)).check(matches(withText("CH 003")));
        onView(withId(R.id.pch_text)).check(matches(withText(("2"))));
    }

    /**
     * 通知有無アイコン表示の設定のテスト　通知ありの場合
     */
    @Test
    public void testSetMessageNotificationDisplayed() throws Exception {
        mFragmentRule.launchActivity(null);
        final HomeFragment fragment = mFragmentRule.getFragment();

        ShortcutKeyItem item = mShortCutKeyList.get(3);
        item.optionImageResource = R.drawable.p0171_notification;
        mShortCutKeyList.set(3, item);

        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setClockView(0);
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setShortcutKeyItems(mShortCutKeyList);
        });

        onView(allOf(withId(R.id.icon), withParent(withParent(allOf(withId(R.id.shortcut_key3), isDisplayed()))))).check(matches(isDisplayed()));
        onView(allOf(withId(R.id.icon_back), withParent(withParent(allOf(withId(R.id.shortcut_key3), isDisplayed()))))).check(matches(isDisplayed()));
    }

    /**
     * 通知有無アイコン表示の設定のテスト　通知なしの場合
     */
    @Test
    public void testSetMessageNotificationNotDisplayed() throws Exception {
        mFragmentRule.launchActivity(null);

        ShortcutKeyItem item = mShortCutKeyList.get(3);
        item.optionImageResource = 0;
        mShortCutKeyList.set(3, item);

        final HomeFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setPlayerView(MediaSourceType.APP_MUSIC);
            fragment.setClockView(0);
            fragment.setColor(R.color.ui_color_aqua);
            fragment.setShortcutKeyItems(mShortCutKeyList);
        });

        onView(withId(R.id.viewPager)).perform(swipeLeft());
        onView(allOf(withId(R.id.icon), withParent(withParent(allOf(withId(R.id.shortcut_key0), isDisplayed()))))).check(matches(not(isDisplayed())));
        onView(allOf(withId(R.id.icon_back), withParent(withParent(allOf(withId(R.id.shortcut_key0), isDisplayed()))))).check(matches(not(isDisplayed())));
    }
}
