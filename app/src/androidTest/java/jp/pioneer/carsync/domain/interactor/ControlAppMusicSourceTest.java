package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.AppMusicSourceController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.content.AppMusicContract;
import jp.pioneer.carsync.domain.model.MediaSourceType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/15.
 */
public class ControlAppMusicSourceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    ControlAppMusicSource mControlAppMusicSource;
    @Mock Handler mHandler;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    CarDevice mCarDevice;
    AppMusicSourceController mAppMusicSourceController;

    @Before
    public void setUp() throws Exception {
        System.setProperty("dexmaker.dexcache", getTargetContext().getCacheDir().toString());

        when(mHandler.sendMessageAtTime(any(Message.class), anyLong())).then(invocationOnMock -> {
            Message message = (Message) invocationOnMock.getArguments()[0];
            mMainHandler.post(() -> {
                message.getCallback().run();
                mSignal.countDown();
            });
            return true;
        });

        mCarDevice = mock(CarDevice.class);
        mAppMusicSourceController = mock(AppMusicSourceController.class);
        when(mCarDevice.getSourceController(eq(MediaSourceType.APP_MUSIC))).thenReturn(mAppMusicSourceController);

        mControlAppMusicSource = new ControlAppMusicSource(mCarDevice);
        mControlAppMusicSource.mHandler = mHandler;

        when(mAppMusicSourceController.isActive()).thenReturn(true);
    }

    @Test
    public void play() throws Exception {
        // setup
        AppMusicContract.PlayParams playParams = mock(AppMusicContract.PlayParams.class);

        // exercise
        mControlAppMusicSource.play(playParams);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).play(playParams);
    }

    @Test
    public void play_isActiveFalse() throws Exception {
        // setup
        AppMusicContract.PlayParams playParams = mock(AppMusicContract.PlayParams.class);
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        mControlAppMusicSource.play(playParams);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController,never()).play(playParams);
    }

    @Test(expected = NullPointerException.class)
    public void playArgNull() throws Exception {
        // setup
        AppMusicContract.PlayParams playParams = null;

        // exercise
        mControlAppMusicSource.play(playParams);
    }

    @Test
    public void togglePlay() throws Exception {
        // exercise
        mControlAppMusicSource.togglePlay();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).togglePlay();
    }

    @Test
    public void togglePlay_isActiveFalse() throws Exception {
        // setup
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        mControlAppMusicSource.togglePlay();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController,never()).togglePlay();
    }

    @Test
    public void toggleRepeatMode() throws Exception {
        // exercise
        mControlAppMusicSource.toggleRepeatMode();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).toggleRepeatMode();
    }

    @Test
    public void toggleRepeatMode_isActiveFalse() throws Exception {
        // setup
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        mControlAppMusicSource.toggleRepeatMode();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController,never()).toggleRepeatMode();
    }

    @Test
    public void toggleShuffleMode() throws Exception {
        // exercise
        mControlAppMusicSource.toggleShuffleMode();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).toggleShuffleMode();
    }

    @Test
    public void toggleShuffleMode_isActiveFalse() throws Exception {
        // setup
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        mControlAppMusicSource.toggleShuffleMode();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController,never()).toggleShuffleMode();
    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mControlAppMusicSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).skipNextTrack();
    }

    @Test
    public void skipNextTrack_isActiveFalse() throws Exception {
        // setup
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        mControlAppMusicSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController,never()).skipNextTrack();
    }

    @Test
    public void skipPreviousTrack() throws Exception {
        // exercise
        mControlAppMusicSource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).skipPreviousTrack();
    }

    @Test
    public void skipPreviousTrack_isActiveFalse() throws Exception {
        // setup
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        mControlAppMusicSource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mAppMusicSourceController,never()).skipPreviousTrack();
    }

    @Test
    public void addSpeAnaDataListener() throws Exception {
        // setup
        AppMusicSourceController.OnSpeAnaDataListener listener = mock(AppMusicSourceController.OnSpeAnaDataListener.class);

        // exercise
        mControlAppMusicSource.addSpeAnaDataListener(listener);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).addSpeAnaDataListener(listener);
    }

    @Test(expected = NullPointerException.class)
    public void addSpeAnaDataListener_ArgNull() throws Exception {
        // exercise
        mControlAppMusicSource.addSpeAnaDataListener(null);
    }

    @Test
    public void deleteSpeAnaDataListener() throws Exception {
        // setup
        AppMusicSourceController.OnSpeAnaDataListener listener = mock(AppMusicSourceController.OnSpeAnaDataListener.class);

        // exercise
        mControlAppMusicSource.deleteSpeAnaDataListener(listener);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).deleteSpeAnaDataListener(listener);
    }

    @Test(expected = NullPointerException.class)
    public void deleteSpeAnaDataListener_ArgNull() throws Exception {
        // exercise
        mControlAppMusicSource.deleteSpeAnaDataListener(null);
    }

    @Test
    public void fastForward() throws Exception {
        // exercise
        mControlAppMusicSource.fastForward(100);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).fastForwardForPlayer(100);
    }

    @Test
    public void fastForward_isActiveFalse() throws Exception {
        // setup
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        mControlAppMusicSource.fastForward(100);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController, never()).fastForwardForPlayer(anyInt());
    }

    @Test
    public void rewind() throws Exception {
        // exercise
        mControlAppMusicSource.rewind(100);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).rewindForPlayer(100);
    }

    @Test
    public void rewind_isActiveFalse() throws Exception {
        // setup
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        mControlAppMusicSource.rewind(100);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController, never()).rewindForPlayer(anyInt());
    }
}