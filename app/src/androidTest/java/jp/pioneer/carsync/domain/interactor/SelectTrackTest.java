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
import jp.pioneer.carsync.domain.model.MediaSourceType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/07/10.
 */
public class SelectTrackTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    SelectTrack mSelectTrack;

    @Mock Handler mHandler;
    @Mock CarDevice mCarDevice;
    @Mock AppMusicSourceController mAppMusicSourceController;

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
        when(mAppMusicSourceController.isActive()).thenReturn(true);
        when(mCarDevice.getSourceController(eq(MediaSourceType.APP_MUSIC))).thenReturn(mAppMusicSourceController);

        mSelectTrack = new SelectTrack(mCarDevice);
        mSelectTrack.mHandler = mHandler;
    }

    @Test
    public void execute() throws Exception {
        // exercise
        mSelectTrack.execute(1);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController).selectTrack(1);
    }

    @Test
    public void execute_isInactive() throws Exception {
        // setup
        when(mAppMusicSourceController.isActive()).thenReturn(false);

        // exercise
        mSelectTrack.execute(1);
        mSignal.await();

        // verify
        verify(mAppMusicSourceController, never()).selectTrack(anyInt());
    }

}