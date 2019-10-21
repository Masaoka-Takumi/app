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
import jp.pioneer.carsync.domain.component.CdSourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/01.
 */
public class ControlCdSourceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    ControlCdSource mControlCdSource;
    @Mock Handler mHandler;
    @Mock CarDevice mCarDevice;
    @Mock CdSourceController mCdSourceController;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

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

        when(mCarDevice.getSourceController(eq(MediaSourceType.CD))).thenReturn(mCdSourceController);
        when(mCdSourceController.isActive()).thenReturn(true);

        mControlCdSource = new ControlCdSource(mCarDevice);
        mControlCdSource.mHandler = mHandler;
    }

    @Test
    public void togglePlay() throws Exception {
        // exercise
        mControlCdSource.togglePlay();
        mSignal.await();

        // verify
        verify(mCdSourceController).togglePlay();
    }

    @Test
    public void toggleRepeatMode() throws Exception {
        // exercise
        mControlCdSource.toggleRepeatMode();
        mSignal.await();

        // verify
        verify(mCdSourceController).toggleRepeatMode();
    }

    @Test
    public void toggleShuffleMode() throws Exception {
        // exercise
        mControlCdSource.toggleShuffleMode();
        mSignal.await();

        // verify
        verify(mCdSourceController).toggleShuffleMode();
    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mControlCdSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mCdSourceController).skipNextTrack();
    }

    @Test
    public void skipPreviousTrack() throws Exception {
        // exercise
        mControlCdSource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mCdSourceController).skipPreviousTrack();
    }

    @Test
    public void togglePlay_isInActive() throws Exception {
        // setup
        when(mCdSourceController.isActive()).thenReturn(false);

        // exercise
        mControlCdSource.togglePlay();
        mSignal.await();

        // verify
        verify(mCdSourceController, never()).togglePlay();
    }

    @Test
    public void toggleRepeatMode_isInActive() throws Exception {
        // setup
        when(mCdSourceController.isActive()).thenReturn(false);

        // exercise
        mControlCdSource.toggleRepeatMode();
        mSignal.await();

        // verify
        verify(mCdSourceController, never()).toggleRepeatMode();
    }

    @Test
    public void toggleShuffleMode_isInActive() throws Exception {
        // setup
        when(mCdSourceController.isActive()).thenReturn(false);

        // exercise
        mControlCdSource.toggleShuffleMode();
        mSignal.await();

        // verify
        verify(mCdSourceController, never()).toggleShuffleMode();
    }

    @Test
    public void skipNextTrack_isInActive() throws Exception {
        // setup
        when(mCdSourceController.isActive()).thenReturn(false);

        // exercise
        mControlCdSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mCdSourceController, never()).skipNextTrack();
    }

    @Test
    public void skipPreviousTrack_isInActive() throws Exception {
        // setup
        when(mCdSourceController.isActive()).thenReturn(false);

        // exercise
        mControlCdSource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mCdSourceController, never()).skipPreviousTrack();
    }

}