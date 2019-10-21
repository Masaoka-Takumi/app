package jp.pioneer.carsync.presentation.view.fragment.screen.player;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.FragmentTestRule;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.content.AppSharedPreference;
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
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.MessagingApp;
import jp.pioneer.carsync.domain.model.MusicApp;
import jp.pioneer.carsync.domain.model.NaviApp;
import jp.pioneer.carsync.presentation.model.SourceSelectItem;
import jp.pioneer.carsync.presentation.model.UiColor;
import jp.pioneer.carsync.presentation.presenter.SourceSelectPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.DialogFragmentTestRule.getTestApp;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ソース選択の画面のテスト
 */
public class SourceSelectFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<SourceSelectFragment> mFragmentRule = new FragmentTestRule<SourceSelectFragment>() {
        private SourceSelectFragment mFragment;

        @Override
        protected SourceSelectFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new SourceSelectFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock SourceSelectPresenter mPresenter;

    private List<SourceSelectItem> mSourceSelectList = new ArrayList<>();
    private Map<MediaSourceType, SourceSelectItem> mAllSourceList = new HashMap<MediaSourceType, SourceSelectItem>() {{
        put(MediaSourceType.RADIO, new SourceSelectItem(MediaSourceType.RADIO, R.string.source_select_radio, R.drawable.p0083_radio));
        put(MediaSourceType.DAB, new SourceSelectItem(MediaSourceType.DAB, R.string.source_select_dab, R.drawable.p0083_radio));
        put(MediaSourceType.SIRIUS_XM, new SourceSelectItem(MediaSourceType.SIRIUS_XM, R.string.source_select_sirius_xm, R.drawable.p0089_sxm));
        put(MediaSourceType.HD_RADIO, new SourceSelectItem(MediaSourceType.HD_RADIO, R.string.source_select_hd_radio, R.drawable.p0083_radio));
        put(MediaSourceType.CD, new SourceSelectItem(MediaSourceType.CD, R.string.source_select_cd, R.drawable.p0084_cd));
        put(MediaSourceType.USB, new SourceSelectItem(MediaSourceType.USB, R.string.source_select_usb, R.drawable.p0085_usb));
        put(MediaSourceType.AUX, new SourceSelectItem(MediaSourceType.AUX, R.string.source_select_aux, R.drawable.p0086_aux));
        put(MediaSourceType.BT_AUDIO, new SourceSelectItem(MediaSourceType.BT_AUDIO, R.string.source_select_bt_audio, R.drawable.p0082_bta));
        put(MediaSourceType.BT_PHONE, new SourceSelectItem(MediaSourceType.BT_PHONE, R.string.source_select_bt_phone, R.drawable.p0082_bta));
        put(MediaSourceType.OFF, new SourceSelectItem(MediaSourceType.OFF, R.string.source_select_off, R.drawable.p0090_off));
        put(MediaSourceType.PANDORA, new SourceSelectItem(MediaSourceType.PANDORA, R.string.source_select_pandora, R.drawable.p0087_pandora));
        put(MediaSourceType.SPOTIFY, new SourceSelectItem(MediaSourceType.SPOTIFY, R.string.source_select_spotify, R.drawable.p0088_spotify));
        put(MediaSourceType.APP_MUSIC, new SourceSelectItem(MediaSourceType.APP_MUSIC, R.string.source_select_app_music, R.drawable.p0081_music));
        put(MediaSourceType.IPOD, new SourceSelectItem(MediaSourceType.IPOD, R.string.source_select_ipod, R.drawable.p0081_music));
        put(MediaSourceType.TI, new SourceSelectItem(MediaSourceType.TI, R.string.source_select_ti, R.drawable.p0081_music));
        //TODO:アイコン画像差し替え
    }};

    private Set<MediaSourceType> mTypeSet = new HashSet<>();
    private AppSharedPreference.Application[] mMusicAppList;
    private AppSharedPreference.Application[] mMessageAppList;
    private AppSharedPreference.Application mNaviApp;


    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })
    public interface TestAppComponent extends AppComponent {
        SourceSelectFragmentTest.TestPresenterComponent presenterComponent(SourceSelectFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = SourceSelectFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public SourceSelectPresenter provideSourceSelectPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        SourceSelectFragmentTest.TestAppComponent appComponent = DaggerSourceSelectFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        SourceSelectFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new SourceSelectFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, SourceSelectFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(SourceSelectFragmentTest.TestPresenterComponent.class), any(SourceSelectFragment.class))).thenReturn(fragmentComponent);

        mTypeSet.clear();
        mTypeSet.add(MediaSourceType.APP_MUSIC);
        mTypeSet.add(MediaSourceType.RADIO);
        mTypeSet.add(MediaSourceType.CD);
        mTypeSet.add(MediaSourceType.PANDORA);
        mTypeSet.add(MediaSourceType.SPOTIFY);
        mMusicAppList = new AppSharedPreference.Application[]{
                new AppSharedPreference.Application(MusicApp.PANDORA.getPackageName(), MusicApp.SPOTIFY.getPackageName()),
        };
        mMessageAppList = new AppSharedPreference.Application[]{
                new AppSharedPreference.Application(MessagingApp.HANGOUTS.getPackageName(), "Hangouts"),
        };
        mNaviApp = new AppSharedPreference.Application(NaviApp.GOOGLE_MAP.getPackageName(), "Google Maps");
        mSourceSelectList.clear();
        for (MediaSourceType type : mTypeSet) {
            mSourceSelectList.add(mAllSourceList.get(type));
        }
        for (AppSharedPreference.Application app : mMessageAppList) {
            mSourceSelectList.add(new SourceSelectItem(app.packageName, app.label));
        }
        mSourceSelectList.add(new SourceSelectItem(mNaviApp.packageName, mNaviApp.label));

    }


    @Test
    public void testSetAdapter() throws Exception {
        mFragmentRule.launchActivity(null);
        SourceSelectFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(UiColor.AQUA.getResource());
            fragment.setAdapter(mSourceSelectList);
            fragment.setCurrentSource(0);
        });
        onView(withId(R.id.text_source)).check(matches(withText(mSourceSelectList.get(0).sourceTypeName)));
    }

    @Test
    public void testSetCurrentSource() throws Exception {
        mFragmentRule.launchActivity(null);
        SourceSelectFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setColor(UiColor.AQUA.getResource());
            fragment.setAdapter(mSourceSelectList);
            fragment.setCurrentSource(0);
            fragment.setCurrentSource(1);
//            fragment.setCurrentSource(2);
//            fragment.setCurrentSource(3);
//            fragment.setCurrentSource(4);
//            fragment.setCurrentSource(5);
//            fragment.setCurrentSource(6);
        });
//        onData(anything()).inAdapterView(withId(R.id.carousel_pager))
//                .atPosition(0).onChildView(withId(R.id.page_content)).perform(click());
//        onView(withText(mSourceSelectList.get(0).sourceTypeName)).perform(click());
//       Thread.sleep(4000);
        onView(withId(R.id.text_source)).check(matches(withText(mSourceSelectList.get(1).sourceTypeName)));
//        verify(mPresenter).onChangeSourceAction(any());
    }

    @Test
    public void testOnClickCustomizeButton() throws Exception {
        mFragmentRule.launchActivity(null);
        SourceSelectFragment fragment = mFragmentRule.getFragment();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.onClickCustomizeButton();
        });
        verify(mPresenter).onCustomizeAction();
    }

}