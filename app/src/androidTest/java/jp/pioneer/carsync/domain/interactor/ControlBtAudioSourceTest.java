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

import jp.pioneer.carsync.domain.component.BtAudioSourceController;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.BtAudioInfo;
import jp.pioneer.carsync.domain.model.MediaSourceType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
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
public class ControlBtAudioSourceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    ControlBtAudioSource mControlBtAudioSource;
    @Mock Handler mHandler;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    CarDevice mCarDevice;
    BtAudioSourceController mBtAudioSourceController;

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
        mBtAudioSourceController = mock(BtAudioSourceController.class);
        when(mCarDevice.getSourceController(eq(MediaSourceType.BT_AUDIO))).thenReturn(mBtAudioSourceController);

        mControlBtAudioSource = new ControlBtAudioSource(mCarDevice);
        mControlBtAudioSource.mHandler = mHandler;

        when(mBtAudioSourceController.isActive()).thenReturn(true);
    }

    @Test
    public void togglePlay() throws Exception {
        // exercise
        mControlBtAudioSource.togglePlay();
        mSignal.await();

        // verify
        verify(mBtAudioSourceController).togglePlay();
    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mControlBtAudioSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mBtAudioSourceController).skipNextTrack();
    }

    @Test
    public void skipPreviousTrack() throws Exception {
        // exercise
        mControlBtAudioSource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mBtAudioSourceController).skipPreviousTrack();
    }

    @Test
    public void togglePlay_isInActive() throws Exception {
        // setup
        when(mBtAudioSourceController.isActive()).thenReturn(false);

        // exercise
        mControlBtAudioSource.togglePlay();
        mSignal.await();

        // verify
        verify(mBtAudioSourceController, never()).togglePlay();
    }

    @Test
    public void skipNextTrack_isInActive() throws Exception {
        // setup
        when(mBtAudioSourceController.isActive()).thenReturn(false);

        // exercise
        mControlBtAudioSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mBtAudioSourceController, never()).skipNextTrack();
    }

    @Test
    public void skipPreviousTrack_isInActive() throws Exception {
        // setup
        when(mBtAudioSourceController.isActive()).thenReturn(false);

        // exercise
        mControlBtAudioSource.skipPreviousTrack();
        mSignal.await();

        // verify
        verify(mBtAudioSourceController, never()).skipPreviousTrack();
    }

}