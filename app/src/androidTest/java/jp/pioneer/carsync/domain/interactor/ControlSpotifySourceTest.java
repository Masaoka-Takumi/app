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

import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.component.SpotifySourceController;
import jp.pioneer.carsync.domain.model.CarDeviceMediaInfoHolder;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.SpotifyMediaInfo;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/05/26.
 */
public class ControlSpotifySourceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    ControlSpotifySource mControlSpotifySource;
    @Mock Handler mHandler;
    @Mock StatusHolder mStatusHolder;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    CarDevice mCarDevice;
    SpotifySourceController mSpotifySourceController;
    SpotifyMediaInfo mSpotifyMediaInfo = new SpotifyMediaInfo();

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
        mSpotifySourceController = mock(SpotifySourceController.class);
        when(mCarDevice.getSourceController(eq(MediaSourceType.SPOTIFY))).thenReturn(mSpotifySourceController);

        mControlSpotifySource = new ControlSpotifySource(mCarDevice);
        mControlSpotifySource.mHandler = mHandler;
        mControlSpotifySource.mStatusHolder = mStatusHolder;

        when(mSpotifySourceController.isActive()).thenReturn(true);
    }

    @Test
    public void togglePlay() throws Exception {
        // exercise
        mControlSpotifySource.togglePlay();
        mSignal.await();

        // verify
        verify(mSpotifySourceController).togglePlay();
    }

    @Test
    public void togglePlay_isActiveFalse() throws Exception {
        // setup
        when(mSpotifySourceController.isActive()).thenReturn(false);

        // exercise
        mControlSpotifySource.togglePlay();
        mSignal.await();

        // verify
        verify(mSpotifySourceController,never()).togglePlay();
    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mControlSpotifySource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mSpotifySourceController).skipNextTrack();
    }

    @Test
    public void skipNextTrack_isActiveFalse() throws Exception {
        // setup
        when(mSpotifySourceController.isActive()).thenReturn(false);

        // exercise
        mControlSpotifySource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mSpotifySourceController,never()).skipNextTrack();
    }

    @Test
    public void skipPreviousTrack() throws Exception {
        // exercise
        mControlSpotifySource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mSpotifySourceController).skipPreviousTrack();
    }

    @Test
    public void skipPreviousTrack_isActiveFalse() throws Exception {
        // setup
        when(mSpotifySourceController.isActive()).thenReturn(false);

        // exercise
        mControlSpotifySource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mSpotifySourceController,never()).skipPreviousTrack();
    }

    @Test
    public void toggleRepeatMode_isPlaying() throws Exception {
        // setup
        mSpotifyMediaInfo.radioPlaying = true;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.toggleRepeatMode();
        mSignal.await();

        // verify
        verify(mSpotifySourceController).toggleRepeatMode();
    }

    @Test
    public void toggleRepeatMode_isNotPlaying() throws Exception {
        // setup
        mSpotifyMediaInfo.radioPlaying = false;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.toggleRepeatMode();

        // verify
        verify(mSpotifySourceController,never()).toggleRepeatMode();
    }

    @Test
    public void toggleRepeatMode_isActiveFalse() throws Exception {
        // setup
        when(mSpotifySourceController.isActive()).thenReturn(false);
        mSpotifyMediaInfo.radioPlaying = true;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.toggleRepeatMode();
        mSignal.await();

        // verify
        verify(mSpotifySourceController,never()).toggleRepeatMode();
    }

    @Test
    public void toggleShuffleMode_isPlaying() throws Exception {
        // setup
        mSpotifyMediaInfo.radioPlaying = true;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.toggleShuffleMode();
        mSignal.await();

        // verify
        verify(mSpotifySourceController).toggleShuffleMode();
    }

    @Test
    public void toggleShuffleMode_isNotPlaying() throws Exception {
        // setup
        mSpotifyMediaInfo.radioPlaying = false;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.toggleShuffleMode();

        // verify
        verify(mSpotifySourceController,never()).toggleShuffleMode();
    }

    @Test
    public void toggleShuffleMode_isActiveFalse() throws Exception {
        // setup
        when(mSpotifySourceController.isActive()).thenReturn(false);
        mSpotifyMediaInfo.radioPlaying = true;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.toggleShuffleMode();
        mSignal.await();

        // verify
        verify(mSpotifySourceController,never()).toggleShuffleMode();
    }

    @Test
    public void setThumbUp_isPlaying() throws Exception {
        // setup
        mSpotifyMediaInfo.radioPlaying = true;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.setThumbUp();
        mSignal.await();

        // verify
        verify(mSpotifySourceController).setThumbUp();
    }

    @Test
    public void setThumbUp_isNotPlaying() throws Exception {
        // setup
        mSpotifyMediaInfo.radioPlaying = false;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.setThumbUp();

        // verify
        verify(mSpotifySourceController,never()).setThumbUp();
    }

    @Test
    public void setThumbUp_isActiveFalse() throws Exception {
        // setup
        when(mSpotifySourceController.isActive()).thenReturn(false);
        mSpotifyMediaInfo.radioPlaying = true;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.setThumbUp();
        mSignal.await();

        // verify
        verify(mSpotifySourceController,never()).setThumbUp();
    }

    @Test
    public void setThumbDown_isPlaying() throws Exception {
        // setup
        mSpotifyMediaInfo.radioPlaying = true;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.setThumbDown();
        mSignal.await();

        // verify
        verify(mSpotifySourceController).setThumbDown();
    }

    @Test
    public void setThumbDown_isNotPlaying() throws Exception {
        // setup
        mSpotifyMediaInfo.radioPlaying = false;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.setThumbDown();

        // verify
        verify(mSpotifySourceController,never()).setThumbDown();
    }

    @Test
    public void setThumbDown_isActiveFalse() throws Exception {
        // setup
        when(mSpotifySourceController.isActive()).thenReturn(false);
        mSpotifyMediaInfo.radioPlaying = true;
        CarDeviceMediaInfoHolder carDeviceMediaInfoHolder = new CarDeviceMediaInfoHolder();
        carDeviceMediaInfoHolder.spotifyMediaInfo = mSpotifyMediaInfo;
        when(mStatusHolder.getCarDeviceMediaInfoHolder()).thenReturn(carDeviceMediaInfoHolder);

        // exercise
        mControlSpotifySource.setThumbDown();
        mSignal.await();

        // verify
        verify(mSpotifySourceController,never()).setThumbDown();
    }

}