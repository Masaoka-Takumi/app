package jp.pioneer.carsync.presentation.view.fragment.screen.player.list;

import android.app.Instrumentation;
import android.database.MatrixCursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.view.View;
import android.widget.AdapterView;

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
import jp.pioneer.carsync.presentation.presenter.ArtistAlbumSongsPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static jp.pioneer.carsync.FragmentTestRule.getTestApp;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ArtistAlbumSongsFragmentのテスト
 */
public class ArtistAlbumSongsFragmentTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public FragmentTestRule<ArtistAlbumSongsFragment> mFragmentRule = new FragmentTestRule<ArtistAlbumSongsFragment>() {
        @Override
        protected ArtistAlbumSongsFragment createDialogFragment() {
            return ArtistAlbumSongsFragment.newInstance(Bundle.EMPTY);
        }
    };
    @Mock ComponentFactory mComponentFactory;
    @Mock ArtistAlbumSongsPresenter mPresenter;
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
        ArtistAlbumSongsFragmentTest.TestPresenterComponent presenterComponent(ArtistAlbumSongsFragmentTest.TestPresenterModule module);
    }

    @PresenterLifeCycle
    @Subcomponent(modules = ArtistAlbumSongsFragmentTest.TestPresenterModule.class)
    public interface TestPresenterComponent extends PresenterComponent {
    }

    @Module
    public class TestPresenterModule {
        public TestPresenterModule() {
        }

        @Provides
        public ArtistAlbumSongsPresenter provideArtistAlbumSongsPresenter() {
            return mPresenter;
        }
    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = getTestApp();
        ArtistAlbumSongsFragmentTest.TestAppComponent appComponent = DaggerArtistAlbumSongsFragmentTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        ArtistAlbumSongsFragmentTest.TestPresenterComponent presenterComponent = appComponent.presenterComponent(new ArtistAlbumSongsFragmentTest.TestPresenterModule());
        FragmentComponent fragmentComponent = presenterComponent.fragmentComponent(new FragmentModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ArtistAlbumSongsFragment.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createFragmentComponent(any(ArtistAlbumSongsFragmentTest.TestPresenterComponent.class), any(ArtistAlbumSongsFragment.class))).thenReturn(fragmentComponent);
        mCursor.addRow(new String[] {"1", "sato" ,"taro","tanaka","1","1",""});
        mCursor.moveToFirst();

    }

    @After
    public void tearDown() throws Exception {
    }
    @Test
    public void testSetAlbumCursor() throws Exception {
        mFragmentRule.launchActivity(null);
        final ArtistAlbumSongsFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setSongCursor(mCursor,args);
//            fragment.setDirectoryPass("pass");
        });
        onView(withText("taro")).check(matches(isDisplayed()));
    }
    @Test
    public void testOnClickListItem() throws Exception {
        mFragmentRule.launchActivity(null);
        final ArtistAlbumSongsFragment fragment = mFragmentRule.getFragment();
        Bundle args = new Bundle();
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            fragment.setSongCursor(mCursor,args);
        });
        onView(withText("taro")).perform(click());
        verify(mPresenter).onArtistAlbumSongPlayAction(anyLong());
    }
}