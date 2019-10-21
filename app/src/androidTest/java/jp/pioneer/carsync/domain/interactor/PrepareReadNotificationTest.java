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

import jp.pioneer.carsync.application.content.AppSharedPreference;
import jp.pioneer.carsync.domain.component.CarDevice;
import jp.pioneer.carsync.domain.model.ReadingRequestType;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2017/11/17.
 */
public class PrepareReadNotificationTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks PrepareReadNotification mPrepareReadNotification;
    @Mock Handler mHandler;
    @Mock CarDevice mCarDevice;

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
    }

    @Test
    public void start() throws Exception {
        // exercise
        mPrepareReadNotification.start();
        mSignal.await();

        // verify
        verify(mCarDevice).requestReadNotification(ReadingRequestType.START);
    }

    @Test
    public void finish() throws Exception {
        // exercise
        mPrepareReadNotification.finish();
        mSignal.await();

        // verify
        verify(mCarDevice).requestReadNotification(ReadingRequestType.FINISH);
    }

}