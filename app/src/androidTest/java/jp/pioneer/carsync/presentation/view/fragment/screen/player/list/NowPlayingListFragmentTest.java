package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Instrumentation;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.MediaStore;
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
import jp.pioneer.carsync.domain.model.PlaybackMode;
import jp.pioneer.carsync.presentation.presenter.NowPlayingListPresenter;
import jp.pioneer.carsync.presentation.view.fragment.dialog.AbstractDialogFragment;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.hamcrest.Matchers.anything;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NowPlayingListの画面のテスト
 */
public class NowPlayingListFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<NowPlayingListFragment> mFragmentRule = new FragmentTestRule<NowPlayingListFragment>() {
        private NowPlayingListFragment mFragment;

        @Override
        protected NowPlayingListFragment createDialogFragment() {
            // HandlerがUIスレッドでインスタンス化しないとだめ
            Instrumentation instr = InstrumentationRegistry.getInstrumentation();
            instr.runOnMainSync(() -> mFragment = new NowPlayingListFragment());
            return mFragment;
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock NowPlayingListPresenter mPresenter;
    @Mock AbstractDialogFragment.Callback mCallback;

    private static final String[] FROM = {MediaStore.Audio.Media._ID,MediaStore.Audio.Media.ARTIST,MediaStore.Audio.Media.TITLE,MediaStore.Audio.Media.ALBUM,MediaStore.Audio.Media.ALBUM_ID,MediaStore.Audio.Media.TRACK,MediaStore.MediaColumns.DATA };
    private MatrixCursor mCursor = new MatrixCursor(FROM);

    @Singleton
    @Component(modules = {
            AppModule.class,
            DomainModule.class,
            InfrastructureModule.class,
            InfrastructureBindsModule.class
    })

    public interface TestAppComponent extends AppComponent {
        NowPlayingListFragmentTest.TestPresenterComponent presenterComponent(NowPlayingListFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = NowPlayingListFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public NowPlayingListPresenter provideNowPlayingListPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        NowPlayingListFragmentTest.TestAppComponent appComponent = DaggerNowPlayingListFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        NowPlayingListFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new NowPlayingListFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, NowPlayingListFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(NowPlayingListFragmentTest.TestPresenterComponent.class), any(NowPlayingListFragment.class))).thenReturn(fragmentComponent);
        mCursor.addRow(new String[] {"0", "ARTIST0","MUSIC0" ,"ALBUM0","0","0",""});
        mCursor.addRow(new String[] {"1", "ARTIST1","MUSIC1" ,"ALBUM1","1","1",""});
        mCursor.moveToFirst();

    }

    @Test
    public void testSetSongCursor() throws Exception {
        mFragmentRule.launchActivity(null);
        final NowPlayingListFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setSongCursor(mCursor,args);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.title_text)).check(matches(withText("MUSIC0")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.subtitle_text)).check(matches(withText("ARTIST0")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.title_text)).check(matches(withText("MUSIC1")));
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(1).onChildView(withId(R.id.subtitle_text)).check(matches(withText("ARTIST1")));
    }

    @Test
    public void testSetNowPlaySong() throws Exception {
        mFragmentRule.launchActivity(null);
        final NowPlayingListFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setSongCursor(mCursor,args);
            fragment.setNowPlaySong(0,0, PlaybackMode.PLAY);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).onChildView(withId(R.id.nowplay_icon)).check(matches(isDisplayed()));
    }

    @Test
    public void testOnClickListItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final NowPlayingListFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setSongCursor(mCursor,args);
        });
        onData(anything()).inAdapterView(withId(R.id.list_view))
                .atPosition(0).perform(click());
        verify(mPresenter).onSongPlayAction(0);
    }

}