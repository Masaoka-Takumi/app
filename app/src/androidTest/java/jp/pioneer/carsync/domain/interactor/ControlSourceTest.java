package jp.pioneer.carsync.domain.interactor;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.concurrent.CountDownLatch;

import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.CarDeviceStatus;
import jp.pioneer.carsync.domain.model.MediaSourceType;
import jp.pioneer.carsync.domain.model.StatusHolder;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/06/22.
 */
public class ControlSourceTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks ControlSource mControlSource;
    @Mock StatusHolder mStatusHolder;
    @Mock CarDevice mCarDevice;
    @Mock Handler mHandler;

    CarDeviceStatus mCarDeviceStatus;
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

        mCarDeviceStatus = new CarDeviceStatus();
        mCarDeviceStatus.sourceType = MediaSourceType.RADIO;
        when(mStatusHolder.getCarDeviceStatus()).thenReturn(mCarDeviceStatus);
        when(mStatusHolder.isInterrupted()).thenReturn(true);

    }

    @Test
    public void selectSource_HappyPath() throws Exception {
        // exercise
        mControlSource.selectSource(MediaSourceType.APP_MUSIC);
        mSignal.await();

        // verify
        verify(mCarDevice).selectSource(MediaSourceType.APP_MUSIC);
    }

    @Test
    public void selectSource_NotAvailable() throws Exception {
        // exercise
        mControlSource.selectSource(MediaSourceType.RADIO);
        mSignal.await();

        // verify
        verify(mCarDevice, never()).selectSource(any(MediaSourceType.class));
    }

    @Test
    public void selectSource_IsUninterrupted() throws Exception {
        // setup
        when(mStatusHolder.isInterrupted()).thenReturn(false);

        // exercise
        mControlSource.selectSource(MediaSourceType.APP_MUSIC);
        mSignal.await();

        // verify
        verify(mCarDevice).selectSource(MediaSourceType.APP_MUSIC);
    }

    @Test
    public void selectSource_SameSourceType() throws Exception {
        // setup
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;

        // exercise
        mControlSource.selectSource(MediaSourceType.APP_MUSIC);
        mSignal.await();

        // verify
        verify(mCarDevice).selectSource(MediaSourceType.APP_MUSIC);
    }

    @Test
    public void selectSource_NoOperation() throws Exception {
        // setup
        when(mStatusHolder.isInterrupted()).thenReturn(false);
        mCarDeviceStatus.sourceType = MediaSourceType.APP_MUSIC;

        // exercise
        mControlSource.selectSource(MediaSourceType.APP_MUSIC);
        mSignal.await();

        // verify
        verify(mCarDevice, never()).selectSource(any(MediaSourceType.class));
    }

    @Test(expected = NullPointerException.class)
    public void selectSource_ArgNull() throws Exception {
        // exercise
        mControlSource.selectSource(null);
    }

    @Test
    public void changePreviousSource() throws Exception {
        // exercise
        mControlSource.changePreviousSource();
        mSignal.await();

        // verify
        verify(mCarDevice).changePreviousSource();
    }

    @Test
    public void changeNextSource() throws Exception {
        // exercise
        mControlSource.changeNextSource();
        mSignal.await();

        // verify
        verify(mCarDevice).changeNextSource();
    }

}