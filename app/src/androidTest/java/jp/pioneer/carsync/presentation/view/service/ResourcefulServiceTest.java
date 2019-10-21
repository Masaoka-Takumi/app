package jp.pioneer.carsync.presentation.view.service;

import android.app.Instrumentation;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.view.View;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import javax.inject.Singleton;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.Subcomponent;
import jp.pioneer.carsync.NonBindServiceTestRule;
import jp.pioneer.carsync.R;
import jp.pioneer.carsync.application.App;
import jp.pioneer.carsync.application.TestApp;
import jp.pioneer.carsync.application.di.PresenterLifeCycle;
import jp.pioneer.carsync.application.di.component.AppComponent;
import jp.pioneer.carsync.application.di.component.PresenterComponent;
import jp.pioneer.carsync.application.di.component.ServiceComponent;
import jp.pioneer.carsync.application.di.module.AppModule;
import jp.pioneer.carsync.application.di.module.DomainModule;
import jp.pioneer.carsync.application.di.module.InfrastructureBindsModule;
import jp.pioneer.carsync.application.di.module.InfrastructureModule;
import jp.pioneer.carsync.application.di.module.ServiceModule;
import jp.pioneer.carsync.application.factory.ComponentFactory;
import jp.pioneer.carsync.domain.model.AndroidMusicMediaInfo;
import jp.pioneer.carsync.presentation.controller.SongChangeToastController;
import jp.pioneer.carsync.presentation.presenter.ResourcefulPresenter;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ReadNotificationServiceのテスト
 */

public class ResourcefulServiceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @Rule public NonBindServiceTestRule<ResourcefulService> mServiceRule = new NonBindServiceTestRule(ResourcefulService.class);

    @Mock ComponentFactory mComponentFactory;
    @Mock ResourcefulPresenter mPresenter;
    @Mock SongChangeToastController mSongChangeToastController;
    @Mock App mApp;

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
        public ResourcefulPresenter provideReadNotificationPresenter() {
            return mPresenter;
        }

        @Provides
        public SongChangeToastController provideSongChangeToastController() {
            return mSongChangeToastController;
        }

    }

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());
        TestApp testApp = mServiceRule.getTestApp();
        TestAppComponent appComponent = DaggerResourcefulServiceTest_TestAppComponent.builder().build();
        testApp.setAppComponent(appComponent);
        testApp.setComponentFactory(mComponentFactory);
        TestPresenterComponent presenterComponent = appComponent.presenterComponent(new TestPresenterModule());
        ServiceComponent serviceComponent = presenterComponent.serviceComponent(new ServiceModule());
        when(mComponentFactory.getPresenterComponent(appComponent, ResourcefulService.class)).thenReturn(presenterComponent);
        when(mComponentFactory.createServiceComponent(any(TestPresenterComponent.class), any(ResourcefulService.class))).thenReturn(serviceComponent);
    }

    @Test
    public void testLifecycle() throws Exception {
        mServiceRule.startService();

        verify(mSongChangeToastController).setOnToastHiddenListener(any(ResourcefulService.class));

        verify(mPresenter, never()).onExistNotificationAction();
    }

    @Test
    public void testDispatchMessageKey() throws Exception {
        final ArgumentCaptor<ResourcefulService> cap = ArgumentCaptor.forClass(ResourcefulService.class);

        mServiceRule.startService();

        verify(mPresenter).takeView(cap.capture());
        cap.getValue().dispatchMessageKey();

        verify(mPresenter).onMessageKeyAction();
    }

    @Test
    public void testShowSongNotification() throws Exception {
        AndroidMusicMediaInfo mediaInfo = new AndroidMusicMediaInfo();
        mediaInfo.artworkImageLocation = Uri.parse("");
        mediaInfo.songTitle = "song";
        mediaInfo.albumTitle = "test_sample";
        mediaInfo.artistName = "taro";
        mediaInfo.genre = "rock";

        final ArgumentCaptor<ResourcefulService> cap = ArgumentCaptor.forClass(ResourcefulService.class);
        final ArgumentCaptor<View> cap2 = ArgumentCaptor.forClass(View.class);

        mServiceRule.startService();

        verify(mPresenter).takeView(cap.capture());
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            cap.getValue().showSongNotification(mediaInfo);
        });

        verify(mSongChangeToastController).show(any(AndroidMusicMediaInfo.class), cap2.capture());
        View view = cap2.getValue();
        assertThat(((TextView) view.findViewById(R.id.title_text)).getText().toString(), is("song"));
        assertThat(((TextView) view.findViewById(R.id.album_text)).getText().toString(), is("test_sample"));
        assertThat(((TextView) view.findViewById(R.id.artist_text)).getText().toString(), is("taro"));
        assertThat(((TextView) view.findViewById(R.id.genre_text)).getText().toString(), is("rock"));
    }

    @Test
    public void testShowSongNotification2() throws Exception {
        AndroidMusicMediaInfo mediaInfo = new AndroidMusicMediaInfo();
        mediaInfo.artworkImageLocation = Uri.parse("");
        mediaInfo.songTitle = "";
        mediaInfo.albumTitle = "";
        mediaInfo.artistName = "";
        mediaInfo.genre = "";

        final ArgumentCaptor<ResourcefulService> cap = ArgumentCaptor.forClass(ResourcefulService.class);
        final ArgumentCaptor<View> cap2 = ArgumentCaptor.forClass(View.class);

        mServiceRule.startService();

        verify(mPresenter).takeView(cap.capture());
        Instrumentation instr = InstrumentationRegistry.getInstrumentation();
        instr.runOnMainSync(() -> {
            cap.getValue().showSongNotification(mediaInfo);
        });

        verify(mSongChangeToastController).show(any(AndroidMusicMediaInfo.class), cap2.capture());
        View view = cap2.getValue();
        assertThat(((TextView) view.findViewById(R.id.title_text)).getText().toString(), is("No Title"));
        assertThat(((TextView) view.findViewById(R.id.album_text)).getText().toString(), is("No Album"));
        assertThat(((TextView) view.findViewById(R.id.artist_text)).getText().toString(), is("No Artist"));
        assertThat(((TextView) view.findViewById(R.id.genre_text)).getText().toString(), is("No Genre"));
    }

    @Test
    public void testHideSongNotification() throws Exception {
        final ArgumentCaptor<ResourcefulService> cap = ArgumentCaptor.forClass(ResourcefulService.class);

        mServiceRule.startService();

        verify(mPresenter).takeView(cap.capture());
        cap.getValue().hideSongNotification();

        verify(mSongChangeToastController).hideNotification();

    }

    @Test
    public void testOnToastHidden2() throws Exception {
        AndroidMusicMediaInfo mediaInfo = mock(AndroidMusicMediaInfo.class);

        final ArgumentCaptor<ResourcefulService> cap = ArgumentCaptor.forClass(ResourcefulService.class);

        mServiceRule.startService();

        verify(mPresenter).takeView(cap.capture());
        cap.getValue().onToastHidden(mediaInfo);

    }
}
