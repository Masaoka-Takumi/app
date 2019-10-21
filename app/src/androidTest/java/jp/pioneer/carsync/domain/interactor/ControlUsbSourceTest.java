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
import jp.pioneer.carsync.domain.component.UsbSourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/01.
 */
public class ControlUsbSourceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    ControlUsbSource mControlUsbSource;
    @Mock Handler mHandler;
    @Mock CarDevice mCarDevice;
    @Mock UsbSourceController mUsbSourceController;

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

        when(mCarDevice.getSourceController(eq(MediaSourceType.USB))).thenReturn(mUsbSourceController);
        when(mUsbSourceController.isActive()).thenReturn(true);

        mControlUsbSource = new ControlUsbSource(mCarDevice);
        mControlUsbSource.mHandler = mHandler;
    }

    @Test
    public void togglePlay() throws Exception {
        // exercise
        mControlUsbSource.togglePlay();
        mSignal.await();

        // verify
        verify(mUsbSourceController).togglePlay();
    }

    @Test
    public void toggleRepeatMode() throws Exception {
        // exercise
        mControlUsbSource.toggleRepeatMode();
        mSignal.await();

        // verify
        verify(mUsbSourceController).toggleRepeatMode();
    }

    @Test
    public void toggleShuffleMode() throws Exception {
        // exercise
        mControlUsbSource.toggleShuffleMode();
        mSignal.await();

        // verify
        verify(mUsbSourceController).toggleShuffleMode();
    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mControlUsbSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mUsbSourceController).skipNextTrack();
    }

    @Test
    public void skipPreviousTrack() throws Exception {
        // exercise
        mControlUsbSource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mUsbSourceController).skipPreviousTrack();
    }

    @Test
    public void togglePlay_isInActive() throws Exception {
        // setup
        when(mUsbSourceController.isActive()).thenReturn(false);

        // exercise
        mControlUsbSource.togglePlay();
        mSignal.await();

        // verify
        verify(mUsbSourceController, never()).togglePlay();
    }

    @Test
    public void toggleRepeatMode_isInActive() throws Exception {
        // setup
        when(mUsbSourceController.isActive()).thenReturn(false);

        // exercise
        mControlUsbSource.toggleRepeatMode();
        mSignal.await();

        // verify
        verify(mUsbSourceController, never()).toggleRepeatMode();
    }

    @Test
    public void toggleShuffleMode_isInActive() throws Exception {
        // setup
        when(mUsbSourceController.isActive()).thenReturn(false);

        // exercise
        mControlUsbSource.toggleShuffleMode();
        mSignal.await();

        // verify
        verify(mUsbSourceController, never()).toggleShuffleMode();
    }

    @Test
    public void skipNextTrack_isInActive() throws Exception {
        // setup
        when(mUsbSourceController.isActive()).thenReturn(false);

        // exercise
        mControlUsbSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mUsbSourceController, never()).skipNextTrack();
    }

    @Test
    public void skipPreviousTrack_isInActive() throws Exception {
        // setup
        when(mUsbSourceController.isActive()).thenReturn(false);

        // exercise
        mControlUsbSource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mUsbSourceController, never()).skipPreviousTrack();
    }

}