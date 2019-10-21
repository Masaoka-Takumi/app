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

import jp.pioneer.carsync.domain.component.SmartPhoneInterruptionController;
import jp.pioneer.carsync.domain.model.SmartPhoneInterruption;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by NSW00_008320 on 2018/01/29.
 */
public class UpdateSmartPhoneInterruptionTest {
    @Rule public MockitoRule mMockito = MockitoJUnit.rule();
    @InjectMocks UpdateSmartPhoneInterruption mInterruption;
    @Mock Handler mHandler;
    @Mock SmartPhoneInterruptionController mController;
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
    public void addInterruption() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mInterruption.addInterruption(SmartPhoneInterruption.LOW);
        mSignal.await();

        // verify
        verify(mController).interrupt(SmartPhoneInterruption.LOW);
    }

    @Test
    public void releaseInterruption() throws Exception {
        // setup
        mSignal = new CountDownLatch(1);

        // exercise
        mInterruption.releaseInterruption(SmartPhoneInterruption.LOW);
        mSignal.await();

        // verify
        verify(mController).releaseInterrupt(SmartPhoneInterruption.LOW);
    }

    @Test(expected = NullPointerException.class)
    public void addInterruption_ArgNull() throws Exception {
        // exercise
        mInterruption.addInterruption(null);
    }

    @Test(expected = NullPointerException.class)
    public void releaseInterruption_ArgNull() throws Exception {
        // exercise
        mInterruption.releaseInterruption(null);
    }
}