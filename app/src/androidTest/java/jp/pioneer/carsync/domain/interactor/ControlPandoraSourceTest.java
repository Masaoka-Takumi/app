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
import jp.pioneer.carsync.domain.component.PandoraSourceController;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.PandoraMediaInfo;

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
public class ControlPandoraSourceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    ControlPandoraSource mControlPandoraSource;
    @Mock Handler mHandler;

    Handler mMainHandler = new Handler(Looper.getMainLooper());
    CountDownLatch mSignal = new CountDownLatch(1);

    CarDevice mCarDevice;
    PandoraSourceController mPandoraSourceController;

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
        mPandoraSourceController = mock(PandoraSourceController.class);
        when(mCarDevice.getSourceController(eq(MediaSourceType.PANDORA))).thenReturn(mPandoraSourceController);

        mControlPandoraSource = new ControlPandoraSource(mCarDevice);
        mControlPandoraSource.mHandler = mHandler;

        when(mPandoraSourceController.isActive()).thenReturn(true);
    }

    @Test
    public void togglePlay() throws Exception {
        // exercise
        mControlPandoraSource.togglePlay();
        mSignal.await();

        // verify
        verify(mPandoraSourceController).togglePlay();
    }

    @Test
    public void skipNextTrack() throws Exception {
        // exercise
        mControlPandoraSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mPandoraSourceController).skipNextTrack();
    }

    @Test
    public void setThumbUp() throws Exception {
        // exercise
        mControlPandoraSource.setThumbUp();
        mSignal.await();

        // verify
        verify(mPandoraSourceController).setThumbUp();
    }

    @Test
    public void setThumbDown() throws Exception {
        // exercise
        mControlPandoraSource.setThumbDown();
        mSignal.await();

        // verify
        verify(mPandoraSourceController).setThumbDown();
    }

    @Test
    public void togglePlay_isInActive() throws Exception {
        // setup
        when(mPandoraSourceController.isActive()).thenReturn(false);

        // exercise
        mControlPandoraSource.togglePlay();
        mSignal.await();

        // verify
        verify(mPandoraSourceController, never()).togglePlay();
    }

    @Test
    public void skipNextTrack_isInActive() throws Exception {
        // setup
        when(mPandoraSourceController.isActive()).thenReturn(false);

        // exercise
        mControlPandoraSource.skipNextTrack();
        mSignal.await();

        // verify
        verify(mPandoraSourceController, never()).skipNextTrack();
    }

    @Test
    public void setThumbUp_isInActive() throws Exception {
        // setup
        when(mPandoraSourceController.isActive()).thenReturn(false);

        // exercise
        mControlPandoraSource.setThumbUp();
        mSignal.await();

        // verify
        verify(mPandoraSourceController, never()).setThumbUp();
    }

    @Test
    public void setThumbDown_isInActive() throws Exception {
        // setup
        when(mPandoraSourceController.isActive()).thenReturn(false);

        // exercise
        mControlPandoraSource.setThumbDown();
        mSignal.await();

        // verify
        verify(mPandoraSourceController, never()).setThumbDown();
    }

}